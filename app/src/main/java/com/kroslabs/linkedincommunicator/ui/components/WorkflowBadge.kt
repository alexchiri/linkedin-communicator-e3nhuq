package com.kroslabs.linkedincommunicator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kroslabs.linkedincommunicator.data.model.WorkflowStage
import com.kroslabs.linkedincommunicator.ui.theme.WorkflowColors

@Composable
fun WorkflowBadge(
    stage: WorkflowStage,
    modifier: Modifier = Modifier
) {
    Text(
        text = stage.displayName,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(
                color = WorkflowColors.getColor(stage),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
