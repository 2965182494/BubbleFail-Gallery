package com.example.myapplication.model

/**
 * 奶茶统计数据
 */
data class BubbleTeaStats(
    val name: String,         // 奶茶名称
    val count: Int,           // 消费次数
    val totalExpense: Double, // 总消费金额
    val percentage: Float,    // 占总消费的百分比
    val imagePath: String     // 图片路径
) 