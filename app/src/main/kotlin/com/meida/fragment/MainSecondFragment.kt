package com.meida.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meida.base.BaseFragment
import com.meida.base.oneClick
import com.meida.rong.ConversationListFragmentEx
import com.meida.uswing.ContactActivity
import com.meida.uswing.MessageActivity
import com.meida.uswing.R
import io.rong.imkit.fragment.ConversationListFragment
import io.rong.imlib.model.Conversation
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

        enterFragment()
    }

    override fun init_title() {
        second_group.oneClick { startActivity<ContactActivity>() }
        second_msg.oneClick { startActivity<MessageActivity>() }
    }

    /**
     * 动态加载 会话列表 ConversationListFragment
     *
     * 1、rc_ext_extension_bar.xml            输入框布局文件。它是整个输入框的容器，内部有对各部分组件功能描述。
     * 2、rc_ext_input_edit_text.xml EditText 布局文件。如果想要替换背景，直接修改即可。
     * 3、rc_ext_voice_input.xml              语音输入布局文件。
     * 4、rc_fr_conversation.xml              app:RCStyle="SCE" ，更改默认输入显示形式
     * 5、rc_fr_conversationlist.xml          修改空布局位置。
     */
    private fun enterFragment() {
        val fragment = ConversationListFragmentEx()
        val uri = Uri.parse("rong://" + activity!!.applicationInfo.packageName).buildUpon()
            .appendPath("conversationlist")
            .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false")    //设置私聊会话是否聚合显示
            .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")       //设置群组会话是否聚合显示
            .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false") //设置讨论组会话是否聚合显示
            .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")     //设置系统会话是否聚合显示
            .build()
        fragment.uri = uri  //设置 ConverssationListFragment 的显示属性

        childFragmentManager.beginTransaction()
            .add(R.id.second_message, fragment)
            .commit()
    }

}
