package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.meida.base.BaseActivity
import com.meida.base.gone
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_video_edit_add.*

class VideoLabelActivity : BaseActivity() {

    private val list = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit_add)
        init_title("动作标签")
    }

    override fun init_title() {
        super.init_title()
        val labels = intent.getStringExtra("label")
        list.addAll(labels.split(","))

        add_mark.adapter = object : TagAdapter<String>(list) {

            override fun getView(parent: FlowLayout, position: Int, item: String): View {
                val itemView = View.inflate(baseContext, R.layout.item_edit_flow, null)
                val itemName = itemView.findViewById<TextView>(R.id.item_flow_title)
                val ivDel = itemView.findViewById<ImageView>(R.id.item_flow_del)

                itemName.text = item
                ivDel.gone()

                return itemView
            }

        }
    }

}
