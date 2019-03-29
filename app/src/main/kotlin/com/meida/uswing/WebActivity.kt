package com.meida.uswing

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.share.BaseHttp
import com.meida.view.webViewX5
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.browse
import org.jetbrains.anko.frameLayout
import org.json.JSONObject

class WebActivity : BaseActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        frameLayout {
            webView = webViewX5 {
                overScrollMode = View.OVER_SCROLL_NEVER
                backgroundColorResource = R.color.transparent

                //支持javascript
                @Suppress("DEPRECATION")
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

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {

                    /* 这个事件，将在用户点击链接时触发。
                     * 通过判断url，可确定如何操作，
                     * 如果返回true，表示我们已经处理了这个request，
                     * 如果返回false，表示没有处理，那么浏览器将会根据url获取网页
                     */
                    @Suppress("DEPRECATION", "OverridingDeprecatedMember")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return when {
                            "tel:" in url -> makeCall(url.replace("tel:", ""))
                            !url.startsWith("https://")
                                    && !url.startsWith("http://") -> {
                                if (url.startsWith("weixin://wap/pay?")) browse(url)
                                true
                            }
                            url.endsWith(".apk") -> browse(url)
                            else -> {
                                view.loadUrl(url)
                                true
                            }
                        }
                    }

                    /*
                     * 在开始加载网页时会回调
                     */
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        showLoadingDialog()
                    }

                    /*
                     * 在结束加载网页时会回调
                     */
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        cancelLoadingDialog()
                    }

                }
            }
        }

        init_title(intent.getStringExtra("title"))

        when (intent.getStringExtra("title")) {
            "积分规则" -> {
                OkGo.post<String>(BaseHttp.find_html_info)
                    .tag(this@WebActivity)
                    .params("htmlKey", "gfgz")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            val obj = JSONObject(response.body())
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
                                    obj.optString("object") +
                                    "</div>" +
                                    "</body>" +
                                    "</html>"

                            webView.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                        }

                    })
            }
            "注册协议", "使用协议" -> {
                OkGo.post<String>(BaseHttp.find_html_info)
                    .tag(this@WebActivity)
                    .params("htmlKey", "syxy")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            val obj = JSONObject(response.body())
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
                                    obj.optString("object") +
                                    "</div>" +
                                    "</body>" +
                                    "</html>"

                            webView.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                        }

                    })
            }
            "隐私说明" -> {
                OkGo.post<String>(BaseHttp.find_html_info)
                    .tag(this@WebActivity)
                    .params("htmlKey", "yssm")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            val obj = JSONObject(response.body())
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
                                    obj.optString("object") +
                                    "</div>" +
                                    "</body>" +
                                    "</html>"

                            webView.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                        }

                    })
            }
            "关于我们" -> {
                OkGo.post<String>(BaseHttp.find_html_info)
                    .tag(this@WebActivity)
                    .params("htmlKey", "gywm")
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            val obj = JSONObject(response.body())
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
                                    obj.optString("object") +
                                    "</div>" +
                                    "</body>" +
                                    "</html>"

                            webView.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                        }

                    })
            }
            "消息详情" -> {
                OkGo.post<String>(BaseHttp.find_msg_details)
                    .tag(this@WebActivity)
                    .headers("token", getString("token"))
                    .params("msgReceiveId", intent.getStringExtra("msgReceiveId"))
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            val obj = JSONObject(response.body())
                                .optJSONObject("object") ?: JSONObject()

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
                                    obj.optString("content") +
                                    "</div>" +
                                    "</body>" +
                                    "</html>"

                            webView.loadDataWithBaseURL(BaseHttp.baseImg, str, "text/html", "utf-8", "")
                        }

                    })
            }
            "详情" -> {
                tvTitle.text = intent.getStringExtra("hint")
                val urlLoad = intent.getStringExtra("url")
                webView.loadUrl(if (urlLoad.startsWith("http")) urlLoad else BaseHttp.baseImg + urlLoad)
                // webView.loadUrl("http://nativeapp.globalwinson.com:8092/slider/quanpain.mp4")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }

}
