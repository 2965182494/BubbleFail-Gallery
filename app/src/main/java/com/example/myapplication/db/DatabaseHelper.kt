package com.example.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myapplication.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BubbleTea.db"

        // 用户表
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 创建用户表
        val CREATE_USERS_TABLE = ("CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")")
        db.execSQL(CREATE_USERS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 如果数据库版本更新，删除旧表并重新创建
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // 添加用户（注册）
    fun addUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EMAIL, user.email)
        values.put(COLUMN_PASSWORD, user.password)
        
        // 插入行
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    // 检查用户是否存在（登录验证）
    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val selection = "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(email, password)
        
        val cursor = db.query(
            TABLE_USERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        
        val count = cursor.count
        cursor.close()
        db.close()
        
        return count > 0
    }
    
    // 检查邮箱是否已注册
    fun checkUserExists(email: String): Boolean {
        val db = this.readableDatabase
        val selection = "$COLUMN_EMAIL = ?"
        val selectionArgs = arrayOf(email)
        
        val cursor = db.query(
            TABLE_USERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        
        return exists
    }
} 