package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.model.LocationMessageEvent
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.trimEndString
import com.meida.utils.trimString
import kotlinx.android.synthetic.main.activity_coach_edit.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import org.json.JSONObject

class CoachEditActivity : BaseActivity() {

    private val mTitle by lazy { intent.getStringExtra("title") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_edit)
        init_title()
    }

    override fun init_title() {
        super.init_title()
        when (mTitle) {
            "添加荣誉", "荣誉认证" -> {
                tvTitle.text = "添加"
                edit_content.hint = "请输入您的荣誉"
                bt_submit.text = "提交"
            }
            "修改简介" -> {
                tvTitle.text = "我的简介"
                edit_content.hint = "请输入您的简介"
                bt_submit.text = "保存"
            }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_submit -> {
                if (edit_content.text.isBlank()) {
                    toast("请输入内容")
                    return
                }

                when (mTitle) {
                    "荣誉认证" -> {
                        EventBus.getDefault().post(
                            LocationMessageEvent(
                                "荣誉认证",
                                edit_content.text.trimString(),
                                System.currentTimeMillis().toString()
                            )
                        )

                        ActivityStack.screenManager.popActivities(this@CoachEditActivity::class.java)
                    }
                    "添加荣誉" -> OkGo.post<String>(BaseHttp.add_honor)
                        .tag(this@CoachEditActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("info", edit_content.text.trimString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                toast(msg)
                                val obj = JSONObject(response.body())
                                    .optJSONObject("object") ?: JSONObject()

                                EventBus.getDefault().post(
                                    RefreshMessageEvent(
                                        "添加荣誉",
                                        obj.optString("honorId"),
                                        obj.optString("honorInfo")
                                    )
                                )
                                ActivityStack.screenManager.popActivities(this@CoachEditActivity::class.java)
                            }

                        })
                    "修改简介" -> OkGo.post<String>(BaseHttp.upadte_certification)
                        .tag(this@CoachEditActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("introduction", edit_content.text.trimEndString())
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                toast(msg)
                                EventBus.getDefault().post(
                                    RefreshMessageEvent(
                                        "修改简介",
                                        edit_content.text.trimEndString()
                                    )
                                )
                                ActivityStack.screenManager.popActivities(this@CoachEditActivity::class.java)
                            }

                        })
                }
            }
        }
    }

}
