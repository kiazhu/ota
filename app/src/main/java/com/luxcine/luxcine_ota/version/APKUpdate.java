package com.luxcine.luxcine_ota.version;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;

import com.google.gson.Gson;
import com.luxcine.luxcine_ota.MyApplication;
import com.luxcine.luxcine_ota.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class APKUpdate {
    private static final String TAG = "APKUpdate";

    private String oldVersion, newVersion, url;
    private com.luxcine.luxcine_ota.version.VersionModel versionModel;

    public static final String api_token = "02adf1aa8df8c3aa9f2fb0026551e709";
    public static final String id = "5f617b7db2eb467dd1a6abed";
    public static final String BASE_URL_FIR = "http://api.bq04.com/apps/latest/"
            + id + "?api_token=" + api_token;

    //香港服务器
    // public static final String BASE_URL_APK_HK = "http://149.129.93.140:8080/otaupdate/xml/update/luxcine_ota.xml";
    //国内服务器
    // public static final String BASE_URL_APK_CN = "http://120.24.53.73:80/otaupdate/xml/update/luxcine_ota.xml";

    //private static final String BASE_URL_APK_HK = "http://149.129.93.140:8080/otaupdate/xml/";
    private static final String BASE_URL_APK_HK = "http://www.chasecolor.com:8080/otaupdate/xml/";
    private static final String BASE_URL_APK_CN = "http://120.24.53.73:80/otaupdate/xml/";

    private String BASE_URL_APK;
    private String launcher;

    private String updateUrl;
    private String md5;
    private String storagemem;
    private int date;
    private String describe;

    private List<ApkData> list;

    public void update() {
        launcher = initLauncher();

        if (launcher.equals("dbos")) {
            BASE_URL_APK = BASE_URL_APK_CN;
        } else if (launcher.equals("atv")) {
            BASE_URL_APK = BASE_URL_APK_HK;
        }

        // BASE_URL_APK = BASE_URL_APK_CN;

        //Log.e(TAG, "update: ------------apk:"+BASE_URL_APK );

        //从本地获取版本号
        getVersionCode();
        //fir.im从网络获取版本号
        //initVersion();
        //服务器从网络获取版本号
        getVersionHK();
    }


    //获取本地软件版本号
    private String getVersionCode() {
        try {
            // 获取软件版本号，对应build.gradle下android:versionCode
            oldVersion = MyApplication.getContext().getPackageManager()
                    .getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionName;
            //Log.e(TAG, "getVersionCode: -------本地版本:" + oldVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return oldVersion;
    }


    public void initVersion() {
        new com.luxcine.luxcine_ota.version.OkHttpUtil().getAsynHttp(BASE_URL_FIR, new OkHttpUtil.OnOkHttpListener() {
            @Override
            public void onSuccess(String response) {
                Log.e(TAG, "onSuccess: ---------" + response);

                versionModel = new Gson().fromJson(response, VersionModel.class);

                if (versionModel != null) {
                    //addAll添加集合
                    //add添加对象
                    //gudideUIList.add(guideUIBean.getData());

                    newVersion = versionModel.getVersionShort();
                    url = versionModel.getInstallUrl();

                    //静默安装
                    silentUpdate();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: -----------" + e.toString());
            }
        });
    }

    //静默升级
    public void silentUpdate() {
        int old1 = Integer.parseInt(oldVersion.replace(".", ""));
        int new1 = Integer.parseInt(newVersion.replace(".", ""));

        Log.e(TAG, "update:------本地版本:" + old1 + ",最新版本:" + new1);

        if (new1 > old1) {
            SilentUpdateAppManager updateAppManager = new SilentUpdateAppManager(MyApplication.getContext(), new1, url);
            updateAppManager.getUpdateMsg();//检查更新
        }
    }


    public void getVersionHK() {
        //在子线程中获取服务器的数据
        Thread thread = new Thread() {
            @Override
            public void run() {
                //String path = BASE_URL_HK;
                String path = BASE_URL_APK + "update/luxcine_ota.xml";
                Log.e(TAG, "run: --------------apk:" + path);
                try {
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        //解析输入流中的数据
                        parseXmlInfo(is);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void parseXmlInfo(InputStream is) {
        /*用pull解析器解析xml文件*/
        //先拿到pull解析器
        XmlPullParser xParser = Xml.newPullParser();
        try {
            xParser.setInput(is, "utf-8");
            //获取事件的类型
            int eventType = xParser.getEventType();
            com.luxcine.luxcine_ota.version.ApkData apkData = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //当事件的开始类型update，代表的是xml文件的数据开始
                        if ("update".equals(xParser.getName())) {
                            list = new ArrayList<ApkData>();
                        } else if ("version".equals(xParser.getName())) {
                            apkData = new com.luxcine.luxcine_ota.version.ApkData();
                        } else if ("date".equals(xParser.getName())) {
                            String date = xParser.nextText();
                            apkData.setDate(date);
                        } else if ("url".equals(xParser.getName())) {
                            String url = xParser.nextText();
                            apkData.setUrl(url);
                        } else if ("md5".equals(xParser.getName())) {
                            String md5 = xParser.nextText();
                            apkData.setMd5(md5);
                        } else if ("storagemem".equals(xParser.getName())) {
                            String storagemem = xParser.nextText();
                            apkData.setStoragemem(storagemem);
                        } else if ("version_code".equals(xParser.getName())) {
                            String version_code = xParser.nextText();
                            apkData.setVersion_code(version_code);
                        } else if ("describe".equals(xParser.getName())) {
                            String describe = xParser.nextText();
                            apkData.setDescribe(describe);
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        //当结束时间是version时，说明一条update已经解析完成，并且加入到集合中
                        if ("version".equals(xParser.getName())) {
                            list.add(apkData);
                        }
                        break;
                }
                eventType = xParser.next();
            }

            /*int max = Integer.parseInt(list.get(0).getDate());
            int min = Integer.parseInt(list.get(0).getDate());

            for (int i = 0; i < list.size(); i++) {
                if (min > Integer.parseInt(list.get(i).getDate())) {
                    min = Integer.parseInt(list.get(i).getDate());
                }
                if (max < Integer.parseInt(list.get(i).getDate())) {
                    max = Integer.parseInt(list.get(i).getDate());
                    date = Integer.parseInt(list.get(i).getDate());
                    url = list.get(i).getUrl();
                    md5 = list.get(i).getMd5();
                    storagemem = list.get(i).getStoragemem();
                    newVersion= list.get(i).getVersion_code();
                    describe = list.get(i).getDescribe();
                }
            }*/
            //Log.e(TAG, "parseXmlInfo: ------min:" + min + ",max:" + max);
            /*Log.e(TAG, "parseXmlInfo: --apk-本地版本:" + oldVersion + ",最新版本:" + newVersion
                    +",date:"+date
                    + ",md5:" + md5 + ",storagemem:" + storagemem + ", updateUrl: " + url);*/

            String strMax = list.get(0).getVersion_code().trim().replace(".", "");
            String strMin = list.get(0).getVersion_code().trim().replace(".", "");

            int max = Integer.parseInt(strMax);
            int min = Integer.parseInt(strMin);


            for (int i = 0; i < list.size(); i++) {
                if (min > Integer.parseInt(list.get(i).getVersion_code().trim().replace(".", ""))) {
                    min = Integer.parseInt(list.get(i).getVersion_code().trim().replace(".", ""));
                }
                if (max < Integer.parseInt(list.get(i).getVersion_code().trim().replace(".", ""))) {
                    max = Integer.parseInt(list.get(i).getVersion_code().trim().replace(".", ""));
                    strMax = list.get(i).getVersion_code().trim();
                }
            }
            int loaclVersionCode = Integer.parseInt(getVersionCode().replace(".", ""));

            Log.e(TAG, "parseXmlInfo: -----:" + loaclVersionCode + "," + max);

            if (max > loaclVersionCode) {
                // url = "http://149.129.93.140:8080/otaupdate/xml/download/zip/luxcine_ota/" + strMax + "/luxcine_ota.apk";
                url = BASE_URL_APK + "download/zip/luxcine_ota/" + strMax + "/luxcine_ota.apk";

                SilentUpdateAppManager updateAppManager = new SilentUpdateAppManager(MyApplication.getContext(), max, url);
                updateAppManager.getUpdateMsg();//检查更新
            }

            Log.e(TAG, "parseXmlInfo: --------apk链接：" + url);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String initLauncher() {
        PackageManager packageManager = MyApplication.getContext().getPackageManager();
        // 创建一个主界面的intent
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 得到包含应用信息的列表
        List<ResolveInfo> ResolveInfos = packageManager.queryIntentActivities(
                intent, 0);
        // 遍历
        for (ResolveInfo ri : ResolveInfos) {
            // 得到包名
            String packageName = ri.activityInfo.packageName;
            if (packageName.equals("com.google.android.youtube.tv")) {
                launcher = "atv";
            } else if (packageName.equals("com.dangbei.leard.leradlauncher.common")
                    || packageName.equals("com.dangbei.mimir.lightos.home")) {
                launcher = "dbos";
            }
        }
        return launcher;
    }
}
