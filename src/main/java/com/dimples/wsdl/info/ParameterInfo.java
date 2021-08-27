package com.dimples.wsdl.info;

import lombok.Data;

/**
 * 一个方法对应的参数
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2021/8/26
 */
@Data
public class ParameterInfo {

	/**
	 * 参数名
	 */
    private String name;
	/**
	 * 参数类型
	 */
    private String kind;
	/**
	 * 参数标识
	 */
    private int id;
	/**
	 * 参数值
	 */
    private String value;
	/**
	 * 服务id
	 */
    private String serviceId;
	/**
	 * 操作名
	 */
    private String operationName;

    private String inputType = null;

    private String type;

}
