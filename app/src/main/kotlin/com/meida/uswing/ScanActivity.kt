package com.meida.uswing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.meida.base.BaseActivity
import com.meida.utils.setDelegate
import kotlinx.android.synthetic.main.activity_scan.*
import android.os.Vibrator
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.meida.base.getString
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import org.jetbrains.anko.toast

class ScanActivity : BaseActivity() {

    private var isFlashlighting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        init_title("扫一扫")
    }

    override fun init_title() {
        super.init_title()
        scan_zxing.setDelegate {
            onScanQRCodeSuccess {
                vibrate()
                OkLogger.i(it)

                val items = it!!.split("/")
                getVideoData(items.last())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        scan_zxing.startCamera() //打开后置摄像头开始预览，但是并未开始识别
        // scan_zxing.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); //打开前置摄像头开始预览，但是并未开始识别
        scan_zxing.startSpotAndShowRect() //显示扫描框，并开始识别
    }

    override fun onStop() {
        super.onStop()
        scan_zxing.stopCamera() //关闭摄像头预览，并且隐藏扫描框
    }

    private fun getBootData(code: String) {
        OkGo.post<String>(BaseHttp.add_startboot)
            .tag(this@ScanActivity)
            .headers("token", getString("token"))
            .params("item", code)
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    toast(msg)
                    ActivityStack.screenManager.popActivities(this@ScanActivity::class.java)
                }

            })
    }

    private fun getVideoData(code: String) {
        OkGo.post<String>(BaseHttp.add_magicvoide)
            .tag(this@ScanActivity)
            .isMultipart(true)
            .headers("token", getString("token"))
            .params("item", code)
            .params("address", "深圳市")
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    toast(msg)
                    ActivityStack.screenManager.popActivities(this@ScanActivity::class.java)
                }

            })
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.scan_light -> {
                isFlashlighting = if (isFlashlighting) {
                    scan_zxing.closeFlashlight()
                    false
                } else {
                    scan_zxing.openFlashlight()
                    true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        scan_zxing.onDestroy() //销毁二维码扫描控件
    }

}
