package com.example.myapplication.utils

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class InputValidation(private val context: Context) {

    // 验证EditText是否为空
    fun isInputEditTextFilled(editText: EditText): Boolean {
        val value = editText.text.toString().trim()
        if (value.isEmpty()) {
            return false
        }
        return true
    }

    // 验证EditText是否为有效的电子邮件
    fun isInputEditTextEmail(editText: EditText): Boolean {
        val value = editText.text.toString().trim()
        if (value.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            return false
        }
        return true
    }

    // 验证两个EditText的内容是否匹配
    fun isInputEditTextMatches(editText1: EditText, editText2: EditText): Boolean {
        val value1 = editText1.text.toString().trim()
        val value2 = editText2.text.toString().trim()
        return value1 == value2
    }
} 