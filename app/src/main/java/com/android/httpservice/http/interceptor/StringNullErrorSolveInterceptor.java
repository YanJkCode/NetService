//package com.android.httpservice.http.interceptor;

/**
 * 用于处理返回对象是String而我们所需的是一个对象导致的异常
 */
//public class StringNullErrorSolveInterceptor implements Interceptor {
//@Override
//public Response intercept(Chain chain) throws IOException {
//   Response response = chain.proceed(chain.request());
//   ResponseBody body = response.body();
//   MediaType contentType = body.contentType();
//   if (!contentType.toString().toLowerCase().contains("json")) {
//      return response;
//   }
//   String json = body.string();
//   Gson gson = new Gson();
//   HttpResult httpResult = gson.fromJson(json, HttpResult.class);
//   if (httpResult.getData() instanceof String) {
//      httpResult.setData(null);
//   }
//   String newjson = gson.toJson(httpResult);
//
//   Response build = response.newBuilder()
//           .body(ResponseBody.create(contentType, newjson))
//           .build();
//   return build;
//}
//}
