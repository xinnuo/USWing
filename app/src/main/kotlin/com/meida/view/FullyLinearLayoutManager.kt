/**
 * created by 小卷毛, 2016/12/13
 * Copyright (c) 2016, 416143467@qq.com All Rights Reserved.
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG             #
 * #                                                   #
 */
package com.meida.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Scrollview 嵌套 RecyclerView 高度自适应
 */
class FullyLinearLayoutManager : LinearLayoutManager {

    private var isScrollEnabled = true
    private val mMeasuredDimension = IntArray(2)

    constructor(context: Context) : super(context)

    constructor(context: Context, orientation: Int) : super(context, orientation, false)

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    )

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int, heightSpec: Int
    ) {

        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)

        var width = 0
        var height = 0

        (0 until itemCount).forEach {
            measureScrapChild(
                recycler,
                View.MeasureSpec.makeMeasureSpec(it, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(it, View.MeasureSpec.UNSPECIFIED),
                mMeasuredDimension
            )

            if (orientation == LinearLayoutManager.HORIZONTAL) {
                width += mMeasuredDimension[0]
                if (it == 0) height = mMeasuredDimension[1]
            } else {
                height += mMeasuredDimension[1]
                if (it == 0) width = mMeasuredDimension[0]
            }
        }

        if (widthMode == View.MeasureSpec.EXACTLY) width = widthSize
        if (heightMode == View.MeasureSpec.EXACTLY) height = heightSize

        setMeasuredDimension(width, height)
    }

    private fun measureScrapChild(
        recycler: RecyclerView.Recycler?,
        widthSpec: Int,
        heightSpec: Int,
        measuredDimension: IntArray
    ) {

        try {
            val view = recycler?.getViewForPosition(0) //fix 动态添加时报IndexOutOfBoundsException
            if (view != null) {
                val p = view.layoutParams as RecyclerView.LayoutParams

                val childWidthSpec = ViewGroup.getChildMeasureSpec(
                    widthSpec,
                    paddingLeft + paddingRight, p.width
                )

                val childHeightSpec = ViewGroup.getChildMeasureSpec(
                    heightSpec,
                    paddingTop + paddingBottom, p.height
                )

                view.measure(childWidthSpec, childHeightSpec)
                measuredDimension[0] = view.measuredWidth + p.leftMargin + p.rightMargin
                measuredDimension[1] = view.measuredHeight + p.bottomMargin + p.topMargin
                recycler.recycleView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun canScrollVertically(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically()
    }

}