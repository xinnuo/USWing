package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.meida.base.BaseActivity
import com.meida.base.load_Linear
import com.meida.base.refresh
import com.meida.model.CommonData
import com.meida.utils.DialogHelper.showHintDialog
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.startActivity
import java.util.ArrayList

class VideoActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        setToolbarVisibility(false)
        init_title()

        list.add(CommonData("1"))
        list.add(CommonData("2"))
        list.add(CommonData("3"))
        list.add(CommonData("4"))

        mAdapter.updateData(list)
    }

    override fun init_title() {
        swipe_refresh.refresh { }
        recycle_list.load_Linear(baseContext, swipe_refresh)

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_video_list) { data, injector ->

                val index = list.indexOf(data)

                injector.visibility(
                    R.id.item_video_divider,
                    if (index == 0) View.VISIBLE else View.GONE
                )
                    .clicked(R.id.item_video_del) {
                        showHintDialog("提示", "您确定要删除视频吗？") {

                        }
                    }
                    .clicked(R.id.item_video_edit) { startActivity<VideoEditActivity>() }
                    .clicked(R.id.item_video) { startActivity<VideoDetailActivity>() }
            }
            .attachTo(recycle_list)
    }

}
