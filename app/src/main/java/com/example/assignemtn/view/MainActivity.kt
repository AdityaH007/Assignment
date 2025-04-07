package com.example.assignemtn.view

import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.assignemtn.R
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var surfaceView: SurfaceView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //LibVlc setup
        val args = arrayListOf("--no-drop-late-frames", "--no-skip-frames")
        libVLC = LibVLC(this, args)
        mediaPlayer = MediaPlayer(libVLC)

        //set up surface view
        surfaceView = findViewById(R.id.videoSurface)
        surfaceView.holder.addCallback(this)

        val playButton = findViewById<AppCompatButton>(R.id.platButton)
        val urledittext = findViewById<AppCompatEditText>(R.id.rtsUrlEditText)


        playButton.setOnClickListener {
            val rtspUrl = urledittext.text.toString()
            if (rtspUrl.isNotEmpty()) {
                playRtspStream(rtspUrl)
            }
        }


    }

    private fun playRtspStream(url: kotlin.String)
    {
        val media = Media(libVLC, Uri.parse(url))
        mediaPlayer.media = media
        mediaPlayer.play()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        mediaPlayer.vlcVout.setVideoSurface(p0.surface,null)
        mediaPlayer.vlcVout.attachViews()
    }

    override fun surfaceChanged(
        p0: SurfaceHolder,
        p1: Int,
        p2: Int,
        p3: Int
    ) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
       mediaPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        libVLC.release()
    }
}





