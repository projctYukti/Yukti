package com.example.yukti.Insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Insights(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Big Card at the Top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 20.dp)
                .clickable { },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Insights",
                    fontSize = 40.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Three Cards at the Bottom
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Card(
                    modifier = Modifier
                        .size(width = 110.dp, height = 125.dp)
                        .clickable { },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Name: Yukti",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Card(
                    modifier = Modifier
                        .size(width = 110.dp, height = 125.dp)
                        .clickable { },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Name: Yukti",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        // Big Card at the Top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 20.dp)
                .clickable { },
            elevation = CardDefaults.cardElevation(4.dp),

        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                Text(
                        text = "Insights",
                        fontSize = 40.sp,
                        style = MaterialTheme.typography.bodyMedium,

                    )

            }
        }
    }
}