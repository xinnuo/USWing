package com.meida.uswing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.amap.api.AMapLocationHelper
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.makeramen.roundedimageview.RoundedImageView
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.LocationMessageEvent
import com.meida.share.BaseHttp
import com.meida.view.FullyLinearLayoutManager
import kotlinx.android.synthetic.main.activity_coach_top.*
import kotlinx.android.synthetic.main.layout_title_top.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class CoachTopActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private val listTop = ArrayList<Any>()
    private var mLat = ""
    private var mLng = ""
    private var mCity = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_top)
        setToolbarVisibility(false)
        init_title()

        EventBus.getDefault().register(this@CoachTopActivity)

        showLoadingDialog()
        getLocationData()
        getCoachData()
    }

    override fun init_title() {
        nav_title.text = "教练列表"

        top_list1.apply {
            load_Linear(baseContext)
            (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
            adapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_coach_near1) { data, injector ->
                    injector.with<RoundedImageView>(R.id.item_coach) {
                        it.setImageURL(BaseHttp.baseImg + data.user_head)
                    }

                        .clicked(R.id.item_coach) {
                            startActivity<CoachDetailActivity>(
                                "certificationId" to data.certification_id
                            )
                        }
                }
                .attachTo(this)
        }

        top_list2.apply {
            layoutManager = FullyLinearLayoutManager(baseContext)
            adapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_coach_near2) { data, injector ->

                    val index = listTop.indexOf(data)
                    val isLast = index == listTop.size - 1

                    injector.text(R.id.item_coach_name, data.nick_name)
                        .with<RoundedImageView>(R.id.item_coach_img) {
                            it.setImageURL(BaseHttp.baseImg + data.user_head)
                        }

                        .visibility(
                            R.id.item_coach_divider1,
                            if (isLast) View.GONE else View.VISIBLE
                        )
                        .visibility(
                            R.id.item_coach_divider2,
                            if (!isLast) View.GONE else View.VISIBLE
                        )

                        .clicked(R.id.item_coach) {
                            startActivity<CoachDetailActivity>(
                                "certificationId" to data.certification_id
                            )
                        }
                }
                .attachTo(this)
        }

        nav_location.oneClick { startActivity<NearCityActivity>("type" to "教练位置") }
        top_more.oneClick {
            startActivity<CoachActivity>(
                "lat" to mLat,
                "lng" to mLng,
                "isNear" to true
            )
        }
        top_top.oneClick { startActivity<CoachActivity>() }
    }

    private fun getLocationData() {
        AMapLocationHelper.getInstance(baseContext)
            .startLocation(500) { location, isSuccessed, codes ->
                if (500 in codes) {
                    if (isSuccessed) {
                        mLat = location.latitude.toString()
                        mLng = location.longitude.toString()
                        mCity = location.city
                        nav_location.text = mCity.replace("市", "")

                        getData()
                    } else {
                        val errorInfo = location?.locationDetail ?: "位置信息获取失败"
                        toast(errorInfo.split("#")[0])

                        getData()
                    }
                }
            }
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.certification_near_list)
            .tag(this@CoachTopActivity)
            .params("lat", mLat)
            .params("lng", mLng)
            .params("page", 1)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        clear()
                        addItems(response.body().`object`)
                    }

                    (top_list1.adapter as SlimAdapter).updateData(list)
                }

                override fun onFinish() {
                    super.onFinish()
                    cancelLoadingDialog()
                }

            })
    }

    private fun getCoachData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.certification_list)
            .tag(this@CoachTopActivity)
            .params("page", 1)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    listTop.apply {
                        clear()
                        addItems(response.body().`object`)
                    }

                    (top_list2.adapter as SlimAdapter).updateData(listTop)
                }

            })
    }

    override fun finish() {
        AMapLocationHelper.getInstance(baseContext).removeCode(500)
        EventBus.getDefault().unregister(this@CoachTopActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: LocationMessageEvent) {
        when (event.type) {
            "教练位置" -> {
                mLat = event.lat
                mLng = event.lng
                mCity = event.city
                nav_location.text = mCity.replace("市", "")

                showLoadingDialog()
                getData()
            }
        }
    }

}
