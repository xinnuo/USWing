package com.meida;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

public class CustomExtensionModule extends DefaultExtensionModule {

    /**
     * 返回需要展示的 plugin 列表
     */
    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        return super.getPluginModules(conversationType);
    }

    /**
     * 返回需要展示的 EmoticonTab 列表
     */
    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }

}
