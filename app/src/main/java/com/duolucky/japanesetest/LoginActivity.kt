package com.duolucky.japanesetest

import android.R.attr.password
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.duolucky.japanesetest.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.geo.type.Viewport
import org.checkerframework.checker.interning.qual.Interned

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        val backButton = binding.backButton
        backButton.setOnClickListener {
            finish()
        }

        Log.d("AppLog", "沒有帳戶登入中")
        val loginStatus = getSharedPreferences("jptest", Context.MODE_PRIVATE)
            .getBoolean("loginStatus", false)
        if (loginStatus) {
            Log.d("AppLog", "之前登入過了")
            val email = getSharedPreferences("jptest", Context.MODE_PRIVATE)
                .getString("userEmail", "no_email").toString()
            val password = getSharedPreferences("jptest", Context.MODE_PRIVATE)
                .getString("userPassword", "no_password").toString()
            val msg1 = "是否要登入先前登入過的帳號？帳號："
            AlertDialog.Builder(this)
                .setMessage("$msg1 $email")
                .setTitle("找到帳號登入紀錄")
                .setPositiveButton("是") { dialog, which ->
                    AlertDialog.Builder(this)
                        .setTitle("登入中...")
                        .setMessage("請稍後...")
                        .show()
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("AppLog", "登入成功")
                                Toast.makeText(this, "登入成功", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Log.w("AppLog", "登入失敗", task.exception)
                                AlertDialog.Builder(this)
                                    .setMessage("請重新輸入帳號密碼")
                                    .setTitle("登入失敗")
                                    .setPositiveButton("確定", null)
                                    .show()
                                binding.emailInputCase.setText(email)
                            }
                        }
                }
                .setNeutralButton("否") { dialog, which ->
                    getSharedPreferences("jptest", Context.MODE_PRIVATE)
                        .edit {
                            putString("userEmail", "no_email")
                            putString("userPassword", "no_password")
                            putBoolean("loginStatus", false)
                        }
                }
                .show()
        }
    }

    fun login(view: View) {
        Log.d("AppLog", "按下登入按鈕")
        val email = binding.emailInputCase.text.toString()
        val password = binding.PasswordImputCase.text.toString()
        AlertDialog.Builder(this)
            .setTitle("登入中...")
            .setMessage("請稍後...")
            .show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("AppLog", "登入成功")
                    Toast.makeText(this, "登入成功", Toast.LENGTH_LONG).show()
                    getSharedPreferences("jptest", Context.MODE_PRIVATE)
                        .edit {
                            putString("userEmail", email)
                            putString("userPassword", password)
                            putBoolean("loginStatus", true)
                        }
                    finish()
                } else {
                    Log.w("AppLog", "登入失敗", task.exception)
                    AlertDialog.Builder(this)
                        .setMessage("登入失敗，請重新輸入帳號密碼")
                        .setTitle("登入失敗")
                        .setPositiveButton("確定", null)
                        .show()
                }
            }
    }
}