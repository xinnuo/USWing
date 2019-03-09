package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.meida.base.BaseActivity
import com.meida.base.gone
import com.meida.base.oneClick
import com.meida.base.visible
import com.meida.model.RefreshMessageEvent
import com.meida.utils.DialogHelper.showCompareDialog
import com.meida.utils.DialogHelper.showShareDialog
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_compare.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import tv.danmaku.ijk.media.MultiVideoManager
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class CompareActivity : BaseActivity() {

    private var mSpeed = 0.5f
    private var isFront = true
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        EventBus.getDefault().register(this@CompareActivity)

        val title = intent.getStringExtra("title") ?: "魔镜对比"
        init_title(title)
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        videoFirstId = intent.getStringExtra("magicvoideId") ?: ""
        videoPositive = intent.getStringExtra("video1") ?: ""
        videoNegative = intent.getStringExtra("video2") ?: ""
        videoPositiveImg = intent.getStringExtra("videoImg1") ?: ""
        videoNegativeImg = intent.getStringExtra("videoImg2") ?: ""

        val isShare = intent.getBooleanExtra("share", false)
        if (isShare) ivRight.visible()

        compare_speed.text = "< ${DecimalFormat("0.#").format(mSpeed)}/2 >"
        compare_first.setSpeedPlaying(mSpeed, true)
        compare_second.setSpeedPlaying(mSpeed, true)

        if (videoPositive.isNotEmpty()) initVideoFirst()

        ivRight.oneClick {
            showShareDialog { }
        }
    }

    private fun initVideoFirst(isAdd: Boolean = false) {
        compare_first.visible()
        compare_add1.gone()
        compare_control.visible()

        compare_first.apply {
            playTag = "compare"
            playPosition = 1
            loadCoverImage(
                if (isFront) {
                    if (videoPositiveImg.isEmpty()) videoPositive else videoPositiveImg
                } else {
                    if (videoNegativeImg.isEmpty()) videoNegative else videoNegativeImg
                }
            )
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

    private fun initVideoSecond() {
        compare_second.visible()
        compare_add2.gone()
        compare_control.visible()

        compare_second.apply {
            playTag = "compare"
            playPosition = 2
            loadCoverImage(
                if (isFront) {
                    if (videoPositiveImgCompare.isEmpty()) videoPositiveCompare else videoPositiveImgCompare
                } else {
                    if (videoNegativeImgCompare.isEmpty()) videoNegativeCompare else videoNegativeImgCompare
                }
            )
            setUp(
                if (isFront) videoPositiveCompare else videoNegativeCompare,
                true,
                ""
            )
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
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

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.compare_speed -> {
                when (mSpeed) {
                    0.5f -> {
                        mSpeed = 1f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                    }
                    1f -> {
                        mSpeed = 1.5f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                    }
                    1.5f -> {
                        mSpeed = 2f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                    }
                    2f -> {
                        mSpeed = 0.5f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                    }
                }
                compare_speed.text = "< ${DecimalFormat("0.#").format(mSpeed)}/2 >"
            }
            R.id.compare_left -> {
                isFront = true
                switchVideoSource()
            }
            R.id.compare_right -> {
                isFront = false
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
            compare_first.setUp(
                if (isFront) videoPositive else videoNegative,
                true,
                ""
            )
            compare_first.loadCoverImage(
                if (isFront) {
                    if (videoPositiveImg.isEmpty()) videoPositive else videoPositiveImg
                } else {
                    if (videoNegativeImg.isEmpty()) videoNegative else videoNegativeImg
                }
            )
        }
    }

    private fun switchVideoSecond() {
        if (videoPositiveCompare.isNotEmpty()
            && videoNegativeCompare.isNotEmpty()
        ) {
            compare_second.setUp(
                if (isFront) videoPositiveCompare else videoNegativeCompare,
                true,
                ""
            )
            compare_second.loadCoverImage(
                if (isFront) {
                    if (videoPositiveImgCompare.isEmpty()) videoPositiveCompare else videoPositiveImgCompare
                } else {
                    if (videoNegativeImgCompare.isEmpty()) videoNegativeCompare else videoNegativeImgCompare
                }
            )
        }
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
                videoFirstId = event.id
                videoPositive = event.name
                videoNegative = event.checkId
                videoPositiveImg = event.title
                videoNegativeImg = event.memo

                initVideoFirst(true)
                if (videoSecondId.isNotEmpty()) {
                    switchVideoSecond()
                    compare_first.setLinkedPlayer(compare_second)
                    compare_second.setLinkedPlayer(compare_first)
                }
            }
            "第二对比" -> {
                videoSecondId = event.id
                videoPositiveCompare = event.name
                videoNegativeCompare = event.checkId
                videoPositiveImgCompare = event.title
                videoNegativeImgCompare = event.memo

                initVideoSecond()
                if (videoFirstId.isNotEmpty()) {
                    switchVideoFirst()
                    compare_first.setLinkedPlayer(compare_second)
                    compare_second.setLinkedPlayer(compare_first)
                }
            }
        }
    }

}
