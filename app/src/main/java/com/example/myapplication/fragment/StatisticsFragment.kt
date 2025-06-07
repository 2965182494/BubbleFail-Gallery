package com.example.myapplication.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.ExpenseAdapter
import com.example.myapplication.adapter.RankingAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.model.BubbleTeaStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 统计Fragment - 只统计本月数据
 */
class StatisticsFragment : Fragment() {

    // 视图组件
    private lateinit var totalCupsTextView: TextView
    private lateinit var avgIntervalTextView: TextView
    private lateinit var totalExpenseTextView: TextView
    private lateinit var avgDailyAmountTextView: TextView
    private lateinit var expenseTotalTextView: TextView
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var rankingRecyclerView: RecyclerView
    private lateinit var monthTitleTextView: TextView
    
    // 适配器
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var rankingAdapter: RankingAdapter
    
    // 数据库帮助类
    private lateinit var dbHelper: DatabaseHelper
    
    // 当前用户ID
    private var currentUserId = 1 // 默认用户ID，实际应用中应该从登录状态获取
    
    // 统计数据
    private var totalCups = 0
    private var avgInterval = 0.0
    private var totalExpense = 0.0
    private var avgDailyAmount = 0.0
    private var expenseList = mutableListOf<BubbleTeaStats>()
    private var rankingList = mutableListOf<BubbleTeaStats>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 从arguments中获取用户ID
        arguments?.let {
            currentUserId = it.getInt("USER_ID", 1)
        }
        
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化数据库帮助类
        dbHelper = DatabaseHelper(requireContext())
        
        // 初始化视图
        initViews(view)
        
        // 初始化适配器
        initAdapters()
        
        // 加载统计数据
        loadStatistics()
        
        // 更新UI
        updateUI()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次恢复时重新加载统计数据
        loadStatistics()
        updateUI()
    }
    
    /**
     * 初始化视图组件
     */
    private fun initViews(view: View) {
        totalCupsTextView = view.findViewById(R.id.tv_total_cups)
        avgIntervalTextView = view.findViewById(R.id.tv_avg_interval)
        totalExpenseTextView = view.findViewById(R.id.tv_total_expense)
        avgDailyAmountTextView = view.findViewById(R.id.tv_avg_daily_amount)
        expenseTotalTextView = view.findViewById(R.id.tv_expense_total)
        expenseRecyclerView = view.findViewById(R.id.recycler_expense)
        rankingRecyclerView = view.findViewById(R.id.recycler_ranking)
    }
    
    /**
     * 初始化适配器
     */
    private fun initAdapters() {
        // 初始化支出适配器
        expenseAdapter = ExpenseAdapter(expenseList)
        expenseRecyclerView.layoutManager = LinearLayoutManager(context)
        expenseRecyclerView.adapter = expenseAdapter
        
        // 初始化排名适配器
        rankingAdapter = RankingAdapter(rankingList)
        rankingRecyclerView.layoutManager = LinearLayoutManager(context)
        rankingRecyclerView.adapter = rankingAdapter
    }
    
    /**
     * 获取本月的第一天和最后一天
     */
    private fun getCurrentMonthRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        
        // 设置为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val firstDay = calendar.time
        
        // 设置为本月最后一天
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val lastDay = calendar.time
        
        return Pair(firstDay, lastDay)
    }
    
    /**
     * 加载统计数据
     */
    private fun loadStatistics() {
        // 获取所有记录
        val allRecords = dbHelper.getAllBubbleTeaRecords(currentUserId)
        
        if (allRecords.isEmpty()) {
            // 如果没有记录，设置默认值
            totalCups = 0
            avgInterval = 0.0
            totalExpense = 0.0
            avgDailyAmount = 0.0
            expenseList.clear()
            rankingList.clear()
            return
        }
        
        // 获取本月日期范围
        val (firstDayOfMonth, lastDayOfMonth) = getCurrentMonthRange()
        
        // 设置月份标题
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthTitle = monthYearFormat.format(firstDayOfMonth)
        try {
            monthTitleTextView.text = monthTitle + "statistics"
        } catch (e: Exception) {
            // 如果找不到月份标题视图，忽略错误
        }
        
        // 筛选本月记录
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthlyRecords = allRecords.filter { record ->
            val recordDate = dateFormat.parse(record.date) ?: Date()
            recordDate.time in firstDayOfMonth.time..lastDayOfMonth.time
        }
        
        if (monthlyRecords.isEmpty()) {
            // 如果本月没有记录，设置默认值
            totalCups = 0
            avgInterval = 0.0
            totalExpense = 0.0
            avgDailyAmount = 0.0
            expenseList.clear()
            rankingList.clear()
            return
        }
        
        // 计算总杯数
        totalCups = monthlyRecords.size
        
        // 计算平均间隔天数
        val dates = monthlyRecords.map { dateFormat.parse(it.date) ?: Date() }.sortedBy { it.time }
        if (dates.size > 1) {
            var totalDays = 0L
            for (i in 1 until dates.size) {
                val diffInMillis = dates[i].time - dates[i-1].time
                totalDays += TimeUnit.MILLISECONDS.toDays(diffInMillis)
            }
            avgInterval = totalDays.toDouble() / (dates.size - 1)
        } else {
            avgInterval = 0.0
        }
        
        // 计算总消费
        totalExpense = monthlyRecords.sumOf { it.expense }
        
        // 计算平均每日消费 - 使用当月总天数
        // 创建一个日历实例，设置为当前统计月份的年月
        val monthCalendar = Calendar.getInstance()
        monthCalendar.time = firstDayOfMonth
        
        // 获取当月的总天数
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // 使用当月总天数计算平均每日消费
        avgDailyAmount = totalExpense / daysInMonth
        
        // 按奶茶名称分组，计算每种奶茶的消费情况
        val teaGroups = monthlyRecords.groupBy { it.name }
        
        // 计算每种奶茶的消费情况
        expenseList = teaGroups.map { (name, records) ->
            val count = records.size
            val expense = records.sumOf { it.expense }
            val percentage = if (totalExpense > 0) (expense / totalExpense).toFloat() else 0f
            
            // 寻找该种奶茶中有图片的记录
            val validImageRecord = records.find { it.imagePath.isNotEmpty() }
            // 使用有效的图片路径，如果所有记录都没有图片，则使用空字符串
            val imagePath = validImageRecord?.imagePath ?: ""
            
            BubbleTeaStats(name, count, expense, percentage, imagePath)
        }.sortedByDescending { it.totalExpense }.toMutableList()
        
        // 计算消费排名
        rankingList = teaGroups.map { (name, records) ->
            val count = records.size
            val expense = records.sumOf { it.expense }
            val percentage = if (totalExpense > 0) (expense / totalExpense).toFloat() else 0f
            
            // 寻找该种奶茶中有图片的记录
            val validImageRecord = records.find { it.imagePath.isNotEmpty() }
            // 使用有效的图片路径，如果所有记录都没有图片，则使用空字符串
            val imagePath = validImageRecord?.imagePath ?: ""
            
            BubbleTeaStats(name, count, expense, percentage, imagePath)
        }.sortedByDescending { it.count }.toMutableList()
    }
    
    /**
     * 更新UI
     */
    private fun updateUI() {
        // 更新概览数据
        totalCupsTextView.text = String.format(Locale.getDefault(), "%.1f", totalCups.toFloat())
        avgIntervalTextView.text = String.format(Locale.getDefault(), "%.1f", avgInterval)
        totalExpenseTextView.text = String.format(Locale.getDefault(), "%.2f", totalExpense)
        avgDailyAmountTextView.text = String.format(Locale.getDefault(), "%.2f", avgDailyAmount)
        
        // 更新总消费
        expenseTotalTextView.text = String.format(Locale.getDefault(), "¥ %.2f", totalExpense)
        
        // 更新支出列表
        expenseAdapter.updateData(expenseList)
        
        // 更新排名列表
        rankingAdapter.updateData(rankingList)
    }
} 