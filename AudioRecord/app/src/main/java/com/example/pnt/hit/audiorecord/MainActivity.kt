package com.example.pnt.hit.audiorecord

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pnt.hit.audiorecord.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private val REQUEST_CODE = 200

    private var permission = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    private var dirPath = ""
    private var fileName = ""

    @SuppressLint("SimpleDateFormat")
    private var simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh:mm.ss")
    private var isRecording = false
    private var isPause = false

    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var amplitudes: ArrayList<Float>
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheet: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        setListener()
    }

    private fun init() {
        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permission[0]
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) ActivityCompat.requestPermissions(
            this,
            permission,
            REQUEST_CODE
        )

        timer = Timer(this)

        vibrator =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        bottomSheet = findViewById<ConstraintLayout>(R.id.bottomMainSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setListener() {
        binding.btRecord.setOnClickListener {
            when {
                isPause -> resumeRecorder()
                isRecording -> pauseRecording()
                else -> startRecording()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        }

        binding.btMenu.setOnClickListener {
            Toast.makeText(this, "Menu click", Toast.LENGTH_LONG).show()
        }

        binding.btDone.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            stopRecorder()
            binding.bottomSheetBackground.visibility = View.VISIBLE
            binding.bottomMainSheet.edFileName.setText(fileName)
        }

        binding.bottomMainSheet.btCancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.bottomMainSheet.btOk.setOnClickListener {
            dismiss()
            save()
        }

        binding.bottomSheetBackground.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        binding.btDelete.setOnClickListener {
            stopRecorder()
            File("$dirPath$fileName.mp3").delete()
        }

        binding.btDelete.isClickable = false
    }

    private fun dismiss() {
        binding.bottomSheetBackground.visibility = View.GONE

//        hideKeyboard(binding.bottomMainSheet.edFileName)

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)
    }

    private fun save() {
        val newFilename = binding.bottomMainSheet.edFileName.text.toString()
        if(newFilename != fileName) {
            val newFile = File("$dirPath$newFilename.mp3")
            File("$dirPath$fileName.mp3").renameTo(newFile)
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permission, REQUEST_CODE)
            return
        }

        // start recoding
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }

        val date = simpleDateFormat.format(Date())

        dirPath = "${externalCacheDir?.absolutePath}/"
        fileName = "audio_record_$date"
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            start()
        }

        binding.btRecord.setImageResource(R.drawable.ic_pause)

        isRecording = true
        isPause = false

        timer.start()

        binding.btDelete.isClickable = true
        binding.btDelete.setImageResource(R.drawable.ic_delete)

        binding.btMenu.visibility = View.GONE
        binding.btDone.visibility = View.VISIBLE
    }

    private fun resumeRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.resume()
        }
        isRecording = true
        isPause = false
        binding.btRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.pause()
        }
        isRecording = false
        isPause = true
        binding.btRecord.setImageResource(R.drawable.ic_play)

        timer.pause()
    }

    @SuppressLint("SetTextI18n")
    private fun stopRecorder() {
        timer.stop()

        mediaRecorder.apply {
            stop()
            release()
        }

        isPause = false
        isRecording = false

        binding.btMenu.visibility = View.VISIBLE
        binding.btDone.visibility = View.GONE

        binding.btDelete.isClickable = false
        binding.btDelete.setImageResource(R.drawable.ic_delete_disable)

        binding.btRecord.setImageResource(R.drawable.ic_play)

        binding.tvTimer.text = "00:00.00"

        amplitudes = binding.waveformView.clear()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        binding.waveformView.addAmplitude(mediaRecorder.maxAmplitude.toFloat())
    }
}