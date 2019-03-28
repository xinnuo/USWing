package com.meida.uswing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.meida.base.BaseActivity
import com.meida.base.gone
import com.meida.base.oneClick
import com.meida.base.visible
import kotlinx.android.synthetic.main.activity_conversation_web.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.startActivity

class ConversationWebActivity : BaseActivity() {

    private var mPrevUrl = ""
    private var mVideoId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_web)
        init_title("魔频详情", "点评")

        web_view.loadUrl(mPrevUrl)
    }

    override fun init_title() {
        super.init_title()
        mPrevUrl = intent.getStringExtra("url") ?: ""
        mVideoId = intent.getStringExtra("videoId") ?: ""

        web_view.apply {
            settings.apply {
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                domStorageEnabled = true
                defaultTextEncodingName = "utf-8"
            }

            @Suppress("DEPRECATION")
            setVerticalScrollbarOverlay(true)
            webViewClient = object : WebViewClient() {
                @Suppress("OverridingDeprecatedMember")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (mPrevUrl.isNotEmpty()) {
                        return if (mPrevUrl != url) {
                            if (!url.toLowerCase().startsWith("http://")
                                && !url.toLowerCase().startsWith("https://")
                            ) {
                                browse(url)
                                true
                            } else {
                                mPrevUrl = url
                                web_view.loadUrl(url)
                                true
                            }
                        } else false
                    } else {
                        mPrevUrl = url
                        web_view.loadUrl(url)
                        return true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    if (newProgress == 100) web_progressbar.gone()
                    else {
                        web_progressbar.visible()
                        web_progressbar.progress = newProgress
                    }
                    super.onProgressChanged(view, newProgress)
                }

                override fun onCloseWindow(window: WebView) {
                    super.onCloseWindow(window)
                    if (!this@ConversationWebActivity.isFinishing) finish()
                }
            }

            setDownloadListener { url, _, _, _, _ ->
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                val apps = packageManager.queryIntentActivities(intent, 0)
                if (apps.isNotEmpty()) {
                    startActivity(intent)
                    if (uri.scheme == "file"
                        && uri.toString().endsWith(".txt")
                    ) finish()
                }
            }
        }


        tvRight.oneClick {
            if (mVideoId.isNotEmpty()) {
                startActivity<VideoEditActivity>(
                    "magicvoideId" to mVideoId,
                    "hasExtra" to false
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        web_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        web_view.onPause()
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) web_view.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        web_view.destroy()
    }

}
