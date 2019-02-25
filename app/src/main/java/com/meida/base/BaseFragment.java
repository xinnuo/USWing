package com.meida.base;

import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.lzy.okgo.OkGo;

import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimAdapterEx;

public class BaseFragment extends Fragment implements
        TextWatcher,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener {

    /**
     * RecyclerView数据管理的LayoutManager
     */
    public LinearLayoutManager linearLayoutManager;
    public GridLayoutManager gridLayoutManager;
    public StaggeredGridLayoutManager staggeredGridLayoutManager;
    /**
     * SlimAdapter的adapter
     */
    public SlimAdapter mAdapter;
    public SlimAdapterEx mAdapterEx;
    /**
     * 分页加载页数
     */
    public int pageNum = 1;
    /**
     * 是否正在上拉加载中
     */
    public boolean isLoadingMore;

    public int mPosition;

    //网络数据请求方法
    public void getData() { }

    public void getData(int pindex) { }

    public void getData(int pindex, boolean isLoading) { }

    //初始化控件
    public void init_title() { }

    @Override
    public void onDestroy() {
        OkGo.getInstance().cancelTag(this);
        super.onDestroy();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) { }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) { }
}
