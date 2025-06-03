package com.example.myapplication.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * 自定义的圆形ImageView，用于显示圆形图片
 */
class CircleImageView : AppCompatImageView {
    
    private val paint = Paint()
    private val borderPaint = Paint()
    private var borderWidth = 0f
    private var borderColor = Color.WHITE
    
    constructor(context: Context) : super(context) {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    
    private fun init() {
        paint.isAntiAlias = true
        borderPaint.isAntiAlias = true
        borderPaint.color = borderColor
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth
    }
    
    /**
     * 设置边框宽度
     */
    fun setBorderWidth(width: Float) {
        borderWidth = width
        borderPaint.strokeWidth = width
        invalidate()
    }
    
    /**
     * 设置边框颜色
     */
    fun setBorderColor(color: Int) {
        borderColor = color
        borderPaint.color = color
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return
        
        val bitmap = getBitmapFromDrawable(drawable) ?: return
        
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        val diameter = Math.min(width, height)
        val radius = diameter / 2f
        
        val circleBitmap = getCircleBitmap(bitmap, radius.toInt())
        
        // 绘制圆形图片
        canvas.drawBitmap(circleBitmap, paddingLeft.toFloat(), paddingTop.toFloat(), null)
        
        // 绘制边框
        if (borderWidth > 0) {
            canvas.drawCircle(
                paddingLeft + radius,
                paddingTop + radius,
                radius - borderWidth / 2,
                borderPaint
            )
        }
    }
    
    /**
     * 从Drawable中获取Bitmap
     */
    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        
        try {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }
    
    /**
     * 获取圆形Bitmap
     */
    private fun getCircleBitmap(bitmap: Bitmap, radius: Int): Bitmap {
        val output = Bitmap.createBitmap(
            radius * 2,
            radius * 2,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(output)
        
        val rect = Rect(0, 0, radius * 2, radius * 2)
        
        paint.reset()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paint)
        
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        
        // 计算缩放比例，确保图片填充整个圆形
        val scale: Float
        val dx: Float
        val dy: Float
        
        if (bitmap.width < bitmap.height) {
            scale = (radius * 2).toFloat() / bitmap.width
            dx = 0f
            dy = (radius * 2 - bitmap.height * scale) * 0.5f
        } else {
            scale = (radius * 2).toFloat() / bitmap.height
            dx = (radius * 2 - bitmap.width * scale) * 0.5f
            dy = 0f
        }
        
        val matrix = android.graphics.Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)
        
        canvas.drawBitmap(bitmap, matrix, paint)
        
        return output
    }
}