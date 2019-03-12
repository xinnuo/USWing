package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.FriendData
import com.meida.share.BaseHttp
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.include
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.ArrayList

class MessageActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("系统消息")

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关系统信息！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_msg_list1) { data, injector ->

                val isLast = list.indexOf(data) == list.size - 1

                injector.text(R.id.item_msg_title, data.title)
                    .text(R.id.item_msg_time, data.send_date)
                    .visibility(
                        R.id.item_msg_dot,
                        if (data.astatus == "0") View.VISIBLE else View.GONE
                    )
                    .visibility(R.id.item_msg_divider1, if (isLast) View.GONE else View.VISIBLE)
                    .visibility(R.id.item_msg_divider2, if (!isLast) View.GONE else View.VISIBLE)

                    .clicked(R.id.item_msg) {
                        startActivity<WebActivity>(
                            "title" to "详情",
                            "msgReceiveId" to data.msg_receive_id
                        )
                    }
            }
            .register<FriendData>(R.layout.item_msg_list2) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_msg_title, data.title)
                    .text(R.id.item_msg_time, data.send_date)
                    .text(R.id.item_msg_memo, "备注：${data.mome}")
                    .text(
                        R.id.item_msg_status, when (data.astatus) {
                            "-1" -> "已忽略"
                            "1" -> "已同意"
                            else -> ""
                        }
                    )

                    .with<GlideImageView>(R.id.item_msg_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }

                    .visibility(
                        R.id.item_msg_status,
                        if (data.astatus != "0") View.VISIBLE else View.GONE
                    )
                    .visibility(
                        R.id.item_msg_agree,
                        if (data.astatus == "0") View.VISIBLE else View.GONE
                    )
                    .visibility(
                        R.id.item_msg_ignore,
                        if (data.astatus == "0") View.VISIBLE else View.GONE
                    )
                    .visibility(R.id.item_msg_divider1, if (isLast) View.GONE else View.VISIBLE)
                    .visibility(R.id.item_msg_divider2, if (!isLast) View.GONE else View.VISIBLE)

                    .clicked(R.id.item_msg_agree) {
                        /* 同意 */
                        OkGo.post<String>(BaseHttp.update_application)
                            .tag(this@MessageActivity)
                            .headers("token", getString("token"))
                            .params("applicationId", data.msg_receive_id)
                            .params("status", "1")
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {

                                    toast(msg)
                                    data.astatus = "1"
                                    mAdapter.notifyItemChanged(index)
                                }

                            })
                    }

                    .clicked(R.id.item_msg_ignore) {
                        /* 忽略 */
                        OkGo.post<String>(BaseHttp.update_application)
                            .tag(this@MessageActivity)
                            .headers("token", getString("token"))
                            .params("applicationId", data.msg_receive_id)
                            .params("status", "-1")
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {

                                    toast(msg)
                                    data.astatus = "-1"
                                    mAdapter.notifyItemChanged(index)
                                }

                            })
                    }
            }
            .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.msg_list_data)
            .tag(this@MessageActivity)
            .headers("token", getString("token"))
            .params("page", pindex)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    val items = ArrayList<CommonData>()
                    items.addItems(response.body().`object`)

                    list.apply {
                        if (pindex == 1) {
                            clear()
                            pageNum = pindex
                        }
                        if (count(response.body().`object`) > 0) pageNum++
                    }

                    items.forEach {
                        if (it.type == "1") {
                            list.add(
                                FriendData(
                                    it.msg_receive_id,
                                    it.send_date,
                                    it.title,
                                    it.mome,
                                    it.astatus,
                                    it.type,
                                    it.user_head ?: ""
                                )
                            )
                        } else list.add(it)
                    }

                    mAdapter.updateData(list)
                }

                override fun onFinish() {
                    super.onFinish()
                    swipe_refresh.isRefreshing = false
                    isLoadingMore = false

                    empty_view.apply { if (list.isEmpty()) visible() else gone() }
                }

            })
    }

}
