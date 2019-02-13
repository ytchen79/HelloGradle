package com.cyt.utils.web;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.List;

public class RestTemplateUtils {

    public static RestTemplate get(int connReqTmo, int connTmo, int rmo, String charset) {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(connReqTmo);
        httpRequestFactory.setConnectTimeout(connTmo);
        httpRequestFactory.setReadTimeout(rmo);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        List<HttpMessageConverter<?>> httpMessageConverterList = restTemplate.getMessageConverters();
        for (int i = 0; i < httpMessageConverterList.size(); ++i) {
            if (httpMessageConverterList.get(i) instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(Charset.forName(charset));
                httpMessageConverterList.set(i, stringHttpMessageConverter);
            }
        }
        return restTemplate;
    }


}
