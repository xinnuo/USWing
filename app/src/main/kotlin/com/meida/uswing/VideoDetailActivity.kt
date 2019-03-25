package com.meida.uswing

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.base.oneClick
import com.meida.base.visible
import com.meida.share.BaseHttp
import com.meida.utils.DialogHelper.showGroupDialog
import com.meida.utils.DialogHelper.showShareDialog
import com.meida.utils.dp2px
import com.meida.utils.getScreenWidth
import kotlinx.android.synthetic.main.activity_video_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import org.jetbrains.anko.sdk25.listeners.onSeekBarChangeListener
import org.jetbrains.anko.sdk25.listeners.onTouch
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import tv.danmaku.ijk.media.MultiVideoManager

class VideoDetailActivity : BaseActivity() {

    private var mSpeed = 0.5f
    private var videoFirstId = ""
    private var videoPositive = ""
    private var videoNegative = ""
    private var videoPositiveImg = ""
    private var videoNegativeImg = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        init_title("我的魔频")

        videoFirstId = intent.getStringExtra("magicvoideId") ?: ""
        videoPositive = intent.getStringExtra("video1") ?: ""
        videoNegative = intent.getStringExtra("video2") ?: ""
        videoPositiveImg = intent.getStringExtra("videoImg1") ?: ""
        videoNegativeImg = intent.getStringExtra("videoImg2") ?: ""

        if (videoPositive.isNotEmpty()) initVideoFirst()
        if (videoNegative.isNotEmpty()) initVideoSecond()
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        nav_collect.setImageResource(R.mipmap.index_icon01)
        nav_collect.setPadding(
            dp2px(13.5f),
            dp2px(13.5f),
            dp2px(13.5f),
            dp2px(13.5f)
        )

        val orientation = resources.configuration.orientation
        ivRight.visibility = if (orientation == 2) View.GONE else View.VISIBLE
        nav_collect.visible()

        compare_speed.text = "< 1/2 >"
        compare_first.setSpeedPlaying(mSpeed, true)
        compare_second.setSpeedPlaying(mSpeed, true)

        compare_container.viewTreeObserver.addOnGlobalLayoutListener {
            val with = compare_container.width
            val height = compare_container.height

            when {
                with > height -> {
                    compare_first.layoutParams =
                        FrameLayout.LayoutParams(getScreenWidth() / 2, FrameLayout.LayoutParams.MATCH_PARENT)
                    compare_second.layoutParams =
                        FrameLayout.LayoutParams(
                            getScreenWidth() / 2,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            Gravity.END
                        )
                }
                with < height -> {
                    compare_first.layoutParams =
                        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height / 2)
                    compare_second.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        height / 2,
                        Gravity.BOTTOM
                    )
                }
            }
        }

        compare_first.setVideoProgressListener { progress, secProgress, _, duration ->
            if (compare_second.isPlaying) {
                val durationSecond = compare_second.duration
                if (duration > durationSecond) {
                    compare_progress.progress = progress
                    compare_progress.secondaryProgress = secProgress
                }
            } else {
                compare_progress.progress = progress
                compare_progress.secondaryProgress = secProgress
            }
        }

        compare_second.setVideoProgressListener { progress, secProgress, currentPosition, duration ->
            if (compare_first.isPlaying) {
                val durationFirst = compare_first.duration
                if (duration > durationFirst) {
                    compare_progress.progress = progress
                    compare_progress.secondaryProgress = secProgress
                }
            } else {
                compare_progress.progress = progress
                compare_progress.secondaryProgress = secProgress
            }
        }

        compare_first.setOnPlayListener {
            if (it) compare_play.setImageResource(R.mipmap.mes_icon17)
            else {
                if (compare_second.isPlaying) compare_play.setImageResource(R.mipmap.mes_icon17)
                else compare_play.setImageResource(R.mipmap.mes_icon16)
            }
        }

        compare_second.setOnPlayListener {
            if (it) compare_play.setImageResource(R.mipmap.mes_icon17)
            else {
                if (compare_first.isPlaying) compare_play.setImageResource(R.mipmap.mes_icon17)
                else compare_play.setImageResource(R.mipmap.mes_icon16)
            }
        }

        compare_progress.onSeekBarChangeListener {
            onStopTrackingTouch {
                if (compare_first.isPlaying) compare_first.updataProgress(it!!.progress)
                if (compare_second.isPlaying) compare_second.updataProgress(it!!.progress)
            }
        }

        compare_progress.onTouch { _, _ ->
            return@onTouch !compare_first.isPlaying && !compare_second.isPlaying
        }

        ivRight.oneClick {
            showShareDialog {
                if (videoFirstId.isEmpty()) {
                    toast("视频信息获取失败")
                    return@showShareDialog
                }

                when (it) {
                    "QQ" -> {
                    }
                    "微信" -> {
                    }
                    "朋友圈" -> {
                    }
                    "问答" -> {
                        showGroupDialog("问答内容", "请输入问答内容") { str ->
                            if (str.isEmpty()) {
                                toast("请输入问答内容")
                                return@showGroupDialog
                            }

                            /* 分享问答 */
                            OkGo.post<String>(BaseHttp.add_circle_share)
                                .tag(this@VideoDetailActivity)
                                .isMultipart(true)
                                .headers("token", getString("token"))
                                .params("circleTitle", str)
                                .params("magicvoideId", videoFirstId)
                                .execute(object : StringDialogCallback(baseContext) {

                                    override fun onSuccessResponse(
                                        response: Response<String>,
                                        msg: String,
                                        msgCode: String
                                    ) {
                                        toast(msg)
                                    }

                                })
                        }
                    }
                    "点评" -> {
                        if (videoPositive.isEmpty()) return@showShareDialog

                        startActivity<CompareContactActivity>(
                            "video" to videoPositive,
                            "videoImg" to videoPositiveImg
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.nav_collect -> startActivity<ScanActivity>()
            R.id.compare_speed -> {
                when (mSpeed) {
                    0.5f -> {
                        mSpeed = 0.25f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                        compare_speed.text = "< 1/4 >"
                    }
                    0.25f -> {
                        mSpeed = 0.125f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                        compare_speed.text = "< 1/8 >"
                    }
                    0.125f -> {
                        mSpeed = 0.5f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                        compare_speed.text = "< 1/2 >"
                    }
                }
            }
            R.id.compare_play -> {
                when {
                    compare_first.isPlaying && !compare_second.isPlaying -> compare_first.startToClick()
                    compare_second.isPlaying && !compare_first.isPlaying -> compare_second.startToClick()
                    else -> {
                        compare_first.startToClick()
                        compare_second.startToClick()
                    }
                }
            }
        }
    }

    private fun initVideoFirst() {
        compare_first.apply {
            playTag = "compare"
            playPosition = 1
            loadCoverImage(if (videoPositiveImg.isEmpty()) videoPositive else videoPositiveImg)
            setUp(videoPositive, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
        }
    }

    private fun initVideoSecond() {
        compare_second.apply {
            playTag = "compare"
            playPosition = 2
            loadCoverImage(if (videoNegativeImg.isEmpty()) videoNegative else videoNegativeImg)
            setUp(videoNegative, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val orientation = resources.configuration.orientation
        ivRight.visibility = if (orientation == 2) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        MultiVideoManager.onPauseAll()
    }

    override fun onResume() {
        super.onResume()
        MultiVideoManager.onResumeAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        MultiVideoManager.clearAllVideo()
    }

}
