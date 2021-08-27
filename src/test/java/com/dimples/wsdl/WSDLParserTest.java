package com.dimples.wsdl;


import com.dimples.wsdl.info.ServiceInfo;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WSDLParserTest {

    private List<String> uris = new ArrayList<>();

    @Before
    public void before() {
        uris.add("http://www.webxml.com.cn/webservices/ChinaStockSmallImageWS.asmx?wsdl");
        uris.add("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl");
        uris.add("http://www.webxml.com.cn/WebServices/WeatherWS.asmx?wsdl");
    }

    @Test
    public void parse() {
        for (String uri : uris) {
            ServiceInfo serviceInfo = WSDLParser.parseUri(uri);
            WSDLParser.print(serviceInfo);
        }
    }

    @Test
    public void getOperations() {
    }
}