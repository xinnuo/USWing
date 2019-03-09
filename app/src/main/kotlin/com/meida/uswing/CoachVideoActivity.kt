package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.LocationMessageEvent
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.include
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.ArrayList

class CoachVideoActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private lateinit var mType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("我的魔频")

        when (mType) {
            "详情魔频", "魔频对比" -> tvTitle.text = "教练魔频"
        }

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        mType = intent.getStringExtra("type") ?: ""

        empty_hint.text = "暂无相关魔频信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_collect_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_collect_desc, data.labels_name)
                    .text(R.id.item_collect_name, data.theme_title)
                    .text(R.id.item_collect_time, "时间：${data.create_date}")

                    .with<GlideImageView>(R.id.item_collect_img) {
                        it.load(BaseHttp.circleImg + data.positive_img, R.mipmap.default_video)
                    }

                    .visibility(
                        R.id.item_collect_divider1,
                        if (index == 0) View.VISIBLE else View.GONE
                    )
                    .visibility(R.id.item_collect_divider2, if (isLast) View.VISIBLE else View.GONE)

                    .clicked(R.id.item_collect) {
                        when (mType) {
                            "添加魔频" -> {
                                val itemIds = intent.getStringExtra("videoIds")

                                if (data.magicvoide_id in itemIds) {
                                    toast("您已经选过该魔频")
                                    return@clicked
                                }

                                EventBus.getDefault().post(
                                    LocationMessageEvent(
                                        "添加魔频",
                                        data.magicvoide_id,
                                        data.theme_title
                                    )
                                )

                                ActivityStack.screenManager.popActivities(this@CoachVideoActivity::class.java)
                            }
                            "魔频对比", "我的魔频" -> {
                                val itemId = intent.getStringExtra("selectId")

                                if (data.magicvoide_id == itemId) {
                                    toast("您已经选过该魔频")
                                    return@clicked
                                }

                                EventBus.getDefault().post(
                                    RefreshMessageEvent(
                                        intent.getStringExtra("flag"),
                                        data.magicvoide_id,
                                        BaseHttp.circleImg + data.positive_voide,
                                        BaseHttp.circleImg + data.negative_voide,
                                        BaseHttp.circleImg + data.positive_img,
                                        BaseHttp.circleImg + data.negative_img
                                    )
                                )

                                ActivityStack.screenManager.popActivities(
                                    CompareCoachActivity::class.java,
                                    this@CoachVideoActivity::class.java
                                )
                            }
                            else -> {
                                val userInfoId = intent.getStringExtra("userInfoId") ?: ""

                                startActivity<CompareActivity>(
                                    "title" to "我的魔频",
                                    "magicvoideId" to data.magicvoide_id,
                                    "video1" to BaseHttp.circleImg + data.positive_voide,
                                    "video2" to BaseHttp.circleImg + data.negative_voide,
                                    "videoImg1" to BaseHttp.circleImg + data.positive_img,
                                    "videoImg2" to BaseHttp.circleImg + data.negative_img,
                                    "share" to (userInfoId == getString("token"))
                                )
                            }
                        }
                    }
            }
            .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_magicvoide_list)
            .tag(this@CoachVideoActivity)
            .headers("token", getString("token"))
            .params("page", pindex)
            .apply {
                when (mType) {
                    "详情魔频", "魔频对比" -> params("userInfoId", intent.getStringExtra("userInfoId"))
                }
            }
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        if (pindex == 1) {
                            clear()
                            pageNum = pindex
                        }
                        addItems(response.body().`object`)
                        if (count(response.body().`object`) > 0) pageNum++
                    }

                    mAdapter.updateData(list)
                }

                override fun onFinish() {
                    super.onFinish()
                    swipe_refresh.isRefreshing = false
                    isLoadingMore = false

                    empty_view.apply { if (list.isEmpty()) visible() else gone() }
                }

            })
    }

}
