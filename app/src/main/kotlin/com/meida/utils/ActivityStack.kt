package com.meida.utils

import android.app.Activity
import java.util.*

class ActivityStack private constructor() {

    /**
     * 移除栈顶的activity
     */
    fun popActivity() {
        val activity = mActivityStack.lastElement()
        activity?.finish()
    }

    /**
     * 移除一个activity
     */
    private fun popActivity(activity: Activity?) {
        activity?.let {
            it.finish()
            mActivityStack.remove(it)
        }
    }

    /**
     * 获取栈顶的activity，先进后出原则
     */
    fun currentActivity(): Activity? {
        // lastElement()获取最后个子元素，这里是栈顶的Activity
        return if (mActivityStack.isEmpty()) null else mActivityStack.lastElement()
    }

    /**
     * 将当前Activity推入栈中
     */
    fun pushActivity(activity: Activity) = mActivityStack.add(activity)

    /**
     * 是否包含指定的Activity
     */
    fun isContainsActivity(cls: Class<*>): Boolean {
        if (mActivityStack.isEmpty()) return false
        return mActivityStack.any { it.javaClass == cls && !it.isDestroyed }
    }

    /**
     * 弹出栈中指定Activity
     */
    fun popOneActivity(cls: Class<*>): Boolean {
        if (mActivityStack.isEmpty()) return false
        for (activity in mActivityStack) {
            if (activity.javaClass == cls) {
                if (!activity.isDestroyed) {
                    popActivity(activity)
                    return true
                } else popActivity()
            }
        }
        return false
    }

    /**
     * 弹出栈中所有Activity，保留指定的一个Activity
     */
    fun popAllActivityExceptOne(cls: Class<*>) {
        while (true) {
            val activity = currentActivity() ?: break
            if (activity.javaClass == cls) break
            popActivity(activity)
        }
    }

    /**
     * 移除指定的多个activity
     */
    fun popActivities(vararg clss: Class<*>) = clss.filter { isContainsActivity(it) }.forEach { popOneActivity(it) }

    /**
     * 弹出栈中所有Activity，保留指定的Activity
     */
    fun popAllActivityExcept(vararg clss: Class<*>) {
        for (i in mActivityStack.indices.reversed()) {
            val activity = mActivityStack[i]
            val isNotFinish = clss.any { activity.javaClass == it }
            if (!isNotFinish) popActivity(activity)
        }
    }

    /**
     * 弹出栈中所有Activity
     */
    fun popAllActivitys() {
        while (true) {
            val activity = currentActivity() ?: break
            popActivity(activity)
        }
    }

    companion object {

        /**
         * 注意：mActivityStack 中包含已经 finished 的 activity
         */
        private val mActivityStack: Stack<Activity> = Stack()
        private var instance: ActivityStack? = null

        val screenManager: ActivityStack
            get() {
                if (instance == null) instance = ActivityStack()
                return instance!!
            }
    }

}
