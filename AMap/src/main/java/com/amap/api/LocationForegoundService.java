package com.amap.api;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

/**
 * 前台定位service
 * 增加权限：FOREGROUND_SERVICE
 * https://github.com/amap-demo/android-o-backgroundlocation
 */
public class LocationForegoundService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private static final int NOTIFICATION_ID = 520;
    private static final int NOTIFICATION_REQUEST = 110;
    private static final String CHANNEL_ONE_ID = "com.ruanmeng.service";
    private static final String CHANNEL_ONE_NAME = "LocationForengound";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Android O上才显示通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) showNotify();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true); //停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }

    //显示通知栏
    @TargetApi(Build.VERSION_CODES.O)
    public void showNotify() {

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_REQUEST,
                new Intent(/*点击时跳转的Activity*/),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set channelId of notification for Android O.
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        // Set the info for the views that show in the notification panel.
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), 0/*resourcesId资源图片*/))
                .setChannelId(CHANNEL_ONE_ID)
                .setSmallIcon(0/*resourcesId资源图片*/)  // the status icon
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setContentTitle("正在后台定位")
                .setContentText("定位进行中")
                .setOngoing(true)
                .build();
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;

        // Send the notification. 设置前台服务
        startForeground(NOTIFICATION_ID, notification);
    }

    public class LocalBinder extends Binder {
        LocationForegoundService getService() {
            return LocationForegoundService.this;
        }
    }
}
