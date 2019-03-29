package com.meida.uswing

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.RefreshMessageEvent
import com.meida.utils.DialogHelper.showCompareDialog
import com.meida.utils.dp2px
import com.meida.utils.getScreenWidth
import com.umeng.socialize.UMShareAPI
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_compare.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.sdk25.listeners.onSeekBarChangeListener
import org.jetbrains.anko.sdk25.listeners.onTouch
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import tv.danmaku.ijk.media.MultiVideoManager
import tv.danmaku.ijk.media.utils.StorageUtils
import java.io.File
import java.util.concurrent.TimeUnit

class CompareActivity : BaseActivity() {

    private var mSpeed = 0.5f
    private var mLayoutHeight = 0
    private var isFront = true
    private var isSame = true
    private var isEditable = true

    private var videoFirstId = ""
    private var videoPositive = ""
    private var videoNegative = ""
    private var videoPositiveImg = ""
    private var videoNegativeImg = ""

    private var videoSecondId = ""
    private var videoPositiveCompare = ""
    private var videoNegativeCompare = ""
    private var videoPositiveImgCompare = ""
    private var videoNegativeImgCompare = ""

    private var videoPositiveLocal = ""
    private var videoNegativeLocal = ""
    private var videoPositiveCompareLocal = ""
    private var videoNegativeCompareLocal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)
        init_title("魔镜对比")

        EventBus.getDefault().register(this@CompareActivity)

        if (videoPositive.isNotEmpty()) {
            getDownloadFile(videoPositive) {
                if (it.isNotEmpty()) {
                    videoPositiveLocal = it
                    compare_first.setUp(videoPositiveLocal, true, "")
                } else toast("视频加载失败")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        compare_control.gone()
        compare_progress.gone()

        videoFirstId = intent.getStringExtra("magicvoideId") ?: ""
        videoPositive = intent.getStringExtra("video1") ?: ""
        videoNegative = intent.getStringExtra("video2") ?: ""
        videoPositiveImg = intent.getStringExtra("videoImg1") ?: ""
        videoNegativeImg = intent.getStringExtra("videoImg2") ?: ""

        compare_speed.text = "1/2"
        compare_first.setSpeedPlaying(mSpeed, true)
        compare_second.setSpeedPlaying(mSpeed, true)

        if (videoPositive.isNotEmpty()) {
            isEditable = false
            initVideoFirst()
        }

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
    }

    private fun initVideoFirst(isAdd: Boolean = false) {
        compare_top.invisible()
        compare_progress.progress = 0
        compare_progress.secondaryProgress = 0

        compare_first.apply {
            playTag = "compare"
            playPosition = 1
            loadCoverImage(if (isFront) videoPositiveImg else videoNegativeImg)
            setUp(
                if (isFront) videoPositive else videoNegative,
                true,
                ""
            )
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
            setGone(!isAdd)
            addButton.oneClick {
                showCompareDialog {
                    when (it) {
                        "教练魔频" -> toCoach("第一对比", videoFirstId)
                        "我的魔频" -> toMine("第一对比", videoFirstId)
                        "我的收藏" -> toCollect("第一对比", videoFirstId)
                    }
                }
            }
        }
    }

    private fun initVideoSecond(isAdd: Boolean = false) {
        compare_bottom.invisible()
        compare_progress.progress = 0
        compare_progress.secondaryProgress = 0

        compare_second.apply {
            playTag = "compare"
            playPosition = 2
            loadCoverImage(if (isFront) videoPositiveImgCompare else videoNegativeImgCompare)
            setUp(
                if (isFront) videoPositiveCompare else videoNegativeCompare,
                true,
                ""
            )
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
            setGone(!isAdd)
            addButton.oneClick {
                showCompareDialog {
                    when (it) {
                        "教练魔频" -> toCoach("第二对比", videoSecondId)
                        "我的魔频" -> toMine("第二对比", videoSecondId)
                        "我的收藏" -> toCollect("第二对比", videoSecondId)
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
                showLoadingDialog()
                OkGo.get<File>(url).execute(object : FileCallback(path, fileName) {
                    private var responeUrl = ""
                    override fun onSuccess(response: Response<File>) { responeUrl = response.body().absolutePath }
                    override fun onError(response: Response<File>) { responeUrl = "" }
                    override fun onFinish() {
                        cancelLoadingDialog()
                        event(responeUrl)
                    }
                })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
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
            R.id.compare_side -> {
                isFront = !isFront
                compare_side.text = if (isFront) "正" else "侧"
                switchVideoSource()
            }
            R.id.compare_select1 -> {
                showCompareDialog {
                    when (it) {
                        "教练魔频" -> toCoach("第一对比", videoFirstId)
                        "我的魔频" -> toMine("第一对比", videoFirstId)
                        "我的收藏" -> toCollect("第一对比", videoFirstId)
                    }
                }
            }
            R.id.compare_select2 -> {
                showCompareDialog {
                    when (it) {
                        "教练魔频" -> toCoach("第二对比", videoSecondId)
                        "我的魔频" -> toMine("第二对比", videoSecondId)
                        "我的收藏" -> toCollect("第二对比", videoSecondId)
                    }
                }
            }
            R.id.compare_play -> {
                if (isFront) {
                    if (videoPositiveLocal.isEmpty()
                        || videoPositiveCompareLocal.isEmpty()) {
                        toast("视频未准备好")
                    }
                } else {
                    if (videoNegativeLocal.isEmpty()
                        || videoNegativeCompareLocal.isEmpty()) {
                        toast("视频未准备好")
                    }
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
            R.id.compare_lay -> {
                if (isSame) {
                    isSame = !isSame
                    compare_lay.setImageResource(R.mipmap.icon_video1)
                    compare_second.setGone(true)
                    changeLayoutSize()
                } else {
                    isSame = !isSame
                    compare_lay.setImageResource(R.mipmap.icon_video2)
                    compare_second.setGone(false)
                    changeLayoutSize()
                }
            }
        }
    }

    private fun toCoach(flag: String, videoId: String) {
        startActivity<CompareCoachActivity>(
            "flag" to flag,
            "selectId" to videoId
        )
    }

    private fun toMine(flag: String, videoId: String) {
        startActivity<CoachVideoActivity>(
            "type" to "我的魔频",
            "flag" to flag,
            "selectId" to videoId
        )
    }

    private fun toCollect(flag: String, videoId: String) {
        startActivity<CompareCollectActivity>(
            "flag" to flag,
            "selectId" to videoId
        )
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

    @SuppressLint("CheckResult")
    private fun switchVideoSource() {
        MultiVideoManager.onPauseAll()

        Completable.timer(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                switchVideoFirst()
                switchVideoSecond()
            }
    }

    private fun switchVideoFirst() {
        if (videoPositive.isNotEmpty()
            && videoNegative.isNotEmpty()
        ) {
            compare_first.loadCoverImage(if (isFront) videoPositiveImg else videoNegativeImg)
            if (isFront) {
                if (videoPositiveLocal.isEmpty()) {
                    compare_first.onVideoPause()
                    getDownloadFile(videoPositive) {
                        if (it.isNotEmpty()) {
                            videoPositiveLocal = it
                            compare_first.setUp(videoPositiveLocal, true, "")
                        } else toast("视频加载失败")
                    }
                } else {
                    compare_first.setUp(videoPositiveLocal, true, "")
                }
            } else {
                if (videoNegativeLocal.isEmpty()) {
                    compare_first.onVideoPause()
                    getDownloadFile(videoNegative) {
                        if (it.isNotEmpty()) {
                            videoNegativeLocal = it
                            compare_first.setUp(videoNegativeLocal, true, "")
                        } else toast("视频加载失败")
                    }
                } else {
                    compare_first.setUp(videoNegativeLocal, true, "")
                }
            }
        }
    }

    private fun switchVideoSecond() {
        if (videoPositiveCompare.isNotEmpty()
            && videoNegativeCompare.isNotEmpty()
        ) {
            compare_second.loadCoverImage(if (isFront) videoPositiveImgCompare else videoNegativeImgCompare)
            if (isFront) {
                if (videoPositiveCompareLocal.isEmpty()) {
                    compare_second.onVideoPause()
                    getDownloadFile(videoPositiveCompare) {
                        if (it.isNotEmpty()) {
                            videoPositiveCompareLocal = it
                            compare_second.setUp(videoPositiveCompareLocal, true, "")
                        } else toast("视频加载失败")
                    }
                } else {
                    compare_second.setUp(videoPositiveCompareLocal, true, "")
                }
            } else {
                if (videoNegativeCompareLocal.isEmpty()) {
                    compare_second.onVideoPause()
                    getDownloadFile(videoNegativeCompare) {
                        if (it.isNotEmpty()) {
                            videoNegativeCompareLocal = it
                            compare_second.setUp(videoNegativeCompareLocal, true, "")
                        } else toast("视频加载失败")
                    }
                } else {
                    compare_second.setUp(videoNegativeCompareLocal, true, "")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this@CompareActivity).onActivityResult(requestCode, resultCode, data)
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

    override fun finish() {
        EventBus.getDefault().unregister(this@CompareActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "第一对比" -> {
                if (event.id == videoSecondId) {
                    toast("已选择过该魔频")
                    return
                }

                videoFirstId = event.id
                videoPositive = event.name
                videoNegative = event.checkId
                videoPositiveImg = event.title
                videoNegativeImg = event.memo

                videoPositiveLocal = ""
                videoNegativeLocal = ""

                initVideoFirst(true)
                if (videoSecondId.isNotEmpty()) {
                    initVideoSecond(true)
                    compare_control.visible()
                    compare_progress.visible()
                }

                getDownloadFile(if (isFront) videoPositive else videoNegative) {
                    if (it.isNotEmpty()) {
                        if (isFront) {
                            videoPositiveLocal = it
                            compare_first.setUp(videoPositiveLocal, true, "")
                        } else {
                            videoNegativeLocal = it
                            compare_first.setUp(videoNegativeLocal, true, "")
                        }
                    } else toast("视频加载失败")
                }
            }
            "第二对比" -> {
                if (event.id == videoFirstId) {
                    toast("已选择过该魔频")
                    return
                }

                videoSecondId = event.id
                videoPositiveCompare = event.name
                videoNegativeCompare = event.checkId
                videoPositiveImgCompare = event.title
                videoNegativeImgCompare = event.memo

                videoPositiveCompareLocal = ""
                videoNegativeCompareLocal = ""

                initVideoSecond(true)
                if (videoFirstId.isNotEmpty()) {
                    initVideoFirst(isEditable)
                    compare_control.visible()
                    compare_progress.visible()
                }

                getDownloadFile(if (isFront) videoPositiveCompare else videoNegativeCompare) {
                    if (it.isNotEmpty()) {
                        if (isFront) {
                            videoPositiveCompareLocal = it
                            compare_second.setUp(videoPositiveCompareLocal, true, "")
                        } else {
                            videoNegativeCompareLocal = it
                            compare_second.setUp(videoNegativeCompareLocal, true, "")
                        }
                    } else toast("视频加载失败")
                }
            }
        }
    }

}
