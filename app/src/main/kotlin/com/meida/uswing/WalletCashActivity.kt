package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.getString
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.BankcardHelper
import com.meida.utils.DecimalNumberFilter
import com.meida.utils.toTextDouble
import com.meida.utils.trimString
import kotlinx.android.synthetic.main.activity_wallet_cash.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.text.DecimalFormat

class WalletCashActivity : BaseActivity() {

    private var mBanlance = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet_cash)
        init_title("提现", "提现记录")
    }

    @SuppressLint("SetTextI18n")
    override fun init_title() {
        super.init_title()
        mBanlance = intent.getDoubleExtra("balance", 0.0)
        cash_balance.text = "可提现金额：${DecimalFormat("0.00").format(mBanlance)}元"
        cash_num.filters = arrayOf<InputFilter>(DecimalNumberFilter())
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_nav_right -> startActivity<WalletCashListActivity>()
            R.id.bt_submit -> {
                when {
                    cash_num.text.isEmpty() -> {
                        toast("请输入提现金额")
                        return
                    }
                    cash_name.text.isBlank() -> {
                        toast("请输入账户名")
                        return
                    }
                    !cash_card.hasMask() -> {
                        toast("请输入银行卡账号")
                        return
                    }
                    cash_bank.text.isBlank() -> {
                        toast("请输入银行名称")
                        return
                    }
                    cash_num.text.toTextDouble() <= 0.0 -> {
                        toast("提现金额不少于0.01元")
                        return
                    }
                    !BankcardHelper.checkBankCard(cash_card.rawText) -> {
                        toast("请输入正确的银行卡账号")
                        return
                    }
                    mBanlance > 0  && cash_num.text.toTextDouble() > mBanlance -> {
                        toast("可提现金额不足")
                        return
                    }
                }

                OkGo.post<String>(BaseHttp.add_withdraw)
                    .tag(this@WalletCashActivity)
                    .isMultipart(true)
                    .headers("token", getString("token"))
                    .params("withdrawSum", cash_num.text.toString())
                    .params("carno", cash_name.text.trimString())
                    .params("bankName", cash_bank.text.trimString())
                    .params("cardNumber", cash_card.rawText)
                    .execute(object : StringDialogCallback(baseContext) {

                        @SuppressLint("SetTextI18n")
                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                            toast(msg)
                            EventBus.getDefault().post(RefreshMessageEvent("提现成功"))

                            val withdrawSum = cash_num.text.toTextDouble()
                            mBanlance -= withdrawSum

                            cash_num.setText("")
                            cash_balance.text = "可提现金额：${DecimalFormat("0.00").format(mBanlance)}元"
                            cash_name.setText("")
                            cash_card.setText("")
                            cash_bank.setText("")
                        }

                    })
            }
        }
    }
}
