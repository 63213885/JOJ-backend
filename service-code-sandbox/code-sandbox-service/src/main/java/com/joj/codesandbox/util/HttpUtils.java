package com.joj.codesandbox.util;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/5 21:02
 */

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求工具类
 * <p>
 * 适合用于：
 * 1. 调用远程代码沙箱
 * 2. 调用内部微服务
 * 3. 调用简单第三方接口
 * <p>
 * Java 8 可用。
 */
public class HttpUtils {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private HttpUtils() {
    }

    /**
     * GET 请求，无 Header
     */
    public static <T> T get(String url, Class<T> responseType) {
        return get(url, null, responseType);
    }

    /**
     * GET 请求，带 Header
     */
    public static <T> T get(String url, Map<String, String> headersMap, Class<T> responseType) {
        HttpHeaders headers = buildHeaders(headersMap);

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<T> responseEntity = REST_TEMPLATE.exchange(
                url,
                HttpMethod.GET,
                entity,
                responseType
        );

        return responseEntity.getBody();
    }

    /**
     * POST 请求，无 Header
     */
    public static <T> T post(String url, Object body, Class<T> responseType) {
        return post(url, body, null, responseType);
    }

    /**
     * POST 请求，带 Header
     */
    public static <T> T post(String url, Object body, Map<String, String> headersMap, Class<T> responseType) {
        HttpHeaders headers = buildHeaders(headersMap);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<Object>(body, headers);

        ResponseEntity<T> responseEntity = REST_TEMPLATE.exchange(
                url,
                HttpMethod.POST,
                entity,
                responseType
        );

        return responseEntity.getBody();
    }

    /**
     * PUT 请求，无 Header
     */
    public static <T> T put(String url, Object body, Class<T> responseType) {
        return put(url, body, null, responseType);
    }

    /**
     * PUT 请求，带 Header
     */
    public static <T> T put(String url, Object body, Map<String, String> headersMap, Class<T> responseType) {
        HttpHeaders headers = buildHeaders(headersMap);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<Object>(body, headers);

        ResponseEntity<T> responseEntity = REST_TEMPLATE.exchange(
                url,
                HttpMethod.PUT,
                entity,
                responseType
        );

        return responseEntity.getBody();
    }

    /**
     * DELETE 请求，无 Header
     */
    public static <T> T delete(String url, Class<T> responseType) {
        return delete(url, null, responseType);
    }

    /**
     * DELETE 请求，带 Header
     */
    public static <T> T delete(String url, Map<String, String> headersMap, Class<T> responseType) {
        HttpHeaders headers = buildHeaders(headersMap);

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);

        ResponseEntity<T> responseEntity = REST_TEMPLATE.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                responseType
        );

        return responseEntity.getBody();
    }

    /**
     * 快速构造 headers。
     * <p>
     * 用法：
     * HttpUtils.headers("auth", "jzz")
     * HttpUtils.headers("auth", "jzz", "token", "abc")
     */
    public static Map<String, String> headers(String... keyValues) {
        Map<String, String> map = new HashMap<String, String>();

        if (keyValues == null || keyValues.length == 0) {
            return map;
        }

        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("headers 参数必须是 key-value 成对出现");
        }

        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }

        return map;
    }

    /**
     * 构造 Header
     */
    private static HttpHeaders buildHeaders(Map<String, String> headersMap) {
        HttpHeaders headers = new HttpHeaders();

        if (headersMap == null || headersMap.isEmpty()) {
            return headers;
        }

        for (Map.Entry<String, String> entry : headersMap.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }

        return headers;
    }
}
