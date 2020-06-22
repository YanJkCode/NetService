package com.android.httpservice.http.util.context;

import android.app.Application;
import android.content.Context;

public class AppclicationContextHelper {

   private static Context context = createApplicationContext();

   /**
    * 获取当前的应用ApplicationContext
    */
   public static final Context createApplicationContext() {
      if (context == null) {
         synchronized (AppclicationContextHelper.class) {
            if (context == null) {
               try {
                  context = (Application) Class
                          .forName("android.app.ActivityThread")
                          .getMethod("currentApplication")
                          .invoke(null, (Object[]) null);
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
      }
      return context;
   }
}
