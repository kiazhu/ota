package com.luxcine.luxcine_ota.version;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import com.luxcine.luxcine_ota.MyApplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ApkController {
    private static final String TAG = "ApkController";

    /**
     * 描述: 安装
     */
    public static boolean install(String apkPath, Context context) {
        // 先判断手机是否有root权限
        if (hasRootPerssion()) {
            Log.e(TAG, "------------------root权限获取成功");
            // 有root权限，利用静默安装实现
            return clientInstall(apkPath);
        } else {
            Log.e(TAG, "------------------没有root权限");
            // 没有root权限，利用意图进行安装
            File file = new File(apkPath);
            if (!file.exists())
                return false;
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            context.startActivity(intent);
            return true;
        }
    }

    /**
     * 描述: 卸载
     */
    public static boolean uninstall(String packageName, Context context) {
        if (hasRootPerssion()) {
            // 有root权限，利用静默卸载实现
            return clientUninstall(packageName);
        } else {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(uninstallIntent);
            return true;
        }
    }

    /**
     * 判断手机是否有root权限
     */
    public static boolean hasRootPerssion() {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 静默安装
     */
    public static boolean clientInstall(String apkPath) {
        Log.e(TAG, "------------------apkPath:" + apkPath);
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su1");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 " + apkPath);
            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            //PrintWriter.println("touch /sdcard/test.txt");
            PrintWriter.println("pm install -r " + apkPath);//-r 重新安装应用，保留应用数据
//          PrintWriter.println("exit");
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();

            Log.e(TAG, "clientInstall: ----------value--0代表成功-----1代表失败----------" + value);

            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "clientInstall: ---------------" + e.toString());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }


    public static void installSlient(Context context, String apk) {

        String cmd = "pm install -r " + apk;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            //静默安装需要root权限
            process = Runtime.getRuntime().exec("su2");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();

            //执行命令
            process.waitFor();
            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();

            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "installSlient: -----------------------" + e.toString());
            }
        }
       //Log.e(TAG, "=================== successMsg: " + successMsg.toString());
        //Log.e(TAG, "=================== errorMsg: " + errorMsg.toString());

        //Toast.makeText(MyApplication.getContext(), errorMsg.toString() + "  " + successMsg, Toast.LENGTH_LONG).show();

        //安装成功
        /*if ("Success".equals(successMsg.toString())) {
            Log.e(TAG, "========= apk install success");
        }*/
        getVersionCode();
    }


    /**
     * 静默卸载
     */
    public static boolean clientUninstall(String packageName) {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
            PrintWriter.println("pm uninstall " + packageName);
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "clientUninstall: --------------------------" + e.toString());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    private static boolean returnResult(int value) {
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }


    //静默安装
    public static boolean installApp(String apkPath) {
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        try {
            process = new ProcessBuilder("pm", "install", "-i", MyApplication.getContext().getPackageName(), "-r", apkPath).start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (Exception e) {
            Log.e(TAG, "installApp: ----" + e.toString());
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "installApp: ----::" + e.toString());
            }
            if (process != null) {
                process.destroy();
            }
        }
        Log.e(TAG, "---installApp:" + errorMsg.toString());

        getVersionCode();
        //如果含有“success”单词则认为安装成功
        return successMsg.toString().equalsIgnoreCase("success");
    }

    //静默卸载
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void unInstallApp() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent sender = PendingIntent.getActivity(MyApplication.getContext(), 0, intent, 0);
        PackageInstaller mPackageInstaller = MyApplication.getContext().getPackageManager().getPackageInstaller();
        mPackageInstaller.uninstall(MyApplication.getContext().getPackageName(), sender.getIntentSender());// 卸载APK
    }

    public static  void installApk(Context context, String downloadApk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(downloadApk);
        Log.e(TAG, "installApk: -------安装路径:"+downloadApk );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName()+".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);

    }

    static String oldVersion;

    //获取本地软件版本号
    private static String getVersionCode() {
        try {
            // 获取软件版本号，对应build.gradle下android:versionCode
            oldVersion = MyApplication.getContext().getPackageManager().getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionName;
            Log.e(TAG, "installApp: ----安装完成:" + oldVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getVersionCode: -------:"+e.toString() );
        }
        return oldVersion;
    }
}
