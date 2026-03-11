package com.example.bd_system

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyDbHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "BloodDonation.db"
        private const val DATABASE_VERSION = 7

        const val TABLE_USERS = "Users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_UID = "uid"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_BLOOD_GROUP = "blood_group"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_ROLE = "role"

        const val TABLE_HISTORY = "Donation_History"
        const val COLUMN_HISTORY_ID = "history_id"
        const val COLUMN_HIST_USER_ID = "user_id"
        const val COLUMN_DONATION_DATE = "donation_date"
        const val COLUMN_UNITS = "units"

        const val TABLE_REQUEST = "Request"
        const val COLUMN_REQUEST_ID = "request_id"
        const val COLUMN_REQ_USER_ID = "user_id"
        const val COLUMN_HOSPITAL_NAME = "hospital_name"
        const val COLUMN_UNIT_REQUIRED = "unit_required"
        const val COLUMN_URGENCY_LEVEL = "urgency_level"
        const val COLUMN_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_UID TEXT," +
                "$COLUMN_FULL_NAME TEXT," +
                "$COLUMN_EMAIL TEXT UNIQUE," +
                "$COLUMN_PASSWORD TEXT," +
                "$COLUMN_BLOOD_GROUP TEXT," +
                "$COLUMN_GENDER TEXT," +
                "$COLUMN_PHONE TEXT," +
                "$COLUMN_ADDRESS TEXT," +
                "$COLUMN_ROLE TEXT)")

        val createHistoryTable = ("CREATE TABLE $TABLE_HISTORY (" +
                "$COLUMN_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_HIST_USER_ID INTEGER," +
                "$COLUMN_DONATION_DATE TEXT," +
                "$COLUMN_UNITS INTEGER," +
                "FOREIGN KEY($COLUMN_HIST_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID))")

        val createRequestTable = ("CREATE TABLE $TABLE_REQUEST (" +
                "$COLUMN_REQUEST_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_REQ_USER_ID INTEGER," +
                "$COLUMN_HOSPITAL_NAME TEXT," +
                "$COLUMN_UNIT_REQUIRED INTEGER," +
                "$COLUMN_URGENCY_LEVEL TEXT," +
                "$COLUMN_STATUS TEXT," +
                "FOREIGN KEY($COLUMN_REQ_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID))")

        db?.execSQL(createUsersTable)
        db?.execSQL(createHistoryTable)
        db?.execSQL(createRequestTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_REQUEST")
        onCreate(db)
    }

    fun registerUser(fullName: String, email: String, password: String, bloodGroup: String, gender: String, phone: String, address: String, uid: String = ""): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_UID, uid)
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_BLOOD_GROUP, bloodGroup)
            put(COLUMN_GENDER, gender)
            put(COLUMN_PHONE, phone)
            put(COLUMN_ADDRESS, address)
            put(COLUMN_ROLE, "user")
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    fun loginUser(email: String, password: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ROLE FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?", arrayOf(email, password))
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return role
    }

    fun getUserDetailsByEmail(email: String): UserProfile? {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_UID, $COLUMN_FULL_NAME, $COLUMN_BLOOD_GROUP, $COLUMN_ADDRESS, $COLUMN_PHONE, $COLUMN_EMAIL, $COLUMN_GENDER FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        var userProfile: UserProfile? = null
        if (cursor.moveToFirst()) {
            userProfile = UserProfile(
                uid = cursor.getString(0) ?: "",
                fullName = cursor.getString(1),
                bloodGroup = cursor.getString(2),
                address = cursor.getString(3),
                phone = cursor.getString(4),
                email = cursor.getString(5),
                gender = cursor.getString(6)
            )
        }
        cursor.close()
        db.close()
        return userProfile
    }

    fun updateUserDetails(originalEmail: String, fullName: String, bloodGroup: String, address: String, phone: String, newEmail: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, fullName)
            put(COLUMN_BLOOD_GROUP, bloodGroup)
            put(COLUMN_ADDRESS, address)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, newEmail)
        }
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(originalEmail))
        db.close()
        return result
    }

    fun updatePassword(email: String, newPassword: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        val result = db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email))
        db.close()
        return result
    }

    fun checkPassword(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?", arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun addDonation(userId: Int, units: Int): Long {
        val db = this.writableDatabase
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put(COLUMN_HIST_USER_ID, userId)
            put(COLUMN_DONATION_DATE, currentTime)
            put(COLUMN_UNITS, units)
        }
        val result = db.insert(TABLE_HISTORY, null, values)
        db.close()
        return result
    }

    fun getTotalDonatedUnits(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COLUMN_UNITS) FROM $TABLE_HISTORY WHERE $COLUMN_HIST_USER_ID = ?", arrayOf(userId.toString()))
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getActiveRequestsCount(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_REQUEST WHERE $COLUMN_REQ_USER_ID = ? AND $COLUMN_STATUS = 'Pending'", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getDonationHistory(userId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_HISTORY WHERE $COLUMN_HIST_USER_ID = ? ORDER BY $COLUMN_HISTORY_ID DESC", arrayOf(userId.toString()))
    }

    fun getRequestHistory(userId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_REQUEST WHERE $COLUMN_REQ_USER_ID = ? ORDER BY $COLUMN_REQUEST_ID DESC", arrayOf(userId.toString()))
    }
}
