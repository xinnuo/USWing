package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.DialogHelper.showHintDialog
import com.meida.utils.trimString
import com.ruanmeng.utils.KeyboardHelper
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title_search.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.ArrayList

class VideoActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()
    private var mKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        setToolbarVisibility(false)
        init_title()

        EventBus.getDefault().register(this@VideoActivity)

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        empty_hint.text = "暂无相关魔频信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_video_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_video_desc, data.labels_name)
                    .text(R.id.item_video_name, data.theme_title)
                    .text(R.id.item_video_adress, "地址：${data.address}")
                    .text(R.id.item_video_time, "时间：${data.create_date}")
                    .text(R.id.item_video_memo, "备注：${data.mome}")

                    .with<GlideImageView>(R.id.item_video_img) {
                        it.load(BaseHttp.circleImg + data.positive_img, R.mipmap.default_video)
                    }

                    .visibility(
                        R.id.item_video_divider1,
                        if (index == 0) View.VISIBLE else View.GONE
                    )
                    .visibility(R.id.item_video_divider2, if (isLast) View.VISIBLE else View.GONE)

                    .clicked(R.id.item_video_del) {
                        showHintDialog("提示", "您确定要删除视频吗？") {
                            if (it == "确定") {
                                OkGo.post<String>(BaseHttp.delete_magicvoide)
                                    .tag(this@VideoActivity)
                                    .headers("token", getString("token"))
                                    .params("magicvoideId", data.magicvoide_id)
                                    .execute(object : StringDialogCallback(baseContext) {

                                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                            toast(msg)
                                            list.remove(data)
                                            mAdapter.notifyItemRemoved(index)
                                            empty_view.apply { if (list.isEmpty()) visible() else gone() }
                                        }

                                    })
                            }
                        }
                    }

                    .clicked(R.id.item_video_edit) {

                        startActivity<VideoEditActivity>(
                            "magicvoideId" to data.magicvoide_id,
                            "title" to data.theme_title,
                            "lableIds" to (data.labels_id ?: ""),
                            "lableNames" to data.labels_name,
                            "memo" to data.mome
                        )
                    }

                    .clicked(R.id.item_video) {
                        startActivity<CompareActivity>(
                            "title" to "我的魔频",
                            "magicvoideId" to data.magicvoide_id,
                            "video1" to BaseHttp.circleImg + data.positive_voide,
                            "video2" to BaseHttp.circleImg + data.negative_voide,
                            "videoImg1" to BaseHttp.circleImg + data.positive_img,
                            "videoImg2" to BaseHttp.circleImg + data.negative_img,
                            "share" to true
                        )
                    }
            }
            .attachTo(recycle_list)

        search_edit.addTextChangedListener(this@VideoActivity)
        search_edit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                KeyboardHelper.hideSoftInput(baseContext) //隐藏软键盘

                if (search_edit.text.isBlank()) {
                    toast("请输入关键字")
                } else {
                    mKey = search_edit.text.trimString()
                    updateList()
                }
            }
            return@setOnEditorActionListener false
        }

        search_close.setOnClickListener { search_edit.setText("") }
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_magicvoide_list)
            .tag(this@VideoActivity)
            .isMultipart(true)
            .headers("token", getString("token"))
            .params("keyword", mKey)
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

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        search_close.visibility = if (s.isEmpty()) View.GONE else View.VISIBLE
        if (s.isEmpty() && mKey.isNotEmpty()) {
            mKey = ""
            updateList()
        }
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@VideoActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "编辑魔频" -> {
                val index = list.indexOfFirst { it.magicvoide_id == event.id }

                list[index].labels_name = event.name
                list[index].labels_id = event.checkId
                list[index].theme_title = event.title
                list[index].mome = event.memo

                mAdapter.notifyItemChanged(index)
            }
        }
    }

}
