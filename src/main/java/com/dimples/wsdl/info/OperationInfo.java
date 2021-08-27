package com.dimples.wsdl.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.wsdl.Message;

import lombok.Data;

/**
 * wsdl定义的方法（操作）
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2021/8/26
 */
@SuppressWarnings("rawtypes")
@Data
public class OperationInfo {

    /**
     * SOAP operation type
     */
    private String operationType = "";

    /**
     * The SOAP encoding style to use.
     */
    private String encodingStyle = "";

    /**
     * The URL where the target object is located.
     */
    private String targetUrl = "";

    /**
     * The namespace URI used for this SOAP operation.
     */
    private String namespaceUri = "";

    /**
     * The URI of the target object to invoke for this SOAP operation.
     */
    private String targetObjectUri = "";

    /**
     * The name used to when making an invocation.
     */
    private String targetMethodName = "";

    /**
     * The input message.
     */
    private String inputMessageText = "";

    /**
     * The output message.
     */
    private String outputMessageText = "";

    /**
     * The name of input message.
     */
    private String inputMessageName = "";

    /**
     * The name of output message.
     */
    private String outputMessageName = "";

    /**
     * The action URI value to use when making a invocation.
     */
    private String soapActionUri = "";

    /**
     * The encoding type "document" vs. "rpc"
     */
    private String style = "document";

    /**
     * 操作所对应的输入参数,一个参数对应一个ParameterInfo类
     */
    private List<ParameterInfo> inParameters = new ArrayList<>();

    /**
     * 操作所对应的输出参数,一个参数对应一个ParameterInfo类
     */
    private List<ParameterInfo> outParameters = new ArrayList<>();

    /**
     * 操作所对应的输入消息
     */
    private Message inMessage;

    /**
     * 操作所对应的输出消息
     */
    private Message outMessage;

    /**
     * 服务所对应的Schemas
     */
    private Vector wsdlTypes;

    private String serviceId;

    public OperationInfo() {
        super();
    }

    public OperationInfo(String style) {
        super();
        setStyle(style);
    }

	public void addInParameter(ParameterInfo parameter) {
		this.inParameters.add(parameter);
	}

	public void addOutParameter(ParameterInfo parameter) {
		this.outParameters.add(parameter);
	}
}
