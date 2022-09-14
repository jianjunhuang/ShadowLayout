package com.lib.jinjunhuang.shadowlayoutlibrary

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import java.lang.Math.abs

/**
 * inspired by [RoundRectDrawableWithShadow]
 */
class ShadowFrameLayout : FrameLayout {

    var customBackgroundColor: Int = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    var backgroundRadius: Float = 4f
        set(value) {
            field = value
            postInvalidate()
        }

    var shadowOffsetX = 0f
        set(value) {
            field = value
            updatePaint()
            updateRect()
            requestLayout()
        }

    var shadowOffsetY = 0f
        set(value) {
            field = value
            updatePaint()
            updateRect()
            requestLayout()
        }
    var shadowColor = Color.BLACK
        set(value) {
            field = value
            updatePaint()
            postInvalidate()
        }

    var shadowRadius = 8f
        set(value) {
            field = value
            updateRect()
            updatePaint()
            requestLayout()
        }

    private val backgroundRect = RectF()
    private val backgroundPaint = Paint()
    private val initPaddingRect = Rect()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, style: Int) : super(context, attrs, style) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
        context.obtainStyledAttributes(attrs, R.styleable.ShadowFrameLayout)?.let {
            customBackgroundColor =
                it.getColor(R.styleable.ShadowFrameLayout_customBackgroundColor, Color.WHITE)
            backgroundRadius = it.getDimension(R.styleable.ShadowFrameLayout_backgroundRadius, 4f)
            shadowOffsetX = it.getDimension(R.styleable.ShadowFrameLayout_shadowOffsetX, 0f)
            shadowOffsetY = it.getDimension(R.styleable.ShadowFrameLayout_shadowOffsetY, 0f)
            shadowColor = it.getColor(R.styleable.ShadowFrameLayout_shadowColor, Color.BLACK)
            shadowRadius = it.getDimension(R.styleable.ShadowFrameLayout_shadowRadius, 8f)
            it.recycle()
        }
        updatePaint()
        initPaddingRect.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount > 1) throw IllegalStateException("only support 1 children!!")
        if (childCount == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val child = getChildAt(0) ?: return
        var childState = 0
        measureChildWithMargins(
            child,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            0
        )
        var maxWidth = 0
        var maxHeight = 0
        val childLp = (child.layoutParams as? MarginLayoutParams)
        maxWidth = Math.max(
            maxWidth,
            child.measuredWidth + (childLp?.leftMargin ?: 0) + (childLp?.rightMargin
                ?: 0) + (shadowRadius * 2 + abs(shadowOffsetX)).toInt()
        )
        maxHeight = Math.max(
            maxHeight,
            child.measuredHeight + (childLp?.topMargin ?: 0) + (childLp?.bottomMargin
                ?: 0) + (shadowRadius * 2 + abs(shadowOffsetY)).toInt()
        )
        childState = combineMeasuredStates(childState, child.measuredState)

        maxWidth += (paddingLeft + paddingRight)
        maxHeight += (paddingTop + paddingBottom)

        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(
                maxHeight,
                heightMeasureSpec,
                childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
        child.measure(
            getChildMeasureSpec(
                widthMeasureSpec,
                paddingLeft + paddingRight + (shadowRadius * 2 + abs(shadowOffsetX)).toInt(),
                childLp?.width ?: 0
            ),
            getChildMeasureSpec(
                heightMeasureSpec,
                paddingTop + paddingBottom + (shadowRadius * 2 + abs(shadowOffsetY)).toInt(),
                childLp?.height ?: 0
            )
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount > 1) throw IllegalStateException("only support 1 children!!")
        updateRect()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            child.layout(
                backgroundRect.left.toInt(),
                backgroundRect.top.toInt(),
                backgroundRect.right.toInt(),
                backgroundRect.bottom.toInt()
            )
        }
    }

    /**
         ┌──────────┬───────────shadow layout─────────────┬───────────┐
         │          │                                     │           │
         │    ┌─────┼─────────background layout───────────┼─────┐     │
         │    │     │                                     │     │     │
         ├────┼─────┼─────────────────────────────────────┼─────┼─────┤
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         │    │     │                                     │     │     │
         ├────┼─────┼─────────────────────────────────────┼─────┼─────┤
         │    │     │                                     │     │     │
         │    └─────┼─────────────────────────────────────┼─────┘     │
         │          │                                     │           │
         └──────────┴─────────────────────────────────────┴───────────┘
         TODO not best practices
     */
    private fun drawShadow(canvas: Canvas) {
        val colors =
            intArrayOf(shadowColor, shadowColor, Color.parseColor("#00000000"))
        val startRatio = backgroundRadius / (backgroundRadius + shadowRadius)
        val positions = floatArrayOf(0f, startRatio, 1f)
        //left rect
        val leftRect = RectF()
        leftRect.set(
            backgroundRect.left - shadowRadius,
            backgroundRect.top + shadowRadius,
            backgroundRect.left + shadowRadius,
            backgroundRect.bottom - shadowRadius
        )
        val leftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        leftPaint.shader = LinearGradient(
            leftRect.right,
            leftRect.top,
            leftRect.left,
            leftRect.top,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(leftRect, leftPaint)

        //top left corner rect
        val topLeftRect = RectF()
        topLeftRect.set(
            backgroundRect.left - shadowRadius,
            backgroundRect.top - shadowRadius,
            backgroundRect.left + shadowRadius * 3,
            backgroundRect.top + shadowRadius * 3
        )
        val topLeftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
        }
        topLeftPaint.shader = RadialGradient(
            topLeftRect.centerX(),
            topLeftRect.centerY(),
            topLeftRect.width() / 2,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(topLeftRect, 180f, 90f, true, topLeftPaint)

        //top rect
        val topRect = RectF()
        topRect.set(
            backgroundRect.left + shadowRadius,
            backgroundRect.top - shadowRadius,
            backgroundRect.right - shadowRadius,
            backgroundRect.top + shadowRadius
        )
        val topPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
        topPaint.shader = LinearGradient(
            topRect.left,
            topRect.bottom,
            topRect.left,
            topRect.top,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(topRect, topPaint)

        //top right corner rect
        val topRightRect = RectF()
        topRightRect.set(
            backgroundRect.right - shadowRadius * 3,
            backgroundRect.top - shadowRadius,
            backgroundRect.right + shadowRadius,
            backgroundRect.top + shadowRadius * 3
        )
        val topRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        topRightPaint.shader = RadialGradient(
            topRightRect.centerX(),
            topRightRect.centerY(),
            topRightRect.width() / 2f,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(topRightRect, -90f, 90f, true, topRightPaint)

        //right rect
        val rightRect = RectF()
        rightRect.set(
            backgroundRect.right - shadowRadius,
            backgroundRect.top + shadowRadius,
            backgroundRect.right + shadowRadius,
            backgroundRect.bottom - shadowRadius
        )
        val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        rightPaint.shader = LinearGradient(
            rightRect.left,
            rightRect.top,
            rightRect.right,
            rightRect.top,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(rightRect, rightPaint)

        //bottom right corner rect
        val bottomRightRect = RectF()
        bottomRightRect.set(
            backgroundRect.right - shadowRadius * 3,
            backgroundRect.bottom - shadowRadius * 3,
            backgroundRect.right + shadowRadius,
            backgroundRect.bottom + shadowRadius
        )
        val bottomRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
        }
        bottomRightPaint.shader = RadialGradient(
            bottomRightRect.centerX(),
            bottomRightRect.centerY(),
            bottomRightRect.width() / 2f,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(bottomRightRect, 0f, 90f, true, bottomRightPaint)

        //bottom rect
        val bottomRect = RectF()
        bottomRect.set(
            backgroundRect.left + shadowRadius,
            backgroundRect.bottom - shadowRadius,
            backgroundRect.right - shadowRadius,
            backgroundRect.bottom + shadowRadius
        )
        val bottomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        bottomPaint.shader = LinearGradient(
            bottomRect.left,
            bottomRect.top,
            bottomRect.left,
            bottomRect.bottom,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(bottomRect, bottomPaint)


        //bottom left corner rect
        val bottomLeftRect = RectF()
        bottomLeftRect.set(
            backgroundRect.left - shadowRadius,
            backgroundRect.bottom - shadowRadius * 3,
            backgroundRect.left + shadowRadius * 3,
            backgroundRect.bottom + shadowRadius
        )
        val bottomLeftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.BLACK
        }
        bottomLeftPaint.shader = RadialGradient(
            bottomLeftRect.centerX(),
            bottomLeftRect.centerY(),
            bottomLeftRect.width() / 2f,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
        canvas.drawArc(bottomLeftRect, 90f, 90f, true, bottomLeftPaint)

    }

    private fun updatePaint() {

        backgroundPaint.apply {
            color = customBackgroundColor
        }
    }

    private fun updateRect() {
        backgroundRect.set(
            paddingLeft + shadowRadius + (if (shadowOffsetX < 0) shadowOffsetX else 0f),
            paddingTop + shadowRadius + (if (shadowOffsetY < 0f) shadowOffsetY else 0f),
            measuredWidth - shadowRadius - paddingRight - (if (shadowOffsetX > 0f) shadowOffsetX else 0f),
            measuredHeight - shadowRadius - paddingBottom - (if (shadowOffsetY > 0f) shadowOffsetY else 0f)
        )
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val count = it.saveCount
            it.translate(shadowOffsetX, shadowOffsetY)
            drawShadow(it)
            it.restoreToCount(count)
        }
        canvas?.drawRoundRect(backgroundRect, backgroundRadius, backgroundRadius, backgroundPaint)
        super.onDraw(canvas)
    }
}