package com.meida.uswing

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.MultiGapDecoration
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.include
import org.jetbrains.anko.startActivity
import java.util.ArrayList

class WatchActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("我的关注")

        EventBus.getDefault().register(this@WatchActivity)

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关关注信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Grid(swipe_refresh, {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }, {
            layoutManager = GridLayoutManager(baseContext, 3)
            addItemDecoration(MultiGapDecoration().apply { isOffsetTopEnabled = true })
        })

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_coach_grid) { data, injector ->
                injector.text(R.id.item_coach_name, data.nick_name)
                    .text(R.id.item_coach_year, "教龄：${data.teach_age}年")
                    .text(R.id.item_coach_adress, "地区：${data.ucity}")
                    .image(
                        R.id.item_coach_gender,
                        if (data.gender == "0") R.mipmap.video_icon08 else R.mipmap.video_icon07
                    )
                    .visibility(
                        R.id.item_coach_jian,
                        if (data.recommend == "1") View.VISIBLE else View.INVISIBLE
                    )

                    .with<GlideImageView>(R.id.item_coach_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }

                    .clicked(R.id.item_coach) {
                        startActivity<CoachDetailActivity>(
                            "certificationId" to data.certification_id
                        )
                    }
            }
            .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_follow_list)
            .tag(this@WatchActivity)
            .headers("token", getString("token"))
            .params("type", 1)
            .params("page", pindex)
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

    private fun updateList() {
        swipe_refresh.isRefreshing = true

        empty_view.gone()
        if (list.isNotEmpty()) {
            list.clear()
            mAdapter.notifyDataSetChanged()
        }

        pageNum = 1
        getData(pageNum)
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@WatchActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "添加关注", "取消关注" -> updateList()
        }
    }

}
