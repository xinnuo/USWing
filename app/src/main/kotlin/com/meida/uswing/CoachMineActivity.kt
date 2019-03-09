package com.meida.uswing

import android.os.Bundle
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import kotlinx.android.synthetic.main.activity_coach_mine.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.sdk25.listeners.onCheckedChange
import org.jetbrains.anko.startActivity

class CoachMineActivity : BaseActivity() {

    private val listHonor = ArrayList<CommonData>()
    private var mInfo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_mine)
        transparentStatusBar(false)
        init_title()

        EventBus.getDefault().register(this@CoachMineActivity)

        getData()
    }

    override fun init_title() {
        coach_watch.oneClick { startActivity<CoachWatchActivity>() }
        coach_honor.oneClick { startActivity<CoachHonorActivity>("list" to listHonor) }
        coach_info.oneClick { startActivity<CoachInfoActivity>("info" to mInfo) }
        coach_video.oneClick { startActivity<CoachVideoActivity>() }
        coach_group.onCheckedChange { _, checkedId ->
            when (checkedId) {
                R.id.coach_check1 -> OkGo.post<String>(BaseHttp.upadate_specialty)
                    .tag(this@CoachMineActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("specialty", "铁杆")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) { }

                    })
                R.id.coach_check2 -> OkGo.post<String>(BaseHttp.upadate_specialty)
                    .tag(this@CoachMineActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("specialty", "木杆")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) { }

                    })
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonData>>(BaseHttp.coach_details)
            .tag(this@CoachMineActivity)
            .params("certificationId", getString("token"))
            .execute(object :
                JacksonDialogCallback<BaseResponse<CommonData>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<CommonData>>) {

                    if (response.body().`object` != null) {
                        val data = response.body().`object`

                        mInfo = data.introduction
                        listHonor.addItems(data.honors)
                        coach_name.text = data.nick_name
                        coach_watch.setRightString(data.follow_ctn)
                        coach_img.loadRectImage(BaseHttp.baseImg + data.user_head)

                        when (data.specialty) {
                            "铁杆" -> coach_check1.isChecked = true
                            "木杆" -> coach_check2.isChecked = true
                        }
                    }
                }

            })
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@CoachMineActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "修改简介" -> mInfo = event.id
            "删除荣誉" -> listHonor.removeAt(event.id.toInt())
            "添加荣誉" -> listHonor.add(CommonData().apply {
                honorId = event.id
                honorInfo = event.name
            })
        }
    }

}
