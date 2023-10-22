package com.muham.petv01.Components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundImageView : AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        // Get the current drawable.
        val drawable = drawable as? BitmapDrawable

        // Check if the drawable is available.
        if (drawable == null) {
            super.onDraw(canvas)
            return
        }

        // Get the Bitmap from drawable.
        val bitmap = drawable.bitmap

        // Initialize the path.
        val path = Path()

        // Get the width and height of the view.
        val width: Int = width
        val height: Int = height

        // Set the path with rounded corners.
        path.addRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), width / 2.0f, height / 2.0f, Path.Direction.CW)

        // Clip the canvas to the defined path.
        canvas.clipPath(path)

        // Draw the bitmap.
        super.onDraw(canvas)
    }
}
