package com.example.jetpacktodolist.UI

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jetpacktodolist.Database.TaskDatabase
import com.example.jetpacktodolist.Database.TaskInfo
import com.example.jetpacktodolist.R
import com.example.jetpacktodolist.ViewModel.MyViewModel
import com.example.jetpacktodolist.ui.theme.JetpackTodoListTheme

class MainActivity : ComponentActivity() {
    lateinit var db: TaskDatabase
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val viewModel by viewModels<MyViewModel>(factoryProducer = {
        object :
            ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyViewModel(db, sharedPref) as T
            }
        }
    })

    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = TaskDatabase(this)
        sharedPref = getSharedPreferences("Order_type", Context.MODE_PRIVATE)
        editor = sharedPref.edit()


        setContent {
            JetpackTodoListTheme {
                MyApp(
                    sharedPref, editor = editor, db = db, viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun MyApp(
    sharedPreferences: SharedPreferences,
    editor: Editor,
    db: TaskDatabase,
    viewModel: MyViewModel
) {
    val ctx: Context = LocalContext.current
    var isAlertShow by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = Color.LightGray) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                MyTopAppBar(
                    viewModel,
                    orderTaskAZ = {
                        viewModel.readOrderList()
                        editor.putString("orderType", "azOrder")
                        editor.apply()
                        viewModel.readType(sharedPreferences.getString("orderType", "")!!)
                    },
                    orderByCheck = {
                        viewModel.readListByChecked()
                        editor.putString("orderType", "checkedOrder")
                        editor.apply()
                        viewModel.readType(sharedPreferences.getString("orderType", "")!!)
                    },
                    clearAllClick = {
                        db.clearAll()
                        viewModel.readListAgain()
                    },
                    clearChecked = {
                        db.clearCheckedTasks()
                        viewModel.readListAgain()
                    })
                MyLazyColumn(
                    checkBoxClick = {
                        db.updateTask(it)
                        when (viewModel.readListType) {
                            "normal" -> viewModel.readListAgain()
                            "azOrder" -> viewModel.readOrderList()
                            "checkedOrder" -> viewModel.readListByChecked()
                        }
                    },
                    editIconClick = {
                        viewModel.returnTaskInfo(it)
                        viewModel.showEditAlert()
                        isAlertShow = false
                    },
                    deleteClick = {
                        db.deleteTask(it)
                        when (viewModel.readListType) {
                            "normal" -> viewModel.readListAgain()
                            "azOrder" -> viewModel.readOrderList()
                            "checkedOrder" -> viewModel.readListByChecked()
                        }
                    },
                    viewModel
                )
            }
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                shape = RoundedCornerShape(100.dp),
                onClick = {
                    isAlertShow = true
                    viewModel.hideEditAlert()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "", modifier = Modifier.size(40.dp))
            }
        }
        if (isAlertShow)
            AddAlertDialog(
                viewModel,
                addClick = {
                    if (it.task.isNotEmpty()) {
                        db.addTask(it)
                        when (viewModel.readListType) {
                            "normal" -> viewModel.readListAgain()
                            "azOrder" -> viewModel.readOrderList()
                            "checkedOrder" -> viewModel.readListByChecked()
                        }
                        isAlertShow = false
                    } else viewModel.checkFieldIsEmpty()
                },
                cancelClick = {
                    isAlertShow = false
                }
            )

        if (viewModel.isEditAlertShow)
            EditAlertDialog(
                viewModel,
                task = viewModel.taskInfo,
                editBtnClick = {
                    if (it.task.isNotEmpty()) {
                        db.updateTask(it)
                        when (viewModel.readListType) {
                            "normal" -> viewModel.readListAgain()
                            "azOrder" -> viewModel.readOrderList()
                            "checkedOrder" -> viewModel.readListByChecked()
                        }
                        viewModel.hideEditAlert()
                    } else viewModel.checkFieldIsEmpty()
                },
                cancelClick = {
                    viewModel.hideEditAlert()
                })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(
    viewModel: MyViewModel,
    orderTaskAZ: () -> Unit,
    orderByCheck: () -> Unit,
    clearAllClick: () -> Unit,
    clearChecked: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        actions = {
            IconButton(
                onClick = {
                    isExpanded = true
                }
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.background
                )
            }
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.order_a_z),
                            color = if (viewModel.readListType == "azOrder") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        orderTaskAZ()
                        isExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.order_by_checked),
                            color = if (viewModel.readListType == "checkedOrder") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        orderByCheck()
                        isExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.clear_all_tasks)) },
                    onClick = {
                        clearAllClick()
                        isExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.clear_all_checked)) },
                    onClick = {
                        clearChecked()
                        isExpanded = false
                    }
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.background
            )
        }
    )
}

@Composable
fun MyLazyColumn(
    checkBoxClick: (TaskInfo) -> Unit,
    editIconClick: (task: TaskInfo) -> Unit,
    deleteClick: (Int) -> Unit,
    viewModel: MyViewModel
) {
    LazyColumn(
        content = {
            items(viewModel.tasksList) { task ->
                key(task.id) {
                    ItemLayout(
                        task,
                        checkBoxClick = {
                            checkBoxClick(it)
                        },
                        editIconClick = { editIconClick(task) },
                        deleteClick = { deleteClick(task.id) }
                    )
                }
            }
        })
}

@Composable
fun ItemLayout(
    item: TaskInfo,
    checkBoxClick: (TaskInfo) -> Unit,
    editIconClick: () -> Unit,
    deleteClick: (Int) -> Unit,
) {
    val state = item.isChecked == 1
    var isChecked by rememberSaveable { mutableStateOf(state) }
    val textColor = if (isChecked) MaterialTheme.colorScheme.primary else Color.Black
    val textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, start = 10.dp, end = 10.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(end = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                modifier = Modifier
                    .wrapContentWidth(),
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    checkBoxClick(TaskInfo(item.id, item.task, if (isChecked) 1 else 0))
                })
            Text(
                color = textColor,
                textDecoration = textDecoration,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = item.task,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(5.dp))
            Row(modifier = Modifier.wrapContentWidth()) {
                Icon(imageVector = Icons.Filled.Edit,
                    "",
                    modifier = Modifier.clickable { editIconClick() })
                Spacer(modifier = Modifier.size(5.dp))
                Icon(imageVector = Icons.Filled.Delete,
                    "",
                    modifier = Modifier
                        .clickable { deleteClick(item.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(viewModel: MyViewModel, addClick: (TaskInfo) -> Unit, cancelClick: () -> Unit) {
    var taskText by rememberSaveable { mutableStateOf("") }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.Gray.copy(0.6f)
    ) {
        Surface(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(shape = RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.White)
                    .padding(horizontal = 15.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = taskText,
                    onValueChange = {
                        taskText = it
                        if (it.isEmpty()) viewModel.checkFieldIsEmpty()
                        else viewModel.isFieldEmpty = false
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.edit_text_hint)
                        )
                    },
                    supportingText = {
                        if (viewModel.isFieldEmpty) {
                            Text(text = "Error", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        if (viewModel.isFieldEmpty)
                            Icon(
                                painter = painterResource(id = R.drawable.error_icon),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.error
                            )
                    }
                )
                Spacer(modifier = Modifier.size(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.width(100.dp),
                        onClick = { addClick(TaskInfo(0, taskText, 0)) }
                    ) {
                        Text(
                            text = stringResource(id = R.string.add_btn_text),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    TextButton(onClick = cancelClick) {
                        Text(
                            text = stringResource(id = R.string.cancel_btn_text),
                            color = MaterialTheme.colorScheme.tertiary,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlertDialog(
    viewModel: MyViewModel,
    task: TaskInfo,
    editBtnClick: (TaskInfo) -> Unit,
    cancelClick: () -> Unit
) {
    var editedTaskText by rememberSaveable { mutableStateOf(task.task) }
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = Color.Gray.copy(0.6f)
    ) {
        Surface(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(shape = RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = editedTaskText,
                    onValueChange = {
                        editedTaskText = it
                        if (it.isEmpty()) viewModel.checkFieldIsEmpty()
                        else viewModel.isFieldEmpty = false
                    },
                    placeholder = {
                        Text(text = stringResource(id = R.string.edit_text_hint))
                    },
                    supportingText = {
                        if (viewModel.isFieldEmpty) {
                            Text(text = "Error", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    trailingIcon = {
                        if (viewModel.isFieldEmpty)
                            Icon(
                                painter = painterResource(id = R.drawable.error_icon),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.error
                            )
                    }
                )
                Spacer(modifier = Modifier.size(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier.width(100.dp),
                        onClick = {
                            editBtnClick(
                                TaskInfo(
                                    task.id,
                                    editedTaskText,
                                    task.isChecked
                                )
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.edit_btn_text),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    TextButton(onClick = cancelClick) {
                        Text(
                            text = stringResource(id = R.string.cancel_btn_text),
                            color = MaterialTheme.colorScheme.tertiary,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetpackTodoListTheme {}
}