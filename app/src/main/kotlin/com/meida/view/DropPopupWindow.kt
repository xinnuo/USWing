/**
 * created by 小卷毛, 2019/4/24
 * Copyright (c) 2019, 416143467@qq.com All Rights Reserved.
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
package com.meida.view

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import com.meida.uswing.R
import com.meida.utils.startRotateAnimator

/**
 * 注：
 * 1、函数的声明可以是val也可以是var
 * 2、当不在主构造函数中声明又想当全局变量使用，可在类中声明，主函数中声明是简化了其写法。
 *    当不在主构造函数中声明时，只能在初始化块以及属性声明中使用
 *
 * 注：
 * 在有默认参数值的方法中使用@JvmOverloads注解，则Kotlin就会暴露多个重载方法。
 * 该注解也可用在构造方法和静态方法
 */
abstract class DropPopupWindow @JvmOverloads constructor(
    view: View,
    private val indicator: View? = null
) : PopupWindow(
    view,
    WindowManager.LayoutParams.MATCH_PARENT,
    WindowManager.LayoutParams.WRAP_CONTENT,
    true
) {

    private lateinit var mContainer: View
    private val animationIn: Animation = AnimationUtils.loadAnimation(contentView.context, R.anim.pop_anim_show)
    private val animationOut: Animation = AnimationUtils.loadAnimation(contentView.context, R.anim.pop_anim_dismiss)
    private var isDismiss = false

    init {
        initView()
        afterInitView(view)
    }

    private fun initView() {
        mContainer = contentView.findViewById(R.id.pop_container)

        mContainer.setOnClickListener { }
        contentView.findViewById<View>(R.id.pop_root).setOnClickListener { dismiss() }
    }

    abstract fun afterInitView(view: View)

    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT == 24) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            height = anchor.resources.displayMetrics.heightPixels - rect.bottom
        }
        super.showAsDropDown(anchor)
        isDismiss = false
        mContainer.startAnimation(animationIn)
        indicator?.startRotateAnimator(0f, 180f)
    }

    override fun dismiss() {
        if (!isDismiss) {
            isDismiss = true
            mContainer.startAnimation(animationOut)
            dismiss()
            animationOut.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {
                    indicator?.startRotateAnimator(180f, 0f)
                }

                @SuppressLint("ObsoleteSdkInt")
                override fun onAnimationEnd(animation: Animation) {
                    this@DropPopupWindow.isDismiss = false

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
                        this@DropPopupWindow.dismiss4Pop()
                    else super@DropPopupWindow.dismiss()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }
    }

    private fun dismiss4Pop() {
        Handler().post { super@DropPopupWindow.dismiss() }
    }

}