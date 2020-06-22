package com.android.httpservice.http.util.context;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UpLoadUtil {
    private static Context context = AppclicationContextHelper.createApplicationContext();

    /**
     * 传入文件的uri和服务器需要的smfile 返回一个封装好的part对象
     *
     * @param uri    文件uri
     * @param smfile 服务器对文件定义的类型
     * @return part对象
     */
    public static MultipartBody.Part getFileUriToPart(@NonNull Uri uri, @NonNull String smfile) {
        String[] parameters = Uri2FileInfo(uri);
        if (parameters == null)
            return null;
        //以服务器类型,文件名,文件类型,文件路径获取part对象
        return getPart(smfile, parameters[0], parameters[1], parameters[2]);
    }

    /**
     * 以uri查询数据
     *
     * @param uri
     * @return 返回一个查询完成的对象
     */
    @NonNull
    private static Cursor queryUri(Uri uri) {
        Cursor query = context
                .getContentResolver()
                .query(uri, null, null, null, null);
        if (query != null)
            query.moveToNext();
        return query;
    }

    private static String[] Uri2FileInfo(@NonNull Uri uri) {
        Cursor query = queryUri(uri);
        if (query == null) {
            return null;
        }
        //获取文件的名字,类型,路径
        String fileName = query.getString(query.getColumnIndex("_display_name"));
        String mimeType = query.getString(query.getColumnIndex("mime_type"));
        String filePath = query.getString(query.getColumnIndex("_data"));

        query.close();
        return new String[]{fileName, mimeType, filePath};
    }

    /**
     * 传入文件的uri和服务器需要的smfile 返回一个封装好的RequestBody对象
     *
     * @param uri    文件uri
     * @param smfile 服务器对文件定义的类型
     * @return RequestBody对象
     */
    public static RequestBody getFileUriToRequestBody(@NonNull Uri uri, @NonNull String smfile) {
        String[] parameters = Uri2FileInfo(uri);
        if (parameters == null)
            return null;
        //以服务器类型,文件名,文件类型,文件路径获取part对象
        return getRequestBody(smfile, parameters[0], parameters[1], parameters[2]);
    }

    private static RequestBody getRequestBody(String smfile, String fileName, String mimeType,
                                              String filePath) {
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        smfile,
                        fileName,
                        RequestBody.create(MediaType.parse(mimeType), new File(filePath))
                )
                .build();

    }

    @NonNull
    private static MultipartBody.Part getPart(String smfile, String fileName, String mimeType, String
            filePath) {
        return MultipartBody.Part.createFormData(
                smfile,
                fileName,
                RequestBody.create(MediaType.parse(mimeType), new File(filePath))
        );
    }
}
