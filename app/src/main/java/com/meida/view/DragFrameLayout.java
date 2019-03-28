package com.meida.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DragFrameLayout extends FrameLayout {

    private List<View> viewList;
    private ViewDragHelper dragHelper;
    private boolean isDragable;

    public DragFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 第二步：创建存放View的集合
        viewList = new ArrayList<>();

        dragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {

            /**
             * 是否捕获childView:
             * 如果viewList包含child，那么捕获childView
             * 如果不包含child，就不捕获childView
             */
            @Override
            public boolean tryCaptureView(@NotNull View child, int pointerId) {
                return viewList.contains(child);
            }

            @Override
            public void onViewPositionChanged(@NotNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                Log.d("onViewPositionChanged", "left=" + left + ",top=" + top + ",dx=" + dx + ",dy=" + dy);
            }

            /**
             * 当捕获到child后的处理：
             * 获取child的监听
             */
            @Override
            public void onViewCaptured(@NotNull View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                if (onDragDropListener != null) {
                    onDragDropListener.onDragDrop(true);
                }
            }

            /**
             * 当释放child后的处理：
             * 取消监听，不再处理
             */
            @Override
            public void onViewReleased(@NotNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                if (onDragDropListener != null) {
                    onDragDropListener.onDragDrop(false);
                }
            }

            /**
             * 当前view的left
             */
            @Override
            public int clampViewPositionHorizontal(@NotNull View child, int left, int dx) {
                // 限定left的范围,不让child超过左右边界
                int maxLeft = getMeasuredWidth() - child.getMeasuredWidth();
                if (left < 0) {
                    left = 0;
                } else if (left > maxLeft) {
                    left = maxLeft;
                }
                return left;
            }

            /**
             * 到上边界的距离
             */
            @Override
            public int clampViewPositionVertical(@NotNull View child, int top, int dy) {
                //限定top的范围,不让child超过上下边界
                int maxTop = getMeasuredHeight() - child.getMeasuredHeight();
                if (top < 0) {
                    top = 0;
                } else if (top > maxTop) {
                    top = maxTop;
                }
                return top;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //当手指抬起或事件取消的时候 就不拦截事件
        int actionMasked = ev.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
            return false;
        }
        return isDragable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    public void addDragChildView(View child) {
        viewList.add(child);
    }

    public void setDragEnable(boolean isEnable) {
        isDragable = isEnable;
    }

    //创建拖动回调
    public interface OnDragDropListener {
        void onDragDrop(boolean captured);
    }

    private OnDragDropListener onDragDropListener;

    public void setOnDragDropListener(OnDragDropListener onDragDropListener) {
        this.onDragDropListener = onDragDropListener;
    }

}
