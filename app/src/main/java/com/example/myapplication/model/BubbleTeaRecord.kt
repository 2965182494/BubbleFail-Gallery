package com.example.myapplication.model

/**
 * 奶茶记录数据模型
 */
data class BubbleTeaRecord(
    val id: Int = 0,           // 记录ID，主键
    val date: String,          // 日期，格式：yyyy-MM-dd
    val name: String,          // 奶茶名称
    val expense: Double,       // 价格
    val sweetness: String,     // 甜度
    val imagePath: String = "" // 图片路径，可选
) 