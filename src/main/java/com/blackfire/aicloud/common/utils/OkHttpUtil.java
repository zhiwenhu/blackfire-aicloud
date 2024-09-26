package com.blackfire.aicloud.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Builder;
import lombok.ToString;
import okhttp3.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zhaominglei on 2020/10/19.
 */
public class OkHttpUtil {

    private final static Logger log = LoggerFactory.getLogger(OkHttpUtil.class);

    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";
    public final static String PATCH = "PATCH";

    private final static String UTF8 = "UTF-8";
    private final static String GBK = "GBK";

    private final static String DEFAULT_CHARSET = UTF8;
    private final static String DEFAULT_METHOD = GET;
    private final static String DEFAULT_MEDIA_TYPE = ContentType.APPLICATION_JSON.getMimeType();
    private final static boolean DEFAULT_LOG = false;
    private static OkHttpClient CLIENT;

    /**
     * 默认信任所有的证书
     * TODO 最好加上证书认证，主流App都有自己的证书
     *
     * @return
     */
    static {
        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        CLIENT = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS).build();
    }

    /**
     * GET请求
     *
     * @param url URL地址
     * @return
     */
    public static String get(String url) {
        return execute(OkHttp.builder().url(url).build());
    }

    /**
     * GET请求
     *
     * @param url URL地址
     * @return
     */
    public static String get(String url, String charset) {
        return execute(OkHttp.builder().url(url).responseCharset(charset).build());
    }

    /**
     * 带查询参数的GET查询
     *
     * @param url      URL地址
     * @param queryMap 查询参数
     * @return
     */
    public static String get(String url, Map<String, String> queryMap) {
        return execute(OkHttp.builder().url(url).queryMap(queryMap).build());
    }

    /**
     * 带查询参数的GET查询
     *
     * @param url      URL地址
     * @param queryMap 查询参数
     * @return
     */
    public static String get(String url, Map<String, String> queryMap, String charset) {
        return execute(OkHttp.builder().url(url).queryMap(queryMap).responseCharset(charset).build());
    }

    /**
     * POST
     * application/json
     *
     * @param url
     * @param obj
     * @return
     */
    public static String postJson(String url, Object obj) {
        return execute(OkHttp.builder().url(url).method(POST).data(JSON.toJSONString(obj)).mediaType(ContentType.APPLICATION_JSON.getMimeType()).build());
    }

    /**
     * POST
     * application/x-www-form-urlencoded
     *
     * @param url
     * @param formMap
     * @return
     */
    public static String postForm(String url, Map<String, String> formMap) {
        String data = "";
        if (MapUtils.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType()).build());
    }

    /**
     * POST
     * application/x-www-form-urlencoded
     *
     * @param url
     * @param formMap
     * @return
     */
    public static String postForm(String url, Map<String, String> headerMap, Map<String, String> formMap) {
        String data = "";
        if (MapUtils.isNotEmpty(formMap)) {
            data = formMap.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors
                    .joining("&"));
        }
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType()).headerMap(headerMap).build());
    }

    /**
     * POST
     * application/json
     *
     * @param url
     * @param obj
     * @return
     */
    public static String postJson(String url, Map<String, String> headerMap, Object obj) {
        return execute(OkHttp.builder().url(url).method(POST).data(JSON.toJSONString(obj)).mediaType(ContentType.APPLICATION_JSON.getMimeType()).headerMap(headerMap).build());
    }

    private static String post(String url, String data, String mediaType, String charset) {
        return execute(OkHttp.builder().url(url).method(POST).data(data).mediaType(mediaType).responseCharset(charset).build());
    }

    /**
     * 通用执行方法
     */
    private static String execute(OkHttp okHttp) {
        if (StringUtils.isBlank(okHttp.requestCharset)) {
            okHttp.requestCharset = DEFAULT_CHARSET;
        }
        if (StringUtils.isBlank(okHttp.responseCharset)) {
            okHttp.responseCharset = DEFAULT_CHARSET;
        }
        if (StringUtils.isBlank(okHttp.method)) {
            okHttp.method = DEFAULT_METHOD;
        }
        if (StringUtils.isBlank(okHttp.mediaType)) {
            okHttp.mediaType = DEFAULT_MEDIA_TYPE;
        }
        //记录请求日志
        if (okHttp.requestLog) {
            log.info(okHttp.toString());
        }

        String url = okHttp.url;

        Request.Builder builder = new Request.Builder();

        if (MapUtils.isNotEmpty(okHttp.queryMap)) {
            String queryParams = okHttp.queryMap.entrySet().stream()
                    .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining("&"));
            url = String.format("%s%s%s", url, url.contains("?") ? "&" : "?", queryParams);
        }
        builder.url(url);

        if (MapUtils.isNotEmpty(okHttp.headerMap)) {
            okHttp.headerMap.forEach(builder::addHeader);
        }

        String method = okHttp.method.toUpperCase();
        String mediaType = String.format("%s;charset=%s", okHttp.mediaType, okHttp.requestCharset);

        if (StringUtils.equals(method, GET)) {
            builder.get();
        } else if (ArrayUtils.contains(new String[]{POST, PUT, DELETE, PATCH}, method)) {
            RequestBody requestBody = RequestBody.create(MediaType.parse(mediaType), okHttp.data);
            builder.method(method, requestBody);
        } else {
            try {
                throw new Exception(String.format("http method:%s not support!", method));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String result = "";
        try {
            Response response = CLIENT.newCall(builder.build()).execute();
            byte[] bytes = response.body().bytes();
            result = new String(bytes, okHttp.responseCharset);

            setCookie(url, response.headers());
            //记录返回日志
            if (okHttp.responseLog) {
                log.info(String.format("Got response->%s", result));
            }
        } catch (Exception e) {
            log.error(okHttp.toString(), e);
        }
        return result;
    }

    private static volatile HashMap<String, String> cookieStore = new HashMap<>();

    private static void setCookie(String url, Headers headers) {
        List<String> cookies = headers.values("Set-Cookie");
        String cookie = Strings.join(cookies, ';').replace("Path=/; HttpOnly;", "").replace("Path=/", "");
        //System.out.println(cookie);
        cookieStore.put(url, cookie);
    }

    public static String getCookie(String url) {
        return cookieStore.get(url);
    }

    @Builder
    @ToString(exclude = {"requestCharset", "responseCharset", "requestLog", "responseLog"})
    static class OkHttp {
        private String url;
        private String method = DEFAULT_METHOD;
        private String data;
        private String mediaType = DEFAULT_MEDIA_TYPE;
        private Map<String, String> queryMap;
        private Map<String, String> headerMap;
        private String requestCharset = DEFAULT_CHARSET;
        private boolean requestLog = DEFAULT_LOG;

        private String responseCharset = DEFAULT_CHARSET;
        private boolean responseLog = DEFAULT_LOG;
        private static final MediaType CONTENT_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    }
}
