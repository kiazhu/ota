package com.luxcine.luxcine_ota;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.MediaRouteButton;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Editor;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.luxcine.luxcine_ota.download.DownLoadObserver;
import com.luxcine.luxcine_ota.download.DownloadInfo;
import com.luxcine.luxcine_ota.download.DownloadManager;
import com.luxcine.luxcine_ota.utils.Constants;
import com.luxcine.luxcine_ota.utils.Data;
import com.luxcine.luxcine_ota.utils.SkyFileOperator;
import com.luxcine.luxcine_ota.version.APKUpdate;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int RESULT = 0;
    private static final int RESULT_UPDATE = 1;
    private static final int DOWNLOAD_FIALED = 2;
    private static final int UI_UPDATE = 3;

    private static int progess;
    private TextView tvVersion;

    private TextView tvOldVersion;
    private static TextView tvNewVersion;

    private String strUrl, minUrl, maxUrl;
    private List<Data> list;
    private static int minVersion, maxVersion, newVersion;
    private static String updateUrl;
    private String md5, minMd5, maxMd5;
    private String storagemem, minStoragemem, maxStoragemem;

    private String usid="";
    private String  productModel, logo, mode;
    private int version;

    private static Button btnUpdate;
    private static TextView tvFailed;
    private static TextView tvProgesss;
    private static ProgressBar progressBar;
    private static TextView tvSecond;

    private static AlertDialog mDialog;
    private static CountDownTimer countDownTimer;

    //private static final String BASE_URL_UPDATE_HK = "http://149.129.93.140:8080/otaupdate/xml/";
    private static final String BASE_URL_UPDATE_HK = "http://www.chasecolor.com:8080/otaupdate/xml/";
    private static final String BASE_URL_UPDATE_CN = "http://120.24.53.73:80/otaupdate/xml/";
    private String BASE_URL_UPDATE;

    private String launcher ="";

    // 外存sdcard存放路径
    //private static final String FILE_PATH = Environment.getExternalStorageDirectory() + "/";
    private static final String FILE_PATH = "/data/data/com.luxcine.luxcine_ota/";
    // 下载应用存放全路径
    private static final String FILE_NAME = FILE_PATH + "update.zip";
    // 准备安装新版本应用标记
    private static final int INSTALL_TOKEN = 1;

    private NetReciver netReceiver;

    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    @SuppressLint("HandlerLeak")
    private static Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message message) {
            switch (message.what) {
                case RESULT:
                    tvNewVersion.setText(com.luxcine.luxcine_ota.MyApplication.getContext().getResources().getString(R.string.already_new));
                    //tvNewVersion.setTextColor(Color.parseColor("#C2BFBF"));
                    break;
                case RESULT_UPDATE:
                    tvNewVersion.setText(com.luxcine.luxcine_ota.MyApplication.getContext().getResources().getString(R.string.new_version) + newVersion);
                    btnUpdate.setVisibility(View.VISIBLE);
                    break;
                case DOWNLOAD_FIALED:
                    btnUpdate.setVisibility(View.VISIBLE);
                    btnUpdate.requestFocus();
                    tvFailed.setVisibility(View.VISIBLE);
                    tvProgesss.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    break;
                case UI_UPDATE:
                    if (btnUpdate.getVisibility() == View.VISIBLE) {
                        btnUpdate.setVisibility(View.GONE);
                    }

                    tvProgesss.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    tvProgesss.setText(MyApplication.getContext().getResources().getString(R.string.downloading) + progess + "%");
                    progressBar.setProgress(progess);

                    break;
                default:
                    break;
            }
        }
    };
    private int startDownload = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
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
        }
        launcher = initLauncher();

        if (launcher.equals("dbos")) {
            BASE_URL_UPDATE = BASE_URL_UPDATE_CN;
        } else if (launcher.equals("atv")) {
            BASE_URL_UPDATE = BASE_URL_UPDATE_HK;
        }

        usid = SkyFileOperator.readFromNandkey("usid");
        version = Integer.parseInt(SystemProperties.get("ro.build.version.incremental", ""));
        //productModel = SystemProperties.get("ro.build.model", "marconi");
        productModel = SystemProperties.get("ro.product.name", "marconi");
        logo = SystemProperties.get("tv.boot.logo", "normal");
        Log.e(TAG, "onCreate:------" + usid + "," + version + "," + productModel + "," + logo);

        if (usid.contains("Q7") || usid.contains("C7")) {
            mode = "c7";
        } else {
            mode = "z4";
        }

        if (productModel.equals("Ray-Smart") && logo.equals("")) {
            logo = "rombica";
        }

        //rk_rk3328_box_normal
        /*version = Integer.parseInt(SystemProperties.get("ro.build.version.incremental", ""));
        BASE_URL_UPDATE = BASE_URL_UPDATE_CN;
        mode = "rk";
        productModel = "rk3328";
        logo = "box_normal";*/

        strUrl = BASE_URL_UPDATE + "update/" + mode + "_" + productModel + "_" + logo + ".xml";
        Log.e(TAG, "onCreate: ------读取文件:" + strUrl);

        initView();
        getVersionCode();

        netReceiver = new NetReciver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netReceiver, intentFilter);


        /*判断update.zip文件是否存在
          判断是否为断点续传
          若文件存在，不为断点续传，则删除原文件*/

        //1下载开始，2下载完成
        sp = getSharedPreferences("luxicne_ota", Context.MODE_PRIVATE);
        editor = sp.edit();
        startDownload = sp.getInt("startDownload", 0);

        boolean isFileExists = fileIsExists("/data/data/com.luxcine.luxcine_ota/update.zip");
        Log.e(TAG, "onCreate: -------文件存在:" + isFileExists + ",startDownload:" + startDownload);

        if (startDownload == 2 && isFileExists) {
            deleteLocal("/data/data/com.luxcine.luxcine_ota/update.zip");//删除文件
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(netReceiver);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        super.onDestroy();
    }

    class NetReciver extends BroadcastReceiver {
        private static final String TAG = "NetBroadCastReciver";

        @Override
        public void onReceive(Context context, Intent intent) {
            //如果是在开启wifi连接和有网络状态下
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                    //连接状态
                    Log.e(TAG, "onReceive: ---网络链接成功-----");

                    //new APKUpdate().update();

                    getVersionInfo();
                } else {
                    Log.e(TAG, "onReceive: ----网络链接失败------");
                }
            }
        }
    }


    public void initView() {
        tvVersion = findViewById(R.id.tv_version);

        tvOldVersion = findViewById(R.id.tv_old_version);
        tvNewVersion = findViewById(R.id.tv_new_version);

        btnUpdate = findViewById(R.id.btn_update);
        tvFailed = findViewById(R.id.tv_failed);
        tvProgesss = findViewById(R.id.tv_progesss);
        progressBar = findViewById(R.id.progesss);
        tvOldVersion.setText(getResources().getString(R.string.current_version) + version);
        btnUpdate.requestFocus();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdate.setVisibility(View.GONE);
                tvFailed.setVisibility(View.GONE);
                tvProgesss.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                //new DownloadAsyncTask().execute();
                //update();

                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                startService(intent);
            }
        });
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
                        } else if ("describe".equals(xParser.getName())) {
                            String describe = xParser.nextText();
                            data.setDescribe(describe);
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
            Log.e(TAG, "parseXmlInfo: ---------版本条数:" + list.size());
            if (list.size() <= 0) {
                handler.sendEmptyMessage(RESULT);
            }

            int max = Integer.parseInt(list.get(list.size() - 1).getDate());
            int min = Integer.parseInt(list.get(0).getDate());

            /*for (int i = 0; i < list.size(); i++) {
                if (min > Integer.parseInt(list.get(i).getDate())) {
                    min = Integer.parseInt(list.get(i).getDate());
                    minVersion = Integer.parseInt(list.get(i).getDate());
                    minUrl = list.get(i).getUrl();
                    minMd5 = list.get(i).getMd5();
                    minStoragemem = list.get(i).getStoragemem();
                }
                if (max < Integer.parseInt(list.get(i).getDate())) {
                    max = Integer.parseInt(list.get(i).getDate());
                    maxVersion = Integer.parseInt(list.get(i).getDate());
                    maxUrl = list.get(i).getUrl();
                    maxMd5 = list.get(i).getMd5();
                    maxStoragemem = list.get(i).getStoragemem();
                }
            }*/
            for (int i = 0; i < list.size(); i++) {
                Log.e(TAG, "parseXmlInfo: -----所有版本:" + list.get(i).getDate());
            }
            Log.e(TAG, "parseXmlInfo: ----------最小版本:" + min + ",最新版本:" + max);

            if (version < min) {
                newVersion = Integer.parseInt(list.get(0).getDate());

                updateUrl = BASE_URL_UPDATE + "download/zip/"
                        + mode + "_" + productModel + "_" + logo + "/"
                        + newVersion + "/" + version + "/update.zip";

            } else if (version < max) {
                newVersion = Integer.parseInt(list.get(list.size() - 1).getDate());

                updateUrl = BASE_URL_UPDATE + "download/zip/"
                        + mode + "_" + productModel + "_" + logo + "/"
                        + newVersion + "/" + version + "/update.zip";

            } else if (version >= max) {
                newVersion = Integer.parseInt(list.get(list.size() - 1).getDate());
            }

            Log.e(TAG, "parseXmlInfo: --本地版本:" + version + ",最小版本:" + min
                    + ",最新版本:" + newVersion + ",md5:" + md5 + ",storagemem:" + storagemem + ", updateUrl: " + updateUrl);

            if (version < newVersion) {
                handler.sendEmptyMessage(RESULT_UPDATE);
            } else {
                handler.sendEmptyMessage(RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class DownloadService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            Log.e(TAG, "onCreate: -----------DownloadService");
            //new DownloadAsyncTask().execute();
            download();
        }

        /**
         * 下载新版本应用
         */
       /* class DownloadAsyncTask extends AsyncTask<Void, Integer, Integer> {

            @Override
            protected void onPreExecute() {
                btnUpdate.setVisibility(View.GONE);
                Log.d(TAG, "执行至--onPreExecute");
            }

            @Override
            protected Integer doInBackground(Void... params) {
                Log.d(TAG, "执行至--doInBackground");

                URL url;
                HttpURLConnection connection = null;
                InputStream in = null;
                FileOutputStream out = null;
                try {
                    // url = new URL(Url.APK_PATH);
                    Log.e(TAG, "doInBackground: -----------" + updateUrl);

                    url = new URL(updateUrl);
                    connection = (HttpURLConnection) url.openConnection();

                    in = connection.getInputStream();
                    long fileLength = connection.getContentLength();
                    File file_path = new File(FILE_PATH);
                    if (!file_path.exists()) {
                        file_path.mkdir();
                    }

                    out = new FileOutputStream(new File(FILE_NAME));//为指定的文件路径创建文件输出流
                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    long readLength = 0;

                    Log.d(TAG, "执行至--readLength = 0");

                    while ((len = in.read(buffer)) != -1) {

                        out.write(buffer, 0, len);//从buffer的第0位开始读取len长度的字节到输出流
                        readLength += len;

                        int curProgress = (int) (((float) readLength / fileLength) * 100);
                        //Log.d(TAG, "--------当前下载进度：" + curProgress);

                        publishProgress(curProgress);
                        if (readLength >= fileLength) {
                            Log.d(TAG, "执行至--readLength >= fileLength");
                            break;
                        }
                    }

                    out.flush();
                    return INSTALL_TOKEN;

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "doInBackground: ---e--------" + e.toString());

                    stopService(new Intent(MyApplication.getContext(), DownloadService.class));
                    handler.sendEmptyMessage(DOWNLOAD_FIALED);

                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if (values[0] == 10 || values[0] == 30 || values[0] == 60 || values[0] == 90) {
                    Log.e(TAG, "onProgressUpdate: -----下载进度:" + values[0]);
                }
                tvProgesss.setText(getResources().getString(R.string.downloading) + values[0] + "%");
                progressBar.setProgress(values[0]);
                if (values[0] == 100) {
                    powerOffDialog();
                    countDown();
                }

                progess = values[0];

                handler.sendEmptyMessage(UI_UPDATE);

            }

            @Override
            protected void onPostExecute(Integer integer) {
                Log.d(TAG, "onPostExecute: --------下载完成--------");
                //update();
               *//* powerOffDialog();
                countDown();*//*
            }
        }*/
        /*public void update() {
            try {
                //签名验证
                RecoverySystem.verifyPackage(new File(FILE_NAME), new RecoverySystem.ProgressListener() {
                    @Override
                    public void onProgress(int progress) {
                        Log.d(TAG, "progress --------签名: " + progress);
                    }
                }, null);
                RecoverySystem.installPackage(com.luxcine.luxcine_ota.MyApplication.getContext(), new File(FILE_NAME));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onPostExecute: ----" + e.toString());
            }
        }*/
        public void download() {
            //updateUrl = "http://gdown.baidu.com/data/wisegame/df65a597122796a4/weixin_821.apk";
            Log.e(TAG, "download: --------------------" + updateUrl);
            DownloadManager.getInstance().download(updateUrl, new DownLoadObserver() {
                @Override
                public void onNext(DownloadInfo value) {
                    super.onNext(value);
                    // progress1.setMax((int) value.getTotal());
                    // progress1.setProgress((int) value.getProgress());

                    long p1 = value.getProgress();
                    long t1 = value.getTotal();

                    int p = (int) Math.floor(p1 * 1.0 / t1 * 100);

                    progess = p;
                    handler.sendEmptyMessage(UI_UPDATE);

                    if (p == 10 || p == 30 || p == 60 || p == 90) {
                        Log.e(TAG, "onNext: ----111--" + value.getProgress() + "," + value.getTotal() + "," + p + "%");
                    }

                    //if (p > 0 && p < 1) {
                    editor.putInt("startDownload", 1);
                    editor.commit();
                    //}

                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    Log.e(TAG, "onError: -------" + e.toString());
                    DownloadManager.getInstance().cancel(updateUrl);
                    stopService(new Intent(MyApplication.getContext(), DownloadService.class));
                    handler.sendEmptyMessage(DOWNLOAD_FIALED);
                }

                @Override
                public void onComplete() {
                    if (downloadInfo != null) {
                        Log.e(TAG, "onComplete: -----下载完成:" + downloadInfo.getFileName() + "-DownloadComplete");

                        editor.putInt("startDownload", 2);
                        editor.commit();

                        update();
                    }
                }
            });
        }
    }

    public static void update() {
        try {
            //签名验证
            RecoverySystem.verifyPackage(new File(FILE_NAME), new RecoverySystem.ProgressListener() {
                @Override
                public void onProgress(int progress) {
                    Log.e(TAG, "progress --------签名: " + progress);
                }
            }, null);
            RecoverySystem.installPackage(MyApplication.getContext(), new File(FILE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onPostExecute: ----" + e.toString());
        }
    }

    //获取本地软件版本号
    private String getVersionCode() {
        String versionCode = null;
        try {
            // 获取软件版本号，对应build.gradle下android:versionName
            versionCode = com.luxcine.luxcine_ota.MyApplication.getContext().getPackageManager()
                    .getPackageInfo(com.luxcine.luxcine_ota.MyApplication.getContext().getPackageName(), 0).versionName;

            tvVersion.setText("version:" + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        }
        return versionCode;
    }

    private static void powerOffDialog() {
        View view = View.inflate(com.luxcine.luxcine_ota.MyApplication.getContext(), R.layout.dialog_power_off, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(com.luxcine.luxcine_ota.MyApplication.getContext());
        tvSecond = view.findViewById(R.id.tv_second);

        builder.setView(view);

        mDialog = builder.create();
        Window window = mDialog.getWindow();
        // 把 DecorView 的默认 padding 取消，同时 DecorView 的默认大小也会取消
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        mDialog.getWindow().setDimAmount(0.7f);

        layoutParams.gravity = Gravity.CENTER;
        // 设置宽度
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;


        /*layoutParams.x = 10;
        layoutParams.y = 10;
        layoutParams.width = 400;
        layoutParams.height =400;
        layoutParams.alpha = 0.6f;*/

        window.setAttributes(layoutParams);

        //给 DecorView 设置背景颜色，很重要，不然导致 Dialog 不全屏显示
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);

        //需要把对话框的类型设为TYPE_SYSTEM_ALERT，否则对话框无法在广播接收器里弹出
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        mDialog.show();

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_HOME
                        || keyCode == KeyEvent.KEYCODE_BACK
                        || keyCode == KeyEvent.KEYCODE_MENU
                        || keyCode == KeyEvent.KEYCODE_F2
                        || keyCode == KeyEvent.KEYCODE_F5) {
                    return true;
                }
                return false;
            }
        });
    }

    private static void countDown() {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(11000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvSecond.setText("" + millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    //Log.e(TAG, "onTick: -----onFinish--------");
                    //update();
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }.start();
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mDialog == null) {
            btnUpdate.requestFocus();
        }
        return super.dispatchKeyEvent(event);
    }

    //判断文件是否存在
    private static boolean fileIsExists(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean deleteLocal(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                if (file.delete()) {
                    Log.e(TAG, "deleteLocal: ------delete  success---------");
                    return true;
                } else {
                    Log.e(TAG, "deleteLocal: ------delete  failed---------");
                    deleteLocal(path);
                    return false;
                }
            }
        }
        return true;
    }
}
