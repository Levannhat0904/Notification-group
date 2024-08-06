package net.braniumacademy.example711

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private var notificationId: Int = 1
    private lateinit var containerLayout: View
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setupNotification()
        } else {
            Snackbar.make(
                containerLayout, R.string.txt_permission_denied,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    private lateinit var editNotificationContent: EditText
    private var counter = 1
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        editNotificationContent = findViewById(R.id.edit_notification_content)
        val btnPostNotification = findViewById<Button>(R.id.btn_post_notification)
        btnPostNotification.setOnClickListener {
            checkPostNotificationPermission()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // đăng ký channel với hệ thống
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPostNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        setupNotification()
    }

    @SuppressLint("MissingPermission")
    private fun setupNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.text_notification_title))
            .setContentText(editNotificationContent.text.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(GROUP_KEY_NEW_MESSAGE)

        messages.add(
            Message(
                notificationId++,
                editNotificationContent.text.toString(),
                builder.build()
            )
        )
        val notificationStyle = NotificationCompat.InboxStyle()
        for (item in messages) {
            notificationStyle.addLine(item.message)
        }
        notificationStyle.setBigContentTitle("${messages.size} new messages")
        notificationStyle.setSummaryText("lancutecute@xmail.com")
        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Message Summary")
            .setStyle(notificationStyle)
            .setGroup(GROUP_KEY_NEW_MESSAGE)
            .setGroupSummary(true)
            .build()

        with(NotificationManagerCompat.from(this)) {
            for (item in messages) {
                notify(item.id, item.notification)
            }
            notify(SUMMARY_ID, summaryNotification)
        }
        editNotificationContent.text.clear()
        if (counter % 5 == 0) {
            messages.clear()
        }
        counter++
    }

    companion object {
        const val CHANNEL_ID = "net.braniumacademy.example711.CHANNEL_1"
        const val GROUP_KEY_NEW_MESSAGE = "NEW_MESSAGE"
        const val SUMMARY_ID = 0
    }
}