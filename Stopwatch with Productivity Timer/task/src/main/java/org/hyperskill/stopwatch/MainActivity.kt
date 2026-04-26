package org.hyperskill.stopwatch

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    val handler = Handler(Looper.getMainLooper())
    var seconds = 0
    var switch = false
    var upperTimeLimitInput: Int? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var settingsButton: Button
    private lateinit var time: TextView

    private val notificationRequestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if(granted) {
                startStopwatch()
            }
        }

    private fun startStopwatch() {
        if(!switch) {
            switch = true
            progressBar.visibility = View.VISIBLE
            settingsButton.isEnabled = false
            tick()
        }
    }

    private fun tick() {
        handler.postDelayed({
            if(switch) {
                val limit = upperTimeLimitInput
                if(limit != null && seconds >= limit) {
                    time.setTextColor(Color.RED)
                }
                if(limit != null && limit > 0 && seconds == limit) {
                    val notification = NotificationCompat.Builder(this, "org.hyperskill")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Time's up!")
                        .setContentText("You reached the upper limit")
                        .setOnlyAlertOnce(true)
                        .build()

                    notification.flags = notification.flags or Notification.FLAG_INSISTENT
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(393939, notification)
                }
                seconds++
                val minutesToDisplay = seconds / 60
                val secondsToDisplay = seconds % 60
                val color = Color.rgb(
                    Random.nextInt(256),
                    Random.nextInt(256),
                    Random.nextInt(256)
                )
                progressBar.indeterminateTintList = ColorStateList.valueOf(color)
                tick()
                time.text = "%02d:%02d".format(minutesToDisplay, secondsToDisplay)
            }
        }, 1000)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Stopwatch"
            val descriptionText = "Focus time"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("org.hyperskill", name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        time = findViewById<TextView>(R.id.textView)
        time.setTextColor(ContextCompat.getColor(this, R.color.timerText))
        time.text = "00:00"

        val startButton = findViewById<Button>(R.id.startButton)
        val resetButton = findViewById<Button>(R.id.resetButton)

        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE
        settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.isEnabled = true


        startButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                    startStopwatch()

                } else {
                    notificationRequestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                startStopwatch()
            }
        }


        resetButton.setOnClickListener {
            switch = false
            settingsButton.isEnabled = true
            progressBar.visibility = View.GONE
            seconds = 0
            time.text = "00:00"
            time.setTextColor(ContextCompat.getColor(this, R.color.timerText))
        }

        settingsButton.setOnClickListener {
            val dialogSettings = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null, false)
            AlertDialog.Builder(this)
                .setMessage("Set upper limit in seconds")
                .setView(dialogSettings)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val editText = dialogSettings.findViewById<EditText>(R.id.upperLimitEditText)
                    val input = editText.text.toString()
                    upperTimeLimitInput = input.toIntOrNull()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }
}