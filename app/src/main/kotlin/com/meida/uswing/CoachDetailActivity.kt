package com.meida.uswing

import android.os.Bundle
import android.text.Html
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.makeramen.roundedimageview.RoundedImageView
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.getScreenHeight
import com.meida.utils.getScreenWidth
import com.meida.utils.toTextInt
import com.meida.view.EmptyControlVideo
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimAdapterEx
import net.moyokoo.diooto.Diooto
import net.moyokoo.diooto.config.DiootoConfig
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.include
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.sdk25.listeners.onTouch
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class CoachDetailActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var certificationId = ""
    private var hasFollow = ""
    private var hasCollect = ""
    private var followSum = 0

    private lateinit var coach_collect: CheckBox
    private lateinit var coach_add: ImageView
    private lateinit var coach_watch: TextView
    private lateinit var coach_year: TextView
    private lateinit var coach_area: TextView
    private lateinit var coach_name: TextView
    private lateinit var coach_name2: TextView
    private lateinit var coach_tel: TextView
    private lateinit var coach_info: TextView
    private lateinit var coach_honor: TextView
    private lateinit var coach_img: RoundedImageView
    private lateinit var coach_gender: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("教练详情", "加好友")

        swipe_refresh.isRefreshing = true
        getData()
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        certificationId = intent.getStringExtra("certificationId")

        val view = inflate<View>(R.layout.header_coach)
        coach_collect = view.findViewById(R.id.coach_collect)
        coach_add = view.findViewById(R.id.coach_add)
        coach_watch = view.findViewById(R.id.coach_watch)
        coach_year = view.findViewById(R.id.coach_year)
        coach_area = view.findViewById(R.id.coach_area)
        coach_name = view.findViewById(R.id.coach_name)
        coach_name2 = view.findViewById(R.id.coach_name2)
        coach_tel = view.findViewById(R.id.coach_tel)
        coach_info = view.findViewById(R.id.coach_info)
        coach_honor = view.findViewById(R.id.coach_honor)
        coach_img = view.findViewById(R.id.coach_img)
        coach_gender = view.findViewById(R.id.coach_gender)

        swipe_refresh.refresh {
            getData()
            getData(1)
        }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }
        mAdapterEx = SlimAdapter.create(SlimAdapterEx::class.java)
            .addHeader(view)
            .register<CommonData>(R.layout.item_coach_detail) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_detail_title, data.video_introduction)
                    .text(R.id.item_detail_time, data.create_date)
                    .with<GlideImageView>(R.id.item_detail_img) {
                    it.load(BaseHttp.circleImg + data.video_img, R.mipmap.default_img)
                }
                    .visibility(R.id.item_detail_divider1, if (isLast) View.GONE else View.VISIBLE)
                    .visibility(R.id.item_detail_divider2, if (!isLast) View.GONE else View.VISIBLE)
                    .clicked(R.id.item_detail) { v ->
                        Diooto(baseContext)
                            .immersive(true)
                            .urls(BaseHttp.circleImg + data.video_img)
                            .views(v)
                            .type(DiootoConfig.VIDEO)
                            .onProvideVideoView { EmptyControlVideo(baseContext) }
                            .onVideoLoadEnd { dragView, _, progressView ->
                                progressView.gone()

                                (dragView.contentView as EmptyControlVideo).apply {
                                    loadCoverImage(BaseHttp.circleImg + data.video_img)
                                    isLooping = true
                                    setUp(BaseHttp.circleImg + data.videos, true, "")
                                    startPlayLogic()
                                    front.onClick { dragView.backToMin() }
                                }

                                dragView.notifySize(getScreenWidth(), getScreenHeight())
                            }
                            .onFinish { (it.contentView as EmptyControlVideo).release() }
                            .start()
                    }
            }
            .attachTo(recycle_list)

        coach_collect.visibility =
            if (certificationId == getString("token")) View.GONE
            else View.VISIBLE
        coach_add.visibility =
            if (certificationId == getString("token")) View.GONE
            else View.VISIBLE

        tvRight.oneClick {
            startActivity<CoachAddActivity>(
                "toUserId" to certificationId
            )
        }

        coach_collect.onTouch { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    when (hasCollect) {
                        "0" -> OkGo.post<String>(BaseHttp.add_collection)
                            .tag(this@CoachDetailActivity)
                            .headers("token", getString("token"))
                            .params("bussId", certificationId)
                            .params("collectionType", "3")
                            .execute(object : StringDialogCallback(baseContext, false) {

                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    toast(msg)
                                    hasCollect = "1"
                                    coach_collect.isChecked = true
                                    EventBus.getDefault().post(RefreshMessageEvent("添加收藏"))
                                }

                            })
                        "1" -> OkGo.post<String>(BaseHttp.delete_collection)
                            .tag(this@CoachDetailActivity)
                            .headers("token", getString("token"))
                            .params("bussId", certificationId)
                            .params("collectionType", "3")
                            .execute(object : StringDialogCallback(baseContext, false) {

                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    toast(msg)
                                    hasCollect = "0"
                                    coach_collect.isChecked = false
                                    EventBus.getDefault().post(RefreshMessageEvent("取消收藏"))
                                }

                            })
                    }
                }
            }

            return@onTouch true
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.coach_video -> {
                startActivity<CoachVideoActivity>(
                    "type" to "详情魔频",
                    "userInfoId" to certificationId,
                    "isUpload" to true
                )
            }
            R.id.coach_state -> startActivity<CoachStateActivity>("userInfoId" to certificationId)
            R.id.coach_add -> {
                if (certificationId == getString("token")) {
                    toast("不能关注自己")
                    return
                }

                when (hasFollow) {
                    "0" -> OkGo.post<String>(BaseHttp.add_coach_follow)
                        .tag(this@CoachDetailActivity)
                        .headers("token", getString("token"))
                        .params("certificationId", certificationId)
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                                toast(msg)
                                hasFollow = "1"
                                followSum += 1
                                coach_watch.text = followSum.toString()
                                coach_add.setImageResource(R.mipmap.video_icon20)
                                EventBus.getDefault().post(RefreshMessageEvent("添加关注"))
                            }

                        })
                    "1" -> OkGo.post<String>(BaseHttp.delete_follow)
                        .tag(this@CoachDetailActivity)
                        .headers("token", getString("token"))
                        .params("certificationId", certificationId)
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                                toast(msg)
                                hasFollow = "0"
                                followSum -= 1
                                coach_watch.text = followSum.toString()
                                coach_add.setImageResource(R.mipmap.video_icon14)
                                EventBus.getDefault().post(RefreshMessageEvent("取消关注"))
                            }

                        })
                }
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<CommonData>>(BaseHttp.coach_details)
            .tag(this@CoachDetailActivity)
            .params("certificationId", certificationId)
            .params("userInfoId", getString("token"))
            .execute(object :
                JacksonDialogCallback<BaseResponse<CommonData>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<CommonData>>) {

                    if (response.body().data != null) {
                        val data = response.body().data

                        hasCollect = data.collection
                        hasFollow = data.follows
                        followSum = data.follow_ctn.toTextInt()

                        val teachAge = data.teach_age.toTextInt()
                        @Suppress("DEPRECATION")
                        coach_year.text = Html.fromHtml(
                            String.format(
                                "%1\$s<small><small>年</small></small>",
                                teachAge
                            )
                        )

                        coach_area.text = data.ucity
                        coach_watch.text = data.follow_ctn
                        coach_name.text = data.nick_name
                        coach_name2.text = data.nick_name
                        coach_tel.text = data.telephone
                        coach_collect.isChecked = hasCollect == "1"

                        tvRight.visibility =
                            if (data.friend == "1" || certificationId == getString("token")) View.GONE
                            else View.VISIBLE

                        coach_img.setImageURL(BaseHttp.baseImg + data.user_head)
                        coach_gender.setImageResource(
                            if (data.gender == "0") R.mipmap.video_icon08
                            else R.mipmap.video_icon07
                        )
                        coach_add.setImageResource(
                            if (data.follows == "1") R.mipmap.video_icon20
                            else R.mipmap.video_icon14
                        )

                        coach_info.text = data.introduction
                        val items = ArrayList<String>()
                        val honors = ArrayList<CommonData>()
                        honors.addItems(data.honors)
                        honors.mapTo(items) { it.honorInfo }
                        coach_honor.text = items.joinToString("\n")
                    }
                }

            })
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_voide_coach)
            .tag(this@CoachDetailActivity)
            .headers("token", certificationId)
            .params("page", pindex)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        if (pindex == 1) {
                            clear()
                            pageNum = pindex
                        }
                        addItems(response.body().data)
                        if (count(response.body().data) > 0) pageNum++
                    }

                    mAdapterEx.updateData(list)
                }

                override fun onFinish() {
                    super.onFinish()
                    swipe_refresh.isRefreshing = false
                    isLoadingMore = false
                }

            })
    }

}
