package com.meida.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

/**
 * 限制中文、英文、数字包括下划线
 */
class InputLimitFilter : InputFilter {

    private val regEx = "^[\\u4E00-\\u9FA5A-Za-z0-9_]+$"

    //source:即将输入的内容 dest：原来输入的内容
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val matcher = Pattern.compile(regEx).matcher(source)
        return if (matcher.find()) "" else source
    }

}
