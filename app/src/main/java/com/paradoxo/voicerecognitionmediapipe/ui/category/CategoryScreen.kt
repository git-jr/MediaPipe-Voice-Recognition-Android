package com.paradoxo.voicerecognitionmediapipe.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mediapipe.tasks.components.containers.Category
import com.paradoxo.voicerecognitionmediapipe.R
import kotlin.collections.forEach


@Composable
fun CategoryScreen(
    categories: List<Category>,
    modifier: Modifier = Modifier
) {

//    val maxScore = categories.maxOfOrNull { it.score() } ?: 1f // Talvez eu deixe isso dinâmico depois
    val maxScore = 1f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp, 16.dp)
    ) {
        categories.forEach { category ->
            if (category.score().isNaN()) return@forEach
            CategoryBar(category, maxScore)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CategoryBar(category: Category, maxScore: Float) {
    val progress = category.score() / maxScore
    val bgColor = getColorForCategory(category)
    val categoryName = category.categoryName().substring(2, category.categoryName().length)

    Row {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(25.dp)
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(bgColor.copy(0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(5.dp))
                    .background(bgColor)
            )
        }
    }

}

fun getColorForCategory(category: Category): Color {
    return when (category.index()) {
        0 -> Color(0xFF8E24AA)
        1 -> Color(0xFFE53935)
        2 -> Color(0xFF15FF2D)
        3 -> Color(0xFF49C2FF)
        else -> Color.Yellow
    }
}

fun getImageForCategory(index: Int): Int {
    return when (index) {
        0 -> R.drawable.coffee
        1 -> R.drawable.netflix
        2 -> R.drawable.palmeiras
        else -> R.drawable.transparent_bg
    }
}
