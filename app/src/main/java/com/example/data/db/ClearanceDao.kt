package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.ActivityItem
import com.example.data.AlertItem
import com.example.data.ChatMessage
import com.example.data.ClearanceDocument
import com.example.data.ClearanceStage
import com.example.data.StudentUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClearanceDao {
    // User Authentication & Registration
    @Query("SELECT * FROM registered_students WHERE LOWER(matricNumber) = LOWER(:matricNumber) LIMIT 1")
    suspend fun getStudentByMatric(matricNumber: String): StudentUserEntity?

    @Query("SELECT * FROM registered_students WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInStudent(): StudentUserEntity?

    @Query("SELECT * FROM registered_students WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInStudentFlow(): Flow<StudentUserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentUserEntity)

    @Update
    suspend fun updateStudent(student: StudentUserEntity)

    @Query("UPDATE registered_students SET isLoggedIn = 0")
    suspend fun logoutAllStudents()

    // Stages
    @Query("SELECT * FROM clearance_stages ORDER BY stageNumber ASC")
    fun getAllStages(): Flow<List<ClearanceStage>>

    @Query("SELECT * FROM clearance_stages WHERE id = :stageId")
    suspend fun getStageById(stageId: Int): ClearanceStage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStages(stages: List<ClearanceStage>)

    @Update
    suspend fun updateStage(stage: ClearanceStage)

    // Documents
    @Query("SELECT * FROM clearance_documents ORDER BY id DESC")
    fun getAllDocuments(): Flow<List<ClearanceDocument>>

    @Query("SELECT * FROM clearance_documents WHERE stageId = :stageId ORDER BY id DESC")
    fun getDocumentsByStage(stageId: Int): Flow<List<ClearanceDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: ClearanceDocument): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<ClearanceDocument>)

    @Query("DELETE FROM clearance_documents WHERE id = :docId")
    suspend fun deleteDocument(docId: Int)

    // Activities
    @Query("SELECT * FROM recent_activities ORDER BY id DESC")
    fun getRecentActivities(): Flow<List<ActivityItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityItem>)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    fun getChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()

    // Alerts
    @Query("SELECT * FROM alert_items ORDER BY id DESC")
    fun getAlerts(): Flow<List<AlertItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertItem>)

    @Query("UPDATE alert_items SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertAsRead(alertId: Int)

    @Query("DELETE FROM clearance_stages")
    suspend fun clearStages()

    @Query("DELETE FROM clearance_documents")
    suspend fun clearDocuments()

    @Query("DELETE FROM recent_activities")
    suspend fun clearActivities()

    @Query("DELETE FROM alert_items")
    suspend fun clearAlerts()
}

