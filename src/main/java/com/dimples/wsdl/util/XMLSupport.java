package com.dimples.wsdl.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.exolab.castor.xml.schema.writer.SchemaWriter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2021/8/26
 */
public class XMLSupport {
    private XMLSupport() {
    }

    public static String outputString(Document doc) {
        XMLOutputter xmlWriter = new XMLOutputter("    ", true);

        return xmlWriter.outputString(doc);
    }

    public static String outputString(Element elem) {
        XMLOutputter xmlWriter = new XMLOutputter("    ", true);
        return xmlWriter.outputString(elem);
    }

    public static Document readXML(String xml) throws JDOMException {
        return readXML(new StringReader(xml));
    }

    public static Document readXML(Reader reader) throws JDOMException {
        SAXBuilder xmlBuilder = new SAXBuilder(false);

        Document doc = xmlBuilder.build(reader);

        return doc;
    }

    public static Schema readSchema(Reader reader) throws IOException {

        InputSource inputSource = new InputSource(reader);

        SchemaReader schemaReader = new SchemaReader(inputSource);
        schemaReader.setValidation(false);

        Schema schema = schemaReader.read();

        return schema;
    }

    public static Element convertSchemaToElement(Schema schema) throws SAXException, IOException, JDOMException {

        String content = outputString(schema);

        if (content != null) {

            Document doc = readXML(new StringReader(content));

            return doc.getRootElement();
        }

        return null;
    }

    public static Schema convertElementToSchema(Element element) throws IOException {
        String content = outputString(element);
        if (content != null) {
            return readSchema(new StringReader(content));
        }
        return null;
    }

    public static String outputString(Schema schema) throws IOException, SAXException {
        StringWriter writer = new StringWriter();
        SchemaWriter schemaWriter = new SchemaWriter(writer);
        schemaWriter.write(schema);
        return writer.toString();
    }

    /**
     * 构建参数
     *
     * @param nameSpace    String
     * @param methodName   String
     * @param parameterMap Map<String, Object>
     * @return OMElement
     */
    public static OMElement createParameterElement(String nameSpace, String methodName, Map<String, Object> parameterMap) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(nameSpace, "");
        OMElement method = fac.createOMElement(methodName, omNs);

        for (Entry<String, Object> entry : parameterMap.entrySet()) {
            OMElement param = fac.createOMElement(entry.getKey(), omNs);
            param.addChild(fac.createOMText(param, String.valueOf(entry.getValue())));
            method.addChild(param);
        }
        method.build();
        return method;
    }
}
