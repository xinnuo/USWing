package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.share.BaseHttp
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.include
import java.util.ArrayList

class WalletCashListActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("提现记录")

        swipe_refresh.isRefreshing = true
        getData(pageNum)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关提现记录！"
        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_cash_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                @Suppress("DEPRECATION")
                injector.text(R.id.item_cash_num, "-${data.withdraw_sum}")
                    .text(R.id.item_cash_name, "账户名：${data.carno}")
                    .text(R.id.item_cash_card, "账户号：${data.card_number}")
                    .text(R.id.item_cash_time, data.create_date)
                    .text(
                        R.id.item_cash_type, when (data.wstatus) {
                            "0" -> "审核失败"
                            "1" -> "已审核"
                            else -> "审核中"
                        }
                    )
                    .textColor(
                        R.id.item_cash_type, resources.getColor(
                            when (data.wstatus) {
                                "-1" -> R.color.red
                                else -> R.color.gray
                            }
                        )
                    )
                    .visibility(
                        R.id.item_cash_divider1,
                        if (index == 0) View.VISIBLE else View.GONE
                    )
                    .visibility(R.id.item_cash_divider2, if (isLast) View.VISIBLE else View.GONE)
            }
            .attachTo(recycle_list)
    }

    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_withdraw_list)
            .tag(this@WalletCashListActivity)
            .headers("token", getString("token"))
            .params("page", pindex)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        if (pindex == 1) {
                            clear()
                            pageNum = pindex
                        }
                        addItems(response.body().`object`)
                        if (count(response.body().`object`) > 0) pageNum++
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
