package com.student.keyboard.util;

import android.app.AlertDialog;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;


import com.student.keyboard.MyApp;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限相关
 * @author shiina
 * @date 2019/12/03
 */
public class Authority {

    /**
     * 确认权限状态
     * @param permissions
     * @param grantResults
     */
    public static void checkAuthority(@NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> deniedPermissionList = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissionList.add(permissions[i]);
            }
        }

        if (deniedPermissionList.isEmpty()) {
            //已经全部授权
            permissionAllGranted();
        } else {
            //拒绝授权
            permissionHasDenied(deniedPermissionList);

        }
    }

    /**
     * 权限全部已经授权
     */
    public static void permissionAllGranted() {
        if (Flags.listener != null) {
            Flags.listener.onGranted();
        }
    }
    /**
     * 有权限被拒绝
     *
     * @param deniedList 被拒绝的权限
     */
    public  static void permissionHasDenied(List<String> deniedList) {
        if (Flags.listener != null) {
            AlertDialog.Builder builder  = new AlertDialog.Builder(Flags.context);
            builder.setTitle("警告Warning！" ) ;
            builder.setMessage("禁止应用获取权限将导致键盘服务无法正常运行！如果想正常" +
                    "启动键盘服务，请在应用权限设置中进行更改。Forbidding apps from " +
                    "gaining permission will cause the keyboard service to not " +
                    "run properly! If you want to start the keyboard service " +
                    "normally, change it in the app permissions settings." ) ;
            builder.setPositiveButton("是" ,  null );
            builder.show();
            Flags.listener.onDenied(deniedList);
        }
    }


    /**
     * android6.0之后要动态获取权限
     * @date 2019/11/28
     */
    public static boolean checkPermission() {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(MyApp.getInstance(),
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Flags.isGetPermission = false;
                return false;

            } else {
                Flags.isGetPermission = true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}

