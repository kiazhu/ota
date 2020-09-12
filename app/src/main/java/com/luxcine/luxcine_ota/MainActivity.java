package com.luxcine.luxcine_ota;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;

import com.luxcine.luxcine_ota.utils.Constants;
import com.luxcine.luxcine_ota.utils.Data;
import com.luxcine.luxcine_ota.utils.SkyFileOperator;
import com.luxcine.luxcine_ota.version.UpdateAppManager;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String strUrl;
    private List<Data> list;
    private String date;
    private String url;
    private String md5;
    private String storagemem;

    private String usid, productModel;
    private int version;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usid = SkyFileOperator.readFromNandkey("usid");
        version = Integer.parseInt(SystemProperties.get("ro.build.version.incremental", ""));
        productModel = SystemProperties.get("ro.build.model", "");
        Log.e(TAG, "onCreate:------" + usid + ":" + version + ":" + productModel);

        if (usid.contains("Q7") || usid.contains("C7")) {
            strUrl = Constants.C7_URL;
        } else {
            strUrl = Constants.Z4_URL;
        }
        getVersionInfo();
    }

    public void getVersionInfo() {
        //在子线程中获取服务器的数据
        Thread thread = new Thread() {
            @Override
            public void run() {
                String path = strUrl;
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
            Data data = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //当事件的开始类型newslist，代表的是xml文件的数据开始
                        if ("update".equals(xParser.getName())) {
                            list = new ArrayList<Data>();
                        } else if ("version".equals(xParser.getName())) {
                            data = new Data();
                        } else if ("date".equals(xParser.getName())) {
                            String date = xParser.nextText();
                            data.setDate(date);
                        } else if ("url".equals(xParser.getName())) {
                            String url = xParser.nextText();
                            data.setUrl(url);
                        } else if ("md5".equals(xParser.getName())) {
                            String md5 = xParser.nextText();
                            data.setMd5(md5);
                        } else if ("storagemem".equals(xParser.getName())) {
                            String storagemem = xParser.nextText();
                            data.setStoragemem(storagemem);
                        }

                        break;
                    case XmlPullParser.END_TAG:
                        //当结束时间是version时，说明一条update已经解析完成，并且加入到集合中
                        if ("version".equals(xParser.getName())) {
                            list.add(data);
                        }
                        break;
                }
                eventType = xParser.next();
            }
            for (Data d : list) {
                Log.e(TAG, "parseXmlInfo:---date: " + d.getDate() + ",url:"
                        + d.getUrl() + ",md5:" + d.getMd5() + ",storagemem:" + d.getStoragemem());

                int date = Integer.parseInt(d.getDate());

                if (version < date) {
                    //UpdateAppManager updateAppManager = new UpdateAppManager(this,date,strUrl);
                    //updateAppManager.downloadAsyncTask().execute();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
