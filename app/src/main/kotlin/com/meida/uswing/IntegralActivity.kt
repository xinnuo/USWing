package com.meida.uswing

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.meida.utils.TimeHelper
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_integral.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class IntegralActivity : BaseActivity() {

    private val list = ArrayList<Any>()
    private var mIntegral = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_integral)
        setTransparentAndToobar(false)
        init_title()

        EventBus.getDefault().register(this@IntegralActivity)

        if (getString("sign") != "1") getSignData()
        else getData()
    }

    override fun init_title() {
        mPosition = 1
        integral_sign.text = when (getString("sign")) {
            "1" -> "已签到"
            else -> "签到+${getString("signSum")}"
        }

        integral_tab.apply {
            onTabSelectedListener {
                onTabSelected {
                    mPosition = 1 - it!!.position

                    OkGo.getInstance().cancelTag(this@IntegralActivity)
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

                injector.text(R.id.item_wallet_time, data.integral_date)
                    .text(
                        R.id.item_wallet_title, when (data.integral_type) {
                            "0" -> "充值"
                            "1" -> "签到"
                            "2" -> "连续签到"
                            "3" -> "来自 ${data.nick_name} 的打赏"
                            "4" -> "提现失败"
                            "6" -> "打赏 ${data.nick_name}"
                            "7" -> "提现"
                            else -> "其他"
                        }
                    )
                    .text(
                        R.id.item_wallet_num,
                        if (mPosition == 1) "+${data.integral_num}" else "-${data.integral_num}"
                    )
                    .visibility(R.id.item_wallet_divider1, if (isLast) View.GONE else View.VISIBLE)
            }
            .attachTo(recycle_list)
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_nav_right -> startActivity<WebActivity>("title" to "积分规则")
            R.id.integral_charge -> startActivity<IntegralChargeActivity>()
            R.id.integral_withdraw -> startActivity<IntegralCashActivity>("integral" to mIntegral)
        }
    }

    /* 积分查询 */
    override fun getData() {
        OkGo.post<String>(BaseHttp.find_user_details)
            .tag(this@IntegralActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext, false) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    val obj = JSONObject(response.body())
                        .optJSONObject("object") ?: JSONObject()

                    mIntegral = obj.optString("integral", "0").toInt()
                    integral_num.text = mIntegral.toString()

                    getSignedData()
                }

            })
    }

    /* 签到 */
    private fun getSignData() {
        OkGo.post<String>(BaseHttp.add_signin)
            .tag(this@IntegralActivity)
            .headers("token", getString("token"))
            .execute(object : StringDialogCallback(baseContext) {

                override fun onSuccessResponse(
                    response: Response<String>,
                    msg: String,
                    msgCode: String
                ) {

                    toast(msg)
                    putString("sign", "1")
                    integral_sign.text = "已签到"
                    getData()
                }

            })
    }

    /* 签到列表 */
    private fun getSignedData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_signin_list)
            .tag(this@IntegralActivity)
            .headers("token", getString("token"))
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                @Suppress("DEPRECATION")
                @SuppressLint("SetTextI18n")
                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    val listSigned = ArrayList<CommonData>()
                    listSigned.addItems(response.body().`object`)

                    val timer = TimeHelper.getInstance()
                    val items = ArrayList<String>()
                    val itemMonth = ArrayList<String>()
                    val nowDate = timer.stringDateShort
                    val nowDay = timer.dayOfWeek

                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 0)
                        )
                    )
                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 1)
                        )
                    )
                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 2)
                        )
                    )
                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 3)
                        )
                    )
                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 4)
                        )
                    )
                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 5)
                        )
                    )
                    items.add(
                        timer.stringToString(
                            "yyyy年MM月dd日",
                            timer.getAnyWeekDay(if (nowDay == 0) -1 else 0, 6)
                        )
                    )

                    items.mapTo(itemMonth) { timer.stringToString("yyyy-MM-dd", "MM/dd", it) }

                    items.forEachWithIndex { index, str ->
                        var signSum = "0"
                        if (listSigned.any { it.signin_day == str }) {
                            signSum = listSigned.first { it.signin_day == str }.signin_sum
                        }

                        when (index) {
                            0 -> {
                                integral_num1.text = "+$signSum"
                                integral_date1.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num1.setTextColor(resources.getColor(R.color.red))
                                    integral_date1.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                            1 -> {
                                integral_num2.text = "+$signSum"
                                integral_date2.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num2.setTextColor(resources.getColor(R.color.red))
                                    integral_date2.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                            2 -> {
                                integral_num3.text = "+$signSum"
                                integral_date3.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num3.setTextColor(resources.getColor(R.color.red))
                                    integral_date3.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                            3 -> {
                                integral_num4.text = "+$signSum"
                                integral_date4.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num4.setTextColor(resources.getColor(R.color.red))
                                    integral_date4.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                            4 -> {
                                integral_num5.text = "+$signSum"
                                integral_date5.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num5.setTextColor(resources.getColor(R.color.red))
                                    integral_date5.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                            5 -> {
                                integral_num6.text = "+$signSum"
                                integral_date6.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num6.setTextColor(resources.getColor(R.color.red))
                                    integral_date6.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                            6 -> {
                                integral_num7.text = "+$signSum"
                                integral_date7.text = itemMonth[index]
                                if (nowDate == str) {
                                    integral_num7.setTextColor(resources.getColor(R.color.red))
                                    integral_date7.setTextColor(resources.getColor(R.color.red))
                                }
                            }
                        }
                    }
                }

            })
    }

    /* 积分列表 */
    override fun getData(pindex: Int) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_integral_list)
            .tag(this@IntegralActivity)
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
        EventBus.getDefault().unregister(this@IntegralActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "积分充值", "积分提现" -> getData()
        }
    }

}
