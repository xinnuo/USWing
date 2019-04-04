/**
 * created by 小卷毛, 2018/10/30
 * Copyright (c) 2018, 416143467@qq.com All Rights Reserved.
 * #                   *********                            #
 * #                  ************                          #
 * #                  *************                         #
 * #                 **  ***********                        #
 * #                ***  ****** *****                       #
 * #                *** *******   ****                      #
 * #               ***  ********** ****                     #
 * #              ****  *********** ****                    #
 * #            *****   ***********  *****                  #
 * #           ******   *** ********   *****                #
 * #           *****   ***   ********   ******              #
 * #          ******   ***  ***********   ******            #
 * #         ******   **** **************  ******           #
 * #        *******  ********************* *******          #
 * #        *******  ******************************         #
 * #       *******  ****** ***************** *******        #
 * #       *******  ****** ****** *********   ******        #
 * #       *******    **  ******   ******     ******        #
 * #       *******        ******    *****     *****         #
 * #        ******        *****     *****     ****          #
 * #         *****        ****      *****     ***           #
 * #          *****       ***        ***      *             #
 * #            **       ****        ****                   #
 */
package com.meida.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.ImageView
import android.app.ActivityManager
import java.util.*

/**
 * 检查是否存在SDCard
 */
fun hasSdcard() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

/**
 * 以数据流的方式将Resources下的图片显示，防止内存溢出
 */
@Suppress("DEPRECATION")
fun Context.getImgFromSD(iv: ImageView, resID: Int) = kotlin.run {
    iv.background = BitmapDrawable(
        resources,
        BitmapFactory.decodeStream(
            resources.openRawResource(resID),
            null,
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inPurgeable = true
                inInputShareable = true
            })
    )
}

/**
 * 获取当前进程名
 */
fun Context.getProcessName() = kotlin.run {
    val pid = android.os.Process.myPid()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.runningAppProcesses.firstOrNull { it.pid == pid }?.processName ?: ""
}

/**
 * 判断网络是否可用
 */
@Suppress("DEPRECATION")
fun Context.isNetworkConnected() = kotlin.run {
    val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectivity.allNetworkInfo
    info?.any { it.state == NetworkInfo.State.CONNECTED } ?: false
}

/**
 * 判断APP是否在前台运行
 */
fun Context.isRunningForeground() = kotlin.run {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcessInfos = activityManager.runningAppProcesses
    appProcessInfos.any {
        it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && it.processName == applicationInfo.processName
    }
}

/**
 * 判断某个服务是否正在运行
 */
@Suppress("DEPRECATION")
fun Context.isServiceWork(serviceName: String) = kotlin.run {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val serviceInfos = activityManager.getRunningServices(40)
    serviceInfos.any { it.service.className == serviceName }
}

/**
 * 屏幕宽度，单位：px
 */
fun Context.getScreenWidth() = kotlin.run {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = manager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    outMetrics.widthPixels
}

/**
 * 屏幕高度，单位：px
 */
fun Context.getScreenHeight() = kotlin.run {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = manager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    outMetrics.heightPixels
}

/**
 * 状态栏高度，单位：px
 */
fun Context.getStatusBarHeight() = kotlin.run {
    var statusBarHeight = 0
    val resourceId = resources.getIdentifier(
        "status_bar_height",
        "dimen",
        "android"
    )
    if (resourceId > 0)
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    statusBarHeight
}

/**
 * 获取32位uuid
 */
fun get32UUID() = get36UUID().replace("-".toRegex(), "")

/**
 * 生成唯一号
 */
fun get36UUID() = UUID.randomUUID().toString()
