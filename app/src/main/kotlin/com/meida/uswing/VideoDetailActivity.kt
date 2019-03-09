package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import com.meida.base.visible
import com.meida.utils.DialogHelper.showCompareDialog
import com.meida.utils.DialogHelper.showShareDialog
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_video_detail.*
import tv.danmaku.ijk.media.MultiVideoManager
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class VideoDetailActivity : BaseActivity() {

    private var mSpeed = 1f
    private var video1 = ""
    private var video2 = ""
    private var videoImg1 = ""
    private var videoImg2 = ""

    val url1 = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/23/266454624066f2b680707492a0664a97.mp4"
    val url2 = "http://jzvd.nathen.cn/35b3dc97fbc240219961bd1fccc6400b/8d9b76ab5a584bce84a8afce012b72d3-5287d2089db37e62345123a1be272f8b.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        init_title("我的魔频")

        video1 = intent.getStringExtra("video1")
        video2 = intent.getStringExtra("video2")
        videoImg1 = intent.getStringExtra("videoImg1")
        videoImg2 = intent.getStringExtra("videoImg2")

        compare_first.apply {
            playTag = "compare"
            playPosition = 1
            loadCoverImage(videoImg1)
            setUp(video1, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
            setLinkedPlayer(compare_second)
            addButton.oneClick {
                showCompareDialog {

                }
            }
        }

        compare_second.apply {
            playTag = "compare"
            playPosition = 2
            loadCoverImage(videoImg1)
            setUp(video1, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
            setLinkedPlayer(compare_first)
            addButton.oneClick {
                showCompareDialog {

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        ivRight.visible()
        compare_speed.text = "< ${DecimalFormat("0.#").format(mSpeed)}/2 >"

        ivRight.oneClick {
            showShareDialog { }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.compare_speed -> {
                when (mSpeed) {
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
                    0.5f -> {
                        mSpeed = 1f
                        compare_first.setSpeedPlaying(mSpeed, true)
                        compare_second.setSpeedPlaying(mSpeed, true)
                    }
                }
                compare_speed.text = "< ${DecimalFormat("0.#").format(mSpeed)}/2 >"
            }
            R.id.compare_left -> { switchVideoSource(video1, videoImg1) }
            R.id.compare_right -> { switchVideoSource(video2, videoImg2) }
        }
    }

    @SuppressLint("CheckResult")
    private fun switchVideoSource(url: String, img: String) {
        MultiVideoManager.onPauseAll()

        Completable.timer(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                compare_first.setUp(url, true, "")
                compare_first.loadCoverImage(img)

                compare_second.setUp(url, true, "")
                compare_second.loadCoverImage(img)
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

}
