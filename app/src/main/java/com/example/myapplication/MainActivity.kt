package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.activity.AddRecordActivity
import com.example.myapplication.activity.CostumeActivity
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.fragment.CalendarFragment
import com.example.myapplication.fragment.ProfileFragment
import com.example.myapplication.fragment.StatisticsFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.myapplication.db.DatabaseHelper as UserDatabaseHelper

class MainActivity : AppCompatActivity() {
    
    // 视图组件
    private lateinit var daysCountTextView: TextView
    private lateinit var congratulations: TextView
    private lateinit var progressPercentageTextView: TextView
    private lateinit var cupLimitTextView: TextView
    private lateinit var cupStatusTextView: TextView
    private lateinit var progressButton: ConstraintLayout
    private lateinit var addRecordButton: FloatingActionButton
    private lateinit var homeButton: ImageView
    private lateinit var calendarButton: ImageView
    private lateinit var statisticsButton: ImageView
    private lateinit var profileButton: ImageView
    private lateinit var mainContent: ConstraintLayout
    private lateinit var fragmentContainer: View
    private lateinit var catBubbleTeaImageView: ImageView
    private lateinit var rewardIndicator: TextView
    
    // 装扮选择结果处理器
    private lateinit var costumeActivityResultLauncher: ActivityResultLauncher<Intent>
    
    // 奶茶记录数据
    private var cupLimit = 5
    private var cupsDrank = 0
    private var cupsRemaining = 0
    private var trackDays = 0
    private var progressPercentage = 0
    private var achievementCount = 0
    
    // 当前选中的导航项
    private var currentNavItem = NAV_HOME
    
    // 数据库帮助类
    private lateinit var dbHelper: DatabaseHelper
    
    // 当前用户ID
    private var currentUserId = 1 // 默认用户ID，实际应用中应该从登录状态获取
    
    // SharedPreferences 用于存储用户设置
    private lateinit var sharedPreferences: SharedPreferences
    
    // 记录上个月的月份，用于检测月份变化
    private var lastCheckedMonth = -1
    private var lastCheckedYear = -1
    
    // 当前选择的装扮ID
    private var currentCostumeId = 0
    
    companion object {
        private const val NAV_HOME = 0
        private const val NAV_CALENDAR = 1
        private const val NAV_STATISTICS = 2
        private const val NAV_PROFILE = 3
        const val PREF_NAME = "bubble_tea_prefs"
        private const val KEY_CUP_LIMIT = "cup_limit"
        private const val KEY_LAST_CHECKED_MONTH = "last_checked_month"
        private const val KEY_LAST_CHECKED_YEAR = "last_checked_year"
        const val KEY_CURRENT_COSTUME = "current_costume"
        const val EXTRA_SHOW_REWARD = "SHOW_REWARD"
        const val EXTRA_USER_EMAIL = "EMAIL"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // 获取用户邮箱
        val userEmail = intent.getStringExtra(EXTRA_USER_EMAIL) ?: ""
        
        // 初始化数据库帮助类
        dbHelper = DatabaseHelper(this)
        
        // 获取用户ID
        currentUserId = getUserIdFromEmail(userEmail)
        
        // 初始化装扮选择结果处理器
        costumeActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    // 获取选择的装扮ID
                    val costumeId = data.getIntExtra(KEY_CURRENT_COSTUME, currentCostumeId)
                    // 保存并更新装扮
                    saveCurrentCostume(costumeId)
                }
            }
        }
        
        // 处理窗口插入，但不为底部导航栏添加内边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 只应用左侧、顶部和右侧的内边距，不应用底部内边距
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        
        // 初始化SharedPreferences - 使用用户特定的配置文件
        sharedPreferences = getSharedPreferences("${PREF_NAME}_$currentUserId", Context.MODE_PRIVATE)
        
        // 从SharedPreferences加载杯数限制
        loadCupLimit()
        
        // 加载当前装扮
        loadCurrentCostume()
        
        // 加载上次检查的月份年份
        loadLastCheckedPeriod()
        
        // 初始化视图
        initViews()
        
        // 加载统计数据
        loadStatistics()
        
        // 检查月度目标
        checkMonthlyAchievement()
        
        // 更新UI
        updateUI()
        
        // 设置监听器
        setupListeners()
        
        // 默认显示主页
        showFragment(NAV_HOME)
        
        // 检查是否有奖励通知
        handleRewardIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleRewardIntent(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 每次恢复时重新加载统计数据
        loadStatistics()
        
        // 检查月度目标
        checkMonthlyAchievement()
        
        // 重新加载当前装扮并更新图片
        loadCurrentCostume()
        updateCatImage()
        
        updateUI()
    }
    
    // 处理奖励通知意图
    private fun handleRewardIntent(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_SHOW_REWARD, false)) {
            // 显示主页并显示奖励指示器
            showFragment(NAV_HOME)
            rewardIndicator.visibility = View.VISIBLE
        }
    }
    
    // 从SharedPreferences加载当前装扮ID
    private fun loadCurrentCostume() {
        currentCostumeId = sharedPreferences.getInt(KEY_CURRENT_COSTUME, 0)
    }
    
    // 保存当前装扮ID
    fun saveCurrentCostume(costumeId: Int) {
        sharedPreferences.edit().putInt(KEY_CURRENT_COSTUME, costumeId).apply()
        currentCostumeId = costumeId
        // 更新主界面图片
        updateCatImage()
    }
    
    // 更新猫咪图片
    private fun updateCatImage() {
        // 根据当前装扮ID设置不同的图片
        val imageResId = when (currentCostumeId) {
            0 -> R.drawable.cat_costume_default
            1 -> R.drawable.cat_costume_1
            2 -> R.drawable.cat_costume_2
            3 -> R.drawable.cat_costume_3
            4 -> R.drawable.cat_costume_4
            5 -> R.drawable.cat_costume_5
            6 -> R.drawable.cat_costume_6
            7 -> R.drawable.cat_costume_7
            else -> R.drawable.cat_costume_default
        }
        catBubbleTeaImageView.setImageResource(imageResId)
    }
    
    // 加载杯数限制
    private fun loadCupLimit() {
        cupLimit = sharedPreferences.getInt(KEY_CUP_LIMIT, 5) // 默认5杯
    }
    
    // 保存杯数限制
    private fun saveCupLimit(limit: Int) {
        sharedPreferences.edit().putInt(KEY_CUP_LIMIT, limit).apply()
        cupLimit = limit
    }
    
    // 加载上次检查的月份年份
    private fun loadLastCheckedPeriod() {
        lastCheckedMonth = sharedPreferences.getInt(KEY_LAST_CHECKED_MONTH, -1)
        lastCheckedYear = sharedPreferences.getInt(KEY_LAST_CHECKED_YEAR, -1)
    }
    
    // 保存上次检查的月份年份
    private fun saveLastCheckedPeriod(year: Int, month: Int) {
        sharedPreferences.edit()
            .putInt(KEY_LAST_CHECKED_MONTH, month)
            .putInt(KEY_LAST_CHECKED_YEAR, year)
            .apply()
        lastCheckedMonth = month
        lastCheckedYear = year
    }
    
    // 加载统计数据
    private fun loadStatistics() {
        // 获取当前月份的第一天和最后一天
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        
        // 设置为当月第一天
        calendar.set(year, month, 1)
        val firstDayOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        // 设置为当月最后一天
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDayOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        // 获取当前日期
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        
        // 获取本月所有记录
        val allRecords = dbHelper.getAllBubbleTeaRecords(currentUserId)
        
        // 筛选当月记录
        val monthRecords = allRecords.filter { 
            it.date >= firstDayOfMonth && it.date <= lastDayOfMonth 
        }
        
        // 获取本月不同日期的记录数量（每天只算一次）
        val distinctDates = monthRecords.map { it.date }.distinct()
        cupsDrank = distinctDates.size
        
        // 计算坚持天数（连续多少天没喝奶茶）
        val sortedDates = distinctDates.sorted()
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        
        // 如果今天没有记录，则从今天开始计算；否则从最后一条记录的第二天开始计算
        val startDay = if (sortedDates.contains(today)) {
            0 // 今天有记录，从0开始计算
        } else {
            // 找到最后一条记录的日期
            val lastRecordDate = sortedDates.lastOrNull()
            if (lastRecordDate != null) {
                // 解析最后一条记录的日期
                val lastRecordCalendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                lastRecordCalendar.time = sdf.parse(lastRecordDate) ?: Calendar.getInstance().time
                
                // 计算距离今天的天数
                val lastRecordDay = lastRecordCalendar.get(Calendar.DAY_OF_MONTH)
                currentDay - lastRecordDay
            } else {
                // 没有任何记录，从月初开始计算
                currentDay
            }
        }
        
        trackDays = startDay
        
        // 获取达成目标的次数
        achievementCount = dbHelper.getAchievementCount(currentUserId)
        
        // 计算剩余杯数和进度百分比
        calculateRemainingCups()
    }
    
    // 初始化视图组件
    private fun initViews() {
        daysCountTextView = findViewById(R.id.tv_days_count)
        progressPercentageTextView = findViewById(R.id.tv_progress_percentage)
        cupLimitTextView = findViewById(R.id.tv_cup_limit)
        cupStatusTextView = findViewById(R.id.tv_cup_status)
        progressButton = findViewById(R.id.btn_progress)
        addRecordButton = findViewById(R.id.fab_add)
        homeButton = findViewById(R.id.btn_home)
        calendarButton = findViewById(R.id.btn_calendar)
        statisticsButton = findViewById(R.id.btn_statistics)
        profileButton = findViewById(R.id.btn_profile)
        mainContent = findViewById(R.id.main_content)
        fragmentContainer = findViewById(R.id.fragment_container)
        catBubbleTeaImageView = findViewById(R.id.iv_cat_bubble_tea)
        rewardIndicator = findViewById(R.id.tv_main_reward_indicator)
//        congratulations = findViewById(R.id.tv_congratulations)
//        val typeFace1: Typeface = Typeface.createFromAsset(assets, "fonts/iconfont.ttf")
//        congratulations.setTypeface(typeFace1);
        // 设置奖励指示器点击事件
        rewardIndicator.setOnClickListener {
            // 跳转到换装页面
            val intent = Intent(this, CostumeActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            costumeActivityResultLauncher.launch(intent)
        }
        
        // 更新猫咪图片
        updateCatImage()
        
        // 初始化导航图标状态
        updateNavUI(currentNavItem)
    }
    
    // 更新UI显示
    private fun updateUI() {
        // 更新达成目标次数
        daysCountTextView.text = achievementCount.toString()
        
        // 更新进度百分比
        progressPercentageTextView.text = "$progressPercentage%"
        
        // 更新杯数限制
        val limitText = "$cupLimit-cup limit"
        val limitSpannable = SpannableString(limitText)
        limitSpannable.setSpan(RelativeSizeSpan(1.3f), 0, cupLimit.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        cupLimitTextView.text = limitSpannable
        
        // 更新杯数状态
        val drankText = "Drank: $cupsDrank"
        val remainingText = "Remaining: $cupsRemaining"
        val statusText = "$drankText\n$remainingText"
        val statusSpannable = SpannableString(statusText)
        
        // 为"Drank:"后面的数字设置更大的尺寸
        statusSpannable.setSpan(
            RelativeSizeSpan(1.3f),
            drankText.indexOf(cupsDrank.toString()),
            drankText.indexOf(cupsDrank.toString()) + cupsDrank.toString().length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 为"Remaining:"后面的数字设置更大的尺寸
        statusSpannable.setSpan(
            RelativeSizeSpan(1.3f),
            statusText.indexOf(cupsRemaining.toString(), drankText.length),
            statusText.indexOf(cupsRemaining.toString(), drankText.length) + cupsRemaining.toString().length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        cupStatusTextView.text = statusSpannable
    }
    
    // 检查月度目标是否达成
    private fun checkMonthlyAchievement() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        
        // 检查是否是新的月份
        if (lastCheckedMonth != -1 && lastCheckedYear != -1) {
            // 如果是上个月的检查，检查上个月是否达成目标
            if (lastCheckedMonth != currentMonth || lastCheckedYear != currentYear) {
                // 检查上个月是否达成目标
                checkPreviousMonthAchievement(lastCheckedYear, lastCheckedMonth)
            }
        }
        
        // 更新最后检查的年月
        saveLastCheckedPeriod(currentYear, currentMonth)
    }
    
    // 检查上个月是否达成目标
    private fun checkPreviousMonthAchievement(year: Int, month: Int) {
        // 获取上个月的记录
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val firstDayOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDayOfMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        // 获取所有记录
        val allRecords = dbHelper.getAllBubbleTeaRecords(currentUserId)
        
        // 筛选上个月记录
        val monthRecords = allRecords.filter { 
            it.date >= firstDayOfMonth && it.date <= lastDayOfMonth 
        }
        
        // 获取上个月不同日期的记录数量（每天只算一次）
        val distinctDates = monthRecords.map { it.date }.distinct()
        val cupsDrankLastMonth = distinctDates.size
        
        // 读取上个月的杯数限制（这里简化处理，使用当前的杯数限制）
        val cupLimitLastMonth = cupLimit
        
        // 如果没有超过限制，记录为达成目标
        if (cupsDrankLastMonth <= cupLimitLastMonth) {
            // 检查是否已经记录过
            if (!dbHelper.hasMonthlyAchievement(currentUserId, year, month)) {
                // 记录达成目标
                dbHelper.recordMonthlyAchievement(currentUserId, year, month, cupLimitLastMonth)
                
                // 更新达成目标次数
                achievementCount = dbHelper.getAchievementCount(currentUserId)
                
                // 显示成就提示
                Toast.makeText(this, "恭喜！你在上个月成功控制了奶茶消费", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // 设置监听器
    private fun setupListeners() {
        // 进度按钮点击事件 - 设置杯数限制
        progressButton.setOnClickListener {
            showCupLimitDialog()
        }
        
        // 添加记录按钮点击事件
        addRecordButton.setOnClickListener {
            // 跳转到添加记录界面
            val intent = Intent(this, AddRecordActivity::class.java)
            intent.putExtra(AddRecordActivity.EXTRA_USER_ID, currentUserId)
            startActivity(intent)
        }
        
        // 底部导航按钮点击事件
        homeButton.setOnClickListener {
            showFragment(NAV_HOME)
        }
        
        calendarButton.setOnClickListener {
            showFragment(NAV_CALENDAR)
        }
        
        statisticsButton.setOnClickListener {
            showFragment(NAV_STATISTICS)
        }
        
        profileButton.setOnClickListener {
            showFragment(NAV_PROFILE)
        }
    }
    
    // 显示设置杯数限制对话框
    private fun showCupLimitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_cup_limit, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.number_picker)
        
        // 设置NumberPicker的范围和当前值
        numberPicker.minValue = 1
        numberPicker.maxValue = 30
        numberPicker.value = cupLimit
        
        // 创建对话框
        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Monthly Cup Limit")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val newLimit = numberPicker.value
                saveCupLimit(newLimit)
                calculateRemainingCups()
                updateUI()
                Toast.makeText(this, "已设置本月奶茶限制为 $newLimit 杯", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .create()
        
        dialog.show()
    }
    
    // 切换Fragment
    private fun showFragment(navItem: Int) {
        if (currentNavItem == navItem) {
            return
        }
        
        currentNavItem = navItem
        
        // 更新UI状态
        updateNavUI(navItem)
        
        if (navItem == NAV_HOME) {
            // 显示主页内容，隐藏Fragment容器
            mainContent.visibility = View.VISIBLE
            fragmentContainer.visibility = View.GONE
            
            // 清除当前Fragment
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(currentFragment)
                    .commit()
            }
        } else {
            // 隐藏主页内容，显示Fragment容器
            mainContent.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            
            // 创建并显示对应的Fragment，并传递用户ID
            val fragment = when (navItem) {
                NAV_CALENDAR -> {
                    val fragment = CalendarFragment()
                    val args = Bundle()
                    args.putInt("USER_ID", currentUserId)
                    fragment.arguments = args
                    fragment
                }
                NAV_STATISTICS -> {
                    val fragment = StatisticsFragment()
                    val args = Bundle()
                    args.putInt("USER_ID", currentUserId)
                    fragment.arguments = args
                    fragment
                }
                NAV_PROFILE -> {
                    val fragment = ProfileFragment()
                    val args = Bundle()
                    args.putInt("USER_ID", currentUserId)
                    fragment.arguments = args
                    fragment
                }
                else -> null
            }
            
            // 如果Fragment不为空，则替换当前Fragment
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            }
        }
    }
    
    // 更新导航UI
    private fun updateNavUI(navItem: Int) {
        // 重置所有按钮状态为未选中图标
        homeButton.setImageResource(R.drawable.b1)
        calendarButton.setImageResource(R.drawable.b2)
        statisticsButton.setImageResource(R.drawable.b3)
        profileButton.setImageResource(R.drawable.b4)
        
        // 设置选中按钮状态为选中图标
        when (navItem) {
            NAV_HOME -> homeButton.setImageResource(R.drawable.y1)
            NAV_CALENDAR -> calendarButton.setImageResource(R.drawable.y2)
            NAV_STATISTICS -> statisticsButton.setImageResource(R.drawable.y3)
            NAV_PROFILE -> profileButton.setImageResource(R.drawable.y4)
        }
    }
    
    // 计算剩余杯数
    private fun calculateRemainingCups() {
        cupsRemaining = cupLimit - cupsDrank
        if (cupsRemaining < 0) cupsRemaining = 0
        
        // 计算进度百分比
        progressPercentage = if (cupLimit > 0) {
            (cupsDrank * 100) / cupLimit
        } else {
            0
        }
    }
    
    // 根据邮箱获取用户ID
    private fun getUserIdFromEmail(email: String): Int {
        if (email.isEmpty()) {
            return 1 // 默认用户ID
        }
        
        // 使用db包中的DatabaseHelper获取用户ID
        val dbUserHelper = UserDatabaseHelper(this)
        val userId = dbUserHelper.getUserIdByEmail(email)
        
        return if (userId > 0) userId else 1 // 如果找不到用户，返回默认ID
    }
}