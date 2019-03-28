package com.meida.uswing

import android.os.Bundle
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import kotlinx.android.synthetic.main.activity_conversation_video.*
import org.jetbrains.anko.startActivity
import tv.danmaku.ijk.media.MultiVideoManager

class ConversationVideoActivity : BaseActivity() {

    private var mPrevUrl = ""
    private var mVideoId = ""
    private var mVideoImg = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_video)
        init_title("魔频详情", "点评")

        msg_video.startToPlay()
    }

    override fun init_title() {
        super.init_title()
        mPrevUrl = intent.getStringExtra("url") ?: ""
        mVideoId = intent.getStringExtra("videoId") ?: ""
        mVideoImg = intent.getStringExtra("videoImg") ?: ""

        msg_video.apply {
            playTag = "message"
            playPosition = 1
            loadCoverImage(mVideoImg)
            setUp(
                mPrevUrl,
                true,
                ""
            )
            isReleaseWhenLossAudio = false
            setIsTouchWiget(false)
            setGone(true)
        }

        tvRight.oneClick {
            if (mVideoId.isNotEmpty()) {
                startActivity<VideoEditActivity>(
                    "magicvoideId" to mVideoId,
                    "hasExtra" to false
                )
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
