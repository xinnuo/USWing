package com.meida.uswing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.example.wechatsmallvideoview.SurfaceVideoViewDownloadCreator
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.FileCallback
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import com.lzy.okgo.request.base.Request
import com.meida.base.setImageURL
import com.meida.utils.getScreenWidth
import kotlinx.android.synthetic.main.activity_video_view.*
import java.io.File

class VideoViewActivity : AppCompatActivity() {

    private lateinit var surfaceVideoViewCreator: SurfaceVideoViewDownloadCreator
    private var videoImg = ""
    private var videoPath = ""
    private var imgWidth = ""
    private var imgHeight = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //隐藏状态栏（全屏）
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_video_view)

        videoImg = intent.getStringExtra("img")
        videoPath = intent.getStringExtra("video")
        imgWidth = intent.getStringExtra("width")
        imgHeight = intent.getStringExtra("height")

        init()
    }

    private fun init() {
        surfaceVideoViewCreator =
            object : SurfaceVideoViewDownloadCreator(this, video_container, videoPath) {

                override fun downloadVideo(
                    videoPath: String,
                    fileDir: String,
                    fileName: String
                ) {
                    OkGo.get<File>(videoPath)
                        .tag(this@VideoViewActivity)
                        .execute(object : FileCallback(fileDir, fileName) {

                            override fun onStart(request: Request<File, out Request<Any, Request<*, *>>>) {
                                downloadBefore()
                            }

                            override fun onSuccess(response: Response<File>) {
                                play(response.body().absolutePath)
                            }

                            override fun onError(response: Response<File>) {
                                if (response.body().exists()) response.body().delete()
                            }

                            override fun downloadProgress(progress: Progress) {
                                updateProgress((progress.fraction * 100).toInt())
                            }
                        })
                }

                override fun getActivity() = this@VideoViewActivity

                override fun setAutoPlay(): Boolean = true

                override fun getSurfaceWidth(): Int = 0

                override fun getSurfaceHeight(): Int =
                    getScreenWidth() * imgHeight.toInt() / imgWidth.toInt()

                override fun setThumbImage(thumbImageView: ImageView) {
                    thumbImageView.setImageURL(videoImg)
                }

                override fun getSecondVideoCachePath(): String? = null
            }
    }

    override fun onPause() {
        super.onPause()
        surfaceVideoViewCreator.onPause()
    }

    override fun onResume() {
        super.onResume()
        surfaceVideoViewCreator.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        surfaceVideoViewCreator.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        surfaceVideoViewCreator.onKeyEvent(event)
        /** 声音的大小调节  */
        return super.dispatchKeyEvent(event)
    }

    companion object {

        fun startVieoView(
            context: Context,
            view: View,
            width: String,
            height: String,
            img: String,
            video: String
        ) {

            val intent = Intent(context, VideoViewActivity::class.java)
            intent.putExtra("width", width)
            intent.putExtra("height", height)
            intent.putExtra("img", img)
            intent.putExtra("video", video)

            //android V4包的类,用于两个activity转场时的缩放效果实现
            val optionsCompat = ActivityOptionsCompat.makeScaleUpAnimation(
                view,
                view.width / 2,
                view.height / 2,
                0,
                0
            )
            try {
                ActivityCompat.startActivity(context, intent, optionsCompat.toBundle())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                context.startActivity(intent)
                (context as Activity).overridePendingTransition(
                    R.anim.browser_enter_anim,
                    0
                )
            }

        }
    }

}
