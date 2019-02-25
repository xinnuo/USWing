package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.meida.base.BaseActivity
import com.meida.model.RefreshMessageEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.*

class CoachInfoActivity : BaseActivity() {

    private lateinit var mInfoView :TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        frameLayout {
            backgroundColorResource = R.color.white

            mInfoView = themedTextView(R.style.Font14_black) {
                padding = dip(15)
                setLineSpacing(dip(10).toFloat(), 1f)
            }
        }
        init_title("我的简介", "修改")

        EventBus.getDefault().register(this@CoachInfoActivity)

        mInfoView.text = intent.getStringExtra("info")
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_nav_right -> startActivity<CoachEditActivity>("title" to "修改简介")
        }
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@CoachInfoActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "修改简介" -> mInfoView.text = event.id
        }
    }

}
