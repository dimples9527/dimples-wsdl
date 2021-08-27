package com.dimples.wsdl.util;


import com.dimples.wsdl.info.OperationInfo;
import com.dimples.wsdl.info.ParameterInfo;
import com.dimples.wsdl.info.ServiceInfo;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleTypesFactory;
import org.exolab.castor.xml.schema.Structure;
import org.exolab.castor.xml.schema.XMLType;
import org.jdom.input.DOMBuilder;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

@SuppressWarnings("rawtypes")
public class ComponentBuilder {

    WSDLFactory wsdlFactory = null;

    SimpleTypesFactory simpleTypesFactory = null;

    private Vector wsdlTypes = new Vector();

    public final static String DEFAULT_SOAP_ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";

    public ComponentBuilder() {
        try {
            wsdlFactory = WSDLFactory.newInstance();
            simpleTypesFactory = new SimpleTypesFactory();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
        }
    }

    /**
     * 开始解析wsdl
     *
     * @param serviceInfo ServiceInfo
     * @return ServiceInfo
     * @throws WSDLException WSDLException
     */
    public ServiceInfo buildServiceInformation(ServiceInfo serviceInfo) throws WSDLException {

        //下载wsdl
        WSDLReader reader = wsdlFactory.newWSDLReader();

        //解析wsdl 生成Definition
        Definition def = reader.readWSDL(null, serviceInfo.getWsdlLocation());

        String targetNamespace = def.getTargetNamespace();
        serviceInfo.setTargetNamespace(targetNamespace);

        def.getPortTypes();
        //获取schema //从Definition 中生成schema
        wsdlTypes = createSchemaFromTypes(def, serviceInfo);

        //构建服务
        Map services = def.getServices();
        if (services != null) {
            Iterator svcIter = services.values().iterator();
            populateComponent(serviceInfo, (Service) svcIter.next());
        }
        return serviceInfo;
    }

    /**
     * 创建schema集合
     *
     * @param wsdlDefinition Definition
     * @param serviceInfo    ServiceInfo
     * @return Vector
     */
    protected Vector createSchemaFromTypes(Definition wsdlDefinition, ServiceInfo serviceInfo) {
        Vector<Schema> schemas = new Vector<Schema>();
        Element element;
        if (wsdlDefinition.getTypes() != null) {
            Vector schemaExtElem = findExtensibilityElement(wsdlDefinition.getTypes().getExtensibilityElements(), "schema");

            for (int i = 0; i < schemaExtElem.size(); i++) {
                ExtensibilityElement schemaElement = (ExtensibilityElement) schemaExtElem.elementAt(i);
                if (schemaElement != null && schemaElement instanceof UnknownExtensibilityElement) {
                    element = ((UnknownExtensibilityElement) schemaElement).getElement();
                    Schema schema = createSchemaFromType(element, wsdlDefinition, serviceInfo);
                    schemas.add(schema);
                }
            }
        }
        return schemas;
    }

    /**
     * 从Definition 中生成schema
     *
     * @param schemaElement  Element
     * @param wsdlDefinition Definition
     * @return Schema
     */
    private Schema createSchemaFromType(Element schemaElement, Definition wsdlDefinition, ServiceInfo serviceInfo) {
        if (schemaElement == null) {
            System.err.println("Unable to find schema extensibility element in WSDL");
            return null;
        }
        DOMBuilder domBuilder = new DOMBuilder();
        org.jdom.Element jdomSchemaElement = domBuilder.build(schemaElement);
        if (jdomSchemaElement == null) {
            System.err.println("Unable to read schema defined in WSDL");
            return null;
        }
        //获取目标命名空间
        String targetNamespace = wsdlDefinition.getTargetNamespace();
        serviceInfo.setTargetNamespace(targetNamespace);

        Map namespaces = wsdlDefinition.getNamespaces();
        if (namespaces != null && !namespaces.isEmpty()) {
            for (Object o : namespaces.keySet()) {
                String nsPrefix = (String) o;
                String nsURI = (String) namespaces.get(nsPrefix);
                if (nsPrefix != null && nsPrefix.length() > 0) {
                    org.jdom.Namespace nsDecl = org.jdom.Namespace.getNamespace(nsPrefix, nsURI);
                    jdomSchemaElement.addNamespaceDeclaration(nsDecl);
                }
            }
        }
        jdomSchemaElement.detach();
        Schema schema = null;
        try {
            schema = XMLSupport.convertElementToSchema(jdomSchemaElement);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return schema;
    }

    /**
     * 构建 serverInfo
     *
     * @param component ServiceInfo
     * @param service   Service
     * @return ServiceInfo
     */
    @SuppressWarnings("unused")
    private ServiceInfo populateComponent(ServiceInfo component, Service service) {
        QName qName = service.getQName();
        String namespace = qName.getNamespaceURI();
        String name = qName.getLocalPart();

        component.setName(name);

        Map ports = service.getPorts();
        for (Object o : ports.values()) {
            Port port = (Port) o;
            Binding binding = port.getBinding();

            List operations = buildOperations(binding);
            for (Object value : operations) {
                OperationInfo operation = (OperationInfo) value;
                Vector addrElems = findExtensibilityElement(port.getExtensibilityElements(), "address");
                ExtensibilityElement element = (ExtensibilityElement) addrElems.elementAt(0);
                if (element != null && element instanceof SOAPAddress) {
                    SOAPAddress soapAddr = (SOAPAddress) element;
                    operation.setTargetUrl(soapAddr.getLocationURI());
                }
                component.addOperation(operation);
            }
        }
        return component;
    }

    /**
     * 构建方法集合
     *
     * @param binding Binding
     * @return List
     */
    @SuppressWarnings("unchecked")
    private List buildOperations(Binding binding) {
        List operationInfos = new ArrayList();

        List operations = binding.getBindingOperations();

        if (operations != null && !operations.isEmpty()) {

            Vector soapBindingElems = findExtensibilityElement(binding.getExtensibilityElements(), "binding");
            // default
            String style = "document";

            ExtensibilityElement soapBindingElem = (ExtensibilityElement) soapBindingElems.elementAt(0);
            if (soapBindingElem != null && soapBindingElem instanceof SOAPBinding) {
                //SOAPBinding类代表的就是<wsdl:binding>下的子元素:<wsdlsoap:binding元素>
                SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
                style = soapBinding.getStyle();
            }

            for (Object operation : operations) {
                //BindingOperation类代表的就是<wsdl:binding>下的子元素:<wsdlsoap:operation元素>
                BindingOperation oper = (BindingOperation) operation;
                Vector operElems = findExtensibilityElement(oper.getExtensibilityElements(), "operation");
                ExtensibilityElement operElem = (ExtensibilityElement) operElems.elementAt(0);
                //SOAPOperation类代表的就是<wsdlsoap:operation>下的子元素:<wsdlsoap:operation/>
                if (operElem != null && operElem instanceof SOAPOperation) {

                    OperationInfo operationInfo = new OperationInfo(style);

                    buildOperation(operationInfo, oper);

                    operationInfos.add(operationInfo);
                }
            }
        }

        return operationInfos;
    }

    /**
     * 构建方法
     *
     * @param operationInfo OperationInfo
     * @param bindingOper   BindingOperation
     * @return OperationInfo
     */
    @SuppressWarnings("unused")
    private OperationInfo buildOperation(OperationInfo operationInfo, BindingOperation bindingOper) {
        Operation oper = bindingOper.getOperation();
        operationInfo.setTargetMethodName(oper.getName());
        Vector operElems = findExtensibilityElement(bindingOper.getExtensibilityElements(), "operation");
        ExtensibilityElement operElem = (ExtensibilityElement) operElems.elementAt(0);
        if (operElem != null && operElem instanceof SOAPOperation) {
            SOAPOperation soapOperation = (SOAPOperation) operElem;
            operationInfo.setSoapActionUri(soapOperation.getSoapActionURI());
        }
        BindingInput bindingInput = bindingOper.getBindingInput();
        BindingOutput bindingOutput = bindingOper.getBindingOutput();
        Vector bodyElems = findExtensibilityElement(bindingInput.getExtensibilityElements(), "body");
        ExtensibilityElement bodyElem = (ExtensibilityElement) bodyElems.elementAt(0);

        if (bodyElem != null && bodyElem instanceof SOAPBody) {
            SOAPBody soapBody = (SOAPBody) bodyElem;

            List styles = soapBody.getEncodingStyles();
            String encodingStyle = null;

            if (styles != null) {

                encodingStyle = styles.get(0).toString();
            }

            if (encodingStyle == null) {

                encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
            }

            operationInfo.setEncodingStyle(encodingStyle);

            operationInfo.setTargetObjectUri(soapBody.getNamespaceURI());
        }

        Input inDef = oper.getInput();
        if (inDef != null) {
            Message inMsg = inDef.getMessage();
            if (inMsg != null) {
                operationInfo.setInputMessageName(inMsg.getQName().getLocalPart());
                //输入消息的参数构建
                getParameterFromMessage(operationInfo, inMsg, 1);
                //从 bing中获取参数
                operationInfo.setInMessage(inMsg);
            }
        }

        Output outDef = oper.getOutput();

        if (outDef != null) {

            Message outMsg = outDef.getMessage();

            if (outMsg != null) {
                operationInfo.setOutputMessageName(outMsg.getQName()
                        .getLocalPart());
                //输出消息的参数构建
                getParameterFromMessage(operationInfo, outMsg, 2);
                operationInfo.setOutMessage(outMsg);
            }
        }

        return operationInfo;
    }

    /**
     * 从OperationInfo 获取参数
     *
     * @param operationInfo OperationInfo
     * @param msg           Message
     * @param manner        int
     */
    @SuppressWarnings("unused")
    private void getParameterFromMessage(OperationInfo operationInfo,
                                         Message msg, int manner) {
        List msgParts = msg.getOrderedParts(null);
        Schema wsdlType = null;
        Iterator iter = msgParts.iterator();
        while (iter.hasNext()) {
            Part part = (Part) iter.next();
            String targetnamespace = "";
            XMLType xmlType = getXMLType(part, wsdlType, operationInfo);
            if (xmlType != null && xmlType.isComplexType()) {
                buildComplexParameter((ComplexType) xmlType, operationInfo,
                        manner);
            } else {
                String partName = part.getName();
                ParameterInfo parameter = new ParameterInfo();
                parameter.setName(partName);

                QName typeName = part.getTypeName();
                if (typeName == null) {
                    continue;
                }
                parameter.setKind(part.getTypeName().getLocalPart());

                if (manner == 1) {
                    //1表示构建的是操作的输入参数
                    operationInfo.addInParameter(parameter);
                } else {
                    operationInfo.addOutParameter(parameter);
                }
            }
            operationInfo.setWsdlTypes(wsdlTypes);

        }

    }

    /**
     * 递归遍历参数
     *
     * @param type          ComplexType
     * @param operationInfo OperationInfo
     * @param manner        int
     */
    private void buildComplexParameter(ComplexType type, OperationInfo operationInfo, int manner) {
        //XML Schema 规范定义了大量的组件，
        //如schema、complexType、simpleType、group、annotation、include、import、element 和 attribute 等等。
        //particleEnum就是ComplexType下的子元素内容,可以是上面的部分组件组合
        Enumeration particleEnum = type.enumerate();
        //group就是元素(可以是复杂类型)集合
        Group group = null;
        if (!particleEnum.hasMoreElements()) {

        }
        while (particleEnum.hasMoreElements()) {
            Particle particle = (Particle) particleEnum.nextElement();
            if (particle instanceof Group) {
                group = (Group) particle;
                break;
            }
        }
        if (group != null) {

            Enumeration groupEnum = group.enumerate();
            while (groupEnum.hasMoreElements()) {
                //看看此复杂数据类型的每一个Element情况
                Structure item = (Structure) groupEnum.nextElement();
                if (item.getStructureType() == Structure.ELEMENT) {
                    ElementDecl elementDecl = (ElementDecl) item;
					
					/*System.out.println(elementDecl.getName());
					if(elementDecl.getName().contains("schema")){
						continue;
					}*/

                    XMLType xmlType = null;
                    try {
                        xmlType = elementDecl.getType();
                    } catch (Exception e) {
                        continue;//如果类型 无法解析  就结束这次循环
                    }

                    if (xmlType != null && xmlType.isComplexType()) {
                        buildComplexParameter((ComplexType) xmlType, operationInfo, manner);
                    } else {
                        ParameterInfo parameter = new ParameterInfo();
                        parameter.setName(elementDecl.getName());
                        parameter.setKind(elementDecl.getType().getName());
                        if (manner == 1) {
                            operationInfo.addInParameter(parameter);
                        } else {
                            operationInfo.addOutParameter(parameter);
                        }
                    }
                }
            }
        }
    }

    protected XMLType getXMLType(Part part, Schema wsdlType, OperationInfo operationInfo) {
        if (wsdlTypes == null) {
            System.out.println("null is here in the 1 ");
            return null;
        }

        XMLType xmlType = null;

        if (part.getElementName() != null) {
            String elemName = part.getElementName().getLocalPart();
            ElementDecl elemDecl = null;
            for (int i = 0; i < wsdlTypes.size(); i++) {
                wsdlType = (Schema) (wsdlTypes.elementAt(i));

                if (wsdlType == null) {
                    continue;
                }

                String targetNamespace = wsdlType.getTargetNamespace();
                operationInfo.setNamespaceUri(targetNamespace);
                elemDecl = wsdlType.getElementDecl(elemName);
                if (elemDecl != null) {
                    break;
                }

            }
            if (elemDecl != null) {
                xmlType = elemDecl.getType();
            }
        }
        return xmlType;
    }

    /**
     * 获取类型下的 schema
     *
     * @param extensibilityElements List
     * @param elementType           String
     * @return Vector
     */
    private static Vector findExtensibilityElement(List extensibilityElements, String elementType) {
        Vector<ExtensibilityElement> elements = new Vector<ExtensibilityElement>();
        if (extensibilityElements != null) {
			for (Object extensibilityElement : extensibilityElements) {
				ExtensibilityElement element = (ExtensibilityElement) extensibilityElement;
				if (element.getElementType().getLocalPart().equalsIgnoreCase(elementType)) {
					elements.add(element);
				}
			}
        }
        return elements;
    }
}
