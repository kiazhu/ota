package com.luxcine.luxcine_ota;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;

import com.luxcine.luxcine_ota.utils.Constants;
import com.luxcine.luxcine_ota.utils.OkHttpUtil;
import com.luxcine.luxcine_ota.utils.SkyFileOperator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String usid = SkyFileOperator.readFromNandkey("usid");
        int version = Integer.parseInt(SystemProperties.get("ro.build.version.incremental", ""));
        String productModel = SystemProperties.get("ro.build.model", "");
        Log.e(TAG, "onCreate:------" + usid + ":" + version + ":" + ":" + productModel);

        getNewVersion();
    }

    public void getNewVersion(){
        new OkHttpUtil().getAsynHttp(Constants.BASE_URL, new OkHttpUtil.OnOkHttpListener() {
            @Override
            public void onSuccess(String response) {
                Log.e(TAG, "onSuccess: ------------" + response);
                try {
                    JSONObject object = new JSONObject(response);
                    int code = object.getInt("code");
                    if (code == 0) {
                        //String data = object.getString("data");
                        //JSONObject object1 = new JSONObject(data);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onSuccess: -----------" + e.toString());

                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "onFailure: -------------ee------" + e.toString());

            }
        });
    }





    //isRaySmart("Ray-Smart")
    private boolean isRaySmart(String p) {
        String productModel = SystemProperties.get("ro.build.model", "");
        if (productModel.equalsIgnoreCase(p))
            return true;
        else
            return false;
    }

}
