package com.meida.uswing

import android.os.Bundle
import android.text.Html
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.StringDialogCallback
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_wallet.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class WalletActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mBanlance = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        init_title("我的钱包")

        EventBus.getDefault().register(this@WalletActivity)
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    override fun init_title() {
        super.init_title()

        @Suppress("DEPRECATION")
        wallet_num.text =
            Html.fromHtml(String.format("￥ <big><big><big>%1\$s</big></big></big>", 0))

        wallet_tab.apply {
            onTabSelectedListener {
                onTabSelected {
                    mPosition = 1 - it!!.position

                    OkGo.getInstance().cancelTag(this@WalletActivity)
                    Completable.timer(300, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { updateList() }
                }
            }

            addTab(this.newTab().setText("收入明细"), true)
            addTab(this.newTab().setText("支出明细"), false)
        }

        swipe_refresh.refresh { getData(1) }
        recycle_list.load_Linear(baseContext, swipe_refresh) {
            if (!isLoadingMore) {
                isLoadingMore = true
                getData(pageNum)
            }
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_wallet_list) { data, injector ->

                val isLast = list.indexOf(data) == list.size - 1

                injector.text(R.id.item_wallet_time, data.create_date)
                    .text(
                        R.id.item_wallet_title, when (data.amount_type) {
                            "1" -> "充值"
                            "2" -> "提现失败"
                            "5" -> "提现"
                            "6" -> "积分充值"
                            else -> "其他"
                        }
                    )
                    .text(
                        R.id.item_wallet_num,
                        if (mPosition == 0) "+${data.opt_amount}" else "-${data.opt_amount}"
                    )
                    .visibility(
                        R.id.item_wallet_divider1,
                        if (isLast) View.GONE else View.VISIBLE
                    )
                    .visibility(R.id.item_wallet_divider2, if (!isLast) View.GONE else View.VISIBLE)
            }
            .attachTo(recycle_list)

        wallet_charge.oneClick { startActivity<WalletChargeActivity>() }
        wallet_withdraw.oneClick { startActivity<WalletCashActivity>("balance" to mBanlance) }
    }

    /* 余额查询 */
    override fun getData() {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@WalletActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    mBanlance = obj.optString("balance", "0").toDouble()
                    @Suppress("DEPRECATION")
                    wallet_num.text = Html.fromHtml(
                        String.format(
                            "¥ <big><big><big>%1\$s</big></big></big>",
                            DecimalFormat("0.00").format(mBanlance)
                        )
                    )
                }

            })
    }

    /* 明细列表 */
    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_amount_List)
            .tag(this@WalletActivity)
            .headers("token", getString("token"))
            .params("type", mPosition)
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
                }

            })
    }

    private fun updateList() {
        swipe_refresh.isRefreshing = true

        if (list.isNotEmpty()) {
            list.clear()
            mAdapter.notifyDataSetChanged()
        }

        pageNum = 1
        getData(pageNum)
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@WalletActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "充值成功", "提现成功" -> getData()
        }
    }

}
