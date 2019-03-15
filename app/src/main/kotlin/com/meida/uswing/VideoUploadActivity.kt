package com.meida.uswing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.amap.api.AMapLocationHelper
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.base.gone
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.share.Const
import com.meida.utils.ActivityStack
import com.meida.utils.FileSizeHelper
import kotlinx.android.synthetic.main.activity_video_upload.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import java.io.File
import java.util.ArrayList

class VideoUploadActivity : BaseActivity() {

    private var mAddress = ""
    private var mVideoFirst = ""
    private var mVideoSecond = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_upload)
        init_title("上传魔频", "保存")

        getLocationData()
    }

    /* 位置 */
    private fun getLocationData() {
        AMapLocationHelper.getInstance(baseContext)
            .startLocation(400) { location, isSuccessed, codes ->
                if (400 in codes) {
                    if (isSuccessed) mAddress = location.address
                    else {
                        val errorInfo = location?.locationDetail ?: "位置信息获取失败"
                        toast(errorInfo.split("#")[0])
                    }
                }
            }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.upload_img1, R.id.upload_select1 -> {
                mPosition = 1

                PictureSelector.create(this@VideoUploadActivity)
                    .openGallery(PictureMimeType.ofVideo())
                    .theme(R.style.picture_white_style)
                    .maxSelectNum(1)
                    .minSelectNum(1)
                    .imageSpanCount(4)
                    .selectionMode(PictureConfig.MULTIPLE)
                    .previewImage(true)
                    .previewVideo(true)
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
                    .selectionMedia(null)
                    .previewEggs(true)
                    .minimumCompressSize(100)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            R.id.upload_img2, R.id.upload_select2 -> {
                mPosition = 2

                PictureSelector.create(this@VideoUploadActivity)
                    .openGallery(PictureMimeType.ofVideo())
                    .theme(R.style.picture_white_style)
                    .maxSelectNum(1)
                    .minSelectNum(1)
                    .imageSpanCount(4)
                    .selectionMode(PictureConfig.MULTIPLE)
                    .previewImage(true)
                    .previewVideo(true)
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
                    .selectionMedia(null)
                    .previewEggs(true)
                    .minimumCompressSize(100)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            R.id.tv_nav_right -> {
                if (mVideoFirst.isEmpty() || mVideoSecond.isEmpty()) {
                    toast("请选择要上传的视频文件")
                    return
                }

                OkGo.post<String>(BaseHttp.add_magicvoide_coach)
                    .tag(this@VideoUploadActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("address", mAddress)
                    .params("video1", File(mVideoFirst))
                    .params("video2", File(mVideoSecond))
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            EventBus.getDefault().post(RefreshMessageEvent("上传魔频"))
                            ActivityStack.screenManager.popActivities(this@VideoUploadActivity::class.java)
                        }

                    })
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
                        when (mPosition) {
                            1 -> {
                                val itemSize = FileSizeHelper.getFileSize(File(selectList[0].path))
                                if (itemSize > 5 * 1024 * 1024) {
                                    toast("上传视频最大不能超过5M")
                                    return
                                }

                                upload_add1.gone()
                                mVideoFirst = selectList[0].path
                                upload_img1.load(mVideoFirst)
                                upload_img1.setBackgroundResource(R.color.white)
                            }
                            2 -> {
                                val itemSize = FileSizeHelper.getFileSize(File(selectList[0].path))
                                if (itemSize > 5 * 1024 * 1024) {
                                    toast("上传视频最大不能超过5M")
                                    return
                                }

                                upload_add2.gone()
                                mVideoSecond = selectList[0].path
                                upload_img2.load(mVideoSecond)
                                upload_img2.setBackgroundResource(R.color.white)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        AMapLocationHelper.getInstance(baseContext).removeCode(400)
    }

}
