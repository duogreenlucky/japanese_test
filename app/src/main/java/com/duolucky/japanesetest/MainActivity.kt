package com.duolucky.japanesetest

import android.app.Activity
import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room;
import com.duolucky.japanesetest.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

val cloudDatabase: DatabaseReference = Firebase.database.reference
val auth: FirebaseAuth = Firebase.auth
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        val RC_STATUS = 30
        val RC_LOGIN = 20
    }
    var userID = "no"
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cacheUserName = getSharedPreferences("jptest", Context.MODE_PRIVATE)
            .getString("userName", "error")
        binding.loginingAccount.text = cacheUserName

        val questionBaseName = getSharedPreferences("jptest", Context.MODE_PRIVATE)
            .getString("cacheQuestionBaseName", "error")
        if ( questionBaseName != "error" ) {
            binding.thisQuestionsName.text = questionBaseName
        } else {
            binding.thisQuestionsName.text = "找不到題庫"
        }

        val userStatus = Firebase.auth.currentUser
        if (userStatus != null) {
            userID = auth.uid.toString()
            CoroutineScope(Dispatchers.IO).launch {
                val userName = cloudDatabase.child("user").child(userID).child("name")
                    .get().await().value.toString()

                withContext(Dispatchers.Main) {
                    binding.loginingAccount.text = userName
                }
            }
        } else {
            binding.loginingAccount.text = "未登入"
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)

        if ( requestCode == RC_STATUS ) {
            if ( resultCode == Activity.RESULT_OK ) {
                Log.d("AppLog", "已收到題庫更新紀錄")
                val questionBaseName = getSharedPreferences("jptest", Context.MODE_PRIVATE)
                    .getString("cacheQuestionBaseName", "error")
                if ( questionBaseName != "error" ) {
                    binding.thisQuestionsName.text = questionBaseName
                } else {
                    binding.thisQuestionsName.text = "找不到題庫"
                }
            }
        } else if ( requestCode == RC_LOGIN ) {
            if ( resultCode == Activity.RESULT_OK ) {
                Log.d("AppLog", "已收到登入成功紀錄")
                userID = auth.uid.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    val userName = cloudDatabase.child("user").child(userID).child("name")
                        .get().await().value.toString()

                    withContext(Dispatchers.Main) {
                        binding.loginingAccount.text = userName
                    }
                }
            }
        }
    }

    fun practiceAll(view: View) { //進入練習畫面(練習全部)
//        Intent(this, PlayActivity::class.java).apply {
//            startActivityForResult(this, SCORE)
//        }
        val practicingQuestion = "題目"
        val score = 10
        Intent(this, PlayingActivity::class.java).apply {
            val bag = bundleOf()
            bag.putInt("score", score)
            bag.putString("questionsName", practicingQuestion)
//            intent.putExtra("questionsName", practicingQuestion)
//            intent.putExtra("score", score)
            putExtras(bag)
            startActivity(this)
        }
    } //進入練習畫面(練習全部)

//    fun addQuestionDatabase(view: View) {
//        Log.d("AppLog", "進入新增資料模式(工程)")
//        val questionData = arrayListOf<CacheQuestions>(
//            CacheQuestions("1Q","1A"),
//            CacheQuestions("2Q","2A"),
//            CacheQuestions("3Q","3A"),
//            CacheQuestions("4Q","4A"),
//            CacheQuestions("5Q","5A"),
//        )
//        val database = Room.databaseBuilder(
//                this,
//                CacheQuestionsDatabase::class.java,
//                "CacheQuestion"
//            ).fallbackToDestructiveMigration(false).build()
//        java.util.concurrent.Executors.newSingleThreadExecutor().execute {
//            for (question in questionData) {
//                database.CacheQuestionsDao().add(question)
//            }
//            Log.d("LogApp", "建立資料成功")
//            Toast.makeText(this, "建立資料成功", Toast.LENGTH_LONG).show()
//        }
//    } //進入新增資料模式(測試)

    fun loadingCloudBase(view: View) {
        val userStatus = Firebase.auth.currentUser
        if (userStatus != null) {
            Intent(this, CloudBaseActivity::class.java).apply {
                startActivityForResult(this, RC_STATUS)
            }
        } else {
            AlertDialog.Builder(this)
                .setMessage("請按下「登入」按鈕來登入帳號")
                .setTitle("沒有帳號！")
                .setPositiveButton("確定", null)
                .show()
        }
    } //按下下載雲端題庫按鈕，切換到CloudBaseActivity

    fun loginActivity(view: View) {
        val userStatus = Firebase.auth.currentUser
        if (userStatus != null) {
            Log.d("AppLog", "現在有帳戶登入狀態")
            Toast.makeText(this, "目前已登入。若要切換帳戶，請按登出按鈕。", Toast.LENGTH_LONG).show()
        } else {
            Intent(this, LoginActivity::class.java).apply {
                startActivityForResult(this, RC_LOGIN)
            }
        }

    } //按下登入按鈕的程式

    fun signOut(view: View) {
        Firebase.auth.signOut()
        binding.loginingAccount.text = "未登入"
        Log.d("AppLog", "帳號已登出")
        Toast.makeText(this, "登出成功", Toast.LENGTH_LONG).show()
    } //按下登出按鈕的程式

    fun cloudBaseActivity(view: View) {
        Intent(this, CloudBaseActivity::class.java).apply {
            startActivity(this)
        }
    } //進入CloudBaseActivity

    fun cacheQuestionViewerActivityButton(view: View) {
        val questionBaseName = getSharedPreferences("jptest", Context.MODE_PRIVATE)
            .getString("cacheQuestionBaseName", "error")
        Intent(this, CacheQuestionViewerActivity::class.java).apply {
            bundleOf().putString("questionBaseName", questionBaseName)
            startActivity(this)
        }
    }
}