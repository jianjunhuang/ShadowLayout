package com.lib.jinjunhuang.shadowlayoutlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * @author jianjunhuang.me@foxmail.com
 *         create on 2017/8/27.
 */

public class ShadowLayout extends RelativeLayout {

    public static final int ALL = 0x1111;
    public static final int LEFT = 0x0001;
    public static final int TOP = 0x0010;
    public static final int RIGHT = 0x0100;
    public static final int BOTTOM = 0x1000;

    protected static final int DEFAULT_COLOR = Color.parseColor("#a1a1a1");

    protected float mShadowRadius = 0;

    protected int mShadowColor = DEFAULT_COLOR;

    protected float mLayoutRadius = 0;

    protected float mShadowDX = 0;

    protected float mShadowDY = 0;

    protected int mShadowPosition = ALL;

    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected RectF mRectF = new RectF();

    protected int mBackgroundColor = Color.parseColor("#f1f1f1");

    public ShadowLayout(Context context) {
        this(context, null);
    }

    public ShadowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        float radius = mShadowRadius + dpToPx(5);
        float rectLeft = 0;
        float rectTop = 0;
        float rectRight = this.getWidth();
        float rectBottom = this.getHeight();

        if (((mShadowPosition & LEFT) == LEFT)) {
            rectLeft = radius;
        }

        if (((mShadowPosition & TOP) == TOP)) {
            rectTop = radius;
        }

        if (((mShadowPosition & RIGHT) == RIGHT)) {
            rectRight = this.getWidth() - radius;
        }

        if (((mShadowPosition & BOTTOM) == BOTTOM)) {
            rectBottom = this.getHeight() - radius;
        }

        mRectF.left = rectLeft;
        mRectF.top = rectTop;
        mRectF.right = rectRight;
        mRectF.bottom = rectBottom;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLayoutRadius > 0) {
            canvas.drawRoundRect(mRectF, mLayoutRadius, mLayoutRadius, mPaint);
        } else {
            canvas.drawRect(mRectF, mPaint);
        }
    }

    private void initAttrs(AttributeSet attrs) {
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //调用 onDraw
        this.setWillNotDraw(false);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        if (typedArray != null) {
            mShadowColor = typedArray.getColor(R.styleable.ShadowLayout_shadowColor, DEFAULT_COLOR);
            mShadowPosition = typedArray.getInt(R.styleable.ShadowLayout_shadowPosition, ALL);
            mShadowRadius = typedArray.getDimension(R.styleable.ShadowLayout_shadowRadius, dpToPx(0));
            mShadowDX = typedArray.getDimension(R.styleable.ShadowLayout_shadowDX, dpToPx(0));
            mShadowDY = typedArray.getDimension(R.styleable.ShadowLayout_shadowDY, dpToPx(0));
            mLayoutRadius = typedArray.getDimension(R.styleable.ShadowLayout_layoutRadius, dpToPx(0));
            mBackgroundColor = typedArray.getColor(R.styleable.ShadowLayout_backgroundColor, Color.WHITE);
            typedArray.recycle();
        }
        initPaint();
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBackgroundColor);
        mPaint.setShadowLayer(mShadowRadius, mShadowDX, mShadowDY, mShadowColor);
    }

    protected float dpToPx(float dp) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float scale = dm.density;
        return (dp * scale + 0.5f);
    }
}
