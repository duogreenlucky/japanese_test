package com.duolucky.japanesetest

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.duolucky.japanesetest.databinding.ActivityCloudBaseBinding
import com.duolucky.japanesetest.databinding.CloudbaseQuestionRowBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudBaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCloudBaseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCloudBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("下載中...")
        builder.setMessage("正在向雲端資料庫取得目錄，請稍後...")
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("AppLog", "開始連線Realtime Database")
            val questionMenuList = mutableListOf<QuestionMenu>()
            val amountSnapshot = cloudDatabase.child("question_base").child("amount").get().await()
            val amount = amountSnapshot.value as? Long ?: 0L
            Log.d("AppLog", "amount = $amount")
                for (num in 1..amount.toInt()) {
                    val snapshot = cloudDatabase.child("question_base").child("$num").get().await()
                    Log.d("AppLog", "$snapshot")
                    val question = snapshot.getValue(QuestionMenu::class.java)
                    Log.d("AppLog", "$question")
                    question?.let {
                        Log.d("AppLog", "$it")
                        questionMenuList.add(it)
                        Log.d("AppLog", "加入題目：${it.name}, 作者：${it.writer}, ID：${it.amountID}")
                    }
                }

            withContext(Dispatchers.Main) {
                Log.d("AppLog", "Question List = $questionMenuList")
                Log.d("AppLog", "開始建立RecyclerView")
                val recycler = binding.recycler
                recycler.layoutManager = LinearLayoutManager(this@CloudBaseActivity)
                val adapter = CloudbaseQuestionAdapter(questionMenuList) { questionMenu ->
                    Log.d("AppLog", "資料：${questionMenu}")
                    val id= questionMenu.amountID
                    Log.d("AppLog", "ID：$id")
                    downloadQuestions(id.toString())
                }
                recycler.adapter = adapter
                alertDialog.dismiss()
            }
        }
    }

    fun downloadQuestions(id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("下載中...")
        builder.setCancelable(false)
        val database = Room.databaseBuilder(
            this,
            CacheQuestionsDatabase::class.java,
            "CacheQuestion"
        ).fallbackToDestructiveMigration(false).build()
        CoroutineScope(Dispatchers.IO).launch {
            val amountSnapshot = cloudDatabase.child("question_base").child(id).child("amount").get().await()
            val amount = amountSnapshot.value as? Long ?: 0L

            builder.setMessage("正在下載題庫，請稍後... 請勿關閉螢幕或程式\n下載進度(0/${amount /2})")

            val alertDialog = withContext(Dispatchers.Main) {
                val bud = builder.create()
                bud.show()
                bud
            }

//            val messageView = withContext(Dispatchers.Main) {
//                val bud = builder.create()
//                bud.show()
//                bud.findViewById<TextView>(android.R.id.message)
//            }

            val messageView = alertDialog.findViewById<TextView>(android.R.id.message)

            val questionDataBase = cloudDatabase.child("question_base").child(id).child("questions")
            for (num in 1..amount.toInt() step 2) {
                val question = questionDataBase.child(num.toString()).get().await()
                val answer = questionDataBase.child("$num"+1.toString()).get().await()
                val questions = arrayListOf<CacheQuestions>(CacheQuestions("$question", "$answer"))
                database.CacheQuestionsDao().add(questions)

                withContext(Dispatchers.Main) {
                    messageView?.text = "正在下載題庫，請稍後... 請勿關閉螢幕或程式\n下載進度(${(num + 1) / 2}/${amount / 2})"
                }
                Log.d("CloudDatabaseLog", "已新增題目到資料庫：$question，$answer")
            }

            val questionBaseName = cloudDatabase.child("question_base").child(id).child("name")
                .get().await().value.toString()
            getSharedPreferences("jptest", Context.MODE_PRIVATE)
                .edit{
                    putString("cacheQuestionBaseName", questionBaseName)
                }
            withContext(Dispatchers.Main) {
                alertDialog.dismiss()

                builder.setTitle("完成！")
                builder.setMessage("下載已完成！")
                builder.setPositiveButton("確定") { dialog, which ->
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                builder.setCancelable(true)
                builder.create().show()
            }
        }
    }
}

class CloudbaseQuestionAdapter(
    private val questionMenuList: List<QuestionMenu>,
    private val onItemClick: (QuestionMenu) -> Unit
) : RecyclerView.Adapter<CloudbaseQuestionAdapter.CloudbaseQuestionViewHolder>() {

    inner class CloudbaseQuestionViewHolder(val binding: CloudbaseQuestionRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameText: TextView = binding.questionName
        val writerText: TextView = binding.questionWriter
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloudbaseQuestionViewHolder {
        val binding = CloudbaseQuestionRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CloudbaseQuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CloudbaseQuestionViewHolder, position: Int) {
        holder.nameText.text = questionMenuList[position].name
        holder.writerText.text = questionMenuList[position].writer

        holder.itemView.setOnClickListener {
            onItemClick(questionMenuList[position])
        }
    }

    override fun getItemCount(): Int = questionMenuList.size
}

class CloudbaseQuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val binding = CloudbaseQuestionRowBinding.bind(view)
    val name = binding.questionName
    val writer = binding.questionWriter
}