package com.meida.uswing

import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.addItems
import com.meida.base.getString
import com.meida.model.CommonData
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import com.meida.view.FullyLinearLayoutManager
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.listeners.onClick
import org.jetbrains.anko.support.v4.nestedScrollView
import java.util.ArrayList

class ReportActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()
    private var reportId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nestedScrollView {
            overScrollMode = View.OVER_SCROLL_NEVER

            verticalLayout {
                lparams(width = matchParent, height = wrapContent)

                themedTextView("请选择举报类型：", R.style.Font14_black) {
                    gravity = Gravity.CENTER_VERTICAL
                }.lparams(width = matchParent, height = dip(45)) {
                    marginStart = dip(15)
                    marginEnd = dip(15)
                }

                view { backgroundColorResource = R.color.divider }.lparams(height = dip(0.5f))

                recyclerView {
                    lparams(width = matchParent, height = wrapContent)

                    layoutManager = FullyLinearLayoutManager(baseContext)
                    mAdapter = SlimAdapter.create()
                        .register<CommonData>(R.layout.item_report_list) { data, injector ->

                            val isLast = list.indexOf(data) == list.size - 1

                            @Suppress("DEPRECATION")
                            injector.text(R.id.item_report_title, data.reportInfo)
                                .textColor(
                                    R.id.item_report_title,
                                    resources.getColor(if (data.isChecked) R.color.black else R.color.light)
                                )
                                .visibility(
                                    R.id.item_report_arrow,
                                    if (data.isChecked) View.VISIBLE else View.GONE
                                )
                                .visibility(
                                    R.id.item_report_divider1,
                                    if (isLast) View.GONE else View.VISIBLE
                                )
                                .visibility(
                                    R.id.item_report_divider2,
                                    if (!isLast) View.GONE else View.VISIBLE
                                )
                                .clicked(R.id.item_report) {
                                    list.filter { it.isChecked }.forEach { it.isChecked = false }
                                    data.isChecked = true
                                    reportId = data.reportinfoId
                                    mAdapter.notifyDataSetChanged()
                                }
                        }
                        .attachTo(this)
                }

                themedButton("提交", R.style.Font16_white) {
                    backgroundResource = R.mipmap.btn01

                    onClick {
                        if (reportId.isEmpty()) {
                            toast("请选择举报类型")
                            return@onClick
                        }

                        /* 举报 */
                        OkGo.post<String>(BaseHttp.add_report)
                            .tag(this@ReportActivity)
                            .headers("token", getString("token"))
                            .params("circleId", intent.getStringExtra("circleId"))
                            .params("reportInfo", reportId)
                            .execute(object : StringDialogCallback(baseContext) {

                                override fun onSuccessResponse(
                                    response: Response<String>,
                                    msg: String,
                                    msgCode: String
                                ) {

                                    toast(msg)
                                    ActivityStack.screenManager.popActivities(this@ReportActivity::class.java)
                                }

                            })
                    }
                }.lparams(width = matchParent, height = dip(44)) {
                    margin = dip(20)
                }
            }
        }



        init_title("举报")

        getData()
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_reportinfo_list)
            .tag(this@ReportActivity)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        clear()
                        addItems(response.body().data)
                    }

                    mAdapter.updateData(list)
                }

            })
    }

}
