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
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Scrollview 嵌套 RecyclerView 高度自适应
 */
class FullyGridLayoutManager : GridLayoutManager {

    private var mwidth = 0
    private var mheight = 0
    private var isScrollEnabled = true
    private val mMeasuredDimension = IntArray(2)

    constructor(context: Context, spanCount: Int) : super(context, spanCount)

    constructor(context: Context, spanCount: Int, orientation: Int) : super(context, spanCount, orientation, false)

    constructor(context: Context, spanCount: Int, orientation: Int, reverseLayout: Boolean) : super(context, spanCount, orientation, reverseLayout)

    override fun onMeasure(recycler: RecyclerView.Recycler,
                           state: RecyclerView.State,
                           widthSpec: Int,
                           heightSpec: Int) {

        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)

        var width = 0
        var height = 0
        val count = itemCount
        val span = spanCount

        (0 until count).forEach {
            measureScrapChild(recycler, it,
                    View.MeasureSpec.makeMeasureSpec(it, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(it, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension)

            if (orientation == LinearLayoutManager.HORIZONTAL) {
                when {
                    it % span == 0 -> width += mMeasuredDimension[0]
                    it == 0 -> height = mMeasuredDimension[1]
                }
            } else {
                when {
                    it % span == 0 -> height += mMeasuredDimension[1]
                    it == 0 -> width = mMeasuredDimension[0]
                }
            }
        }

        if (widthMode == View.MeasureSpec.EXACTLY) width = widthSize
        if (heightMode == View.MeasureSpec.EXACTLY) height = heightSize

        mheight = height
        mwidth = width
        setMeasuredDimension(width, height)
    }

    private fun measureScrapChild(recycler: RecyclerView.Recycler?,
                                  position: Int,
                                  widthSpec: Int,
                                  heightSpec: Int,
                                  measuredDimension: IntArray) {

        if (position < itemCount) {
            try {
                val view = recycler?.getViewForPosition(0) //fix 动态添加时报IndexOutOfBoundsException
                if (view != null) {
                    val p = view.layoutParams as RecyclerView.LayoutParams
                    val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                            paddingLeft + paddingRight, p.width)
                    val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                            paddingTop + paddingBottom, p.height)
                    view.measure(childWidthSpec, childHeightSpec)
                    measuredDimension[0] = view.measuredWidth + p.leftMargin + p.rightMargin
                    measuredDimension[1] = view.measuredHeight + p.bottomMargin + p.topMargin
                    recycler.recycleView(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun canScrollVertically(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically()
    }

}
