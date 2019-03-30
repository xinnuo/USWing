package com.meida.uswing

import android.os.Bundle
import android.text.InputFilter
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.base.oneClick
import com.meida.base.putString
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.NameLengthFilter
import com.meida.utils.trimString
import kotlinx.android.synthetic.main.activity_info_name.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.sdk25.listeners.textChangedListener
import org.jetbrains.anko.toast
import java.util.regex.Pattern

class InfoNameActivity : BaseActivity() {

    private var mOldName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_name)
        init_title(intent.getStringExtra("title"), "保存")
    }

    override fun init_title() {
        super.init_title()
        mOldName = intent.getStringExtra("name") ?: ""

        name_name.setText(if (mOldName.isEmpty()) getString("nickName") else mOldName)
        name_name.setSelection(name_name.text.length)

        name_name.filters = arrayOf<InputFilter>(NameLengthFilter(16))
        name_name.textChangedListener {
            afterTextChanged { s ->
                pageNum = 0
                (0 until s!!.length).forEach {
                    val matcher = Pattern.compile("[\u4e00-\u9fa5]").matcher(s[it].toString())
                    if (matcher.matches()) pageNum += 2
                    else pageNum++
                }
            }
        }

        name_close.oneClick { name_name.setText("") }

        tvRight.oneClick {
            when {
                name_name.text.isBlank() -> toast("请输入昵称")
                name_name.text.trimString() == getString("nickName") -> toast("未做任何修改")
                pageNum < 4 -> toast("昵称长度不少于4个字符（一个汉字两个字符）")
                else -> {
                    if (mOldName.isEmpty()) {
                        OkGo.post<String>(BaseHttp.update_userInfo)
                            .tag(this@InfoNameActivity)
                            .isMultipart(true)
                            .headers("token", getString("token"))
                            .params("nickName", name_name.text.trimString())
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {

                                    toast(msg)
                                    putString("nickName", name_name.text.trimString())
                                    ActivityStack.screenManager.popActivities(this@InfoNameActivity::class.java)
                                }

                            })
                    } else {
                        val friendId = intent.getStringExtra("friendId")

                        OkGo.post<String>(BaseHttp.update_friend_nickanme)
                            .tag(this@InfoNameActivity)
                            .isMultipart(true)
                            .headers("token", getString("token"))
                            .params("friendId", friendId)
                            .params("nickName", name_name.text.trimString())
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {

                                    toast(msg)
                                    EventBus.getDefault().post(
                                        RefreshMessageEvent(
                                            "修改昵称",
                                            friendId,
                                            name_name.text.trimString()
                                        )
                                    )
                                    ActivityStack.screenManager.popActivities(this@InfoNameActivity::class.java)
                                }

                            })
                    }
                }
            }
        }
    }
}
