package com.student.keyboard.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author jarvis
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent service = new Intent(context,MyInputMethodService.class);
        context.startService(service);
        Log.v("TAG", "开机自动服务自动启动.....");
    }
}
