package com.meida.utils

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

class MultiGapDecoration
@JvmOverloads
constructor(
        size: Int? = null,
        isEnable: Boolean? = null
) : RecyclerView.ItemDecoration() {

    private var gapSize = dp2px(size?.toFloat() ?: 10f)
    private var isOffsetTopEnabled = isEnable ?: false

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildLayoutPosition(view)
        val adapterPostion = parent.getChildAdapterPosition(view)

        when {
            parent.layoutManager is GridLayoutManager -> {
                val manager = parent.layoutManager as GridLayoutManager
                val spanCount = manager.spanCount                                     //列数
                val sizeLookup = manager.spanSizeLookup //item所占列数管理类
                val spanSize = sizeLookup.getSpanSize(adapterPostion)                 //当前item所占列数

                if (spanCount > 0) {
                    if (spanSize == spanCount) {
                        //item占满一行，不做偏移处理
                        outRect.set(0, 0, 0, 0)
                    } else {
                        var left = 0
                        val top: Int
                        var right = 0
                        val bottom = gapSize
                        val lastFullSpanCountPos = getLastFullSpanCountPostion(sizeLookup, spanCount, adapterPostion)

                        //检查是否位于网格中的最后一列
                        val isLastCol = isLastGridCol(spanCount, position, lastFullSpanCountPos)

                        if (spanCount == 2) {
                            //这里这样分割主要为了让每个grid当中的item的宽度都是保持一致
                            right = if (isLastCol) gapSize else gapSize / 2
                            left = if (isLastCol) gapSize / 2 else gapSize
                        } else if (spanCount > 2) {
                            when ((position - lastFullSpanCountPos) % (spanCount / spanSize)) {
                                0 -> {
                                    right = gapSize
                                    left = gapSize / 2
                                }
                                1 -> {
                                    right = gapSize / 2
                                    left = gapSize
                                }
                                else -> {
                                    right = gapSize / 2
                                    left = gapSize / 2
                                }
                            }
                        }

                        //检查是否允许网格中的第一行元素的marginTop是否允许设置值 -true标识允许
                        top = if (isOffsetTopEnabled && isFristGridRow(spanCount, position, lastFullSpanCountPos)) gapSize else 0

                        outRect.set(left, top, right, bottom)
                    }
                }
            }
            parent.layoutManager is LinearLayoutManager -> {
                outRect.set(0, 0, 0, 0)
                return
            }
            parent.layoutManager is StaggeredGridLayoutManager -> IllegalAccessError("暂时不支持瀑布流")
        }
    }

    //寻找上方最近一个占据spanCount整列的位置
    private fun getLastFullSpanCountPostion(
            sizeLookup: GridLayoutManager.SpanSizeLookup,
            spanCount: Int,
            adapterPostion: Int
    ): Int {
        (adapterPostion downTo 0).forEach {
            if (sizeLookup.getSpanSize(it) == spanCount) return it
        }
        return -1
    }

    //是否为最后一列数据
    private fun isLastGridCol(
            spanCount: Int,
            position: Int,
            lastFullSpanPosition: Int
    ) = (position - lastFullSpanPosition) % spanCount == 0

    //是否为第一行数据
    private fun isFristGridRow(
            spanCount: Int,
            position: Int,
            lastFullSpanPosition: Int
    ) = position - lastFullSpanPosition <= spanCount

}
