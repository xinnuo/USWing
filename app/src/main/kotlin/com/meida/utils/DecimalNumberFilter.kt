package com.meida.utils

import android.text.InputFilter
import android.text.Spanned

/**
 * edittext保留两位小数，控制最大值
 */
class DecimalNumberFilter : InputFilter {

    private val decimalNumber = 2 //小数点后保留位数

    //source:即将输入的内容 dest：原来输入的内容
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val sourceContent = source.toString()
        val lastInputContent = dest.toString()

        return when {
            //验证删除等按键
            sourceContent.isEmpty() -> ""
            //以"0"开头，只能输入"."
            lastInputContent == "0" && sourceContent != "." -> ""
            //以小数点"."开头，默认为设置为"0."开头
            sourceContent == "." && lastInputContent.isEmpty() -> "0."
            //输入"0"，默认设置为以"0."开头
            sourceContent == "0" && lastInputContent.isEmpty() -> "0."
            //小数点后保留两位
            lastInputContent.contains(".") -> {
                val index = lastInputContent.indexOf(".")
                if (dend - index >= decimalNumber + 1) "" else source
            }
            else -> source
        }
    }

}
