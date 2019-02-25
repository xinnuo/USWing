package com.meida.uswing

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import com.cuieney.rxpay_annotation.WX
import com.cuieney.sdk.rxpay.RxPay
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.DecimalNumberFilter
import com.meida.utils.toTextDouble
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_wallet_charge.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject

@WX(packageName = "com.meida.uswing")
class WalletChargeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_charge)
        init_title("充值", "充值记录")
    }

    override fun init_title() {
        super.init_title()
        charge_num.filters = arrayOf<InputFilter>(DecimalNumberFilter())
        charge_group.check(R.id.charge_check1)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_nav_right -> startActivity<WalletChargeListActivity>()
            R.id.bt_done -> {
                when {
                    charge_num.text.isEmpty() -> {
                        toast("请输入充值金额")
                        return
                    }
                    charge_num.text.toTextDouble() == 0.0 -> {
                        toast("充值金额不少于0.01元")
                        return
                    }
                }

                when (charge_group.checkedRadioButtonId) {
                    R.id.charge_check1 -> getPayData(charge_num.text.toString(), "AliPay")
                    R.id.charge_check2 -> getPayData(charge_num.text.toString(), "WxPay")
                }
            }
        }
    }

    /* 支付宝、微信支付 */
    private fun getPayData(count: String, way: String) {
        OkGo.post<String>(BaseHttp.add_recharge)
            .tag(this@WalletChargeActivity)
            .headers("token", getString("token"))
            .params("rechargeSum", count)
            .params("payType", way)
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body()).optString("object")
                    val data = JSONObject(response.body()).optString("object")
                    when (way) {
                        "AliPay" -> RxPay(baseContext)
                            .requestAlipay(obj)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (it) {
                                    toast("支付成功")
                                    charge_num.setText("")
                                    EventBus.getDefault().post(RefreshMessageEvent("充值成功"))
                                } else {
                                    toast("支付失败")
                                }
                            }) { OkLogger.printStackTrace(it) }
                        "WxPay" -> RxPay(baseContext)
                            .requestWXpay(data)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (it) {
                                    toast("支付成功")
                                    charge_num.setText("")
                                    EventBus.getDefault().post(RefreshMessageEvent("充值成功"))
                                } else {
                                    toast("支付失败")
                                }
                            }) { OkLogger.printStackTrace(it) }
                    }
                }

            })
    }

}
