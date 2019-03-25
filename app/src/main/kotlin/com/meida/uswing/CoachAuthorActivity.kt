package com.meida.uswing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.LocationMessageEvent
import com.meida.share.BaseHttp
import com.meida.share.Const
import com.meida.utils.ActivityStack
import com.meida.utils.DialogHelper.showItemDialog
import com.meida.utils.DialogHelper.showSchoolDialog
import com.meida.utils.NameLengthFilter
import com.meida.utils.trimEndString
import com.meida.utils.trimString
import com.meida.view.FullyLinearLayoutManager
import kotlinx.android.synthetic.main.activity_coach_author.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.sdk25.listeners.onCheckedChange
import org.jetbrains.anko.sdk25.listeners.textChangedListener
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.regex.Pattern

class CoachAuthorActivity : BaseActivity() {

    private var selectList = ArrayList<LocalMedia>()
    private var listHonor = ArrayList<CommonData>()
    private var listVideo = ArrayList<CommonData>()

    private lateinit var mGender: String
    private lateinit var mProvince: String
    private lateinit var mCity: String
    private var mSpecial = ""
    private var mAge = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coach_author)
        init_title("认证")

        EventBus.getDefault().register(this@CoachAuthorActivity)
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        mProvince = getString("province")
        mCity = getString("city")

        author_name.textChangedListener {
            onTextChanged { str, _, _, _ ->
                author_name_close.visibility = if (str!!.isEmpty()) View.INVISIBLE else View.VISIBLE
            }
            afterTextChanged { s ->
                pageNum = 0
                (0 until s!!.length).forEach {
                    val matcher = Pattern.compile("[\u4e00-\u9fa5]").matcher(s[it].toString())
                    if (matcher.matches()) pageNum += 2
                    else pageNum++
                }
            }
        }
        author_name.setText(getString("nickName"))
        author_name.setSelection(author_name.text.length)
        author_name.filters = arrayOf<InputFilter>(NameLengthFilter(16))
        author_name_close.setOnClickListener { author_name.setText("") }

        loadUserHead(getString("userHead"))
        author_tel.text = getString("mobile")
        author_gender.text = when (getString("gender")) {
            "0" -> {
                mGender = "0"
                "女"
            }
            else -> {
                mGender = "1"
                "男"
            }
        }

        if (mCity.isNotEmpty()) {
            author_adress.text = "$mProvince $mCity"
        }

        author_honor_list.apply {
            isNestedScrollingEnabled = false
            layoutManager = FullyLinearLayoutManager(baseContext)
            adapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_honor_list) { data, injector ->

                    val index = listHonor.indexOf(data)
                    val isLast = index == listHonor.size - 1

                    injector.text(R.id.item_honor_title, data.honorInfo)
                        .visibility(
                            R.id.item_honor_divider1,
                            if (!isLast) View.VISIBLE else View.GONE
                        )
                        .visibility(
                            R.id.item_honor_divider2,
                            if (isLast) View.VISIBLE else View.GONE
                        )

                        .clicked(R.id.item_honor_del) {
                            listHonor.remove(data)
                            (this.adapter as SlimAdapter).notifyDataSetChanged()
                        }
                }
                .attachTo(this)
                .updateData(listHonor)
        }

        author_video_list.apply {
            isNestedScrollingEnabled = false
            layoutManager = FullyLinearLayoutManager(baseContext)
            adapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_honor_list) { data, injector ->

                    val index = listHonor.indexOf(data)
                    val isLast = index == listHonor.size - 1

                    injector.text(R.id.item_honor_title, data.theme_title)
                        .visibility(
                            R.id.item_honor_divider1,
                            if (!isLast) View.VISIBLE else View.GONE
                        )
                        .visibility(
                            R.id.item_honor_divider2,
                            if (isLast) View.VISIBLE else View.GONE
                        )

                        .clicked(R.id.item_honor_del) {
                            listHonor.remove(data)
                            (this.adapter as SlimAdapter).notifyDataSetChanged()
                        }
                }
                .attachTo(this)
                .updateData(listVideo)
        }

        author_group.onCheckedChange { _, checkedId ->
            when (checkedId) {
                R.id.author_check1 -> mSpecial = "铁杆"
                R.id.author_check2 -> mSpecial = "木杆"
            }
        }

        author_honor.oneClick { startActivity<CoachEditActivity>("title" to "荣誉认证") }
        author_video.oneClick {
            val items = ArrayList<String>()
            listVideo.mapTo(items) { it.magicvoide_id }

            startActivity<CoachVideoActivity>(
                "type" to "添加魔频",
                "videoIds" to items.joinToString(",")
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.author_img_ll -> {
                PictureSelector.create(this@CoachAuthorActivity)
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
                    .enableCrop(true)
                    .withAspectRatio(1, 1)
                    .hideBottomControls(true)
                    .compressSavePath(cacheDir.absolutePath)
                    .freeStyleCropEnabled(false)
                    .circleDimmedLayer(false)
                    .showCropFrame(true)
                    .showCropGrid(true)
                    .isGif(false)
                    .openClickSound(false)
                    .selectionMedia(selectList.apply { clear() })
                    .previewEggs(true)
                    .minimumCompressSize(100)
                    .isDragFrame(false)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            R.id.author_gender_ll -> {
                showItemDialog("男", "女") {
                    when (it) {
                        0 -> {
                            mGender = "1"
                            author_gender.text = "男"
                        }
                        1 -> {
                            mGender = "0"
                            author_gender.text = "女"
                        }
                    }
                }
            }
            R.id.author_adress_ll -> startActivity<NearCityActivity>("type" to "选择地区")
            /*R.id.author_school_ll -> {
                if (listCollege.isEmpty()) {
                    OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_college_list)
                        .tag(this@CoachAuthorActivity)
                        .execute(object :
                            JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(
                                baseContext,
                                true
                            ) {

                            override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                                listCollege.apply {
                                    clear()
                                    addItems(response.body().`object`)
                                }

                                val items = ArrayList<String>()
                                listCollege.mapTo(items) { it.collegeName }

                                showSchoolDialog(
                                    "所属学院",
                                    if (mCollegeId.isEmpty()) 0 else listCollege.indexOfFirst { it.collegeId == mCollegeId },
                                    items
                                ) { index, str ->
                                    mCollegeId = listCollege[index].collegeId
                                    author_school.text = str
                                }
                            }

                        })
                } else {
                    val items = ArrayList<String>()
                    listCollege.mapTo(items) { it.collegeName }

                    showSchoolDialog(
                        "所属学院",
                        if (mCollegeId.isEmpty()) 0 else listCollege.indexOfFirst { it.collegeId == mCollegeId },
                        items
                    ) { index, str ->
                        mCollegeId = listCollege[index].collegeId
                        author_school.text = str
                    }
                }
            }*/
            R.id.author_age_ll -> {
                showSchoolDialog(
                    "选择教龄(年)",
                    mAge,
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                ) { index, str ->
                    mAge = index + 1
                    author_age.text = "${str}年"
                }
            }
            R.id.bt_submit -> {
                when {
                    getString("userHead").isEmpty() -> {
                        toast("请上传个人头像")
                        return
                    }
                    author_name.text.isBlank() -> {
                        toast("请输入姓名")
                        return
                    }
                    mCity.isEmpty() -> {
                        toast("请选择地址")
                        return
                    }
                    author_school.text.isBlank() -> {
                        toast("请输入所属学院")
                        return
                    }
                    mAge == 0 -> {
                        toast("请选择教龄")
                        return
                    }
                    listHonor.isEmpty() -> {
                        toast("请添加荣誉")
                        return
                    }
                    listVideo.isEmpty() -> {
                        toast("请添加魔频")
                        return
                    }
                    mSpecial.isEmpty() -> {
                        toast("请选择特长")
                        return
                    }
                    mSpecial.isEmpty() -> {
                        toast("请选择特长")
                        return
                    }
                    author_content.text.isBlank() -> {
                        toast("请输入个人简介")
                        return
                    }
                    pageNum in 1..3 -> {
                        toast("昵称长度不少于4个字符（一个汉字两个字符）")
                        return
                    }
                    listVideo.size in 1..3 -> {
                        toast("添加魔频数量不少于4个")
                        return
                    }
                    listVideo.size > 8 -> {
                        toast("添加魔频数量不超过8个")
                        return
                    }
                }

                val arr = JSONArray()
                listHonor.forEach { arr.put(JSONObject().apply { put("info", it.honorInfo) }) }

                val items = ArrayList<String>()
                listVideo.mapTo(items) { it.magicvoide_id }

                OkGo.post<String>(BaseHttp.add_certification)
                    .tag(this@CoachAuthorActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("head", getString("userHead"))
                    .params("name", author_name.text.trimString())
                    .params("gender", mGender)
                    .params("address", "$mProvince $mCity")
                    .params("college", author_school.text.trimString())
                    .params("honor", arr.toString())
                    .params("specialty", mSpecial)
                    .params("introduction", author_content.text.trimEndString())
                    .params("magicVoides", items.joinToString(","))
                    .params("teachAge", Calendar.getInstance().get(Calendar.YEAR) - mAge)
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            ActivityStack.screenManager.popActivities(this@CoachAuthorActivity::class.java)
                        }

                    })
            }
        }
    }

    private fun getHeadData() {
        OkGo.post<String>(BaseHttp.userinfo_uploadhead_sub)
            .tag(this@CoachAuthorActivity)
            .headers("token", getString("token"))
            .params("img", File(selectList[0].compressPath))
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    toast(msg)
                    val userhead = JSONObject(response.body()).optString("object")
                    putString("userHead", userhead)
                    loadUserHead(userhead)
                }

            })
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

                    if (selectList[0].isCompressed) getHeadData()
                }
            }
        }
    }

    private fun loadUserHead(path: String) = author_img.loadRectImage(BaseHttp.baseImg + path)

    override fun finish() {
        EventBus.getDefault().unregister(this@CoachAuthorActivity)
        super.finish()
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    fun onMessageEvent(event: LocationMessageEvent) {
        when (event.type) {
            "荣誉认证" -> {
                listHonor.add(CommonData().apply {
                    honorId = event.lng
                    honorInfo = event.lat
                })
                (author_honor_list.adapter as SlimAdapter).notifyDataSetChanged()
            }
            "添加魔频" -> {
                listVideo.add(CommonData().apply {
                    magicvoide_id = event.lat
                    theme_title = event.lng
                })
                (author_video_list.adapter as SlimAdapter).notifyDataSetChanged()
            }
            "选择地区" -> {
                mProvince = event.province
                mCity = event.city
                author_adress.text = "$mProvince $mCity"
            }
        }
    }

}
