package com.student.keyboard.database.databaseutil;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author shiina
 * @date 2019/11/26
 * 手机内存获取类
 */
public class FileConfig {
    /**
     * .db在手机内存地址
     */
    private static String mWorkPath = null;
    private static String mRootPath = null;
    private static Boolean mGetSDPath = false;
    private final static String DB_PATH_NAME = "database/";
    public static long copyTime = 0;

    private static Context mContext;
    private final static String SEPARATOR = "/";

    /**
     * 获取根目录
     *
     * @return
     */
    public static String getRootPath() {
        if (!mGetSDPath || mRootPath == null) {
            mGetSDPath = true;
            boolean sdCardExist = Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
            // 判断sd卡是否存在
            if (sdCardExist) {
                File sdDir = Environment.getExternalStorageDirectory();
                // 获取跟目录
                mRootPath = sdDir.toString();
            } else {
                mRootPath = mContext.getFilesDir().toString();
            }
        }
        if (!mRootPath.endsWith(SEPARATOR)) {
            mRootPath += SEPARATOR;
        }
        return mRootPath;
    }

    /**
     * 设置工作目录
     *
     * @param context app context,不然会造成内存泄漏
     * @param path
     */
    public static void setWorkPath(Context context, String path) {
        mContext = context;
        if (null != getRootPath()) {
            mWorkPath = mRootPath + path;
        }
        if (!mWorkPath.endsWith(SEPARATOR)) {
            mWorkPath += SEPARATOR;
        }

        File file = new File(mWorkPath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            Log.e("mkdirs", "" + mkdirs);
        }
    }

    /**
     * 获取数据库地址
     *
     * @return
     */
    public static String getDbPath() {
        File file = new File(mWorkPath + DB_PATH_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return mWorkPath + DB_PATH_NAME;
    }

    /**
     * 以流的形式打开数据库
     *
     * @param context
     */
    public static void copyAccessDB(Context context) {
        try {
            String[] dbNames = context.getAssets().list("db");
            for (String dbName : dbNames) {
                long startTime = System.currentTimeMillis();
                String filePath = FileConfig.getDbPath() + dbName;
                File dbFile = new File(filePath);
                if (!dbFile.exists()) {
                    FileOutputStream fos = null;
                    try {
                        dbFile.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    InputStream is = context.getAssets().open("db/" + dbName);
                    fos = new FileOutputStream(dbFile);

                    byte[] buffer = new byte[2048];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    long endTime = System.currentTimeMillis();
                    long useTime = endTime - startTime;
                    copyTime += useTime;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
