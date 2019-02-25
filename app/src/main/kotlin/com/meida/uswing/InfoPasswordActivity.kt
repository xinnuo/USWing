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
import kotlinx.android.synthetic.main.activity_info_password.*
import org.jetbrains.anko.toast

class InfoPasswordActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_password)
        init_title("修改密码")
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_submit -> {
                when {
                    pwd_now.text.isBlank() -> {
                        pwd_now.requestFocus()
                        toast("请输入当前密码")
                        return
                    }
                    pwd_new.text.isBlank() -> {
                        pwd_new.requestFocus()
                        toast("请输入新密码")
                        return
                    }
                    pwd_confirm.text.isBlank() -> {
                        pwd_confirm.requestFocus()
                        toast("请再次输入新密码")
                        return
                    }
                    pwd_now.text.length < 6
                            || pwd_new.text.length < 6
                            || pwd_confirm.text.length < 6 -> {
                        toast("密码长度不少于6位")
                        return
                    }
                    pwd_new.text.toString() != pwd_confirm.text.toString() -> {
                        toast("密码输入不一致，请重新输入")
                        return
                    }
                }

                OkGo.post<String>(BaseHttp.password_change_sub)
                    .tag(this@InfoPasswordActivity)
                    .headers("token", getString("token"))
                    .params("oldPwd", pwd_now.text.toString())
                    .params("newPwd", pwd_new.text.toString())
                    .params("confirmPwd", pwd_confirm.text.toString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            ActivityStack.screenManager.popActivities(this@InfoPasswordActivity::class.java)
                        }

                    })
            }
        }
    }
}
