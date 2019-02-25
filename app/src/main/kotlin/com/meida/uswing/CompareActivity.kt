package com.meida.uswing

import android.os.Bundle
import com.meida.base.BaseActivity

class CompareActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)
        init_title("魔镜对比")
    }
}
