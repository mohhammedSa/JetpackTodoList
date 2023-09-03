package com.example.jetpacktodolist.Database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast


const val DB_NAME = "TasksDB03"
const val TABLE_NAME = "TasksTable"
const val ID_COL = "id"
const val TASK_COL = "Task"
const val IS_CHECKED = "IsChecked"

class TaskDatabase(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
    override fun onCreate(p0: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME ($ID_COL INTEGER PRIMARY KEY, $TASK_COL TEXT, $IS_CHECKED INTEGER)"
        p0?.execSQL(createTable)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        val dropTable = "DROP TABLE IF EXISTS $TABLE_NAME"
        p0?.execSQL(dropTable)
    }

    fun addTask(task: TaskInfo) {
        val db = this.writableDatabase
        val content = ContentValues()
        content.put(TASK_COL, task.task)
        content.put(IS_CHECKED, task.isChecked)
        db.insert(TABLE_NAME, null, content)
        db.close()
    }

    @SuppressLint("Range")
    fun readTasks(): ArrayList<TaskInfo> {
        val db = this.readableDatabase
        val list = ArrayList<TaskInfo>()
        val query = "select * from $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(ID_COL))
                val taskText = cursor.getString(cursor.getColumnIndex(TASK_COL))
                val isChecked = cursor.getInt(cursor.getColumnIndex(IS_CHECKED))
                val task = TaskInfo(id, taskText, isChecked)
                list.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    @SuppressLint("Range")
    fun orderTaskAZ(): ArrayList<TaskInfo> {
        val db = this.readableDatabase
        val list = ArrayList<TaskInfo>()
        val query = "select * from $TABLE_NAME order by $TASK_COL ASC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(ID_COL))
                val taskText = cursor.getString(cursor.getColumnIndex(TASK_COL))
                val isChecked = cursor.getInt(cursor.getColumnIndex(IS_CHECKED))
                val task = TaskInfo(id, taskText, isChecked)
                list.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    @SuppressLint("Range")
    fun orderByChecked(): ArrayList<TaskInfo> {
        val db = this.readableDatabase
        val list = ArrayList<TaskInfo>()
        val query = "select * from $TABLE_NAME order by $IS_CHECKED DESC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(ID_COL))
                val taskText = cursor.getString(cursor.getColumnIndex(TASK_COL))
                val isChecked = cursor.getInt(cursor.getColumnIndex(IS_CHECKED))
                val task = TaskInfo(id, taskText, isChecked)
                list.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun deleteTask(id: Int): Boolean {
        val db = readableDatabase
        val selection = "$ID_COL =? "
        val selectionArgs = arrayOf(id.toString())
        val deleted = db.delete(TABLE_NAME, selection, selectionArgs)
        db.close()
        return deleted > 0
    }

    fun updateTask(task: TaskInfo): Boolean {
        val db = writableDatabase
        val content = ContentValues()
        content.put(ID_COL, task.id)
        content.put(TASK_COL, task.task)
        content.put(IS_CHECKED, task.isChecked)
        val selection = "$ID_COL = ?"
        val selectionArgs = arrayOf(task.id.toString())
        val updated = db.update(TABLE_NAME, content, selection, selectionArgs)
        db.close()
        return updated > 0
    }

    fun clearCheckedTasks() {
        val db = writableDatabase
        val selection = "$IS_CHECKED = ?"
        val selectionArgs = arrayOf("1")
        db.delete(TABLE_NAME, selection, selectionArgs)
        db.close()
    }

    fun clearAll() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}