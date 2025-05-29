package com.example.myapplication.model

data class CalendarDay(
    val day: String,           // 显示的日期文本
    val date: String,          // 格式化的日期字符串 yyyy-MM-dd
    val isCurrentMonth: Boolean, // 是否为当前月份
    val hasRecord: Boolean,    // 是否有记录
    val iconPath: String = ""  // 图标路径
) 