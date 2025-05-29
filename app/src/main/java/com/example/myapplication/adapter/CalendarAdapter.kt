package com.example.myapplication.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.CalendarDay
import java.io.File

class CalendarAdapter(private var days: MutableList<CalendarDay>) : 
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {
    
    fun updateData(newDays: List<CalendarDay>) {
        Log.d("CalendarAdapter", "Updating data with ${newDays.size} days")
        days.clear()
        days.addAll(newDays)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, position)
    }
    
    override fun getItemCount(): Int = days.size
    
    inner class CalendarViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val dayText: TextView = itemView.findViewById(R.id.tv_day)
        private val drinkIcon: ImageView = itemView.findViewById(R.id.iv_drink_icon)
        private val dayImage: ImageView = itemView.findViewById(R.id.iv_day_image)
        
        fun bind(day: CalendarDay, position: Int) {
            // If this is a weekday header row
            if (position < 7) {
                dayText.text = day.day
                dayText.setTypeface(null, Typeface.BOLD)
                dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_dark))
                dayText.visibility = View.VISIBLE
                dayImage.visibility = View.GONE
                drinkIcon.visibility = View.GONE
                return
            }
            
            // Normal date item - set as not clickable
            itemView.isClickable = false
            
            // Check if there's a record
            if (day.hasRecord && day.iconPath.isNotEmpty()) {
                // Has record and image path, show image instead of date text
                dayText.visibility = View.GONE
                dayImage.visibility = View.VISIBLE
                drinkIcon.visibility = View.GONE
                
                // Load user uploaded image
                try {
                    val file = File(day.iconPath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        dayImage.setImageBitmap(bitmap)
                    } else {
                        // If image doesn't exist, show default icon
                        dayImage.setImageResource(R.drawable.ic_bubble_tea_placeholder)
                    }
                } catch (e: Exception) {
                    Log.e("CalendarAdapter", "Error loading image: ${e.message}")
                    // Show default icon on error
                    dayImage.setImageResource(R.drawable.ic_bubble_tea_placeholder)
                }
            } else {
                // No record, show date text
                dayText.visibility = View.VISIBLE
                dayImage.visibility = View.GONE
                drinkIcon.visibility = View.GONE
                
                dayText.text = day.day
                
                // Set date text color
                if (day.isCurrentMonth) {
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_dark))
                    dayText.setTypeface(null, Typeface.NORMAL)
                } else {
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_light))
                    dayText.setTypeface(null, Typeface.NORMAL)
                }
            }
        }
    }
} 