package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
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
import com.meida.utils.*
import com.meida.utils.DialogHelper.showCommentDialog
import com.meida.utils.DialogHelper.showRewardDialog
import com.meida.utils.DialogHelper.showRightPopup
import com.meida.view.CenterImageSpan
import com.meida.view.EmptyControlVideo
import com.ruanmeng.view.FullyLinearLayoutManager
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.activity_state_detail.*
import net.idik.lib.slimadapter.SlimAdapter
import net.moyokoo.diooto.Diooto
import net.moyokoo.diooto.config.DiootoConfig
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

class StateDetailActivity : BaseActivity() {

    private val circleId by lazy { intent.getStringExtra("circleId") }
    private var mData: CommonData? = null
    private var mIntegral = 0

    private val itemLike = ArrayList<CommonData>()
    private val itemComment = ArrayList<CommonData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state_detail)
        init_title("动态详情")

        getData()
        getNumData()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()

        state_people.apply {
            isNestedScrollingEnabled = false

            //去除焦点
            isFocusableInTouchMode = false
            isFocusable = false

            layoutManager = LinearLayoutManager(
                baseContext,
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

        state_comments.apply {
            isNestedScrollingEnabled = false

            //去除焦点
            isFocusableInTouchMode = false
            isFocusable = false

            layoutManager = FullyLinearLayoutManager(baseContext)
            adapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_state_inner) { item, injector ->

                    val hasUser =
                        if (item.comment_user.isNullOrEmpty()) View.GONE else View.VISIBLE

                    injector.text(R.id.item_inner_title1, item.nick_name)
                        .text(R.id.item_inner_title2, item.comment_nick_name)
                        .visibility(R.id.item_inner_ll, hasUser)
                        .with<TextView>(R.id.item_inner_content) {
                            if (mData?.send_user == getString("token")
                                && mData?.circle_type == "1"
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
                                    .tag(this@StateDetailActivity)
                                    .headers("token", getString("token"))
                                    .params("circleId", circleId)
                                    .params("info", it)
                                    .params("commentUser", item.user_info_id)
                                    .execute(object :
                                        StringDialogCallback(baseContext) {

                                        @SuppressLint("SetTextI18n")
                                        override fun onSuccessResponse(
                                            response: Response<String>,
                                            msg: String,
                                            msgCode: String
                                        ) {

                                            toast(msg)
                                            val commentSum = mData?.comment_ctn!!.toTextInt()
                                            mData?.comment_ctn = (commentSum + 1).toString()
                                            state_num1.text = "评价${mData?.comment_ctn}"

                                            itemComment.add(0, CommonData().apply {
                                                user_info_id = getString("token")
                                                nick_name = getString("nickName")
                                                comment_user = item.user_info_id
                                                comment_nick_name = item.nick_name
                                                comment_info = it
                                            })

                                            EventBus.getDefault().post(RefreshMessageEvent("评论回复"))
                                            (state_comments.adapter as SlimAdapter).updateData(
                                                itemComment
                                            )
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
                                    .tag(this@StateDetailActivity)
                                    .headers("token", getString("token"))
                                    .params("circleId", circleId)
                                    .params("info", it)
                                    .params("commentUser", item.comment_user)
                                    .execute(object :
                                        StringDialogCallback(baseContext) {

                                        @SuppressLint("SetTextI18n")
                                        override fun onSuccessResponse(
                                            response: Response<String>,
                                            msg: String,
                                            msgCode: String
                                        ) {

                                            toast(msg)
                                            val commentSum = mData?.comment_ctn!!.toTextInt()
                                            mData?.comment_ctn = (commentSum + 1).toString()
                                            state_num1.text = "评价${mData?.comment_ctn}"

                                            itemComment.add(0, CommonData().apply {
                                                user_info_id = getString("token")
                                                nick_name = getString("nickName")
                                                comment_user = item.comment_user
                                                comment_nick_name =
                                                    item.comment_nick_name
                                                comment_info = it
                                            })

                                            EventBus.getDefault().post(RefreshMessageEvent("评论回复"))
                                            (state_comments.adapter as SlimAdapter).updateData(
                                                itemComment
                                            )
                                        }

                                    })
                            }
                        }
                        .clicked(R.id.item_inner_content) {
                            if (mData?.send_user == getString("token")
                                && mData?.circle_type == "1"
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
                                        .tag(this@StateDetailActivity)
                                        .headers("token", getString("token"))
                                        .params("rewardSum", it)
                                        .params("rewardUser", item.user_info_id)
                                        .execute(object :
                                            StringDialogCallback(baseContext) {

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
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.state_more -> showRightPopup(
                state_more,
                if (mData?.fctn == "1") "已关注" else "关注"
            ) { hint ->
                when (hint) {
                    "关注" -> {
                        OkGo.post<String>(BaseHttp.add_coach_follow)
                            .tag(this@StateDetailActivity)
                            .headers("token", getString("token"))
                            .params("certificationId", mData?.send_user)
                            .execute(object :
                                StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {
                                    toast(msg)
                                    mData?.fctn = "1"
                                    EventBus.getDefault()
                                        .post(RefreshMessageEvent("关注成功", circleId))
                                }

                            })
                    }
                    "已关注" -> {
                        OkGo.post<String>(BaseHttp.delete_follow)
                            .tag(this@StateDetailActivity)
                            .headers("token", getString("token"))
                            .params("certificationId", mData?.send_user)
                            .execute(object :
                                StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {
                                    toast(msg)
                                    mData?.fctn = "0"
                                    EventBus.getDefault()
                                        .post(RefreshMessageEvent("取消关注", circleId))
                                }

                            })
                    }
                    "举报" -> startActivity<ReportActivity>("circleId" to circleId)
                }
            }
            R.id.state_fram -> {
                val imgArr = JSONArray(mData?.circle_imgs)
                if (mData?.vtype == "1" && imgArr.length() > 0) {
                    val videoPath = imgArr.optJSONObject(0).optString("fPath")
                    val imgVideoPath = imgArr.optJSONObject(0).optString("imgurl")

                    Diooto(baseContext)
                        .immersive(true)
                        .urls(BaseHttp.circleImg + imgVideoPath)
                        .views(v)
                        .type(DiootoConfig.VIDEO)
                        .onProvideVideoView { EmptyControlVideo(baseContext) }
                        .onVideoLoadEnd { dragView, _, progressView ->
                            progressView.gone()

                            (dragView.contentView as EmptyControlVideo).apply {
                                loadCoverImage(BaseHttp.circleImg + imgVideoPath)
                                isLooping = true
                                setUp(BaseHttp.circleImg + videoPath, true, "")
                                startPlayLogic()
                                front.onClick { dragView.backToMin() }
                            }

                            dragView.notifySize(getScreenWidth(), getScreenHeight())
                        }
                        .onFinish { (it.contentView as EmptyControlVideo).release() }
                        .start()
                }
            }
            R.id.state_num1 -> showCommentDialog { str ->
                if (str.isEmpty()) {
                    toast("请输入评论内容")
                    return@showCommentDialog
                }

                /* 圈子评论 */
                OkGo.post<String>(BaseHttp.add_comment)
                    .tag(this@StateDetailActivity)
                    .headers("token", getString("token"))
                    .params("circleId", circleId)
                    .params("info", str)
                    .params("commentUser", "")
                    .execute(object : StringDialogCallback(baseContext) {

                        @SuppressLint("SetTextI18n")
                        override fun onSuccessResponse(
                            response: Response<String>,
                            msg: String,
                            msgCode: String
                        ) {

                            toast(msg)
                            val commentSum = mData?.comment_ctn!!.toTextInt()
                            mData?.comment_ctn = (commentSum + 1).toString()
                            state_num1.text = "评价${mData?.comment_ctn}"

                            itemComment.add(0, CommonData().apply {
                                user_info_id = getString("token")
                                nick_name = getString("nickName")
                                comment_user = ""
                                comment_nick_name = ""
                                comment_info = str
                            })

                            EventBus.getDefault().post(RefreshMessageEvent("评论回复"))
                            (state_comments.adapter as SlimAdapter).updateData(itemComment)
                        }

                    })
            }
            R.id.state_click2 -> {
                if (mData?.lctn != "1") {
                    /* 点赞 */
                    OkGo.post<String>(BaseHttp.add_likes)
                        .tag(this@StateDetailActivity)
                        .headers("token", getString("token"))
                        .params("circleId", circleId)
                        .execute(object : StringDialogCallback(baseContext) {

                            @SuppressLint("SetTextI18n")
                            override fun onSuccessResponse(
                                response: Response<String>,
                                msg: String,
                                msgCode: String
                            ) {

                                toast(msg)
                                mData?.lctn = "1"
                                val likeSum = mData?.like_ctn!!.toTextInt()
                                mData?.like_ctn = (likeSum + 1).toString()

                                state_num2.text = "点赞${mData?.like_ctn}"
                                state_num2.isChecked = mData?.lctn == "1"

                                itemLike.add(0, CommonData().apply {
                                    user_head = getString("userHead")
                                    user_info_id = getString("token")
                                    nick_name = getString("nickName")
                                    circle_id = circleId
                                })

                                EventBus.getDefault().post(RefreshMessageEvent("点赞成功", circleId))
                                (state_people.adapter as SlimAdapter).notifyItemInserted(0)
                            }

                        })
                } else {
                    /* 取消点赞 */
                    OkGo.post<String>(BaseHttp.delete_likes)
                        .tag(this@StateDetailActivity)
                        .headers("token", getString("token"))
                        .params("circleId", circleId)
                        .execute(object : StringDialogCallback(baseContext) {

                            @SuppressLint("SetTextI18n")
                            override fun onSuccessResponse(
                                response: Response<String>,
                                msg: String,
                                msgCode: String
                            ) {

                                toast(msg)
                                mData?.lctn = "0"
                                val likeSum = mData?.like_ctn!!.toTextInt()
                                mData?.like_ctn = (likeSum - 1).toString()

                                state_num2.text = "点赞${mData?.like_ctn}"
                                state_num2.isChecked = mData?.lctn == "1"

                                itemLike.removeAll { it.user_info_id == getString("token") }
                                EventBus.getDefault().post(RefreshMessageEvent("取消点赞", circleId))
                                (state_people.adapter as SlimAdapter).notifyDataSetChanged()
                            }

                        })
                }
            }
            R.id.state_num3 -> {
                if (mData?.send_user == getString("token")) {
                    toast("不能打赏给自己")
                    return
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
                        .tag(this@StateDetailActivity)
                        .headers("token", getString("token"))
                        .params("circleId", circleId)
                        .params("rewardSum", it)
                        .execute(object : StringDialogCallback(baseContext) {

                            @SuppressLint("SetTextI18n")
                            override fun onSuccessResponse(
                                response: Response<String>,
                                msg: String,
                                msgCode: String
                            ) {
                                toast(msg)
                                mIntegral -= it.toTextInt()
                                val likeSum = mData?.reward_ctn!!.toTextInt()
                                mData?.reward_ctn = (likeSum + 1).toString()
                                state_num3.text = "打赏${mData?.reward_ctn}"

                                EventBus.getDefault().post(RefreshMessageEvent("打赏成功", circleId))
                            }

                        })
                }
            }
        }
    }

    /* 积分查询 */
    private fun getNumData() {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@StateDetailActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

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

    override fun getData() {
        OkGo.post<BaseResponse<CommonData>>(BaseHttp.find_circle_details)
            .tag(this@StateDetailActivity)
            .headers("token", getString("token"))
            .params("circleId", circleId)
            .execute(object : JacksonDialogCallback<BaseResponse<CommonData>>(baseContext, true) {

                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: Response<BaseResponse<CommonData>>) {

                    mData = response.body().`object`

                    if (mData != null) {
                        val data = mData!!
                        val imgArr = JSONArray(data.circle_imgs)

                        itemLike.clear()
                        itemComment.clear()
                        itemLike.addItems(data.likes)
                        itemComment.addItems(data.comments)

                        state_img.loadRectImage(BaseHttp.baseImg + data.user_head)
                        state_name.text = data.nick_name
                        state_time.text = TimeHelper.getDiffTime(data.create_date_time.toLong())
                        state_type.setImageResource(if (data.coach == "1") R.mipmap.mes_icon10 else R.mipmap.mes_icon19)
                        state_more.visibility =
                            if (data.send_user == getString("token")) View.GONE else View.VISIBLE
                        state_nine.visibility =
                            if (data.vtype == "0" && imgArr.length() > 0) View.VISIBLE else View.GONE
                        state_video.visibility =
                            if (data.vtype == "1" && imgArr.length() > 0) View.VISIBLE else View.GONE

                        state_num2.isChecked = data.lctn == "1"
                        state_num1.text = "评价${data.comment_ctn}"
                        state_num2.text = "点赞${data.like_ctn}"
                        state_num3.text = "打赏${data.reward_ctn}"

                        if (data.circle_title.isNotEmpty()) {
                            state_title.visible()
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
                                    state_title.text = span
                                }
                            } else state_title.text = data.circle_title
                        } else state_title.gone()

                        if (data.vtype != "1") {
                            val items = ArrayList<String>()
                            for (i in 0 until imgArr.length()) {
                                items.add(BaseHttp.circleImg + imgArr.optJSONObject(i).optString("imgurl"))
                            }

                            state_nine.setSpacing(dp2px(10f).toFloat())
                            state_nine.loadUriList(items)
                            state_nine.updateViews()
                            state_nine.setOnClickImageListener { position, _, _, urlList ->

                                Diooto(baseContext)
                                    .immersive(true)
                                    .urls(urlList.toArray(arrayOfNulls(urlList.size)))
                                    .type(DiootoConfig.PHOTO)
                                    .position(position)
                                    .views(state_nine.itemViews.toArray(arrayOfNulls(urlList.size)))
                                    .loadPhotoBeforeShowBigImage { imageview, pos ->
                                        imageview.displayImage(urlList[pos])
                                    }
                                    .onFinish { }
                                    .start()
                            }
                        }

                        if (data.vtype == "1" && imgArr.length() > 0) {
                            state_fram.load(
                                BaseHttp.circleImg + imgArr.optJSONObject(0).optString("imgurl"),
                                R.mipmap.default_img
                            )
                        }

                        (state_people.adapter as SlimAdapter).updateData(itemLike)
                        (state_comments.adapter as SlimAdapter).updateData(itemComment)

                    }
                }

            })
    }

}
