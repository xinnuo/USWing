/**
 * created by 小卷毛, 2019/1/31
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
package com.meida.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class CenterImageSpan : ImageSpan {

    constructor(drawable: Drawable) : super(drawable)

    constructor(context: Context, resourceId: Int) : super(context, resourceId)

    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {

        val fm = paint.fontMetricsInt
        val transY = (y + fm.descent + y + fm.ascent) / 2 - drawable.bounds.bottom / 2

        canvas.save()
        canvas.translate(x, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }

}