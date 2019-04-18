package com.meida.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gyf.immersionbar.ImmersionBar;
import com.lzy.okgo.OkGo;
import com.meida.uswing.R;
import com.meida.utils.ActivityStack;

import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimAdapterEx;

public class BaseActivity extends AppCompatActivity implements
        TextWatcher,
        View.OnClickListener,
        RadioGroup.OnCheckedChangeListener,
        CompoundButton.OnCheckedChangeListener {

    private Toolbar toolbar;
    public TextView tvRight, tvTitle, btRight;
    public ImageView ivBack, ivRight;
    /**
     * 上下文context
     */
    public Activity baseContext;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);

        transparentStatusBar(true, true);

        initToolbar();
        baseContext = this;
        ActivityStack.Companion.getScreenManager().pushActivity(this);
    }

    public void setSuperContentView(int layoutId) {
        super.setContentView(layoutId);
    }

    /**
     * 设置沉浸状态栏和状态栏字体颜色
     *
     * @param isFit      是否沉浸，true不沉浸，false沉浸
     * @param isDarkFont 状态栏字体是否深色，true深色，false亮色
     */
    public void transparentStatusBar(boolean isFit, boolean isDarkFont) {
        if (isFit) {
            ImmersionBar.with(this)
                    .fitsSystemWindows(true)                               //是否沉浸
                    .statusBarColor(R.color.colorPrimaryDark)              //状态栏颜色
                    .statusBarDarkFont(isDarkFont, isDarkFont ? 0.2f : 0f) //状态栏字体是否深色
                    .init();
        } else {
            ImmersionBar.with(this)
                    .fitsSystemWindows(false)                              //是否沉浸
                    .transparentStatusBar()                                //透明状态栏
                    .statusBarDarkFont(isDarkFont, isDarkFont ? 0.2f : 0f) //状态栏字体是否深色
                    .init();
        }
    }

    /**
     * 设置沉浸状态栏，状态栏字体颜色
     *
     * @param isDarkFont 状态栏字体是否深色，true深色，false亮色
     */
    public void transparentStatusBar(boolean isDarkFont) {
        transparentStatusBar(false, isDarkFont);
    }

    /**
     * 设置沉浸状态栏，状态栏字体为亮色
     */
    public void transparentStatusBar() {
        transparentStatusBar(false);
    }

    /**
     * 沉浸状态栏（字体为亮色），设置Toolbar是否可见
     *
     * @param isVisible Toolbar是否可见，true可见，false不可见
     */
    public void setTransparentAndToobar(boolean isVisible) {
        setTransparentAndToobar(false, isVisible);
    }

    /**
     * 沉浸状态栏，设置状态栏字体颜色和Toolbar是否可见
     *
     * @param isDarkFont 状态栏字体是否深色，true深色，false亮色
     * @param isVisible  Toolbar是否可见，true可见，false不可见
     */
    public void setTransparentAndToobar(boolean isDarkFont, boolean isVisible) {
        transparentStatusBar(isDarkFont);
        setToolbarVisibility(isVisible);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        }
    }

    //初始化控件
    public void init_title() {
        ivBack = findViewById(R.id.iv_nav_back);
        ivRight = findViewById(R.id.iv_nav_right);
        tvTitle = findViewById(R.id.tv_nav_title);
        tvRight = findViewById(R.id.tv_nav_right);
        btRight = findViewById(R.id.btn_nav_right);
    }

    //初始化控件，改变中间标题
    public void init_title(String title) {
        init_title();
        changeTitle(title);
    }

    //初始化控件，改变中间和右侧标题
    public void init_title(String title, String name) {
        init_title();
        changeTitle(title, name);
    }

    //改变中间标题
    public void changeTitle(String title) {
        if (tvTitle == null) tvTitle = findViewById(R.id.tv_nav_title);
        assert tvTitle != null;
        tvTitle.setText(title);
    }

    //改变中间和右侧标题
    public void changeTitle(String title, String name) {
        changeTitle(title);

        if (tvRight == null) tvRight = findViewById(R.id.tv_nav_right);
        if (name == null) {
            assert tvRight != null;
            tvRight.setVisibility(View.INVISIBLE);
        } else {
            assert tvRight != null;
            tvRight.setVisibility(View.VISIBLE);
            tvRight.setText(name);
        }
    }

    //设置Toolbar是否可见
    public void setToolbarVisibility(boolean isVisible) {
        if (toolbar != null) toolbar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.divider).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setContentView(int layoutId) {
        setContentView(View.inflate(this, layoutId, null));
    }

    @Override
    public void setContentView(View view) {
        LinearLayout rootLayout = findViewById(R.id.content_layout);
        if (rootLayout == null) return;
        rootLayout.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initToolbar();
    }

    //标题栏的返回按钮，onclick = "doClick"
    public void doClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.iv_nav_back:
                onBackPressed();
                break;
        }
    }

    //网络数据请求方法
    public void getData() { }

    public void getData(int pindex) { }

    public void getData(int pindex, boolean isLoading) { }

    //隐藏键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im == null) return;
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    protected void onDestroy() {
        OkGo.getInstance().cancelTag(this);
        super.onDestroy(); }

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

    @Override
    public void onClick(View v) { }

}
