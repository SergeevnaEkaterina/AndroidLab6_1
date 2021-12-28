package ru.spbstu.icc.kspt.lab2.continuewatch

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class MainActivity : AppCompatActivity() {
    private lateinit var textSecondsLeft: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var secondsLeft: Int = 0
    private var timeOfStart: Long = 0
    private var timeOfEnd: Long = 0
    private lateinit var taskFromBackGround: Future<*>

    private fun increment(first: Long, second: Long): Long {
        return ((first - second) / 1000)
    }

    private fun convertValueToString(time: Long): String {
        return getString(
            R.string.text_view, time
        )
    }

    private fun logMessage(action: String) {
        Log.i("thread", "${Thread.currentThread()}" + action)
    }

    private fun getFromSharedPreferences(key: String, value: Int): Int {
        return sharedPreferences.getInt(key, value)
    }

    private fun putToSharedPreferences(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            apply()
        }
    }

    private fun runBackGround(executorService: ExecutorService) = executorService.submit {
        while (true) {
            Thread.sleep(1000)
            logMessage("is executing")
            textSecondsLeft.post {
                textSecondsLeft.text = convertValueToString(
                    secondsLeft
                            + increment(System.currentTimeMillis(), timeOfStart)
                )

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textSecondsLeft = findViewById(R.id.textSecondsLeft)
        sharedPreferences = getSharedPreferences("Seconds left", Context.MODE_PRIVATE)
        textSecondsLeft.text =
            convertValueToString(getFromSharedPreferences("Seconds left", secondsLeft).toLong())

    }


    override fun onStart() {
        timeOfStart = System.currentTimeMillis()
        logMessage("started")
        secondsLeft = sharedPreferences.getInt("Seconds left", secondsLeft)
        taskFromBackGround = runBackGround(MainApp.executorService)
        super.onStart()
    }


    override fun onStop() {
        taskFromBackGround.cancel(true)
        logMessage("stopped")
        timeOfEnd = System.currentTimeMillis()
        secondsLeft += increment(timeOfEnd, timeOfStart).toInt()
        putToSharedPreferences("Seconds left", secondsLeft)
        super.onStop()
    }

}
