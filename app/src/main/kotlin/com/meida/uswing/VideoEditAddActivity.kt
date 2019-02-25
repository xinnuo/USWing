package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.meida.base.BaseActivity
import com.meida.model.CommonData
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_video_edit_add.*

class VideoEditAddActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit_add)
        init_title("动作标签", "保存")
    }

    override fun init_title() {
        super.init_title()

        val items = ArrayList<CommonData>()
        items.add(CommonData("木杆"))
        items.add(CommonData("前伸"))
        items.add(CommonData("太贴近身体"))
        items.add(CommonData("偏左"))

        add_mark.adapter = object : TagAdapter<CommonData>(items) {

            override fun getView(parent: FlowLayout, position: Int, item: CommonData): View {
                val itemView = View.inflate(baseContext, R.layout.item_edit_flow, null)
                val itemName = itemView.findViewById<TextView>(R.id.item_flow_title)
                val ivDel = itemView.findViewById<ImageView>(R.id.item_flow_del)

                itemName.text = item.nick_name
                ivDel.setImageResource(R.mipmap.video_icon17)
                ivDel.visibility = if (item.isChecked) View.VISIBLE else View.INVISIBLE

                return itemView
            }

        }
        add_mark.setOnTagClickListener { view, position, parent ->
            items[position].isChecked = !items[position].isChecked
            add_mark.adapter.notifyDataChanged()
            return@setOnTagClickListener true
        }
    }
}
