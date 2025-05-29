package com.example.myapplication.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.model.BubbleTeaRecord
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddRecordActivity : AppCompatActivity() {

    // 视图组件
    private lateinit var bubbleTeaImageView: ImageView
    private lateinit var dateEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var expenseEditText: EditText
    private lateinit var sweetnessEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var imageCardView: CardView
    
    // 数据库帮助类
    private lateinit var dbHelper: DatabaseHelper
    
    // 日期选择器
    private val calendar = Calendar.getInstance()
    
    // 当前用户ID
    private var currentUserId = 1 // 默认用户ID，实际应用中应该从登录状态获取
    
    // 图片路径
    private var selectedImagePath = ""
    
    // 权限请求代码
    companion object {
        private const val TAG = "AddRecordActivity"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 101
        
        // 图片压缩参数
        private const val MAX_IMAGE_WIDTH = 800
        private const val MAX_IMAGE_HEIGHT = 800
        private const val IMAGE_QUALITY = 80
    }
    
    // 图库选择结果处理
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                // 将图片保存到应用私有目录并获取路径
                val savedImagePath = saveImageToAppDirectory(imageUri)
                if (savedImagePath.isNotEmpty()) {
                    // 压缩图片
                    val compressedImagePath = compressAndSaveImage(savedImagePath)
                    if (compressedImagePath.isNotEmpty()) {
                        // 显示图片
                        val bitmap = BitmapFactory.decodeFile(compressedImagePath)
                        bubbleTeaImageView.setImageBitmap(bitmap)
                        bubbleTeaImageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        
                        // 保存图片路径
                        selectedImagePath = compressedImagePath
                        
                        // 删除原始图片
                        if (compressedImagePath != savedImagePath) {
                            File(savedImagePath).delete()
                        }
                    }
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)
        
        // 设置返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_record)
        
        // 初始化数据库帮助类
        dbHelper = DatabaseHelper(this)
        
        // 初始化视图
        initViews()
        
        // 设置监听器
        setupListeners()
        
        // 设置默认日期为今天
        setDefaultDate()
    }
    
    // 初始化视图组件
    private fun initViews() {
        bubbleTeaImageView = findViewById(R.id.iv_bubble_tea_image)
        dateEditText = findViewById(R.id.et_date)
        nameEditText = findViewById(R.id.et_name)
        expenseEditText = findViewById(R.id.et_expense)
        sweetnessEditText = findViewById(R.id.et_sweetness)
        saveButton = findViewById(R.id.btn_save)
        imageCardView = findViewById(R.id.card_image)
    }
    
    // 设置监听器
    private fun setupListeners() {
        // 日期选择
        dateEditText.setOnClickListener {
            showDatePicker()
        }
        
        // 图片选择
        imageCardView.setOnClickListener {
            checkStoragePermissionAndOpenGallery()
        }
        
        // 保存按钮
        saveButton.setOnClickListener {
            saveRecord()
        }
    }
    
    // 检查存储权限并打开相册
    private fun checkStoragePermissionAndOpenGallery() {
        if (hasStoragePermission()) {
            openGallery()
        } else {
            requestStoragePermission()
        }
    }
    
    // 检查是否有存储权限
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Android 10及以上，应用可以访问自己的私有目录，不需要额外权限
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // 请求存储权限
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10及以上不需要请求WRITE_EXTERNAL_STORAGE权限
            openGallery()
            return
        }
        
        val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, storagePermission)) {
            // 向用户解释为什么需要这个权限
            AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_storage_rationale)
                .setPositiveButton("确定") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(storagePermission),
                        STORAGE_PERMISSION_REQUEST_CODE
                    )
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            // 直接请求权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(storagePermission),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 存储权限已授予，打开相册
                    openGallery()
                } else {
                    Toast.makeText(this, getString(R.string.permission_storage_rationale), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // 打开相册选择图片
    private fun openGallery() {
        Log.d(TAG, "打开图库选择图片")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    
    // 保存图片到应用私有目录
    private fun saveImageToAppDirectory(uri: Uri): String {
        val contentResolver: ContentResolver = contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        
        // 创建目标文件
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "BUBBLE_TEA_${UUID.randomUUID()}.jpg"
        val file = File(storageDir, fileName)
        
        return try {
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(4 * 1024) // 4KB buffer
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.image_save_failed), Toast.LENGTH_SHORT).show()
            ""
        }
    }
    
    // 压缩并保存图片
    private fun compressAndSaveImage(imagePath: String): String {
        try {
            // 加载原始图片
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            // 计算缩放比例
            var width = options.outWidth
            var height = options.outHeight
            var scale = 1
            
            while (width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT) {
                width /= 2
                height /= 2
                scale *= 2
            }
            
            // 加载压缩后的图片
            val compressOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            val bitmap = BitmapFactory.decodeFile(imagePath, compressOptions)
            
            // 创建压缩后的文件
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val fileName = "COMPRESSED_${UUID.randomUUID()}.jpg"
            val compressedFile = File(storageDir, fileName)
            
            // 保存压缩后的图片
            val outputStream = FileOutputStream(compressedFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
            
            return compressedFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return imagePath // 如果压缩失败，返回原始路径
        }
    }
    
    // 设置默认日期为今天
    private fun setDefaultDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(calendar.time)
        dateEditText.setText(today)
    }
    
    // 显示日期选择器
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                updateDateInView()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
    
    // 更新日期显示
    private fun updateDateInView() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateEditText.setText(dateFormat.format(calendar.time))
    }
    
    // 保存记录
    private fun saveRecord() {
        val date = dateEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val expenseStr = expenseEditText.text.toString().trim()
        val sweetness = sweetnessEditText.text.toString().trim()
        
        // 验证输入
        if (date.isEmpty() || name.isEmpty() || expenseStr.isEmpty() || sweetness.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 解析价格
        val expense = try {
            expenseStr.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "请输入有效的价格", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建记录对象
        val record = BubbleTeaRecord(
            date = date,
            name = name,
            expense = expense,
            sweetness = sweetness,
            imagePath = selectedImagePath
        )
        
        // 保存到数据库
        val recordId = dbHelper.addBubbleTeaRecord(record, currentUserId)
        
        if (recordId > 0) {
            Toast.makeText(this, getString(R.string.record_saved), Toast.LENGTH_SHORT).show()
            // 返回主界面
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.record_save_failed), Toast.LENGTH_SHORT).show()
        }
    }
    
    // 处理返回按钮点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 