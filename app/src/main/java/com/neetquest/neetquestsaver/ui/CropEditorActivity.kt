package com.neetquest.neetquestsaver.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.neetquest.neetquestsaver.service.ScreenCaptureService
import com.neetquest.neetquestsaver.ui.theme.NEETQuestSaverTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CropHolder {
    var croppedBitmap: Bitmap? = null
}

enum class DragHandle { NONE, TL, TR, BL, BR, MOVE }

class CropEditorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bitmap = ScreenCaptureService.capturedBitmap.value
        if (bitmap == null) { finish(); return }

        setContent {
            NEETQuestSaverTheme {
                CropEditorScreen(
                    bitmap = bitmap,
                    onCancel = {
                        ScreenCaptureService.clearCapture()
                        finish()
                    },
                    onCropDone = { cropped ->
                        CropHolder.croppedBitmap = cropped
                        ScreenCaptureService.clearCapture()
                        startActivity(
                            Intent(this, SaveQuestionActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onCropDone: (Bitmap) -> Unit
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var activeHandle by remember { mutableStateOf(DragHandle.NONE) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    var rectAtDrag by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(canvasSize) {
        if (canvasSize != Size.Zero && cropRect == null) {
            val m = min(canvasSize.width, canvasSize.height) * 0.08f
            cropRect = Rect(m, m, canvasSize.width - m, canvasSize.height - m)
        }
    }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Select Question Area", fontWeight = FontWeight.Bold,
                        color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                },
                actions = {
                    cropRect?.let { r ->
                        Text(
                            "${(r.width * bitmap.width / canvasSize.width).toInt()} × " +
                            "${(r.height * bitmap.height / canvasSize.height).toInt()}",
                            color = Color(0xFF80DEEA),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.Black) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { cropRect = null },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                    ) {
                        Icon(Icons.Default.RestartAlt, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reset")
                    }
                    Text(
                        "Drag corners to resize",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Button(
                        onClick = {
                            val r = cropRect ?: return@Button
                            onCropDone(cropBitmap(bitmap, r, canvasSize))
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C35A5))
                    ) {
                        Icon(Icons.Default.Crop, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Crop & Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding).background(Color.Black)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val r = cropRect ?: return@detectDragGestures
                                activeHandle = detectHandle(offset, r, 36f)
                                dragStart = offset
                                rectAtDrag = r
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val r = rectAtDrag ?: return@detectDragGestures
                                val delta = change.position - dragStart
                                cropRect = applyDrag(r, delta, activeHandle, canvasSize)
                            },
                            onDragEnd = { activeHandle = DragHandle.NONE }
                        )
                    }
            ) {
                drawImage(imageBitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()))

                val r = cropRect ?: return@Canvas

                val path = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addRect(r)
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path, Color.Black.copy(alpha = 0.6f))

                drawRect(
                    Color.White, topLeft = Offset(r.left, r.top),
                    size = Size(r.width, r.height),
                    style = Stroke(width = 2.dp.toPx())
                )

                val w3 = r.width / 3f; val h3 = r.height / 3f
                for (i in 1..2) {
                    drawLine(Color.White.copy(alpha = 0.3f),
                        Offset(r.left + w3*i, r.top), Offset(r.left + w3*i, r.bottom), strokeWidth = 0.5f)
                    drawLine(Color.White.copy(alpha = 0.3f),
                        Offset(r.left, r.top + h3*i), Offset(r.right, r.top + h3*i), strokeWidth = 0.5f)
                }

                val hl = 28.dp.toPx(); val hw = 4.dp.toPx()
                val hc = Color(0xFF80DEEA)
                drawLine(hc, Offset(r.left, r.top), Offset(r.left+hl, r.top), strokeWidth = hw)
                drawLine(hc, Offset(r.left, r.top), Offset(r.left, r.top+hl), strokeWidth = hw)
                drawLine(hc, Offset(r.right, r.top), Offset(r.right-hl, r.top), strokeWidth = hw)
                drawLine(hc, Offset(r.right, r.top), Offset(r.right, r.top+hl), strokeWidth = hw)
                drawLine(hc, Offset(r.left, r.bottom), Offset(r.left+hl, r.bottom), strokeWidth = hw)
                drawLine(hc, Offset(r.left, r.bottom), Offset(r.left, r.bottom-hl), strokeWidth = hw)
                drawLine(hc, Offset(r.right, r.bottom), Offset(r.right-hl, r.bottom), strokeWidth = hw)
                drawLine(hc, Offset(r.right, r.bottom), Offset(r.right, r.bottom-hl), strokeWidth = hw)
            }
        }
    }
}

private fun detectHandle(pos: Offset, rect: Rect, radius: Float): DragHandle {
    fun near(a: Offset, b: Offset) = abs(a.x-b.x) < radius && abs(a.y-b.y) < radius
    return when {
        near(pos, Offset(rect.left, rect.top))     -> DragHandle.TL
        near(pos, Offset(rect.right, rect.top))    -> DragHandle.TR
        near(pos, Offset(rect.left, rect.bottom))  -> DragHandle.BL
        near(pos, Offset(rect.right, rect.bottom)) -> DragHandle.BR
        rect.contains(pos)                          -> DragHandle.MOVE
        else                                        -> DragHandle.NONE
    }
}

private fun applyDrag(r: Rect, d: Offset, h: DragHandle, canvas: Size): Rect {
    val MIN = 80f
    fun cl(v: Float, mn: Float, mx: Float) = max(mn, min(mx, v))
    return when (h) {
        DragHandle.TL -> Rect(cl(r.left+d.x,0f,r.right-MIN), cl(r.top+d.y,0f,r.bottom-MIN), r.right, r.bottom)
        DragHandle.TR -> Rect(r.left, cl(r.top+d.y,0f,r.bottom-MIN), cl(r.right+d.x,r.left+MIN,canvas.width), r.bottom)
        DragHandle.BL -> Rect(cl(r.left+d.x,0f,r.right-MIN), r.top, r.right, cl(r.bottom+d.y,r.top+MIN,canvas.height))
        DragHandle.BR -> Rect(r.left, r.top, cl(r.right+d.x,r.left+MIN,canvas.width), cl(r.bottom+d.y,r.top+MIN,canvas.height))
        DragHandle.MOVE -> {
            val nl = cl(r.left+d.x, 0f, canvas.width-r.width)
            val nt = cl(r.top+d.y, 0f, canvas.height-r.height)
            Rect(nl, nt, nl+r.width, nt+r.height)
        }
        DragHandle.NONE -> r
    }
}

private fun cropBitmap(bmp: Bitmap, rect: Rect, canvas: Size): Bitmap {
    val sx = bmp.width / canvas.width; val sy = bmp.height / canvas.height
    val l = (rect.left*sx).toInt().coerceIn(0, bmp.width)
    val t = (rect.top*sy).toInt().coerceIn(0, bmp.height)
    val w = ((rect.width*sx).toInt()).coerceIn(1, bmp.width-l)
    val h = ((rect.height*sy).toInt()).coerceIn(1, bmp.height-t)
    return Bitmap.createBitmap(bmp, l, t, w, h)
}
