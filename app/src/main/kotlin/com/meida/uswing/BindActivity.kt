package com.meida.uswing

import android.os.Bundle
import com.meida.base.BaseActivity

class BindActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)
        init_title("绑定手机号")
    }
}
