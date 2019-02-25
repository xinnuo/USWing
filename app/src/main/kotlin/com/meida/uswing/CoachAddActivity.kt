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
import kotlinx.android.synthetic.main.activity_coach_add.*
import org.jetbrains.anko.toast

class CoachAddActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_add)
        init_title("添加好友")
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_add -> {
                if (add_memo.text.isBlank()) {
                    toast("请输入备注信息")
                    return
                }

                OkGo.post<String>(BaseHttp.add_apply_friend)
                    .tag(this@CoachAddActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("toUserId", intent.getStringExtra("toUserId"))
                    .params("mome", add_memo.text.trimString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            ActivityStack.screenManager.popActivities(this@CoachAddActivity::class.java)
                        }

                    })
            }
        }
    }

}
