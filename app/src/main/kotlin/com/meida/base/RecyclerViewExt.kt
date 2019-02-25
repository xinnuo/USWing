/**
 * created by 小卷毛, 2018/03/06
 * Copyright (c) 2018, 416143467@qq.com All Rights Reserved.
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
package com.meida.base

import android.app.Activity
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.bumptech.glide.Glide
import com.meida.uswing.R

fun <T> ArrayList<T>.addItems(items: List<T>? = null): ArrayList<T> {
    if (items != null && items.isNotEmpty()) addAll(items)
    return this
}

fun <T> count(items: List<T>? = null): Int = items?.size ?: 0

/*
 * 内联函数减少内存开销，却增加代码量。要适当地取舍
 * inline fun SwipeRefreshLayout.refresh(crossinline init: SwipeRefreshLayout.() -> Unit) { }
 *
 * 此处方法在很多 Activity 中调用，不应该进行内联
 */
fun SwipeRefreshLayout.refresh(init: SwipeRefreshLayout.() -> Unit) {
    @Suppress("DEPRECATION")
    setColorSchemeColors(resources.getColor(R.color.colorAccent))
    setOnRefreshListener { init() }
}

/*
 * inline fun RecyclerView.load_Linear(mContext: Context, refreshLayout: SwipeRefreshLayout) { }
 * inline fun RecyclerView.loadMore_Linear(mContext: Context, refreshLayout: SwipeRefreshLayout, isLoadingMore: Boolean, crossinline init: RecyclerView.() -> Unit) { }
 *
 * 非内联函数方法，减少方法重载
 */
fun RecyclerView.load_Linear(mContext: Activity,
                             refreshLayout: SwipeRefreshLayout? = null,
                             init: (() -> Unit)? = null) {

    layoutManager = LinearLayoutManager(mContext)

    if (init != null) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val total = layoutManager!!.itemCount
                val lastVisibleItem = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy > 0 表示向下滑动
                if (lastVisibleItem >= total - 1 && dy > 0) init()
            }
        })
    }

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        /*
         * Glide图片加载优化，
         * 只在拖动和静止时加载，自动滑动时不加载
         */
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> Glide.with(context.applicationContext).resumeRequests()
                RecyclerView.SCROLL_STATE_SETTLING -> Glide.with(context.applicationContext).pauseRequests()
                RecyclerView.SCROLL_STATE_IDLE -> Glide.with(context.applicationContext).resumeRequests()
            }
        }
    })

    if (refreshLayout != null) {
        setOnTouchListener { _, _ -> return@setOnTouchListener refreshLayout.isRefreshing }
    }
}

/*
 * inline fun RecyclerView.load_Grid(refreshLayout: SwipeRefreshLayout, init: RecyclerView.() -> Unit) { }
 * inline fun RecyclerView.loadMore_Grid(refreshLayout: SwipeRefreshLayout, isLoadingMore: Boolean, init: RecyclerView.() -> Unit, crossinline load: RecyclerView.() -> Unit) { }
 *
 * 非内联函数方法，减少方法重载
 */
fun RecyclerView.load_Grid(refreshLayout: SwipeRefreshLayout? = null,
                           load: (() -> Unit)? = null,
                           init: RecyclerView.() -> Unit) {

    init()

    if (load != null) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val total = layoutManager?.itemCount ?: 0
                val lastVisibleItem = (layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy > 0 表示向下滑动
                if (lastVisibleItem >= total - 1 && dy > 0) load()
            }
        })
    }

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        /*
         * Glide图片加载优化，
         * 只在拖动和静止时加载，自动滑动时不加载
         */
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> Glide.with(context.applicationContext).resumeRequests()
                RecyclerView.SCROLL_STATE_SETTLING -> Glide.with(context.applicationContext).pauseRequests()
                RecyclerView.SCROLL_STATE_IDLE -> Glide.with(context.applicationContext).resumeRequests()
            }
        }
    })

    if (refreshLayout != null) {
        setOnTouchListener { _, _ -> return@setOnTouchListener refreshLayout.isRefreshing }
    }
}