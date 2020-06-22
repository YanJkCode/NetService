package com.android.httpservice.http;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.httpservice.http.util.context.AppclicationContextHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class NetUtil {
    private static NetUtil netUtil;
    private static Retrofit.Builder builder;
    private static final Context context = AppclicationContextHelper.createApplicationContext();

    /**
     * 请求超时时间默认20秒
     */
    private static final int TIMEOUT = 20;
    private static List<Interceptor> sInterceptor;
    private static List<Interceptor> sNetWorkInterceptor;
    private static int sCacheSize;


    /**
     * 双检锁单例
     *
     * @return
     */
    public static NetUtil getNetUtil() {
        if (netUtil == null) {
            synchronized (NetUtil.class) {
                if (netUtil == null) {
                    netUtil = new NetUtil();
                    initRetrofit();
                }
            }
        }
        return netUtil;
    }


    /**
     * 初始化自定义拦截器
     *
     * @param interceptors
     */
    public static void addInterceptor(Interceptor... interceptors) {
        if (sInterceptor == null) {
            sInterceptor = new ArrayList<>();
            sInterceptor.addAll(Arrays.asList(interceptors));
        } else {
            sInterceptor.addAll(Arrays.asList(interceptors));
        }
    }

    /**
     * 初始化自定义网络拦截器
     *
     * @param netWorkInterceptor
     */
    public static void addNetWorkInterceptor(Interceptor... netWorkInterceptor) {
        if (sNetWorkInterceptor == null) {
            sNetWorkInterceptor = new ArrayList<>();
            sNetWorkInterceptor.addAll(Arrays.asList(netWorkInterceptor));
        } else {
            sNetWorkInterceptor.addAll(Arrays.asList(netWorkInterceptor));
        }
    }

    /**
     * 初始化网络请求对象
     */
    private static void initRetrofit() {
        //初始化 okhttp
        if (sCacheSize == 0) {
            sCacheSize = 1024 * 1024 * 50;
        }
        OkHttpClient okHttpClient = createOkhttp(sCacheSize);
        //初始化retrofit的builder对象
        builder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient);
    }

    /**
     * 设置Ok的缓存大小
     *
     * @param cacheSize
     */
    public static void setCacheSize(int cacheSize) {
        sCacheSize = cacheSize;
    }

    /**
     * 创建okhttp
     */
    private static OkHttpClient createOkhttp(int cacheSize) {
        //缓存
        okhttp3.Cache cache = new okhttp3.Cache(context.getCacheDir(), cacheSize);
        //设置日志拦截器
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        if (isApkInDebug(context)) {
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        } else {
            logInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(cache);
        //判断是否需要添加额外的拦截器
        if (sInterceptor != null && sInterceptor.size() != 0) {
            for (Interceptor interceptor : sInterceptor) {
                if (interceptor != null) {
                    builder.addInterceptor(interceptor);
                }
            }
        }

        //判断是否需要添加额外的网络拦截器
        if (sNetWorkInterceptor != null && sNetWorkInterceptor.size() != 0) {
            for (Interceptor interceptor : sNetWorkInterceptor) {
                if (interceptor != null) {
                    builder.addNetworkInterceptor(interceptor);
                }
            }
        }

        return builder
                .addInterceptor(logInterceptor)
                .addInterceptor(new MyCacheIntercepter())
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();
    }


    /**
     * 发起网络请求
     *
     * @param baseUrl 跟地址
     * @param tClass  请求接口
     */
    public <T> T getService(String baseUrl, Class<T> tClass) {
        return builder.baseUrl(baseUrl).build().create(tClass);
    }

    /**
     * 缓存拦截器类
     */
    static class MyCacheIntercepter implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!isNetworkAvailable()) {
                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
            }

            Response originalResponse = chain.proceed(request);

            if (isNetworkAvailable()) {
                int maxAge = 0;
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public ,max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 15 * 60;
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }

        /**
         * 判断当前网络是否连接
         */
        private static boolean isNetworkAvailable() {
            if (context != null) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                        .CONNECTIVITY_SERVICE);
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null) {
                    return info.isAvailable();
                }
            }
            return false;
        }
    }

    /**
     * 判断当前应用是否是debug状态
     */
    private static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
