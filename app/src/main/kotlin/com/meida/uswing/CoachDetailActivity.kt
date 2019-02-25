package com.meida.uswing

import android.os.Bundle
import android.text.Html
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.toTextInt
import kotlinx.android.synthetic.main.activity_coach_detail.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class CoachDetailActivity : BaseActivity() {

    private var certificationId = ""
    private var hasFollow = ""
    private var followSum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_detail)
        init_title("教练详情", "加好友")

        getData()
    }

    override fun init_title() {
        super.init_title()
        certificationId = intent.getStringExtra("certificationId")
        tvRight.oneClick {
            startActivity<CoachAddActivity>(
                "toUserId" to certificationId
            )
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.coach_video -> {
                startActivity<CoachVideoActivity>(
                    "type" to "详情魔频",
                    "userInfoId" to certificationId
                )
            }
            R.id.coach_state -> startActivity<CoachStateActivity>("userInfoId" to certificationId)
            R.id.coach_add -> {
                if (certificationId == getString("token")) {
                    toast("不能关注自己")
                    return
                }

                when (hasFollow) {
                    "0" -> OkGo.post<String>(BaseHttp.add_coach_follow)
                        .tag(this@CoachDetailActivity)
                        .headers("token", getString("token"))
                        .params("certificationId", certificationId)
                        .execute(object :
                            StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(
                                response: Response<String>,
                                msg: String,
                                msgCode: String
                            ) {
                                toast(msg)
                                hasFollow = "1"
                                followSum += 1
                                coach_watch.text = followSum.toString()
                                coach_add.setImageResource(R.mipmap.video_icon20)
                                EventBus.getDefault().post(RefreshMessageEvent("添加关注"))
                            }

                        })
                    "1" -> OkGo.post<String>(BaseHttp.delete_follow)
                        .tag(this@CoachDetailActivity)
                        .headers("token", getString("token"))
                        .params("certificationId", certificationId)
                        .execute(object :
                            StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(
                                response: Response<String>,
                                msg: String,
                                msgCode: String
                            ) {
                                toast(msg)
                                hasFollow = "0"
                                followSum -= 1
                                coach_watch.text = followSum.toString()
                                coach_add.setImageResource(R.mipmap.video_icon14)
                                EventBus.getDefault().post(RefreshMessageEvent("取消关注"))
                            }

                        })
                }
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonData>>(BaseHttp.coach_details)
            .tag(this@CoachDetailActivity)
            .params("certificationId", certificationId)
            .params("userInfoId", getString("token"))
            .execute(object :
                JacksonDialogCallback<BaseResponse<CommonData>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<CommonData>>) {

                    if (response.body().`object` != null) {
                        val data = response.body().`object`

                        hasFollow = data.follows
                        followSum = data.follow_ctn.toTextInt()

                        val teachAge = data.teach_age.toTextInt()
                        @Suppress("DEPRECATION")
                        coach_year.text = Html.fromHtml(
                            String.format(
                                "%1\$s<small><small>年</small></small>",
                                teachAge
                            )
                        )

                        coach_area.text = data.ucity
                        coach_watch.text = data.follow_ctn
                        coach_name.text = data.nick_name
                        coach_name2.text = data.nick_name
                        coach_tel.text = data.telephone

                        coach_add.visibility =
                            if (certificationId == getString("token")) View.GONE
                            else View.VISIBLE

                        tvRight.visibility =
                            if (data.friend == "1" || certificationId == getString("token")) View.GONE
                            else View.VISIBLE

                        coach_img.setImageURL(BaseHttp.baseImg + data.user_head)
                        coach_gender.setImageResource(
                            if (data.gender == "0") R.mipmap.video_icon08
                            else R.mipmap.video_icon07
                        )
                        coach_add.setImageResource(
                            if (data.follows == "1") R.mipmap.video_icon20
                            else R.mipmap.video_icon14
                        )

                        coach_info.text = data.introduction
                        val items = ArrayList<String>()
                        val honors = ArrayList<CommonData>()
                        honors.addItems(data.honors)
                        honors.mapTo(items) { it.honorInfo }
                        coach_honor.text = items.joinToString("\n")
                    }
                }

            })
    }

}
