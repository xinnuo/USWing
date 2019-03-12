package com.meida.exo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.model.VideoModel;

/**
 * 自定义列表数据model
 */
public class ExoModel extends VideoModel {

    List<String> urls;

    public ExoModel(List<String> urls, Map<String, String> mapHeadData, boolean loop, float speed, boolean cache, File cachePath) {
        super("", mapHeadData, loop, speed, cache, cachePath, "");
        if (urls == null) this.urls = new ArrayList<>();
        else this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

}
