package com.neetquest.neetquestsaver.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neetquest.neetquestsaver.ui.theme.NEETQuestSaverTheme
import com.neetquest.neetquestsaver.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

val SUBJECTS_LIST = listOf("Physics", "Chemistry", "Botany", "Zoology")
val DIFFICULTIES_LIST = listOf("Easy", "Medium", "Hard")
val TAGS_LIST = listOf("PYQ", "Formula", "Diagram", "Shortcut", "Derivation", "Graph", "Exception", "Important")

val CHAPTERS_MAP = mapOf(
    "Physics" to listOf("Kinematics", "Laws of Motion", "Work, Energy & Power",
        "Gravitation", "Properties of Matter", "Thermodynamics", "Oscillations",
        "Waves", "Electrostatics", "Current Electricity", "Magnetic Effects",
        "EMI & AC", "Electromagnetic Waves", "Optics", "Dual Nature",
        "Atoms & Nuclei", "Electronic Devices"),
    "Chemistry" to listOf("Basic Concepts", "Structure of Atom", "Chemical Bonding",
        "States of Matter", "Thermodynamics", "Equilibrium", "Redox Reactions",
        "Electrochemistry", "Chemical Kinetics", "Surface Chemistry",
        "p-Block Elements", "d & f Block Elements", "Coordination Compounds",
        "Hydrocarbons", "Haloalkanes", "Alcohols & Phenols",
        "Aldehydes & Ketones", "Amines", "Biomolecules", "Polymers"),
    "Botany" to listOf("The Living World", "Biological Classification",
        "Plant Kingdom", "Morphology of Flowering Plants",
        "Anatomy of Flowering Plants", "Cell: The Unit of Life",
        "Cell Cycle & Division", "Transport in Plants", "Mineral Nutrition",
        "Photosynthesis", "Respiration in Plants", "Plant Growth",
        "Sexual Reproduction in Plants", "Principles of Inheritance",
        "Molecular Basis of Inheritance", "Evolution",
        "Strategies for Food Production", "Microbes in Human Welfare",
        "Biotechnology", "Organisms & Populations", "Ecosystem",
        "Biodiversity & Conservation", "Environmental Issues"),
    "Zoology" to listOf("Animal Kingdom", "Structural Organisation in Animals",
        "Locomotion & Movement", "Body Fluids & Circulation",
        "Excretory Products", "Neural Control & Coordination",
        "Chemical Coordination", "Digestion & Absorption",
        "Breathing & Gas Exchange", "Human Reproduction",
        "Reproductive Health", "Human Health & Disease", "Animal Husbandry")
)

val CATEGORIES_LIST = listOf("Important", "Tricky", "Repeated (PYQ)",
    "Formula Based", "Weak Topic", "Concept Based",
    "High Weightage", "Easy", "Revision", "Must Solve")

@AndroidEntryPoint
class SaveQuestionActivity : ComponentActivity() {

    private var onGalleryResult: ((Bitmap?) -> Unit)? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = uriToBitmap(it)
            onGalleryResult?.invoke(bmp)
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val stream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream)
        } catch (e: Exception) { null }
    }

    fun pickFromGallery(callback: (Bitmap?) -> Unit) {
        onGalleryResult = callback
        galleryLauncher.launch("image/*")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bitmap = CropHolder.croppedBitmap

        setContent {
            NEETQuestSaverTheme {
                SaveFormScreen(
                    initialBitmap = bitmap,
                    onPickGallery = { callback -> pickFromGallery(callback) },
                    onSaved = {
                        CropHolder.croppedBitmap = null
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveFormScreen(
    initialBitmap: Bitmap?,
    onPickGallery: ((Bitmap?) -> Unit) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    var bitmap by remember { mutableStateOf(initialBitmap) }
    var subject by remember { mutableStateOf("Physics") }
    var chapter by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Important") }
    var difficulty by remember { mutableStateOf("Medium") }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }
    val saveState by viewModel.saveState.collectAsState()

    val chaptersForSubject = CHAPTERS_MAP[subject] ?: emptyList()

    LaunchedEffect(subject) { chapter = chaptersForSubject.firstOrNull() ?: "" }

    LaunchedEffect(saveState.savedId) {
        if (saveState.savedId != null) {
            viewModel.resetSaveState()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Save Question", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val bmp = bitmap ?: return@Button
                        viewModel.saveQuestion(
                            bitmap = bmp,
                            subject = subject,
                            chapter = chapter,
                            category = category,
                            tags = selectedTags.toList(),
                            difficulty = difficulty,
                            notes = notes
                        )
                    },
                    enabled = bitmap != null && chapter.isNotBlank() && !saveState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (saveState.isSaving) {
                        CircularProgressIndicator(Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Save Question", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Question image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null,
                                Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                onPickGallery { bmp -> bitmap = bmp }
                            }) {
                                Text("Pick from Gallery")
                            }
                        }
                    }
                }
            }

            Text("Subject", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SUBJECTS_LIST) { subj ->
                    FilterChip(
                        selected = subject == subj,
                        onClick = { subject = subj },
                        label = { Text(subj) }
                    )
                }
            }

            FormDropdownSave(
                label = "Chapter *",
                value = chapter,
                options = chaptersForSubject,
                onValueChange = { chapter = it }
            )

            FormDropdownSave(
                label = "Category *
