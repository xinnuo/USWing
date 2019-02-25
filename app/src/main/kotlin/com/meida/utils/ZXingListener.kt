/**
 * created by 小卷毛, 2019/1/28
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

import cn.bingoogolapple.qrcode.core.QRCodeView

/**
 * 项目名称：USWing
 * 创建人：小卷毛
 * 创建时间：2019-01-28 14:03
 */
fun QRCodeView.setDelegate(init: _Delegate.() -> Unit) {
    val listener = _Delegate()
    listener.init()
    setDelegate(listener)
}

open class _Delegate : QRCodeView.Delegate {

    private var _onScanQRCodeSuccess: ((String?) -> Unit)? = null

    /**
     * 处理扫描结果
     *
     * @param result 摄像头扫码时只要回调了该方法 result 就一定有值，不会为 null。解析本地图片或 Bitmap 时 result 可能为 null
     */
    override fun onScanQRCodeSuccess(result: String?) {
        _onScanQRCodeSuccess?.invoke(result)
    }

    fun onScanQRCodeSuccess(listener: (String?) -> Unit) {
        _onScanQRCodeSuccess = listener
    }

    private var _onCameraAmbientBrightnessChanged: ((Boolean) -> Unit)? = null

    /**
     * 摄像头环境亮度发生变化
     *
     * @param isDark 是否变暗
     */
    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        _onCameraAmbientBrightnessChanged?.invoke(isDark)
    }

    fun onCameraAmbientBrightnessChanged(listener: (Boolean) -> Unit) {
        _onCameraAmbientBrightnessChanged = listener
    }

    private var _onScanQRCodeOpenCameraError: (() -> Unit)? = null

    /**
     * 处理打开相机出错
     */
    override fun onScanQRCodeOpenCameraError() {
        _onScanQRCodeOpenCameraError?.invoke()
    }

    fun onCameraAmbientBrightnessChanged(listener: () -> Unit) {
        _onScanQRCodeOpenCameraError = listener
    }

}