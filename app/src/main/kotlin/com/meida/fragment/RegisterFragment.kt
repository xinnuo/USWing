package com.meida.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseFragment
import com.meida.base.oneClick
import com.meida.share.BaseHttp
import com.meida.uswing.BuildConfig

import com.meida.uswing.R
import com.meida.uswing.WebActivity
import com.meida.utils.isMobile
import kotlinx.android.synthetic.main.fragment_register.*
import org.jetbrains.anko.sdk25.listeners.onCheckedChange
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject

@SuppressLint("SetTextI18n")
class RegisterFragment : BaseFragment() {

    private var timeCount: Int = 180
    private var mYZM: String = ""
    private var mTel: String = ""
    private var isAgreed = true
    private val thread: Runnable by lazy {
        Runnable {
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()
    }

    override fun init_title() {
        register_check.isChecked = true

        register_check.onCheckedChange { _, isChecked -> isAgreed = isChecked }
        register_login.oneClick { (activity as OnFragmentListener).onViewClick("登录") }
        register_deal.oneClick { startActivity<WebActivity>("title" to "注册协议") }

        tv_yzm.oneClick {
            when {
                register_tel.text.isEmpty() -> {
                    register_tel.requestFocus()
                    toast("请输入手机号")
                    return@oneClick
                }
                !register_tel.text.isMobile() -> {
                    register_tel.requestFocus()
                    toast("请输入正确的手机号")
                    return@oneClick
                }
            }

            OkGo.post<String>(BaseHttp.identify_get)
                .tag(this@RegisterFragment)
                .params("mobile", register_tel.text.toString())
                .execute(object : StringDialogCallback(activity) {

                    override fun onSuccessResponse(
                        response: Response<String>,
                        msg: String,
                        msgCode: String
                    ) {

                        mYZM = JSONObject(response.body()).optString("object")
                        mTel = register_tel.text.toString()
                        if (BuildConfig.LOG_DEBUG) {
                            register_yzm.setText(mYZM)
                            register_yzm.setSelection(register_yzm.text.length)
                        }

                        tv_yzm.isClickable = false
                        timeCount = 180
                        tv_yzm.post(thread)
                    }

                })
        }

        bt_register.oneClick {
            when {
                register_tel.text.isEmpty() -> {
                    register_tel.requestFocus()
                    toast("请输入手机号")
                    return@oneClick
                }
                register_yzm.text.isEmpty() -> {
                    register_yzm.requestFocus()
                    toast("请输入验证码")
                    return@oneClick
                }
                register_pwd.text.isEmpty() -> {
                    register_pwd.requestFocus()
                    toast("请输入6~20位登录密码")
                    return@oneClick
                }
                !register_tel.text.isMobile() -> {
                    register_tel.requestFocus()
                    toast("请输入正确的手机号")
                    return@oneClick
                }
                register_tel.text.toString() != mTel -> {
                    toast("手机号码不匹配，请重新获取验证码")
                    return@oneClick
                }
                register_yzm.text.toString() != mYZM -> {
                    register_yzm.requestFocus()
                    register_yzm.setText("")
                    toast("请输入正确的验证码")
                    return@oneClick
                }
                register_pwd.text.length < 6 -> {
                    toast("密码长度不少于6位")
                    return@oneClick
                }
                !isAgreed -> {
                    toast("请同意高球魔镜《用户注册协议》")
                    return@oneClick
                }
            }

            OkGo.post<String>(BaseHttp.register_sub)
                .tag(this@RegisterFragment)
                .params("mobile", mTel)
                .params("smscode", register_yzm.text.toString())
                .params("password", register_pwd.text.toString())
                .execute(object : StringDialogCallback(activity) {

                    override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                        toast(msg)
                        (activity as OnFragmentListener).onViewClick("登录")
                    }

                })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tv_yzm.removeCallbacks(thread)
    }

}
