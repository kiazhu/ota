package com.luxcine.luxcine_ota;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.luxcine.luxcine_ota.version.APKUpdate;


public class NetService extends Service {
    private static final String TAG = "NetService";

    private NetBroadCastReciver netChangedReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        netChangedReceiver = new NetBroadCastReciver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netChangedReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(netChangedReceiver);
        super.onDestroy();
    }

    class NetBroadCastReciver extends BroadcastReceiver {
        private static final String TAG = "NetBroadCastReciver";

        @Override
        public void onReceive(Context context, Intent intent) {
            //如果是在开启wifi连接和有网络状态下
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                    //连接状态
                    Log.e(TAG, "onReceive: ----------网络链接成功-------------");

                    new APKUpdate().update();

                } else {
                    Log.e(TAG, "onReceive: -----------网络链接失败------------");
                }
            }
        }
    }
}
