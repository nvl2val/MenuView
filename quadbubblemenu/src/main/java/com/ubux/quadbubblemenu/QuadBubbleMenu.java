package com.ubux.quadbubblemenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

    public static final int TOP_START = 0;
    public static final int TOP_END = 1;
    public static final int BOTTOM_START = 2;
    public static final int BOTTOM_END = 3;

    private int mItemRadius = 0;
    private int mItemSpace = 0;
    private Drawable mCollapsingDrawable;
    private Drawable mExpandingDrawable;
    private Drawable mAxisViewBackground = null;

    /**
     * Indicates the quadrant shape is top_start, top_end,
     * bottom_start or bottom_end of a circle.
     */
    private int mQuadrantLocation = TOP_START;

    /**
     * Minimum distance from item view's center to axis view's center and minus
     * twice radius.
     */
    private int mMinDistanceToAxisView = 0;

    private boolean mIsCollapsing = true;

    private CircleImageView mAxisView = null;

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
        mMinDistanceToAxisView = a.getDimensionPixelSize(
                R.styleable.QuadBubbleMenu_minDistanceToAxisView, mMinDistanceToAxisView);
        mAxisViewBackground =
                a.getDrawable(R.styleable.QuadBubbleMenu_axisViewBackground);
        mQuadrantLocation =
                a.getInt(R.styleable.QuadBubbleMenu_quadrantLocation, mQuadrantLocation);
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
                    (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
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
        if (mAxisViewBackground ==null){
            mAxisViewBackground =
                    UiUtil.getDrawable(context, R.color.quad_bubble_menu_item_default);
        }

        mAxisView = new CircleImageView(context);
        mAxisView.setLayoutParams(new LayoutParams(2*mItemRadius,
                2*mItemRadius));
        mAxisView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        if (Build.VERSION.SDK_INT<16) {
            mAxisView.setBackgroundDrawable(mAxisViewBackground);
        }else {
            mAxisView.setBackground(mAxisViewBackground);
        }
        mAxisView.setImageDrawable(mCollapsingDrawable);
        mAxisView.setOnClickListener(new OnClickListener() {
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
        addView(mAxisView);
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

    public void setQuadrantLocation(int quadLocation){
        if (quadLocation!= TOP_START && quadLocation!= TOP_END
                && quadLocation!= BOTTOM_START && quadLocation!= BOTTOM_END){
            throw new RuntimeException("Illegal quadLocation!");
        }
        mQuadrantLocation = quadLocation;
        requestLayout();
    }
    public int getQuadrantLocation(){
        return mQuadrantLocation;
    }

    private boolean isAxisUnderItems(){
        return mQuadrantLocation == TOP_START || mQuadrantLocation == TOP_END;
    }

    private boolean isAxisLeftOfItems(){
        boolean isLtr = !getContext().getResources().getBoolean(R.bool.is_rtl);
        boolean ansWhenLtr = mQuadrantLocation == TOP_END || mQuadrantLocation == BOTTOM_END;
        return isLtr == ansWhenLtr;
    }

    /**
     * Will be invoked after requestLayout(). However, when invoked,
     * this view may have not been measured in time.
     */
    private AnimatorSet createExpandAnim(){
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        AnimatorSet animSet = new AnimatorSet();
        final boolean isAxisUnderItems = isAxisUnderItems();
        final boolean isAxisLeftOfItems = isAxisLeftOfItems();
        int childCount = getChildCount();
        if (childCount==2) {
            View child = getChildAt(0);
            if (child==mAxisView){
                child = getChildAt(1);
            }
            ObjectAnimator anim;
            if (isAxisUnderItems){
                anim = ObjectAnimator.ofFloat(child, "translationY",
                        getMeasuredHeight()-2*mItemRadius, 0);
            }else {
                anim = ObjectAnimator.ofFloat(child, "translationY",
                        2*mItemRadius-getMeasuredHeight(), 0);
            }
            animSet.play(anim);
        }else if (childCount>2) {
            AnimatorSet.Builder animBuilder = null;
            final float W = getMeasuredWidth();
            final double ITEM_THETA = Math.toRadians(90)/(childCount-2);
            final double bigRadius = W-2*mItemRadius;
            double theta = 0;
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child != mAxisView) {
                    ObjectAnimator trX, trY;

                    if (isAxisLeftOfItems) {
                        trX = ObjectAnimator.ofFloat(child, "translationX",
                                -(float) (bigRadius * Math.cos(theta)), 0);
                    }else {
                        trX = ObjectAnimator.ofFloat(child, "translationX",
                                (float) (bigRadius * Math.cos(theta)), 0);
                    }
                    if (isAxisUnderItems) {
                        trY = ObjectAnimator.ofFloat(child, "translationY",
                                (float) (bigRadius * Math.sin(theta)), 0);
                    }else {
                        trY = ObjectAnimator.ofFloat(child, "translationY",
                                -(float) (bigRadius * Math.sin(theta)), 0);
                    }
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

    /**
     * Will be invoked before collapsed.
     */
    private AnimatorSet createCollapseAnim(Animator.AnimatorListener listener){
        AnimatorSet animSet = new AnimatorSet();
        final boolean isAxisUnderItems = isAxisUnderItems();
        final boolean isAxisLeftOfItems = isAxisLeftOfItems();
        final int childCount = getChildCount();
        if (childCount==2){
            View child = getChildAt(0);
            if (child==mAxisView){
                child = getChildAt(1);
            }
            ObjectAnimator anim;
            if (isAxisUnderItems){
                anim = ObjectAnimator.ofFloat(child, "translationY",
                        0, getMeasuredHeight()-2*mItemRadius);
            }else {
                anim = ObjectAnimator.ofFloat(child, "translationY",
                        0, 2*mItemRadius-getMeasuredHeight());
            }
            animSet.play(anim);
        }else if (childCount>2){
            AnimatorSet.Builder animBuilder = null;
            final float W = getMeasuredWidth();
            final double ITEM_THETA = Math.toRadians(90)/(childCount-2);
            final double bigRadius = W-2*mItemRadius;
            double theta = 0;
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child!=mAxisView) {
                    ObjectAnimator trX, trY;
                    if (isAxisLeftOfItems) {
                        trX = ObjectAnimator.ofFloat(child, "translationX",
                                0, -(float) (bigRadius * Math.cos(theta)));
                    }else {
                        trX = ObjectAnimator.ofFloat(child, "translationX",
                                0, (float) (bigRadius * Math.cos(theta)));
                    }
                    if (isAxisUnderItems) {
                        trY = ObjectAnimator.ofFloat(child, "translationY",
                                0, (float) (bigRadius * Math.sin(theta)));
                    }else {
                        trY = ObjectAnimator.ofFloat(child, "translationY",
                                0, -(float) (bigRadius * Math.sin(theta)));
                    }
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
            measuredHeight = 4 * itemRadius + mMinDistanceToAxisView;
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

    @SuppressWarnings("ResourceType")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            final int W = r-l;
            final int H = b-t;
            final int itemRadius = getChildAt(0).getMeasuredWidth()/2;
            final boolean isAxisUnderItems = isAxisUnderItems();
            final boolean isAxisLeftOfItems = isAxisLeftOfItems();
            mReusedItemViews.clear();
            for (int i=0; i<getChildCount(); ++i){
                View view = getChildAt(i);
                if (view != mAxisView){
                    mReusedItemViews.add(view);
                }
            }

            if (!mIsCollapsing) {
                final int realChildCount = mReusedItemViews.size();
                if (realChildCount == 1) {
                    View topV = mReusedItemViews.get(0);
                    int top = isAxisUnderItems? 0: 2*itemRadius+mMinDistanceToAxisView;
                    topV.layout(0, top, topV.getMeasuredWidth(), top+topV.getMeasuredHeight());
                } else if (realChildCount > 1) {
                    final double theta = Math.toRadians(90) / (realChildCount - 1);
                    final int bigRadius = W - 2 * itemRadius;
                    View child;
                    for (int i = 0; i < realChildCount; ++i) {
                        child = mReusedItemViews.get(i);
                        final int nCos = (int)(bigRadius * Math.cos(theta * i));
                        final int nSin = (int)(bigRadius * Math.sin(theta * i));
                        // (cx, cy) is the coordinates of item view's center.
                        // And (0, 0) is mapped to (l, b) for real.
                        int cx = (isAxisLeftOfItems? nCos: bigRadius-nCos)+itemRadius;
                        int cy = (isAxisUnderItems? bigRadius-nSin: nSin) + itemRadius;

                        child.layout(cx - itemRadius, cy - itemRadius,
                                cx + itemRadius, cy + itemRadius);
                    }
                }
            }

            int axisL = isAxisLeftOfItems? 0: W-2*itemRadius;
            int axisT = isAxisUnderItems? H-2*itemRadius: 0;
            mAxisView.layout(axisL, axisT, axisL+mAxisView.getMeasuredWidth(),
                    axisT+mAxisView.getMeasuredHeight());
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
        addView(view, getChildCount()-1);
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
