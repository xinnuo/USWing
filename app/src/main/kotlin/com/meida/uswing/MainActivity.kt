package com.meida.uswing

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.widget.CompoundButton
import com.meida.base.BaseActivity
import com.meida.fragment.MainFirstFragment
import com.meida.fragment.MainFourthFragment
import com.meida.fragment.MainSecondFragment
import com.meida.fragment.MainThirdFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbarVisibility(false)
        init_title()

        main_check1.performClick()
    }

    override fun init_title() {
        main_check1.setOnCheckedChangeListener(this)
        main_check2.setOnCheckedChangeListener(this)
        main_check3.setOnCheckedChangeListener(this)
        main_check4.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        // instantiateItem从FragmentManager中查找Fragment，找不到就getItem新建一个，
        // setPrimaryItem设置隐藏和显示，最后finishUpdate提交事务。
        if (isChecked) {
            val fragment = mFragmentPagerAdapter
                .instantiateItem(main_container, buttonView.id) as Fragment
            mFragmentPagerAdapter.setPrimaryItem(main_container, 0, fragment)
            mFragmentPagerAdapter.finishUpdate(main_container)
        }
    }

    private val mFragmentPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {

        override fun getItem(position: Int): Fragment = when (position) {
            R.id.main_check1 -> MainFirstFragment()
            R.id.main_check2 -> MainSecondFragment()
            R.id.main_check3 -> MainThirdFragment()
            R.id.main_check4 -> MainFourthFragment()
            else -> MainFirstFragment()
        }

        override fun getCount(): Int = 4
    }

    private var exitTime: Long = 0
    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            toast("再按一次退出程序")
            exitTime = System.currentTimeMillis()
        } else super.onBackPressed()
    }
}
