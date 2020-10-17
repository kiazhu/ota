package com.luxcine.luxcine_ota;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.luxcine.luxcine_ota.utils.SkyFileOperator;
import com.luxcine.luxcine_ota.version.APKUpdate;
import com.luxcine.luxcine_ota.version.OkHttpUtil;
import com.luxcine.luxcine_ota.version.SilentUpdateAppManager;
import com.luxcine.luxcine_ota.version.VersionModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
            Log.e(TAG, "----luxcine_ota-------BootBroadcastReceiver onReceive(), Do thing!");

            //获取当前时间
            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            Log.e(TAG, "--------111---------" + sdf.format(date));*/

            //启动服务
            intent = new Intent(context, NetService.class);
            context.startService(intent);

            //解除HOME按键屏蔽.
            SystemProperties.set("persist.is.burning.video", "0");

            //增加开机检查HDCP的KEY是否烧录成功
            Log.e(TAG, "BootBroadcastReceiver  key len:"+SkyFileOperator.readFromNandkey("hdcp14_rx").length());
            if(SkyFileOperator.readFromNandkey("hdcp14_rx").length() < 200) {
                addHDCPSupport();
                Log.e(TAG, "BootBroadcastReceiver---key len:"+SkyFileOperator.readFromNandkey("hdcp14_rx").length());
            }

        }
    }

    public String addHDCPSupport(){
        String result = "";
        try {
            InputStream in = MyApplication.getContext().getResources().getAssets().open("key");
            int lenght = in.available();
            byte[]  buffer = new byte[lenght];
            in.read(buffer);

            if (!SkyFileOperator.writeStringToFileWithoutSync("/sys/class/unifykeys/name", "hdcp14_rx")){
                Log.e(TAG, "write  error: hdcp14_rx");
                return null;
            }

            File file = new File("/sys/class/unifykeys/write");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return result;
    }


}

