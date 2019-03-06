package tv.danmaku.ijk.media.cache;

import com.danikula.videocache.headers.HeaderInjector;

import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.utils.Debuger;

/**
 * for android video cache header
 */
public class ProxyCacheUserAgentHeadersInjector implements HeaderInjector {

    public final static Map<String, String> mMapHeadData = new HashMap<>();

    @Override
    public Map<String, String> addHeaders(String url) {
        Debuger.printfLog("****** proxy addHeaders ****** " + mMapHeadData.size());
        return mMapHeadData;
    }

}