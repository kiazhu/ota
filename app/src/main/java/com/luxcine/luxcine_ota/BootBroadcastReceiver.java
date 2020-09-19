package com.luxcine.luxcine_ota_customized;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.luxcine.luxcine_ota_customized.version.APKUpdate;
import com.luxcine.luxcine_ota_customized.version.OkHttpUtil;
import com.luxcine.luxcine_ota_customized.version.SilentUpdateAppManager;
import com.luxcine.luxcine_ota_customized.version.VersionModel;

//监听开机广播
public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";

    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    private Context mContext;




    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals(ACTION_BOOT)) { //开机启动完成后，要做的事情
            Log.e(TAG, "----luxcine_ota_customized--------BootBroadcastReceiver onReceive(), Do thing!");

            //获取当前时间
            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            Log.e(TAG, "--------111---------" + sdf.format(date));*/

            //启动服务
            intent = new Intent(context, NetService.class);
            context.startService(intent);

        }
    }
}

