package com.meida.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * <pre>
 * 键盘相关工具类
 * </pre>
 */

/**
 * 动态强制显示软键盘
 */
fun Activity.showForcedSoftInput() {
    var view = currentFocus
    if (view == null) view = View(this)
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
}

/**
 * 动态隐藏软键盘
 */
fun Activity.hideForcedSoftInput() {
    var view = currentFocus
    if (view == null) view = View(this)
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * 显示软键盘，Dialog使用
 */
fun Activity.showSoftInput() {
    val inputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * 隐藏软键盘
 */
fun Activity.hideSoftInput() {
    val inputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}
