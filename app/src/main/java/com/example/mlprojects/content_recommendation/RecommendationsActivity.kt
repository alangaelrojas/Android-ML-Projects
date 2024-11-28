package com.example.mlprojects.content_recommendation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mlprojects.R

class RecommendationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendations_activity)
    }

    private fun search(query: String) {
        // Not implemented in demo app. Just log query instead.
        Log.d(TAG, query)
    }

    companion object {
        const val TAG = "RecommendationsActivity"

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, RecommendationsActivity::class.java))
        }

    }
}