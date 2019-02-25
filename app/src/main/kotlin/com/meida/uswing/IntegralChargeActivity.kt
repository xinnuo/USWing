package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.meida.utils.toNotDouble
import com.meida.utils.toTextDouble
import com.meida.utils.toTextInt
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_integral_charge.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.text.DecimalFormat

@WX(packageName = "com.meida.uswing")
class IntegralChargeActivity : BaseActivity() {

    private var mBanlance = 0.0
    private var mRatio = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_integral_charge)
        init_title("充值")

        getData()
        getRatioData()
        integral_group.check(R.id.integral_check1)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.bt_done -> {
                when {
                    integral_num.text.isEmpty() -> {
                        toast("请输入积分充值数量")
                        return
                    }
                    integral_num.text.toTextDouble() == 0.0 -> {
                        toast("积分充值数量不少于0")
                        return
                    }
                }

                when (integral_group.checkedRadioButtonId) {
                    R.id.integral_check1 -> getPayData(integral_num.text.toString(), "AliPay")
                    R.id.integral_check2 -> getPayData(integral_num.text.toString(), "WxPay")
                    R.id.integral_check3 -> {
                        if (mBanlance == 0.0) {
                            toast("当前余额不足")
                            return
                        }

                        if (mRatio > 0) {
                            val mCount = integral_num.text.toTextInt()
                            val money = mCount / mRatio
                            if (money > mBanlance) {
                                toast("当前余额不足")
                                return
                            }
                        }

                        getBalanceData(integral_num.text.toString())
                    }
                }
            }
        }
    }

    /* 余额查询 */
    override fun getData() {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@IntegralChargeActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                @SuppressLint("SetTextI18n")
                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    mBanlance = obj.optString("balance", "0").toDouble()
                    integral_check3.text = "余额充值(${DecimalFormat("0.00").format(mBanlance)}元)"
                }

            })
    }

    /* 积分比例查询 */
    private fun getRatioData() {
        OkGo.post<String>(BaseHttp.signin_proportion)
            .tag(this@IntegralChargeActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    val obj = JSONObject(response.body()).optString("object")
                    mRatio = obj.toNotDouble()
                }

            })
    }

    /* 余额支付 */
    private fun getBalanceData(count: String) {
        OkGo.post<String>(BaseHttp.add_recharge_integral_balance)
            .tag(this@IntegralChargeActivity)
            .headers("token", getString("token"))
            .params("rechargeSum", count)
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                    toast(msg)
                    integral_num.setText("")
                    EventBus.getDefault().post(RefreshMessageEvent("积分充值"))
                }

            })
    }

    /* 支付宝、微信支付 */
    private fun getPayData(count: String, way: String) {
        OkGo.post<String>(BaseHttp.add_recharge_integral)
            .tag(this@IntegralChargeActivity)
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
                                    integral_num.setText("")
                                    EventBus.getDefault().post(RefreshMessageEvent("积分充值"))
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
                                    integral_num.setText("")
                                    EventBus.getDefault().post(RefreshMessageEvent("积分充值"))
                                } else {
                                    toast("支付失败")
                                }
                            }) { OkLogger.printStackTrace(it) }
                    }
                }

            })
    }

}
