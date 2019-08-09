/**
 * created by 小卷毛, 2019/8/9
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

import android.content.Context
import com.tencent.smtt.sdk.QbSdk

/**
 * 项目名称：USWing
 * 创建人：小卷毛
 * 创建时间：2019-08-09 17:13
 */

fun Context.initX5WebView(listener: _PreInitCallback.() -> Unit) {
    val mListener = _PreInitCallback()
    mListener.listener()
    QbSdk.initX5Environment(this, mListener)
}

class _PreInitCallback: QbSdk.PreInitCallback {

    override fun onCoreInitFinished() {
        _onCoreInitFinished?.invoke()
    }

    private var _onCoreInitFinished: (() -> Unit)? = null

    fun onCoreInitFinished(listener: () -> Unit) {
        _onCoreInitFinished = listener
    }

    override fun onViewInitFinished(isSuccessed: Boolean) {
        _onViewInitFinished?.invoke(isSuccessed)
    }

    private var _onViewInitFinished: ((Boolean) -> Unit)? = null

    fun onViewInitFinished(listener: (Boolean) -> Unit) {
        _onViewInitFinished = listener
    }

}