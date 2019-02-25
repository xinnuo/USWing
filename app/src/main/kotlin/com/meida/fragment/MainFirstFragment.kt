package com.meida.fragment

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.allen.library.SuperTextView
import com.amap.api.AMapLocationHelper
import com.jude.rollviewpager.RollPagerView
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.adapter.LoopAdapter
import com.meida.base.*
import com.meida.model.*
import com.meida.share.BaseHttp
import com.meida.uswing.*
import com.meida.utils.DialogHelper.showHintDialog
import com.meida.utils.toTextDouble
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.fragment_main_first.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimAdapterEx
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.text.DecimalFormat
import java.util.*

class MainFirstFragment : BaseFragment() {

    private val list = ArrayList<Any>()
    private val listCoach = ArrayList<CommonData>()
    private val listSliders = ArrayList<CommonData>()
    private var mLat = ""
    private var mLng = ""
    private var mCity = ""

    private lateinit var banner: RollPagerView
    private lateinit var mLoopAdapter: LoopAdapter

    //调用这个方法切换时不会释放掉Fragment
    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        this.view?.visibility = if (menuVisible) View.VISIBLE else View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()

        swipe_refresh.isRefreshing = true
        getLocationData()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun init_title() {
        super.init_title()
        swipe_refresh.refresh { getLocationData() }
        recycle_list.load_Linear(activity!!, swipe_refresh)

        val view = LayoutInflater.from(activity).inflate(R.layout.header_first, null)
        banner = view.findViewById(R.id.first_banner)

        mLoopAdapter = LoopAdapter(activity, banner)
        banner.apply {
            setAdapter(mLoopAdapter)
            setOnItemClickListener { /*轮播图点击事件*/ }
        }

        mAdapterEx = SlimAdapter.create(SlimAdapterEx::class.java)
            .addHeader(view)
            .register<String>(R.layout.item_first_dividier) { data, injector ->
                injector.with<SuperTextView>(R.id.first_type) { it.setLeftString(data) }
                    .clicked(R.id.first_type) {
                        when (data) {
                            "附近试炼场" -> startActivity<NearActivity>()
                            "魔镜教练推荐" -> startActivity<CoachActivity>()
                            "高球资讯" -> startActivity<NewsActivity>()
                        }
                    }
            }
            .register<NearData>(R.layout.item_first_near) { data, injector ->
                injector.text(R.id.item_first_name, data.court_name)
                    .text(R.id.item_first_adress, "地址：${data.court_adress}")
                    .text(R.id.item_first_tel, "电话：${data.court_tel}")

                    .with<TextView>(R.id.item_first_length) {
                        val length = data.distance.toTextDouble()

                        when {
                            length == 0.0 -> it.text = "0m"
                            length < 1000 -> it.text = "${DecimalFormat("0.00").format(length)}m"
                            else -> it.text = "${DecimalFormat("0.00").format(length / 1000)}m"
                        }
                    }

                    .with<GlideImageView>(R.id.item_first_img) {
                        it.load(BaseHttp.baseImg + data.court_img, R.mipmap.default_img)
                    }

                    .clicked(R.id.item_near) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivity(
                                Intent(
                                    activity,
                                    NearDetailActivity::class.java
                                ).apply {
                                    putExtra("courtId", data.court_id)
                                    putExtra("lat", mLat)
                                    putExtra("lng", mLng)
                                },
                                ActivityOptions.makeSceneTransitionAnimation(
                                    activity,
                                    it,
                                    "image"
                                ).toBundle()
                            )
                        } else {
                            startActivity<NearDetailActivity>(
                                "courtId" to data.court_id,
                                "lat" to mLat,
                                "lng" to mLng
                            )
                        }

                    }
            }
            .register<CommonData>(R.layout.item_first_recommand) { _, injector ->
                injector.with<RecyclerView>(R.id.first_coach) {
                    it.apply {
                        load_Linear(activity!!)
                        (layoutManager as LinearLayoutManager).orientation =
                            LinearLayoutManager.HORIZONTAL
                        adapter = SlimAdapter.create()
                            .register<CommonData>(R.layout.item_first_coach) { data, injector ->
                                injector.text(R.id.item_first_name, data.nick_name)
                                    .text(R.id.item_first_year, "教龄：${data.teach_age}年")
                                    .text(R.id.item_first_adress, "地区：${data.ucity}")
                                    .visibility(
                                        R.id.item_first_jian,
                                        if (data.recommend == "1") View.VISIBLE else View.INVISIBLE
                                    )

                                    .with<GlideImageView>(R.id.item_first_img) { img ->
                                        img.load(
                                            BaseHttp.baseImg + data.certification_img,
                                            R.mipmap.default_coach
                                        )
                                    }

                                    .clicked(R.id.item_coach) {
                                        startActivity<CoachDetailActivity>(
                                            "certificationId" to data.certification_id
                                        )
                                    }
                            }
                            .attachTo(this)
                    }

                    (it.adapter as SlimAdapter).updateData(listCoach)
                }
            }
            .register<NewsData>(R.layout.item_first_news) { data, injector ->

                val indexNow = list.indexOf(data)
                val isFirst = indexNow == list.indexOfFirst { it is NewsData }
                val isLast = indexNow == list.size - 1

                injector.text(R.id.item_first_title, data.news_title)
                    .text(R.id.item_first_content, data.news_introduction)
                    .text(R.id.item_first_time, data.create_date)
                    .visibility(R.id.item_first_divider1, if (isFirst) View.GONE else View.VISIBLE)
                    .visibility(R.id.item_first_divider2, if (isLast) View.VISIBLE else View.GONE)

                    .with<GlideImageView>(R.id.item_first_img) {
                        it.load(BaseHttp.baseImg + data.news_img, R.mipmap.default_img)
                    }

                    .clicked(R.id.item_news) { startActivity<NewsDetailActivity>("newsId" to data.news_id) }
            }
            .attachTo(recycle_list)

        val mVideo = view.findViewById<LinearLayout>(R.id.first_video)
        val mCompare = view.findViewById<LinearLayout>(R.id.first_compare)
        mVideo.oneClick { startActivity<VideoActivity>() }
        mCompare.oneClick { startActivity<CompareActivity>() }
        first_scan.oneClick { startActivity<ScanActivity>() }
        first_service.oneClick {
            showHintDialog(
                "拨打电话",
                "400-800-9999",
                "取消",
                "拨打"
            ) {

            }
        }
    }

    private fun getLocationData() {
        AMapLocationHelper.getInstance(activity)
            .startLocation(100) { location, isSuccessed, codes ->
                if (100 in codes) {
                    if (isSuccessed) {
                        mLat = location.latitude.toString()
                        mLng = location.longitude.toString()
                        mCity = location.city

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
        OkGo.post<BaseResponse<CommonModel>>(BaseHttp.index_data)
            .tag(this@MainFirstFragment)
            .isMultipart(true)
            .params("lat", mLat)
            .params("lng", mLng)
            .params("city", mCity)
            .execute(object : JacksonDialogCallback<BaseResponse<CommonModel>>(activity) {

                override fun onSuccess(response: Response<BaseResponse<CommonModel>>) {

                    val data = response.body().`object`

                    list.clear()
                    listCoach.clear()
                    listSliders.clear()

                    listCoach.addItems(data.certifications)
                    listSliders.addItems(data.sliders)

                    list.add("附近试炼场")
                    list.addItems(data.courts)

                    list.add("魔镜教练推荐")
                    list.add(CommonData())

                    list.add("高球资讯")
                    list.addItems(data.news)

                    mAdapterEx.updateData(list)

                    val imgs = ArrayList<String>()
                    listSliders.mapTo(imgs) { it.sliderImg }
                    mLoopAdapter.setImgs(imgs)
                    if (imgs.size < 2) {
                        banner.pause()
                        banner.setHintViewVisibility(false)
                    } else {
                        banner.resume()
                        banner.setHintViewVisibility(true)
                    }

                }

                override fun onFinish() {
                    super.onFinish()
                    swipe_refresh.isRefreshing = false
                }

            })
    }

}
