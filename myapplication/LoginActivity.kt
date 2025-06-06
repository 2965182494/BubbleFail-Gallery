package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
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
import com.example.myapplication.utils.InputValidation

class LoginActivity : AppCompatActivity() {
    
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpText: TextView
    private lateinit var togglePasswordVisibility: ImageView
    
    private lateinit var inputValidation: InputValidation
    private lateinit var databaseHelper: DatabaseHelper
    private var isPasswordVisible = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        // 设置状态栏和导航栏
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_container)) { v, insets ->
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
        signInButton = findViewById(R.id.btn_sign_in)
        signUpText = findViewById(R.id.tv_sign_up)
        togglePasswordVisibility = findViewById(R.id.iv_toggle_password)
    }
    
    // 初始化监听器
    private fun initListeners() {
        // 设置密码可见性切换
        togglePasswordVisibility.setOnClickListener {
            togglePasswordVisibility()
        }
        
        // 登录按钮点击事件
        signInButton.setOnClickListener {
            verifyFromSQLite()
        }
        
        // 注册文本点击事件
        signUpText.setOnClickListener {
            // 跳转到注册页面
            startActivity(Intent(this, RegisterActivity::class.java))
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
    
    // 验证输入并从SQLite验证
    private fun verifyFromSQLite() {
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
        
        // 检查用户是否存在
        if (databaseHelper.checkUser(emailEditText.text.toString().trim(), 
                passwordEditText.text.toString().trim())) {
            // 登录成功，跳转到主页面
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("EMAIL", emailEditText.text.toString().trim())
            emptyInputEditText()
            startActivity(intent)
            finish()
        } else {
            // 登录失败
            Toast.makeText(this, getString(R.string.error_login_failed), Toast.LENGTH_SHORT).show()
        }
    }
    
    // 清空输入框
    private fun emptyInputEditText() {
        emailEditText.text.clear()
        passwordEditText.text.clear()
    }
} 