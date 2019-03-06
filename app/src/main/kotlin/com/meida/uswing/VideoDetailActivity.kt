package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import com.meida.base.visible
import com.meida.utils.DialogHelper.showCompareDialog
import com.meida.utils.DialogHelper.showShareDialog
import kotlinx.android.synthetic.main.activity_video_detail.*
import tv.danmaku.ijk.media.MultiVideoManager
import java.text.DecimalFormat

class VideoDetailActivity : BaseActivity() {

    private var mSpeed = 1f

    val url1 = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/23/266454624066f2b680707492a0664a97.mp4"
    val url2 = "http://jzvd.nathen.cn/63f3f73712544394be981d9e4f56b612/69c5767bb9e54156b5b60a1b6edeb3b5-5287d2089db37e62345123a1be272f8b.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        init_title("我的魔频")
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        ivRight.visible()
        compare_speed.text = "< ${DecimalFormat("0.#").format(mSpeed)}/2 >"

        val video = intent.getStringExtra("video")
        val videoImg = intent.getStringExtra("videoImg")

        compare_first.apply {
            playTag = "compare"
            playPosition = 1
            loadCoverImage(videoImg)
            setUp(url1, true, "")
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
            loadCoverImage(videoImg)
            setUp(url1, true, "")
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
            setLinkedPlayer(compare_first)
            addButton.oneClick {
                showCompareDialog {

                }
            }
        }

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
            R.id.compare_left -> {
                compare_first.setUp(url1, true, "")
                compare_second.setUp(url1, true, "")
                compare_first.startPlayLogic()
                compare_second.startPlayLogic()
            }
            R.id.compare_right -> {
                compare_first.setUp(url2, true, "")
                compare_second.setUp(url2, true, "")
                compare_first.startPlayLogic()
                compare_second.startPlayLogic()
            }
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
