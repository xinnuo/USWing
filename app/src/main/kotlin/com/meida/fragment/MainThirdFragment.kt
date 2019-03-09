package com.meida.fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.uswing.R
import com.meida.uswing.ReportActivity
import com.meida.uswing.StateDetailActivity
import com.meida.uswing.StateIssueActivity
import com.meida.utils.*
import com.meida.utils.DialogHelper.showCommentDialog
import com.meida.utils.DialogHelper.showItemDialog
import com.meida.utils.DialogHelper.showRewardDialog
import com.meida.utils.DialogHelper.showRightPopup
import com.meida.view.CenterImageSpan
import com.meida.view.EmptyControlVideo
import com.meida.view.NineGridLayout
import com.ruanmeng.view.FullyLinearLayoutManager
import com.sunfusheng.GlideImageView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main_third.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.moyokoo.diooto.Diooto
import net.moyokoo.diooto.config.DiootoConfig
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MainThirdFragment : BaseFragment() {

    private val list = ArrayList<CommonData>()
    private var mIntegral = 0
    private var mCircleType = ""
    private var mType = ""

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
        return inflater.inflate(R.layout.fragment_main_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()

        EventBus.getDefault().register(this@MainThirdFragment)
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        third_tab.apply {
            onTabSelectedListener {
                onTabSelected {
                    when (it!!.position) {
                        0 -> {
                            mCircleType = "2"
                            mType = "1"
                        }
                        1 -> {
                            mCircleType = "1"
                            mType = ""
                        }
                        2 -> {
                            mCircleType = "2"
                            mType = "2"
                        }
                    }

                    OkGo.getInstance().cancelTag(this@MainThirdFragment)
                    Completable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { updateList() }
                }
            }

            getChildAt(0).apply {
                this as LinearLayout
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                dividerDrawable =
                    ContextCompat.getDrawable(activity!!, R.drawable.layout_divider_vertical)
                dividerPadding = dp2px(15f)
            }

            addTab(this.newTab().setText("关注"), true)
            addTab(this.newTab().setText("问答"), false)
            addTab(this.newTab().setText("好友"), false)
        }

        empty_hint.text = "暂无相关圈子信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(activity!!, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_state_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                val imgArr = JSONArray(data.circle_imgs)
                val timeFormat = TimeHelper.getDiffTime(data.create_date_time.toLong())
                val resourceId = if (data.coach == "1") R.mipmap.mes_icon10 else R.mipmap.mes_icon19
                val hasLast = if (isLast) View.VISIBLE else View.GONE
                val hasMine = if (data.send_user == getString("token")) View.GONE else View.VISIBLE
                val hasVideo =
                    if (data.vtype == "1" && imgArr.length() > 0) View.VISIBLE else View.GONE
                val hasImg =
                    if (data.vtype == "0" && imgArr.length() > 0) View.VISIBLE else View.GONE

                val itemLike = data.likes ?: ArrayList()
                val itemComment = data.comments ?: ArrayList()

                injector.text(R.id.item_state_name, data.nick_name)
                    .text(R.id.item_state_time, timeFormat)
                    .text(R.id.item_state_num1, "评价${data.comment_ctn}")
                    .text(R.id.item_state_num2, "点赞${data.like_ctn}")
                    .text(R.id.item_state_num3, "打赏${data.reward_ctn}")
                    .image(R.id.item_state_type, resourceId)
                    .checked(R.id.item_state_num2, data.lctn == "1")
                    .visibility(R.id.item_state_more, hasMine)
                    .visibility(R.id.item_state_video, hasVideo)
                    .visibility(R.id.item_state_nine, hasImg)
                    .visibility(R.id.item_state_divider, hasLast)

                    .with<GlideImageView>(R.id.item_state_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }

                    .with<TextView>(R.id.item_state_title) {
                        if (data.circle_title.isNotEmpty()) {
                            it.visible()
                            val span = SpannableStringBuilder()
                            if (data.circle_type == "1") {
                                span.append("★ ")
                                span.append(data.circle_title)
                                val matcher = Pattern.compile("★").matcher(span)
                                if (matcher.find()) {
                                    @Suppress("DEPRECATION")
                                    val drawable = resources.getDrawable(R.mipmap.mes_icon18)
                                    drawable.setBounds(0, 0, dp2px(17f), dp2px(17f))
                                    span.setSpan(
                                        CenterImageSpan(drawable),
                                        matcher.start(),
                                        matcher.end(),
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    it.text = span
                                }
                            } else it.text = data.circle_title
                        } else it.gone()
                    }

                    .with<NineGridLayout>(R.id.item_state_nine) {
                        if (data.vtype == "1") return@with

                        val items = ArrayList<String>()
                        for (i in 0 until imgArr.length()) {
                            items.add(BaseHttp.circleImg + imgArr.optJSONObject(i).optString("imgurl"))
                        }

                        it.setSpacing(dp2px(10f).toFloat())
                        it.loadUriList(items)
                        it.setOnClickImageListener { position, _, _, urlList ->

                            Diooto(activity)
                                .immersive(true)
                                .urls(urlList.toArray(arrayOfNulls(urlList.size)))
                                .type(DiootoConfig.PHOTO)
                                .position(position)
                                .views(it.itemViews.toArray(arrayOfNulls(urlList.size)))
                                .loadPhotoBeforeShowBigImage { imageview, pos ->
                                    imageview.displayImage(urlList[pos])
                                }
                                .onFinish { }
                                .start()
                        }
                    }

                    .with<GlideImageView>(R.id.item_state_fram) {
                        if (data.vtype != "1") return@with

                        if (imgArr.length() > 0) {
                            it.load(
                                BaseHttp.circleImg + imgArr.optJSONObject(0).optString("imgurl"),
                                R.mipmap.default_img
                            )
                        }
                    }

                    .with<RecyclerView>(R.id.item_state_people) {
                        it.apply {
                            isNestedScrollingEnabled = false

                            //去除焦点
                            isFocusableInTouchMode = false
                            isFocusable = false

                            layoutManager = LinearLayoutManager(
                                activity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            adapter = SlimAdapter.create()
                                .register<CommonData>(R.layout.item_state_people) { item, injector ->
                                    injector.with<GlideImageView>(R.id.item_people) { inner ->
                                        inner.loadRectImage(BaseHttp.baseImg + item.user_head)
                                    }
                                }
                                .attachTo(this)
                        }

                        (it.adapter as SlimAdapter).updateData(itemLike)
                    }

                    .with<RecyclerView>(R.id.item_state_comments) { view ->
                        view.apply {
                            isNestedScrollingEnabled = false

                            //去除焦点
                            isFocusableInTouchMode = false
                            isFocusable = false

                            layoutManager = FullyLinearLayoutManager(activity!!)
                            adapter = SlimAdapter.create()
                                .register<CommonData>(R.layout.item_state_inner) { item, injector ->

                                    val hasUser =
                                        if (item.comment_user.isNullOrEmpty()) View.GONE else View.VISIBLE

                                    injector.text(R.id.item_inner_title1, item.nick_name)
                                        .text(R.id.item_inner_title2, item.comment_nick_name)
                                        .visibility(R.id.item_inner_ll, hasUser)
                                        .with<TextView>(R.id.item_inner_content) {
                                            if (data.send_user == getString("token")
                                                && data.circle_type == "1"
                                                && item.comment_user.isNullOrEmpty()
                                                && item.user_info_id != getString("token")
                                            ) {
                                                val span = SpannableStringBuilder()
                                                span.append(item.comment_info)
                                                span.append(" ★")
                                                val matcher = Pattern.compile("★").matcher(span)
                                                if (matcher.find()) {
                                                    @Suppress("DEPRECATION")
                                                    val drawable =
                                                        resources.getDrawable(R.mipmap.mes_icon20)
                                                    drawable.setBounds(0, 0, dp2px(40f), dp2px(16f))
                                                    span.setSpan(
                                                        CenterImageSpan(drawable),
                                                        matcher.start(),
                                                        matcher.end(),
                                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                                    )
                                                    it.text = span
                                                }
                                            } else it.text = item.comment_info
                                        }
                                        .clicked(R.id.item_inner_title1) {
                                            if (item.user_info_id == getString("token")) return@clicked

                                            showCommentDialog {
                                                if (it.isEmpty()) {
                                                    toast("请输入评论内容")
                                                    return@showCommentDialog
                                                }

                                                /* 回复评论 */
                                                OkGo.post<String>(BaseHttp.add_comment)
                                                    .tag(this@MainThirdFragment)
                                                    .headers("token", getString("token"))
                                                    .params("circleId", data.circle_id)
                                                    .params("info", it)
                                                    .params("commentUser", item.user_info_id)
                                                    .execute(object :
                                                        StringDialogCallback(activity) {

                                                        override fun onSuccessResponse(
                                                            response: Response<String>,
                                                            msg: String,
                                                            msgCode: String
                                                        ) {

                                                            toast(msg)
                                                            val commentSum =
                                                                data.comment_ctn.toTextInt()
                                                            data.comment_ctn =
                                                                (commentSum + 1).toString()
                                                            itemComment.add(0, CommonData().apply {
                                                                user_info_id = getString("token")
                                                                nick_name = getString("nickName")
                                                                comment_user = item.user_info_id
                                                                comment_nick_name = item.nick_name
                                                                comment_info = it
                                                            })
                                                            mAdapter.notifyItemChanged(index)
                                                        }

                                                    })
                                            }
                                        }
                                        .clicked(R.id.item_inner_title2) {
                                            if (item.comment_user == getString("token")) return@clicked

                                            showCommentDialog {
                                                if (it.isEmpty()) {
                                                    toast("请输入评论内容")
                                                    return@showCommentDialog
                                                }

                                                /* 回复评论 */
                                                OkGo.post<String>(BaseHttp.add_comment)
                                                    .tag(this@MainThirdFragment)
                                                    .headers("token", getString("token"))
                                                    .params("circleId", data.circle_id)
                                                    .params("info", it)
                                                    .params("commentUser", item.comment_user)
                                                    .execute(object :
                                                        StringDialogCallback(activity) {

                                                        override fun onSuccessResponse(
                                                            response: Response<String>,
                                                            msg: String,
                                                            msgCode: String
                                                        ) {

                                                            toast(msg)
                                                            val commentSum =
                                                                data.comment_ctn.toTextInt()
                                                            data.comment_ctn =
                                                                (commentSum + 1).toString()
                                                            itemComment.add(0, CommonData().apply {
                                                                user_info_id = getString("token")
                                                                nick_name = getString("nickName")
                                                                comment_user = item.comment_user
                                                                comment_nick_name =
                                                                    item.comment_nick_name
                                                                comment_info = it
                                                            })
                                                            mAdapter.notifyItemChanged(index)
                                                        }

                                                    })
                                            }
                                        }
                                        .clicked(R.id.item_inner_content) {
                                            if (data.send_user == getString("token")
                                                && data.circle_type == "1"
                                                && item.comment_user.isNullOrEmpty()
                                                && item.user_info_id != getString("token")
                                            ) {
                                                showRewardDialog(mIntegral) {
                                                    if (it.isEmpty() || it.toTextInt() <= 0) {
                                                        toast("请输入打赏积分数")
                                                        return@showRewardDialog
                                                    }

                                                    if (it.toTextInt() > mIntegral) {
                                                        toast("当前积分不足")
                                                        return@showRewardDialog
                                                    }

                                                    /* 打赏用户 */
                                                    OkGo.post<String>(BaseHttp.add_reward_user)
                                                        .tag(this@MainThirdFragment)
                                                        .headers("token", getString("token"))
                                                        .params("rewardSum", it)
                                                        .params("rewardUser", item.user_info_id)
                                                        .execute(object :
                                                            StringDialogCallback(activity) {

                                                            override fun onSuccessResponse(
                                                                response: Response<String>,
                                                                msg: String,
                                                                msgCode: String
                                                            ) {
                                                                toast(msg)
                                                                mIntegral -= it.toTextInt()
                                                            }

                                                        })
                                                }
                                            }
                                        }
                                }
                                .attachTo(this)
                        }

                        (view.adapter as SlimAdapter).updateData(itemComment)
                    }

                    .clicked(R.id.item_state_video) { v ->
                        if (data.vtype == "1") {
                            val videoPath = imgArr.optJSONObject(0).optString("fPath")
                            val imgVideoPath = imgArr.optJSONObject(0).optString("imgurl")

                            Diooto(activity)
                                .immersive(true)
                                .urls(BaseHttp.circleImg + imgVideoPath)
                                .views(v)
                                .type(DiootoConfig.VIDEO)
                                .onProvideVideoView { EmptyControlVideo(activity) }
                                .onVideoLoadEnd { dragView, _, progressView ->
                                    progressView.gone()

                                    (dragView.contentView as EmptyControlVideo).apply {
                                        loadCoverImage(BaseHttp.circleImg + imgVideoPath)
                                        isLooping = true
                                        setUp(BaseHttp.circleImg + videoPath, true, "")
                                        startPlayLogic()
                                        front.onClick { dragView.backToMin() }
                                    }

                                    dragView.notifySize(activity!!.getScreenWidth(), activity!!.getScreenHeight())
                                }
                                .onFinish { (it.contentView as EmptyControlVideo).release() }
                                .start()
                        }
                    }

                    .clicked(R.id.item_state_more) {
                        showRightPopup(it, if (data.fctn == "1") "已关注" else "关注") { hint ->
                            when (hint) {
                                "关注" -> {
                                    OkGo.post<String>(BaseHttp.add_coach_follow)
                                        .tag(this@MainThirdFragment)
                                        .headers("token", getString("token"))
                                        .params("certificationId", data.send_user)
                                        .execute(object :
                                            StringDialogCallback(activity) {

                                            override fun onSuccessResponse(
                                                response: Response<String>,
                                                msg: String,
                                                msgCode: String
                                            ) {
                                                toast(msg)
                                                data.fctn = "1"
                                                mAdapter.notifyItemChanged(index)
                                            }

                                        })
                                }
                                "已关注" -> {
                                    OkGo.post<String>(BaseHttp.delete_follow)
                                        .tag(this@MainThirdFragment)
                                        .headers("token", getString("token"))
                                        .params("certificationId", data.send_user)
                                        .execute(object :
                                            StringDialogCallback(activity) {

                                            override fun onSuccessResponse(
                                                response: Response<String>,
                                                msg: String,
                                                msgCode: String
                                            ) {
                                                toast(msg)
                                                if (mType == "1") {
                                                    list.removeAll { inner ->
                                                        inner.send_user == data.send_user
                                                    }

                                                    empty_view.apply { if (list.isEmpty()) visible() else gone() }
                                                    mAdapter.notifyDataSetChanged()
                                                } else {
                                                    data.fctn = "0"
                                                    mAdapter.notifyItemChanged(index)
                                                }
                                            }

                                        })
                                }
                                "举报" -> startActivity<ReportActivity>("circleId" to data.circle_id)
                            }
                        }
                    }

                    .clicked(R.id.item_state_num1) {
                        showCommentDialog { str ->
                            if (str.isEmpty()) {
                                toast("请输入评论内容")
                                return@showCommentDialog
                            }

                            /* 圈子评论 */
                            OkGo.post<String>(BaseHttp.add_comment)
                                .tag(this@MainThirdFragment)
                                .headers("token", getString("token"))
                                .params("circleId", data.circle_id)
                                .params("info", str)
                                .params("commentUser", "")
                                .execute(object : StringDialogCallback(activity) {

                                    override fun onSuccessResponse(
                                        response: Response<String>,
                                        msg: String,
                                        msgCode: String
                                    ) {

                                        toast(msg)
                                        val commentSum = data.comment_ctn.toTextInt()
                                        data.comment_ctn = (commentSum + 1).toString()
                                        itemComment.add(0, CommonData().apply {
                                            user_info_id = getString("token")
                                            nick_name = getString("nickName")
                                            comment_info = str
                                        })
                                        mAdapter.notifyItemChanged(index)
                                    }

                                })
                        }
                    }

                    .clicked(R.id.item_state_click2) {
                        if (data.lctn != "1") {
                            /* 点赞 */
                            OkGo.post<String>(BaseHttp.add_likes)
                                .tag(this@MainThirdFragment)
                                .headers("token", getString("token"))
                                .params("circleId", data.circle_id)
                                .execute(object : StringDialogCallback(activity) {

                                    override fun onSuccessResponse(
                                        response: Response<String>,
                                        msg: String,
                                        msgCode: String
                                    ) {

                                        toast(msg)
                                        data.lctn = "1"
                                        val likeSum = data.like_ctn.toTextInt()
                                        data.like_ctn = (likeSum + 1).toString()
                                        itemLike.add(0, CommonData().apply {
                                            user_head = getString("userHead")
                                            user_info_id = getString("token")
                                            nick_name = getString("nickName")
                                            circle_id = data.circle_id
                                        })
                                        mAdapter.notifyItemChanged(index)
                                    }

                                })
                        } else {
                            /* 取消点赞 */
                            OkGo.post<String>(BaseHttp.delete_likes)
                                .tag(this@MainThirdFragment)
                                .headers("token", getString("token"))
                                .params("circleId", data.circle_id)
                                .execute(object : StringDialogCallback(activity) {

                                    override fun onSuccessResponse(
                                        response: Response<String>,
                                        msg: String,
                                        msgCode: String
                                    ) {

                                        toast(msg)
                                        data.lctn = "0"
                                        val likeSum = data.like_ctn.toTextInt()
                                        data.like_ctn = (likeSum - 1).toString()
                                        itemLike.removeAll { it.user_info_id == getString("token") }
                                        mAdapter.notifyItemChanged(index)
                                    }

                                })
                        }
                    }

                    .clicked(R.id.item_state_num3) {
                        if (data.send_user == getString("token")) {
                            toast("不能打赏给自己")
                            return@clicked
                        }

                        showRewardDialog(mIntegral) {
                            if (it.isEmpty() || it.toTextInt() <= 0) {
                                toast("请输入打赏积分数")
                                return@showRewardDialog
                            }

                            if (it.toTextInt() > mIntegral) {
                                toast("当前积分不足")
                                return@showRewardDialog
                            }

                            /* 打赏圈子 */
                            OkGo.post<String>(BaseHttp.add_reward)
                                .tag(this@MainThirdFragment)
                                .headers("token", getString("token"))
                                .params("circleId", data.circle_id)
                                .params("rewardSum", it)
                                .execute(object :
                                    StringDialogCallback(activity) {

                                    override fun onSuccessResponse(
                                        response: Response<String>,
                                        msg: String,
                                        msgCode: String
                                    ) {
                                        toast(msg)
                                        mIntegral -= it.toTextInt()
                                        val likeSum = data.reward_ctn.toTextInt()
                                        data.reward_ctn = (likeSum + 1).toString()
                                        mAdapter.notifyItemChanged(index)
                                    }

                                })
                        }
                    }

                    .clicked(R.id.item_state) { startActivity<StateDetailActivity>("circleId" to data.circle_id) }
            }
            .attachTo(recycle_list)

        third_issue.onClick {
            showItemDialog("动态", "问答") {
                when (it) {
                    0 -> startActivity<StateIssueActivity>("circleIype" to "2")
                    1 -> startActivity<StateIssueActivity>("circleIype" to "1")
                }
            }
        }
    }

    /* 积分查询 */
    override fun getData() {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@MainThirdFragment)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(activity, false) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    mIntegral = obj.optString("integral", "0").toInt()
                }

            })
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_circle_list)
            .tag(this@MainThirdFragment)
            .headers("token", getString("token"))
            .params("circleType", mCircleType)
            .params("type", mType)
            .params("page", pindex)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(activity) {

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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@MainThirdFragment)
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "关注成功" -> list.first { it.circle_id == event.id }.fctn = "1"
            "点赞成功" -> {
                val index = list.indexOfFirst { it.circle_id == event.id }
                val itemLike = list[index].likes ?: ArrayList()

                list[index].lctn = "1"
                val likeSum = list[index].like_ctn.toTextInt()
                list[index].like_ctn = (likeSum + 1).toString()

                itemLike.add(0, CommonData().apply {
                    user_head = getString("userHead")
                    user_info_id = getString("token")
                    nick_name = getString("nickName")
                    circle_id = event.id
                })

                mAdapter.notifyItemChanged(index)
            }
            "取消点赞" -> {
                val index = list.indexOfFirst { it.circle_id == event.id }
                val itemLike = list[index].likes ?: ArrayList()

                list[index].lctn = "0"
                val likeSum = list[index].like_ctn.toTextInt()
                list[index].like_ctn = (likeSum - 1).toString()

                itemLike.removeAll { it.user_info_id == getString("token") }
                mAdapter.notifyItemChanged(index)
            }
            "打赏成功" -> {
                val index = list.indexOfFirst { it.circle_id == event.id }

                val likeSum = list[index].reward_ctn.toTextInt()
                list[index].reward_ctn = (likeSum + 1).toString()
                mAdapter.notifyItemChanged(index)
            }
            "取消关注", "评论回复" -> updateList()
        }
    }

}
