package com.meida.uswing

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.amap.api.AMapLocationHelper
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.LocationMessageEvent
import com.meida.model.NearData
import com.meida.share.BaseHttp
import com.meida.utils.MultiGapDecoration
import com.meida.utils.hideSoftInput
import com.meida.utils.toTextDouble
import com.meida.utils.trimString
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_title_location.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.text.DecimalFormat
import java.util.*

class NearActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mLat = ""
    private var mLng = ""
    private var mCity = ""
    private var mKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_near)
        setToolbarVisibility(false)
        init_title()

        EventBus.getDefault().register(this@NearActivity)

        swipe_refresh.isRefreshing = true
        getLocationData(pageNum)
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        empty_hint.text = "暂无相关试炼场信息！"
        swipe_refresh.refresh {
            if (mLat.isEmpty() && mLng.isEmpty()) getLocationData(1)
            else getData(1)
        }
        recycle_list.load_Grid(swipe_refresh, {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }, {
            layoutManager = GridLayoutManager(baseContext, 2)
            addItemDecoration(MultiGapDecoration().apply { isOffsetTopEnabled = true })
        })

        mAdapter = SlimAdapter.create()
            .register<NearData>(R.layout.item_first_near) { data, injector ->
                injector.text(R.id.item_first_name, data.court_name)
                    .text(R.id.item_first_tel, data.court_tel)

                    .with<TextView>(R.id.item_first_length) {
                        val length = data.distance.toTextDouble()

                        when {
                            length == 0.0 -> it.text = "0m"
                            length < 1000 -> it.text = "${DecimalFormat("0.00").format(length)}m"
                            else -> it.text = "${DecimalFormat("0.00").format(length / 1000)}km"
                        }
                    }

                    .with<GlideImageView>(R.id.item_first_img) {
                        it.load(BaseHttp.baseImg + data.court_img, R.mipmap.default_img)
                    }

                    .clicked(R.id.item_near) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivity(
                                Intent(
                                    baseContext,
                                    NearDetailActivity::class.java
                                ).apply {
                                    putExtra("courtId", data.court_id)
                                    putExtra("lat", mLat)
                                    putExtra("lng", mLng)
                                },
                                ActivityOptions.makeSceneTransitionAnimation(
                                    this,
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
            .attachTo(recycle_list)

        search_edit.addTextChangedListener(this@NearActivity)
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
            R.id.search_location -> startActivity<NearCityActivity>("type" to "选择位置")
        }
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<NearData>>>(BaseHttp.find_court_list)
            .tag(this@NearActivity)
            .isMultipart(true)
            .params("lat", mLat)
            .params("lng", mLng)
            .params("city", mCity)
            .params("keyword", mKey)
            .params("page", pindex)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<NearData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<NearData>>>) {

                    list.apply {
                        if (pindex == 1) {
                            clear()
                            pageNum = pindex
                        }
                        addItems(response.body().data)
                        if (count(response.body().data) > 0) pageNum++
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

    private fun getLocationData(pindex: Int) {
        AMapLocationHelper.getInstance(baseContext)
            .startLocation(200) { location, isSuccessed, codes ->
                if (200 in codes) {
                    if (isSuccessed) {
                        mLat = location.latitude.toString()
                        mLng = location.longitude.toString()
                        mCity = location.city
                        search_location.text = mCity.replace("市", "")

                        getData(pindex)
                    } else {
                        val errorInfo = location?.locationDetail ?: "位置信息获取失败"
                        toast(errorInfo.split("#")[0])

                        getData(pindex)
                    }
                }
            }
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
        AMapLocationHelper.getInstance(baseContext).removeCode(200)
        EventBus.getDefault().unregister(this@NearActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: LocationMessageEvent) {
        when (event.type) {
            "选择位置" -> {
                mLat = event.lat
                mLng = event.lng
                mCity = event.city
                search_location.text = mCity.replace("市", "")

                updateList()
            }
        }
    }

}
