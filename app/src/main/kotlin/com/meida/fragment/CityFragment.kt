package com.meida.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meida.base.BaseFragment
import com.meida.base.load_Linear
import com.meida.model.CommonData
import com.meida.uswing.R
import kotlinx.android.synthetic.main.fragment_city.*
import net.idik.lib.slimadapter.SlimAdapter

class CityFragment : BaseFragment() {

    private lateinit var list: ArrayList<CommonData>
    private var mTitle = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_city, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()

        mTitle = arguments?.getString("title") ?: ""
        @Suppress("UNCHECKED_CAST")
        list = arguments?.getSerializable("list") as ArrayList<CommonData>
        mAdapter.updateData(list)
    }

    override fun init_title() {
        activity?.let { recycle_list.load_Linear(it) }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_city_list) { data, injector ->

                val isLast = list.indexOf(data) == list.size - 1

                injector.text(R.id.item_city_name, data.areaName)
                    .visibility(R.id.item_city_divider1, if (isLast) View.GONE else View.VISIBLE)
                    .visibility(R.id.item_city_divider2, if (!isLast) View.GONE else View.VISIBLE)

                    .clicked(R.id.item_city) {
                        (activity as OnFragmentCityListener).onViewClick(mTitle, data.areaCode, data.areaName)
                    }
            }
            .attachTo(recycle_list)
    }

}
