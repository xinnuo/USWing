package com.meida.base;

import android.content.Context;

public abstract class BasePopup extends com.flyco.dialog.widget.popup.base.BasePopup {

    public BasePopup(Context context) {
        super(context);
    }

    @Override
    public void setUiBeforShow() { }

}
