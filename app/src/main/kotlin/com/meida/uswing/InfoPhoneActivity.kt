package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.putString
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.isMobile
import kotlinx.android.synthetic.main.activity_info_phone.*
import org.jetbrains.anko.toast
import org.json.JSONObject

class InfoPhoneActivity : BaseActivity() {

    private var timeCount: Int = 180
    private lateinit var thread: Runnable
    private var mYZM: String = ""
    private var mTel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_phone)
        init_title("更换手机号")
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_yzm -> {
                when {
                    tel_now.text.isBlank() -> {
                        tel_now.requestFocus()
                        toast("请输入当前手机号")
                        return
                    }
                    !tel_now.text.isMobile() -> {
                        tel_now.requestFocus()
                        toast("请输入正确的当前手机号")
                        return
                    }
                }

                thread = Runnable {
                    tv_yzm.text = "${timeCount}秒后重发"
                    if (timeCount > 0) {
                        tv_yzm.postDelayed(thread, 1000)
                        timeCount--
                    } else {
                        tv_yzm.text = "获取验证码"
                        tv_yzm.isClickable = true
                        timeCount = 180
                    }
                }

                OkGo.post<String>(BaseHttp.identify_get_mobile)
                    .tag(this@InfoPhoneActivity)
                    .params("mobile", tel_now.text.toString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            mYZM = JSONObject(response.body()).optString("object")
                            mTel = tel_now.text.toString()
                            if (BuildConfig.LOG_DEBUG) {
                                tel_yzm.setText(mYZM)
                                tel_yzm.setSelection(tel_yzm.text.length)
                            }

                            tv_yzm.isClickable = false
                            timeCount = 180
                            tv_yzm.post(thread)
                        }

                    })
            }
            R.id.bt_submit -> {
                when {
                    tel_now.text.isBlank() -> {
                        tel_now.requestFocus()
                        toast("请输入当前手机号")
                        return
                    }
                    tel_yzm.text.isBlank() -> {
                        tel_yzm.requestFocus()
                        toast("请输入验证码")
                        return
                    }
                    tel_new.text.isBlank() -> {
                        tel_new.requestFocus()
                        toast("请输入新手机号")
                        return
                    }
                    tel_pwd.text.isBlank() -> {
                        tel_pwd.requestFocus()
                        toast("请输入新密码")
                        return
                    }
                    !tel_now.text.isMobile() -> {
                        tel_now.requestFocus()
                        toast("请输入正确的当前手机号")
                        return
                    }
                    !tel_new.text.isMobile() -> {
                        tel_new.requestFocus()
                        toast("请输入正确的新手机号")
                        return
                    }
                    tel_now.text.toString() != mTel -> {
                        toast("当前手机号码不匹配，请重新获取验证码")
                        return
                    }
                    tel_yzm.text.toString() != mYZM -> {
                        tel_yzm.requestFocus()
                        tel_yzm.setText("")
                        toast("请输入正确的验证码")
                        return
                    }
                    tel_pwd.text.length < 6 -> {
                        toast("新密码长度不少于6位")
                        return
                    }
                }

                OkGo.post<String>(BaseHttp.update_mobile)
                    .tag(this@InfoPhoneActivity)
                    .params("mobile", tel_new.text.toString())
                    .params("smscode", tel_yzm.text.toString())
                    .params("password", tel_pwd.text.toString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            putString("mobile", tel_new.text.toString())
                            ActivityStack.screenManager.popActivities(this@InfoPhoneActivity::class.java)
                        }

                    })
            }
        }
    }
}
