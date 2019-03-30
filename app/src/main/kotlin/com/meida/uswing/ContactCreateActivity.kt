package com.meida.uswing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import com.meida.sort.CharacterParser
import com.meida.sort.NormalDecoration
import com.meida.sort.PinyinComparator
import com.meida.utils.ActivityStack
import com.meida.utils.DialogHelper.showGroupDialog
import com.meida.utils.dp2px
import com.meida.utils.sp2px
import com.sunfusheng.GlideImageView
import kotlinx.android.synthetic.main.activity_contact_create.*
import kotlinx.android.synthetic.main.layout_empty.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList

class ContactCreateActivity : BaseActivity() {

    private val letters by lazy {
        listOf(
            "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "#"
        )
    }
    private val list = ArrayList<CommonData>()
    private val listChecked = ArrayList<CommonData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_create)
        init_title("添加联系人", "保存")

        getData()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关好友信息！"

        create_list.apply {
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

                injector.text(R.id.item_contact_name, data.nick_name)
                    .checked(R.id.item_contact_check, data.isChecked)

                    .with<GlideImageView>(R.id.item_contact_img) {
                        it.loadRectImage(BaseHttp.baseImg + data.user_head)
                    }

                    .visibility(
                        R.id.item_contact_divider1,
                        if ((!isLast && data.letter != list[index + 1].letter) || isLast) View.GONE else View.VISIBLE
                    )
                    .clicked(R.id.item_contact) {
                        data.isChecked = !data.isChecked
                        mAdapter.notifyItemChanged(index)

                        val itemUrl = BaseHttp.baseImg + data.user_head
                        if (listChecked.none { it.fuser_id == data.fuser_id }) {
                            listChecked.add(0, CommonData().apply {
                                fuser_id = data.fuser_id
                                user_head = itemUrl
                            })
                        }
                        else listChecked.removeAll { it.fuser_id == data.fuser_id }
                        (create_selected.adapter as SlimAdapter).updateData(listChecked)
                    }
            }
            .attachTo(create_list)

        create_index.setIndexBarHeightRatio(0.9f)
        create_index.indexBar.apply {
            setIndexsList(letters)
            setIndexChangeListener { name ->
                if (list.any { it.letter == name }) {
                    val index = list.indexOfFirst { it.letter == name }
                    linearLayoutManager.scrollToPositionWithOffset(index, 0)
                    return@setIndexChangeListener
                }
            }
        }

        create_selected.apply {
            layoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
            adapter = SlimAdapter.create()
                .register<CommonData>(R.layout.item_create_list) { data, injector ->
                    injector.with<GlideImageView>(R.id.item_create) {
                        it.loadRectImage(data.user_head!!)
                    }
                }
                .attachTo(this)
        }
    }

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_nav_right -> {
                if (listChecked.isEmpty()) {
                    toast("请选择要添加的联系人")
                    return
                }

                showGroupDialog { str ->
                    if (str.isEmpty()) {
                        toast("请输入群聊名称")
                        return@showGroupDialog
                    }

                    val items = ArrayList<String>()
                    list.filter { it.isChecked }.mapTo(items) { it.fuser_id }

                    /* 创建群组 */
                    OkGo.post<String>(BaseHttp.add_groupchat)
                        .tag(this@ContactCreateActivity)
                        .isMultipart(true)
                        .headers("token", getString("token"))
                        .params("users", items.joinToString(","))
                        .params("groupchatName", str)
                        .execute(object : StringDialogCallback(baseContext) {

                            override fun onSuccessResponse(response: Response<String>, msg: String, msgCode: String) {

                                toast(msg)
                                EventBus.getDefault().post(RefreshMessageEvent("创建群聊"))
                                ActivityStack.screenManager.popActivities(this@ContactCreateActivity::class.java)
                            }

                        })
                }
            }
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_friend_list)
            .tag(this@ContactCreateActivity)
            .headers("token", getString("token"))
            .execute(object :
                JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext, true) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    list.apply {
                        clear()
                        addItems(response.body().`object`)
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
