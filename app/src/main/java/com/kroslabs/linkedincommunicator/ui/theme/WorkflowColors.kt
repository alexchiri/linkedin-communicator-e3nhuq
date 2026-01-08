package com.kroslabs.linkedincommunicator.ui.theme

import androidx.compose.ui.graphics.Color
import com.kroslabs.linkedincommunicator.data.model.WorkflowStage

object WorkflowColors {
    fun getColor(stage: WorkflowStage): Color {
        return when (stage) {
            WorkflowStage.DRAFT -> Color(0xFFFFC107) // Yellow
            WorkflowStage.PROOFREAD -> Color(0xFFFF9800) // Orange
            WorkflowStage.CONDENSED -> Color(0xFF2196F3) // Blue
            WorkflowStage.TRANSLATED -> Color(0xFF4CAF50) // Green
            WorkflowStage.REVIEWED -> Color(0xFF009688) // Teal
            WorkflowStage.READY_TO_POST -> Color(0xFF9C27B0) // Purple
        }
    }
}
