/**
 * created by 小卷毛, 2019/02/21
 * Copyright (c) 2019, 416143467@qq.com All Rights Reserved.
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG             #
 * #                                                   #
 */
package com.meida.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.LoopPagerAdapter;
import com.meida.share.BaseHttp;
import com.meida.uswing.R;
import com.sunfusheng.GlideImageView;

import java.util.ArrayList;
import java.util.List;

public class LoopAdapter extends LoopPagerAdapter {

    private Context context;
    private List<String> imgs = new ArrayList<>();

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
        notifyDataSetChanged();
    }

    public LoopAdapter(Context context, RollPagerView viewPager) {
        super(viewPager);
        this.context = context;
    }

    @Override
    public View getView(ViewGroup container, int position) {
        View view = View.inflate(context, R.layout.item_banner_img, null);
        GlideImageView iv_img = view.findViewById(R.id.iv_banner_img);

        iv_img.load(BaseHttp.INSTANCE.getBaseImg() + imgs.get(position), R.mipmap.default_img);

        return view;
    }

    @Override
    public int getRealCount() {
        return imgs.size();
    }

}
