package com.meida.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.share.BaseHttp
import com.meida.uswing.*
import com.meida.utils.phoneReplaceWithStar
import io.rong.imkit.RongIM
import io.rong.imlib.model.UserInfo
import kotlinx.android.synthetic.main.fragment_main_fourth.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.json.JSONObject

class MainFourthFragment : BaseFragment() {

    //调用这个方法切换时不会释放掉Fragment
    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        this.view?.visibility = if (menuVisible) View.VISIBLE else View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_fourth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()
    }

    override fun onStart() {
        super.onStart()

        getInfoData()
    }

    override fun init_title() {
        fourth_setting.oneClick { startActivity<SettingActivity>() }
        fourth_img.oneClick { startActivity<InfoActivity>() }
        fourth_sign.oneClick { startActivity<IntegralActivity>() }
        fourth_wallet.oneClick { startActivity<WalletActivity>() }
        fourth_collect.oneClick { startActivity<CollectActivity>() }
        fourth_watch.oneClick { startActivity<WatchActivity>() }
        fourth_state.oneClick { startActivity<StateActivity>() }
        fourth_code.oneClick { startActivity<CodeActivity>() }
        fourth_coach.oneClick {
            when (getString("auth")) {
                "1" -> startActivity<CoachMineActivity>()
                "0" -> {
                    toast("提交信息正在审核中，请耐心等待！")
                    return@oneClick
                }
                else -> startActivity<CoachAuthorActivity>()
            }
        }
    }

    /* 个人资料 */
    private fun getInfoData() {
        OkGo.post<String>(BaseHttp.user_msg_data)
            .tag(this@MainFourthFragment)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(activity, false) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    putString("nickName", obj.optString("nick_name"))
                    putString("userHead", obj.optString("user_head"))
                    putString("mobile", obj.optString("telephone"))
                    putString("gender", obj.optString("gender"))
                    putString("auth", obj.optString("auth"))
                    putString("coach", obj.optString("coach"))
                    putString("sign", obj.optString("signin"))
                    putString("signSum", obj.optString("signinSum"))
                    putString("province", obj.optString("uprovince"))
                    putString("city", obj.optString("ucity"))

                    fourth_name.text = getString("nickName")
                    fourth_phone.text = getString("mobile").phoneReplaceWithStar()
                    fourth_sign.text = when (getString("sign")) {
                        "1" -> "已签到"
                        else -> "签到+${getString("signSum")}"
                    }
                    fourth_coach.setRightString(
                        when (getString("auth")) {
                            "1" -> "已认证"
                            "0" -> "认证中"
                            else -> "未认证"
                        }
                    )

                    if (fourth_img.getTag(R.id.image_tag) == null) {
                        fourth_img.setImageURL(BaseHttp.baseImg + getString("userHead"))
                        fourth_img.setTag(R.id.image_tag, getString("userHead"))
                    } else {
                        if (fourth_img.getTag(R.id.image_tag) != getString("userHead")) {
                            fourth_img.setImageURL(BaseHttp.baseImg + getString("userHead"))
                            fourth_img.setTag(R.id.image_tag, getString("userHead"))
                        }
                    }

                    RongIM.getInstance().setCurrentUserInfo(
                        UserInfo(
                            getString("token"),
                            getString("nickName"),
                            Uri.parse(BaseHttp.baseImg + getString("userHead"))
                        )
                    )
                }

            })
    }

}
