package com.meida.uswing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.lqr.ninegridimageview.LQRNineGridImageView
import com.lzg.extend.BaseResponse
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
import com.meida.utils.dp2px
import com.meida.utils.setAdapter
import com.meida.utils.sp2px
import kotlinx.android.synthetic.main.activity_contact_group.*
import kotlinx.android.synthetic.main.layout_empty.*
import net.idik.lib.slimadapter.SlimAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.startActivity
import java.util.*

class ContactGroupActivity : BaseActivity() {

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
        setContentView(R.layout.activity_contact_group)
        init_title("群聊", "创建")

        EventBus.getDefault().register(this@ContactGroupActivity)

        getData()
    }

    @Suppress("DEPRECATION")
    override fun init_title() {
        super.init_title()
        empty_hint.text = "暂无相关群聊信息！"

        group_list.apply {
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
            .register<CommonData>(R.layout.item_group_list) { data, injector ->

                val index = list.indexOf(data)
                val isLast = index == list.size - 1

                injector.text(R.id.item_group_name, data.groupchat_name)
                    .visibility(
                        R.id.item_group_divider1,
                        if ((!isLast && data.letter != list[index + 1].letter) || isLast) View.GONE else View.VISIBLE
                    )
                    .with<LQRNineGridImageView<String>>(R.id.item_group_nine) {
                        it.setAdapter {
                            onDisplayImage { _, imageView, url ->
                                imageView.setImageURL(BaseHttp.baseImg + url)
                            }
                        }

                        if (!data.ls.isNullOrEmpty()) it.setImagesData(data.ls)
                    }
            }
            .attachTo(group_list)

        group_index.setIndexBarHeightRatio(0.9f)
        group_index.indexBar.apply {
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

    override fun doClick(v: View) {
        super.doClick(v)
        when (v.id) {
            R.id.tv_nav_right -> startActivity<ContactCreateActivity>()
        }
    }

    override fun getData() {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_groupchat_list)
            .tag(this@ContactGroupActivity)
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
                val letter = CharacterParser.getInstance().getSelling(it.groupchat_name)
                val sortStr = letter.substring(0, 1).toUpperCase()
                it.letter = if (sortStr.matches("[A-Z]".toRegex())) sortStr else "#"
            }
            Collections.sort(list, PinyinComparator())
        }
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@ContactGroupActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "创建群聊" -> getData()
        }
    }

}
