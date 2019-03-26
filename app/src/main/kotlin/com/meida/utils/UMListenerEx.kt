/**
 * created by 小卷毛, 2019/3/26
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
package com.meida.utils

import android.app.Activity
import android.content.Context
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.UMShareConfig
import com.umeng.socialize.bean.SHARE_MEDIA

fun Context.setShareConfig(config: UMShareConfig) = UMShareAPI.get(this).setShareConfig(config)

fun Activity.getPlatformInfo(
    platform: SHARE_MEDIA,
    listener: _UMAuthListener.() -> Unit
) {
    val mListener = _UMAuthListener()
    mListener.listener()
    UMShareAPI.get(this).getPlatformInfo(this, platform, mListener)
}

class _UMAuthListener : UMAuthListener {

    /**
     * @desc 授权成功的回调
     * @param platform 平台名称
     * @param action 行为序号，开发者用不上
     * @param data 用户资料返回
     */
    override fun onComplete(platform: SHARE_MEDIA, action: Int, data: MutableMap<String, String>) {
        _onComplete?.invoke(platform, action, data)
    }

    private var _onComplete: ((SHARE_MEDIA, Int, MutableMap<String, String>) -> Unit)? = null

    fun onComplete(listener: (SHARE_MEDIA, Int, MutableMap<String, String>) -> Unit) {
        _onComplete = listener
    }

    /**
     * @desc 授权取消的回调
     * @param platform 平台名称
     * @param action 行为序号，开发者用不上
     */
    override fun onCancel(platform: SHARE_MEDIA, action: Int) {
        _onCancel?.invoke(platform, action)
    }

    private var _onCancel: ((SHARE_MEDIA, Int) -> Unit)? = null

    fun onCancel(listener: (SHARE_MEDIA, Int) -> Unit) {
        _onCancel = listener
    }

    /**
     * @desc 授权失败的回调
     * @param platform 平台名称
     * @param action 行为序号，开发者用不上
     * @param t 错误原因
     */
    override fun onError(platform: SHARE_MEDIA, action: Int, throwable: Throwable) {
        _onError?.invoke(platform, action, throwable)
    }

    private var _onError: ((SHARE_MEDIA, Int, Throwable) -> Unit)? = null

    fun onError(listener: (SHARE_MEDIA, Int, Throwable) -> Unit) {
        _onError = listener
    }

    /**
     * @desc 授权开始的回调
     * @param platform 平台名称
     */
    override fun onStart(platform: SHARE_MEDIA) {
        _onStart?.invoke(platform)
    }

    private var _onStart: ((SHARE_MEDIA) -> Unit)? = null

    fun onStart(listener: (SHARE_MEDIA) -> Unit) {
        _onStart = listener
    }

}
