package com.dimples.wsdl.info;

import org.exolab.castor.xml.schema.Schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Data;

/**
 * 服务
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2021/8/26
 */
@Data
public class ServiceInfo {

    private String name;

	/**
	 * wsdl url地址
	 */
	private String wsdlLocation;

    private String endpoint;

	/**
	 * 目标命名空间
	 */
	private String targetNamespace;

    private Schema wsdlType;

    /**
     * The list of operations that this service defines.
     */
    List<OperationInfo> operations = new ArrayList<>();

	public void addOperation(OperationInfo operation) {
		operations.add(operation);
	}
	public List<OperationInfo> getOperation() {
		return operations;
	}

	public Iterator<OperationInfo> getOperations() {
		return operations.iterator();
	}
}
