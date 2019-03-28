package com.meida.utils

import android.app.Activity
import java.util.*

class ActivityStack private constructor() {

    /**
     * 移除栈顶的activity
     */
    fun popActivity() {
        if (mActivityStack.isNotEmpty()) {
            val activity = mActivityStack.pop()
            activity.finish()
        }
    }

    /**
     * 移除一个activity
     */
    private fun popActivity(index: Int, activity: Activity) {
        if (!activity.isDestroyed) activity.finish()
        mActivityStack.removeElementAt(index)
    }

    /**
     * 获取栈顶的activity，先进后出原则
     *
     * lastElement()获取最后个子元素，这里是栈顶的Activity
     */
    fun currentActivity(): Activity? =
        if (mActivityStack.isEmpty()) null else mActivityStack.lastElement()

    /**
     * 将当前Activity推入栈中
     */
    fun pushActivity(activity: Activity) = mActivityStack.addElement(activity)

    /**
     * 是否包含未销毁的Activity
     */
    fun isContainsActivity(cls: Class<*>): Boolean {
        if (mActivityStack.isEmpty()) return false
        return mActivityStack.any { it.javaClass == cls && !it.isDestroyed }
    }

    /**
     * 弹出栈中指定Activity
     */
    fun popOneActivity(cls: Class<*>) {
        if (mActivityStack.any { it.javaClass == cls }) {
            val index = mActivityStack.size - 1
            (index downTo 0).forEach {
                val activity = mActivityStack[it]
                if (activity.javaClass == cls) popActivity(it, activity)
            }
        }
    }

    /**
     * 弹出栈中所有Activity，保留指定的一个Activity
     */
    fun popAllActivityExceptOne(cls: Class<*>) {
        if (mActivityStack.isNotEmpty()) {
            val index = mActivityStack.size - 1
            (index downTo 0).forEach {
                val activity = mActivityStack[it]
                if (activity.javaClass != cls) popActivity(it, activity)
            }
        }
    }

    /**
     * 弹出栈中所有Activity，保留指定的Activity
     */
    fun popAllActivityExcept(vararg clss: Class<*>) {
        if (mActivityStack.isNotEmpty()) {
            val index = mActivityStack.size - 1
            (index downTo 0).forEach {
                val activity = mActivityStack[it]
                if (activity.javaClass !in clss) popActivity(it, activity)
            }
        }
    }

    /**
     * 移除指定的多个activity
     */
    fun popActivities(vararg clss: Class<*>) = clss.forEach { popOneActivity(it) }

    /**
     * 弹出栈中所有Activity
     */
    fun popAllActivitys() {
        if (mActivityStack.isNotEmpty()) {
            val index = mActivityStack.size - 1
            (index downTo 0).forEach { popActivity(it, mActivityStack[it]) }
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
