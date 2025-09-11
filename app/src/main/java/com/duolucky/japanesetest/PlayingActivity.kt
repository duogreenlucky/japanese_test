package com.duolucky.japanesetest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.duolucky.japanesetest.databinding.ActivityPlayingBinding

class PlayingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val allAnswerQuestions = intent.getIntExtra("score", 0)
        val answerQuestions = intent.getIntExtra("score", 0)
        val wrongAnswerQuestions = intent.getIntExtra("score", 0)
        val waitingQuestions = intent.getIntExtra("score", 0)
        val questionsName = intent.getStringExtra("questionsName")
        binding.practicingQuestionsName.text = questionsName
        binding.answerQuestions.text = answerQuestions.toString()
        binding.answerWrongQuestions.text = wrongAnswerQuestions.toString()
        binding.answerAllQuestions.text = allAnswerQuestions.toString()
        binding.waitingQuestions.text= waitingQuestions.toString()
    }
}