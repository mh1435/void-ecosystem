package com.voidecosystem.feature.notes

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class NoteAudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(): File {
        val file = File(context.filesDir, "note_audio_${System.currentTimeMillis()}.m4a")
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mediaRecorder
        outputFile = file
        return file
    }

    /** Stops the active recording and returns the saved file, or null if nothing was captured. */
    fun stop(): File? {
        val active = recorder ?: return null
        return try {
            active.stop()
            active.release()
            recorder = null
            outputFile
        } catch (e: Exception) {
            active.release()
            recorder = null
            outputFile?.delete()
            null
        }
    }

    fun cancel() {
        val active = recorder
        recorder = null
        try {
            active?.stop()
        } catch (e: Exception) {
            // Recording never actually started producing data — nothing to clean up beyond the file below.
        } finally {
            active?.release()
            outputFile?.delete()
            outputFile = null
        }
    }
}

class NoteAudioPlayer {
    private var player: MediaPlayer? = null

    fun play(path: String, onCompletion: () -> Unit) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener {
                onCompletion()
                stop()
            }
            prepare()
            start()
        }
    }

    fun stop() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }
}
