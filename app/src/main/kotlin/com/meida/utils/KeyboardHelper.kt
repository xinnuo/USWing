package com.ruanmeng.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * <pre>
 * 键盘相关工具类
 * </pre>
 */
object KeyboardHelper {

    /**
     * 动态强制显示软键盘
     *
     * @param activity activity
     */
    fun showForcedSoftInput(activity: Activity) {
        var view = activity.currentFocus
        if (view == null) view = View(activity)
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    }

    /**
     * 动态隐藏软键盘
     *
     * @param activity activity
     */
    fun hideForcedSoftInput(activity: Activity) {
        var view = activity.currentFocus
        if (view == null) view = View(activity)
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * 显示软键盘，Dialog使用
     *
     * @param activity 当前Activity
     */
    fun showSoftInput(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    /**
     * 隐藏软键盘
     *
     * @param activity 当前Activity
     */
    fun hideSoftInput(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
    }

}