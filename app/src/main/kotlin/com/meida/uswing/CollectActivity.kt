package com.meida.uswing

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.NewsData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.MultiGapDecoration
import com.meida.utils.dp2px
import com.sunfusheng.GlideImageView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_collect.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class CollectActivity : BaseActivity() {

    private lateinit var mAdapterCoach: SlimAdapter
    private lateinit var mAdapterNews: SlimAdapter
    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect)
        init_title("我的收藏")

        EventBus.getDefault().register(this@CollectActivity)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关收藏信息！"
        swipe_refresh.refresh { getData(1) }

        collect_tab.apply {
            onTabSelectedListener {
                onTabSelected {
                    if (list.isNotEmpty()) {
                        list.clear()
                        when (mPosition) {
                            2 -> mAdapter.notifyDataSetChanged()
                            3 -> mAdapterCoach.notifyDataSetChanged()
                            1 -> mAdapterNews.notifyDataSetChanged()
                        }
                    }

                    mPosition = when (it!!.position) {
                        1 -> 3
                        2 -> 1
                        else -> 2
                    }

                    recycle_list.clearOnScrollListeners()
                    if (mPosition == 3) {
                        recycle_list.load_Grid(swipe_refresh, {
                            if (!isLoadingMore) {
                                isLoadingMore = true
                                getData(pageNum)
                            }
                        }, {
                            layoutManager = GridLayoutManager(baseContext, 3)
                            addItemDecoration(MultiGapDecoration().apply {
                                isOffsetTopEnabled = true
                            })
                        })
                    } else {
                        recycle_list.load_Linear(baseContext, swipe_refresh) {
                            if (!isLoadingMore) {
                                isLoadingMore = true
                                getData(pageNum)
                            }
                        }
                    }

                    OkGo.getInstance().cancelTag(this@CollectActivity)
                    Completable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { updateSelectList() }
                }
            }

            getChildAt(0).apply {
                this as LinearLayout
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                dividerDrawable =
                    ContextCompat.getDrawable(baseContext, R.drawable.layout_divider_vertical)
                dividerPadding = dp2px(15f)
            }

            addTab(this.newTab().setText("魔频"), true)
            addTab(this.newTab().setText("教练"), false)
            addTab(this.newTab().setText("资讯"), false)
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_collect_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_collect_name, data.theme_title)
                    .text(R.id.item_collect_time, "时间：${data.create_date}")

                    .with<TextView>(R.id.item_collect_desc) {
                        it.text = data.labels_name
                        val textWidth = it.paint.measureText(data.labels_name)
                        data.labels_width = textWidth
                    }

                    .with<ImageView>(R.id.item_collect_more) {
                        it.visibility =
                            if (data.labels_width > dp2px(110f)) View.VISIBLE
                            else View.INVISIBLE
                    }

                    .with<GlideImageView>(R.id.item_collect_img) {
                        it.load(BaseHttp.circleImg + data.positive_img, R.mipmap.default_video)
                    }

                    .visibility(
                        R.id.item_collect_divider1,
                        if (index == 0) View.VISIBLE else View.GONE
                    )
                    .visibility(R.id.item_collect_divider2, if (isLast) View.VISIBLE else View.GONE)

                    .clicked(R.id.item_collect_label) {
                        if (data.labels_name.isNotEmpty()) {
                            startActivity<VideoLabelActivity>("label" to data.labels_name)
                        }
                    }

                    .clicked(R.id.item_collect) {
                        startActivity<VideoDetailActivity>(
                            "title" to "教练魔频",
                            "magicvoideId" to data.magicvoide_id,
                            "video1" to BaseHttp.circleImg + data.positive_voide,
                            "video2" to BaseHttp.circleImg + data.negative_voide,
                            "videoImg1" to BaseHttp.circleImg + data.positive_img,
                            "videoImg2" to BaseHttp.circleImg + data.negative_img,
                            "share" to false
                        )
                    }
            }

        mAdapterCoach = SlimAdapter.create()
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

        mAdapterNews = SlimAdapter.create()
            .register<NewsData>(R.layout.item_first_news) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.visible(R.id.item_first_divider1)
                    .text(R.id.item_first_title, data.news_title)
                    .text(R.id.item_first_content, data.news_introduction)
                    .text(R.id.item_first_time, data.create_date)
                    .visibility(R.id.item_first_divider2, if (isLast) View.VISIBLE else View.GONE)

                    .with<GlideImageView>(R.id.item_first_img) {
                        it.load(BaseHttp.baseImg + data.news_img, R.mipmap.default_img)
                    }

                    .clicked(R.id.item_news) { startActivity<NewsDetailActivity>("newsId" to data.news_id) }
            }
    }

    override fun getData(pindex: Int) {
        when (mPosition) {
            2, 3 -> OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_collection_list)
                .tag(this@CollectActivity)
                .headers("token", getString("token"))
                .params("type", mPosition)
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

                        when (mPosition) {
                            2 -> {
                                if (!isLoadingMore) mAdapter.attachTo(recycle_list)
                                mAdapter.updateData(list)
                            }
                            3 -> {
                                if (!isLoadingMore) mAdapterCoach.attachTo(recycle_list)
                                mAdapterCoach.updateData(list)
                            }
                        }
                    }

                    override fun onFinish() {
                        super.onFinish()
                        swipe_refresh.isRefreshing = false
                        isLoadingMore = false

                        empty_view.apply { if (list.isEmpty()) visible() else gone() }
                    }

                })

            1 -> OkGo.post<BaseResponse<ArrayList<NewsData>>>(BaseHttp.find_collection_list)
                .tag(this@CollectActivity)
                .headers("token", getString("token"))
                .params("type", mPosition)
                .params("page", pindex)
                .execute(object :
                    JacksonDialogCallback<BaseResponse<ArrayList<NewsData>>>(baseContext) {

                    override fun onSuccess(response: Response<BaseResponse<ArrayList<NewsData>>>) {

                        list.apply {
                            if (pindex == 1) {
                                clear()
                                pageNum = pindex
                            }
                            addItems(response.body().`object`)
                            if (count(response.body().`object`) > 0) pageNum++
                        }

                        if (!isLoadingMore) mAdapterNews.attachTo(recycle_list)
                        mAdapterNews.updateData(list)
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

    private fun updateSelectList() {
        swipe_refresh.isRefreshing = true
        empty_view.gone()

        pageNum = 1
        getData(pageNum)
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
        EventBus.getDefault().unregister(this@CollectActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "添加收藏", "取消收藏" -> updateList()
        }
    }

}
