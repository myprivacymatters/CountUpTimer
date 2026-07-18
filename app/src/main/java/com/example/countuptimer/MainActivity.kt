package com.example.countuptimer

import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountUpTimerApp()
        }
    }
}

@Composable
fun CountUpTimerApp() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("CountUpPrefs", Context.MODE_PRIVATE)

    var startDateEpoch by remember { mutableStateOf(prefs.getLong("startDate", LocalDate.now().toEpochDay())) }
    var labelText by remember { mutableStateOf(prefs.getString("label", "My Streak") ?: "") }
    var isStreakMode by remember { mutableStateOf(prefs.getBoolean("isStreakMode", true)) }
    var labelAbove by remember { mutableStateOf(prefs.getBoolean("labelAbove", true)) }

    var bgColor by remember { mutableStateOf(Color(prefs.getInt("bgColor", Color.Black.toArgb()))) }
    var fgColor by remember { mutableStateOf(Color(prefs.getInt("fgColor", Color.White.toArgb()))) }
    var labelColor by remember { mutableStateOf(Color(prefs.getInt("labelColor", Color.White.toArgb()))) }

    val todayEpoch = LocalDate.now().toEpochDay()
    val daysElapsed = todayEpoch - startDateEpoch
    val displayValue = if (isStreakMode) daysElapsed + 1 else daysElapsed
    val modeText = if (isStreakMode) "Days In Streak" else "Days Elapsed"

    val updateWidgets = {
        val intent = Intent(context, CountUpWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, CountUpWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }

    LaunchedEffect(startDateEpoch, labelText, isStreakMode, labelAbove, bgColor, fgColor, labelColor) {
        prefs.edit()
            .putLong("startDate", startDateEpoch)
            .putString("label", labelText)
            .putBoolean("isStreakMode", isStreakMode)
            .putBoolean("labelAbove", labelAbove)
            .putInt("bgColor", bgColor.toArgb())
            .putInt("fgColor", fgColor.toArgb())
            .putInt("labelColor", labelColor.toArgb())
            .apply()
        updateWidgets()
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            startDateEpoch = LocalDate.of(year, month + 1, dayOfMonth).toEpochDay()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        if (labelAbove) Text(labelText, color = labelColor, fontSize = 24.sp)
        Text("$displayValue", color = fgColor, fontSize = 84.sp)
        if (!labelAbove) Text(labelText, color = labelColor, fontSize = 24.sp)
        Text(modeText, color = labelColor, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(48.dp))

        Surface(color = Color.DarkGray.copy(alpha = 0.9f), shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Streak Name", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) { 
                    Text("Select Start Date") 
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { isStreakMode = !isStreakMode }) {
                        Text(if (isStreakMode) "Days In Streak" else "Days Elapsed")
                    }
                    Button(onClick = { labelAbove = !labelAbove }) {
                        Text(if (labelAbove) "Label Top" else "Label Bottom")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                ColorPickerRow("Background", bgColor) { bgColor = it }
                ColorPickerRow("Number", fgColor) { fgColor = it }
                ColorPickerRow("Label Text", labelColor) { labelColor = it }

                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            startDateEpoch = LocalDate.now().toEpochDay()
                            labelText = "New Streak"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Delete Streak") }

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            activity?.finishAffinity()
                        }
                    ) { Text("Shutdown App") }
                }
            }
        }
    }
}

@Composable
fun ColorPickerRow(title: String, currentColor: Color, onColorSelected: (Color) -> Unit) {
    val presetColors = listOf(Color.Black, Color.White, Color.DarkGray, Color.Blue, Color(0xFF4CAF50), Color.Red, Color(0xFF9C27B0))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Text("$title: ", color = Color.White, modifier = Modifier.width(100.dp))
        presetColors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(2.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (color == currentColor) 2.dp else 0.dp,
                        color = if (color == currentColor) Color.Yellow else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}
