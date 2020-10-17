package com.luxcine.luxcine_ota.version;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.luxcine.luxcine_ota.MyApplication;

import java.util.List;

public class DownAPKService extends Service {
    private static final String TAG = "DownAPKService";

    private String oldVersion, newVersion, url;
    private VersionModel versionModel;

    private IntentFilter intentFilter;
    private NetworkBroadcast networkBroadcast;

    public static final String api_token = "02adf1aa8df8c3aa9f2fb0026551e709";
    public static final String id = "5f617b7db2eb467dd1a6abed";
    public static final String BASE_URL = "http://api.bq04.com/apps/latest/"
            + id + "?api_token=" + api_token;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        /**
         * 动态获取权限
         */
       /* if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
             , Manifest.permission.READ_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);

                     try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }*/

        //动态注册
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkBroadcast = new NetworkBroadcast();
        registerReceiver(networkBroadcast, intentFilter);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkBroadcast);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    public void initVersion() {
        new OkHttpUtil().getAsynHttp(BASE_URL, new OkHttpUtil.OnOkHttpListener() {
            @Override
            public void onSuccess(String response) {
                Log.e(TAG, "onSuccess: ---------------------" + response);

                versionModel = new Gson().fromJson(response, VersionModel.class);

                if (versionModel != null) {
                    //addAll添加集合
                    //add添加对象
                    //gudideUIList.add(guideUIBean.getData());

                    newVersion = versionModel.getVersionShort();
                    url = versionModel.getInstallUrl();
                    //url = versionModel.getUpdate_url();

                    Log.e(TAG, "onStartCommand: ---------------" + oldVersion + "---" + newVersion);

                    int old1 = Integer.parseInt(oldVersion.replace(".", ""));
                    int new1 = Integer.parseInt(newVersion.replace(".", ""));

                    if (new1 > old1) {
                        SilentUpdateAppManager updateAppManager = new SilentUpdateAppManager(MyApplication.getContext(), new1, url);
                        updateAppManager.getUpdateMsg();//检查更新
                    }

                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: ---------------------" + e.toString());
            }
        });
    }

    //获取本地软件版本号
    private String getVersionCode() {
        try {
            // 获取软件版本号，对应build.gradle下android:versionCode
            oldVersion = MyApplication.getContext().getPackageManager().getPackageInfo(
                    getPackageName(), 0).versionName;

            Log.e(TAG, "getVersionCode: ----------------------" + oldVersion);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return oldVersion;
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;

    }


    //监测网络广播
    class NetworkBroadcast extends BroadcastReceiver {
        private static final String TAG = "NetworkBroadcast";


        @Override
        public void onReceive(Context context, Intent intent) {
            //得到网络连接管理器
            ConnectivityManager connectionManager = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            //通过管理器得到网络实例
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            //判断是否连接
            if (networkInfo != null && networkInfo.isAvailable()) {
                Log.e(TAG, "onReceive: ------网络已连接---------------");

                //判断是否在前台
                boolean b = isApplicationBroughtToBackground(MyApplication.getContext());
                Log.e(TAG, "onCreate: ---是否在前台-------------" + b);

                if (!b) {//如果在前台
                    return;
                }


                //从本地获取版本号
                getVersionCode();

                //从网络获取版本号
                initVersion();

            } else {
                Log.e(TAG, "onReceive: ------网络未连接---------------");
            }
        }
    }


}
