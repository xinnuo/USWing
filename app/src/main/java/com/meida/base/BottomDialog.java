package com.meida.base;

import android.content.Context;
import android.view.View;

import com.flyco.dialog.widget.base.BottomBaseDialog;

public abstract class BottomDialog extends BottomBaseDialog {

    public BottomDialog(Context context, View animateView) {
        super(context, animateView);
    }

    public BottomDialog(Context context) {
        super(context);
    }

    @Override
    public void setUiBeforShow() {}
}
