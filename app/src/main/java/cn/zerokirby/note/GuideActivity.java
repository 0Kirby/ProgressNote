package cn.zerokirby.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

/*
 *  描述： 引导页
 */
public class GuideActivity extends BaseActivity implements View.OnClickListener {

    private int page;
    private boolean mIsScrolled; //  viewpager是否处于惯性滑动
    private ViewPager mViewPager;
    //容器
    private List<View> mList = new ArrayList<>();
    private View view1, view2, view3, view4, view5, view6;
    //小圆点
    private ImageView point1, point2, point3, point4, point5, point6;
    //跳过
    private Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        initView();
    }

    //初始化View
    private void initView() {


        point1 = findViewById(R.id.point1);
        point2 = findViewById(R.id.point2);
        point3 = findViewById(R.id.point3);
        point4 = findViewById(R.id.point4);
        point5 = findViewById(R.id.point5);
        point6 = findViewById(R.id.point6);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        //设置默认图片
        setPointImg(true, false, false, false, false, false);
        mViewPager = findViewById(R.id.mViewPager);
        view1 = View.inflate(this, R.layout.pager_item_1, null);
        view2 = View.inflate(this, R.layout.pager_item_2, null);
        view3 = View.inflate(this, R.layout.pager_item_3, null);
        view4 = View.inflate(this, R.layout.pager_item_4, null);
        view5 = View.inflate(this, R.layout.pager_item_5, null);
        view6 = View.inflate(this, R.layout.pager_item_6, null);
        view6.findViewById(R.id.btn_start).setOnClickListener(this);

        mList.add(view1);
        mList.add(view2);
        mList.add(view3);
        mList.add(view4);
        mList.add(view5);
        mList.add(view6);

        //设置适配器
        mViewPager.setAdapter(new GuideAdapter());


        //监听ViewPager滑动
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //pager切换
            @Override
            public void onPageSelected(int position) {//设置圆点
                page = position;//记录页码
                switch (position) {
                    case 0:
                        setPointImg(true, false, false, false, false, false);
                        btn_back.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        setPointImg(false, true, false, false, false, false);
                        btn_back.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        setPointImg(false, false, true, false, false, false);
                        btn_back.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        setPointImg(false, false, false, true, false, false);
                        btn_back.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        setPointImg(false, false, false, false, true, false);
                        btn_back.setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        setPointImg(false, false, false, false, false, true);
                        btn_back.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {

                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mIsScrolled = false;
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        mIsScrolled = true;
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        if (page == mList.size() - 1 && !mIsScrolled) {//到最后一页时滑动进入主界面
                            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            finish();
                        }
                        mIsScrolled = true;
                        break;
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
            case R.id.btn_back:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                break;
        }
    }

    //设置小圆点的选中效果
    private void setPointImg(boolean isCheck1, boolean isCheck2, boolean isCheck3, boolean isCheck4, boolean isCheck5, boolean isCheck6) {
        if (isCheck1) {
            point1.setBackgroundResource(R.drawable.point_on);
        } else {
            point1.setBackgroundResource(R.drawable.point_off);
        }

        if (isCheck2) {
            point2.setBackgroundResource(R.drawable.point_on);
        } else {
            point2.setBackgroundResource(R.drawable.point_off);
        }

        if (isCheck3) {
            point3.setBackgroundResource(R.drawable.point_on);
        } else {
            point3.setBackgroundResource(R.drawable.point_off);
        }

        if (isCheck4) {
            point4.setBackgroundResource(R.drawable.point_on);
        } else {
            point4.setBackgroundResource(R.drawable.point_off);
        }

        if (isCheck5) {
            point5.setBackgroundResource(R.drawable.point_on);
        } else {
            point5.setBackgroundResource(R.drawable.point_off);
        }

        if (isCheck6) {
            point6.setBackgroundResource(R.drawable.point_on);
        } else {
            point6.setBackgroundResource(R.drawable.point_off);
        }
    }

    class GuideAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mList.get(position));
            return mList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));
        }
    }

}
