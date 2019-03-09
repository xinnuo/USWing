package com.meida.uswing

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.inputmethod.EditorInfo
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.share.BaseHttp
import com.meida.utils.MultiGapDecoration
import com.meida.utils.trimString
import com.ruanmeng.utils.KeyboardHelper
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title_search.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*

class CompareCoachActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_coach)
        setToolbarVisibility(false)
        init_title()

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        empty_hint.text = "暂无相关教练信息！"
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
                        startActivity<CoachVideoActivity>(
                            "type" to "魔频对比",
                            "userInfoId" to data.certification_id,
                            "flag" to intent.getStringExtra("flag"),
                            "selectId" to intent.getStringExtra("selectId")
                        )
                    }
            }
            .attachTo(recycle_list)

        search_edit.addTextChangedListener(this@CompareCoachActivity)
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
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.certification_list)
            .tag(this@CompareCoachActivity)
            .isMultipart(true)
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

}
