package com.example.assignemtn.view

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.example.assignemtn.R
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var videoLayout: VLCVideoLayout
    private lateinit var playButton: Button
    private lateinit var pipButton: Button
    private lateinit var recordButton: Button
    private lateinit var urlInput: EditText

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer


    private var isRecording = false
    private var currentRecordingPath: String = ""


    private val REQUEST_CODE_PERMISSIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        videoLayout = findViewById(R.id.videoSurface)
        playButton = findViewById(R.id.playButton)
        pipButton = findViewById(R.id.pipButton)
        recordButton = findViewById(R.id.recordButton)
        urlInput = findViewById(R.id.rtsUrlEditText)


        libVLC = LibVLC(this)
        mediaPlayer = MediaPlayer(libVLC)


        mediaPlayer.attachViews(videoLayout, null, false, false)


        playButton.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotBlank()) {
                playStream(url)
            } else {
                Toast.makeText(this, "Enter a valid RTSP URL", Toast.LENGTH_SHORT).show()
            }
        }

        pipButton.setOnClickListener {
            enterPipMode()
        }

        recordButton.setOnClickListener {

            if (mediaPlayer.isPlaying) {
                handlePermissionsAndRecord()
            } else {
                Toast.makeText(this, "Start playing a stream first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playStream(url: String) {

        mediaPlayer.stop()
        if (isRecording) {

        }
        mediaPlayer.detachViews()
        mediaPlayer.attachViews(videoLayout, null, false, false)


        val media = Media(libVLC, Uri.parse(url))
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=150")
        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
        Log.d("MainActivity", "Playing stream: $url")
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val aspectRatio = Rational(videoLayout.width, if (videoLayout.height > 0) videoLayout.height else 1)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(params)
        } else {
            Toast.makeText(this, "PiP not supported on your device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePermissionsAndRecord() {

        val permissionsToRequest = mutableListOf<String>()


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        } else {
            toggleRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                toggleRecording()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleRecording() {
        val rtspUrl = urlInput.text.toString()
        if (rtspUrl.isBlank()) {
            Toast.makeText(this, "Enter a valid URL", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isRecording) {

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outputFile = File(downloadsDir, "recording_${System.currentTimeMillis()}.mp4")
            currentRecordingPath = outputFile.absolutePath


            val command = arrayOf(
                "-i", rtspUrl,
                "-c", "copy",
                "-f", "mp4",
                currentRecordingPath
            ).joinToString(" ")


            FFmpegKit.executeAsync(command) { session ->
                val returnCode = session.returnCode
                runOnUiThread {
                    if (returnCode.isValueSuccess) {
                        Toast.makeText(this, "Recording saved to ${outputFile.name}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show()
                    }
                    isRecording = false
                    recordButton.text = "Start Recording"
                }
            }
            isRecording = true
            recordButton.text = "Stop Recording"
            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
        } else {

            Toast.makeText(this, "kill the app to save recording", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop()
    {
        super.onStop()
        if (!isInPictureInPictureMode)
        {
            mediaPlayer.pause()
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        mediaPlayer.stop()

        mediaPlayer.detachViews()
        mediaPlayer.release()
        libVLC.release()
    }
}
