package tv.danmaku.ijk.media.listener;


import java.io.File;

/**
 * 截屏保存结果
 */
public interface IVideoShotSaveListener {

    void result(boolean success, File file);

}
