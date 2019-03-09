package com.meida.uswing

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.base.oneClick
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.utils.trimString
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.activity_video_edit.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class VideoEditActivity : BaseActivity() {

    private val itemIds = ArrayList<String>()
    private val itemNames = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)
        init_title("编辑视频")

        EventBus.getDefault().register(this@VideoEditActivity)
    }

    override fun init_title() {
        super.init_title()
        val title = intent.getStringExtra("title") ?: ""
        val lableIds = intent.getStringExtra("lableIds") ?: ""
        val lableNames = intent.getStringExtra("lableNames") ?: ""
        val memo = intent.getStringExtra("memo") ?: ""

        edit_name.setText(title)
        edit_name.setSelection(edit_name.text.length)
        edit_memo.setText(memo)
        itemIds.addAll(lableIds.split(","))
        itemNames.addAll(lableNames.split(","))
        setTagData()

        edit_add.oneClick {
            startActivity<VideoEditAddActivity>(
                "ids" to itemIds.joinToString(",")
            )
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_save -> {
                when {
                    edit_name.text.isBlank() -> {
                        toast("请输入主题名称")
                        return
                    }
                    itemIds.isEmpty() -> {
                        toast("请选择动作标签")
                        return
                    }
                }

                OkGo.post<String>(BaseHttp.edit_magicvoide)
                    .tag(this@VideoEditActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("magicvoideId", intent.getStringExtra("magicvoideId"))
                    .params("themeTitle", edit_name.text.trimString())
                    .params("labelsId", itemIds.joinToString(","))
                    .params("mome", edit_memo.text.trimString())
                    .execute(object : StringDialogCallback(baseContext) {

                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            EventBus.getDefault().post(
                                RefreshMessageEvent(
                                    "编辑魔频",
                                    intent.getStringExtra("magicvoideId"),
                                    itemNames.joinToString(","),
                                    itemIds.joinToString(","),
                                    edit_name.text.trimString(),
                                    edit_memo.text.trimString()
                                )
                            )
                            ActivityStack.screenManager.popActivities(this@VideoEditActivity::class.java)
                        }

                    })
            }
        }
    }

    private fun setTagData() {
        if (itemNames.isNotEmpty()) {
            edit_mark.adapter = object : TagAdapter<String>(itemNames) {

                override fun getView(parent: FlowLayout, position: Int, item: String): View {
                    val itemView = View.inflate(baseContext, R.layout.item_edit_flow, null)
                    val itemName = itemView.findViewById<TextView>(R.id.item_flow_title)
                    val ivDel = itemView.findViewById<ImageView>(R.id.item_flow_del)

                    itemName.text = item
                    ivDel.onClick {
                        val index = itemNames.indexOf(item)
                        itemIds.removeAt(index)
                        itemNames.removeAt(index)

                        notifyDataChanged()
                    }

                    return itemView
                }

            }
        }
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@VideoEditActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "选择标签" -> {
                itemIds.clear()
                itemNames.clear()

                itemIds.addAll(event.id.split(","))
                itemNames.addAll(event.name.split(","))

                setTagData()
            }
        }
    }

}
