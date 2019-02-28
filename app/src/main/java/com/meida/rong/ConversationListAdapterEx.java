package com.meida.rong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lqr.ninegridimageview.LQRNineGridImageView;
import com.lqr.ninegridimageview.LQRNineGridImageViewAdapter;
import com.meida.uswing.R;

import java.util.ArrayList;
import java.util.Arrays;

import io.rong.common.RLog;
import io.rong.eventbus.EventBus;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.Event;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.ProviderContainerView;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public class ConversationListAdapterEx extends ConversationListAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private OnPortraitItemClick mOnPortraitItemClick;

    public ConversationListAdapterEx(Context context) {
        super(context);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    @Override
    protected View newView(Context context, int i, ViewGroup viewGroup) {
        View result = mInflater.inflate(R.layout.rc_item_group_conversation_ex, null);
        ViewHolder holder = new ViewHolder();
        holder.layout = findViewById(result, R.id.rc_item_conversation);
        holder.leftImageLayout = findViewById(result, R.id.rc_item1);
        holder.rightImageLayout = findViewById(result, R.id.rc_item2);
        holder.leftUnReadView = findViewById(result, R.id.rc_unread_view_left);
        holder.rightUnReadView = findViewById(result, R.id.rc_unread_view_right);
        holder.leftImageView = findViewById(result, R.id.rc_left);
        holder.rightImageView = findViewById(result, R.id.rc_right);
        holder.contentView = findViewById(result, R.id.rc_content);
        holder.unReadMsgCount = findViewById(result, R.id.rc_unread_message);
        holder.unReadMsgCountRight = findViewById(result, R.id.rc_unread_message_right);
        holder.unReadMsgCountIcon = findViewById(result, R.id.rc_unread_message_icon);
        holder.unReadMsgCountRightIcon = findViewById(result, R.id.rc_unread_message_icon_right);
        result.setTag(holder);
        return result;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void bindView(View v, int position, UIConversation data) {
        ViewHolder holder = (ViewHolder) v.getTag();
        if (data != null) {
            IContainerItemProvider provider = RongContext.getInstance().getConversationTemplate(data.getConversationType().getName());

            if (provider == null) RLog.e("ConversationListAdapter", "provider is null");
            else {
                View view = holder.contentView.inflate(provider);
                //noinspection unchecked
                provider.bindView(view, position, data);

                if (data.isTop()) {
                    //noinspection deprecation
                    holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_item_top_list_selector));
                } else {
                    //noinspection deprecation
                    holder.layout.setBackgroundDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_item_list_selector));
                }

                ConversationProviderTag tag = RongContext.getInstance().getConversationProviderTag(data.getConversationType().getName());
                int defaultId;

                if (data.getConversationType().equals(Conversation.ConversationType.GROUP)) {
                    defaultId = R.mipmap.default_logo;
                } else if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION)) {
                    defaultId = R.mipmap.default_logo;
                } else {
                    defaultId = R.mipmap.default_logo;
                }

                if (tag.portraitPosition() == 1) {
                    holder.leftImageLayout.setVisibility(View.VISIBLE);
                    holder.leftImageLayout.setOnClickListener(v1 -> {
                        if (mOnPortraitItemClick != null) {
                            mOnPortraitItemClick.onPortraitItemClick(v1, data);
                        }

                    });
                    holder.leftImageLayout.setOnLongClickListener(v12 -> {
                        if (mOnPortraitItemClick != null) {
                            mOnPortraitItemClick.onPortraitItemLongClick(v12, data);
                        }

                        return true;
                    });

                    ArrayList<String> imgs = new ArrayList<>();
                    LQRNineGridImageViewAdapter adapter = new LQRNineGridImageViewAdapter<String>() {
                        @Override
                        protected void onDisplayImage(Context context, ImageView imageView, String url) {
                            Glide.with(context).load(url)
                                    .apply(RequestOptions.centerCropTransform()
                                            .placeholder(R.mipmap.default_logo)
                                            .error(R.mipmap.default_logo)
                                            .dontAnimate())
                                    .into(imageView);
                        }
                    };

                    if (data.getConversationGatherState()) {
                        imgs.add("");
                        holder.leftImageView.setAdapter(adapter);
                        holder.leftImageView.setImagesData(imgs);
                    } else if (data.getIconUrl() != null) {
                        String[] urls = data.getIconUrl().toString().split(",");
                        imgs.addAll(Arrays.asList(urls));
                        holder.leftImageView.setAdapter(adapter);
                        holder.leftImageView.setImagesData(imgs);
                    } else {
                        imgs.add("");
                        holder.leftImageView.setAdapter(adapter);
                        holder.leftImageView.setImagesData(imgs);
                    }

                    if (data.getUnReadMessageCount() > 0) {
                        holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                        this.setUnReadViewLayoutParams(holder.leftUnReadView, data.getUnReadType());
                        if (data.getUnReadType().equals(UIConversation.UnreadRemindType.REMIND_WITH_COUNTING)) {
                            if (data.getUnReadMessageCount() > 99) {
                                holder.unReadMsgCount.setText(this.mContext.getResources().getString(R.string.rc_message_unread_count));
                            } else {
                                holder.unReadMsgCount.setText(Integer.toString(data.getUnReadMessageCount()));
                            }

                            holder.unReadMsgCount.setVisibility(View.VISIBLE);
                            holder.unReadMsgCountIcon.setImageResource(R.drawable.rc_unread_count_bg);
                        } else {
                            holder.unReadMsgCount.setVisibility(View.GONE);
                            holder.unReadMsgCountIcon.setImageResource(R.drawable.rc_unread_remind_list_count);
                        }
                    } else {
                        holder.unReadMsgCountIcon.setVisibility(View.GONE);
                        holder.unReadMsgCount.setVisibility(View.GONE);
                    }

                    holder.rightImageLayout.setVisibility(View.GONE);
                } else if (tag.portraitPosition() == 2) {
                    holder.rightImageLayout.setVisibility(View.VISIBLE);
                    holder.rightImageLayout.setOnClickListener(v13 -> {
                        if (mOnPortraitItemClick != null) {
                            mOnPortraitItemClick.onPortraitItemClick(v13, data);
                        }

                    });
                    holder.rightImageLayout.setOnLongClickListener(v14 -> {
                        if (mOnPortraitItemClick != null) {
                            mOnPortraitItemClick.onPortraitItemLongClick(v14, data);
                        }

                        return true;
                    });
                    if (data.getConversationGatherState()) {
                        holder.rightImageView.setAvatar(null, defaultId);
                    } else if (data.getIconUrl() != null) {
                        holder.rightImageView.setAvatar(data.getIconUrl().toString(), defaultId);
                    } else {
                        holder.rightImageView.setAvatar(null, defaultId);
                    }

                    if (data.getUnReadMessageCount() > 0) {
                        holder.unReadMsgCountRightIcon.setVisibility(View.VISIBLE);
                        this.setUnReadViewLayoutParams(holder.rightUnReadView, data.getUnReadType());
                        if (data.getUnReadType().equals(UIConversation.UnreadRemindType.REMIND_WITH_COUNTING)) {
                            holder.unReadMsgCount.setVisibility(View.VISIBLE);
                            if (data.getUnReadMessageCount() > 99) {
                                holder.unReadMsgCountRight.setText(this.mContext.getResources().getString(R.string.rc_message_unread_count));
                            } else {
                                holder.unReadMsgCountRight.setText(Integer.toString(data.getUnReadMessageCount()));
                            }

                            holder.unReadMsgCountRightIcon.setImageResource(R.drawable.rc_unread_count_bg);
                        } else {
                            holder.unReadMsgCount.setVisibility(View.GONE);
                            holder.unReadMsgCountRightIcon.setImageResource(R.drawable.rc_unread_remind_without_count);
                        }
                    } else {
                        holder.unReadMsgCountIcon.setVisibility(View.GONE);
                        holder.unReadMsgCount.setVisibility(View.GONE);
                    }

                    holder.leftImageLayout.setVisibility(View.GONE);
                } else {
                    if (tag.portraitPosition() != 3) {
                        throw new IllegalArgumentException("the portrait position is wrong!");
                    }

                    holder.rightImageLayout.setVisibility(View.GONE);
                    holder.leftImageLayout.setVisibility(View.GONE);
                }

                MessageContent content = data.getMessageContent();
                if (content != null && content.isDestruct()) {
                    RongIMClient.getInstance().getMessage(data.getLatestMessageId(), new RongIMClient.ResultCallback<Message>() {
                        public void onSuccess(Message message) {
                            if (message == null) {
                                //noinspection RedundantArrayCreation
                                EventBus.getDefault().post(new Event.MessageDeleteEvent(new int[]{data.getLatestMessageId()}));
                            } else if (message.getReadTime() > 0L) {
                                long readTime = message.getReadTime();
                                long serverTime = System.currentTimeMillis() - RongIMClient.getInstance().getDeltaTime();
                                long delay = message.getContent().getDestructTime() - (serverTime - readTime) / 1000L;
                                if (delay > 0L) {
                                    RongIM.getInstance().createDestructionTask(message, null, ConversationListFragment.TAG);
                                } else {
                                    EventBus.getDefault().post(new Event.DestructionEvent(message));
                                }
                            }

                        }

                        public void onError(RongIMClient.ErrorCode e) {
                        }
                    });
                }

            }
        }
    }

    public void setOnPortraitItemClick(ConversationListAdapter.OnPortraitItemClick onPortraitItemClick) {
        this.mOnPortraitItemClick = onPortraitItemClick;
    }

    protected class ViewHolder {
        public View layout;
        View leftImageLayout;
        View rightImageLayout;
        View leftUnReadView;
        View rightUnReadView;
        LQRNineGridImageView leftImageView;
        TextView unReadMsgCount;
        ImageView unReadMsgCountIcon;
        AsyncImageView rightImageView;
        TextView unReadMsgCountRight;
        ImageView unReadMsgCountRightIcon;
        ProviderContainerView contentView;
    }

}
