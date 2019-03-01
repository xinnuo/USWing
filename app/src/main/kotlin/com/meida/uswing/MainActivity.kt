package com.meida.uswing

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.widget.CompoundButton
import com.lzg.extend.BaseResponse
import com.lzg.extend.jackson.JacksonDialogCallback
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import com.lzy.okgo.utils.OkLogger
import com.meida.RongCloudContext
import com.meida.base.BaseActivity
import com.meida.base.addItems
import com.meida.base.getBoolean
import com.meida.base.getString
import com.meida.fragment.MainFirstFragment
import com.meida.fragment.MainFourthFragment
import com.meida.fragment.MainSecondFragment
import com.meida.fragment.MainThirdFragment
import com.meida.model.CommonData
import com.meida.model.GroupModel
import com.meida.model.RefreshMessageEvent
import com.meida.share.BaseHttp
import io.rong.imkit.RongIM
import io.rong.imkit.model.GroupUserInfo
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Group
import io.rong.imlib.model.UserInfo
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast

class MainActivity : BaseActivity() {

    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbarVisibility(false)
        init_title()

        EventBus.getDefault().register(this@MainActivity)

        main_check1.performClick()
    }

    override fun onStart() {
        super.onStart()

        if (getBoolean("isLogin")
            && getString("rongToken").isNotEmpty()
        ) {
            if (!isConnected) connect(getString("rongToken"))
        }
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

    private fun connect(token: String) {
        /**
         * IMKit SDK调用第二步,建立与服务器的连接
         *
         * <p>连接服务器，在整个应用程序全局，只需要调用一次，需在 {@link #init(Context)} 之后调用。</p>
         * <p>如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
         * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。</p>
         *
         * @param token    从服务端获取的用户身份令牌（Token）。
         * @param callback 连接回调。
         * @return RongIM  客户端核心类的实例。
         */
        RongIM.connect(token, object : RongIMClient.ConnectCallback() {
            /**
             * 连接融云成功
             * @param userid 当前 token 对应的用户 id
             */
            override fun onSuccess(userid: String) {
                isConnected = true
                OkLogger.i("融云连接成功， 用户ID：$userid")
                OkLogger.i(RongIMClient.getInstance().currentConnectionStatus.message)

                RongCloudContext.getInstance().connectedListener()
            }

            /**
             * 连接融云失败
             * @param errorCode 错误码，可到官网 查看错误码对应的注释
             */
            override fun onError(errorCode: RongIMClient.ErrorCode) {
                OkLogger.e("融云连接失败，错误码：" + errorCode.message)
            }

            /**
             * Token 错误。可以从下面两点检查
             * 1.Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
             * 2.token 对应的 appKey 和工程里设置的 appKey 是否一致
             */
            override fun onTokenIncorrect() {
                OkLogger.e("融云token错误！！！")
            }
        })
    }

    /* 更新用户信息 */
    private fun getUserData(userId: String, groupId: String = "") {
        OkGo.post<BaseResponse<ArrayList<CommonData>>>(BaseHttp.find_user_heads)
            .tag(this@MainActivity)
            .headers("token", getString("token"))
            .params("userInfoids", userId)
            .execute(object : JacksonDialogCallback<BaseResponse<ArrayList<CommonData>>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<ArrayList<CommonData>>>) {

                    val items = ArrayList<CommonData>()
                    items.addItems(response.body().`object`)

                    items.forEach {
                        if (groupId.isEmpty()) {
                            RongIM.getInstance().refreshUserInfoCache(
                                UserInfo(
                                    userId,
                                    it.nick_name,
                                    Uri.parse(BaseHttp.baseImg + it.user_head)
                                )
                            )
                        } else {
                            RongIM.getInstance().refreshGroupUserInfoCache(
                                GroupUserInfo(
                                    groupId,
                                    userId,
                                    it.nick_name
                                )
                            )
                        }
                    }

                }

            })
    }

    /* 更新群组及成员信息 */
    private fun getGroupData(groupId: String) {
        OkGo.post<BaseResponse<GroupModel>>(BaseHttp.find_groupchat_users)
            .tag(this@MainActivity)
            .headers("token", getString("token"))
            .params("groupchatId", groupId)
            .execute(object : JacksonDialogCallback<BaseResponse<GroupModel>>(baseContext) {

                override fun onSuccess(response: Response<BaseResponse<GroupModel>>) {

                    val items = ArrayList<CommonData>()
                    val imgs = ArrayList<String>()
                    val groupData = response.body().`object`.groupchat ?: CommonData()

                    items.addItems(response.body().`object`.ls)

                    items.mapTo(imgs) { BaseHttp.baseImg + it.user_head }
                    RongIM.getInstance().refreshGroupInfoCache(
                        Group(
                            groupId,
                            groupData.groupchatName,
                            Uri.parse(imgs.joinToString(","))
                        )
                    )

                    items.forEach {
                        RongIM.getInstance().refreshGroupUserInfoCache(
                            GroupUserInfo(
                                groupId,
                                it.user_info_id,
                                it.nick_name
                            )
                        )
                    }

                }

            })
    }

    override fun finish() {
        EventBus.getDefault().unregister(this@MainActivity)
        super.finish()
    }

    @Subscribe
    fun onMessageEvent(event: RefreshMessageEvent) {
        when (event.type) {
            "用户信息" -> getUserData(event.id)
            "群组信息" -> getGroupData(event.id)
            "群组成员" -> getUserData(event.id, event.name)
        }
    }

}
