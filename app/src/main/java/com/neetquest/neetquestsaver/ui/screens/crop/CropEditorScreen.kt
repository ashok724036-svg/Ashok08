package com.neetquest.neetquestsaver.ui.screens.crop

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.neetquest.neetquestsaver.service.ScreenCaptureService
import com.neetquest.neetquestsaver.ui.Screen
import com.neetquest.neetquestsaver.viewmodel.CropViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val HANDLE_RADIUS = 24f   // touch target radius in px
private const val MIN_CROP_SIZE = 100f  // minimum crop rect size

enum class DragHandle { NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, MOVE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
    navController: NavController,
    viewModel: CropViewModel = hiltViewModel()
) {
    val capturedBitmap by ScreenCaptureService.capturedBitmap.collectAsStateWithLifecycle()
    val bitmap = capturedBitmap

    if (bitmap == null) {
        // No bitmap — go back
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // Crop rect state in canvas coordinates
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var activeHandle by remember { mutableStateOf(DragHandle.NONE) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    var rectAtDragStart by remember { mutableStateOf<Rect?>(null) }

    val density = LocalDensity.current

    // Initialize cropRect once canvas size is known
    LaunchedEffect(canvasSize) {
        if (canvasSize != Size.Zero && cropRect == null) {
            val margin = min(canvasSize.width, canvasSize.height) * 0.1f
            cropRect = Rect(
                left = margin,
                top = margin,
                right = canvasSize.width - margin,
                bottom = canvasSize.height - margin
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Question", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        ScreenCaptureService.clearCapture()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.Black) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset crop rect
                            cropRect = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.RestartAlt, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            val rect = cropRect ?: return@Button
                            val croppedBitmap = cropBitmap(bitmap, rect, canvasSize)
                            viewModel.setCroppedBitmap(croppedBitmap)
                            ScreenCaptureService.clearCapture()
                            navController.navigate(Screen.SaveQuestion.route) {
                                popUpTo(Screen.CropEditor.route) { inclusive = true }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Crop, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Crop & Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            // Full-screen canvas with bitmap and crop overlay
            val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val rect = cropRect ?: return@detectDragGestures
                                activeHandle = detectHandle(offset, rect, HANDLE_RADIUS)
                                dragStart = offset
                                rectAtDragStart = rect
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val rect = rectAtDragStart ?: return@detectDragGestures
                                val delta = change.position - dragStart
                                cropRect = applyDrag(rect, delta, activeHandle, canvasSize)
                            },
                            onDragEnd = { activeHandle = DragHandle.NONE }
                        )
                    }
            ) {
                // Draw screenshot
                drawImage(
                    image = imageBitmap,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                )

                val rect = cropRect ?: return@Canvas

                // Dim outside crop rect
                val path = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addRect(rect)
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path, Color.Black.copy(alpha = 0.55f))

                // Crop rect border
                drawRect(
                    color = Color.White,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Rule-of-thirds grid lines
                drawRuleOfThirds(rect)

                // Corner handles
                val handleColor = Color(0xFF80DEEA)
                val handleStroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                val hl = 24.dp.toPx()
                drawHandle(Offset(rect.left, rect.top), hl, Corner.TL, handleColor, handleStroke)
                drawHandle(Offset(rect.right, rect.top), hl, Corner.TR, handleColor, handleStroke)
                drawHandle(Offset(rect.left, rect.bottom), hl, Corner.BL, handleColor, handleStroke)
                drawHandle(Offset(rect.right, rect.bottom), hl, Corner.BR, handleColor, handleStroke)
            }

            // Instruction hint
            Text(
                "Drag corners to select the question area",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

// ── Canvas helpers ────────────────────────────────────────────────────────────

private enum class Corner { TL, TR, BL, BR }

private fun DrawScope.drawRuleOfThirds(rect: Rect) {
    val color = Color.White.copy(alpha = 0.3f)
    val stroke = Stroke(width = 1f)
    val w3 = rect.width / 3f
    val h3 = rect.height / 3f
    for (i in 1..2) {
        drawLine(color, Offset(rect.left + w3 * i, rect.top), Offset(rect.left + w3 * i, rect.bottom), strokeWidth = 0.5f)
        drawLine(color, Offset(rect.left, rect.top + h3 * i), Offset(rect.right, rect.top + h3 * i), strokeWidth = 0.5f)
    }
}

private fun DrawScope.drawHandle(pos: Offset, len: Float, corner: Corner, color: Color, stroke: Stroke) {
    val hx = len
    val hy = len
    when (corner) {
        Corner.TL -> {
            drawLine(color, pos, Offset(pos.x + hx, pos.y), strokeWidth = stroke.width)
            drawLine(color, pos, Offset(pos.x, pos.y + hy), strokeWidth = stroke.width)
        }
        Corner.TR -> {
            drawLine(color, pos, Offset(pos.x - hx, pos.y), strokeWidth = stroke.width)
            drawLine(color, pos, Offset(pos.x, pos.y + hy), strokeWidth = stroke.width)
        }
        Corner.BL -> {
            drawLine(color, pos, Offset(pos.x + hx, pos.y), strokeWidth = stroke.width)
            drawLine(color, pos, Offset(pos.x, pos.y - hy), strokeWidth = stroke.width)
        }
        Corner.BR -> {
            drawLine(color, pos, Offset(pos.x - hx, pos.y), strokeWidth = stroke.width)
            drawLine(color, pos, Offset(pos.x, pos.y - hy), strokeWidth = stroke.width)
        }
    }
}

private fun detectHandle(pos: Offset, rect: Rect, radius: Float): DragHandle {
    fun near(a: Offset, b: Offset) = abs(a.x - b.x) < radius && abs(a.y - b.y) < radius
    return when {
        near(pos, Offset(rect.left, rect.top))     -> DragHandle.TOP_LEFT
        near(pos, Offset(rect.right, rect.top))    -> DragHandle.TOP_RIGHT
        near(pos, Offset(rect.left, rect.bottom))  -> DragHandle.BOTTOM_LEFT
        near(pos, Offset(rect.right, rect.bottom)) -> DragHandle.BOTTOM_RIGHT
        rect.contains(pos)                          -> DragHandle.MOVE
        else                                        -> DragHandle.NONE
    }
}

private fun applyDrag(rect: Rect, delta: Offset, handle: DragHandle, canvas: Size): Rect {
    return when (handle) {
        DragHandle.TOP_LEFT -> Rect(
            left = clamp(rect.left + delta.x, 0f, rect.right - MIN_CROP_SIZE),
            top = clamp(rect.top + delta.y, 0f, rect.bottom - MIN_CROP_SIZE),
            right = rect.right,
            bottom = rect.bottom
        )
        DragHandle.TOP_RIGHT -> Rect(
            left = rect.left,
            top = clamp(rect.top + delta.y, 0f, rect.bottom - MIN_CROP_SIZE),
            right = clamp(rect.right + delta.x, rect.left + MIN_CROP_SIZE, canvas.width),
            bottom = rect.bottom
        )
        DragHandle.BOTTOM_LEFT -> Rect(
            left = clamp(rect.left + delta.x, 0f, rect.right - MIN_CROP_SIZE),
            top = rect.top,
            right = rect.right,
            bottom = clamp(rect.bottom + delta.y, rect.top + MIN_CROP_SIZE, canvas.height)
        )
        DragHandle.BOTTOM_RIGHT -> Rect(
            left = rect.left,
            top = rect.top,
            right = clamp(rect.right + delta.x, rect.left + MIN_CROP_SIZE, canvas.width),
            bottom = clamp(rect.bottom + delta.y, rect.top + MIN_CROP_SIZE, canvas.height)
        )
        DragHandle.MOVE -> {
            val w = rect.width
            val h = rect.height
            val newLeft = clamp(rect.left + delta.x, 0f, canvas.width - w)
            val newTop = clamp(rect.top + delta.y, 0f, canvas.height - h)
            Rect(left = newLeft, top = newTop, right = newLeft + w, bottom = newTop + h)
        }
        DragHandle.NONE -> rect
    }
}

private fun clamp(value: Float, min: Float, max: Float) = max(min, min(max, value))

private fun cropBitmap(bitmap: Bitmap, rect: Rect, canvasSize: Size): Bitmap {
    val scaleX = bitmap.width / canvasSize.width
    val scaleY = bitmap.height / canvasSize.height
    val left   = (rect.left   * scaleX).toInt().coerceIn(0, bitmap.width)
    val top    = (rect.top    * scaleY).toInt().coerceIn(0, bitmap.height)
    val right  = (rect.right  * scaleX).toInt().coerceIn(0, bitmap.width)
    val bottom = (rect.bottom * scaleY).toInt().coerceIn(0, bitmap.height)
    val width  = (right - left).coerceAtLeast(1)
    val height = (bottom - top).coerceAtLeast(1)
    return Bitmap.createBitmap(bitmap, left, top, width, height)
}
