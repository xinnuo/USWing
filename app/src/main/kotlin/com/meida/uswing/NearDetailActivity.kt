package com.meida.uswing

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.share.BaseHttp
import com.meida.utils.toTextDouble
import kotlinx.android.synthetic.main.activity_near_detail.*
import org.json.JSONObject
import java.text.DecimalFormat
import android.annotation.TargetApi
import android.view.View
import android.view.ViewTreeObserver


class NearDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_near_detail)
        init_title("试练场详情")

        getData()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition()
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.find_court_detils)
            .tag(this@NearDetailActivity)
            .params("courtId", intent.getStringExtra("courtId"))
            .params("lat", intent.getStringExtra("lat"))
            .params("lng", intent.getStringExtra("lng"))
            .execute(object : StringDialogCallback(baseContext) {

                @SuppressLint("SetTextI18n")
                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    near_name.text = obj.optString("court_name")
                    near_adress.text = "地址：${obj.optString("court_adress")}"
                    near_tel.text = "电话：${obj.optString("court_tel")}"

                    val length = obj.optString("distance").toTextDouble()

                    when {
                        length == 0.0 -> near_length.text = "0m"
                        length < 1000 -> near_length.text =
                            "${DecimalFormat("0.00").format(length)}m"
                        else -> near_length.text = "${DecimalFormat("0.00").format(length / 1000)}m"
                    }

                    Glide.with(baseContext)
                        .load(BaseHttp.baseImg + obj.optString("court_img"))
                        .apply(
                            RequestOptions
                                .centerCropTransform()
                                .placeholder(R.mipmap.default_img)
                                .error(R.mipmap.default_img)
                                .dontAnimate()
                        )
                        .listener(object : RequestListener<Drawable> {

                            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                scheduleStartPostponedTransition(near_img)
                                return true
                            }

                            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                near_img.setImageDrawable(resource)
                                scheduleStartPostponedTransition(near_img)
                                return true
                            }

                        })
                        .into(near_img)

                    near_info.load(BaseHttp.baseImg + obj.optString("court_info"))
                }

            })
    }

    private fun scheduleStartPostponedTransition(sharedElement: View) {
        sharedElement.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onPreDraw(): Boolean {
                    sharedElement.viewTreeObserver.removeOnPreDrawListener(this)
                    startPostponedEnterTransition()
                    return true
                }

            })
    }

}
