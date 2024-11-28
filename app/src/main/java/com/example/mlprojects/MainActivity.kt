package com.example.mlprojects

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mlprojects.animals.AnimalsActivity
import com.example.mlprojects.content_recommendation.RecommendationsActivity
import com.example.mlprojects.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAnimals.setOnClickListener { AnimalsActivity.startActivity(this) }
        binding.btnRecommendations.setOnClickListener { RecommendationsActivity.startActivity(this) }
        binding.btnRunningWalking.setOnClickListener { }

    }
}