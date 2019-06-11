package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.addItems
import com.meida.base.getString
import com.meida.base.oneClick
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_video_edit_add.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast

class VideoEditAddActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit_add)
        init_title("动作标签", "保存")

        getData()
    }

    override fun init_title() {
        super.init_title()

        add_mark.setOnTagClickListener { _, position, _ ->
            list[position].isChecked = !list[position].isChecked
            add_mark.adapter.notifyDataChanged()
            return@setOnTagClickListener true
        }

        tvRight.oneClick {
            if (list.none { it.isChecked }) {
                toast("请选择要添加的标签")
                return@oneClick
            }

            val itemIds = ArrayList<String>()
            val itemNames = ArrayList<String>()
            list.filter { it.isChecked }.mapTo(itemIds) { it.labelsId }
            list.filter { it.isChecked }.mapTo(itemNames) { it.labelsName }

            EventBus.getDefault().post(
                RefreshMessageEvent(
                    "选择标签",
                    itemIds.joinToString(","),
                    itemNames.joinToString(",")
                )
            )
            ActivityStack.screenManager.popActivities(this@VideoEditAddActivity::class.java)
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.labels_all)
            .tag(this@VideoEditAddActivity)
            .headers("token", getString("token"))
            .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.addItems(response.body().data)

                    val ids = intent.getStringExtra("ids")
                    if (ids.isNotEmpty()) {
                        val items = ids.split(",")
                        list.forEach { if (it.labelsId in items) it.isChecked = true }
                    }

                    add_mark.adapter = object : TagAdapter<CommonData>(list) {

                        override fun getView(parent: FlowLayout, position: Int, item: CommonData): View {
                            val itemView = View.inflate(baseContext, R.layout.item_edit_flow, null)
                            val itemName = itemView.findViewById<TextView>(R.id.item_flow_title)
                            val ivDel = itemView.findViewById<ImageView>(R.id.item_flow_del)

                            itemName.text = item.labelsName
                            ivDel.setImageResource(R.mipmap.video_icon17)
                            ivDel.visibility = if (item.isChecked) View.VISIBLE else View.INVISIBLE

                            return itemView
                        }

                    }

                }

            })
    }

}
