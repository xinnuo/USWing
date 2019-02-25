package com.meida.base;

import android.content.Context;

public abstract class BaseDialog extends com.flyco.dialog.widget.base.BaseDialog {

    public BaseDialog(Context context) {
        super(context);
    }

    public BaseDialog(Context context, boolean isPopupStyle) {
        super(context, isPopupStyle);
    }

    @Override
    public void setUiBeforShow() { }

}
