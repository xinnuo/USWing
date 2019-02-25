/**
 * created by 小卷毛, 2018/11/2
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

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

/**
 * 常用单位转换的辅助类
 */


/**
 * dp转px
 *
 * @param dpVal dp值
 * @return      px值
 */
fun dp2px(dpVal: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dpVal,
        Resources.getSystem().displayMetrics).toInt()

/**
 * sp转px
 *
 * @param spVal sp值
 * @return      px值
 */
fun sp2px(spVal: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        spVal,
        Resources.getSystem().displayMetrics).toInt()

/**
 * px转dp
 *
 * @param pxVal px值
 * @return      dp值
 */
fun px2dp(pxVal: Float) = pxVal / Resources.getSystem().displayMetrics.density

/**
 * px转sp
 *
 * @param pxVal px值
 * @return      sp值
 */
fun px2sp(pxVal: Float) = pxVal / Resources.getSystem().displayMetrics.scaledDensity

/**
 * 各种单位转换，该方法存在于TypedValue
 *
 * @param unit    单位
 * @param value   值
 * @param metrics DisplayMetrics
 * @return 转换结果
 */
fun applyDimension(
        unit: Int,
        value: Float,
        metrics: DisplayMetrics) = when (unit) {
    TypedValue.COMPLEX_UNIT_PX -> value
    TypedValue.COMPLEX_UNIT_DIP -> value * metrics.density
    TypedValue.COMPLEX_UNIT_SP -> value * metrics.scaledDensity
    TypedValue.COMPLEX_UNIT_PT -> value * metrics.xdpi * (1.0f / 72)
    TypedValue.COMPLEX_UNIT_IN -> value * metrics.xdpi
    TypedValue.COMPLEX_UNIT_MM -> value * metrics.xdpi * (1.0f / 25.4f)
    else -> 0f
}