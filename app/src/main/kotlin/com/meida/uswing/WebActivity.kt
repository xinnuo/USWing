package com.meida.uswing

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.meida.base.BaseActivity
import com.meida.base.cancelLoadingDialog
import com.meida.base.showLoadingDialog
import com.meida.utils.isWeb
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.browse
import org.jetbrains.anko.webView

class WebActivity : BaseActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = webView {
            overScrollMode = View.OVER_SCROLL_NEVER
            backgroundColorResource = R.color.transparent

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

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {

                /* 这个事件，将在用户点击链接时触发。
                 * 通过判断url，可确定如何操作，
                 * 如果返回true，表示我们已经处理了这个request，
                 * 如果返回false，表示没有处理，那么浏览器将会根据url获取网页
                 */
                @Suppress("DEPRECATION", "OverridingDeprecatedMember")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {


                    if (!url.isWeb()) return true

                    if (url.isNotEmpty() && url.endsWith("apk")) browse(url)
                    else {
                        view.loadUrl(url)
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, url)
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

        init_title(intent.getStringExtra("title"))
    }
}
