package com.example.myapplication.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.BubbleTeaStats
import java.io.File
import java.util.Locale

/**
 * 支出列表适配器
 */
class ExpenseAdapter(private var items: List<BubbleTeaStats>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    /**
     * 更新数据
     */
    fun updateData(newItems: List<BubbleTeaStats>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /**
     * 支出列表项ViewHolder
     */
    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teaIcon: ImageView = itemView.findViewById(R.id.iv_tea_icon)
        private val teaName: TextView = itemView.findViewById(R.id.tv_tea_name)
        private val percentage: TextView = itemView.findViewById(R.id.tv_percentage)
        private val expenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

        fun bind(stats: BubbleTeaStats) {
            // 设置奶茶名称
            teaName.text = stats.name
            
            // 设置百分比
            val percentValue = (stats.percentage * 100).toInt()
            percentage.text = "$percentValue%"
            
            // 设置消费金额
            expenseAmount.text = String.format(Locale.getDefault(), "¥%.2f", stats.totalExpense)
            
            // 设置进度条
            progressBar.progress = percentValue
            
            // 设置图片
            if (stats.imagePath.isNotEmpty()) {
                try {
                    val file = File(stats.imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        teaIcon.setImageBitmap(bitmap)
                    } else {
                        // 图片文件不存在，使用默认图标
                        teaIcon.setImageResource(R.drawable.ic_bubble_tea_placeholder)
                    }
                } catch (e: Exception) {
                    // 加载图片出错，使用默认图标
                    teaIcon.setImageResource(R.drawable.ic_bubble_tea_placeholder)
                }
            } else {
                // 没有图片路径，使用默认图标
                teaIcon.setImageResource(R.drawable.ic_bubble_tea_placeholder)
            }
        }
    }
} 