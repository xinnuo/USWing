/**
 * created by 小卷毛, 2018/3/14 0014
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

package com.meida.base

import android.text.Html
import android.text.Spanned
import android.widget.TextView
import com.meida.uswing.R

/**
 * 文本加载扩展类
 */
fun TextView.setColor(text: String, key: String) {
    @Suppress("DEPRECATION")
    setText(Html.fromHtml(text.replace(key, "<font color='${resources.getColor(R.color.colorAccent)}'>$key</font>")))
}

fun TextView.setColor(text: String, key: String, color: String) {
    @Suppress("DEPRECATION")
    setText(Html.fromHtml(text.replace(key, "<font color='$color'>$key</font>")))
}

fun TextView.setUnicodeText(text: String) {
    if (text.isEmpty()) setText(text)
    else {
        var start = 0
        var end: Int
        val buffer = StringBuilder()
        while (start > -1) {
            end = text.indexOf("\\u", start + 2)
            val charStr = text.substring(start + 2, if (end == -1) text.length else end)
            val letter = Integer.parseInt(charStr, 16).toChar() // 16进制parse整形字符串
            buffer.append(Character.valueOf(letter).toString())
            start = end
        }
        setText(buffer.toString())
    }
}

@Suppress("DEPRECATION")
fun getColorText(text: String, key: String): Spanned = Html.fromHtml(text.replace(key, "<font color='#C20D23'>$key</font>"))

@Suppress("DEPRECATION")
fun getColorText(text: String, key: String, color: String): Spanned = Html.fromHtml(text.replace(key, "<font color='$color'>$key</font>"))