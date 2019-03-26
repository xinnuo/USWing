package com.meida.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.meida.base.*
import com.meida.share.BaseHttp
import com.meida.uswing.BindActivity
import com.meida.uswing.ForgetActivity
import com.meida.uswing.R
import com.meida.utils.getPlatformInfo
import com.meida.utils.isMobile
import com.meida.utils.setShareConfig
import com.umeng.socialize.UMShareConfig
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject

class LoginFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()
    }

    override fun init_title() {
        if (getString("mobile").isNotBlank()) {
            login_tel.setText(getString("mobile"))
            login_tel.setSelection(login_tel.text.length)
        }

        login_register.oneClick { (activity as OnFragmentListener).onViewClick("注册") }
        login_forget.oneClick { startActivity<ForgetActivity>() }
        bt_login.oneClick {
            when {
                login_tel.text.isEmpty() -> {
                    login_tel.requestFocus()
                    toast("请输入手机号")
                    return@oneClick
                }
                login_pwd.text.isEmpty() -> {
                    login_pwd.requestFocus()
                    toast("请输入6~20位登录密码")
                    return@oneClick
                }
                !login_tel.text.isMobile() -> {
                    login_pwd.requestFocus()
                    toast("请输入正确的手机号")
                    return@oneClick
                }
                login_pwd.text.length < 6 -> {
                    toast("密码长度不少于6位")
                    return@oneClick
                }
            }

            OkGo.post<String>(BaseHttp.login_sub)
                .tag(this@LoginFragment)
                .params("accountName", login_tel.text.toString())
                .params("password", login_pwd.text.toString())
                .params("loginType", "mobile")
                .execute(object : StringDialogCallback(activity) {

                    override fun onSuccessResponse(
                        response: Response<String>,
                        msg: String,
                        msgCode: String
                    ) {

                        val obj = JSONObject(response.body())
                            .optJSONObject("object") ?: JSONObject()

                        putString("token", obj.optString("token"))
                        putString("rongToken", obj.optString("rongtoken"))
                        putString("mobile", obj.optString("mobile"))
                        putString("nickName", obj.optString("nick_name"))
                        putString("userHead", obj.optString("user_head"))
                        putString("loginType", "mobile")

                        (activity as OnFragmentListener).onViewClick("登录成功")
                    }

                })
        }

        login_wechat.oneClick {
            activity!!.apply {
                setShareConfig(UMShareConfig().apply { isNeedAuthOnGetUserInfo = true })
                getPlatformInfo(SHARE_MEDIA.WEIXIN) {
                    onComplete { _, _, data ->
                        getThirdLogin(
                            "WX",
                            data["uid"] ?: "",
                            data["name"] ?: "",
                            data["iconurl"] ?: ""
                        )
                    }
                    onError { _, _, throwable ->
                        OkLogger.e(throwable.message)
                        toast("授权失败")
                    }
                }
            }
        }
        login_qq.oneClick {
            activity!!.apply {
                setShareConfig(UMShareConfig().apply { isNeedAuthOnGetUserInfo = true })
                getPlatformInfo(SHARE_MEDIA.QQ) {
                    onComplete { _, _, data ->
                        getThirdLogin(
                            "QQ",
                            data["uid"] ?: "",
                            data["name"] ?: "",
                            data["iconurl"] ?: ""
                        )
                    }
                    onError { _, _, throwable ->
                        OkLogger.e(throwable.message)
                        toast("授权失败")
                    }
                }
            }
        }
    }

    private fun getThirdLogin(
        loginType: String,
        openId: String,
        nickName: String,
        headImgUrl: String
    ) {
        OkGo.post<String>(BaseHttp.login_sub)
            .tag(this@LoginFragment)
            .params("loginType", loginType)
            .params("openId", openId)
            .execute(object : StringDialogCallback(activity) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    putString("token", obj.optString("token"))
                    putString("rongToken", obj.optString("rongtoken"))
                    putString("mobile", obj.optString("mobile"))
                    putString("nickName", obj.optString("nick_name"))
                    putString("userHead", obj.optString("user_head"))
                    putString("loginType", loginType)

                    (activity as OnFragmentListener).onViewClick("登录成功")
                }

                override fun onSuccessResponseErrorCode(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {
                    if (msgCode == "105") {
                        startActivityEx<BindActivity>(
                            "loginType" to loginType,
                            "openId" to openId,
                            "nickName" to nickName,
                            "headImgUrl" to headImgUrl
                        )
                    } else toast(msg)
                }

            })
    }

}
