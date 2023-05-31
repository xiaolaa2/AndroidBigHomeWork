package com.example.androidbighomework.todoPage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidbighomework.MyApplication
import com.example.androidbighomework.R
import com.example.androidbighomework.Theme.MyTheme
import com.example.androidbighomework.ViewModel.TodoPageViewModel
import com.example.androidbighomework.todoPage.Dao.Todo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TodoPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodoPageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onStart() {
        super.onStart()
        // 设置一下状态以便service的开启和关闭
        MyApplication.todoStatus.value = ConcentrateStatus.NotBegin
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_todo_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCompose()
    }

    // 随机获取背景图片
    @SuppressLint("DiscouragedApi")
    private fun GetRandomPicture(): Int {
        val ranId = Random(System.currentTimeMillis()).nextInt(5)
        val imageList = listOf<Int>(
            R.drawable.bg1,
            R.drawable.bg2,
            R.drawable.bg3,
            R.drawable.bg4,
            R.drawable.bg5
        )
        return imageList[ranId]
    }

    @SuppressLint("ShowToast")
    @OptIn(ExperimentalMaterial3Api::class)
    private fun initCompose() {
        requireView().findViewById<ComposeView>(R.id.compose_view).setContent {
            // 顶部的大背景
            MyTheme {
                ConstraintLayout {
                    val (button, mainContent) = createRefs()
                    val coroutineScope = rememberCoroutineScope()
                    val viewModel: TodoPageViewModel = viewModel()

                    // 待办列表获取
                    val todoList = viewModel.todoList.collectAsState()

                    // 用户当前选中的todo
                    var nowPickTodo by remember {
                        mutableStateOf(Todo(-1, "", 1, 1, 0, "", 0, "", "", "", 0, 0))
                    }
                    var nowPickTodoIndex by remember {
                        mutableStateOf(-1)
                    }

                    /*
                     * 控制显示的变量
                     */
                    // 显示添加待办的对话框
                    var dialogVisiable by remember {
                        mutableStateOf(false)
                    }

                    var showTodoEditDialog by remember {
                        mutableStateOf(false)
                    }
                    var confirmTodoDeleteDialog by remember {
                        mutableStateOf(false)
                    }
                    // 修改待办
                    var showTodoEditDialog2 by remember {
                        mutableStateOf(false)
                    }

                    if (confirmTodoDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                confirmTodoDeleteDialog = false
                                showTodoEditDialog = true
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        // 删除todo
                                        val todoDao = MyApplication.db.todoDao()
                                        val result = todoDao.deleteTodo(todoList.value[nowPickTodoIndex])
                                        Log.d("todo", todoList.value[nowPickTodoIndex].todoName)
                                        // 移除ui显示中的todo
                                        CoroutineScope(Dispatchers.Main).launch {
                                            if (result > 0) {
                                                viewModel.removeTodoByIndex(nowPickTodoIndex)
                                                Toast.makeText(
                                                    context,
                                                    "待办删除成功",
                                                    Toast.LENGTH_SHORT
                                                )
                                                confirmTodoDeleteDialog = false
                                            }
                                        }
                                    }
                                }) {
                                    Text("确认", color = Color(0xff409EFF))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    confirmTodoDeleteDialog = false
                                    showTodoEditDialog = true
                                }) {
                                    Text("取消", color = Color(0xffF56C6C))
                                }
                            },
                            title = { Text(text = "删除", color = Color(0xffF56C6C)) },
                            text = { Text(text = "确定删除此待办?") },
                            modifier = Modifier.clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                        )
                    }

                    if (showTodoEditDialog2) {
                        var addTodoText by remember {
                            mutableStateOf("")
                        }
                        var customTimeText by remember {
                            mutableStateOf("")
                        }

                        var todoTypeIndex by remember {
                            mutableStateOf(-1)
                        }
                        val todoTypeList = mutableListOf<String>("普通番茄钟", "定目标", "养习惯")
                        var countTypeIndex by remember {
                            mutableStateOf(-1)
                        }
                        val countTypeList = mutableListOf<String>("倒计时", "正向计时", "不计时")
                        var totalTimeIndex by remember {
                            mutableStateOf(-1)
                        }
                        val totalTimeList = mutableListOf<String>("25分钟", "35分钟", "自定义")

                        // 显示自定义时间输入框
                        val showCustomTotalTime = remember {
                            derivedStateOf { totalTimeIndex == 2 }
                        }
                        MyDialog(
                            dialogVisible = showTodoEditDialog2,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)   // 设置大小为父元素的75%
                                .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                .background(color = Color.White),
                            onClose = {
                                showTodoEditDialog2 = false
                                showTodoEditDialog = true
                            },
                            onConfirm = {

                                // TODO: 错误性检验
                                // 数据库提交一个todo
                                CoroutineScope(Dispatchers.IO).launch {
                                    val todoDao = MyApplication.db.todoDao()
                                    val item = todoList.value[nowPickTodoIndex]
                                    item.todoName = addTodoText
                                    item.total_time = when (totalTimeIndex) {
                                        0 -> 25 * 60
                                        1 -> 35 * 60
                                        2 -> customTimeText.toInt() * 60
                                        else -> 0
                                    }
                                    item.count_type = countTypeList[countTypeIndex]
                                    item.todo_type = todoTypeList[todoTypeIndex]
                                    val result = todoDao.updateTodo(item)
                                    if (result > 0) {
                                        // TODO: 日后研究研究协程
                                        CoroutineScope(Dispatchers.Main).launch {// 这里需不需要去掉
                                            viewModel.updateTodoByIndex(nowPickTodoIndex, item)
                                            showTodoEditDialog2 = false
                                            Toast.makeText(
                                                    context,
                                                    "待办修改成功",
                                                    Toast.LENGTH_SHORT
                                                )
                                        }
                                    }
                                }

                            },
                            title = {
                                Text(
                                    text = "编辑代办",
                                    style = MyTheme.typography.todoListTitle.copy(
                                        color = Color(0xff303133)
                                    )
                                )
                            }
                        ) {
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = MyTheme.elevation.sidePadding)
                            ) {
                                // 待办信息输入栏
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = addTodoText,
                                    label = {
                                        Text(text = "待办名称")
                                    },
                                    placeholder = {
                                        Text(text = "请输入待办名称")
                                    },
                                    onValueChange = {
                                        addTodoText = it
                                    }
                                )
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                // 自定义时间输入
                                if (showCustomTotalTime.value) {
                                    // 待办信息输入栏
                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = customTimeText,
                                        label = {
                                            Text(text = "自定义时间")
                                        },
                                        placeholder = {
                                            Text(text = "请输入您的待办完成时间")
                                        },
                                        onValueChange = { text ->
                                            // 如果是数字就接受
                                            when (text.isNotEmpty() && text.all { c -> c.isDigit() }) {
                                                true -> {
                                                    customTimeText = text
                                                }
                                                false -> {}
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                }
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((index, item) in todoTypeList.withIndex()) {
                                        MyButton(
                                            todoTypeIndex = todoTypeIndex,
                                            selfIndex = index,
                                            text = item
                                        ) {
                                            todoTypeIndex = index
                                        }
                                        Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                    }
                                }

                                // 倒计时类型
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((index, item) in countTypeList.withIndex()) {
                                        MyButton(
                                            todoTypeIndex = countTypeIndex,
                                            selfIndex = index,
                                            text = item
                                        ) {
                                            countTypeIndex = index
                                        }
                                        Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                    }
                                }

                                // 时间限制
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((index, item) in totalTimeList.withIndex()) {
                                        MyButton(
                                            todoTypeIndex = totalTimeIndex,
                                            selfIndex = index,
                                            text = item
                                        ) {
                                            // 回调函数
                                            totalTimeIndex = index
                                        }
                                        Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                    }
                                }

                                // 时间限制
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                            }
                        }
                    }

                    if (showTodoEditDialog) {
                        Dialog(onDismissRequest = {
                            showTodoEditDialog = false
                        }) {
                            Column(
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                    .background(Color.White)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xff409EFF
                                        ).copy(alpha = 0.2f)
                                    ),
                                    onClick = {
                                        showTodoEditDialog2 = true
                                        showTodoEditDialog = false
                                    }
                                ) {
                                    Text("编辑", color = Color(0xff409EFF))
                                }
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xffF56C6C
                                        ).copy(alpha = 0.2f)
                                    ),
                                    onClick = {
                                        confirmTodoDeleteDialog = true
                                        showTodoEditDialog = false
                                    }
                                ) {
                                    Text("删除", color = Color(0xffF56C6C))
                                }
                            }
                        }
                    }

                    if (dialogVisiable) {
                        var addTodoText by remember {
                            mutableStateOf("")
                        }
                        var customTimeText by remember {
                            mutableStateOf("")
                        }

                        var todoTypeIndex by remember {
                            mutableStateOf(-1)
                        }
                        val todoTypeList = mutableListOf<String>("普通番茄钟", "定目标", "养习惯")
                        var countTypeIndex by remember {
                            mutableStateOf(-1)
                        }
                        val countTypeList = mutableListOf<String>("倒计时", "正向计时", "不计时")
                        var totalTimeIndex by remember {
                            mutableStateOf(-1)
                        }
                        val totalTimeList = mutableListOf<String>("25分钟", "35分钟", "自定义")

                        // 显示自定义时间输入框
                        val showCustomTotalTime = remember {
                            derivedStateOf { totalTimeIndex == 2 }
                        }
                        MyDialog(
                            dialogVisible = dialogVisiable,
                            modifier = Modifier
                                .fillMaxWidth(0.75f)   // 设置大小为父元素的75%
                                .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                .background(color = Color.White),
                            onClose = { dialogVisiable = false },
                            onConfirm = {
                                // TODO: 错误性检验
                                // 数据库提交一个todo
                                CoroutineScope(Dispatchers.IO).launch {
                                    val todoDao = MyApplication.db.todoDao()
                                    val item = Todo(
                                        id = 0,     // id填0的时候默认被认为没有设置id，如果填了AutoGenrate的话就能够自动递增
                                        todoName = addTodoText,
                                        total_time = when (totalTimeIndex) {
                                            0 -> 25 * 60
                                            1 -> 35 * 60
                                            2 -> customTimeText.toInt() * 60
                                            else -> 0
                                        },
                                        current_progress = 0,
                                        add_date = System.currentTimeMillis(),
                                        count_type = countTypeList[countTypeIndex],
                                        break_time = 240,
                                        todo_notes = "",
                                        todo_type = todoTypeList[todoTypeIndex],
                                        repeat_time = "",
                                        is_complete = 0,
                                        background_image = GetRandomPicture()
                                    )
                                    val result = todoDao.insertTodo(item)
                                    item.id = result
                                    if (result > 0) {
                                        // TODO: 日后研究研究协程
                                        CoroutineScope(Dispatchers.Main).launch {// 这里需不需要去掉
                                            viewModel.addTodo(item)
                                            dialogVisiable = false
                                        }
                                    }
                                }
                            },
                            title = {
                                Text(
                                    text = "添加待办",
                                    style = MyTheme.typography.todoListTitle.copy(
                                        color = Color(0xff303133)
                                    )
                                )
                            }
                        ) {
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = MyTheme.elevation.sidePadding)
                            ) {
                                // 待办信息输入栏
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = addTodoText,
                                    label = {
                                        Text(text = "待办名称")
                                    },
                                    placeholder = {
                                        Text(text = "请输入待办名称")
                                    },
                                    onValueChange = {
                                        addTodoText = it
                                    }
                                )
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                // 自定义时间输入
                                if (showCustomTotalTime.value) {
                                    // 待办信息输入栏
                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = customTimeText,
                                        label = {
                                            Text(text = "自定义时间")
                                        },
                                        placeholder = {
                                            Text(text = "请输入您的待办完成时间")
                                        },
                                        onValueChange = { text ->
                                            // 如果是数字就接受
                                            when (text.isNotEmpty() && text.all { c -> c.isDigit() }) {
                                                true -> {
                                                    customTimeText = text
                                                }
                                                false -> {}
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                }
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((index, item) in todoTypeList.withIndex()) {
                                        MyButton(
                                            todoTypeIndex = todoTypeIndex,
                                            selfIndex = index,
                                            text = item
                                        ) {
                                            todoTypeIndex = index
                                        }
                                        Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                    }
                                }

                                // 倒计时类型
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((index, item) in countTypeList.withIndex()) {
                                        MyButton(
                                            todoTypeIndex = countTypeIndex,
                                            selfIndex = index,
                                            text = item
                                        ) {
                                            countTypeIndex = index
                                        }
                                        Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                    }
                                }

                                // 时间限制
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for ((index, item) in totalTimeList.withIndex()) {
                                        MyButton(
                                            todoTypeIndex = totalTimeIndex,
                                            selfIndex = index,
                                            text = item
                                        ) {
                                            // 回调函数
                                            totalTimeIndex = index
                                        }
                                        Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                    }
                                }

                                // 时间限制
                                Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                            }
                        }
                    }



                    Column(
                        modifier = Modifier
                            .constrainAs(mainContent) {
                                top.linkTo(parent.top)
                            }
                            .background(color = MyTheme.colors.background[1])
                            .fillMaxSize()
                    ) {

                        // 顶部ActionBar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = MyTheme.elevation.topTitlePadding,
                                    horizontal = MyTheme.elevation.sidePadding
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "待办",
                                style = MyTheme.typography.topTitle
                            )
                            // IconList
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = {
                                        if (!dialogVisiable) {
                                            dialogVisiable = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = {

                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        modifier = Modifier
                                            .size(MyTheme.size.actionBarIcon),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {

                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Menu,
                                        modifier = Modifier
                                            .size(MyTheme.size.actionBarIcon),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // 正文内容
                        Column(
                            modifier = Modifier
                                .clip(
                                    shape = RoundedCornerShape(
                                        topStart = MyTheme.size.roundedCorner,
                                        topEnd = MyTheme.size.roundedCorner
                                    )
                                )
                                .height(0.dp)
                                .weight(1f)
                                .background(color = Color.White)
                                .padding(horizontal = MyTheme.elevation.sidePadding),
                        ) {
                            Spacer(modifier = Modifier.height(40.dp))
                            Text(text = "待办事项", style = MyTheme.typography.title)

                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                            Text(
                                "待办是指您要专注的事，点击卡片\"+\"添加，长按待办编辑删除。",
                                style = MyTheme.typography.regularText
                            )

                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                            // 立刻执行内容淡出动画
                            val state = remember {
                                MutableTransitionState(false).apply {
                                    // Start the animation immediately.
                                    targetState = true
                                }
                            }

                            // 待办列表
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(MyTheme.elevation.contentPadding)
                            ) {
                                // TODO: Drow down 阴影不会搞
                                items(todoList.value.size) { index ->
                                    val item = todoList.value[index]
                                    AnimatedVisibility(
                                        visibleState = state,
                                        enter = fadeIn(animationSpec = tween(durationMillis = 850))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .clip(
                                                    shape = RoundedCornerShape(MyTheme.size.roundedCorner),
                                                )
                                                .paint(
                                                    painter = painterResource(id = item.background_image),
                                                    contentScale = ContentScale.Crop,
                                                    alignment = Alignment.TopCenter
                                                )
                                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(onTap = {
                                                        showTodoEditDialog = true
                                                        nowPickTodo = item
                                                        nowPickTodoIndex = index
                                                    }, onLongPress = {
                                                        showTodoEditDialog = true
                                                        nowPickTodo = item
                                                        nowPickTodoIndex = index
                                                    })
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(0.dp)
                                                    .weight(1f),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = item.todoName,
                                                    style = MyTheme.typography.todoListTitle.copy(
                                                        shadow = MyTheme.shadow.toDoListText
                                                    )
                                                )
                                                Row() {
                                                    when (item.todo_type) {
                                                        "普通番茄钟" -> {
                                                            when (item.count_type) {
                                                                "正向计时" -> {
                                                                    Text(
                                                                        text = "正向计时",
                                                                        style = MyTheme.typography.todoListText.copy(
                                                                            shadow = MyTheme.shadow.toDoListText
                                                                        )
                                                                    )
                                                                    Spacer(
                                                                        modifier = Modifier.width(
                                                                            10.dp
                                                                        )
                                                                    )
                                                                    Text(
                                                                        text = (item.total_time / 60).toString() + "分钟",
                                                                        style = MyTheme.typography.todoListText.copy(
                                                                            shadow = MyTheme.shadow.toDoListText
                                                                        )
                                                                    )
                                                                }
                                                                "倒计时" -> {
                                                                    Text(
                                                                        text = "倒计时",
                                                                        style = MyTheme.typography.todoListText.copy(
                                                                            shadow = MyTheme.shadow.toDoListText
                                                                        )
                                                                    )
                                                                    Spacer(
                                                                        modifier = Modifier.width(
                                                                            10.dp
                                                                        )
                                                                    )
                                                                    Text(
                                                                        text = (item.total_time / 60).toString() + "分钟",
                                                                        style = MyTheme.typography.todoListText.copy(
                                                                            shadow = MyTheme.shadow.toDoListText
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Column(
                                                modifier = Modifier,
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                when (item.is_complete) {
                                                    0 -> {
                                                        TextButton(onClick = {
                                                            // 开始计时捏
                                                            // 打开专注activity并传递参数
                                                            val bundle = Bundle().apply {
                                                                putLong("todoId", item.id)
                                                            }
                                                            val intent = Intent(
                                                                requireContext(),
                                                                ConcentrateScreen::class.java
                                                            )
                                                            intent.putExtras(bundle)
                                                            startActivity(intent)
                                                        }) {
                                                            Text(
                                                                text = "开始",
                                                                style = MyTheme.typography.textButton.copy(
                                                                    shadow = MyTheme.shadow.toDoListText
                                                                )
                                                            )
                                                        }
                                                    }
                                                    1 -> {

                                                        Text(
                                                            text = "已完成",
                                                            style = MyTheme.typography.textButton.copy(
                                                                shadow = MyTheme.shadow.toDoListText
                                                            )
                                                        )
                                                    }
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Divider(thickness = 2.dp, color = MyTheme.colors.navBorder)
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TodoPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TodoPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}