package com.dimples.wsdl;

import com.dimples.wsdl.info.OperationInfo;
import com.dimples.wsdl.info.ParameterInfo;
import com.dimples.wsdl.info.ServiceInfo;
import com.dimples.wsdl.util.CacheUtil;
import com.dimples.wsdl.util.ComponentBuilder;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * https://github.com/lovercws/wsdl
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2021/8/25
 */
public class WSDLParser {

    private static final Logger log = Logger.getLogger(WSDLParser.class);

    /**
     * 解析wsdl
     * 形如：http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl
     *
     * @param uri String
     * @return ServiceInfo
     */
    public static ServiceInfo parseUri(String uri) {
        WSDLParser parser = new WSDLParser();
        return parser.parse(uri);
    }

    /**
     * 返回方法的集合
     *
     * @param uri String
     * @return List<OperationInfo>
     */
    public static List<OperationInfo> getOperations(String uri) {
        ServiceInfo serviceInfo = parseUri(uri);
        return serviceInfo.getOperation();
    }

    /**
     * 打印方法 参数
     *
     * @param serviceInfo ServiceInfo
     */
    public static void print(ServiceInfo serviceInfo) {
        int i = 0;
        Iterator<OperationInfo> iter = serviceInfo.getOperations();
        System.out.println(serviceInfo.getName() + "提供的操作有:");

        while (iter.hasNext()) {
            i++;
            OperationInfo oper = iter.next();
            System.out.println("操作:" + i + " " + oper.getTargetMethodName());
            List<ParameterInfo> inps = oper.getInParameters();
            List<ParameterInfo> outps = oper.getOutParameters();
            if (inps.size() != 0) {
                System.out.println("输入参数:");
                for (ParameterInfo element : inps) {
                    System.out.println("参数名为:" + element.getName() + "    参数类型为:" + element.getKind());
                }
            }
            if (outps.size() != 0) {
                System.out.println("输出参数:");
                for (ParameterInfo element : outps) {
                    System.out.println("类型为:" + element.getKind());
                }
            }
        }
    }

    /**
     * 解析SOAP wsdl生成ServiceInfo
     *
     * @param uri webservice
     * @return ServiceInfo
     */
    private ServiceInfo parse(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        //缓存中存在 直接获取
        if (CacheUtil.contains(uri)) {
            return CacheUtil.get(uri);
        }
        ServiceInfo serviceInfo = new ServiceInfo();
        ComponentBuilder builder = new ComponentBuilder();
        try {
            serviceInfo.setWsdlLocation(uri);

            serviceInfo = builder.buildServiceInformation(serviceInfo);

            //缓存 搜索结果
            CacheUtil.put(uri, serviceInfo);
        } catch (Exception e) {
            log.error("解析wsdl异常", e);
        }
        return serviceInfo;
    }

	/*public ServiceInfo parser(InputSource inputSource){

	}*/

}
