package com.example.mlprojects.walking

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mlprojects.R
import com.example.mlprojects.databinding.ActivityRunningWalkingClassifierBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class RunningWalkingClassifierActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRunningWalkingClassifierBinding
    private var interpreter: Interpreter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRunningWalkingClassifierBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInterpreter()

        binding.btnStartMocking.setOnClickListener { readData() }
    }

    private fun setInterpreter() {
        try {
            interpreter = Interpreter(loadModelFile(assets, "running_walking.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readData() {
        try {

            val fileDescriptor = this.assets.open("mocked_data.csv")
            val reader = BufferedReader(InputStreamReader(fileDescriptor))

            binding.animationView.playAnimation()

            // Read lines and split by commas
            CoroutineScope(Dispatchers.Default).launch {
                reader.useLines { lines ->
                    lines.forEach { line ->
                        val values = line.split(",")
                        val testData = arrayOf(
                            floatArrayOf(
                                values[1].toFloat(),
                                values[2].toFloat(),
                                values[3].toFloat(),
                                values[4].toFloat(),
                                values[5].toFloat(),
                                values[6].toFloat()
                            )
                        )

                        // Run inference and get result
                        val result: Int = classify(testData)
                        withContext(Dispatchers.Main) {

                            val resultText = when (result) {
                                0 -> {
                                    binding.animationView.speed = 5f
                                    "Running"
                                }

                                1 -> {
                                    binding.animationView.speed = 0.5f
                                    "Walking"
                                }

                                2 -> {
                                    binding.animationView.speed = 0.0f
                                    "Stand"
                                }

                                else -> {
                                    binding.animationView.speed = 0.0f
                                    "NaN"
                                }
                            }


                            println("Prediction: $resultText")

                        }

                        delay(100)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Error", e)
            e.printStackTrace()
        }
    }

    // Method to load the .tflite model from the assets folder
    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Method to run inference on a 2D array input and return a single prediction label
    fun classify(inputData: Array<FloatArray>): Int {
        val outputData = Array(1) { FloatArray(3) }

        // Run inference
        interpreter?.run(inputData, outputData)


        // Find the class with the highest probability
        var predictedClassIndex = -1
        var maxProbability = Float.NEGATIVE_INFINITY
        for (i in 0 until outputData[0].size) {
            if (outputData[0][i] > maxProbability) {
                maxProbability = outputData[0][i]
                predictedClassIndex = i
            }
        }
        return predictedClassIndex

    }

    // Close the interpreter to release resources
    fun close() {
        interpreter?.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        close()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, RunningWalkingClassifierActivity::class.java))
        }
    }
}