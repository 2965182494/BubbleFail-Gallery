package com.example.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.activity.AddRecordActivity
import com.example.myapplication.activity.EditRecordActivity
import com.example.myapplication.adapter.CalendarAdapter
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.model.BubbleTeaRecord
import com.example.myapplication.model.CalendarDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    // View components
    private lateinit var monthYearText: TextView
    private lateinit var previousButton: ImageView
    private lateinit var nextButton: ImageView
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var daysRecyclerView: RecyclerView
    private lateinit var soberDaysText: TextView
    private lateinit var soberDaysCount: TextView
    private lateinit var rewardNotification: TextView
    private lateinit var noRecordsText: TextView
    
    // Calendar data
    private val calendar = Calendar.getInstance()
    private var currentMonth = calendar.get(Calendar.MONTH)
    private var currentYear = calendar.get(Calendar.YEAR)
    private val calendarAdapter = CalendarAdapter(mutableListOf())
    
    // Month and year data
    private val months = arrayOf("January", "February", "March", "April", "May", "June", 
                                "July", "August", "September", "October", "November", "December")
    private val years = (2020..2030).map { "$it" }.toTypedArray()
    
    // Record data
    private val drinkRecords = mutableMapOf<String, String>() // date -> icon path
    private var soberDays = 3 // Days without drinking bubble tea this month
    
    // Database helper
    private lateinit var dbHelper: DatabaseHelper
    
    // Current user ID
    private var currentUserId = 1 // Default user ID, should be obtained from login state in real app
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        
        Log.d("CalendarFragment", "onCreateView called")
        
        // 从arguments中获取用户ID
        arguments?.let {
            currentUserId = it.getInt("USER_ID", 1)
            Log.d("CalendarFragment", "Get the user ID: $currentUserId")
        }
        
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("CalendarFragment", "onViewCreated called")
        
        try {
            // Initialize database helper
            dbHelper = DatabaseHelper(requireContext())
            
            // Initialize views
            initViews(view)
            // Setup listeners
            setupListeners()
            // Load bubble tea records
            loadBubbleTeaRecords()
            // Initialize calendar
            setupCalendar()
            // Update UI
            updateUI()
        } catch (e: Exception) {
            Log.e("CalendarFragment", "Error in onViewCreated", e)
            Toast.makeText(context, "Calendar loading error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reload records every time the fragment resumes
        loadBubbleTeaRecords()
        updateCalendar()
    }
    
    // Initialize view components
    private fun initViews(view: View) {
        monthYearText = view.findViewById(R.id.tv_month_year)
        previousButton = view.findViewById(R.id.btn_previous)
        nextButton = view.findViewById(R.id.btn_next)
        monthSpinner = view.findViewById(R.id.spinner_month)
        yearSpinner = view.findViewById(R.id.spinner_year)
        daysRecyclerView = view.findViewById(R.id.recycler_days)
        soberDaysText = view.findViewById(R.id.tv_sober_days_text)
        soberDaysCount = view.findViewById(R.id.tv_sober_days_count)
        rewardNotification = view.findViewById(R.id.tv_reward_notification)
        noRecordsText = view.findViewById(R.id.tv_no_records)
        
        // Setup month dropdown
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter
        monthSpinner.setSelection(currentMonth)
        
        // Setup year dropdown
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
        yearSpinner.setSelection(years.indexOf("$currentYear"))
        
        // Setup calendar grid
        daysRecyclerView.layoutManager = GridLayoutManager(context, 7) // 7 columns for Mon-Sun
        daysRecyclerView.adapter = calendarAdapter
        
        // 设置日历点击事件
        calendarAdapter.setOnDayClickListener(object : CalendarAdapter.OnDayClickListener {
            override fun onDayClick(day: CalendarDay) {
                if (day.hasRecord) {
                    // 获取该日期的记录
                    val records = dbHelper.getBubbleTeaRecordsByDate(day.date, currentUserId)
                    if (records.isNotEmpty()) {
                        // 打开编辑记录页面
                        openEditRecordActivity(records[0])
                    }
                }
            }
        })
        
        // Setup reward notification click listener
        rewardNotification.setOnClickListener {
            showRewardDialog()
        }
    }
    
    // 打开编辑记录页面
    private fun openEditRecordActivity(record: BubbleTeaRecord) {
        val intent = Intent(requireContext(), EditRecordActivity::class.java)
        intent.putExtra(EditRecordActivity.EXTRA_RECORD_ID, record.id)
        intent.putExtra(EditRecordActivity.EXTRA_USER_ID, currentUserId)
        startActivity(intent)
    }
    
    // Setup listeners
    private fun setupListeners() {
        // Previous month button
        previousButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            updateSpinners()
            updateCalendar()
        }
        
        // Next month button
        nextButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            currentMonth = calendar.get(Calendar.MONTH)
            currentYear = calendar.get(Calendar.YEAR)
            updateSpinners()
            updateCalendar()
        }
        
        // Month selection
        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != currentMonth) {
                    currentMonth = position
                    calendar.set(Calendar.MONTH, currentMonth)
                    updateCalendar()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Year selection
        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedYear = years[position].toInt()
                if (selectedYear != currentYear) {
                    currentYear = selectedYear
                    calendar.set(Calendar.YEAR, currentYear)
                    updateCalendar()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    // Update spinner selections
    private fun updateSpinners() {
        monthSpinner.setSelection(currentMonth)
        yearSpinner.setSelection(years.indexOf("$currentYear"))
    }
    
    // Load bubble tea records from database
    private fun loadBubbleTeaRecords() {
        drinkRecords.clear()
        
        // Get all records
        val records = dbHelper.getAllBubbleTeaRecords(currentUserId)
        
        // Convert records to date->image path mapping
        for (record in records) {
            if (record.imagePath.isNotEmpty()) {
                drinkRecords[record.date] = record.imagePath
            }
        }
        
        Log.d("CalendarFragment", "Loaded ${drinkRecords.size} records from database")
    }
    
    // Initialize calendar
    private fun setupCalendar() {
        updateCalendar()
    }
    
    // Update calendar
    private fun updateCalendar() {
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)
        
        // Update month and year selectors
        updateSpinners()
        
        // Update month and year text
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        monthYearText.text = dateFormat.format(calendar.time)
        
        // Get days in month
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Get what day of week the first day is
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        var firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Adjust to start from 0
        if (firstDayOfWeek == 0) firstDayOfWeek = 7 // Sunday adjusted to 7
        
        // Generate calendar data
        val days = mutableListOf<CalendarDay>()
        
        // Add Monday to Sunday headers
        days.add(CalendarDay("Mo", "", false, false))
        days.add(CalendarDay("Tu", "", false, false))
        days.add(CalendarDay("We", "", false, false))
        days.add(CalendarDay("Th", "", false, false))
        days.add(CalendarDay("Fr", "", false, false))
        days.add(CalendarDay("Sa", "", false, false))
        days.add(CalendarDay("Su", "", false, false))
        
        // Add previous month's dates
        calendar.add(Calendar.MONTH, -1)
        val prevMonthDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.MONTH, 1) // Restore to current month
        
        // Add previous month's dates
        for (i in 0 until firstDayOfWeek - 1) {
            val day = prevMonthDays - (firstDayOfWeek - 2) + i
            val date = formatDate(currentYear, currentMonth - 1, day)
            val hasRecord = drinkRecords.containsKey(date)
            val iconPath = if (hasRecord) drinkRecords[date] ?: "" else ""
            days.add(CalendarDay(day.toString(), date, false, hasRecord, iconPath))
        }
        
        // Add current month's dates
        for (i in 1..daysInMonth) {
            val date = formatDate(currentYear, currentMonth, i)
            val hasRecord = drinkRecords.containsKey(date)
            val iconPath = if (hasRecord) drinkRecords[date] ?: "" else ""
            days.add(CalendarDay(i.toString(), date, true, hasRecord, iconPath))
        }
        
        // Add next month's dates
        val remainingCells = 7 - (days.size % 7)
        if (remainingCells < 7) {
            for (i in 1..remainingCells) {
                val date = formatDate(currentYear, currentMonth + 1, i)
                val hasRecord = drinkRecords.containsKey(date)
                val iconPath = if (hasRecord) drinkRecords[date] ?: "" else ""
                days.add(CalendarDay(i.toString(), date, false, hasRecord, iconPath))
            }
        }
        
        // Update adapter data
        calendarAdapter.updateData(days)
        
        // Calculate consecutive days without drinking bubble tea
        calculateSoberDays()
        
        // Update UI
        updateUI()
    }
    
    // Update UI
    private fun updateUI() {
        // 检查是否有记录
        val allRecords = dbHelper.getAllBubbleTeaRecords(currentUserId)
        val daysSuffix = view?.findViewById<TextView>(R.id.tv_sober_days_suffix)
        
        if (allRecords.isEmpty()) {
            // 如果没有记录，显示"No records yet"文本，隐藏其他文本
            noRecordsText.visibility = View.VISIBLE
            soberDaysText.visibility = View.INVISIBLE
            soberDaysCount.visibility = View.INVISIBLE
            daysSuffix?.visibility = View.INVISIBLE
            rewardNotification.visibility = View.INVISIBLE
        } else {
            // 有记录，显示正常文本，隐藏"No records yet"
            noRecordsText.visibility = View.GONE
            soberDaysText.visibility = View.VISIBLE
            soberDaysCount.visibility = View.VISIBLE
            soberDaysCount.text = soberDays.toString()
            daysSuffix?.visibility = View.VISIBLE
            
            // 检查是否显示奖励通知
            if (soberDays >= 3) {
                rewardNotification.visibility = View.VISIBLE
            } else {
                rewardNotification.visibility = View.INVISIBLE
            }
        }
    }
    
    // Calculate consecutive days without drinking bubble tea
    private fun calculateSoberDays() {
        // Get current date
        val today = Calendar.getInstance()
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
        
        // Get all records
        val allRecords = dbHelper.getAllBubbleTeaRecords(currentUserId)
        
        // If there are no records at all, set sober days to 0 instead of current day
        if (allRecords.isEmpty()) {
            soberDays = 0
            Log.d("CalendarFragment", "No records found, sober days set to 0")
            return
        }
        
        // Sort records by date (newest first)
        val sortedRecords = allRecords.sortedByDescending { it.date }
        
        // Start calculating consecutive days without bubble tea
        soberDays = 0
        val checkCalendar = Calendar.getInstance()
        
        // Check if there's a record for today
        val hasTodayRecord = sortedRecords.any { it.date == todayDate }
        
        if (hasTodayRecord) {
            // If there's a record for today, consecutive days is 0
            soberDays = 0
        } else {
            // If there's no record for today, count from today
            soberDays = 1 // At least today
            
            // Start checking from yesterday
            checkCalendar.add(Calendar.DAY_OF_MONTH, -1)
            
            // Set a maximum limit to prevent infinite loops (e.g., check up to 31 days in the past)
            val maxDaysToCheck = 31
            var daysChecked = 0
            
            while (daysChecked < maxDaysToCheck) {
                val dateToCheck = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkCalendar.time)
                val hasRecord = sortedRecords.any { it.date == dateToCheck }
                
                if (hasRecord) {
                    // Found a day with record, stop counting
                    break
                } else {
                    // No record, increment consecutive days
                    soberDays++
                    // Check previous day
                    checkCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    daysChecked++
                }
            }
            
            // If we've checked the maximum number of days and still haven't found a record,
            // it means the user has no records in the past maxDaysToCheck days
            if (daysChecked >= maxDaysToCheck) {
                Log.d("CalendarFragment", "No records found within $maxDaysToCheck days, capping sober days at $maxDaysToCheck")
                // We could cap the count at maxDaysToCheck if desired
                // soberDays = maxDaysToCheck
            }
        }
        
        Log.d("CalendarFragment", "Consecutive days without bubble tea: $soberDays")
    }
    
    // Format date as yyyy-MM-dd
    private fun formatDate(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.set(year, month, day)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(cal.time)
    }
    
    // Show reward dialog
    private fun showRewardDialog() {
        // 检查是否有记录，如果没有记录，不显示奖励
        val allRecords = dbHelper.getAllBubbleTeaRecords(currentUserId)
        if (allRecords.isEmpty()) {
            // 没有记录，不显示奖励
            Toast.makeText(context, getString(R.string.need_records_first), Toast.LENGTH_SHORT).show()
            return
        }
        
        // 有记录，显示奖励对话框
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.reward_title)
            .setMessage(R.string.reward_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                // Jump to main activity and mark as reward available
                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_SHOW_REWARD, true)
                startActivity(intent)
            }
            .setNegativeButton(R.string.no, null)
            .create()
        
        // Set "Do you want to try a new costume?" small text
        try {
            dialog.setOnShowListener {
                val textView = dialog.findViewById<TextView>(android.R.id.message)
                textView?.let {
                    val message = getString(R.string.reward_message)
                    val question = getString(R.string.reward_question)
                    val spannableString = SpannableString("$message\n\n$question")
                    val start = message.length + 2  // +2 for the newlines
                    val end = spannableString.length
                    spannableString.setSpan(RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    it.text = spannableString
                }
            }
        } catch (e: Exception) {
            Log.e("CalendarFragment", "Error setting dialog message style", e)
        }
        
        dialog.show()
    }
} 