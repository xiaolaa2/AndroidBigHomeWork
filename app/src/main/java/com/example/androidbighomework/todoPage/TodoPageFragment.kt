package com.example.androidbighomework.todoPage

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.androidbighomework.R
import com.example.androidbighomework.Theme.MyDataBaseHelper
import com.example.androidbighomework.Theme.MyTheme
import kotlinx.coroutines.launch

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
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

        db = MyDataBaseHelper(requireContext(), "tomatoTodo", 2).writableDatabase
        initCompose()
    }

    // 获取倒计时时间工具
    private fun CountTimeTool(index: Int): Int {
        return when(index) {
            0 -> 25
            1 -> 35
            2 -> 25
            else -> 0
        }
    }

    @Composable
    private fun MyButton(todoTypeIndex: Int, selfIndex: Int, text: String, CallBack: () -> Unit) {
        // 自定义按钮
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(MyTheme.size.buttonRoundedCorner))
                .background(if (todoTypeIndex == selfIndex) Color(0xffd9ecff) else Color(0xffE6E8EB))
                .padding(
                    horizontal = 7.dp,
                    vertical = 8.dp
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            CallBack()
                        }
                    )
                }
        ) {
            Text(
                text = text,
                style =
                if (todoTypeIndex == selfIndex)
                    MyTheme.typography.regularText.copy(color = Color(0xff409EFF))
                else
                    MyTheme.typography.regularText
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun initCompose() {
        requireView().findViewById<ComposeView>(R.id.compose_view).setContent {
            // 顶部的大背景
            MyTheme {
                ConstraintLayout() {
                    val (button, mainContent) = createRefs()
                    val coroutineScope = rememberCoroutineScope()
                    var dialogVisiable by remember {
                        mutableStateOf(true)
                    }
                    var addTodoText by remember {
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
                    val totalTimeList = mutableListOf<String>("25分钟", "35分钟", "25分钟")

                    // 添加待办弹窗
                    AnimatedVisibility(
                        modifier = Modifier
                            .zIndex(1f),
                        visible = dialogVisiable,
                        enter = fadeIn(animationSpec = tween(durationMillis = 1550)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 1550))
                    ) {
                        Box(modifier = Modifier.zIndex(1f)) {

                            // 遮罩层最大
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = Color.Black.copy(alpha = 0.6f))
                            ) {
                                AnimatedVisibility(
                                    visible = dialogVisiable,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 1550)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 1550))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .width(400.dp)
                                            .height(300.dp)
                                            .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                            .background(color = Color.White)
                                    ) {
                                        // 标题栏和按钮
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .height(60.dp)
                                                .fillMaxWidth()
                                                .background(
                                                    Color(0xffE6E8EB)
                                                )
                                                .padding(horizontal = MyTheme.elevation.sidePadding)
                                        ) {
                                            Text(
                                                text = "添加待办",
                                                style = MyTheme.typography.todoListTitle.copy(
                                                    color = Color(0xff303133)
                                                )
                                            )
                                            Row() {
                                                // 提交按钮
                                                IconButton(onClick = {
                                                    coroutineScope.launch {
                                                        db.execSQL("insert into todoList (name, total_time, current_progress, add_date, count_type, break_time, todo_notes, todo_notes, repeat_time) values(?, ?, ?, ?, ?, ?, ?,?, ?)",
                                                            arrayOf(addTodoText, CountTimeTool(totalTimeIndex), 0, 0, countTypeList[countTypeIndex], 0, "", "")
                                                            )
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Done,
                                                        contentDescription = null
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    dialogVisiable = false
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
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
                                                        totalTimeIndex = index
                                                    }
                                                    Spacer(modifier = Modifier.width(MyTheme.elevation.contentPadding))
                                                }
                                            }
                                        }
                                    }
                                }
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


                            // 待办列表
                            LazyColumn() {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(
                                                shape = RoundedCornerShape(MyTheme.size.roundedCorner),
                                            )
                                            .paint(
                                                painter = painterResource(id = R.drawable.bg2),
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.TopCenter
                                            )
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures(onTap = {
                                                    Log.d("Todo", "Todo was clicked")
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
                                                text = "待办是您要专注的事情",
                                                style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText)
                                            )
                                            Row() {
                                                Text(
                                                    text = "正向计时",
                                                    style = MyTheme.typography.todoListText.copy(
                                                        shadow = MyTheme.shadow.toDoListText
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "1分钟",
                                                    style = MyTheme.typography.todoListText.copy(
                                                        shadow = MyTheme.shadow.toDoListText
                                                    )
                                                )
                                            }
                                        }
                                        Column(
                                            modifier = Modifier,
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            TextButton(onClick = { /*TODO*/ }) {

                                                Text(
                                                    text = "开始",
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
                        Divider(thickness = 2.dp, color = MyTheme.colors.navBorder)
                    }
                }
            }
        }
    }

    @Composable
    private fun MyJumpOutDialog(
        visible: Boolean
    ) {

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