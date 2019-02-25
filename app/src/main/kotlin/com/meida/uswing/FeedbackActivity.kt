package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.trimString
import kotlinx.android.synthetic.main.activity_feedback.*
import org.jetbrains.anko.toast

class FeedbackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        init_title("意见反馈")
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_submit -> {
                if (feedback_content.text.isBlank()) {
                    toast("请输入内容")
                    return
                }

                OkGo.post<String>(BaseHttp.leave_message_sub)
                    .tag(this@FeedbackActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("content", feedback_content.text.trimString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            ActivityStack.screenManager.popActivities(this@FeedbackActivity::class.java)
                        }

                    })
            }
        }
    }
}
