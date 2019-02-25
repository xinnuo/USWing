package com.meida.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meida.base.BaseFragment
import com.meida.base.oneClick
import com.meida.uswing.ContactActivity
import com.meida.uswing.MessageActivity
import com.meida.uswing.R
import kotlinx.android.synthetic.main.fragment_main_second.*
import org.jetbrains.anko.support.v4.startActivity

class MainSecondFragment : BaseFragment() {

    //调用这个方法切换时不会释放掉Fragment
    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        this.view?.visibility = if (menuVisible) View.VISIBLE else View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init_title()
    }

    override fun init_title() {
        second_group.oneClick { startActivity<ContactActivity>() }
        second_msg.oneClick { startActivity<MessageActivity>() }
    }

}
