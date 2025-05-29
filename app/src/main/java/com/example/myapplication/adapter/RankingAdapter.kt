package com.example.myapplication.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.BubbleTeaStats
import java.io.File

/**
 * 排名列表适配器
 */
class RankingAdapter(private var items: List<BubbleTeaStats>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    /**
     * 更新数据
     */
    fun updateData(newItems: List<BubbleTeaStats>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(items[position], position + 1)
    }

    override fun getItemCount(): Int = items.size

    /**
     * 排名列表项ViewHolder
     */
    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teaIcon: ImageView = itemView.findViewById(R.id.iv_tea_icon)
        private val teaName: TextView = itemView.findViewById(R.id.tv_tea_name)
        private val rankNumber: TextView = itemView.findViewById(R.id.tv_rank_number)

        fun bind(stats: BubbleTeaStats, rank: Int) {
            // 设置奶茶名称
            teaName.text = stats.name
            
            // 设置右侧数字为消费次数
            rankNumber.text = stats.count.toString()
            
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