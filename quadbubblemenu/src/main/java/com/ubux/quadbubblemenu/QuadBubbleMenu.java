package com.ubux.quadbubblemenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ubux.quadbubblemenu.util.UiUtil;

import java.util.ArrayList;
import java.util.List;


public class QuadBubbleMenu extends ViewGroup {

    private static final String TAG = QuadBubbleMenu.class.getSimpleName();

    private int mItemRadius = 0;
    private int mItemSpace = 0;
    private Drawable mCollapsingDrawable;
    private Drawable mExpandingDrawable;
    private int mDefaultAxisViewBgColor = 0;

    /**
     * Minimum distance from item view's center to axis view's center and minus
     * twice radius.
     */
    private float mMinDistanceToAxisView;

    private boolean mIsCollapsing = true;

    private List<View> mReusedItemViews = new ArrayList<>();

    private Animator mCollapseAnim = null;
    private Animator mExpandAnim = null;

    public QuadBubbleMenu(Context context) {
        super(context);
        defaultInitIfNeeded();
    }

    public QuadBubbleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.QuadBubbleMenu);
        mItemRadius = a.getDimensionPixelSize(R.styleable.QuadBubbleMenu_itemRadius, mItemRadius);
        mItemSpace = a.getDimensionPixelSize(R.styleable.QuadBubbleMenu_itemSpace, mItemSpace);
        mDefaultAxisViewBgColor =
                a.getColor(R.styleable.QuadBubbleMenu_axisViewColor, 0);
        a.recycle();
        defaultInitIfNeeded();
    }

    private void defaultInitIfNeeded(){
        Context context = getContext();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (mItemRadius==0) {
            mItemRadius = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    20, dm);
        }
        if (mItemSpace==0) {
            mItemSpace = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    8, dm);
        }
        if (mMinDistanceToAxisView==0) {
            mMinDistanceToAxisView =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            16, dm);
        }
        if (mCollapsingDrawable==null) {
            mCollapsingDrawable =
                    UiUtil.getDrawable(context, R.drawable.ic_quad_bubble_menu_collapsing);
        }
        if (mExpandingDrawable==null) {
            mExpandingDrawable =
                    UiUtil.getDrawable(context, R.drawable.ic_quad_bubble_menu_expanding);
        }
        if (mDefaultAxisViewBgColor==0){
            mDefaultAxisViewBgColor =
                    UiUtil.getColor(context, R.color.quad_bubble_menu_item_default);
        }

        CircleImageView view = new CircleImageView(context);
        view.setLayoutParams(new LayoutParams(2*mItemRadius,
                2*mItemRadius));
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setBackgroundColor(mDefaultAxisViewBgColor);
        view.setImageDrawable(mCollapsingDrawable);
        view.setTag(R.id.quad_bubble_menu_axis_view_tag, true);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsCollapsing = !mIsCollapsing;
                final CircleImageView civ = (CircleImageView)v;


                if (!mIsCollapsing){
                    civ.setImageDrawable(mExpandingDrawable);
                    requestLayout();
                    mExpandAnim = createExpandAnim();
                    mExpandAnim.start();
                }else {
                    mCollapseAnim = createCollapseAnim(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            civ.setImageDrawable(mCollapsingDrawable);
                            requestLayout();
                        }
                    });
                    mCollapseAnim.start();
                }
            }
        });
        addView(view);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCollapseAnim!=null && mCollapseAnim.isRunning()){
            mCollapseAnim.end();
        }
        if (mExpandAnim!=null && mExpandAnim.isRunning()){
            mExpandAnim.end();
        }
    }

    private float getMeasuredWidthWith2ItemsMore(){
        final int childCount = getChildCount();
        if (childCount<=2){
            throw new RuntimeException("Child count must be greater than 2!");
        }

        float itemCenterDist = 2*mItemRadius+mItemSpace;
        double theta = Math.toRadians(90)/(getChildCount()-2);
        float tmpEdgeSize = (float)(itemCenterDist/2/Math.sin(theta/2)+2*mItemRadius);
        float minEdgeSize = 4*mItemRadius+mMinDistanceToAxisView;

        return Float.compare(tmpEdgeSize, minEdgeSize)>0?(int)tmpEdgeSize:(int)minEdgeSize;
    }

    private AnimatorSet createExpandAnim(){
        AnimatorSet animSet = new AnimatorSet();
        int childCount = getChildCount();
        if (childCount==2) {
            View child = getChildAt(0);
            if (child.getTag(R.id.quad_bubble_menu_axis_view_tag)!=null){
                child = getChildAt(1);
            }
            ObjectAnimator anim = ObjectAnimator.ofInt(child, "translationY",
                    getMeasuredHeight()-mItemRadius, 0);
            animSet.play(anim);
        }else if (childCount>2) {
            AnimatorSet.Builder animBuilder = null;
            final float W = getMeasuredWidthWith2ItemsMore();
            final double ITEM_THETA = Math.toRadians(90)/(childCount-2);
            final double bigRadius = W-2*mItemRadius;
            double theta = 0;
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child.getTag(R.id.quad_bubble_menu_axis_view_tag) == null) {
                    ObjectAnimator trX = ObjectAnimator.ofFloat(child, "translationX",
                            (float) (bigRadius*Math.cos(theta)), 0);
                    ObjectAnimator trY = ObjectAnimator.ofFloat(child, "translationY",
                            (float)(bigRadius*Math.sin(theta)), 0);
                    if (animBuilder==null){
                        animBuilder = animSet.play(trX);
                    }else {
                        animBuilder.with(trX);
                    }
                    animBuilder.with(trY);
                    theta += ITEM_THETA;
                }
            }
        }
        animSet.setDuration(200);
        return animSet;
    }

    private AnimatorSet createCollapseAnim(Animator.AnimatorListener listener){
        AnimatorSet animSet = new AnimatorSet();
        final int childCount = getChildCount();
        if (childCount==2){
            View child = getChildAt(0);
            if (child.getTag(R.id.quad_bubble_menu_axis_view_tag)!=null){
                child = getChildAt(1);
            }
            ObjectAnimator anim = ObjectAnimator.ofInt(child, "translationY",
                    -getMeasuredHeight()-mItemRadius, 0);
            animSet.play(anim);
        }else if (childCount>2){
            AnimatorSet.Builder animBuilder = null;
            final float W = getMeasuredWidthWith2ItemsMore();
            final double ITEM_THETA = Math.toRadians(90)/(childCount-2);
            final double bigRadius = W-2*mItemRadius;
            double theta = 0;
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child.getTag(R.id.quad_bubble_menu_axis_view_tag) == null) {
                    ObjectAnimator trX = ObjectAnimator.ofFloat(child, "translationX",
                            0, (float) (bigRadius*Math.cos(theta)));
                    ObjectAnimator trY = ObjectAnimator.ofFloat(child, "translationY",
                            0, (float)(bigRadius*Math.sin(theta)));
                    if (animBuilder==null){
                        animBuilder = animSet.play(trX);
                    }else {
                        animBuilder.with(trX);
                    }
                    animBuilder.with(trY);
                    theta += ITEM_THETA;
                }
            }
        }
        animSet.setDuration(200);
        animSet.addListener(listener);
        return animSet;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        final int itemRadius = getChildAt(0).getMeasuredWidth()/2;

        if (mIsCollapsing){
            setMeasuredDimension(2*itemRadius, 2*itemRadius);
            return;
        }

        // Visibility of 'GONE' is considered to be impossible.
        if (childCount==1){
            measuredWidth = measuredHeight = 2*itemRadius;
        }else if (childCount == 2){
            measuredWidth = 2*itemRadius;
            measuredHeight = (int) (4 * itemRadius + mMinDistanceToAxisView);
        }else {
            float itemCenterDist = 2*itemRadius+mItemSpace;
            // Radians angle of two adjacent fan shapes.
            double theta = Math.toRadians(90)/(childCount-2);
            float tmpEdgeSize = (float)(itemCenterDist/2/Math.sin(theta/2)+2*itemRadius);
            float minEdgeSize = 4*itemRadius+mMinDistanceToAxisView;

            measuredWidth = measuredHeight =
                    Float.compare(tmpEdgeSize, minEdgeSize)>0?(int)tmpEdgeSize:(int)minEdgeSize;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            final int W = r-l;
            final int H = b-t;
            final int itemRadius = getChildAt(0).getMeasuredWidth()/2;
            View axisView = null;
            mReusedItemViews.clear();
            for (int i=0; i<getChildCount(); ++i){
                View view = getChildAt(i);
                if (view.getTag(R.id.quad_bubble_menu_axis_view_tag)!=null){
                    axisView = view;
                }else {
                    mReusedItemViews.add(view);
                }
            }
            //noinspection ConstantConditions
            axisView.layout(W-2*itemRadius, H-2*itemRadius, W, H);

            if (mIsCollapsing){
                return;
            }

            final int realChildCount = mReusedItemViews.size();
            if (realChildCount==1){
                mReusedItemViews.get(0).layout(W-2*itemRadius,
                        (int)(H-4*itemRadius-mMinDistanceToAxisView),
                        W, (int)(H-2*itemRadius-mMinDistanceToAxisView));
            }else if (realChildCount>1){
                final double theta = Math.toRadians(90)/(realChildCount-1);
                final int bigRadius = W-2*itemRadius;
                View child;
                for (int i=0; i<realChildCount; ++i){
                    child = mReusedItemViews.get(i);
                    // (cx, cy) is the coordinates of item view's center.
                    // And (0, 0) is mapped to (l, b) for real.
                    double cx = bigRadius-(bigRadius*Math.cos(theta*i))+itemRadius;
                    double cy = bigRadius-bigRadius*Math.sin(theta*i)+itemRadius;
                    child.layout((int)(cx-itemRadius), (int)(cy-itemRadius),
                            (int)(cx+itemRadius), (int)(cy+itemRadius));
                }
            }
        }
    }

    public void addItem(MenuItem item){
        CircleImageView view = new CircleImageView(getContext());
        view.setLayoutParams(new LayoutParams(2*mItemRadius,
                2*mItemRadius));
        if (item.getIcon()!=null) {
            view.setImageDrawable(item.getIcon());
        }else {
            view.setImageDrawable(new ColorDrawable(
                    UiUtil.getColor(getContext(), R.color.quad_bubble_menu_item_default)));
        }
        view.setContentDescription(item.getName());
        view.setOnClickListener(item.getOnClickListener());
        addView(view);
    }

    public static class MenuItem{
        private String mName;
        private Drawable mIcon;
        private OnClickListener mOnClickListener;

        public MenuItem(){}
        public MenuItem(String name, Drawable icon){
            this(name, icon, null);
        }
        public MenuItem(String name, Drawable icon, OnClickListener listener){
            mName = name;
            mIcon = icon;
            mOnClickListener = listener;
        }

        public void setName(String name){
            mName = name;
        }
        public String getName(){
            return mName;
        }
        public void setIcon(Drawable icon){
            mIcon = icon;
        }
        public Drawable getIcon(){
            return mIcon;
        }
        public void setOnClickListener(OnClickListener listener){
            mOnClickListener = listener;
        }
        public OnClickListener getOnClickListener(){
            return mOnClickListener;
        }
    }
}
