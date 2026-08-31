package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ClearanceStatus {
    COMPLETED,
    ACTION_REQUIRED,
    PENDING,
    READY,
    LOCKED
}

enum class DocumentStatus {
    APPROVED,
    REJECTED,
    PENDING_REVIEW,
    NOT_UPLOADED
}

@Entity(tableName = "registered_students")
data class StudentUserEntity(
    @PrimaryKey val matricNumber: String,
    val fullName: String,
    val email: String,
    val department: String,
    val level: String = "ND II",
    val session: String = "2023/2024 Academic Session",
    val loginPin: String,
    val clearancePin: String = "JSP-CLR-001",
    val registrationDate: String = "Today",
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "clearance_stages")
data class ClearanceStage(
    @PrimaryKey val id: Int,
    val stageNumber: Int,
    val title: String,
    val department: String,
    val description: String,
    val status: ClearanceStatus,
    val approvalDate: String? = null,
    val rejectionReason: String? = null,
    val documentName: String? = null,
    val documentStatus: DocumentStatus = DocumentStatus.NOT_UPLOADED,
    val receiptNumber: String? = null,
    val paymentDate: String? = null,
    val isActionRequired: Boolean = false,
    val actionButtonText: String? = null,
    val isExpandedByDefault: Boolean = false,
    val primaryDocumentType: String? = null
)

@Entity(tableName = "clearance_documents")
data class ClearanceDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stageId: Int,
    val stageTitle: String,
    val documentType: String,
    val fileName: String,
    val fileUri: String? = null,
    val fileType: String = "IMAGE", // IMAGE, PDF, SCAN
    val receiptNumber: String? = null,
    val paymentDate: String? = null,
    val uploadDate: String = "Just now",
    val status: DocumentStatus = DocumentStatus.PENDING_REVIEW,
    val rejectionReason: String? = null,
    val remarks: String? = null
)

@Entity(tableName = "recent_activities")
data class ActivityItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timeAgo: String,
    val status: ClearanceStatus,
    val stageId: Int
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rejectedDocName: String? = null,
    val rejectedReason: String? = null,
    val actionButtonText: String? = null,
    val actionStageId: Int? = null,
    val isTyping: Boolean = false
)

@Entity(tableName = "alert_items")
data class AlertItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timeAgo: String,
    val isUrgent: Boolean = false,
    val isRead: Boolean = false,
    val stageId: Int? = null
)

data class StudentProfile(
    val studentId: String = "",
    val fullName: String = "",
    val email: String = "",
    val faculty: String = "School of Technology & Applied Sciences",
    val department: String = "Computer Science",
    val level: String = "ND II",
    val session: String = "2023/2024 Academic Session",
    val matricNumber: String = "",
    val clearancePin: String = "",
    val role: String = "student",
    val isLoggedIn: Boolean = false,
    val loginPin: String = "",
    val lastLoginTime: String = ""
)

data class StageRequirement(
    val stageId: Int,
    val departmentName: String,
    val primaryDocumentLabel: String,
    val requiredDocuments: List<String>,
    val guidelines: String,
    val defaultReceiptPrefix: String
)

object DepartmentRequirementsProvider {
    val jigawaPolyDepartments = listOf(
        "Computer Science",
        "Electrical / Electronic Engineering",
        "Civil Engineering Technology",
        "Mechanical Engineering Technology",
        "Science Laboratory Technology (SLT)",
        "Statistics & Mathematics",
        "Accountancy",
        "Business Administration & Management",
        "Public Administration",
        "Office Technology & Management",
        "Architectural Technology",
        "Building Technology",
        "Quantity Surveying",
        "Mass Communication",
        "Environmental Science",
        "Agricultural Technology",
        "Library & Information Science"
    )

    val jigawaPolySchools = listOf(
        "School of Technology & Applied Sciences",
        "School of Engineering Technology",
        "School of Business & Management Studies",
        "School of Environmental Studies",
        "School of General & Remedial Studies"
    )

    val requirements = listOf(
        StageRequirement(
            stageId = 1,
            departmentName = "Directorate of Admissions & Registration",
            primaryDocumentLabel = "JAMB / JSP Admission Letter",
            requiredDocuments = listOf(
                "JAMB Official Admission Letter / JSP Admission Slip",
                "Senior Secondary Certificate (SSCE / WAEC / NECO / NABTEB)",
                "JSP Acceptance Fee Payment E-Receipt",
                "Indigene Certificate & Birth Certificate"
            ),
            guidelines = "All academic credentials and polytechnic admission letters must be verified by the Admissions Directorate before clearance sign-off.",
            defaultReceiptPrefix = "JSP-ADM-"
        ),
        StageRequirement(
            stageId = 2,
            departmentName = "Polytechnic Central Library, Dutse",
            primaryDocumentLabel = "Library Clearance & Book Return Slip",
            requiredDocuments = listOf(
                "JSP Library Membership / Reader Card",
                "Book Return Slip (0 Books Outstanding)",
                "Library Fee Clearance Slip",
                "E-Library Portal Verification Slip"
            ),
            guidelines = "All borrowed reference books, journals, and project monographs must be returned to the Circulation Desk at the Central Library.",
            defaultReceiptPrefix = "JSP-LIB-"
        ),
        StageRequirement(
            stageId = 3,
            departmentName = "Academic Department & School",
            primaryDocumentLabel = "Departmental & ND/HND Project Sign-off",
            requiredDocuments = listOf(
                "Departmental Association Dues Receipt",
                "HOD Clearance Endorsement Form",
                "Final Year ND/HND Project Defense Approval Page",
                "SIWES / Industrial Training Logbook Clearance"
            ),
            guidelines = "Ensure your Project Supervisor, Departmental Clearance Officer, and HOD sign the official departmental clearance certificate.",
            defaultReceiptPrefix = "JSP-DPT-"
        ),
        StageRequirement(
            stageId = 4,
            departmentName = "Bursary & Accounts Directorate",
            primaryDocumentLabel = "School Fees & Sundry E-Receipt",
            requiredDocuments = listOf(
                "All Semesters Tuition E-Receipts (Remita RRR / Bank)",
                "Convocation & Clearance Processing Fee Slip",
                "Departmental & Laboratory Levy Receipt"
            ),
            guidelines = "All bank teller receipts and Remita transaction RRR numbers must be clearly legible and reconciled with Bursary records.",
            defaultReceiptPrefix = "JSP-BUR-"
        ),
        StageRequirement(
            stageId = 5,
            departmentName = "Directorate of Examinations & Records",
            primaryDocumentLabel = "Statement of Results & Exam Pass",
            requiredDocuments = listOf(
                "Statement of Academic Semester Results",
                "Examination Identity Pass Surrender",
                "Transcript & Certificate Clearance Slip"
            ),
            guidelines = "Academic records division verifies that all required semester credit units have been completed without carryovers.",
            defaultReceiptPrefix = "JSP-EXM-"
        ),
        StageRequirement(
            stageId = 6,
            departmentName = "Directorate of Sports & Physical Education",
            primaryDocumentLabel = "Sports Kit & Equipment Return Slip",
            requiredDocuments = listOf(
                "Sports Council Clearance Certificate",
                "Polytechnic Games Kit Return Form",
                "Sports Dues Payment Receipt"
            ),
            guidelines = "Surrender all sports tournament gear and verify fitness equipment clearance with the Sports Unit.",
            defaultReceiptPrefix = "JSP-SPT-"
        ),
        StageRequirement(
            stageId = 7,
            departmentName = "Dean of Student Affairs (DSA)",
            primaryDocumentLabel = "Hostel & Conduct Clearance Form",
            requiredDocuments = listOf(
                "Hall of Residence Room Clearance Form",
                "Disciplinary Committee Standing Certificate",
                "National Youth Service (NYSC) / Exemption Mobilization Form",
                "JSP Alumni Association Registration Slip"
            ),
            guidelines = "Student Affairs confirms good conduct, hostel damage clearance, and eligibility for NYSC mobilization or exemption certificate.",
            defaultReceiptPrefix = "JSP-DSA-"
        ),
        StageRequirement(
            stageId = 8,
            departmentName = "Academic Board & Registry",
            primaryDocumentLabel = "JSP National Diploma / Higher National Diploma Clearance Certificate",
            requiredDocuments = listOf(
                "Consolidated 7-Stage Digital Clearance Seal",
                "Polytechnic ID Card Surrender Receipt",
                "Academic Board Graduation Approval"
            ),
            guidelines = "Upon successful verification of all 7 prerequisite departmental clearances, the Registrar issues the official Jigawa State Polytechnic Clearance Certificate.",
            defaultReceiptPrefix = "JSP-REG-"
        )
    )

    fun getRequirementForStage(stageId: Int): StageRequirement {
        return requirements.find { it.stageId == stageId } ?: requirements[0]
    }
}
