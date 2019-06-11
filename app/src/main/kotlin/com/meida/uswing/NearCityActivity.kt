package com.meida.uswing

import android.os.Bundle
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.BaseActivity
import com.meida.base.addItems
import com.meida.fragment.CityFragment
import com.meida.fragment.OnFragmentCityListener
import com.meida.model.CommonData
import com.meida.model.LocationMessageEvent
import com.meida.share.BaseHttp
import com.meida.utils.ActivityStack
import org.greenrobot.eventbus.EventBus

class NearCityActivity : BaseActivity(), OnFragmentCityListener {

    private var listProvince = ArrayList<CommonData>()
    private var listCity = ArrayList<CommonData>()
    private var mProvince = ""
    private var mCity = ""

    private lateinit var first: CityFragment
    private lateinit var second: CityFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_near_city)
        init_title("选择省份")

        supportFragmentManager.addOnBackStackChangedListener {
            tvTitle.text = when (supportFragmentManager.backStackEntryCount) {
                1 -> "选择城市"
                else -> "选择省份"
            }
        }

        getProvince()
    }

    override fun onViewClick(type: String, id: String, name: String) {
        when (type) {
            "省" -> {
                mProvince = name
                getCity(id)
            }
            "市" -> {
                mCity = name

                val item = listCity.first { it.areaCode == id }
                EventBus.getDefault().post(
                    LocationMessageEvent(
                        intent.getStringExtra("type"),
                        item.lng,
                        item.lat,
                        mProvince,
                        mCity
                    )
                )

                ActivityStack.screenManager.popActivities(this@NearCityActivity::class.java)
            }
        }
    }

    private fun getProvince() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_area_level)
            .tag(this@NearCityActivity)
            .params("areaLevel", "province")
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                    listProvince.apply {
                        clear()
                        addItems(response.body().data)
                    }

                    if (listProvince.isNotEmpty()) {

                        first = CityFragment()
                        first.arguments = Bundle().apply {
                            putSerializable("list", listProvince)
                            putString("title", "省")
                        }

                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.city_container, first)
                            .commit()
                    }
                }

            })
    }

    private fun getCity(parentId: String) {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_area_parent)
            .tag(this@NearCityActivity)
            .params("parentId", parentId)
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {
                    listCity.apply {
                        clear()
                        addItems(response.body().data)
                    }

                    if (listCity.isNotEmpty()) {
                        tvTitle.text = "选择城市"
                        second = CityFragment()
                        second.arguments = Bundle().apply {
                            putSerializable("list", listCity)
                            putString("title", "市")
                        }

                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.push_left_in,
                                R.anim.push_left_out,
                                R.anim.push_right_in,
                                R.anim.push_right_out
                            )
                            .add(R.id.city_container, second)
                            .hide(first)
                            .addToBackStack(null)
                            .commit()
                    }
                }

            })
    }

}
