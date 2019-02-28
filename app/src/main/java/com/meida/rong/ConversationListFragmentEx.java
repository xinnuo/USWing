package com.meida.rong;

import android.content.Context;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

public class ConversationListFragmentEx extends ConversationListFragment {

    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new ConversationListAdapterEx(context);
    }

}
