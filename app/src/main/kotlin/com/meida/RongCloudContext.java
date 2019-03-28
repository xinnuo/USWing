package com.meida;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.meida.model.RefreshMessageEvent;
import com.meida.share.BaseHttp;
import com.meida.uswing.ConversationVideoActivity;
import com.meida.uswing.ConversationWebActivity;
import com.meida.uswing.LoginActivity;
import com.meida.uswing.R;
import com.meida.utils.PreferencesUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * 描述：融云相关监听 事件集合类
 */
public class RongCloudContext implements
        RongIM.ConversationListBehaviorListener, //设置会话列表界面操作的监听器
        RongIM.ConversationClickListener,        //设置会话界面操作的监听器
        RongIMClient.OnReceiveMessageListener,   //设置接收消息的监听器
        RongIM.OnSendMessageListener,            //设置发送消息的监听
        RongIMClient.ConnectionStatusListener,   //设置连接状态变化的监听器
        RongIM.LocationProvider,                 //位置信息的提供者，实现后获取用户位置信息
        RongIM.UserInfoProvider,                 //设置用户信息的提供者，供RongIM调用获取用户名称和头像信息
        RongIM.GroupInfoProvider,                //群组信息的提供者
        RongIM.GroupUserInfoProvider             //群组中用户信息的提供者
{

    private static final String TAG = RongCloudContext.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static RongCloudContext mRongCloudInstance;
    private Context mContext;
    private OnMessageExtraListener listener;

    private NotificationManager manager;
    private static final int NOTIFICATION_REQUEST = 130;
    private static final String CHANNEL_ONE_ID = "com.meida.rong";
    private static final String CHANNEL_ONE_NAME = "RongMessageForengound";

    private RongCloudContext(Context mContext) {
        this.mContext = mContext;
        initListener();
    }

    /**
     * 初始化 RongCloud.
     */
    public static void init(Context context) {
        if (mRongCloudInstance == null) {
            synchronized (RongCloudContext.class) {
                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new RongCloudContext(context);
                }
            }
        }
    }

    /**
     * 获取RongCloud 实例。
     */
    public static RongCloudContext getInstance() {
        return mRongCloudInstance;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        RongIM.setConversationListBehaviorListener(this);  //设置会话列表界面操作的监听器
        RongIM.setConversationClickListener(this);         //设置会话界面操作的监听器
        RongIM.setOnReceiveMessageListener(this);          //设置接收消息的监听器
        RongIM.getInstance().setSendMessageListener(this); //设置发送消息的监听
        RongIM.setConnectionStatusListener(this);          //设置连接状态变化的监听器

        setCustomExtensionModule(); //自定义输入区域扩展栏

        Conversation.ConversationType[] types = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.DISCUSSION
        };
        RongIM.getInstance().setReadReceiptConversationTypeList(types); //设置发送消息回执的会话类型
    }

    /**
     * connect 后能设置的监听
     */
    public void connectedListener() {
        /*
         * 设置会话界面未读新消息是否展示 注:未读新消息大于1条即展示。
         *
         * @param state true 展示，false 不展示。
         */
        RongIM.getInstance().enableNewComingMessageIcon(true);

        /*
         * 设置会话界面历史消息是否展示 注:历史消息大于10条即展示。
         *
         * @param state true 展示，false 不展示。
         */
        RongIM.getInstance().enableUnreadMessageIcon(true);

        /*
         * 设置当前用户信息。<br>
         * 如果开发者没有实现用户信息提供者，而是使用消息携带用户信息，
         * 需要使用这个方法设置当前用户的信息， 然后在init(Context)之后调用setMessageAttachedUserInfo(boolean)，
         * 这样可以在每条消息中携带当前用户的信息，IMKit会在接收到消息的时候取出用户信息并刷新到界面上。
         *
         * 注：设置当前用户信息时，登录需返回用户信息，且更改名称和头像时重新调用该方法。
         *
         * @param userInfo 当前用户信息。
         */
        RongIM.getInstance().setCurrentUserInfo(
                new UserInfo(
                        PreferencesUtils.getString(mContext, "token"),
                        PreferencesUtils.getString(mContext, "nickName"),
                        Uri.parse(BaseHttp.INSTANCE.getBaseImg() + PreferencesUtils.getString(mContext, "userHead")))
        );

        /*
         * 设置消息体内是否携带用户信息。
         *
         * @param state 是否携带用户信息，true 携带，false 不携带。
         */
        RongIM.getInstance().setMessageAttachedUserInfo(true); //设置消息体内是否携带用户信息

        /*
         * 设置地理位置提供者。
         *
         * @param locationProvider 位置信息提供者。
         */
        RongIM.setLocationProvider(this);

        /*
         * 设置用户信息的提供者，供 RongIM 调用获取用户名称和头像信息。
         *
         * @param userInfoProvider 用户信息提供者。
         * @param isCacheUserInfo  设置是否由 IMKit 来缓存用户信息。<br>
         *                         如果 App 提供的 UserInfoProvider
         *                         每次都需要通过网络请求用户数据，而不是将用户数据缓存到本地内存，会影响用户信息的加载速度；<br>
         *                         此时最好将本参数设置为 true，由 IMKit 将用户信息缓存到本地内存中。
         */
        RongIM.setUserInfoProvider(this, true);

        /*
         * 设置群组信息的提供者。
         *
         * @param groupInfoProvider 群组信息提供者。
         * @param isCacheGroupInfo  设置是否由 IMKit 来缓存用户信息。<br>
         *                          如果 App 提供的 GroupInfoProvider。
         *                          每次都需要通过网络请求群组数据，而不是将群组数据缓存到本地，会影响群组信息的加载速度；<br>
         *                          此时最好将本参数设置为 true，由 IMKit 来缓存群组信息。
         */
        RongIM.setGroupInfoProvider(this, true);

        /*
         * 设置GroupUserInfo提供者，供RongIM 调用获取GroupUserInfo。
         *
         * @param userInfoProvider 群组用户信息提供者。
         * @param isCacheUserInfo  设置是否由 IMKit 来缓存 GroupUserInfo。<br>
         *                         如果 App 提供的 GroupUserInfoProvider。
         *                         每次都需要通过网络请求数据，而不是将数据缓存到本地，会影响信息的加载速度；<br>
         *                         此时最好将本参数设置为 true，由 IMKit 来缓存信息。
         */
        RongIM.setGroupUserInfoProvider(this, true); //群组中用户信息的提供者
    }

    /**
     * 自定义输入区域扩展栏
     * <p>
     * 可以通过更改 rc_fr_conversation.xml 里 app:RCStyle="SCE" ，更改默认输入显示形式
     */
    public void setCustomExtensionModule() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            if (defaultModule != null) {
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
                RongExtensionManager.getInstance().registerExtensionModule(new CustomExtensionModule());
            }
        }
    }

    public void setDefaultExtensionModule() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule currentModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module != null)
                    RongExtensionManager.getInstance().unregisterExtensionModule(currentModule);
            }

            RongExtensionManager.getInstance().registerExtensionModule(new DefaultExtensionModule());
        }
    }

    /**
     * 当点击会话头像后执行。
     *
     * @param context          上下文。
     * @param conversationType 会话类型。
     * @param targetId         被点击的用户id。
     * @return 如果用户自己处理了点击后的逻辑处理，则返回 true，否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String targetId) {
        return false;
    }

    /**
     * 当长按会话头像后执行。
     *
     * @param context          上下文。
     * @param conversationType 会话类型。
     * @param targetId         被点击的用户id。
     * @return 如果用户自己处理了点击后的逻辑处理，则返回 true，否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String targetId) {
        return false;
    }

    /**
     * 长按会话列表中的 item 时执行。
     *
     * @param context        上下文。
     * @param view           触发点击的 View。
     * @param uiConversation 长按时的会话条目。
     * @return 如果用户自己处理了长按会话后的逻辑处理，则返回 true， 否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    /**
     * 点击会话列表中的 item 时执行。
     *
     * @param context        上下文。
     * @param view           触发点击的 View。
     * @param uiConversation 会话条目。
     * @return 如果用户自己处理了点击会话后的逻辑处理，则返回 true， 否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    /**
     * 当点击用户头像后执行。
     *
     * @param context          上下文。
     * @param conversationType 会话类型。
     * @param userInfo         被点击的用户的信息。
     * @param targetId         会话 id。
     * @return 如果用户自己处理了点击后的逻辑，则返回 true，否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String targetId) {
        return false;
    }

    /**
     * 当长按用户头像后执行。
     *
     * @param context          上下文。
     * @param conversationType 会话类型。
     * @param userInfo         被点击的用户的信息。
     * @param targetId         会话 id。
     * @return 如果用户自己处理了点击后的逻辑，则返回 true，否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String targetId) {
        return false;
    }

    /**
     * 当点击消息时执行。
     *
     * @param context 上下文。
     * @param view    触发点击的 View。
     * @param message 被点击的消息的实体信息。
     * @return 如果用户自己处理了点击后的逻辑，则返回 true， 否则返回 false, false 走融云默认处理方式。
     */
    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        if (message.getContent() instanceof RichContentMessage) {
            RichContentMessage contentMessage = (RichContentMessage) message.getContent();

            Intent intent = new Intent(mContext, ConversationVideoActivity.class);
            intent.putExtra("videoId", contentMessage.getExtra());
            intent.putExtra("url", contentMessage.getUrl());
            intent.putExtra("videoImg", contentMessage.getImgUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * 当点击链接消息时执行
     *
     * @param context 上下文。
     * @param link    被点击的链接。
     * @param message 被点击的消息的实体信息。
     * @return 如果用户自己处理了点击后的逻辑处理，则返回 true， 否则返回 false, false 走融云默认处理方式。
     */
    @Override
    public boolean onMessageLinkClick(Context context, String link, Message message) {
        return false;
    }

    /**
     * 当长按消息时执行。
     *
     * @param context 上下文。
     * @param view    触发点击的 View。
     * @param message 被长按的消息的实体信息。
     * @return 如果用户自己处理了长按后的逻辑，则返回 true，否则返回 false，false 走融云默认处理方式。
     */
    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    /**
     * 退出应用时，清除通知栏消息
     */
    public void clearNotificationMessage() {
        if (manager != null) manager.cancelAll();
    }

    /**
     * 收到消息的处理。
     *
     * @param message 收到的消息实体。
     * @param left    剩余未拉取消息数目。
     * @return 收到消息是否处理完成，true 表示自己处理铃声和后台通知，false 走融云默认处理方式。
     */
    @Override
    public boolean onReceived(Message message, int left) {
        String content = "";
        String tagetId = message.getTargetId();

        MessageContent messageContent = message.getContent();

        if (messageContent instanceof TextMessage) { //文本消息
            TextMessage textMessage = (TextMessage) messageContent;
            textMessage.getExtra();
            content = textMessage.getContent();

            Log.i(TAG, "onReceived-TextMessage:" + textMessage.getContent());
        } else if (messageContent instanceof ImageMessage) { //图片消息
            ImageMessage imageMessage = (ImageMessage) messageContent;
            content = "[图片]";

            Log.i(TAG, "onReceived-ImageMessage:" + imageMessage.getRemoteUri());
        } else if (messageContent instanceof VoiceMessage) { //语音消息
            VoiceMessage voiceMessage = (VoiceMessage) messageContent;
            content = "[语音]";

            Log.i(TAG, "onReceived-voiceMessage:" + voiceMessage.getUri().toString());
        } else if (messageContent instanceof LocationMessage) { //位置消息
            LocationMessage locationMessage = (LocationMessage) messageContent;
            content = "[位置]";

            Log.i(TAG, "onReceived-locationMessage:" + locationMessage.getImgUri().toString());
        } else if (messageContent instanceof FileMessage) { //文件消息
            FileMessage fileMessage = (FileMessage) messageContent;
            content = "[文件]";

            Log.i(TAG, "onReceived-fileMessage:" + fileMessage.getFileUrl().toString());
        } else if (messageContent instanceof RichContentMessage) { //图文消息
            RichContentMessage richContentMessage = (RichContentMessage) messageContent;
            content = "[图文]";

            Log.i(TAG, "onReceived-RichContentMessage:" + richContentMessage.getContent());
        } else if (messageContent instanceof InformationNotificationMessage) { //小灰条消息
            InformationNotificationMessage informationNotificationMessage = (InformationNotificationMessage) messageContent;
            content = "[小灰条]";

            Log.i(TAG, "onReceived-informationNotificationMessage:" + informationNotificationMessage.getMessage());
        } else {
            Log.i(TAG, "onReceived-其他消息，自己来判断处理");
        }

        String contentTitle;
        String extra = message.getExtra();
        if (extra != null && !TextUtils.isEmpty(extra)) contentTitle = extra;
        else {
            //判断是否携带用户信息
            if (messageContent.getUserInfo() == null) contentTitle = "对方消息";
            else contentTitle = messageContent.getUserInfo().getName();
        }

        //在Android进行通知处理，首先需要重系统哪里获得通知管理器NotificationManager，它是一个系统Service
        if (manager == null)
            manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        //Android O设置channelId.
        NotificationChannel notificationChannel;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(notificationChannel);
        }

        //通过Notification.Builder来创建通知
        NotificationCompat.Builder notify = new NotificationCompat.Builder(mContext, CHANNEL_ONE_ID)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                //设置状态栏中的小图片，这个图片同样也是在下拉状态栏中所显示，需要更换更大的图片，可以使用setLargeIcon(Bitmap icon)
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置在status bar上显示的提示文字
                .setTicker("您有一条消息")
                //设置在下拉status bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentTitle(contentTitle)
                //TextView中显示的详细内容
                .setContentText(content)
                //是否可清除
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        Intent intent = new Intent();

        if (RongContext.getInstance() != null) {
            Uri uri = Uri.parse("rong://" + mContext.getApplicationInfo().packageName)
                    .buildUpon()
                    .appendPath("conversation")
                    .appendPath(message.getConversationType().getName().toLowerCase())
                    .appendQueryParameter("targetId", tagetId)
                    .appendQueryParameter("title", contentTitle)
                    .build();
            intent.setData(uri);
            intent.setAction("android.intent.action.VIEW");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NOTIFICATION_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notify.setContentIntent(pendingIntent);
        manager.notify(Math.abs(messageContent.getUserInfo() == null ?
                tagetId.hashCode() :
                messageContent.getUserInfo().getUserId().hashCode()), notify.build());

        return true;
    }


    /**
     * 消息发送前监听器处理接口（是否发送成功可以从 SentStatus 属性获取）。
     *
     * @param message 发送的消息实例。
     * @return 处理后的消息实例。
     */
    @Override
    public Message onSend(Message message) {
        if (listener != null) listener.onExtra(message);
        return message;
    }

    /**
     * 消息在 UI 展示后执行/自己的消息发出后执行,无论成功或失败。
     *
     * @param message              消息实例。
     * @param sentMessageErrorCode 发送消息失败的状态码，消息发送成功 SentMessageErrorCode 为 null。
     * @return true 表示走自己的处理方式，false 走融云默认处理方式。
     */
    @Override
    public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
        if (message.getSentStatus() == Message.SentStatus.FAILED) {
            switch (sentMessageErrorCode) {
                case NOT_IN_CHATROOM:       //不在聊天室
                    break;
                case NOT_IN_DISCUSSION:     //不在讨论组
                    break;
                case NOT_IN_GROUP:          //不在群组
                    break;
                case REJECTED_BY_BLACKLIST: //你在他的黑名单中
                    break;
            }
        }
        return false;
    }


    /**
     * 连接状态监听器，以获取连接相关状态:ConnectionStatusListener 的回调方法，网络状态变化时执行。
     *
     * @param connectionStatus 网络状态。
     */
    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case CONNECTED:
                Log.i(TAG, "onChanged: 连接成功");
                break;
            case DISCONNECTED:
                Log.i(TAG, "onChanged: 断开连接");
                break;
            case CONNECTING:
                Log.i(TAG, "onChanged: 连接中");
                break;
            case NETWORK_UNAVAILABLE:
                Log.i(TAG, "onChanged: 网络不可用");
                break;
            case KICKED_OFFLINE_BY_OTHER_CLIENT:
                Log.i(TAG, "onChanged: 用户账户在其他设备登录");

                Intent intent = new Intent(mContext, LoginActivity.class);
                intent.putExtra("offLine", true);
                intent.putExtra("isToast", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                break;
        }
    }


    /**
     * 位置信息提供者:LocationProvider 的回调方法，打开第三方地图页面。
     *
     * @param context          上下文
     * @param locationCallback 回调
     */
    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) { }


    @Override
    public UserInfo getUserInfo(String userId) {
        RefreshMessageEvent event = new RefreshMessageEvent();
        event.setType("用户信息");
        event.setId(userId);
        EventBus.getDefault().post(event);
        return null;
    }


    @Override
    public Group getGroupInfo(String groupId) {
        RefreshMessageEvent event = new RefreshMessageEvent();
        event.setType("群组信息");
        event.setId(groupId);
        EventBus.getDefault().post(event);
        return null;
    }


    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
        RefreshMessageEvent event = new RefreshMessageEvent();
        event.setType("群组成员");
        event.setId(userId);
        event.setName(groupId);
        EventBus.getDefault().post(event);
        return null;
    }

    public void setOnMessageExtraListener(OnMessageExtraListener listener) {
        this.listener = listener;
    }

    public interface OnMessageExtraListener {
        void onExtra(Message message);
    }

}
