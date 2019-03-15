package com.meida.uswing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.compress.Luban
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.PictureSelectionConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.DateUtils
import com.luck.picture.lib.tools.PictureFileUtils
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.share.BaseHttp
import com.meida.share.Const
import com.meida.utils.ActivityStack
import com.meida.utils.BitmapHelper
import com.meida.utils.BitmapHelper.getVideoThumbnail
import com.meida.utils.DialogHelper.showItemDialog
import com.meida.utils.trimString
import com.meida.view.FullyGridLayoutManager
import com.sunfusheng.GlideImageView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_state_issue.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.toast
import java.io.File
import java.util.*

class StateIssueActivity : BaseActivity() {

    private var selectList = ArrayList<LocalMedia>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state_issue)
        init_title("发布")

        selectList.add(LocalMedia())
        mAdapter.updateData(selectList)
    }

    override fun init_title() {
        super.init_title()

        issue_grid.apply {
            layoutManager = FullyGridLayoutManager(baseContext, 3)

            mAdapter = SlimAdapter.create()
                .register<LocalMedia>(R.layout.item_issue_grid) { data, injector ->

                    val pictureType = PictureMimeType.isPictureType(data.pictureType)

                    injector.text(R.id.item_issue_duration, DateUtils.timeParse(data.duration))
                        .with<GlideImageView>(R.id.item_issue_img) {
                            if (data.mimeType == 0) it.setImageResource(R.mipmap.video_icon19)
                            else it.loadRectImage(data.compressPath)
                        }
                        .visibility(
                            R.id.item_issue_duration,
                            if (pictureType == PictureConfig.TYPE_VIDEO) View.VISIBLE else View.GONE
                        )
                        .visibility(
                            R.id.item_issue_del,
                            if (data.mimeType == 0) View.INVISIBLE else View.VISIBLE
                        )
                        .clicked(R.id.item_issue_img) {
                            when (data.mimeType) {
                                PictureMimeType.ofAll() -> showBottomDialog()
                                PictureMimeType.ofVideo() ->
                                    PictureSelector.create(this@StateIssueActivity)
                                        .externalPictureVideo(data.path)
                                PictureMimeType.ofImage() ->
                                    PictureSelector.create(this@StateIssueActivity)
                                        .themeStyle(R.style.picture_white_style)
                                        .openExternalPreview(
                                            selectList.indexOf(data),
                                            selectList.filter { it.mimeType == PictureMimeType.ofImage() })
                            }
                        }
                        .clicked(R.id.item_issue_del) {
                            val index = selectList.indexOf(data)
                            selectList.remove(data)
                            mAdapter.notifyItemRemoved(index)
                            if (selectList.none { it.mimeType == 0 }) {
                                selectList.add(LocalMedia())
                                mAdapter.notifyItemInserted(selectList.size - 1)
                            }
                        }
                }
                .attachTo(this)
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.issue_done -> {
                if (issue_content.text.isBlank()
                    && selectList.none { it.mimeType != 0 }
                ) {
                    toast("请输入发布文字内容或选择图片和视频")
                    return
                }

                if (selectList.any { it.mimeType == PictureMimeType.ofVideo() }) {
                    OkGo.post<String>(BaseHttp.add_circle_voides)
                        .tag(this@StateIssueActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("circleTitle", issue_content.text.trimString())
                        .params("circleIype", intent.getStringExtra("circleIype"))
                        .params("voides", File(selectList[0].path))
                        .params("voidesimg", File(selectList[0].compressPath))
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                toast(msg)
                                ActivityStack.screenManager.popActivities(this@StateIssueActivity::class.java)
                            }

                        })
                } else {
                    OkGo.post<String>(BaseHttp.add_circle)
                        .tag(this@StateIssueActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("circleTitle", issue_content.text.trimString())
                        .params("circleIype", intent.getStringExtra("circleIype"))
                        .apply {
                            selectList.filter { it.mimeType == PictureMimeType.ofImage() }
                                .forEachWithIndex { index, item ->
                                    params("img$index", File(item.compressPath))
                                }
                        }
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                toast(msg)
                                ActivityStack.screenManager.popActivities(this@StateIssueActivity::class.java)
                            }

                        })
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data) as ArrayList<LocalMedia>
                    // LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

                    if (selectList[0].mimeType == PictureMimeType.ofVideo()) {
                        Flowable.just(selectList[0].path)
                            .observeOn(Schedulers.io())
                            .map {
                                BitmapHelper.saveBitmap(
                                    getVideoThumbnail(it),
                                    PictureFileUtils.createCameraFile(
                                        baseContext,
                                        PictureConfig.TYPE_IMAGE,
                                        PictureSelectionConfig.getInstance().outputCameraPath,
                                        ".jpg"
                                    )
                                )
                            }
                            .map {
                                Luban.with(baseContext)
                                    .setTargetDir(PictureSelectionConfig.getInstance().compressSavePath)
                                    .ignoreBy(PictureSelectionConfig.getInstance().minimumCompressSize)
                                    .load(it)
                                    .get() ?: ArrayList()
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe { showLoadingDialog() }
                            .doFinally { cancelLoadingDialog() }
                            .subscribe {
                                if (it.isNotEmpty()) {
                                    selectList[0].compressPath = it[0].absolutePath
                                    mAdapter.updateData(selectList)
                                }
                            }
                    } else {
                        if (selectList.size < 9) selectList.add(LocalMedia())
                        mAdapter.updateData(selectList)
                    }
                }
            }
        }
    }

    private fun showBottomDialog() {

        showItemDialog("视频", "图片") { index ->
            when (index) {
                0 -> {
                    if (selectList.any { it.mimeType == PictureMimeType.ofImage() }) {
                        toast("不能同时选择图片和视频")
                        return@showItemDialog
                    }

                    PictureSelector.create(this@StateIssueActivity)
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
                1 -> {
                    if (selectList.any { it.mimeType == PictureMimeType.ofVideo() }) {
                        toast("不能同时选择图片和视频")
                        return@showItemDialog
                    }

                    PictureSelector.create(this@StateIssueActivity)
                        .openGallery(PictureMimeType.ofImage())
                        .theme(R.style.picture_white_style)
                        .maxSelectNum(9)
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
                        .selectionMedia(selectList.filter { it.mimeType == PictureMimeType.ofImage() })
                        .previewEggs(true)
                        .minimumCompressSize(100)
                        .forResult(PictureConfig.CHOOSE_REQUEST)
                }
            }
        }

    }

}
