package com.meida.utils

import android.text.InputFilter
import android.text.Spanned

import java.util.regex.Pattern

/**
 * 限制中英文输入长度（一个汉字算两个字母）
 */
class NameLengthFilter(private val MAX_EN: Int) : InputFilter { // 最大英文/数字长度

    private val regEx = "[\\u4e00-\\u9fa5]"  // unicode编码，判断是否为汉字

    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence {
        val destCount = dest.length + getChineseCount(dest.toString())
        val sourceCount = source.length + getChineseCount(source.toString())

        return if (destCount + sourceCount > MAX_EN) "" else source
    }

    private fun getChineseCount(str: String): Int {
        var count = 0
        val m = Pattern.compile(regEx).matcher(str)
        while (m.find()) for (i in 0..m.groupCount()) count += 1
        return count
    }
}
