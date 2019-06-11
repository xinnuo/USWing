package com.meida.uswing

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.meida.base.*
import com.meida.model.CommonData
import com.meida.share.BaseHttp
import com.meida.sort.CharacterParser
import com.meida.sort.NormalDecoration
import com.meida.sort.PinyinComparator
import com.meida.utils.ActivityStack
import com.meida.utils.dp2px
import com.meida.utils.sp2px
import com.sunfusheng.GlideImageView
import io.rong.imkit.RongIM
import io.rong.imlib.IRongCallback
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.UserInfo
import io.rong.message.RichContentMessage
import kotlinx.android.synthetic.main.activity_compare_contact.*
import kotlinx.android.synthetic.main.layout_empty.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.toast
import java.util.*

class CompareContactActivity : BaseActivity() {

    private val letters by lazy {
        listOf(
            "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "#"
        )
    }
    private val list = ArrayList<CommonData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_contact)
        init_title("好友")

        getData()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关好友信息！"
        contact_list.apply {
            linearLayoutManager = LinearLayoutManager(baseContext)
            layoutManager = linearLayoutManager

            addItemDecoration(object : NormalDecoration() {
                override fun getHeaderName(pos: Int): String = list[pos].letter
            }.apply {
                setHeaderContentColor(resources.getColor(R.color.background))
                setHeaderHeight(dp2px(40f))
                setTextSize(sp2px(16f))
                setTextColor(resources.getColor(R.color.black))
            })
        }

        mAdapter = SlimAdapter.create()
            .register<CommonData>(R.layout.item_contact_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.gone(R.id.item_contact_check)
                    .text(R.id.item_contact_name, data.nick_name)

                    .with<GlideImageView>(R.id.item_contact_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }

                    .visibility(
                        R.id.item_contact_divider1,
                        if ((!isLast && data.letter != list[index + 1].letter) || isLast) View.GONE else View.VISIBLE
                    )

                    .clicked(R.id.item_contact) {
                        val contentMsg = RichContentMessage.obtain(
                            "",
                            "点评",
                            intent.getStringExtra("videoImg"),
                            intent.getStringExtra("video"))
                        contentMsg.extra = intent.getStringExtra("videoId")

                        val sendMsg = Message.obtain(data.fuser_id, Conversation.ConversationType.PRIVATE, contentMsg)

                        RongIM.getInstance().sendMessage(sendMsg, null, null, object : IRongCallback.ISendMessageCallback {

                            override fun onAttached(message: Message) { }

                            override fun onSuccess(message: Message) {
                                toast("发送成功")
                            }

                            override fun onError(message: Message, errorCode: RongIMClient.ErrorCode) { }

                        })

                        //融云刷新用户信息
                        RongIM.getInstance().refreshUserInfoCache(
                            UserInfo(
                                data.fuser_id,
                                data.nick_name,
                                Uri.parse(BaseHttp.baseImg + data.user_head)
                            )
                        )
                        //融云单聊
                        RongIM.getInstance()
                            .startPrivateChat(baseContext, data.fuser_id, data.nick_name)

                        ActivityStack.screenManager.popActivities(this@CompareContactActivity::class.java)
                    }
            }
            .attachTo(contact_list)

        contact_index.setIndexBarHeightRatio(0.9f)
        contact_index.indexBar.apply {
            setIndexsList(letters)
            setIndexChangeListener { name ->
                if (list.any { it.letter == name }) {
                    val index = list.indexOfFirst { it.letter == name }
                    linearLayoutManager.scrollToPositionWithOffset(index, 0)
                    return@setIndexChangeListener
                }
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_friend_list)
            .tag(this@CompareContactActivity)
            .headers("token", getString("token"))
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        clear()
                        addItems(response.body().data)
                    }

                    seperateLists()
                    mAdapter.updateData(list)
                }

                override fun onFinish() {
                    super.onFinish()
                    empty_view.apply { if (list.isEmpty()) visible() else gone() }
                }

            })
    }

    private fun seperateLists() {
        if (list.isNotEmpty()) {
            list.forEach {
                val letter = CharacterParser.getInstance().getSelling(it.nick_name)
                val sortStr = letter.substring(0, 1).toUpperCase()
                it.letter = if (sortStr.matches("[A-Z]".toRegex())) sortStr else "#"
            }
            Collections.sort(list, PinyinComparator())
        }
    }

}
