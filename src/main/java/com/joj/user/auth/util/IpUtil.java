package com.joj.user.auth.util;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/15 17:46
 */

import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Objects;

/**
 * 基于 ip2region 3.3.7 官方新版 API 的 IP 工具类
 *
 * 资源文件建议放置：
 * src/main/resources/ip/ip2region_v4.xdb
 * src/main/resources/ip/ip2region_v6.xdb
 *
 * region 格式：
 * 国家|区域|省份|城市|ISP
 * 例如：
 * 中国|0|广东省|深圳市|电信
 */

/**
 String ip = IpUtil.getClientIp(request);

 String province = IpUtil.getProvince(ip);
 String province = IpUtil.getProvince(request);

 IpUtil.RegionInfo regionInfo = IpUtil.getRegionInfo(ip);
 String ua = IpUtil.getUserAgent(request);
 */
public final class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCAL_IPV4 = "127.0.0.1";
    private static final String LOCAL_IPV6 = "::1";
    private static final String LOCAL_IPV6_FULL = "0:0:0:0:0:0:0:1";

    private static final String V4_XDB_CLASSPATH = "ip/ip2region_v4.xdb";
    private static final String V6_XDB_CLASSPATH = "ip/ip2region_v6.xdb";

    private static final Ip2Region IP2_REGION = initIp2Region();

    private IpUtil() {
    }

    private static InputStream getResourceAsStream(String path) {
        return IpUtil.class.getClassLoader().getResourceAsStream(path);
    }

    private static Ip2Region initIp2Region() {
        try {
            final InputStream v4InputStream = getResourceAsStream(V4_XDB_CLASSPATH);
            final InputStream v6InputStream = getResourceAsStream(V6_XDB_CLASSPATH);

            // 官方 README 说明：
            // 使用 XdbInputStream 主要是方便从 jar 中加载 xdb，
            // 且这种情况下 cachePolicy 只能使用 BufferCache。
            final Config v4Config = v4InputStream == null ? null : Config.custom()
                    .setCachePolicy(Config.BufferCache)
                    .setXdbInputStream(v4InputStream)
                    .asV4();

            final Config v6Config = v6InputStream == null ? null : Config.custom()
                    .setCachePolicy(Config.BufferCache)
                    .setXdbInputStream(v6InputStream)
                    .asV6();

            if (v4Config == null && v6Config == null) {
                throw new IllegalStateException("未找到 ip2region xdb 文件，请检查 resources/ip 目录");
            }

            return Ip2Region.create(v4Config, v6Config);
        } catch (Exception e) {
            throw new RuntimeException("初始化 Ip2Region 失败", e);
        }
    }

    /**
     * 从 HttpServletRequest 中提取客户端 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = firstNonBlankHeader(
                request,
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA"
        );

        if (isBlank(ip)) {
            ip = request.getRemoteAddr();
        }

        if (isBlank(ip)) {
            return null;
        }

        // X-Forwarded-For 可能有多个，取第一个有效值
        if (ip.contains(",")) {
            String[] parts = ip.split(",");
            for (String part : parts) {
                String candidate = normalizeIp(part);
                if (isUsableIp(candidate)) {
                    return candidate;
                }
            }
        }

        return normalizeIp(ip);
    }

    /**
     * 获取 User-Agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return trimToNull(request.getHeader("User-Agent"));
    }

    /**
     * 查询原始 region 字符串
     * 失败返回 null，查不到返回空串时会被归一化为 null
     */
    public static String searchRegion(String ip) {
        String normalizedIp = normalizeIp(ip);
        if (isBlank(normalizedIp)) {
            return null;
        }

        try {
            String region = IP2_REGION.search(stripPort(normalizedIp));
            return isBlank(region) ? null : region;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取地域信息
     */
    public static RegionInfo getRegionInfo(String ip) {
        String normalizedIp = normalizeIp(ip);
        if (isBlank(normalizedIp)) {
            return RegionInfo.unknown(null);
        }

        if (isLocalIp(normalizedIp)) {
            return RegionInfo.local(normalizedIp);
        }

        String region = searchRegion(normalizedIp);
        if (isBlank(region)) {
            return RegionInfo.unknown(normalizedIp);
        }

        String[] parts = region.split("\\|", -1);
        return new RegionInfo(
                normalizedIp,
                normalizeRegionPart(getPart(parts, 0)),
                normalizeRegionPart(getPart(parts, 1)),
                normalizeRegionPart(getPart(parts, 2)),
                normalizeRegionPart(getPart(parts, 3)),
                normalizeRegionPart(getPart(parts, 4))
        );
    }

    /**
     * 直接从 request 解析地域信息
     */
    public static RegionInfo getRegionInfo(HttpServletRequest request) {
        return getRegionInfo(getClientIp(request));
    }

    /**
     * 获取省份
     */
    public static String getProvince(String ip) {
        return getRegionInfo(ip).getProvince();
    }

    /**
     * 直接从 request 获取省份
     */
    public static String getProvince(HttpServletRequest request) {
        return getProvince(getClientIp(request));
    }

    public static boolean isIpv4(String ip) {
        if (isBlank(ip)) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(stripPort(normalizeIp(ip)));
            return !(address instanceof Inet6Address);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isIpv6(String ip) {
        if (isBlank(ip)) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(stripPort(normalizeIp(ip)));
            return address instanceof Inet6Address;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLocalIp(String ip) {
        if (isBlank(ip)) {
            return false;
        }
        String value = normalizeIp(ip);
        return Objects.equals(LOCAL_IPV4, value) || Objects.equals(LOCAL_IPV6, value);
    }

    /**
     * 规范化 IP
     */
    public static String normalizeIp(String ip) {
        if (isBlank(ip)) {
            return null;
        }

        String value = ip.trim();

        if (LOCAL_IPV6_FULL.equals(value)) {
            return LOCAL_IPV6;
        }

        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }

        return value;
    }

    /**
     * 去掉端口
     * 例：
     * 127.0.0.1:8080 -> 127.0.0.1
     * [2408:8000::1]:443 -> 2408:8000::1
     */
    public static String stripPort(String ip) {
        if (isBlank(ip)) {
            return ip;
        }

        String value = ip.trim();

        if (value.startsWith("[") && value.contains("]:")) {
            int right = value.indexOf(']');
            if (right > 0) {
                return value.substring(1, right);
            }
        }

        if (value.contains(".") && value.contains(":")) {
            int lastColon = value.lastIndexOf(':');
            if (lastColon > 0) {
                String host = value.substring(0, lastColon);
                String port = value.substring(lastColon + 1);
                if (port.matches("\\d+")) {
                    return host;
                }
            }
        }

        return normalizeIp(value);
    }

    /**
     * 应用关闭时调用
     */
    public static void close() {
        try {
            IP2_REGION.close();
        } catch (Exception ignored) {
        }
    }

    private static String firstNonBlankHeader(HttpServletRequest request, String... headerNames) {
        for (String headerName : headerNames) {
            String value = trimToNull(request.getHeader(headerName));
            if (isUsableIp(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isUsableIp(String value) {
        return !isBlank(value) && !UNKNOWN.equalsIgnoreCase(value.trim());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String getPart(String[] parts, int index) {
        if (parts == null || index < 0 || index >= parts.length) {
            return null;
        }
        return parts[index];
    }

    private static String normalizeRegionPart(String value) {
        if (isBlank(value) || "0".equals(value)) {
            return "未知";
        }
        return value.trim();
    }

    public static final class RegionInfo {
        private final String ip;
        private final String country;
        private final String area;
        private final String province;
        private final String city;
        private final String isp;

        public RegionInfo(String ip, String country, String area, String province, String city, String isp) {
            this.ip = ip;
            this.country = country;
            this.area = area;
            this.province = province;
            this.city = city;
            this.isp = isp;
        }

        public static RegionInfo unknown(String ip) {
            return new RegionInfo(ip, "未知", "未知", "未知", "未知", "未知");
        }

        public static RegionInfo local(String ip) {
            return new RegionInfo(ip, "本地", "本地", "本地", "本地", "本地");
        }

        public String getIp() {
            return ip;
        }

        public String getCountry() {
            return country;
        }

        public String getArea() {
            return area;
        }

        public String getProvince() {
            return province;
        }

        public String getCity() {
            return city;
        }

        public String getIsp() {
            return isp;
        }

        @Override
        public String toString() {
            return "RegionInfo{" +
                    "ip='" + ip + '\'' +
                    ", country='" + country + '\'' +
                    ", area='" + area + '\'' +
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", isp='" + isp + '\'' +
                    '}';
        }
    }
}
