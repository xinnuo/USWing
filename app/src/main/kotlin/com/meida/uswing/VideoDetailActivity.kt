package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import com.meida.base.setImageURL
import com.meida.base.visible
import com.meida.utils.DialogHelper.showCompareDialog
import com.meida.utils.DialogHelper.showShareDialog
import kotlinx.android.synthetic.main.activity_video_detail.*
import org.salient.artplayer.MediaPlayerManager
import org.salient.artplayer.ui.ControlPanel

class VideoDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        init_title("我的魔频")
    }

    override fun init_title() {
        super.init_title()
        ivRight.visible()

        MediaPlayerManager.instance().unbindOrientationManager()
        compare_video.controlPanel = ControlPanel(baseContext).apply {
            findViewById<ImageView>(R.id.video_cover)
                .setImageURL(
                    intent.getStringExtra("videoImg"),
                    R.mipmap.default_img
                )
        }
        compare_video.setUp(intent.getStringExtra("video"))
        compare_video.start()

        ivRight.oneClick {
            showShareDialog { }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.compare_compare -> showCompareDialog { }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerManager.instance().releasePlayerAndView(baseContext)
    }

}
