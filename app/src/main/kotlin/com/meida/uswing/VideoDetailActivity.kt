package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import com.meida.base.visible
import com.meida.utils.DialogHelper.showCompareDialog
import com.meida.utils.DialogHelper.showShareDialog

class VideoDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_detail)
        init_title("我的魔频")
    }

    override fun init_title() {
        super.init_title()
        ivRight.visible()

        ivRight.oneClick {
            showShareDialog { }
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.compare_compare -> showCompareDialog { }
        }
    }

}
