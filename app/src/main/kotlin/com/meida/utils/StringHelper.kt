package com.meida.utils

import java.util.regex.Pattern

fun CharSequence.trimString(): String = trim().toString()
fun CharSequence.trimStartString(): String = trimStart().toString()
fun CharSequence.trimEndString(): String = trimEnd().toString()
fun CharSequence.trimToUpperCase(): String = trim().toString().toUpperCase()

fun CharSequence.toTextInt(): Int = toString().toNotInt()
fun CharSequence.toTextFloat(): Float = toString().toNotFloat()
fun CharSequence.toTextDouble(): Double = toString().toNotDouble()

fun String.toNotInt(): Int = if (isEmpty()) "0".toInt() else toInt()
fun String.toNotFloat(): Float = if (isEmpty()) "0".toFloat() else toFloat()
fun String.toNotDouble(): Double = if (isEmpty()) "0".toDouble() else toDouble()

/**
 * 姓名替换，保留姓氏
 * 如果姓名为空 或者 null ,返回空 ；否则，返回替换后的字符串；
 */
fun String.nameReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    else -> replaceAction("(?<=[\\u4e00-\\u9fa5]{" + (if (length > 3) "2" else "1") + "})[\\u4e00-\\u9fa5](?=[\\u4e00-\\u9fa5]{0})")
}

/**
 * 手机号号替换，保留前三位和后四位
 * 如果手机号为空 或者 null ,返回空 ；否则，返回替换后的字符串；
 */
fun String.phoneReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 7 -> this
    else -> replaceAction("(?<=\\d{3})\\d(?=\\d{4})")
}

/**
 * 身份证号替换，保留前四位和后四位
 * 如果身份证号为空 或者 null ,返回空 ；否则，返回替换后的字符串；
 */
fun String.idCardReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 8 -> this
    else -> replaceAction("(?<=\\d{4})\\d(?=\\d{4})")
}

/**
 * 银行卡替换，保留后四位
 * 如果银行卡号为空 或者 null ,返回空 ；否则，返回替换后的字符串；
 */
fun String.bankCardReplaceWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 4 -> this
    else -> replaceAction("(?<=\\d{0})\\d(?=\\d{4})")
}

/**
 * 银行卡替换，保留前六位和后四位
 * 如果银行卡号为空 或者 null ,返回空 ；否则，返回替换后的字符串；
 */
fun String.bankCardReplaceHeaderWithStar(): String = when {
    isNullOrEmpty() -> ""
    length < 10 -> this
    else -> replaceAction( "(?<=\\d{6})\\d(?=\\d{4})")
}

/**
 * 实际替换动作
 */
fun String.replaceAction(regular: String): String = replace(regular.toRegex(), "*")

/**
 * 判断字符串是否为整数和小数
 */
fun CharSequence.isNumeric(): Boolean {
    val pattern = Pattern.compile("-?[0-9]+.?[0-9]+")
    return pattern.matcher(this).matches()
}

/**
 * 车牌号校验（含新能源车牌）
 */
fun CharSequence.isCarNumber(): Boolean {
    if (length < 6) return false
    // val regex = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))\$"
    val regex = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼](([A-HJ-Z][A-HJ-NP-Z0-9]{5})|([A-HJ-Z](([DF][A-HJ-NP-Z0-9][0-9]{4})|([0-9]{5}[DF])))|([A-HJ-Z][A-D0-9][0-9]{3}警)))|([0-9]{6}使)|((([沪粤川云桂鄂陕蒙藏黑辽渝]A)|鲁B|闽D|蒙E|蒙H)[0-9]{4}领)|(WJ[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼·•][0-9]{4}[TDSHBXJ0-9])|([VKHBSLJNGCE][A-DJ-PR-TVY][0-9]{5}))\$"
    val p = Pattern.compile(regex)
    return p.matcher(this).matches()
}

/**
 * 手机号校验
 */
fun CharSequence.isMobile(): Boolean {
    if (length != 11) return false
    val regex = "^((1[3|5|8][0-9])|(14[5|7])|(16[6])|(17[0|1|3|5|6|7|8])|(19[8|9]))\\d{8}$"
    val p = Pattern.compile(regex)
    return p.matcher(this).matches()
}

/**
 * 传真校验
 */
fun CharSequence.isFax(): Boolean {
    val regex = "^((\\d{7,8})|(0\\d{2,3}-\\d{7,8}))$"
    val p = Pattern.compile(regex)
    return p.matcher(this).matches()
}

/**
 * 固话校验
 */
fun CharSequence.isTel(): Boolean {
    val regex = "^((\\d{7,8})|(0\\d{2,3}-\\d{7,8})|(400-\\d{3}-\\d{4})|(1[3456789]\\\\d{9}))$" //固话、400固话、匹配手机

    /*val reg = "(?:(\\(\\+?86\\))(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)|" +
                "(?:(86-?)?(0[0-9]{2,3}\\-?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)";*/
    val p = Pattern.compile(regex)
    return p.matcher(this).matches()
}

/**
 * 邮箱校验
 */
fun CharSequence.isEmail(): Boolean {
    // val regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
    val regex = "^\\s*\\w+(?:\\.?[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"

    val p = Pattern.compile(regex)
    return p.matcher(this).matches()
}

/**
 * 网址校验
 */
fun CharSequence.isWeb(): Boolean {
    val regex = "(http://|ftp://|https://|www)?[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*"
    // val regex = "^([hH][tT]{2}[pP]:/*|[hH][tT]{2}[pP][sS]:/*|[fF][tT][pP]:/*)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+(\\?{0,1}(([A-Za-z0-9-~]+\\={0,1})([A-Za-z0-9-~]*)\\&{0,1})*)$"

    val p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
    return p.matcher(this).matches()
}