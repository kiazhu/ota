package com.luxcine.luxcine_ota_customized.version;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtil {
    private static final String TAG = "OkHttpUtil";

    String str = null;

    private OkHttpClient mOkHttpClient = new OkHttpClient();

    public static final MediaType MEDIA_TYPE_MARKDOWN
            //= MediaType.parse("text/x-markdown; charset=utf-8");
            = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");


    /**
     * get异步请求
     */
    public void getAsynHttp(String url, final OnOkHttpListener listener) {

        Request.Builder requestBuilder = new Request.Builder().url(url);
        requestBuilder.method("GET", null);
        final Request request = requestBuilder.build();

        Call mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ----------getAsynHttp---------"+e.toString());

                if (listener != null) {
                    listener.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    str = response.cacheResponse().toString();
                    Log.e(TAG, "cache---------------------" + response);
                } else {

                    String str = response.body().string();

                    if (listener != null) {
                        listener.onSuccess(str);
                    }

                }

            }
        });

    }


    /**
     * get异步请求
     */
    public void getAsynHttpAccessToken(String url, final OnOkHttpListener listener) {

        //MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
        //String access_token = myPreferences.getAccessToken();

        Request.Builder requestBuilder = new Request.Builder().url(url)
                .header("X-Renmaituan-Business-App-Name", "BUSINESS_CARD")
                //.header("Authorization", access_token)
                ;
        requestBuilder.method("GET", null);
        final Request request = requestBuilder.build();

        Call mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: -------------------");

                if (listener != null) {
                    listener.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    str = response.cacheResponse().toString();
                    // Log.e(TAG, "cache---" + response);
                } else {

                    String str = response.body().string();

                    if (listener != null) {
                        listener.onSuccess(str);
                    }

                }

            }
        });

    }

    /**
     * get异步请求
     * 多个参数
     */
    public void getAsynHttpAccessTokenMap(String url, Map<String, String> map, final OnOkHttpListener listener) {

        Iterator<String> keys = map.keySet().iterator();
        Iterator<String> values = map.values().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("?");

        for (int i = 0; i < map.size(); i++) {
            String value = null;
            try {
                value = URLEncoder.encode(values.next(), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            stringBuffer.append(keys.next() + "=" + value);
            if (i != map.size() - 1) {
                stringBuffer.append("&");
            }
            //int stringBuffer1 = Log.e("", "stringBuffer", stringBuffer.toString());
        }
        Log.e(TAG, "getAsynHttpAccessTokenMap: ----------------" + (url + stringBuffer.toString()));

        url = url+stringBuffer.toString();


      //Form表单格式的参数传递
        //if (map.size() > 0) {
           /* FormBody.Builder builder = new FormBody.Builder();
            for (String key : map.keySet()) {
                String value = map.get(key);
                builder.add(key, value);
            }
            FormBody formBody = builder.build();*/

            //MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
           // String access_token = myPreferences.getAccessToken();

            Request.Builder requestBuilder = new Request.Builder().url(url)
                    .header("X-Renmaituan-Business-App-Name", "BUSINESS_CARD")
                    //.header("Authorization", access_token)
                    ;

            requestBuilder.method("GET", null);
            final Request request = requestBuilder.build();

            Call mCall = mOkHttpClient.newCall(request);
            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "onFailure: -------------------");

                    if (listener != null) {
                        listener.onFailure(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null != response.cacheResponse()) {
                        str = response.cacheResponse().toString();
                        // Log.e(TAG, "cache---" + response);
                    } else {

                        String str = response.body().string();

                        if (listener != null) {
                            listener.onSuccess(str);
                        }

                    }

                }
            });
      //  }
    }

    /**
     * post异步请求
     * 请求头 X-Renmaituan-Business-App-Name
     */
    public void postAsynHttp(String url, Map<String, String> paramsMap, final OnOkHttpListener listener) {
        //Form表单格式的参数传递
        if (paramsMap.size() > 0) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                String value = paramsMap.get(key);
                builder.add(key, value);
            }
            FormBody formBody = builder.build();

            final Request request = new Request.Builder()
                    .url(url)
                    .header("X-Renmaituan-Business-App-Name", "BUSINESS_CARD")
                    .post(formBody)
                    .build();


            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null != response.cacheResponse()) {
                        str = response.toString();
                        Log.e(TAG, "cache---" + str);
                    } else {

                        String str = response.body().string();
                        if (listener != null) {
                            listener.onSuccess(str);
                            //Log.e(TAG, "onResponse: -------" + str);
                        }

                        if (response.code() == 200) {
                            Log.e(TAG, "success: ------------------------200");


                        } else if (response.code() == 401) {
                            Log.e(TAG, "success: -------------没有权限-----------401");

                        } else if (response.code() == 400) {
                            Log.e(TAG, "success: ------------------------400");
                        } else if (response.code() == 500) {
                            Log.e(TAG, "success: -----------服务器异常，请稍后再试-------------500");
                        }

                    }

                }


            });


        }
    }

    /**
     * post异步请求
     * 请求头 带token
     */
    public void postAsynHttpAccessToken(String url, Map<String, String> paramsMap, final OnOkHttpListener listener) {
        //Form表单格式的参数传递
        if (paramsMap.size() > 0) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                String value = paramsMap.get(key);
                builder.add(key, value);
            }
            FormBody formBody = builder.build();

            //MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
            //String access_token = myPreferences.getAccessToken();

           // Log.e(TAG, "postAsynHttpAccessToken: ---------------" + access_token);

            final Request request = new Request.Builder()
                    .url(url)
                    .header("X-Renmaituan-Business-App-Name", "BUSINESS_CARD")
                   // .header("Authorization", access_token)
                    .post(formBody)
                    .build();


            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(e);

                        Log.e(TAG, "onFailure: ------------------" + e.toString());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        Log.e(TAG, "success: ------------------------200");
                        if (null != response.cacheResponse()) {
                            str = response.toString();
                            Log.e(TAG, "cache---" + str);
                        } else {
                            String str = response.body().string();
                            //Log.e(TAG, "onResponse: ----OkHttpUtil---" + str);
                            if (listener != null) {
                                listener.onSuccess(str);

                            }
                        }

                    } else if (response.code() == 401) {
                        Log.e(TAG, "success: -------------没有权限-----------401");

                    } else if (response.code() == 400) {
                        Log.e(TAG, "success: -------------参数错误-----------400");
                    } else if (response.code() == 500) {
                        Log.e(TAG, "success: -----------服务器异常，请稍后再试-------------500");
                    }

                }


            });


        }
    }

    /**
     * post异步请求
     * 请求头 带token
     */
    public void postAsynHttpAccessTokenJson(String url, Map<String, String> map, final OnOkHttpListener listener) {
        Gson mGson = new Gson();
        String params = mGson.toJson(map);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, params);

        //MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
       // String access_token = myPreferences.getAccessToken();

        //Log.e(TAG, "postAsynHttpAccessToken: ---------------" + access_token);
        final Request request = new Request.Builder()
                .url(url)
                //.header("Authorization", access_token)
                .post(body)
                .build();


        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 200) {
                    Log.e(TAG, "success: ------------------------200");
                    if (null != response.cacheResponse()) {
                        str = response.toString();
                        Log.e(TAG, "cache---" + str);
                    } else {
                        String str = response.body().string();
                        Log.e(TAG, "onResponse: ----OkHttpUtil---" + str);
                        if (listener != null) {
                            listener.onSuccess(str);

                        }
                    }

                } else if (response.code() == 401) {
                    Log.e(TAG, "success: -------------没有权限-----------401");

                } else if (response.code() == 400) {
                    Log.e(TAG, "success: -------------参数错误-----------400");
                } else if (response.code() == 500) {
                    Log.e(TAG, "success: -----------服务器异常，请稍后再试-------------500");
                }

            }


        });

    }


    /**
     * post异步请求
     * 请求头 access_token
     */
    public void postAsynHttpToken(String url, Map<String, String> map, final OnOkHttpListener listener) {
        //请求头参数传递
        if (map.size() > 0) {
            String access_token = null;
            for (String key : map.keySet()) {
                access_token = map.get(key);
            }
            final Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", access_token)
                    .post(RequestBody.create(null, ""))
                    .build();


            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null != response.cacheResponse()) {
                        str = response.toString();
                        Log.e(TAG, "cache---" + str);
                    } else {

                        String str = response.body().string();
                        if (listener != null) {
                            listener.onSuccess(str);
                            //Log.e(TAG, "onResponse: -------" + str);
                        }


                       /* if (response.code() == 200) {
                            Log.e(TAG, "success: ------------------------200");

                        } else if (response.code() == 401) {
                            Log.e(TAG, "success: ------------------------401");

                        } else if (response.code() == 400) {
                            Log.e(TAG, "success: ------------------------400");
                        } else if (response.code() == 500) {
                            Log.e(TAG, "success: -----------服务器异常，请稍后再试-------------500");
                        }*/
                    }

                }


            });


        }
    }

    /**
     * post异步请求
     * 请求头 带token
     */
    public void putAsynHttpAccessToken(String url, Map<String, String> paramsMap, final OnOkHttpListener listener) {
        //Form表单格式的参数传递
        if (paramsMap.size() > 0) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : paramsMap.keySet()) {
                String value = paramsMap.get(key);
                builder.add(key, value);
            }
            FormBody formBody = builder.build();

            //MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
            //String access_token = myPreferences.getAccessToken();

            //Log.e(TAG, "postAsynHttpAccessToken: ---------------" + access_token);

            final Request request = new Request.Builder()
                    .url(url)
                    .header("X-Renmaituan-Business-App-Name", "BUSINESS_CARD")
                    //.header("Authorization", access_token)
                    .put(formBody)
                    .build();


            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (listener != null) {
                        listener.onFailure(e);

                        Log.e(TAG, "onFailure: ------------------" + e.toString());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.code() == 200) {
                        Log.e(TAG, "success: ------------------------200");
                        if (null != response.cacheResponse()) {
                            str = response.toString();
                            Log.e(TAG, "cache---" + str);
                        } else {
                            String str = response.body().string();
                            //Log.e(TAG, "onResponse: ----OkHttpUtil---" + str);
                            if (listener != null) {
                                listener.onSuccess(str);

                            }
                        }

                    } else if (response.code() == 401) {
                        Log.e(TAG, "success: -------------没有权限-----------401");

                    } else if (response.code() == 400) {
                        Log.e(TAG, "success: -------------参数错误-----------400");
                    } else if (response.code() == 500) {
                        Log.e(TAG, "success: -----------服务器异常，请稍后再试-------------500");
                    }

                }


            });


        }
    }



    /* public void doDelete(String url){

     *//*  OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                .sslSocketFactory(OKHttpSafeUtils.getUnSafeSocketFactory(), OKHttpSafeUtils.UnSafeTrustManager)
                .hostnameVerifier(OKHttpSafeUtils.UnSafeHostnameVerifier)
                .build();*//*
        MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
        String access_token = myPreferences.getAccessToken();

        FormBody formBody = new FormBody.Builder().build();
        Request.Builder builder = new Request.Builder().url(url).delete(formBody);
        builder .header("Authorization", access_token);
        Request request = builder.build();

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "删除失败onFailure:------ " +e.getMessage());

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String s = body.toString();
                Log.e(TAG, "删除成功onResponse---------: "+s );
            }
        });

    }*/


    public void doDelete(String url, final OnOkHttpListener listener) {
         /*  OkHttpClient mOkHttpClient = new OkHttpClient().newBuilder()
                .sslSocketFactory(OKHttpSafeUtils.getUnSafeSocketFactory(), OKHttpSafeUtils.UnSafeTrustManager)
                .hostnameVerifier(OKHttpSafeUtils.UnSafeHostnameVerifier)
                .build();*/
        //MyPreferences myPreferences = new MyPreferences(MyApplication.getContext());
        //String access_token = myPreferences.getAccessToken();

        FormBody formBody = new FormBody.Builder().build();
        Request.Builder builder = new Request.Builder().url(url).delete(formBody);
        //builder.header("Authorization", access_token);
        Request request = builder.build();

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "删除失败onFailure:------ " + e.getMessage());

                if (listener != null) {
                    listener.onFailure(e);
                }

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String s = body.toString();
                Log.e(TAG, "删除成功onResponse---------: " + s);

                if (null != response.cacheResponse()) {
                    str = response.toString();
                    Log.e(TAG, "cache---" + str);
                } else {

                    String str = response.body().string();
                    if (listener != null) {
                        listener.onSuccess(str);
                        //Log.e(TAG, "onResponse: -------" + str);
                    }
                }
            }
        });
    }


    /**
     * 异步上传文件
     */
    private void postAsynFile() {
        File file = new File("/sdcard/wangshu.txt");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, response.body().string());
            }
        });
    }


    /**
     * 异步下载文件
     */
    private void downAsynFile() {
        String url = "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg";
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File("/sdcard/wangshu.jpg"));
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.e(TAG, "IOException");
                    e.printStackTrace();
                }

                Log.e(TAG, "文件下载成功");
            }
        });
    }

    private void sendMultipart() {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "wangshu")
                .addFormDataPart("image", "wangshu.jpg",
                        RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/wangshu.jpg")))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + "...")
                .url("https://api.imgur.com/3/image")
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, response.body().string());
            }
        });
    }


    public interface OnOkHttpListener {

        void onSuccess(String response);

        void onFailure(Exception e);

    }

}
