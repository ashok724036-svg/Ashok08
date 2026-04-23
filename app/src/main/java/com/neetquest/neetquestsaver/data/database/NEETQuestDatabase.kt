package com.neetquest.neetquestsaver.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neetquest.neetquestsaver.data.dao.CategoryDao
import com.neetquest.neetquestsaver.data.dao.ChapterDao
import com.neetquest.neetquestsaver.data.dao.SavedQuestionDao
import com.neetquest.neetquestsaver.data.entity.Category
import com.neetquest.neetquestsaver.data.entity.Chapter
import com.neetquest.neetquestsaver.data.entity.SavedQuestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SavedQuestion::class, Chapter::class, Category::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NEETQuestDatabase : RoomDatabase() {

    abstract fun savedQuestionDao(): SavedQuestionDao
    abstract fun chapterDao(): ChapterDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: NEETQuestDatabase? = null

        fun getDatabase(context: Context): NEETQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NEETQuestDatabase::class.java,
                    "neetquest_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private suspend fun seedDatabase(database: NEETQuestDatabase) {
            // Seed chapters
            database.chapterDao().insertAll(getSeedChapters())
            // Seed categories
            database.categoryDao().insertAll(getSeedCategories())
        }

        private fun getSeedChapters(): List<Chapter> = buildList {

            // ── PHYSICS ──────────────────────────────────────────────────────
            val physicsChapters = listOf(
                "Physical World & Measurement" to "Unit 1",
                "Kinematics" to "Unit 1",
                "Laws of Motion" to "Unit 2",
                "Work, Energy and Power" to "Unit 2",
                "Motion of System of Particles & Rigid Body" to "Unit 3",
                "Gravitation" to "Unit 3",
                "Properties of Bulk Matter" to "Unit 4",
                "Thermodynamics" to "Unit 4",
                "Behaviour of Perfect Gas & Kinetic Theory" to "Unit 4",
                "Oscillations" to "Unit 5",
                "Waves" to "Unit 5",
                "Electrostatics" to "Unit 6",
                "Current Electricity" to "Unit 6",
                "Magnetic Effects of Current & Magnetism" to "Unit 7",
                "Electromagnetic Induction & Alternating Currents" to "Unit 7",
                "Electromagnetic Waves" to "Unit 8",
                "Optics" to "Unit 8",
                "Dual Nature of Matter & Radiation" to "Unit 9",
                "Atoms & Nuclei" to "Unit 9",
                "Electronic Devices" to "Unit 10"
            )
            physicsChapters.forEach { (name, unit) ->
                add(Chapter(subject = "Physics", name = name, unit = unit))
            }

            // ── CHEMISTRY ────────────────────────────────────────────────────
            val physicalChemChapters = listOf(
                "Some Basic Concepts of Chemistry" to "Physical",
                "Structure of Atom" to "Physical",
                "Classification of Elements & Periodicity" to "Physical",
                "Chemical Bonding & Molecular Structure" to "Physical",
                "States of Matter" to "Physical",
                "Thermodynamics" to "Physical",
                "Equilibrium" to "Physical",
                "Redox Reactions" to "Physical",
                "Solutions" to "Physical",
                "Electrochemistry" to "Physical",
                "Chemical Kinetics" to "Physical",
                "Surface Chemistry" to "Physical"
            )
            physicalChemChapters.forEach { (name, unit) ->
                add(Chapter(subject = "Chemistry", name = name, unit = unit))
            }

            val inorganicChemChapters = listOf(
                "General Principles & Processes of Isolation of Elements" to "Inorganic",
                "Hydrogen" to "Inorganic",
                "s-Block Elements (Alkali & Alkaline earth metals)" to "Inorganic",
                "p-Block Elements" to "Inorganic",
                "d and f Block Elements" to "Inorganic",
                "Coordination Compounds" to "Inorganic",
                "Environmental Chemistry" to "Inorganic"
            )
            inorganicChemChapters.forEach { (name, unit) ->
                add(Chapter(subject = "Chemistry", name = name, unit = unit))
            }

            val organicChemChapters = listOf(
                "Organic Chemistry – Basic Principles & Techniques" to "Organic",
                "Hydrocarbons" to "Organic",
                "Haloalkanes & Haloarenes" to "Organic",
                "Alcohols, Phenols & Ethers" to "Organic",
                "Aldehydes, Ketones & Carboxylic Acids" to "Organic",
                "Amines" to "Organic",
                "Biomolecules" to "Organic",
                "Polymers" to "Organic",
                "Chemistry in Everyday Life" to "Organic"
            )
            organicChemChapters.forEach { (name, unit) ->
                add(Chapter(subject = "Chemistry", name = name, unit = unit))
            }

            // ── BOTANY ───────────────────────────────────────────────────────
            val botanyChapters = listOf(
                "The Living World" to "Diversity of Life",
                "Biological Classification" to "Diversity of Life",
                "Plant Kingdom" to "Diversity of Life",
                "Morphology of Flowering Plants" to "Structural Organisation",
                "Anatomy of Flowering Plants" to "Structural Organisation",
                "Cell: The Unit of Life" to "Cell Biology",
                "Cell Cycle and Cell Division" to "Cell Biology",
                "Transport in Plants" to "Plant Physiology",
                "Mineral Nutrition" to "Plant Physiology",
                "Photosynthesis in Higher Plants" to "Plant Physiology",
                "Respiration in Plants" to "Plant Physiology",
                "Plant Growth and Development" to "Plant Physiology",
                "Sexual Reproduction in Flowering Plants" to "Reproduction",
                "Molecular Basis of Inheritance" to "Genetics & Evolution",
                "Principles of Inheritance and Variation" to "Genetics & Evolution",
                "Evolution" to "Genetics & Evolution",
                "Strategies for Enhancement in Food Production" to "Biology & Human Welfare",
                "Microbes in Human Welfare" to "Biology & Human Welfare",
                "Biotechnology – Principles and Processes" to "Biotechnology",
                "Biotechnology and its Applications" to "Biotechnology",
                "Organisms and Populations" to "Ecology",
                "Ecosystem" to "Ecology",
                "Biodiversity and Conservation" to "Ecology",
                "Environmental Issues" to "Ecology"
            )
            botanyChapters.forEach { (name, unit) ->
                add(Chapter(subject = "Botany", name = name, unit = unit))
            }

            // ── ZOOLOGY ──────────────────────────────────────────────────────
            val zoologyChapters = listOf(
                "Animal Kingdom" to "Diversity of Life",
                "Structural Organisation in Animals" to "Structural Organisation",
                "Locomotion and Movement" to "Human Physiology",
                "Body Fluids and Circulation" to "Human Physiology",
                "Excretory Products and their Elimination" to "Human Physiology",
                "Neural Control and Coordination" to "Human Physiology",
                "Chemical Coordination and Integration" to "Human Physiology",
                "Digestion and Absorption" to "Human Physiology",
                "Breathing and Exchange of Gases" to "Human Physiology",
                "Human Reproduction" to "Reproduction",
                "Reproductive Health" to "Reproduction",
                "Human Health and Disease" to "Biology & Human Welfare",
                "Animal Husbandry" to "Biology & Human Welfare"
            )
            zoologyChapters.forEach { (name, unit) ->
                add(Chapter(subject = "Zoology", name = name, unit = unit))
            }
        }

        private fun getSeedCategories(): List<Category> = listOf(
            Category(name = "Important", color = 0xFFE53935, isDefault = true),
            Category(name = "Tricky", color = 0xFFFF6F00, isDefault = true),
            Category(name = "Repeated (PYQ)", color = 0xFF7B1FA2, isDefault = true),
            Category(name = "Formula Based", color = 0xFF1565C0, isDefault = true),
            Category(name = "Weak Topic", color = 0xFFD32F2F, isDefault = true),
            Category(name = "Concept Based", color = 0xFF00695C, isDefault = true),
            Category(name = "High Weightage", color = 0xFFC62828, isDefault = true),
            Category(name = "Easy", color = 0xFF388E3C, isDefault = true),
            Category(name = "Revision", color = 0xFF0277BD, isDefault = true),
            Category(name = "Must Solve", color = 0xFF6A1B9A, isDefault = true)
        )
    }
}
