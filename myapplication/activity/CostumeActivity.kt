package com.example.myapplication.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import kotlin.random.Random

class CostumeActivity : AppCompatActivity() {
    
    private lateinit var costumeImageView: ImageView
    private lateinit var randomCostumeButton: Button
    private lateinit var backButton: ImageButton
//    private lateinit var costumeIndicator: TextView
    private lateinit var prevButton: ImageView
    private lateinit var nextButton: ImageView
    private lateinit var selectButton: Button
    
    // 当前选择的装扮ID（0-7，共8种）
    private var currentCostumeId = 0
    
    // 最大装扮数量
    private val maxCostumeCount = 8
    
    // 用户ID
    private var userId = 1
    
    // 装扮资源ID数组
    private val costumeResources = arrayOf(
        R.drawable.cat_costume_default,
        R.drawable.cat_costume_1,
        R.drawable.cat_costume_2,
        R.drawable.cat_costume_3,
        R.drawable.cat_costume_4,
        R.drawable.cat_costume_5,
        R.drawable.cat_costume_6,
        R.drawable.cat_costume_7
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_costume)
        
        // 获取用户ID
        userId = intent.getIntExtra("USER_ID", 1)
        
        // 初始化视图
        initViews()
        
        // 加载当前装扮ID
        loadCurrentCostume()
        
        // 更新UI
        updateCostumeUI()
        
        // 设置点击事件
        setupListeners()
    }
    
    private fun initViews() {
        costumeImageView = findViewById(R.id.iv_costume)
        randomCostumeButton = findViewById(R.id.btn_random_costume)
        backButton = findViewById(R.id.btn_back)
//        costumeIndicator = findViewById(R.id.tv_costume_indicator)
        prevButton = findViewById(R.id.btn_prev_costume)
        nextButton = findViewById(R.id.btn_next_costume)
        selectButton = findViewById(R.id.btn_select_costume)
    }
    
    private fun loadCurrentCostume() {
        // 从MainActivity获取当前装扮ID，使用用户特定的SharedPreferences
        val sharedPreferences = getSharedPreferences("${MainActivity.PREF_NAME}_$userId", MODE_PRIVATE)
        currentCostumeId = sharedPreferences.getInt(MainActivity.KEY_CURRENT_COSTUME, 0)
    }
    
    private fun updateCostumeUI() {
        // 更新装扮指示器
//        costumeIndicator.text = getString(R.string.costume_indicator, currentCostumeId + 1, maxCostumeCount)
        
        // 根据当前装扮ID设置图片
        costumeImageView.setImageResource(costumeResources[currentCostumeId])
    }
    
    private fun setupListeners() {
        // 返回按钮点击事件
        backButton.setOnClickListener {
            finish()
        }
        
        // 随机换装按钮点击事件
        randomCostumeButton.setOnClickListener {
            // 随机选择一个不同的装扮ID
            val oldCostumeId = currentCostumeId
            do {
                currentCostumeId = Random.nextInt(maxCostumeCount)
            } while (currentCostumeId == oldCostumeId && maxCostumeCount > 1)
            
            // 更新UI
            updateCostumeUI()
        }
        
        // 上一个装扮按钮点击事件
        prevButton.setOnClickListener {
            currentCostumeId = (currentCostumeId - 1 + maxCostumeCount) % maxCostumeCount
            updateCostumeUI()
        }
        
        // 下一个装扮按钮点击事件
        nextButton.setOnClickListener {
            currentCostumeId = (currentCostumeId + 1) % maxCostumeCount
            updateCostumeUI()
        }
        
        // 选择当前装扮按钮点击事件
        selectButton.setOnClickListener {
            // 保存当前装扮ID
            saveCurrentCostume()
            // 返回主界面
            finish()
        }
    }
    
    private fun saveCurrentCostume() {
        // 保存当前装扮ID到用户特定的SharedPreferences
        val sharedPreferences = getSharedPreferences("${MainActivity.PREF_NAME}_$userId", MODE_PRIVATE)
        sharedPreferences.edit().putInt(MainActivity.KEY_CURRENT_COSTUME, currentCostumeId).apply()
        
        // 设置结果并返回
        val resultIntent = Intent()
        resultIntent.putExtra(MainActivity.KEY_CURRENT_COSTUME, currentCostumeId)
        setResult(RESULT_OK, resultIntent)
    }
} 