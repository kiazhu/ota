package com.luxcine.luxcine_ota_customized.version;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.luxcine.luxcine_ota_customized.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateAppManager {
    private static final String TAG = "UpdateAppManager";

    // 外存sdcard存放路径
    private static final String FILE_PATH = Environment.getExternalStorageDirectory() + "/";
    // 下载应用存放全路径
    private static final String FILE_NAME = FILE_PATH + "luxcine_update.apk";
    // 准备安装新版本应用标记
    private static final int INSTALL_TOKEN = 1;

    private Context context;

    // 下载应用的进度条
    private ProgressDialog progressDialog;

    //新版本号和描述语言
    private String newVersion;
    private String update_describe, urlStr;
    private int version;

    private VersionModel mVersionBean;


    public UpdateAppManager(Context context, int version, String url) {
        this.context = context;
        this.version = version;
        this.urlStr = url;

    }


    // 获取当前版本号
    private int getCurrentVersion() {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            Log.e(TAG, "当前版本名和版本号" + info.versionName + "--" + info.versionCode);

            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();

            Log.e(TAG, "获取当前版本号出错");
            return 0;
        }
    }

    // 从服务器获得更新信息

    public void getUpdateMsg() {

      /*  class mAsyncTask extends AsyncTask<String, Integer, String> {
            @Override
            protected String doInBackground(String... params) {

                HttpURLConnection connection = null;
                try {
                    URL url_version = new URL(params[0]);
                    connection = (HttpURLConnection) url_version.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    InputStream in = connection.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                  //  Log.e(TAG, "bufferReader读到的数据---------" + reader);

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {             //回到主线程，更新UI
                //Log.e(TAG, "异步消息处理反馈--" + s);

                if (s != null) {
                    mVersionBean = new Gson().fromJson(s, VersionModel.class);
                    Log.e(TAG, "onPostExecute: ----------" + mVersionBean);

                    newVersion =   mVersionBean.getData().getACCESS_CONTROL_SYSTEM_VERSION().getName();


                   // update_describe = mVersionBean.getData().getDescribe();

                    //   Log.e(TAG, "新版本号--" + newVersion);
                    //  Log.e(TAG, "新版本描述--" + update_describe);

                    String two = newVersion.replace(".", "");
                    int news = new Integer(two);

                    if (news > getCurrentVersion()) {
                        Log.e(TAG, "提示更新！");
                        showNoticeDialog();
                    } else {
                        Log.e(TAG, "已是最新版本！");
                    }
                }
            }
        }
        //获取版本数据的地址
        //new mAsyncTask().execute(version_path);
         new mAsyncTask().execute(Url.VERSION_PATH);*/


        Log.e(TAG, "新版本号--" + version);
        Log.e(TAG, "新版本描述--" + getCurrentVersion());


        if (version > getCurrentVersion()) {
            Log.e(TAG, "提示更新！");

            showNoticeDialog();
        } else {
            Log.e(TAG, "已是最新版本！");
        }

    }

    // 显示提示更新对话框
    private void showNoticeDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("检测到新版本！")
                .setMessage(update_describe)
                .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showDownloadDialog();
                    }
                }).setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();


    }


    // 显示下载进度对话框
    public void showDownloadDialog() {

        Log.e(TAG, "showDownloadDialog: -------------------------");
        if (context != null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("正在下载...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            new downloadAsyncTask().execute();

        }


    }

    /**
     * 下载新版本应用
     */
    private class downloadAsyncTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            Log.e(TAG, "执行至--onPreExecute");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.e(TAG, "执行至--doInBackground");

            URL url;
            HttpURLConnection connection = null;
            InputStream in = null;
            FileOutputStream out = null;
            try {
                // url = new URL(Url.APK_PATH);

                Log.e(TAG, "doInBackground: -----------------------" + urlStr);

                url = new URL(urlStr);
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

                Log.e(TAG, "执行至--readLength = 0");

                while ((len = in.read(buffer)) != -1) {

                    out.write(buffer, 0, len);//从buffer的第0位开始读取len长度的字节到输出流
                    readLength += len;

                    int curProgress = (int) (((float) readLength / fileLength) * 100);

                    //Log.e(TAG, "当前下载进度：" + curProgress);

                    publishProgress(curProgress);

                    if (readLength >= fileLength) {

                        //   Log.e(TAG, "执行至--readLength >= fileLength");
                        break;
                    }
                }

                out.flush();
                return INSTALL_TOKEN;

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "doInBackground: -----------e-----------" + e.toString());
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
            //  Log.e(TAG, "异步更新进度接收到的值：" + values[0]);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progressDialog.dismiss();//关闭进度条
            //安装应用
            installApk();
        }
    }



    /**
     * 安装新版本应用
     */
    public static void installApk() {

        File apkFile = new File(FILE_NAME);
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 将此段代码移到此，可正常安装
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            apkUri = FileProvider.getUriForFile(MyApplication.getContext(), "com.example.luxcine_update.fileprovider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        MyApplication.getContext().startActivity(intent);



    }

}


