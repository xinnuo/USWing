package com.meida.uswing

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import com.sunfusheng.GlideImageView
import io.rong.imkit.RongIM
import io.rong.imlib.model.UserInfo
import kotlinx.android.synthetic.main.activity_contact_member.*
import kotlinx.android.synthetic.main.layout_empty.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class ContactMemberActivity : BaseActivity() {

    private val list = ArrayList<CommonData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_member)
        init_title("群成员")

        EventBus.getDefault().register(this@ContactMemberActivity)

        @Suppress("UNCHECKED_CAST")
        list.addAll(intent.getSerializableExtra("list") as ArrayList<CommonData>)
        mAdapter.updateData(list)
        empty_view.apply { if (list.isEmpty()) visible() else gone() }
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关成员信息！"
        contact_list.load_Linear(baseContext)

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_contact_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.gone(R.id.item_contact_check)
                    .text(R.id.item_contact_name, data.nick_name)

                    .with<GlideImageView>(R.id.item_contact_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }

                    .visibility(R.id.item_contact_divider1, if (isLast) View.GONE else View.VISIBLE)
                    .visibility(
                        R.id.item_contact_divider2,
                        if (!isLast) View.GONE else View.VISIBLE
                    )

                    .clicked(R.id.item_contact) {
                        if (data.user_info_id != getString("token")) {
                            //融云刷新用户信息
                            RongIM.getInstance().refreshUserInfoCache(
                                UserInfo(
                                    data.user_info_id,
                                    data.nick_name,
                                    Uri.parse(BaseHttp.baseImg + data.user_head)
                                )
                            )
                            //融云单聊
                            RongIM.getInstance()
                                .startPrivateChat(baseContext, data.user_info_id, data.nick_name)
                        }
                    }
            }
            .attachTo(contact_list)
    }

    override fun finish() {
        super.finish()
        EventBus.getDefault().unregister(this@ContactMemberActivity)
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "修改昵称" -> {
                val index = list.indexOfFirst { it.friend_id == event.id }
                list[index].nick_name = event.name
                mAdapter.updateData(list)
            }
        }
    }

}
