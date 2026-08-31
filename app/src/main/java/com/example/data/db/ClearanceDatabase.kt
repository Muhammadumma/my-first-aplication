package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.ActivityItem
import com.example.data.AlertItem
import com.example.data.ChatMessage
import com.example.data.ClearanceDocument
import com.example.data.ClearanceStage
import com.example.data.ClearanceStatus
import com.example.data.DocumentStatus
import com.example.data.StudentUserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentUserEntity::class,
        ClearanceStage::class,
        ClearanceDocument::class,
        ActivityItem::class,
        ChatMessage::class,
        AlertItem::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ClearanceDatabase : RoomDatabase() {
    abstract fun clearanceDao(): ClearanceDao

    companion object {
        @Volatile
        private var INSTANCE: ClearanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ClearanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClearanceDatabase::class.java,
                    "jigawa_poly_clearance_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

fun createCleanJigawaPolyStages(): List<ClearanceStage> {
    return listOf(
        ClearanceStage(
            id = 1,
            stageNumber = 1,
            title = "Admissions & Registration",
            department = "Directorate of Admissions & Registration",
            description = "Upload your JAMB Admission Letter / JSP Admission slip, O'Level Certificate, and Acceptance Fee receipt.",
            status = ClearanceStatus.READY,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload Credentials",
            primaryDocumentType = "JAMB / JSP Admission Letter",
            isExpandedByDefault = true
        ),
        ClearanceStage(
            id = 2,
            stageNumber = 2,
            title = "Polytechnic Library",
            department = "Polytechnic Central Library, Dutse",
            description = "Return all borrowed library books, journals, and surrender your JSP Reader Card.",
            status = ClearanceStatus.PENDING,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload Library Slip",
            primaryDocumentType = "Library Clearance & Book Return Slip"
        ),
        ClearanceStage(
            id = 3,
            stageNumber = 3,
            title = "Academic Department",
            department = "Academic Department & School",
            description = "Final year ND/HND project defense sign-off, departmental dues payment, and HOD endorsement.",
            status = ClearanceStatus.PENDING,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload Project Sign-off",
            primaryDocumentType = "Departmental & ND/HND Project Sign-off"
        ),
        ClearanceStage(
            id = 4,
            stageNumber = 4,
            title = "Bursary & Accounts",
            department = "Bursary & Accounts Directorate",
            description = "Upload bank teller / Remita e-receipts for all semesters tuition and clearance processing fees.",
            status = ClearanceStatus.PENDING,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload School Fees Receipt",
            primaryDocumentType = "School Fees & Sundry E-Receipt"
        ),
        ClearanceStage(
            id = 5,
            stageNumber = 5,
            title = "Examinations & Records",
            department = "Directorate of Examinations & Records",
            description = "Statement of academic semester results validation and examination pass surrender.",
            status = ClearanceStatus.PENDING,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload Result Statement",
            primaryDocumentType = "Statement of Results & Exam Pass"
        ),
        ClearanceStage(
            id = 6,
            stageNumber = 6,
            title = "Sports & Physical Education",
            department = "Directorate of Sports & Physical Education",
            description = "Surrender polytechnic sports kit and verify physical education clearance.",
            status = ClearanceStatus.PENDING,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload Sports Slip",
            primaryDocumentType = "Sports Kit & Equipment Return Slip"
        ),
        ClearanceStage(
            id = 7,
            stageNumber = 7,
            title = "Student Affairs Division",
            department = "Dean of Student Affairs (DSA)",
            description = "Hostel room key surrender, disciplinary conduct clearance, and NYSC/Exemption mobilization form.",
            status = ClearanceStatus.PENDING,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Upload DSA Form",
            primaryDocumentType = "Hostel & Conduct Clearance Form"
        ),
        ClearanceStage(
            id = 8,
            stageNumber = 8,
            title = "Academic Board & Registry",
            department = "Academic Board & Registry",
            description = "Issuance of official Jigawa State Polytechnic National Diploma (ND) / Higher National Diploma (HND) Certificate.",
            status = ClearanceStatus.LOCKED,
            documentStatus = DocumentStatus.NOT_UPLOADED,
            actionButtonText = "Generate Certificate",
            primaryDocumentType = "JSP National Diploma / Higher National Diploma Clearance Certificate"
        )
    )
}

