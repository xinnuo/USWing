package com.meida.utils;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class LandDecoration extends RecyclerView.ItemDecoration {

    private int gapSize;

    public LandDecoration() {
        gapSize = DensityHelperKt.dp2px(10);
    }

    public LandDecoration(int gapSize) {
        this.gapSize = DensityHelperKt.dp2px(gapSize);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildLayoutPosition(view);
        int adapterPostion = parent.getChildAdapterPosition(view);

        int totalChildCount = parent.getAdapter().getItemCount();
        int spanCount; // grid的列数
        int spanSize;

        GridLayoutManager.SpanSizeLookup sizeLookup;

        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();
            spanCount = manager.getSpanCount();
            sizeLookup = manager.getSpanSizeLookup();
            spanSize = sizeLookup.getSpanSize(adapterPostion);

            if (spanCount == 0) return;

            //数据视图
            if (spanSize == spanCount) {
                //视图占满一行，不做偏移处理
                outRect.set(0, 0, 0, 0);
            } else {
                int left = 0, top = 0, right = 0, bottom = 0;
                int lastFullSpanCountPos = getLastFullSpanCountPostion(sizeLookup, spanCount, adapterPostion);

                //检查是否位于网格中的最后一列
                boolean isLastCol = isLastGridCol(spanCount, position, lastFullSpanCountPos);

                if (spanCount == 2) {
                    //这里这样分割主要为了让每个grid当中的item的宽度都是保持一致
                    right = isLastCol ? gapSize : gapSize / 2;
                    left = isLastCol ? gapSize / 2 : gapSize;
                } else if (spanCount > 2) {
                    switch ((position - lastFullSpanCountPos) % (spanCount / spanSize)) {
                        case 0:
                            right = gapSize;
                            left = gapSize / 2;
                            break;
                        case 1:
                            right = gapSize / 2;
                            left = gapSize;
                            break;
                        default:
                            right = gapSize / 2;
                            left = gapSize / 2;
                            break;
                    }
                }

                //检查是否允许网格中的第一行元素的marginTop是否允许设置值 -true标识允许
                top = isFristGridRow(spanCount, position, lastFullSpanCountPos) ? 0 : gapSize;

                outRect.set(left, top, right, bottom);
            }

        }

    }

    //寻找最近一个占据spanCount列的位置
    private int getLastFullSpanCountPostion(GridLayoutManager.SpanSizeLookup sizeLookup, int spanCount, int adapterPostion) {

        for (int index = adapterPostion; index >= 0; index--) {
            if (sizeLookup.getSpanSize(index) == spanCount)
                return index;
        }

        return -1;
    }

    //是否为最后一列数据
    public boolean isLastGridCol(int spanCount, int position, int lastFullSpanCountPos) {
        return (position - lastFullSpanCountPos) % spanCount == 0;
    }

    //是否为第一行数据
    public boolean isFristGridRow(int spanCount, int position, int lastFullSpanCountPos) {
        return (position - lastFullSpanCountPos) <= spanCount;
    }

}
