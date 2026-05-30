package com.example.snapmind.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.snapmind.core.ai.AutoTagRuleEngine
import com.example.snapmind.data.local.dao.ClassificationDao
import com.example.snapmind.data.local.dao.MemoryItemDao
import com.example.snapmind.data.local.dao.MemorySearchDao
import com.example.snapmind.data.local.dao.OcrTextDao
import com.example.snapmind.data.local.dao.VisionLabelDao
import com.example.snapmind.data.local.entity.StandardProcessingStatus
import com.example.snapmind.data.repository.MemoryAggregateBuilder
import com.example.snapmind.data.repository.TagAssigner
import com.example.snapmind.data.repository.refreshFtsRow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoTaggingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val memoryItemDao: MemoryItemDao,
    private val ocrTextDao: OcrTextDao,
    private val classificationDao: ClassificationDao,
    private val visionLabelDao: VisionLabelDao,
    private val memorySearchDao: MemorySearchDao,
    private val ruleEngine: AutoTagRuleEngine,
    private val tagAssigner: TagAssigner,
    private val aggregateBuilder: MemoryAggregateBuilder,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val memoryId = inputData.getLong(LocalMemoryProcessingWorker.KEY_MEMORY_ID, -1L)
        if (memoryId <= 0L) return Result.failure()
        if (memoryItemDao.getById(memoryId) == null) return Result.success()

        val now = System.currentTimeMillis()
        memoryItemDao.setTaggingStatus(memoryId, StandardProcessingStatus.RUNNING, now)

        return try {
            val ocrText = ocrTextDao.getByMemoryId(memoryId)?.fullText
            val classifications = classificationDao.getByMemoryId(memoryId)
            val visionLabels = visionLabelDao.getByMemoryId(memoryId)

            val requests = ruleEngine.buildAssignments(ocrText, classifications, visionLabels)
            tagAssigner.assignAll(memoryId, requests, System.currentTimeMillis())

            refreshFtsRow(memoryId, memoryItemDao, aggregateBuilder, memorySearchDao)
            memoryItemDao.setTaggingStatus(memoryId, StandardProcessingStatus.SUCCESS, System.currentTimeMillis())
            Result.success()
        } catch (t: Throwable) {
            memoryItemDao.setTaggingStatus(memoryId, StandardProcessingStatus.FAILED, System.currentTimeMillis())
            Result.failure()
        }
    }
}
