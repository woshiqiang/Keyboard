package com.student.keyboard.listener;


import com.student.keyboard.util.Flags;

import java.util.List;

/**
 * PermissionListener的实现类，根据权限是否授予设定键盘可弹出位为true or false
 * @author shiina
 * @date 2019/11/30
 */
public class PermissionListenerImp implements PermissionListener {
    /**
     * 允许获取权限时调用
     */
    @Override
    public void onGranted() {
       Flags.isGetPermission = true;
    }

    /**
     * 拒绝获取权限时调用
     * @param deniedPermission
     */
    @Override
    public void onDenied(List<String> deniedPermission) {
        Flags.isGetPermission = false;
    }

    /**
     * 拒绝并不在提醒时调用
     * @param deniedPermission
     */
    @Override
    public void onShouldShowRationale(List<String> deniedPermission) {
        Flags.isGetPermission = false;
    }
}
