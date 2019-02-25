package com.meida.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class DrawableCenterTextView extends AppCompatTextView {

    public DrawableCenterTextView(Context context) {
        super(context);
    }

    public DrawableCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableCenterTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables[0] != null) {  //left
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth;
            drawableWidth = drawables[0].getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            canvas.translate((getWidth() - bodyWidth) / 2, 0);
        } else if (drawables[2] != null) {  //right
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth;
            drawableWidth = drawables[2].getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            setPadding(0, 0, (int) (getWidth() - bodyWidth), 0);
            canvas.translate((getWidth() - bodyWidth) / 2, 0);
        }
        super.onDraw(canvas);
    }
}
