package com.example.jetpacktodolist.ViewModel

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.jetpacktodolist.Database.TaskDatabase
import com.example.jetpacktodolist.Database.TaskInfo

class MyViewModel(private val db: TaskDatabase, val sharedPref: SharedPreferences) : ViewModel() {

    var readListType by mutableStateOf(sharedPref.getString("orderType", "normal")!!)
        private set


    fun readType(readType: String) {
        readListType = readType
    }

    var tasksList by mutableStateOf(
        when (readListType) {
            "normal" -> {
                db.readTasks()
            }
            "azOrder" -> {
                db.orderTaskAZ()
            }
            else -> {
                db.orderByChecked()
            }
        }
    )
        private set

    fun readListAgain() {
        tasksList = db.readTasks()
    }

    fun readOrderList() {
        tasksList = db.orderTaskAZ()
    }

    fun readListByChecked() {
        tasksList = db.orderByChecked()
    }



    var isEditAlertShow by mutableStateOf(false)
        private set

    fun showEditAlert() {
        isEditAlertShow = true
    }

    fun hideEditAlert() {
        isEditAlertShow = false
    }

    var taskInfo by mutableStateOf(TaskInfo())

    fun returnTaskInfo(task: TaskInfo): TaskInfo {
        taskInfo = task
        return taskInfo
    }


    var isFieldEmpty by mutableStateOf(false)

    fun checkFieldIsEmpty() {
        isFieldEmpty = true
    }
}