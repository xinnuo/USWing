package com.meida.uswing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.meida.base.BaseActivity
import com.meida.utils.setDelegate
import kotlinx.android.synthetic.main.activity_scan.*
import android.os.Vibrator
import android.view.View
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.meida.base.getString
import com.meida.share.BaseHttp
import com.meida.share.Const
import com.meida.utils.ActivityStack
import com.meida.utils.DialogHelper.showPayDialog
import com.meida.utils.isWeb
import com.meida.utils.toNotDouble
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class ScanActivity : BaseActivity() {

    private var isFlashlighting = false
    private var mPrice = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        init_title("扫一扫")

        getData()
    }

    override fun init_title() {
        super.init_title()
        scan_zxing.setDelegate {
            onScanQRCodeSuccess {
                vibrate()
                OkLogger.i(it)

                when {
                    it == null -> resetZxing("扫描失败，请重试！")
                    it.startsWith("token:") -> {
                        val userId = it.replace("token:", "")

                        if (userId == getString("token")) {
                            resetZxing("不能添加自己为好友！")
                        } else {
                            startActivity<CoachAddActivity>("toUserId" to userId)
                            ActivityStack.screenManager.popActivities(this@ScanActivity::class.java)
                        }
                    }
                    it.startsWith("uswing://") -> {
                        val items = it.split("/")
                        getVideoData(items.last())
                    }
                    it.isWeb() && "device=" in it -> {
                        val param = it.substring(it.indexOf("?") + 1)
                        val params = param.split("&")

                        val code = params.first { item -> "device=" in item }
                            .replace("device=", "")

                        if (mPrice.isEmpty()) {
                            resetZxing("开机信息获取失败！")
                        } else {
                            getBanlanceData { banlance ->
                                showPayDialog(
                                    DecimalFormat("0.00").format(mPrice.toNotDouble()),
                                    DecimalFormat("0.00").format(banlance)
                                ) { str ->
                                    if (str == "确定") getBootData(code)
                                    else resetZxing()
                                }
                            }
                        }
                    }
                }
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

    /* 开机价格 */
    override fun getData() {
        OkGo.post<String>(BaseHttp.find_startboot_price)
            .tag(this@ScanActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    mPrice = JSONObject(response.body()).optString("object")
                }

            })
    }

    /* 开机 */
    private fun getBootData(code: String) {
        OkGo.post<String>(BaseHttp.add_startboot)
            .tag(this@ScanActivity)
            .headers("token", getString("token"))
            .params("item", code)
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    toast(msg)
                    ActivityStack.screenManager.popActivities(this@ScanActivity::class.java)
                }

            })
    }

    /* 余额查询 */
    private fun getBanlanceData(listener: (Double) -> Unit) {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@ScanActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    val money = obj.optString("balance", "0").toDouble()
                    listener.invoke(money)
                }

            })
    }

    /* 获取魔频 */
    private fun getVideoData(code: String) {
        OkGo.post<String>(BaseHttp.add_magicvoide)
            .tag(this@ScanActivity)
            .isMultipart(true)
            .headers("token", getString("token"))
            .params("item", code)
            .params("address", "深圳市")
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

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

    @SuppressLint("CheckResult")
    private fun resetZxing(hint: String = "") {
        if (hint.isNotEmpty()) toast(hint)
        Completable.timer(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { scan_zxing.startSpotAndShowRect() }
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
            R.id.scan_img -> {
                PictureSelector.create(this@ScanActivity)
                    .openGallery(PictureMimeType.ofImage())
                    .theme(R.style.picture_white_style)
                    .maxSelectNum(1)
                    .minSelectNum(1)
                    .imageSpanCount(4)
                    .selectionMode(PictureConfig.MULTIPLE)
                    .previewImage(true)
                    .previewVideo(false)
                    .enablePreviewAudio(false)
                    .isCamera(true)
                    .imageFormat(PictureMimeType.PNG)
                    .isZoomAnim(true)
                    .setOutputCameraPath(Const.SAVE_FILE)
                    .compress(true)
                    .glideOverride(160, 160)
                    .enableCrop(false)
                    .compressSavePath(cacheDir.absolutePath)
                    .isGif(false)
                    .openClickSound(false)
                    .previewEggs(true)
                    .minimumCompressSize(100)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择结果回调
                    val selectList = PictureSelector.obtainMultipleResult(data) as ArrayList<LocalMedia>
                    // LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

                    if (!selectList.isNullOrEmpty()) {
                        scan_zxing.decodeQRCode(selectList[0].compressPath)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scan_zxing.onDestroy() //销毁二维码扫描控件
    }

}
