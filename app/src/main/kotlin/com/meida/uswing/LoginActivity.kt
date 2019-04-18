package com.meida.uswing

import android.content.Intent
import android.os.Bundle
import cn.jpush.android.api.JPushInterface
import com.meida.RongCloudContext
import com.meida.base.*
import com.meida.fragment.LoginFragment
import com.meida.fragment.OnFragmentListener
import com.meida.fragment.RegisterFragment
import com.meida.utils.ActivityStack
import com.umeng.socialize.UMShareAPI
import io.rong.imkit.RongIM
import io.rong.push.RongPushClient
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class LoginActivity : BaseActivity(), OnFragmentListener {

    private lateinit var mLogin: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTransparentAndToobar(true, false)
        init_title()
    }

    override fun init_title() {
        if (intent.getBooleanExtra("offLine", false)) {
            val isToast = intent.getBooleanExtra("isToast", false)
            if (isToast) toast("当前账户在其他设备登录")

            clearData()
            ActivityStack.screenManager.popAllActivityExcept(this@LoginActivity::class.java)
        }

        mLogin = LoginFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.login_container, mLogin)
            .commit()
    }

    override fun onViewClick(name: String) {
        when (name) {
            "登录" -> onBackPressed()
            "登录成功" -> {
                putBoolean("isLogin", true)
                startActivity<MainActivity>()
                ActivityStack.screenManager.popActivities(this@LoginActivity::class.java)
            }
            "注册" -> {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.push_left_in,
                        R.anim.push_left_out,
                        R.anim.push_right_in,
                        R.anim.push_right_out
                    )
                    .add(R.id.login_container, RegisterFragment())
                    .hide(mLogin)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun clearData() {
        clearBoolean("isLogin", "isTS")

        clearString(
            "token",
            "rongToken",
            "loginType",
            "nickName",
            "userHead",
            "gender",
            "auth",
            "coach",
            "sign",
            "signSum",
            "province",
            "city"
        )

        //停止及清除极光推送
        JPushInterface.stopPush(applicationContext)
        JPushInterface.clearAllNotifications(applicationContext)

        //清除及退出融云
        RongCloudContext.getInstance().clearNotificationMessage()
        RongPushClient.clearAllPushNotifications(applicationContext)
        RongIM.getInstance().logout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this@LoginActivity).onActivityResult(requestCode, resultCode, data)
    }

}
