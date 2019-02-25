package com.meida.uswing

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.NewsData
import com.meida.share.BaseHttp
import com.meida.utils.dp2px
import com.sunfusheng.GlideImageView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.startActivity
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class NewsActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mRecommend = ""
    private var mType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
        init_title("高球资讯")
    }

    override fun init_title() {
        super.init_title()

        news_tab.apply {
            onTabSelectedListener {
                onTabSelected {
                    when (it!!.position) {
                        0 -> {
                            mRecommend = "1"
                            mType = ""
                        }
                        1 -> {
                            mRecommend = "0"
                            mType = "0"
                        }
                        2 -> {
                            mRecommend = "0"
                            mType = "1"
                        }
                        3 -> {
                            mRecommend = "0"
                            mType = "2"
                        }
                    }

                    OkGo.getInstance().cancelTag(this@NewsActivity)
                    Completable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { updateList() }
                }
            }

            getChildAt(0).apply {
                this as LinearLayout
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                dividerDrawable = ContextCompat.getDrawable(baseContext, R.drawable.layout_divider_vertical)
                dividerPadding = dp2px(15f)
            }

            addTab(this.newTab().setText("推荐"), true)
            addTab(this.newTab().setText("视频"), false)
            addTab(this.newTab().setText("球技"), false)
            addTab(this.newTab().setText("球场"), false)
        }

        empty_hint.text = "暂无相关资讯信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
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
            .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<NewsData>>>(BaseHttp.find_news_list)
            .tag(this@NewsActivity)
            .params("recommend", mRecommend)
            .params("newsType", mType)
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

}
