package com.meida.uswing

import android.os.Bundle
import android.view.View
import com.lzg.extend.StringDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.DialogHelper.showHintDialog
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.include
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class CoachHonorActivity : BaseActivity() {

    private val list = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        include<View>(R.layout.layout_list)
        init_title("我的荣誉", "添加")

        EventBus.getDefault().register(this@CoachHonorActivity)

        @Suppress("UNCHECKED_CAST")
        list.addItems(intent.getSerializableExtra("list") as ArrayList<CommonData>)
        mAdapter.updateData(list)
    }

    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关荣誉记录！"
        swipe_refresh.refresh {
            Completable.timer(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    swipe_refresh.isRefreshing = false
                    mAdapter.notifyDataSetChanged()
                }
        }
        recycle_list.load_Linear(baseContext, swipe_refresh)

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_honor_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_honor_title, data.honorInfo)
                    .visibility(R.id.item_honor_divider1, if (!isLast) View.VISIBLE else View.GONE)
                    .visibility(R.id.item_honor_divider2, if (isLast) View.VISIBLE else View.GONE)

                    .clicked(R.id.item_honor_del) {
                        showHintDialog("提示", "您确定要删除该荣誉吗？") {
                            if (it == "确定") {
                                OkGo.post<String>(BaseHttp.delete_honor)
                                    .tag(this@CoachHonorActivity)
                                    .headers("token", getString("token"))
                                    .params("honorId", data.honorId)
                                    .execute(object : StringDialogCallback(baseContext) {

                                        override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                            toast(msg)
                                            list.remove(data)
                                            mAdapter.notifyDataSetChanged()
                                            empty_view.apply { if (list.isEmpty()) visible() else gone() }
                                            EventBus.getDefault().post(RefreshMessageEvent("删除荣誉", index.toString()))
                                        }

                                    })
                            }
                        }
                    }
            }
            .attachTo(recycle_list)

        tvRight.oneClick { startActivity<CoachEditActivity>("title" to "添加荣誉") }
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@CoachHonorActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "添加荣誉" -> {
                empty_view.gone()

                list.add(CommonData().apply {
                    honorId = event.id
                    honorInfo = event.name
                })

                mAdapter.notifyDataSetChanged()
            }
        }
    }

}
