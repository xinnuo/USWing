package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.ImageView
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.getBoolean
import com.meida.base.setImageURL
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import org.jetbrains.anko.*
import org.json.JSONObject

/**
 * 不继承BaseActivity，解决打开显示空白的问题
 */
class GuideActivity : AppCompatActivity() {

    private lateinit var ivImg: ImageView
    private var isReady: Boolean = false

    @SuppressLint("HandlerLeak")
    private var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (isReady) quitGuide()
            else isReady = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏状态栏（全屏）
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        frameLayout {
            ivImg = imageView {
                imageResource = R.drawable.guide
                scaleType = ImageView.ScaleType.FIT_XY
            }.lparams(width = matchParent, height = matchParent)
        }

        ActivityStack.screenManager.pushActivity(this@GuideActivity)

        window.decorView.postDelayed({ handler.sendEmptyMessage(0) }, 2000)

        AndPermission.with(this@GuideActivity)
            .runtime()
            .permission(
                Permission.ACCESS_FINE_LOCATION,
                Permission.ACCESS_COARSE_LOCATION,
                Permission.CALL_PHONE,
                Permission.CAMERA,
                Permission.READ_EXTERNAL_STORAGE,
                Permission.WRITE_EXTERNAL_STORAGE
            )
            .onGranted { handler.sendEmptyMessage(0) }
            .onDenied {
                toast("请求权限被拒绝")
                onBackPressed()
            }
            .start()

        getData()
    }

    private fun getData() {
        OkGo.post<String>(BaseHttp.find_startimg)
            .tag(this@GuideActivity)
            .execute(object : StringDialogCallback(this, false) {
                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {
                    val url = JSONObject(response.body()).optString("object")
                    ivImg.setImageURL(BaseHttp.baseImg + url, R.drawable.guide)
                }
            })
    }

    private fun quitGuide() {
        if (getBoolean("isLogin")) startActivity<MainActivity>()
        else startActivity<LoginActivity>()
        ActivityStack.screenManager.popActivities(this@GuideActivity::class.java)
    }

    override fun onDestroy() {
        OkGo.getInstance().cancelTag(this@GuideActivity)
        super.onDestroy()
    }

}
