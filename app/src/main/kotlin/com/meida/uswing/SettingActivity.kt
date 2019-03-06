package com.meida.uswing

import android.os.Bundle
import cn.jpush.android.api.JPushInterface
import com.luck.picture.lib.tools.PictureFileUtils
import com.meida.base.*
import com.meida.share.Const
import com.meida.utils.DialogHelper.showHintDialog
import com.meida.utils.GlideCacheUtil
import com.meida.utils.Tools
import kotlinx.android.synthetic.main.activity_setting.*
import org.jetbrains.anko.startActivity
import tv.danmaku.ijk.media.MultiVideoManager
import java.io.File

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init_title("设置")
    }

    override fun init_title() {
        super.init_title()
        setting_version.setRightString("v${Tools.getVersion(baseContext)}")
        setting_switch.isChecked = !getBoolean("isTS")

        setting_cache.setRightString(
            GlideCacheUtil.getInstance().getAllCacheSize(baseContext)
        )

        setting_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                JPushInterface.resumePush(applicationContext)
                JPushInterface.setAlias(
                    applicationContext,
                    Const.JPUSH_SEQUENCE,
                    getString("token")
                )
                putBoolean("isTS", false)
            } else {
                JPushInterface.stopPush(applicationContext)
                putBoolean("isTS", true)
            }
        }

        setting_deal.oneClick { startActivity<WebActivity>("title" to "使用协议") }
        setting_private.oneClick { startActivity<WebActivity>("title" to "隐私说明") }
        setting_about.oneClick { startActivity<WebActivity>("title" to "关于我们") }
        setting_feedback.oneClick { startActivity<FeedbackActivity>() }
        setting_cache.oneClick {
            showHintDialog("清空缓存", "确定要清空缓存吗？") { result ->
                if (result == "确定") {
                    GlideCacheUtil.getInstance().clearImageAllCache(baseContext)
                    PictureFileUtils.deleteCacheDirFile(baseContext)
                    MultiVideoManager.instance().values.forEach { it.clearAllDefaultCache(baseContext) }
                    setting_cache.setRightString("0B")
                }
            }
        }
        bt_quit.oneClick {
            showHintDialog("退出登录", "确定要退出当前账号吗？") {
                if (it == "确定") {
                    startActivity<LoginActivity>("offLine" to true)
                }
            }
        }
    }
}
