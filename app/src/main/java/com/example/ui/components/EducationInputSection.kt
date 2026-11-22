package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.BangladeshData
import com.example.data.EducationEntry
import com.example.ui.screens.FormDropdownField

@Composable
fun EducationInputSection(
    educations: List<EducationEntry>,
    onEducationsChange: (List<EducationEntry>) -> Unit,
    modifier: Modifier = Modifier,
    maxEntries: Int = 10
) {
    val entries = educations.ifEmpty {
        listOf(EducationEntry(courseName = BangladeshData.courseNames.first()))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Educational Qualifications (একাধিক ডিগ্রি যোগ করা যাবে)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        entries.forEachIndexed { index, entry ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Degree #${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entries.size > 1) {
                            IconButton(
                                onClick = {
                                    onEducationsChange(entries.filterIndexed { i, _ -> i != index })
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("remove_degree_button_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Remove Degree ${index + 1}",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val courseOptions = (BangladeshData.courseNames + entry.courseName)
                            .filter { it.isNotBlank() }
                            .distinct()
                        FormDropdownField(
                            label = "Course Name *",
                            selected = entry.courseName.ifBlank { BangladeshData.courseNames.first() },
                            options = courseOptions,
                            onSelect = { selectedCourse ->
                                val next = entries.toMutableList()
                                next[index] = entry.copy(courseName = selectedCourse)
                                onEducationsChange(next)
                            },
                            modifier = Modifier.weight(1.25f)
                        )

                        OutlinedTextField(
                            value = entry.major,
                            onValueChange = { input ->
                                val next = entries.toMutableList()
                                next[index] = entry.copy(major = input.take(60))
                                onEducationsChange(next)
                            },
                            label = { Text("Major") },
                            placeholder = { Text("e.g., Midwifery / ICU") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edu_major_$index")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = entry.university,
                            onValueChange = { input ->
                                val next = entries.toMutableList()
                                next[index] = entry.copy(university = input.take(100))
                                onEducationsChange(next)
                            },
                            label = { Text("University / Board") },
                            placeholder = { Text("e.g., BSMMU / DU / BNMC") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.45f)
                                .testTag("edu_university_$index")
                        )

                        OutlinedTextField(
                            value = entry.passedYear,
                            onValueChange = { input ->
                                val next = entries.toMutableList()
                                next[index] = entry.copy(
                                    passedYear = input.filter { it.isDigit() }.take(4)
                                )
                                onEducationsChange(next)
                            },
                            label = { Text("Passed Year") },
                            placeholder = { Text("2021") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.75f)
                                .testTag("edu_year_$index")
                        )
                    }
                }
            }
        }

        if (entries.size < maxEntries) {
            OutlinedButton(
                onClick = {
                    val next = entries + EducationEntry(courseName = BangladeshData.courseNames.first())
                    onEducationsChange(next)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_another_degree_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Another Degree (আরও ডিগ্রি যোগ করুন)")
            }
        }
    }
}
