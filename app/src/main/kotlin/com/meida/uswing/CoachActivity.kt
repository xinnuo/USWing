package com.meida.uswing

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.meida.utils.hideSoftInput
import com.meida.utils.trimString
import com.meida.view.DropPopWindow
import com.sunfusheng.GlideImageView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_coach.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title_filter.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.*
import java.util.concurrent.TimeUnit

class CoachActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private val items by lazy {
        listOf(
            CommonData("教龄由高到底"),
            CommonData("年龄由高到底"),
            CommonData("男"),
            CommonData("女"),
            CommonData("木杆"),
            CommonData("铁杆")
        )
    }
    private lateinit var dropFilter: DropPopWindow
    private var mTeachAge = ""
    private var mAge = ""
    private var mGender = ""
    private var mSpecial = ""
    private var mKey = ""

    private var isNear = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach)
        setToolbarVisibility(false)
        init_title()

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        isNear = intent.getBooleanExtra("isNear", false)

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
                        startActivity<CoachDetailActivity>(
                            "certificationId" to data.certification_id
                        )
                    }
            }
            .attachTo(recycle_list)

        search_edit.addTextChangedListener(this@CoachActivity)
        search_edit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideSoftInput() //隐藏软键盘

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

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.search_location -> showDropFilter()
        }
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(
            if (isNear) BaseHttp.certification_near_list else BaseHttp.certification_list
        )
            .tag(this@CoachActivity)
            .isMultipart(true)
            .params("teachAge", mTeachAge)
            .params("age", mAge)
            .params("gender", mGender)
            .params("specialty", mSpecial)
            .params("keyword", mKey)
            .params("page", pindex)
            .apply {
                if (isNear) {
                    params("lat", intent.getStringExtra("lat") ?: "")
                    params("lng", intent.getStringExtra("lng") ?: "")
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

    private fun showDropFilter() {
        dropFilter = object : DropPopWindow(baseContext, R.layout.pop_layout_filter) {

            override fun afterInitView(view: View) {
                val recyclerView = view.findViewById<RecyclerView>(R.id.pop_container)
                recyclerView.apply {
                    load_Linear(baseContext)
                    adapter = SlimAdapter.create()
                        .register<CommonData>(R.layout.item_report_list) { data, injector ->

                            val index = items.indexOf(data)
                            val isLast = index == items.size - 1

                            @Suppress("DEPRECATION")
                            injector.text(R.id.item_report_title, data.letter)
                                .textColor(
                                    R.id.item_report_title,
                                    resources.getColor(if (data.isChecked) R.color.black else R.color.light)
                                )
                                .visibility(
                                    R.id.item_report_arrow,
                                    if (data.isChecked) View.VISIBLE else View.GONE
                                )
                                .visibility(
                                    R.id.item_report_divider1,
                                    if (isLast) View.GONE else View.VISIBLE
                                )
                                .visibility(
                                    R.id.item_report_divider2,
                                    if (!isLast) View.GONE else View.VISIBLE
                                )
                                .clicked(R.id.item_report) {
                                    items.filter { it.isChecked }.forEach { it.isChecked = false }
                                    data.isChecked = true
                                    (adapter as SlimAdapter).notifyDataSetChanged()

                                    when (index) {
                                        0 -> {
                                            mTeachAge = "1"
                                            mAge = ""
                                            mGender = ""
                                            mSpecial = ""
                                        }
                                        1 -> {
                                            mTeachAge = ""
                                            mAge = "1"
                                            mGender = ""
                                            mSpecial = ""
                                        }
                                        2 -> {
                                            mTeachAge = ""
                                            mAge = ""
                                            mGender = "1"
                                            mSpecial = ""
                                        }
                                        3 -> {
                                            mTeachAge = ""
                                            mAge = ""
                                            mGender = "0"
                                            mSpecial = ""
                                        }
                                        4 -> {
                                            mTeachAge = ""
                                            mAge = ""
                                            mGender = ""
                                            mSpecial = "木杆"
                                        }
                                        5 -> {
                                            mTeachAge = ""
                                            mAge = ""
                                            mGender = ""
                                            mSpecial = "铁杆"
                                        }
                                    }

                                    dropFilter.dismiss()

                                    Completable.timer(350, TimeUnit.MILLISECONDS)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe { updateList() }
                                }
                        }
                        .attachTo(this)

                    (adapter as SlimAdapter).updateData(items)
                }

            }
        }
        dropFilter.showAsDropDown(coach_divider)
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
