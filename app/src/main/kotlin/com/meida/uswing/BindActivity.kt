package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.putBoolean
import com.meida.base.putString
import com.meida.share.BaseHttp
import com.meida.share.Const
import com.meida.utils.*
import kotlinx.android.synthetic.main.activity_bind.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject

class BindActivity : BaseActivity() {

    private var timeCount: Int = 180
    private lateinit var thread: Runnable
    private var mYZM: String = ""
    private var mTel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind)
        init_title("绑定手机号")
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_yzm -> {
                when {
                    bind_tel.text.isBlank() -> {
                        bind_tel.requestFocus()
                        toast("请输入手机号")
                        return
                    }
                    !bind_tel.text.isMobile() -> {
                        bind_tel.requestFocus()
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

                EncryptUtil.DESIV = EncryptUtil.getiv(Const.MAKER)
                val encodeTel =
                    DESUtil.encode(EncryptUtil.getkey(Const.MAKER), bind_tel.text.toString())

                OkGo.post<String>(BaseHttp.identify_get2)
                    .tag(this@BindActivity)
                    .params("mobile", encodeTel)
                    .params("time", Const.MAKER)
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(
                            response: Response<String>,
                            msg: String,
                            msgCode: String
                        ) {

                            mYZM = JSONObject(response.body()).optString("object")
                            mTel = bind_tel.text.toString()
                            if (BuildConfig.LOG_DEBUG) {
                                bind_yzm.setText(mYZM)
                                bind_yzm.setSelection(bind_yzm.text.length)
                            }

                            tv_yzm.isClickable = false
                            timeCount = 180
                            tv_yzm.post(thread)
                        }

                    })
            }
            R.id.bt_bind -> {
                when {
                    bind_tel.text.isBlank() -> {
                        bind_tel.requestFocus()
                        toast("请输入手机号")
                        return
                    }
                    bind_yzm.text.isBlank() -> {
                        bind_yzm.requestFocus()
                        toast("请输入验证码")
                        return
                    }
                    bind_pwd.text.isBlank() -> {
                        bind_pwd.requestFocus()
                        toast("请输入6~20位登录新密码")
                        return
                    }
                    !bind_tel.text.isMobile() -> {
                        bind_tel.requestFocus()
                        toast("请输入正确的手机号")
                        return
                    }
                    bind_tel.text.toString() != mTel -> {
                        toast("手机号码不匹配，请重新获取验证码")
                        return
                    }
                    bind_yzm.text.toString() != mYZM -> {
                        bind_yzm.requestFocus()
                        bind_yzm.setText("")
                        toast("请输入正确的验证码")
                        return
                    }
                    bind_pwd.text.length < 6 -> {
                        toast("新密码长度不少于6位")
                        return
                    }
                }

                OkGo.post<String>(BaseHttp.login_sub)
                    .tag(this@BindActivity)
                    .isMultipart(true)
                    .params("mobile", mTel)
                    .params("smscode", bind_yzm.text.trimString())
                    .params("password", bind_pwd.text.trimString())
                    .params("loginType", "WX")
                    .params("openId", intent.getStringExtra("openId"))
                    .params("nickName", intent.getStringExtra("nickName"))
                    .params("headImgUrl", intent.getStringExtra("headImgUrl"))
                    .execute(object : StringDialogCallback(baseContext) {
                        override fun onSuccessResponse(
                            response: Response<String>,
                            msg: String,
                            msgCode: String
                        ) {

                            val obj = JSONObject(response.body()).getJSONObject("object")

                            putBoolean("isLogin", true)
                            putString("token", obj.optString("token"))
                            putString("rongToken", obj.optString("rongtoken"))
                            putString("mobile", obj.optString("mobile"))
                            putString("nickName", obj.optString("nick_name"))
                            putString("userHead", obj.optString("user_head"))
                            putString("loginType", intent.getStringExtra("loginType"))

                            startActivity<MainActivity>()
                            ActivityStack.screenManager.popActivities(
                                this@BindActivity::class.java,
                                LoginActivity::class.java
                            )
                        }

                    })
            }
        }
    }

}
