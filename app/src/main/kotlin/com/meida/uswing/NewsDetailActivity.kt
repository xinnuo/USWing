package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.webkit.WebSettings
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import kotlinx.android.synthetic.main.activity_news_detail.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.sdk25.listeners.onTouch
import org.jetbrains.anko.toast
import org.json.JSONObject

class NewsDetailActivity : BaseActivity() {

    private var mCollect = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        init_title("详情")

        getData()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun init_title() {
        super.init_title()
        news_web.apply {
            //支持javascript
            settings.javaScriptEnabled = true
            //设置可以支持缩放
            settings.setSupportZoom(true)
            //自适应屏幕
            settings.loadWithOverviewMode = true
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            isHorizontalScrollBarEnabled = false

            //设置出现缩放工具
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            //设置是否使用缓存
            settings.setAppCacheEnabled(true)
            settings.domStorageEnabled = true
        }

        news_collect.onTouch { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    when (mCollect) {
                        "0" -> OkGo.post<String>(BaseHttp.add_collection)
                            .tag(this@NewsDetailActivity)
                            .headers("token", getString("token"))
                            .params("bussId", intent.getStringExtra("newsId"))
                            .params("collectionType", "1")
                            .execute(object : StringDialogCallback(baseContext) {

                                @SuppressLint("SetTextI18n")
                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    toast(msg)
                                    mCollect = "1"
                                    news_collect.text = "取消收藏"
                                    news_collect.isChecked = true
                                    EventBus.getDefault().post(RefreshMessageEvent("添加收藏"))
                                }

                            })
                        "1" -> OkGo.post<String>(BaseHttp.delete_collection)
                            .tag(this@NewsDetailActivity)
                            .headers("token", getString("token"))
                            .params("bussId", intent.getStringExtra("newsId"))
                            .params("collectionType", "1")
                            .execute(object : StringDialogCallback(baseContext) {

                                @SuppressLint("SetTextI18n")
                                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                    toast(msg)
                                    mCollect = "0"
                                    news_collect.text = "收藏"
                                    news_collect.isChecked = false
                                    EventBus.getDefault().post(RefreshMessageEvent("取消收藏"))
                                }

                            })
                    }
                }
            }

            return@onTouch true
        }
    }

    override fun getData() {
        OkGo.post<String>(BaseHttp.find_news_details)
            .tag(this@NewsDetailActivity)
            .params("newsId", intent.getStringExtra("newsId"))
            .params("userInfoId", getString("token"))
            .execute(object : StringDialogCallback(baseContext) {

                @SuppressLint("SetTextI18n")
                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    news_title.text = obj.optString("news_introduction")
                    news_time.text = obj.optString("create_date")

                    mCollect = obj.optString("collection")
                    if (mCollect == "1") {
                        news_collect.text = "取消收藏"
                        news_collect.isChecked = true
                    } else {
                        news_collect.text = "收藏"
                        news_collect.isChecked = false
                    }

                    val str = "<!doctype html><html>\n" +
                            "<meta charset=\"utf-8\">" +
                            "<style type=\"text/css\">" +
                            "body{ padding:0; margin:0; }\n" +
                            ".con{ width:95%; margin:0 auto; color:#666; padding:0.5em 0; overflow:hidden; display:block; font-size:0.92em; line-height:1.8em; }\n" +
                            ".con h1,h2,h3,h4,h5,h6{ font-size:1em; }\n " +
                            "img{ width:auto; max-width: 100% !important; height:auto !important; margin:0 auto; display:block; }\n" +
                            "*{ max-width:100% !important; }\n" +
                            "</style>\n" +
                            "<body style=\"padding:0; margin:0; \">" +
                            "<div class=\"con\">" +
                            obj.optString("news_details") +
                            "</div>" +
                            "</body>" +
                            "</html>"

                    news_web.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                }

            })
    }

    override fun onResume() {
        super.onResume()
        news_web.onResume()
    }

    override fun onPause() {
        super.onPause()
        news_web.onPause()
    }

    override fun onBackPressed() {
        if (news_web.canGoBack()) news_web.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        news_web.destroy()
    }

}
