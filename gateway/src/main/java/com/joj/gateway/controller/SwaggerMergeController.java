package com.joj.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/5/15 19:38
 */

@RestController
@RequiredArgsConstructor
public class SwaggerMergeController {

    private static final String DOC_PATH = "/api/v2/api-docs";
    private static final String SERVICE_PREFIX = "service-";

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.application.name:service-gateway}")
    private String gatewayServiceName;

    @GetMapping("/api/v2/api-docs")
    public Mono<Map<String, Object>> apiDocs(ServerHttpRequest request) {
        WebClient webClient = webClientBuilder.build();

        return Flux.fromIterable(discoveryClient.getServices())
                .filter(this::isTargetService)
                .flatMap(serviceName -> fetchServiceDoc(webClient, serviceName))
                .collectList()
                .map(docs -> mergeSwaggerDocs(docs, request));
    }

    private boolean isTargetService(String serviceName) {
        return serviceName != null
                && serviceName.startsWith(SERVICE_PREFIX)
                && !gatewayServiceName.equals(serviceName);
    }

    private Mono<Map<String, Object>> fetchServiceDoc(WebClient webClient, String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

        if (CollectionUtils.isEmpty(instances)) {
            return Mono.empty();
        }

        ServiceInstance instance = instances.get(0);
        String docUrl = buildDocUrl(instance);

        return webClient.get()
                .uri(docUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(doc -> {
                    doc.put("_serviceName", serviceName);
                    doc.put("_docUrl", docUrl);
                    return doc;
                })
                .onErrorResume(ex -> Mono.empty());
    }

    private String buildDocUrl(ServiceInstance instance) {
        URI uri = instance.getUri();
        return uri.toString() + DOC_PATH;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeSwaggerDocs(List<Map<String, Object>> docs, ServerHttpRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("swagger", "2.0");
        result.put("info", buildInfo(docs));
        result.put("host", resolveHost(request));
        result.put("basePath", resolveBasePath(docs));

        List<Object> mergedTags = new ArrayList<>();
        Map<String, Object> tagMap = new LinkedHashMap<>();

        Map<String, Object> mergedPaths = new LinkedHashMap<>();
        Map<String, Object> mergedDefinitions = new LinkedHashMap<>();

        Map<String, Object> xOpenapi = null;

        for (Map<String, Object> doc : docs) {
            String serviceName = String.valueOf(doc.get("_serviceName"));

            List<Object> tags = (List<Object>) doc.get("tags");
            mergeTags(mergedTags, tagMap, tags);

            Map<String, Object> paths = (Map<String, Object>) doc.get("paths");
            mergePaths(mergedPaths, paths, serviceName);

            Map<String, Object> definitions = (Map<String, Object>) doc.get("definitions");
            mergeDefinitions(mergedDefinitions, definitions, serviceName);

            if (xOpenapi == null && doc.get("x-openapi") instanceof Map) {
                xOpenapi = (Map<String, Object>) doc.get("x-openapi");
            }
        }

        result.put("tags", mergedTags);
        result.put("paths", mergedPaths);
        result.put("definitions", mergedDefinitions);

        if (xOpenapi != null) {
            result.put("x-openapi", xOpenapi);
        }

        return result;
    }

    private Map<String, Object> buildInfo(List<Map<String, Object>> docs) {
        if (!CollectionUtils.isEmpty(docs)) {
            Object info = docs.get(0).get("info");
            if (info instanceof Map) {
                return deepCopyMap((Map<String, Object>) info);
            }
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("version", "1.0");
        info.put("title", "接口文档");
        info.put("contact", new LinkedHashMap<String, Object>());
        return info;
    }

    private String resolveBasePath(List<Map<String, Object>> docs) {
        if (!CollectionUtils.isEmpty(docs)) {
            Object basePath = docs.get(0).get("basePath");
            if (basePath != null) {
                return basePath.toString();
            }
        }

        return "/api";
    }

    private String resolveHost(ServerHttpRequest request) {
        String forwardedHost = request.getHeaders().getFirst("X-Forwarded-Host");
        if (forwardedHost != null && forwardedHost.length() > 0) {
            return forwardedHost;
        }

        URI uri = request.getURI();
        if (uri.getRawAuthority() != null && uri.getRawAuthority().length() > 0) {
            return uri.getRawAuthority();
        }

        InetSocketAddress address = request.getHeaders().getHost();
        if (address == null) {
            return "";
        }

        String host = address.getHostString();
        int port = address.getPort();

        if (port > 0) {
            return host + ":" + port;
        }

        return host;
    }

    @SuppressWarnings("unchecked")
    private void mergeTags(List<Object> mergedTags,
                           Map<String, Object> tagMap,
                           List<Object> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return;
        }

        for (Object tagObj : tags) {
            if (!(tagObj instanceof Map)) {
                continue;
            }

            Map<String, Object> tag = (Map<String, Object>) tagObj;
            Object name = tag.get("name");

            if (name == null) {
                continue;
            }

            String tagName = name.toString();

            if (!tagMap.containsKey(tagName)) {
                Map<String, Object> newTag = deepCopyMap(tag);
                tagMap.put(tagName, newTag);
                mergedTags.add(newTag);
            }
        }
    }

    private void mergePaths(Map<String, Object> mergedPaths,
                            Map<String, Object> paths,
                            String serviceName) {
        if (paths == null || paths.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : paths.entrySet()) {
            String path = entry.getKey();

            if (mergedPaths.containsKey(path)) {
                throw new IllegalStateException("Swagger paths 冲突: " + path + ", service=" + serviceName);
            }

            Object value = entry.getValue();

            if (value instanceof Map) {
                mergedPaths.put(path, deepCopyMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                mergedPaths.put(path, deepCopyList((List<Object>) value));
            } else {
                mergedPaths.put(path, value);
            }
        }
    }

    private void mergeDefinitions(Map<String, Object> mergedDefinitions,
                                  Map<String, Object> definitions,
                                  String serviceName) {
        if (definitions == null || definitions.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : definitions.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if (!mergedDefinitions.containsKey(name)) {
                if (value instanceof Map) {
                    mergedDefinitions.put(name, deepCopyMap((Map<String, Object>) value));
                } else if (value instanceof List) {
                    mergedDefinitions.put(name, deepCopyList((List<Object>) value));
                } else {
                    mergedDefinitions.put(name, value);
                }
                continue;
            }

            Object oldValue = mergedDefinitions.get(name);

            if (oldValue == null && value == null) {
                continue;
            }

            if (oldValue != null && oldValue.equals(value)) {
                continue;
            }

            throw new IllegalStateException("Swagger definitions 冲突: " + name + ", service=" + serviceName);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopyMap(Map<String, Object> source) {
        Map<String, Object> target = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof Map) {
                target.put(entry.getKey(), deepCopyMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                target.put(entry.getKey(), deepCopyList((List<Object>) value));
            } else {
                target.put(entry.getKey(), value);
            }
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    private List<Object> deepCopyList(List<Object> source) {
        List<Object> target = new ArrayList<>();

        for (Object value : source) {
            if (value instanceof Map) {
                target.add(deepCopyMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                target.add(deepCopyList((List<Object>) value));
            } else {
                target.add(value);
            }
        }

        return target;
    }
}
