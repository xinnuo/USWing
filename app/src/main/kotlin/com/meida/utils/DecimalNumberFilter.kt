package com.meida.utils

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils

/**
 * edittext保留两位小数，控制最大值
 */
class DecimalNumberFilter : InputFilter {

    private val decimalNumber = 2 //小数点后保留位数

    //source:即将输入的内容 dest：原来输入的内容
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
        val sourceContent = source.toString()
        val lastInputContent = dest.toString()

        //验证删除等按键
        if (TextUtils.isEmpty(sourceContent)) return ""

        //以小数点"."开头，默认为设置为“0.”开头
        if (sourceContent == "." && lastInputContent.isEmpty()) return "0."

        //输入“0”，默认设置为以"0."开头
        if (sourceContent == "0" && lastInputContent.isEmpty()) return "0."

        //小数点后保留两位
        if (lastInputContent.contains(".")) {
            val index = lastInputContent.indexOf(".")
            if (dend - index >= decimalNumber + 1) return ""
        }

        return source
    }

}
