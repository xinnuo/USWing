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
import com.meida.share.BaseHttp
import com.meida.utils.MultiGapDecoration
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.include
import java.util.ArrayList

class CoachWatchActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("我的粉丝")

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关粉丝信息！"
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
            .register<CommonData>(R.layout.item_coach_grid2) { data, injector ->
                injector.text(R.id.item_coach_name, data.nick_name)
                    .text(R.id.item_coach_adress, "地区：${data.ucity}")
                    .image(
                        R.id.item_coach_gender,
                        if (data.gender == "0") R.mipmap.video_icon08 else R.mipmap.video_icon07
                    )

                    .with<GlideImageView>(R.id.item_coach_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }
            }
            .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_follow_list)
            .tag(this@CoachWatchActivity)
            .headers("token", getString("token"))
            .params("type", 0)
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

}
