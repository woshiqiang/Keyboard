package com.student.keyboard.listener;

import java.util.List;

/**
 * 权限获取的监听接口
 * @author shiina
 * @date 2019/11/30
 */
public interface PermissionListener {
    /**
     * 允许获取权限时调用
     */
    void onGranted();

    /**
     * 拒绝获取权限时调用
     * @param deniedPermission
     */
    void onDenied(List<String> deniedPermission);

    /**
     * 拒绝并不在提醒时调用
     * @param deniedPermission
     */
    void onShouldShowRationale(List<String> deniedPermission);
}
