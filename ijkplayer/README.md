
#### D、代码中的全局切换支持（更多请参看下方文档和demo）

```

//EXOPlayer内核，支持格式更多
PlayerFactory.setPlayManager(Exo2PlayerManager.class);
//系统内核模式
PlayerFactory.setPlayManager(SystemPlayerManager.class);
//ijk内核，默认模式
PlayerFactory.setPlayManager(IjkPlayerManager.class);

//exo缓存模式，支持m3u8，只支持exo
CacheFactory.setCacheManager(ExoPlayerCacheManager.class);
//代理缓存模式，支持所有模式，不支持m3u8等，默认
CacheFactory.setCacheManager(ProxyCacheManager.class);

//切换渲染模式
VideoType.setShowType(VideoType.SCREEN_MATCH_FULL);
//默认显示比例
VideoType.SCREEN_TYPE_DEFAULT = 0;
//16:9
VideoType.SCREEN_TYPE_16_9 = 1;
//4:3
VideoType.SCREEN_TYPE_4_3 = 2;
//全屏裁减显示，为了显示正常 CoverImageView 建议使用FrameLayout作为父布局
VideoType.SCREEN_TYPE_FULL = 4;
//全屏拉伸显示，使用这个属性时，surface_container建议使用FrameLayout
VideoType.SCREEN_MATCH_FULL = -4;

//切换绘制模式
VideoType.setRenderType(VideoType.SUFRACE);
VideoType.setRenderType(VideoType.GLSURFACE);
VideoType.setRenderType(VideoType.TEXTURE);

//ijk关闭log
IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);

//exoplayer自定义MediaSource
ExoSourceManager.setExoMediaSourceInterceptListener(new ExoMediaSourceInterceptListener() {
    @Override
    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir) {
        //可自定义MediaSource
        return null;
    }
});

```