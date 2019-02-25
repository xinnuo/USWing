package com.meida.uswing

import android.os.Bundle
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.startActivity

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init_title("设置")
    }

    override fun init_title() {
        super.init_title()

        setting_switch.setOnCheckedChangeListener { _, isChecked ->  }
        setting_deal.oneClick     { startActivity<WebActivity>("title" to "使用协议") }
        setting_private.oneClick  { startActivity<WebActivity>("title" to "隐私说明") }
        setting_about.oneClick    { startActivity<WebActivity>("title" to "关于我们") }
        setting_feedback.oneClick { startActivity<FeedbackActivity>() }
        bt_quit.oneClick          { startActivity<LoginActivity>("offLine" to true) }
    }
}
