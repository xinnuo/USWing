package com.meida.uswing

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.DialogHelper.showGroupDialog
import com.meida.utils.DialogHelper.showShareDialog
import com.meida.utils.dp2px
import com.meida.utils.getScreenWidth
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_video_detail.*
import kotlinx.android.synthetic.main.layout_title.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.sdk25.listeners.onSeekBarChangeListener
import org.jetbrains.anko.sdk25.listeners.onTouch
import tv.danmaku.ijk.media.MultiVideoManager
import tv.danmaku.ijk.media.utils.StorageUtils
import java.io.File
import java.util.concurrent.TimeUnit

class VideoDetailActivity : BaseActivity() {

    private var mSpeed = 0.5f
    private var mLayoutHeight = 0
    private var isSame = true

    private var videoFirstId = ""
    private var videoPositive = ""
    private var videoNegative = ""
    private var videoPositiveImg = ""
    private var videoNegativeImg = ""

    private var videoPositiveLocal = ""
    private var videoNegativeLocal = ""

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        init_title("我的魔频")

        showLoadingDialog()
        getDownloadFile(videoPositive) {
            if (videoNegativeLocal.isNotEmpty()) cancelLoadingDialog()
            if (it.isNotEmpty()) {
                videoPositiveLocal = it
                compare_first.setUp(videoPositiveLocal, true, "")
            } else toast("视频加载失败")
        }
        getDownloadFile(videoNegative) {
            if (videoPositiveLocal.isNotEmpty()) cancelLoadingDialog()
            if (it.isNotEmpty()) {
                videoNegativeLocal = it
                compare_second.setUp(videoNegativeLocal, true, "")
            } else toast("视频加载失败")
        }

        Completable.timer(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val guideCount = getInt("guide_index")
                if (guideCount == 5) {
                    putInt("guide_index", 5)
                    (window.decorView as FrameLayout).addView(createView())
                }
            }
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

        videoFirstId = intent.getStringExtra("magicvoideId") ?: ""
        videoPositive = intent.getStringExtra("video1") ?: ""
        videoNegative = intent.getStringExtra("video2") ?: ""
        videoPositiveImg = intent.getStringExtra("videoImg1") ?: ""
        videoNegativeImg = intent.getStringExtra("videoImg2") ?: ""

        if (videoPositive.isNotEmpty()) initVideoFirst()
        if (videoNegative.isNotEmpty()) initVideoSecond()

        compare_speed.text = "1/2"
        compare_first.setSpeedPlaying(mSpeed, true)
        compare_second.setSpeedPlaying(mSpeed, true)

        compare_container.viewTreeObserver.addOnGlobalLayoutListener {
            val height = compare_container.height

            if (mLayoutHeight != height) {
                mLayoutHeight = height
                changeLayoutSize()
            }
        }

        compare_first.setVideoProgressListener { progress, secProgress, _, duration ->
            if (compare_second.isPlaying) {
                val durationSecond = compare_second.duration
                if (duration >= durationSecond) {
                    compare_progress.progress = progress
                    compare_progress.secondaryProgress = secProgress
                }
            } else {
                compare_progress.progress = progress
                compare_progress.secondaryProgress = secProgress
            }
        }

        compare_first.setOnPlayListener {
            val durationFirst = compare_first.duration
            val durationSecond = compare_second.duration
            if (durationSecond <= durationFirst) {
                compare_play.setImageResource(if (it) R.mipmap.video_pause else R.mipmap.video_play)
            }
        }

        compare_second.setVideoProgressListener { progress, secProgress, _, duration ->
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

        compare_second.setOnPlayListener {
            val durationFirst = compare_first.duration
            val durationSecond = compare_second.duration
            if (durationFirst <= durationSecond) {
                compare_play.setImageResource(if (it) R.mipmap.video_pause else R.mipmap.video_play)
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
                        ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.QQ)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(videoNegative).apply {
                                title = getString(R.string.app_name)
                                description = "为你分享我的魔频"
                                setThumb(UMImage(baseContext, R.mipmap.icon_logo))
                            })
                            .share()
                    }
                    "微信" -> {
                        ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.WEIXIN)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(videoNegative).apply {
                                title = getString(R.string.app_name)
                                description = "为你分享我的魔频"
                                setThumb(UMImage(baseContext, R.mipmap.icon_logo))
                            })
                            .share()
                    }
                    "朋友圈" -> {
                        ShareAction(baseContext)
                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                            .withText(getString(R.string.app_name))
                            .withMedia(UMWeb(videoNegative).apply {
                                title = getString(R.string.app_name)
                                description = "为你分享我的魔频"
                                setThumb(UMImage(baseContext, R.mipmap.icon_logo))
                            })
                            .share()
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
                            "videoId" to videoFirstId,
                            "video" to videoPositive,
                            "videoImg" to videoPositiveImg
                        )
                    }
                }
            }
        }
    }

    private fun getDownloadFile(url: String, event: ((String) -> Unit)) {
        if (url.isNotEmpty()) {
            val path = StorageUtils.getIndividualCacheDirectory(baseContext).absolutePath
            val fileName = url.split("/").last()
            val filePath = File(path + File.separator + fileName)
            if (filePath.exists()) event(filePath.absolutePath)
            else {
                OkGo.get<File>(url).execute(object : FileCallback(path, fileName) {
                    private var responeUrl = ""
                    override fun onSuccess(response: Response<File>) { responeUrl = response.body().absolutePath }
                    override fun onError(response: Response<File>) { responeUrl = "" }
                    override fun onFinish() { event(responeUrl) }
                })
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
                        compare_speed.text = "1/4"
                    }
                    0.25f -> {
                        mSpeed = 0.125f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                        compare_speed.text = "1/8"
                    }
                    0.125f -> {
                        mSpeed = 0.5f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                        compare_speed.text = "1/2"
                    }
                }
            }
            R.id.compare_play -> {
                if (videoPositiveLocal.isEmpty()
                    || videoNegativeLocal.isEmpty()) {
                    toast("视频未准备好")
                }

                when {
                    compare_first.isPlaying && !compare_second.isPlaying -> compare_first.startToClick()
                    compare_second.isPlaying && !compare_first.isPlaying -> compare_second.startToClick()
                    else -> {
                        compare_first.startToClick()
                        compare_second.startToClick()
                    }
                }
            }
            R.id.compare_link -> {
                MultiVideoManager.onPauseAll()
                MultiVideoManager.clearAllVideo()

                startActivity<CompareActivity>(
                    "title" to "我的魔频",
                    "magicvoideId" to videoFirstId,
                    "video1" to videoPositive,
                    "video2" to videoNegative,
                    "videoImg1" to videoPositiveImg,
                    "videoImg2" to videoNegativeImg,
                    "share" to true
                )

                ActivityStack.screenManager.popActivities(this@VideoDetailActivity::class.java)
            }
            R.id.compare_lay -> {
                if (isSame) {
                    isSame = !isSame
                    compare_lay.setImageResource(R.mipmap.icon_video1)
                    changeLayoutSize()
                } else {
                    isSame = !isSame
                    compare_lay.setImageResource(R.mipmap.icon_video2)
                    changeLayoutSize()
                }
            }
        }
    }

    private fun changeLayoutSize() {
        val orientation = resources.configuration.orientation
        when (orientation) {
            1 -> {
                if (isSame) {
                    compare_container.setDragEnable(false)

                    compare_first.layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            mLayoutHeight / 2
                        )
                    compare_second.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        mLayoutHeight / 2,
                        Gravity.BOTTOM
                    )
                } else {
                    compare_container.addDragChildView(compare_second)
                    compare_container.setDragEnable(true)

                    compare_first.layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )

                    compare_second.layoutParams =
                        FrameLayout.LayoutParams(
                            dp2px(120f),
                            dp2px(160f),
                            Gravity.BOTTOM
                        )
                }
            }
            2 -> {
                if (isSame) {
                    compare_container.setDragEnable(false)

                    compare_first.layoutParams =
                        FrameLayout.LayoutParams(
                            getScreenWidth() / 2,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    compare_second.layoutParams =
                        FrameLayout.LayoutParams(
                            getScreenWidth() / 2,
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            Gravity.END
                        )
                } else {
                    compare_container.addDragChildView(compare_second)
                    compare_container.setDragEnable(true)

                    compare_first.layoutParams =
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )

                    compare_second.layoutParams =
                        FrameLayout.LayoutParams(
                            dp2px(160f),
                            dp2px(120f),
                            Gravity.BOTTOM
                        )
                }
            }
        }
    }

    private fun initVideoFirst() {
        compare_first.apply {
            playTag = "compare"
            playPosition = 1
            loadCoverImage(if (videoPositiveImg.isEmpty()) videoPositive else videoPositiveImg)
            // setUp(videoPositive, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
        }
    }

    private fun initVideoSecond() {
        compare_second.apply {
            playTag = "compare"
            playPosition = 2
            loadCoverImage(if (videoNegativeImg.isEmpty()) videoNegative else videoNegativeImg)
            // setUp(videoNegative, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
        }
    }

    private fun createView() = UI {
        verticalLayout {
            lparams(width = matchParent, height = matchParent)
            imageView {
                scaleType = ImageView.ScaleType.FIT_XY
                imageResource = R.mipmap.icon_guide6
                onClick {
                    putInt("guide_index", 6)
                    val parent = window.decorView as FrameLayout
                    parent.removeViewAt(parent.childCount - 1)
                }
            }.lparams(width = matchParent, height = matchParent)
        }
    }.view

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this@VideoDetailActivity).onActivityResult(requestCode, resultCode, data)
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
