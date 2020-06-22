package com.android.httpservice.http.interceptor;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommonParamsInterceptor implements Interceptor {

   private HashMap<String, String> mParams;

   public CommonParamsInterceptor() {
   }

   public CommonParamsInterceptor(HashMap defRequestHeads) {
      //在构造里创建一个map集合用于存储默认的请求头值
      mParams = defRequestHeads;
   }

   public void addHeads(HashMap<String, String> mParams) {
   }

   @Override
   public Response intercept(Chain chain) throws IOException {
      //在拦截器方法中首先获取请求体对象
      Request request = chain.request();
      //获取请求体的请求方式是GET还是POST
      String method = request.method();
      if (mParams != null) {
         addHeads(mParams);
      } else {
         mParams = new HashMap<>();
         addHeads(mParams);
      }


      //拦截的请求的路径,用于判断是否有拼接请求数据
      String oldUrl = request.url().toString();

      if ("GET".equals(method.toUpperCase())) {//判断当前请求是否是GET
         //创建一个StringBuilder拼接需要的字符串
         StringBuilder stringBuilder = new StringBuilder();
         //首先拼接请求路径
         stringBuilder.append(oldUrl);
         //判断地址中是否有? 有说明已经拼接过请求数据了
         if (oldUrl.contains("?")) {
            //判断?是否在字符串的最后一位如果是就不需要拼接&符,否则说明已经拼接过其他的数据了需要手动拼接&
            if (oldUrl.indexOf("?") == oldUrl.length() - 1) {
            } else {
               stringBuilder.append("&");
            }
         } else {
            //没有?说明没有请求数据 我们手动拼接
            stringBuilder.append("?");
         }
         if (mParams != null && mParams.size() > 0) {
            //把构造里初始化的map集合中的数据取出来拼接到请求地址中
            for (Map.Entry<String, String> entry : mParams.entrySet()) {
               stringBuilder
                       .append(entry.getKey())
                       .append("=")
                       .append(entry.getValue())
                       .append("&");

            }
         }
         //删掉最后一个&符号,因为在拼接集合数据时最后都会拼接一个&然而,在最后一个数据拼接时也会拼接一个&
         if (stringBuilder.indexOf("&") != -1) {
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("&"));
         }
         //得到含有公共参数的新路径.....使用新路径去请求
         String newUrl = stringBuilder.toString();
         //新的路径构建一个新的请求,创建一个新的请求体
         request = request.newBuilder().url(newUrl).build();

      } else if ("POST".equals(method.toUpperCase())) {//判断是不是post请求

         //POST的请求数据是在请求体中的所以取出请求体
         RequestBody requestBody = request.body();
         //判断请求体是不是表单数据
         if (requestBody instanceof FormBody) { // 如果是form 表单提交数据
            //把请求体转换为表单请求体
            FormBody oldBody = (FormBody) requestBody;
            //创建一个新的请求体
            FormBody.Builder builder = new FormBody.Builder();

            //把之前的请求体参数转移到新的请求体中
            for (int i = 0 ; i < oldBody.size() ; i++) {
               //键值对的形式添加
               builder.add(oldBody.name(i), oldBody.value(i));
            }

            if (mParams != null && mParams.size() > 0) {
               //把构造初始化的请求头信息添加到新的请求体中
               for (Map.Entry<String, String> entry : mParams.entrySet()) {
                  builder.add(entry.getKey(), entry.getValue());
               }
            }

            //创建一个新的请求体
            request = request.newBuilder().url(oldUrl).post(builder.build()).build();
         }
      }
      //返回响应体
      return chain.proceed(request);
   }
}
