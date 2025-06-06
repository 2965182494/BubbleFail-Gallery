package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.utils.InputValidation

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView
    private lateinit var togglePasswordVisibility: ImageView
    private lateinit var toggleConfirmPasswordVisibility: ImageView
    
    private lateinit var inputValidation: InputValidation
    private lateinit var databaseHelper: DatabaseHelper
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        // 设置状态栏和导航栏
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化视图
        initViews()
        // 初始化监听器
        initListeners()
        // 初始化对象
        initObjects()
    }
    
    // 初始化视图
    private fun initViews() {
        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.et_password)
        confirmPasswordEditText = findViewById(R.id.et_confirm_password)
        signUpButton = findViewById(R.id.btn_sign_up)
        signInText = findViewById(R.id.tv_sign_in)
        togglePasswordVisibility = findViewById(R.id.iv_toggle_password)
        toggleConfirmPasswordVisibility = findViewById(R.id.iv_toggle_confirm_password)
    }
    
    // 初始化监听器
    private fun initListeners() {
        // 设置密码可见性切换
        togglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }
        
        toggleConfirmPasswordVisibility.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }
        
        // 注册按钮点击事件
        signUpButton.setOnClickListener {
            registerUser()
        }
        
        // 登录文本点击事件
        signInText.setOnClickListener {
            // 跳转到登录页面
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    // 初始化对象
    private fun initObjects() {
        databaseHelper = DatabaseHelper(this)
        inputValidation = InputValidation(this)
    }
    
    // 切换密码可见性
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            // 显示密码
            passwordEditText.transformationMethod = null
            togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            // 隐藏密码
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            togglePasswordVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }
        // 将光标移动到文本末尾
        passwordEditText.setSelection(passwordEditText.text.length)
    }
    
    // 切换确认密码可见性
    private fun toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        if (isConfirmPasswordVisible) {
            // 显示密码
            confirmPasswordEditText.transformationMethod = null
            toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            // 隐藏密码
            confirmPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggleConfirmPasswordVisibility.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }
        // 将光标移动到文本末尾
        confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
    }
    
    // 注册用户
    private fun registerUser() {
        // 验证输入是否为空
        if (!inputValidation.isInputEditTextFilled(emailEditText)) {
            Toast.makeText(this, getString(R.string.error_email_empty), Toast.LENGTH_SHORT).show()
            return
        }
        
        // 验证输入是否为电子邮件
        if (!inputValidation.isInputEditTextEmail(emailEditText)) {
            Toast.makeText(this, getString(R.string.error_email_invalid), Toast.LENGTH_SHORT).show()
            return
        }
        
        // 验证密码是否为空
        if (!inputValidation.isInputEditTextFilled(passwordEditText)) {
            Toast.makeText(this, getString(R.string.error_password_empty), Toast.LENGTH_SHORT).show()
            return
        }
        
        // 验证确认密码是否为空
        if (!inputValidation.isInputEditTextFilled(confirmPasswordEditText)) {
            Toast.makeText(this, getString(R.string.error_confirm_password_empty), Toast.LENGTH_SHORT).show()
            return
        }
        
        // 验证两个密码是否匹配
        if (!inputValidation.isInputEditTextMatches(passwordEditText, confirmPasswordEditText)) {
            Toast.makeText(this, getString(R.string.error_password_match), Toast.LENGTH_SHORT).show()
            return
        }
        
        // 检查用户是否已经存在
        if (!databaseHelper.checkUserExists(emailEditText.text.toString().trim())) {
            // 创建用户对象
            val user = User(
                email = emailEditText.text.toString().trim(),
                password = passwordEditText.text.toString().trim()
            )
            
            // 将用户添加到数据库
            val id = databaseHelper.addUser(user)
            
            // 检查插入是否成功
            if (id > 0) {
                Toast.makeText(this, getString(R.string.success_register), Toast.LENGTH_SHORT).show()
                emptyInputEditText()
                
                // 跳转到登录页面
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_register), Toast.LENGTH_SHORT).show()
            }
        } else {
            // 用户已存在
            Toast.makeText(this, getString(R.string.error_email_exists), Toast.LENGTH_SHORT).show()
        }
    }
    
    // 清空输入框
    private fun emptyInputEditText() {
        emailEditText.text.clear()
        passwordEditText.text.clear()
        confirmPasswordEditText.text.clear()
    }
} 