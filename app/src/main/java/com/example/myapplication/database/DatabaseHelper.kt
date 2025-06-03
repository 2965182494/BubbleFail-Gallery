package com.example.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myapplication.model.BubbleTeaRecord

/**
 * 数据库帮助类，用于管理奶茶记录的存储
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "bubble_tea_db"
        private const val DATABASE_VERSION = 2

        // 用户表
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"

        // 奶茶记录表
        const val TABLE_RECORDS = "bubble_tea_records"
        const val COLUMN_RECORD_ID = "id"
        const val COLUMN_RECORD_DATE = "date"
        const val COLUMN_RECORD_NAME = "name"
        const val COLUMN_RECORD_EXPENSE = "expense"
        const val COLUMN_RECORD_SWEETNESS = "sweetness"
        const val COLUMN_RECORD_IMAGE_PATH = "image_path"
        const val COLUMN_RECORD_USER_ID = "user_id"
        
        // 达成目标记录表
        const val TABLE_ACHIEVEMENTS = "user_achievements"
        const val COLUMN_ACHIEVEMENT_ID = "id"
        const val COLUMN_ACHIEVEMENT_USER_ID = "user_id"
        const val COLUMN_ACHIEVEMENT_YEAR = "year"
        const val COLUMN_ACHIEVEMENT_MONTH = "month"
        const val COLUMN_ACHIEVEMENT_LIMIT = "cup_limit"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 创建用户表
        val createUserTable = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_EMAIL TEXT UNIQUE, " +
                "$COLUMN_USER_PASSWORD TEXT)"
        db.execSQL(createUserTable)

        // 创建奶茶记录表
        val createRecordTable = "CREATE TABLE $TABLE_RECORDS (" +
                "$COLUMN_RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_RECORD_DATE TEXT, " +
                "$COLUMN_RECORD_NAME TEXT, " +
                "$COLUMN_RECORD_EXPENSE REAL, " +
                "$COLUMN_RECORD_SWEETNESS TEXT, " +
                "$COLUMN_RECORD_IMAGE_PATH TEXT, " +
                "$COLUMN_RECORD_USER_ID INTEGER, " +
                "FOREIGN KEY($COLUMN_RECORD_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID))"
        db.execSQL(createRecordTable)
        
        // 创建达成目标记录表
        val createAchievementTable = "CREATE TABLE $TABLE_ACHIEVEMENTS (" +
                "$COLUMN_ACHIEVEMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_ACHIEVEMENT_USER_ID INTEGER, " +
                "$COLUMN_ACHIEVEMENT_YEAR INTEGER, " +
                "$COLUMN_ACHIEVEMENT_MONTH INTEGER, " +
                "$COLUMN_ACHIEVEMENT_LIMIT INTEGER, " +
                "FOREIGN KEY($COLUMN_ACHIEVEMENT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID), " +
                "UNIQUE($COLUMN_ACHIEVEMENT_USER_ID, $COLUMN_ACHIEVEMENT_YEAR, $COLUMN_ACHIEVEMENT_MONTH))"
        db.execSQL(createAchievementTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 创建达成目标记录表
            val createAchievementTable = "CREATE TABLE $TABLE_ACHIEVEMENTS (" +
                    "$COLUMN_ACHIEVEMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_ACHIEVEMENT_USER_ID INTEGER, " +
                    "$COLUMN_ACHIEVEMENT_YEAR INTEGER, " +
                    "$COLUMN_ACHIEVEMENT_MONTH INTEGER, " +
                    "$COLUMN_ACHIEVEMENT_LIMIT INTEGER, " +
                    "FOREIGN KEY($COLUMN_ACHIEVEMENT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID), " +
                    "UNIQUE($COLUMN_ACHIEVEMENT_USER_ID, $COLUMN_ACHIEVEMENT_YEAR, $COLUMN_ACHIEVEMENT_MONTH))"
            db.execSQL(createAchievementTable)
        }
    }

    /**
     * 添加奶茶记录
     */
    fun addBubbleTeaRecord(record: BubbleTeaRecord, userId: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECORD_DATE, record.date)
            put(COLUMN_RECORD_NAME, record.name)
            put(COLUMN_RECORD_EXPENSE, record.expense)
            put(COLUMN_RECORD_SWEETNESS, record.sweetness)
            put(COLUMN_RECORD_IMAGE_PATH, record.imagePath)
            put(COLUMN_RECORD_USER_ID, userId)
        }

        // 插入记录并返回记录ID
        val id = db.insert(TABLE_RECORDS, null, values)
        db.close()
        return id
    }

    /**
     * 获取用户的所有奶茶记录
     */
    fun getAllBubbleTeaRecords(userId: Int): List<BubbleTeaRecord> {
        val records = mutableListOf<BubbleTeaRecord>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RECORDS WHERE $COLUMN_RECORD_USER_ID = $userId ORDER BY $COLUMN_RECORD_DATE DESC"
        
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_ID))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_DATE))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_NAME))
                val expense = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RECORD_EXPENSE))
                val sweetness = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_SWEETNESS))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_IMAGE_PATH))
                
                val record = BubbleTeaRecord(id, date, name, expense, sweetness, imagePath)
                records.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
    }

    /**
     * 获取指定日期的奶茶记录
     */
    fun getBubbleTeaRecordsByDate(date: String, userId: Int): List<BubbleTeaRecord> {
        val records = mutableListOf<BubbleTeaRecord>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RECORDS WHERE $COLUMN_RECORD_DATE = ? AND $COLUMN_RECORD_USER_ID = ?"
        
        val cursor = db.rawQuery(query, arrayOf(date, userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_ID))
                val recordDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_DATE))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_NAME))
                val expense = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RECORD_EXPENSE))
                val sweetness = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_SWEETNESS))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_IMAGE_PATH))
                
                val record = BubbleTeaRecord(id, recordDate, name, expense, sweetness, imagePath)
                records.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
    }

    /**
     * 通过ID获取奶茶记录
     */
    fun getBubbleTeaRecordById(recordId: Int): List<BubbleTeaRecord> {
        val records = mutableListOf<BubbleTeaRecord>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RECORDS WHERE $COLUMN_RECORD_ID = ?"
        
        val cursor = db.rawQuery(query, arrayOf(recordId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD_ID))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_DATE))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_NAME))
                val expense = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RECORD_EXPENSE))
                val sweetness = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_SWEETNESS))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_IMAGE_PATH))
                
                val record = BubbleTeaRecord(id, date, name, expense, sweetness, imagePath)
                records.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
    }

    /**
     * 获取所有有记录的日期
     */
    fun getAllRecordDates(userId: Int): List<String> {
        val dates = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $COLUMN_RECORD_DATE FROM $TABLE_RECORDS WHERE $COLUMN_RECORD_USER_ID = ? ORDER BY $COLUMN_RECORD_DATE DESC"
        
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECORD_DATE))
                dates.add(date)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return dates
    }

    /**
     * 删除奶茶记录
     */
    fun deleteBubbleTeaRecord(recordId: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_RECORDS, "$COLUMN_RECORD_ID = ?", arrayOf(recordId.toString()))
        db.close()
        return result > 0
    }

    /**
     * 更新奶茶记录
     */
    fun updateBubbleTeaRecord(record: BubbleTeaRecord): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECORD_DATE, record.date)
            put(COLUMN_RECORD_NAME, record.name)
            put(COLUMN_RECORD_EXPENSE, record.expense)
            put(COLUMN_RECORD_SWEETNESS, record.sweetness)
            put(COLUMN_RECORD_IMAGE_PATH, record.imagePath)
        }

        val result = db.update(TABLE_RECORDS, values, "$COLUMN_RECORD_ID = ?", arrayOf(record.id.toString()))
        db.close()
        return result > 0
    }

    /**
     * 记录用户达成本月奶茶限制目标
     */
    fun recordMonthlyAchievement(userId: Int, year: Int, month: Int, cupLimit: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ACHIEVEMENT_USER_ID, userId)
            put(COLUMN_ACHIEVEMENT_YEAR, year)
            put(COLUMN_ACHIEVEMENT_MONTH, month)
            put(COLUMN_ACHIEVEMENT_LIMIT, cupLimit)
        }

        // 使用REPLACE策略，如果已存在则替换
        val id = db.insertWithOnConflict(TABLE_ACHIEVEMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return id
    }

    /**
     * 获取用户达成目标的总次数
     */
    fun getAchievementCount(userId: Int): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_ACHIEVEMENTS WHERE $COLUMN_ACHIEVEMENT_USER_ID = $userId"
        
        val cursor = db.rawQuery(query, null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    /**
     * 检查用户某月是否已达成目标
     */
    fun hasMonthlyAchievement(userId: Int, year: Int, month: Int): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_ACHIEVEMENTS WHERE " +
                "$COLUMN_ACHIEVEMENT_USER_ID = $userId AND " +
                "$COLUMN_ACHIEVEMENT_YEAR = $year AND " +
                "$COLUMN_ACHIEVEMENT_MONTH = $month"
        
        val cursor = db.rawQuery(query, null)
        var hasAchievement = false
        if (cursor.moveToFirst()) {
            hasAchievement = cursor.getInt(0) > 0
        }
        cursor.close()
        db.close()
        return hasAchievement
    }
    
    /**
     * 清空用户的所有奶茶记录
     * @return 删除的记录数量
     */
    fun clearUserRecords(userId: Int): Int {
        val db = this.writableDatabase
        // 删除奶茶记录
        val recordsDeleted = db.delete(TABLE_RECORDS, "$COLUMN_RECORD_USER_ID = ?", arrayOf(userId.toString()))
        // 删除成就记录
        db.delete(TABLE_ACHIEVEMENTS, "$COLUMN_ACHIEVEMENT_USER_ID = ?", arrayOf(userId.toString()))
        db.close()
        return recordsDeleted
    }
} 