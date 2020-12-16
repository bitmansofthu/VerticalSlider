package com.hmomeni.verticalslider

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class VerticalSlider : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val a = context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.VerticalSlider,
            0, 0
        )

        try {
            val iconLoResId = a.getResourceId(R.styleable.VerticalSlider_vs_iconLow, -1)
            val iconMedResId = a.getResourceId(R.styleable.VerticalSlider_vs_iconMedium, -1)
            val iconHiResId = a.getResourceId(R.styleable.VerticalSlider_vs_iconHigh, -1)
            max = a.getInteger(R.styleable.VerticalSlider_vs_max, max)
            progress = a.getInteger(R.styleable.VerticalSlider_vs_progress, progress)
            cornerRadius = a.getDimension(R.styleable.VerticalSlider_vs_cornerRadius, cornerRadius)
            setProgressDrawableResource(a.getResourceId(R.styleable.VerticalSlider_vs_progressDrawable, -1))
            setLayoutDrawableResource(a.getResourceId(R.styleable.VerticalSlider_vs_layoutDrawable, -1))
            thread {
                if (iconHiResId != -1)
                    iconHigh = getBitmapFromVectorDrawable(context, iconHiResId)
                if (iconMedResId != -1)
                    iconMedium = getBitmapFromVectorDrawable(context, iconMedResId)
                if (iconLoResId != -1)
                    iconLow = getBitmapFromVectorDrawable(context, iconLoResId)
            }
        } finally {
            a.recycle()
        }
    }

    var iconHigh: Bitmap? = null
    var iconMedium: Bitmap? = null
    var iconLow: Bitmap? = null

    var layoutDrawable: Drawable? = null
    var progressDrawable: Drawable? = null

    var cornerRadius = dpToPx(10).toFloat()
        set(value) {
            field = value
            invalidate()
        }
    var max: Int = 10
    private var _progress: Int = 5
    var progress: Int
        set(value) {
            updateProgress(value)
            onProgressChangeListener?.onChanged(this, value, false)
        }
        get() {
            return _progress
        }
    var onProgressChangeListener: OnSliderProgressChangeListener? = null

    fun setIconHighResource(@DrawableRes resId: Int) {
        iconHigh = getBitmapFromVectorDrawable(context, resId)
    }

    fun setIconMediumResource(@DrawableRes resId: Int) {
        iconMedium = getBitmapFromVectorDrawable(context, resId)
    }

    fun setIconLowResource(@DrawableRes resId: Int) {
        iconLow = getBitmapFromVectorDrawable(context, resId)
    }

    fun setLayoutDrawableResource(@DrawableRes resId: Int) {
        if (resId != -1) {
            layoutDrawable = ContextCompat.getDrawable(context, resId)
        }
    }

    fun setProgressDrawableResource(@DrawableRes resId: Int) {
        if (resId != -1) {
            progressDrawable = ContextCompat.getDrawable(context, resId)
        }
    }

    private val iconWidth = dpToPx(36)
    private val iconRect: RectF = RectF()
    private val layoutRect: RectF = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    private val layoutPaint = Paint().apply {
        color = Color.parseColor("#aa787878")
        isAntiAlias = true
    }
    private val progressRect: RectF = RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
    private val progressPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }
    private val path = Path()
    private val convertRect = Rect()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (measuredHeight > 0 && measuredWidth > 0) {
            layoutRect.set(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
            progressRect.set(
                0f,
                (1 - calculateProgress()) * measuredHeight,
                measuredWidth.toFloat(),
                measuredHeight.toFloat()
            )
            iconRect.set(
                measuredWidth / 2f - iconWidth / 2,
                measuredHeight / 2f - iconWidth / 2,
                measuredWidth / 2f + iconWidth / 2,
                measuredHeight / 2f + iconWidth / 2
            )
            path.reset()
            path.addRoundRect(layoutRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(path)
        if (layoutDrawable == null) {
            canvas.drawRect(layoutRect, layoutPaint)
        } else {
            layoutRect.round(convertRect)
            layoutDrawable?.bounds = convertRect
            layoutDrawable?.draw(canvas)
        }
        if (progressDrawable == null) {
            canvas.drawRect(progressRect, progressPaint)
        } else {
            progressRect.round(convertRect)
            progressDrawable?.bounds = convertRect
            progressDrawable?.draw(canvas)
        }

        if (iconLow != null && iconMedium != null && iconHigh != null) {
            when {
                progress < max / 3 -> {
                    canvas.drawBitmap(iconLow, null, iconRect, null)
                }
                progress < max * 2 / 3 -> {
                    canvas.drawBitmap(iconMedium, null, iconRect, null)
                }
                else -> {
                    canvas.drawBitmap(iconHigh, null, iconRect, null)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onProgressChangeListener?.onPressed(this)
                return true
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val y = event.y
                val currentHeight = measuredHeight - y
                val percent = currentHeight / measuredHeight.toFloat()
                updateProgress(when {
                    percent >= 1 -> max
                    percent <= 0 -> 0
                    else -> (max * percent).toInt()
                })
                if (event.action == MotionEvent.ACTION_UP) {
                    onProgressChangeListener?.onReleased(this)
                } else {
                    onProgressChangeListener?.onChanged(this, _progress, true)
                }
                return true
            }
        }
        return false
    }

    private fun calculateProgress(): Float {
        return progress.toFloat() / max.toFloat()
    }

    protected fun updateProgress(progress: Int) {
        if (progress > max) {
            throw RuntimeException("progress must not be larger than max")
        }
        _progress = progress
        progressRect.set(
            0f,
            (1 - calculateProgress()) * measuredHeight,
            measuredWidth.toFloat(),
            measuredHeight.toFloat()
        )
        invalidate()
    }

    interface OnSliderProgressChangeListener {
        fun onChanged(slider: VerticalSlider, progress: Int, fromUser: Boolean)
        fun onPressed(slider: VerticalSlider)
        fun onReleased(slider: VerticalSlider)
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

}