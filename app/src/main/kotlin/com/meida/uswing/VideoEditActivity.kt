package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.meida.base.BaseActivity
import com.meida.base.oneClick
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_video_edit.*
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.startActivity

class VideoEditActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)
        init_title("编辑视频")
    }

    override fun init_title() {
        super.init_title()

        val items = arrayListOf("木杆", "前伸", "铁杆", "太贴近身体", "站姿过直")
        edit_mark.adapter = object : TagAdapter<String>(items) {

            override fun getView(parent: FlowLayout, position: Int, item: String): View {
                val itemView = View.inflate(baseContext, R.layout.item_edit_flow, null)
                val itemName = itemView.findViewById<TextView>(R.id.item_flow_title)
                val ivDel = itemView.findViewById<ImageView>(R.id.item_flow_del)

                itemName.text = item
                ivDel.onClick {
                    items.remove(item)
                    notifyDataChanged()
                }

                return itemView
            }

        }

        edit_add.oneClick { startActivity<VideoEditAddActivity>() }
    }
}
