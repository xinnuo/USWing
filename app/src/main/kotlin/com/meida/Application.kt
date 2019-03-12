/**
 * created by 小卷毛, 2019/1/21
 * Copyright (c) 2019, 416143467@qq.com All Rights Reserved.
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
package com.meida

import android.support.multidex.MultiDexApplication
import cn.jpush.android.api.JPushInterface
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheEntity
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.DBCookieStore
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.lzy.okgo.utils.OkLogger
import com.meida.uswing.BuildConfig
import io.rong.common.SystemUtils.getCurrentProcessName
import io.rong.imkit.RongIM
import okhttp3.OkHttpClient
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.player.PlayerFactory
import tv.danmaku.ijk.media.utils.VideoType
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * 项目名称：USWing
 * 创建人：41614
 * 创建时间：2019-01-21 17:54
 */
class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        initOkGo()

        VideoType.enableMediaCodec()
        VideoType.enableMediaCodecTexture()
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java) //EXO模式

        //极光推送
        JPushInterface.setDebugMode(BuildConfig.LOG_DEBUG) //设置开启日志,发布时请关闭日志
        JPushInterface.init(this@Application)              //初始化 JPush

        /*
         * OnCreate 会被多个进程重入，这段保护代码，确保只有您需要使用 RongIM 的进程和 Push 进程执行了 init。
         * io.rong.push 为融云 push 进程名称，不可修改。
         */
        if (applicationInfo.packageName == getCurrentProcessName(applicationContext)
            || "io.rong.push" == getCurrentProcessName(applicationContext)
        ) {

            // RongPushClient.registerHWPush(this@Application)         //华为推送
            // RongPushClient.registerMiPush(this@Application, "", "") //小米推送
            // RongPushClient.registerGCM(this@Application)            //谷歌推送

            RongIM.init(this@Application)

            /*
             * 融云SDK事件监听处理
             */
            if (applicationInfo.packageName == getCurrentProcessName(applicationContext)) {
                RongCloudContext.init(this@Application)
            }
        }
    }

    private fun initOkGo() {
        val builder = OkHttpClient.Builder()

        OkLogger.debug("USWing", BuildConfig.LOG_DEBUG)

        //log相关
        val loggingInterceptor = HttpLoggingInterceptor("OkGo")
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY) //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO)                        //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor)                          //添加OkGo默认debug日志

        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)    //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)   //全局的写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS) //全局的连接超时时间

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        builder.cookieJar(CookieJarImpl(DBCookieStore(this))) //使用数据库保持cookie，如果cookie不过期，则一直有效
        // builder.cookieJar(CookieJarImpl(SPCookieStore(this)))        //使用sp保持cookie，如果cookie不过期，则一直有效
        // builder.cookieJar(CookieJarImpl(MemoryCookieStore()))        //使用内存保持cookie，app退出后，cookie消失

        //https相关设置，信任所有证书，不安全有风险
        val sslParams1 = HttpsUtils.getSslSocketFactory()
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier) //配置https的域名匹配规则

        // 其他统一的配置
        OkGo.getInstance().init(this@Application)    //必须调用初始化
            .setOkHttpClient(builder.build())             //建议设置OkHttpClient，不设置会使用默认的
            .setCacheMode(CacheMode.NO_CACHE)             //全局统一缓存模式，默认不使用缓存，可以不传
            .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE) //全局统一缓存时间，默认永不过期，可以不传
            .retryCount = 3                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
    }

}
