package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.isMobile
import kotlinx.android.synthetic.main.activity_forget.*
import org.jetbrains.anko.toast
import org.json.JSONObject

class ForgetActivity : BaseActivity() {

    private var timeCount: Int = 180
    private lateinit var thread: Runnable
    private var mYZM: String = ""
    private var mTel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget)
        init_title("忘记密码")
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_yzm -> {
                when {
                    forget_tel.text.isBlank() -> {
                        forget_tel.requestFocus()
                        toast("请输入手机号")
                        return
                    }
                    !forget_tel.text.isMobile() -> {
                        forget_tel.requestFocus()
                        toast("请输入正确的手机号")
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

                OkGo.post<String>(BaseHttp.identify_getbyforget)
                    .tag(this@ForgetActivity)
                    .params("mobile", forget_tel.text.toString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            mYZM = JSONObject(response.body()).optString("object")
                            mTel = forget_tel.text.toString()
                            if (BuildConfig.LOG_DEBUG) {
                                forget_yzm.setText(mYZM)
                                forget_yzm.setSelection(forget_yzm.text.length)
                            }

                            tv_yzm.isClickable = false
                            timeCount = 180
                            tv_yzm.post(thread)
                        }

                    })
            }
            R.id.bt_submit -> {
                when {
                    forget_tel.text.isBlank() -> {
                        forget_tel.requestFocus()
                        toast("请输入手机号")
                        return
                    }
                    forget_yzm.text.isBlank() -> {
                        forget_yzm.requestFocus()
                        toast("请输入验证码")
                        return
                    }
                    forget_pwd.text.isBlank() -> {
                        forget_pwd.requestFocus()
                        toast("请输入6~20位登录新密码")
                        return
                    }
                    !forget_tel.text.isMobile() -> {
                        forget_tel.requestFocus()
                        toast("请输入正确的手机号")
                        return
                    }
                    forget_tel.text.toString() != mTel -> {
                        toast("手机号码不匹配，请重新获取验证码")
                        return
                    }
                    forget_yzm.text.toString() != mYZM -> {
                        forget_yzm.requestFocus()
                        forget_yzm.setText("")
                        toast("请输入正确的验证码")
                        return
                    }
                    forget_pwd.text.length < 6 -> {
                        toast("新密码长度不少于6位")
                        return
                    }
                }

                OkGo.post<String>(BaseHttp.pwd_forget_sub)
                    .tag(this@ForgetActivity)
                    .params("mobile", mTel)
                    .params("smscode", forget_yzm.text.toString())
                    .params("newpwd", forget_pwd.text.toString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            ActivityStack.screenManager.popActivities(this@ForgetActivity::class.java)
                        }

                    })
            }
        }
    }
}
