package com.example.brainbyte.ocr

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.text.Text

class TextOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class TextElement(
        val text: String,
        val boundingBox: Rect,
        var isSelected: Boolean = false
    )

    data class TextLine(
        val text: String,
        val boundingBox: Rect,
        val elements: List<TextElement>,
        var isSelected: Boolean = false
    )

    data class TextBlock(
        val text: String,
        val boundingBox: Rect,
        val lines: List<TextLine>,
        var isSelected: Boolean = false
    )

    private val unselectedPaint = Paint().apply {
        color = Color.argb(60, 100, 181, 246)
        style = Paint.Style.FILL
    }

    private val selectedPaint = Paint().apply {
        color = Color.argb(120, 33, 150, 243)
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.argb(180, 33, 150, 243)
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val selectedBorderPaint = Paint().apply {
        color = Color.argb(255, 25, 118, 210)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private var textBlocks = mutableListOf<TextBlock>()
    private var allElements = mutableListOf<TextElement>()
    private var scaleX = 1f
    private var scaleY = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var imageWidth = 0
    private var imageHeight = 0
    private var isSwipeSelecting = false
    private var swipeStartX = 0f
    private var swipeStartY = 0f
    private var swipeEndX = 0f
    private var swipeEndY = 0f
    private val swipePath = Path()
    private val swipePathPaint = Paint().apply {
        color = Color.argb(100, 255, 152, 0)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    enum class SelectionMode {
        WORD,
        LINE,
        BLOCK
    }

    var selectionMode = SelectionMode.WORD

    var onSelectionChangedListener: ((List<String>) -> Unit)? = null

    fun setTextResult(text: Text, imageWidth: Int, imageHeight: Int) {
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight

        textBlocks.clear()
        allElements.clear()

        for (block in text.textBlocks) {
            val blockBoundingBox = block.boundingBox ?: continue
            val lines = mutableListOf<TextLine>()

            for (line in block.lines) {
                val lineBoundingBox = line.boundingBox ?: continue
                val elements = mutableListOf<TextElement>()

                for (element in line.elements) {
                    val elementBoundingBox = element.boundingBox ?: continue
                    val textElement = TextElement(
                        text = element.text,
                        boundingBox = elementBoundingBox
                    )
                    elements.add(textElement)
                    allElements.add(textElement)
                }

                if (elements.isNotEmpty()) {
                    lines.add(TextLine(
                        text = line.text,
                        boundingBox = lineBoundingBox,
                        elements = elements
                    ))
                }
            }

            if (lines.isNotEmpty()) {
                textBlocks.add(TextBlock(
                    text = block.text,
                    boundingBox = blockBoundingBox,
                    lines = lines
                ))
            }
        }

        calculateScaleFactors()
        invalidate()
    }

    private fun calculateScaleFactors() {
        if (width == 0 || height == 0 || imageWidth == 0 || imageHeight == 0) return

        val viewAspect = width.toFloat() / height.toFloat()
        val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()

        if (imageAspect > viewAspect) {
            scaleX = width.toFloat() / imageWidth.toFloat()
            scaleY = scaleX
            offsetX = 0f
            offsetY = (height - imageHeight * scaleY) / 2f
        } else {
            scaleY = height.toFloat() / imageHeight.toFloat()
            scaleX = scaleY
            offsetX = (width - imageWidth * scaleX) / 2f
            offsetY = 0f
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateScaleFactors()
    }

    private fun transformRect(rect: Rect): RectF {
        return RectF(
            rect.left * scaleX + offsetX,
            rect.top * scaleY + offsetY,
            rect.right * scaleX + offsetX,
            rect.bottom * scaleY + offsetY
        )
    }

    private fun viewToImageCoords(x: Float, y: Float): PointF {
        return PointF(
            (x - offsetX) / scaleX,
            (y - offsetY) / scaleY
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (selectionMode) {
            SelectionMode.WORD -> drawWordBoxes(canvas)
            SelectionMode.LINE -> drawLineBoxes(canvas)
            SelectionMode.BLOCK -> drawBlockBoxes(canvas)
        }

        if (isSwipeSelecting) {
            canvas.drawPath(swipePath, swipePathPaint)
        }
    }

    private fun drawWordBoxes(canvas: Canvas) {
        for (element in allElements) {
            val rect = transformRect(element.boundingBox)
            val fillPaint = if (element.isSelected) selectedPaint else unselectedPaint
            val strokePaint = if (element.isSelected) selectedBorderPaint else borderPaint

            canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
            canvas.drawRoundRect(rect, 4f, 4f, strokePaint)
        }
    }

    private fun drawLineBoxes(canvas: Canvas) {
        for (block in textBlocks) {
            for (line in block.lines) {
                val rect = transformRect(line.boundingBox)
                val isSelected = line.isSelected || line.elements.any { it.isSelected }
                val fillPaint = if (isSelected) selectedPaint else unselectedPaint
                val strokePaint = if (isSelected) selectedBorderPaint else borderPaint

                canvas.drawRoundRect(rect, 4f, 4f, fillPaint)
                canvas.drawRoundRect(rect, 4f, 4f, strokePaint)
            }
        }
    }

    private fun drawBlockBoxes(canvas: Canvas) {
        for (block in textBlocks) {
            val rect = transformRect(block.boundingBox)
            val isSelected = block.isSelected || block.lines.any { it.isSelected }
            val fillPaint = if (isSelected) selectedPaint else unselectedPaint
            val strokePaint = if (isSelected) selectedBorderPaint else borderPaint

            canvas.drawRoundRect(rect, 8f, 8f, fillPaint)
            canvas.drawRoundRect(rect, 8f, 8f, strokePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                swipeStartX = event.x
                swipeStartY = event.y
                swipeEndX = event.x
                swipeEndY = event.y
                swipePath.reset()
                swipePath.moveTo(event.x, event.y)
                isSwipeSelecting = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(event.x - swipeStartX)
                val dy = Math.abs(event.y - swipeStartY)

                if (dx > 20 || dy > 20) {
                    isSwipeSelecting = true
                }

                if (isSwipeSelecting) {
                    swipeEndX = event.x
                    swipeEndY = event.y
                    swipePath.lineTo(event.x, event.y)

                    selectElementsInSwipePath(event.x, event.y)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isSwipeSelecting) {
                    handleTap(event.x, event.y)
                }

                isSwipeSelecting = false
                swipePath.reset()
                invalidate()
                notifySelectionChanged()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTap(x: Float, y: Float) {
        val imageCoords = viewToImageCoords(x, y)

        when (selectionMode) {
            SelectionMode.WORD -> {
                for (element in allElements) {
                    if (element.boundingBox.contains(imageCoords.x.toInt(), imageCoords.y.toInt())) {
                        element.isSelected = !element.isSelected
                        break
                    }
                }
            }
            SelectionMode.LINE -> {
                for (block in textBlocks) {
                    for (line in block.lines) {
                        if (line.boundingBox.contains(imageCoords.x.toInt(), imageCoords.y.toInt())) {
                            line.isSelected = !line.isSelected
                            line.elements.forEach { it.isSelected = line.isSelected }
                            break
                        }
                    }
                }
            }
            SelectionMode.BLOCK -> {
                for (block in textBlocks) {
                    if (block.boundingBox.contains(imageCoords.x.toInt(), imageCoords.y.toInt())) {
                        block.isSelected = !block.isSelected
                        block.lines.forEach { line ->
                            line.isSelected = block.isSelected
                            line.elements.forEach { it.isSelected = block.isSelected }
                        }
                        break
                    }
                }
            }
        }
    }

    private fun selectElementsInSwipePath(currentX: Float, currentY: Float) {
        val imageCoords = viewToImageCoords(currentX, currentY)

        when (selectionMode) {
            SelectionMode.WORD -> {
                for (element in allElements) {
                    if (element.boundingBox.contains(imageCoords.x.toInt(), imageCoords.y.toInt())) {
                        element.isSelected = true
                    }
                }
            }
            SelectionMode.LINE -> {
                for (block in textBlocks) {
                    for (line in block.lines) {
                        if (line.boundingBox.contains(imageCoords.x.toInt(), imageCoords.y.toInt())) {
                            line.isSelected = true
                            line.elements.forEach { it.isSelected = true }
                        }
                    }
                }
            }
            SelectionMode.BLOCK -> {
                for (block in textBlocks) {
                    if (block.boundingBox.contains(imageCoords.x.toInt(), imageCoords.y.toInt())) {
                        block.isSelected = true
                        block.lines.forEach { line ->
                            line.isSelected = true
                            line.elements.forEach { it.isSelected = true }
                        }
                    }
                }
            }
        }
    }

    fun getSelectedText(): String {
        val selectedTexts = mutableListOf<String>()

        when (selectionMode) {
            SelectionMode.WORD -> {
                for (element in allElements) {
                    if (element.isSelected) {
                        selectedTexts.add(element.text)
                    }
                }
            }
            SelectionMode.LINE -> {
                for (block in textBlocks) {
                    for (line in block.lines) {
                        if (line.isSelected) {
                            selectedTexts.add(line.text)
                        }
                    }
                }
            }
            SelectionMode.BLOCK -> {
                for (block in textBlocks) {
                    if (block.isSelected) {
                        selectedTexts.add(block.text)
                    }
                }
            }
        }

        return selectedTexts.joinToString(" ")
    }


    fun getSelectedTextList(): List<String> {
        val selectedTexts = mutableListOf<String>()

        when (selectionMode) {
            SelectionMode.WORD -> {
                for (element in allElements) {
                    if (element.isSelected) {
                        selectedTexts.add(element.text)
                    }
                }
            }
            SelectionMode.LINE -> {
                for (block in textBlocks) {
                    for (line in block.lines) {
                        if (line.isSelected) {
                            selectedTexts.add(line.text)
                        }
                    }
                }
            }
            SelectionMode.BLOCK -> {
                for (block in textBlocks) {
                    if (block.isSelected) {
                        selectedTexts.add(block.text)
                    }
                }
            }
        }

        return selectedTexts
    }


    fun clearSelection() {
        allElements.forEach { it.isSelected = false }
        textBlocks.forEach { block ->
            block.isSelected = false
            block.lines.forEach { line ->
                line.isSelected = false
                line.elements.forEach { it.isSelected = false }
            }
        }
        invalidate()
        notifySelectionChanged()
    }


    fun selectAll() {
        allElements.forEach { it.isSelected = true }
        textBlocks.forEach { block ->
            block.isSelected = true
            block.lines.forEach { line ->
                line.isSelected = true
                line.elements.forEach { it.isSelected = true }
            }
        }
        invalidate()
        notifySelectionChanged()
    }

    private fun notifySelectionChanged() {
        onSelectionChangedListener?.invoke(getSelectedTextList())
    }

}