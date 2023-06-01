package com.example.androidbighomework.todoPage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidbighomework.AppActivity
import com.example.androidbighomework.MyApplication
import com.example.androidbighomework.Theme.MyTheme
import com.example.androidbighomework.ViewModel.ConcentrateScreenViewModel
import com.example.androidbighomework.todoPage.Dao.Music
import com.example.androidbighomework.todoPage.Dao.Todo
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class ConcentrateScreen : ComponentActivity() {

    private var todoId: Long = -1
    private lateinit var context: Context
    var mediaPlayer = MediaPlayer()

    //震动马达
    @RequiresApi(Build.VERSION_CODES.S)
    lateinit var vibratorManager: VibratorManager

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todoId = intent.extras?.getLong("todoId") ?: -1
        context = this

        vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

        setContent {
            InitCompose()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        startForegroundService(Intent(applicationContext, CounterService::class.java))
    }

    override fun onResume() {
        super.onResume()
        MyApplication.appStatus.value = AppStatus.Front
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun InitCompose() {
        var isLoadingComplete by remember {
            mutableStateOf(false)
        }
        val viewModel: ConcentrateScreenViewModel = viewModel()
        val todo: Todo by viewModel.todo.collectAsState()
        // 要使用协程来获取数据
        LaunchedEffect(true) {
            CoroutineScope(Dispatchers.IO).launch {
                val todoDao = MyApplication.db.todoDao()
                viewModel.todo.value = todoDao.getTodoById(todoId)
//                todo = todoDao.getTheFirstTodo()
                // 切换到主线程
                CoroutineScope(Dispatchers.Main).launch {
                    isLoadingComplete = true
                }
            }
        }
        // 这里一定要判断是否已经加载完毕，如果没有加载完毕就显示加载页面，加载完毕就显示专注页面
        when (isLoadingComplete) {
            true -> {
                val countState by viewModel.page.collectAsState()
                // 页面切换加动画
                Crossfade(targetState = countState) { page ->
                    when (page) {
                        PageType.CountDown -> {
                            LaunchedEffect(true) {
                                if (MyApplication.todoStatus.value == ConcentrateStatus.NotBegin) {
                                    MyApplication.countDown.value = todo.current_progress
                                }
                                MyApplication.todoStatus.value = ConcentrateStatus.Focusing
                            }
                            CountDown(
                                vibratorManager = vibratorManager,
                                viewModel = viewModel
                            )
                        }
                        PageType.ForwardTiming -> {
                            LaunchedEffect(true) {
                                if (MyApplication.todoStatus.value == ConcentrateStatus.NotBegin) {
                                    MyApplication.countDown.value = todo.current_progress
                                }
                                MyApplication.todoStatus.value = ConcentrateStatus.Focusing
                            }
                            ForwardTiming(
                                vibratorManager = vibratorManager,
                                viewModel = viewModel
                            )
                        }
                        PageType.Break -> {
                            LaunchedEffect(true) {
                                MyApplication.todoStatus.value = ConcentrateStatus.Breaking
                            }
                            BreakTime(
                                vibratorManager = vibratorManager,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
            false -> {
                // 这里显示加载页面
                Text(text = "正在加载当中")
            }
        }
    }

    @SuppressLint("Recycle", "Range", "CoroutineCreationDuringComposition",
        "StateFlowValueCalledInComposition"
    )
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun CountDown(
        vibratorManager: VibratorManager,
        viewModel: ConcentrateScreenViewModel
    ) {
        MyTheme {
            // 当前的背景图片
            val nowBgPic by remember {
                mutableStateOf(GetRandomPicture())
            }
            val todo by viewModel.todo.collectAsState()
            val appStatus = MyApplication.appStatus.collectAsState()
            val cour = rememberCoroutineScope()
            // 是否开始计时、倒计时
            var isCount by remember {
                mutableStateOf(true)
            }
            // 当前已经计时的时间
            val nowCountTime = MyApplication.countDown.collectAsState()
            // 判断是否已经到达时间
            var hadStop by remember {
                mutableStateOf(false)
            }
            //待办名称
            val todoNameText by remember {
                mutableStateOf(todo.todoName)
            }
            val totalTime = todo.total_time
            val vibrator = vibratorManager.defaultVibrator
            var customMusicUri by remember {
                mutableStateOf<Uri?>(null)
            }
            val musicList = remember {
                mutableStateListOf<Music?>(null)
            }
            val nowPickMusicIndex by viewModel.nowPickMusicIndex.collectAsState()
            val nowPlayingMusicIndex by viewModel.nowPlayingMusicIndex.collectAsState()
            val mediaPlayerStatus by viewModel.mediaPlayerStatus.collectAsState()
            var isLoop by remember {
                mutableStateOf(mediaPlayer.isLooping)
            }

            // 打开暂停对话框
            var dialogVisible by remember {
                mutableStateOf(false)
            }
            // 打开暂停对话框
            var stopDialogVisible by remember {
                mutableStateOf(false)
            }
            // 打开音乐选择
            var showMusicChooseDialog by remember {
                mutableStateOf(false)
            }

            // 获取背景音乐相关信息
            LaunchedEffect(true) {
                var result: Array<Music> = arrayOf()
                withContext(Dispatchers.IO) {
                    val musicDao = MyApplication.db.MusicDao()
                    result = musicDao.getAllMusic()
                }
                withContext(Dispatchers.Main) {
                    if (result.isNotEmpty()) {
                        musicList.addAll(result)
                    }
                }
            }

            // 注册文件选择器
            val musicPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                    if (it != null) {
                        customMusicUri = it
                        // 解析一下文件的名称
                        val cursor = contentResolver.query(
                            it,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val name =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            cursor.close()

                            // 把文件先存储到本地
                            cour.launch {
                                var result: Long = -1
                                var music: Music? = null
                                withContext(Dispatchers.IO) {
                                    val folder = File(filesDir, "audio")
                                    if (!folder.exists()) {
                                        folder.mkdirs()
                                    }
                                    val file = File(folder, name)
                                    val output = FileOutputStream(file)
                                    contentResolver.openInputStream(it)?.copyTo(output)

                                    if (file.exists()) {
                                        // 保存到sqlite
                                        val musicDao = MyApplication.db.MusicDao()
                                        music = Music(
                                            id = 0,
                                            name = name,
                                            uri = file.absolutePath,
                                            is_custom = 1
                                        )
                                        result = musicDao.addMusic(music!!)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    if (result > 0 && music != null) {
                                        musicList.add(music)
                                    }
                                }
                            }
                        }
                    }
                }

            // 计时器
            if (isCount && appStatus.value == AppStatus.Front) {
                LaunchedEffect(isCount) {
                    repeat(totalTime) {
                        MyApplication.countDown.value++
                        // 修改todo内容的值
                        viewModel.todo.value.current_progress = nowCountTime.value
                        delay(1000)   // 延迟一秒
                    }
                }
            } else if (!isCount && appStatus.value == AppStatus.Front) {
//                    Toast.makeText(this, "计时停止", Toast.LENGTH_SHORT).show()
                LaunchedEffect(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val todoDao = MyApplication.db.todoDao()
                        todoDao.updateTodo(todo)
                    }
                }
            }

            // 时间到了
            if (nowCountTime.value == totalTime && !hadStop) {
                isCount = false
                Toast.makeText(this, "时间到啦", Toast.LENGTH_SHORT).show()
                hadStop = true
                // 如果时间已经满了就标记当前的todo为已完成
                viewModel.todo.value.is_complete = 1
                // 响1秒，停0.5秒，重复播放震动
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 0, 500, 0, 1000, 0, 500),
                        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
                        -1
                    )
                )
                // 更新一下数据库里的todo
                LaunchedEffect(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val todoDao = MyApplication.db.todoDao()
                        todoDao.updateTodo(todo)
                    }
                }
                viewModel.changePage(PageType.Break)
            }

            // 打开音乐选择器
            if (showMusicChooseDialog) {
                Dialog(onDismissRequest = { showMusicChooseDialog = false }) {
                    Row(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                            .heightIn(min = 0.dp, 400.dp)
                            .background(MyTheme.colors.background[0])
                    ) {
                        // 左侧音乐列表显示
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(MyTheme.elevation.contentPadding),
                            modifier = Modifier
                                .widthIn(min = 0.dp, max = 210.dp)
                                .padding(MyTheme.elevation.sidePadding)
                        ) {
                            items(musicList.size) { index ->
                                val item = musicList[index]
                                LazyRow {
                                    item {
                                        if (item != null) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                                    .background(
                                                        if (nowPickMusicIndex == index) Color.Gray.copy(
                                                            alpha = 0.2f
                                                        ) else Color.Unspecified
                                                    )
                                            ) {
                                                TextButton(onClick = {
                                                    viewModel.nowPickMusicIndex.value = index
                                                }) {
                                                    Text(
                                                        text = item.name,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                TextButton(onClick = {
                                    musicPicker.launch(arrayOf("audio/*"))
                                }) {
                                    Text(text = "自定义", color = Color(0xffF56C6C))
                                }
                            }
                        }
                        // 右侧音乐控制
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(MyTheme.colors.divider)
                                .padding(MyTheme.elevation.sidePadding),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 播放按钮
                            Button(
                                onClick = {
                                    when (mediaPlayerStatus) {
                                        MediaPlayerStatus.NotStart -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Pausing -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    if (nowPlayingMusicIndex != nowPickMusicIndex) {
                                                        mediaPlayer.reset()
                                                        mediaPlayer.release()
                                                        mediaPlayer = MediaPlayer()
                                                        mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                        mediaPlayer.prepare()
                                                    }
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Wait -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Playing -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    // 如果正在播放要释放资源重新创建一个MediaPlayer
                                                    mediaPlayer.reset()
                                                    mediaPlayer.release()
                                                    mediaPlayer = MediaPlayer()
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                    }
                                }, modifier = Modifier,
                                enabled =
                                mediaPlayerStatus == MediaPlayerStatus.NotStart ||
                                        mediaPlayerStatus == MediaPlayerStatus.Pausing ||
                                        nowPickMusicIndex != nowPlayingMusicIndex
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                            Button(
                                onClick = {
                                    when (mediaPlayerStatus) {
                                        MediaPlayerStatus.NotStart -> {}
                                        MediaPlayerStatus.Pausing -> {}
                                        MediaPlayerStatus.Wait -> {}
                                        MediaPlayerStatus.Playing -> {
                                            mediaPlayer.pause()
                                            viewModel.mediaPlayerStatus.value =
                                                MediaPlayerStatus.Pausing
                                        }
                                    }
                                },
                                modifier = Modifier,
                                enabled = mediaPlayerStatus == MediaPlayerStatus.Playing,
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Pause,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                            Button(
                                onClick = {
                                    mediaPlayer.isLooping = !mediaPlayer.isLooping
                                    isLoop = mediaPlayer.isLooping
                                },
                                modifier = Modifier,
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Loop,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            var resetTime by remember {
                mutableStateOf(false)
            }

            // 重置进度弹窗
            if (resetTime) {
                AlertDialog(
                    onDismissRequest = {
                        resetTime = false
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            cour.launch {
                                withContext(Dispatchers.IO) {
                                    todo.current_progress = 0
                                    MyApplication.db.todoDao().updateTodo(todo)
                                }
                                withContext(Dispatchers.Main) {
                                    MyApplication.countDown.value = 0
                                    resetTime = false
                                }
                            }
                        }) {
                            Text("确认", color = Color(0xff409EFF))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            resetTime = false
                        }) {
                            Text("取消", color = Color(0xffF56C6C))
                        }
                    },
                    title = { Text(text = "时间重置", color = Color(0xffF56C6C)) },
                    text = { Text(text = "确认重置当前待办的进度?") },
                    modifier = Modifier.clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                )
            }

            // 停止弹窗
            MyDialog(
                dialogVisible = stopDialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.80f)   // 设置大小为父元素的75%
                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                    .background(color = Color.White),
                onClose = {
                    stopDialogVisible = false
                },
                title = {
                    Text(
                        text = "请选择你的操作",
                        style = MyTheme.typography.todoListTitle.copy(
                            color = Color(0xff303133)
                        )
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(context, AppActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "放弃当前计时",
                                style = MyTheme.typography.regularText.copy(color = Color.Red)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cour.launch {
                                        withContext(Dispatchers.IO) {
                                            todo.is_complete = 1
                                            // 提前完成计时
                                            MyApplication.db
                                                .todoDao()
                                                .updateTodo(todo)
                                        }
                                        withContext(Dispatchers.Main) {
                                            val intent = Intent(context, AppActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("提前完成计时", style = MyTheme.typography.regularText)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    stopDialogVisible = false
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("取消", style = MyTheme.typography.regularText)
                        }
                    }
                }
            )

            // 暂停弹框
            MyDialog(
                dialogVisible = dialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                    .background(color = Color.White),
                onClose = {
                    dialogVisible = false
                    isCount = true
                },
                title = {
                    Text(
                        text = "暂停",
                        style = MyTheme.typography.todoListTitle.copy(
                            color = Color(0xff303133)
                        )
                    )
                },
                content = {

                }
            )

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = painterResource(id = nowBgPic),
                        contentScale = ContentScale.Crop,
                    )
            ) {
                val (
                    musicLabel,
                    progressBar,
                    todoStatusText,
                    todoName,
                    countTimeText,
                    topActionBar,
                    controlBar) = createRefs()


                val current_progress = remember(nowCountTime.value) {
                    derivedStateOf { (totalTime - nowCountTime.value) / totalTime.toFloat() }
                }
                val initialProgressBarSize = with(LocalDensity.current) {
                    200.dp.toPx()
                }

                // 进度条的动画显示
                val transition = rememberInfiniteTransition()
                val progressSize = transition.animateFloat(
                    initialValue = initialProgressBarSize,
                    targetValue = initialProgressBarSize + 60f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha = transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha2 = transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val strokeSize = with(LocalDensity.current) {
                    4.dp.toPx()
                }

                // 音乐名称显示

                AnimatedVisibility(
                    modifier = Modifier
                        .constrainAs(musicLabel) {
                            centerHorizontallyTo(parent)
                            top.linkTo(topActionBar.bottom, margin = 10.dp)
                        }
                        .zIndex(2f),
                    visible = mediaPlayerStatus == MediaPlayerStatus.Playing,
                    enter = fadeIn()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .constrainAs(musicLabel) {
                                top.linkTo(topActionBar.bottom, margin = 10.dp)
                                centerHorizontallyTo(parent)
                            }
                            .zIndex(2f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White
                        )
                        musicList[nowPlayingMusicIndex]?.let {
                            Text(
                                text = it.name,
                                style = MyTheme.typography.todoListTitle.copy(
                                    shadow = MyTheme.shadow.toDoListText,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }


                // 加一点遮罩背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // 外部的倒计时进度条
                Canvas(modifier = Modifier.constrainAs(progressBar) {
                    centerTo(parent)
                }) {
                    // 底部圈
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(width = progressSize.value, height = progressSize.value),
                        topLeft = Offset(
                            x = -(progressSize.value / 2),
                            y = -(progressSize.value / 2)
                        ),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha.value
                    )
                    // 白色色进度条
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(
                            width = initialProgressBarSize,
                            height = initialProgressBarSize
                        ),
                        topLeft = Offset(
                            x = -(initialProgressBarSize / 2),
                            y = -(initialProgressBarSize / 2)
                        ),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha2.value
                    )
                    // 白色色进度条
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = current_progress.value * 360f,
                        useCenter = false,
                        size = Size(
                            width = initialProgressBarSize,
                            height = initialProgressBarSize
                        ),
                        topLeft = Offset(
                            x = -(initialProgressBarSize / 2),
                            y = -(initialProgressBarSize / 2)
                        ),
                        style = Stroke(width = strokeSize),
                    )
                }

                // 顶部的ActionBar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MyTheme.elevation.sidePadding)
                        .constrainAs(topActionBar) {
                            top.linkTo(parent.top, margin = 10.dp)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        val intent = Intent(context, AppActivity::class.java)
                        startActivity(intent)
                        finish()
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Close,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "关闭专注页返回待办页面"
                        )
                    }
                    Row() {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Edit,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Share,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Menu,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                    }
                }
                // 显示当前时长
                Text(
                    text = getCurrentTime(totalTime - nowCountTime.value),
                    modifier = Modifier
                        .constrainAs(countTimeText) {
                            centerTo(parent)
                        },
                    style = MyTheme.typography.todoListTitle.copy(
                        fontSize = 36.sp,
                        shadow = MyTheme.shadow.toDoListText
                    )
                )

                val todoNameTextMargin = with(LocalDensity.current) {
                    initialProgressBarSize.toDp() / 2 + 30.dp
                }

                // todoNameText
                Text(
                    text = todoNameText,
                    modifier = Modifier.constrainAs(todoName) {
                        top.linkTo(progressBar.bottom, margin = todoNameTextMargin)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText)
                )

                // todoStatusText
                Text(
                    text = if (isCount) "进行中" else "已暂停",
                    modifier = Modifier.constrainAs(todoStatusText) {
                        top.linkTo(todoName.bottom, margin = 8.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListText.copy(
                        shadow = MyTheme.shadow.toDoListText,
                    )
                )

                // 控制栏
                Row(
                    modifier = Modifier
                        .constrainAs(controlBar) {
                            // 在底部
                            bottom.linkTo(parent.bottom, margin = 70.dp)
                            // 水平居中
                            centerHorizontallyTo(parent, 0.5f)
                        },
                    horizontalArrangement = Arrangement.spacedBy(15.dp)  // 图标的间隔
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Brightness5,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {
                        showMusicChooseDialog = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.LibraryMusic,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "切换背景音乐"
                        )
                    }
                    IconButton(onClick = {
                        isCount = false
                        dialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Pause,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "暂停计时"
                        )
                    }
                    IconButton(onClick = {
                        resetTime = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Refresh,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "重新计时"
                        )
                    }
                    IconButton(onClick = {
                        stopDialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Stop,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "停止计时"
                        )
                    }
                }

            }
        }
    }

    @SuppressLint("Recycle", "Range", "CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun BreakTime(
        vibratorManager: VibratorManager,
        viewModel: ConcentrateScreenViewModel,
    ) {
        MyTheme {
            // 当前的背景图片
            val nowBgPic by remember {
                mutableStateOf(GetRandomPicture())
            }
            val todo by viewModel.todo.collectAsState()
            val appStatus = MyApplication.appStatus.collectAsState()
            val cour = rememberCoroutineScope()
            // 是否开始计时、倒计时
            var isCount by remember {
                mutableStateOf(true)
            }
            // 当前已经计时的时间
            val nowCountTime = MyApplication.BreakcountDown.collectAsState()
            // 判断是否已经到达时间
            var hadStop by remember {
                mutableStateOf(false)
            }
            //待办名称
            val todoNameText by remember {
                mutableStateOf(todo.todoName)
            }
            val totalTime = todo.break_time
            val vibrator = vibratorManager.defaultVibrator
            var customMusicUri by remember {
                mutableStateOf<Uri?>(null)
            }
            val musicList = remember {
                mutableStateListOf<Music?>(null)
            }
            val nowPickMusicIndex by viewModel.nowPickMusicIndex.collectAsState()
            val nowPlayingMusicIndex by viewModel.nowPlayingMusicIndex.collectAsState()
            val mediaPlayerStatus by viewModel.mediaPlayerStatus.collectAsState()
            var isLoop by remember {
                mutableStateOf(mediaPlayer.isLooping)
            }

            // 打开暂停对话框
            var dialogVisible by remember {
                mutableStateOf(false)
            }
            // 打开暂停对话框
            var stopDialogVisible by remember {
                mutableStateOf(false)
            }
            // 打开音乐选择
            var showMusicChooseDialog by remember {
                mutableStateOf(false)
            }

            // 获取背景音乐相关信息
            LaunchedEffect(true) {
                var result: Array<Music> = arrayOf()
                withContext(Dispatchers.IO) {
                    val musicDao = MyApplication.db.MusicDao()
                    result = musicDao.getAllMusic()
                }
                withContext(Dispatchers.Main) {
                    if (result.isNotEmpty()) {
                        musicList.addAll(result)
                    }
                }
            }

            // 注册文件选择器
            val musicPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                    if (it != null) {
                        customMusicUri = it
                        // 解析一下文件的名称
                        val cursor = contentResolver.query(
                            it,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val name =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            cursor.close()

                            // 把文件先存储到本地
                            cour.launch {
                                var result: Long = -1
                                var music: Music? = null
                                withContext(Dispatchers.IO) {
                                    val folder = File(filesDir, "audio")
                                    if (!folder.exists()) {
                                        folder.mkdirs()
                                    }
                                    val file = File(folder, name)
                                    val output = FileOutputStream(file)
                                    contentResolver.openInputStream(it)?.copyTo(output)

                                    if (file.exists()) {
                                        // 保存到sqlite
                                        val musicDao = MyApplication.db.MusicDao()
                                        music = Music(
                                            id = 0,
                                            name = name,
                                            uri = file.absolutePath,
                                            is_custom = 1
                                        )
                                        result = musicDao.addMusic(music!!)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    if (result > 0 && music != null) {
                                        musicList.add(music)
                                    }
                                }
                            }
                        }
                    }
                }

            // 计时器
            if (isCount && appStatus.value == AppStatus.Front) {
                LaunchedEffect(isCount) {
                    repeat(totalTime) {
                        MyApplication.BreakcountDown.value++
                        delay(1000)   // 延迟一秒
                    }
                }
            } else if (!isCount && appStatus.value == AppStatus.Front) {
                LaunchedEffect(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val todoDao = MyApplication.db.todoDao()
                        todoDao.updateTodo(todo)
                    }
                }
            }

            // 时间到了
            if (nowCountTime.value == totalTime && !hadStop) {
                isCount = false
                Toast.makeText(this, "时间到啦", Toast.LENGTH_SHORT).show()
                hadStop = true
                // 响1秒，停0.5秒，重复播放震动
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 0, 500, 0, 1000, 0, 500),
                        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
                        -1
                    )
                )
            }

            // 打开音乐选择器
            if (showMusicChooseDialog) {
                Dialog(onDismissRequest = { showMusicChooseDialog = false }) {
                    Row(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                            .heightIn(min = 0.dp, 400.dp)
                            .background(MyTheme.colors.background[0])
                    ) {
                        // 左侧音乐列表显示
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(MyTheme.elevation.contentPadding),
                            modifier = Modifier
                                .widthIn(min = 0.dp, max = 210.dp)
                                .padding(MyTheme.elevation.sidePadding)
                        ) {
                            items(musicList.size) { index ->
                                val item = musicList[index]
                                LazyRow {
                                    item {
                                        if (item != null) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                                    .background(
                                                        if (nowPickMusicIndex == index) Color.Gray.copy(
                                                            alpha = 0.2f
                                                        ) else Color.Unspecified
                                                    )
                                            ) {
                                                TextButton(onClick = {
                                                    viewModel.nowPickMusicIndex.value = index
                                                }) {
                                                    Text(
                                                        text = item.name,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                TextButton(onClick = {
                                    musicPicker.launch(arrayOf("audio/*"))
                                }) {
                                    Text(text = "自定义", color = Color(0xffF56C6C))
                                }
                            }
                        }
                        // 右侧音乐控制
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(MyTheme.colors.divider)
                                .padding(MyTheme.elevation.sidePadding),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 播放按钮
                            Button(
                                onClick = {
                                    when (mediaPlayerStatus) {
                                        MediaPlayerStatus.NotStart -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Pausing -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    if (nowPlayingMusicIndex != nowPickMusicIndex) {
                                                        mediaPlayer.reset()
                                                        mediaPlayer.release()
                                                        mediaPlayer = MediaPlayer()
                                                        mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                        mediaPlayer.prepare()
                                                    }
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Wait -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Playing -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    // 如果正在播放要释放资源重新创建一个MediaPlayer
                                                    mediaPlayer.reset()
                                                    mediaPlayer.release()
                                                    mediaPlayer = MediaPlayer()
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                    }
                                }, modifier = Modifier,
                                enabled =
                                mediaPlayerStatus == MediaPlayerStatus.NotStart ||
                                        mediaPlayerStatus == MediaPlayerStatus.Pausing ||
                                        nowPickMusicIndex != nowPlayingMusicIndex
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                            Button(
                                onClick = {
                                    when (mediaPlayerStatus) {
                                        MediaPlayerStatus.NotStart -> {}
                                        MediaPlayerStatus.Pausing -> {}
                                        MediaPlayerStatus.Wait -> {}
                                        MediaPlayerStatus.Playing -> {
                                            mediaPlayer.pause()
                                            viewModel.mediaPlayerStatus.value =
                                                MediaPlayerStatus.Pausing
                                        }
                                    }
                                },
                                modifier = Modifier,
                                enabled = mediaPlayerStatus == MediaPlayerStatus.Playing,
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Pause,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                            Button(
                                onClick = {
                                    mediaPlayer.isLooping = !mediaPlayer.isLooping
                                    isLoop = mediaPlayer.isLooping
                                },
                                modifier = Modifier,
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Loop,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 停止弹窗
            MyDialog(
                dialogVisible = stopDialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.80f)   // 设置大小为父元素的75%
                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                    .background(color = Color.White),
                onClose = {
                    stopDialogVisible = false
                },
                title = {
                    Text(
                        text = "请选择你的操作",
                        style = MyTheme.typography.todoListTitle.copy(
                            color = Color(0xff303133)
                        )
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(context, AppActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "放弃当前计时",
                                style = MyTheme.typography.regularText.copy(color = Color.Red)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cour.launch {
                                        withContext(Dispatchers.IO) {
                                            todo.is_complete = 1
                                            // 提前完成计时
                                            MyApplication.db
                                                .todoDao()
                                                .updateTodo(todo)
                                        }
                                        withContext(Dispatchers.Main) {
                                            val intent = Intent(context, AppActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("提前完成计时", style = MyTheme.typography.regularText)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    stopDialogVisible = false
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("取消", style = MyTheme.typography.regularText)
                        }
                    }
                }
            )

            // 暂停弹框
            MyDialog(
                dialogVisible = dialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                    .background(color = Color.White),
                onClose = {
                    dialogVisible = false
                    isCount = true
                },
                title = {
                    Text(
                        text = "暂停",
                        style = MyTheme.typography.todoListTitle.copy(
                            color = Color(0xff303133)
                        )
                    )
                },
                content = {

                }
            )

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = painterResource(id = nowBgPic),
                        contentScale = ContentScale.Crop,
                    )
            ) {
                val (
                    musicLabel,
                    progressBar,
                    todoStatusText,
                    todoName,
                    countTimeText,
                    topActionBar,
                    controlBar) = createRefs()


                val current_progress = remember(nowCountTime.value) {
                    derivedStateOf { (totalTime - nowCountTime.value) / totalTime.toFloat() }
                }
                val initialProgressBarSize = with(LocalDensity.current) {
                    200.dp.toPx()
                }

                // 进度条的动画显示
                val transition = rememberInfiniteTransition()
                val progressSize = transition.animateFloat(
                    initialValue = initialProgressBarSize,
                    targetValue = initialProgressBarSize + 60f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha = transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha2 = transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val strokeSize = with(LocalDensity.current) {
                    4.dp.toPx()
                }

                // 音乐名称显示

                AnimatedVisibility(
                    modifier = Modifier
                        .constrainAs(musicLabel) {
                            centerHorizontallyTo(parent)
                            top.linkTo(topActionBar.bottom, margin = 10.dp)
                        }
                        .zIndex(2f),
                    visible = mediaPlayerStatus == MediaPlayerStatus.Playing,
                    enter = fadeIn()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .constrainAs(musicLabel) {
                                top.linkTo(topActionBar.bottom, margin = 10.dp)
                                centerHorizontallyTo(parent)
                            }
                            .zIndex(2f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White
                        )
                        musicList[nowPlayingMusicIndex]?.let {
                            Text(
                                text = it.name,
                                style = MyTheme.typography.todoListTitle.copy(
                                    shadow = MyTheme.shadow.toDoListText,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }


                // 加一点遮罩背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // 外部的倒计时进度条
                Canvas(modifier = Modifier.constrainAs(progressBar) {
                    centerTo(parent)
                }) {
                    // 底部圈
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(width = progressSize.value, height = progressSize.value),
                        topLeft = Offset(
                            x = -(progressSize.value / 2),
                            y = -(progressSize.value / 2)
                        ),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha.value
                    )
                    // 白色色进度条
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(
                            width = initialProgressBarSize,
                            height = initialProgressBarSize
                        ),
                        topLeft = Offset(
                            x = -(initialProgressBarSize / 2),
                            y = -(initialProgressBarSize / 2)
                        ),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha2.value
                    )
                    // 白色色进度条
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = current_progress.value * 360f,
                        useCenter = false,
                        size = Size(
                            width = initialProgressBarSize,
                            height = initialProgressBarSize
                        ),
                        topLeft = Offset(
                            x = -(initialProgressBarSize / 2),
                            y = -(initialProgressBarSize / 2)
                        ),
                        style = Stroke(width = strokeSize),
                    )
                }

                // 顶部的ActionBar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MyTheme.elevation.sidePadding)
                        .constrainAs(topActionBar) {
                            top.linkTo(parent.top, margin = 10.dp)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        val intent = Intent(context, AppActivity::class.java)
                        startActivity(intent)
                        finish()
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Close,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "关闭专注页返回待办页面"
                        )
                    }
                    Row() {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Edit,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Share,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Menu,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                    }
                }
                // 显示当前时长
                Text(
                    text = getCurrentTime(totalTime - nowCountTime.value),
                    modifier = Modifier
                        .constrainAs(countTimeText) {
                            centerTo(parent)
                        },
                    style = MyTheme.typography.todoListTitle.copy(
                        fontSize = 36.sp,
                        shadow = MyTheme.shadow.toDoListText
                    )
                )

                val todoNameTextMargin = with(LocalDensity.current) {
                    initialProgressBarSize.toDp() / 2 + 30.dp
                }

                // todoNameText
                Text(
                    text = todoNameText,
                    modifier = Modifier.constrainAs(todoName) {
                        top.linkTo(progressBar.bottom, margin = todoNameTextMargin)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText)
                )

                // todoStatusText
                Text(
                    text = "休息中",
                    modifier = Modifier.constrainAs(todoStatusText) {
                        top.linkTo(todoName.bottom, margin = 8.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListText.copy(
                        shadow = MyTheme.shadow.toDoListText,
                    )
                )

                // 控制栏
                Row(
                    modifier = Modifier
                        .constrainAs(controlBar) {
                            // 在底部
                            bottom.linkTo(parent.bottom, margin = 70.dp)
                            // 水平居中
                            centerHorizontallyTo(parent, 0.5f)
                        },
                    horizontalArrangement = Arrangement.spacedBy(15.dp)  // 图标的间隔
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Brightness5,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {
                        showMusicChooseDialog = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.LibraryMusic,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "切换背景音乐"
                        )
                    }
                    IconButton(onClick = {
                        isCount = false
                        dialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Pause,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "暂停计时"
                        )
                    }
                    IconButton(onClick = {
                        stopDialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Stop,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "停止计时"
                        )
                    }
                }

            }
        }
    }

        @SuppressLint("Recycle", "Range", "CoroutineCreationDuringComposition",
        "StateFlowValueCalledInComposition"
    )
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    private fun ForwardTiming(
        vibratorManager: VibratorManager,
        viewModel: ConcentrateScreenViewModel
    ) {
        MyTheme {
            // 当前的背景图片
            val nowBgPic by remember {
                mutableStateOf(GetRandomPicture())
            }
            val todo by viewModel.todo.collectAsState()
            val appStatus = MyApplication.appStatus.collectAsState()
            val cour = rememberCoroutineScope()
            // 是否开始计时、倒计时
            var isCount by remember {
                mutableStateOf(true)
            }
            // 当前已经计时的时间
            val nowCountTime = MyApplication.countDown.collectAsState()
            // 判断是否已经到达时间
            var hadStop by remember {
                mutableStateOf(false)
            }
            //待办名称
            val todoNameText by remember {
                mutableStateOf(todo.todoName)
            }
            val totalTime = todo.total_time
            val vibrator = vibratorManager.defaultVibrator
            var customMusicUri by remember {
                mutableStateOf<Uri?>(null)
            }
            val musicList = remember {
                mutableStateListOf<Music?>(null)
            }
            val nowPickMusicIndex by viewModel.nowPickMusicIndex.collectAsState()
            val nowPlayingMusicIndex by viewModel.nowPlayingMusicIndex.collectAsState()
            val mediaPlayerStatus by viewModel.mediaPlayerStatus.collectAsState()
            var isLoop by remember {
                mutableStateOf(mediaPlayer.isLooping)
            }

            // 打开暂停对话框
            var dialogVisible by remember {
                mutableStateOf(false)
            }
            // 打开暂停对话框
            var stopDialogVisible by remember {
                mutableStateOf(false)
            }
            // 打开音乐选择
            var showMusicChooseDialog by remember {
                mutableStateOf(false)
            }

            // 获取背景音乐相关信息
            LaunchedEffect(true) {
                var result: Array<Music> = arrayOf()
                withContext(Dispatchers.IO) {
                    val musicDao = MyApplication.db.MusicDao()
                    result = musicDao.getAllMusic()
                }
                withContext(Dispatchers.Main) {
                    if (result.isNotEmpty()) {
                        musicList.addAll(result)
                    }
                }
            }

            // 注册文件选择器
            val musicPicker =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                    if (it != null) {
                        customMusicUri = it
                        // 解析一下文件的名称
                        val cursor = contentResolver.query(
                            it,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val name =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            cursor.close()

                            // 把文件先存储到本地
                            cour.launch {
                                var result: Long = -1
                                var music: Music? = null
                                withContext(Dispatchers.IO) {
                                    val folder = File(filesDir, "audio")
                                    if (!folder.exists()) {
                                        folder.mkdirs()
                                    }
                                    val file = File(folder, name)
                                    val output = FileOutputStream(file)
                                    contentResolver.openInputStream(it)?.copyTo(output)

                                    if (file.exists()) {
                                        // 保存到sqlite
                                        val musicDao = MyApplication.db.MusicDao()
                                        music = Music(
                                            id = 0,
                                            name = name,
                                            uri = file.absolutePath,
                                            is_custom = 1
                                        )
                                        result = musicDao.addMusic(music!!)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    if (result > 0 && music != null) {
                                        musicList.add(music)
                                    }
                                }
                            }
                        }
                    }
                }

            // 计时器
            if (isCount && appStatus.value == AppStatus.Front) {
                LaunchedEffect(isCount) {
                    repeat(totalTime) {
                        MyApplication.countDown.value++
                        // 修改todo内容的值
                        viewModel.todo.value.current_progress = nowCountTime.value
                        delay(1000)   // 延迟一秒
                    }
                }
            } else if (!isCount && appStatus.value == AppStatus.Front) {
//                    Toast.makeText(this, "计时停止", Toast.LENGTH_SHORT).show()
                LaunchedEffect(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val todoDao = MyApplication.db.todoDao()
                        todoDao.updateTodo(todo)
                    }
                }
            }

            // 时间到了
            if (nowCountTime.value == totalTime && !hadStop) {
                isCount = false
                Toast.makeText(this, "时间到啦", Toast.LENGTH_SHORT).show()
                hadStop = true
                // 如果时间已经满了就标记当前的todo为已完成
                viewModel.todo.value.is_complete = 1
                // 响1秒，停0.5秒，重复播放震动
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 0, 500, 0, 1000, 0, 500),
                        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
                        -1
                    )
                )
                // 更新一下数据库里的todo
                LaunchedEffect(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val todoDao = MyApplication.db.todoDao()
                        todoDao.updateTodo(todo)
                    }
                }
                viewModel.changePage(PageType.Break)
            }

            // 打开音乐选择器
            if (showMusicChooseDialog) {
                Dialog(onDismissRequest = { showMusicChooseDialog = false }) {
                    Row(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                            .heightIn(min = 0.dp, 400.dp)
                            .background(MyTheme.colors.background[0])
                    ) {
                        // 左侧音乐列表显示
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(MyTheme.elevation.contentPadding),
                            modifier = Modifier
                                .widthIn(min = 0.dp, max = 210.dp)
                                .padding(MyTheme.elevation.sidePadding)
                        ) {
                            items(musicList.size) { index ->
                                val item = musicList[index]
                                LazyRow {
                                    item {
                                        if (item != null) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                                                    .background(
                                                        if (nowPickMusicIndex == index) Color.Gray.copy(
                                                            alpha = 0.2f
                                                        ) else Color.Unspecified
                                                    )
                                            ) {
                                                TextButton(onClick = {
                                                    viewModel.nowPickMusicIndex.value = index
                                                }) {
                                                    Text(
                                                        text = item.name,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                TextButton(onClick = {
                                    musicPicker.launch(arrayOf("audio/*"))
                                }) {
                                    Text(text = "自定义", color = Color(0xffF56C6C))
                                }
                            }
                        }
                        // 右侧音乐控制
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(MyTheme.colors.divider)
                                .padding(MyTheme.elevation.sidePadding),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 播放按钮
                            Button(
                                onClick = {
                                    when (mediaPlayerStatus) {
                                        MediaPlayerStatus.NotStart -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Pausing -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    if (nowPlayingMusicIndex != nowPickMusicIndex) {
                                                        mediaPlayer.reset()
                                                        mediaPlayer.release()
                                                        mediaPlayer = MediaPlayer()
                                                        mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                        mediaPlayer.prepare()
                                                    }
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Wait -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                        MediaPlayerStatus.Playing -> {
                                            cour.launch {
                                                withContext(Dispatchers.IO) {
                                                    // 如果正在播放要释放资源重新创建一个MediaPlayer
                                                    mediaPlayer.reset()
                                                    mediaPlayer.release()
                                                    mediaPlayer = MediaPlayer()
                                                    mediaPlayer.setDataSource(musicList[nowPickMusicIndex]!!.uri)
                                                    mediaPlayer.prepare()
                                                }
                                                withContext(Dispatchers.Main) {
                                                    mediaPlayer.start()
                                                    viewModel.mediaPlayerStatus.value =
                                                        MediaPlayerStatus.Playing
                                                    viewModel.nowPlayingMusicIndex.value =
                                                        nowPickMusicIndex
                                                }
                                            }
                                        }
                                    }
                                }, modifier = Modifier,
                                enabled =
                                mediaPlayerStatus == MediaPlayerStatus.NotStart ||
                                        mediaPlayerStatus == MediaPlayerStatus.Pausing ||
                                        nowPickMusicIndex != nowPlayingMusicIndex
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                            Button(
                                onClick = {
                                    when (mediaPlayerStatus) {
                                        MediaPlayerStatus.NotStart -> {}
                                        MediaPlayerStatus.Pausing -> {}
                                        MediaPlayerStatus.Wait -> {}
                                        MediaPlayerStatus.Playing -> {
                                            mediaPlayer.pause()
                                            viewModel.mediaPlayerStatus.value =
                                                MediaPlayerStatus.Pausing
                                        }
                                    }
                                },
                                modifier = Modifier,
                                enabled = mediaPlayerStatus == MediaPlayerStatus.Playing,
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Pause,
                                        contentDescription = null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(MyTheme.elevation.contentPadding))
                            Button(
                                onClick = {
                                    mediaPlayer.isLooping = !mediaPlayer.isLooping
                                    isLoop = mediaPlayer.isLooping
                                },
                                modifier = Modifier,
                            ) {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Loop,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            var resetTime by remember {
                mutableStateOf(false)
            }

            // 重置进度弹窗
            if (resetTime) {
                AlertDialog(
                    onDismissRequest = {
                        resetTime = false
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            cour.launch {
                                withContext(Dispatchers.IO) {
                                    todo.current_progress = 0
                                    MyApplication.db.todoDao().updateTodo(todo)
                                }
                                withContext(Dispatchers.Main) {
                                    MyApplication.countDown.value = 0
                                    resetTime = false
                                }
                            }
                        }) {
                            Text("确认", color = Color(0xff409EFF))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            resetTime = false
                        }) {
                            Text("取消", color = Color(0xffF56C6C))
                        }
                    },
                    title = { Text(text = "时间重置", color = Color(0xffF56C6C)) },
                    text = { Text(text = "确认重置当前待办的进度?") },
                    modifier = Modifier.clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                )
            }

            // 停止弹窗
            MyDialog(
                dialogVisible = stopDialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.80f)   // 设置大小为父元素的75%
                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                    .background(color = Color.White),
                onClose = {
                    stopDialogVisible = false
                },
                title = {
                    Text(
                        text = "请选择你的操作",
                        style = MyTheme.typography.todoListTitle.copy(
                            color = Color(0xff303133)
                        )
                    )
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(context, AppActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "放弃当前计时",
                                style = MyTheme.typography.regularText.copy(color = Color.Red)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cour.launch {
                                        withContext(Dispatchers.IO) {
                                            todo.is_complete = 1
                                            // 提前完成计时
                                            MyApplication.db
                                                .todoDao()
                                                .updateTodo(todo)
                                        }
                                        withContext(Dispatchers.Main) {
                                            val intent = Intent(context, AppActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("提前完成计时", style = MyTheme.typography.regularText)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    stopDialogVisible = false
                                }
                                .padding(top = 20.dp, bottom = 20.dp, start = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("取消", style = MyTheme.typography.regularText)
                        }
                    }
                }
            )

            // 暂停弹框
            MyDialog(
                dialogVisible = dialogVisible,
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .clip(shape = RoundedCornerShape(MyTheme.size.roundedCorner))
                    .background(color = Color.White),
                onClose = {
                    dialogVisible = false
                    isCount = true
                },
                title = {
                    Text(
                        text = "暂停",
                        style = MyTheme.typography.todoListTitle.copy(
                            color = Color(0xff303133)
                        )
                    )
                },
                content = {

                }
            )

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .paint(
                        painter = painterResource(id = nowBgPic),
                        contentScale = ContentScale.Crop,
                    )
            ) {
                val (
                    musicLabel,
                    progressBar,
                    todoStatusText,
                    todoName,
                    countTimeText,
                    topActionBar,
                    controlBar) = createRefs()

                val initialProgressBarSize = with(LocalDensity.current) {
                    200.dp.toPx()
                }

                // 进度条的动画显示
                val transition = rememberInfiniteTransition()
                val progressSize = transition.animateFloat(
                    initialValue = initialProgressBarSize,
                    targetValue = initialProgressBarSize + 60f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha = transition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val progressAlpha2 = transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val strokeSize = with(LocalDensity.current) {
                    4.dp.toPx()
                }

                // 音乐名称显示

                AnimatedVisibility(
                    modifier = Modifier
                        .constrainAs(musicLabel) {
                            centerHorizontallyTo(parent)
                            top.linkTo(topActionBar.bottom, margin = 10.dp)
                        }
                        .zIndex(2f),
                    visible = mediaPlayerStatus == MediaPlayerStatus.Playing,
                    enter = fadeIn()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .constrainAs(musicLabel) {
                                top.linkTo(topActionBar.bottom, margin = 10.dp)
                                centerHorizontallyTo(parent)
                            }
                            .zIndex(2f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White
                        )
                        musicList[nowPlayingMusicIndex]?.let {
                            Text(
                                text = it.name,
                                style = MyTheme.typography.todoListTitle.copy(
                                    shadow = MyTheme.shadow.toDoListText,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }


                // 加一点遮罩背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // 外部的倒计时进度条
                Canvas(modifier = Modifier.constrainAs(progressBar) {
                    centerTo(parent)
                }) {
                    // 底部圈
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(width = progressSize.value, height = progressSize.value),
                        topLeft = Offset(
                            x = -(progressSize.value / 2),
                            y = -(progressSize.value / 2)
                        ),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha.value
                    )
                    // 白色色进度条
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = (todo.current_progress.toFloat() / todo.total_time.toFloat()) * 360f,
                        useCenter = false,
                        size = Size(
                            width = initialProgressBarSize,
                            height = initialProgressBarSize
                        ),
                        topLeft = Offset(
                            x = -(initialProgressBarSize / 2),
                            y = -(initialProgressBarSize / 2)
                        ),
                        style = Stroke(width = strokeSize),
                    )
                    // 白色色进度条
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        size = Size(
                            width = initialProgressBarSize,
                            height = initialProgressBarSize
                        ),
                        topLeft = Offset(
                            x = -(initialProgressBarSize / 2),
                            y = -(initialProgressBarSize / 2)
                        ),
                        style = Stroke(width = strokeSize),
                        alpha = progressAlpha2.value
                    )
                }

                // 顶部的ActionBar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MyTheme.elevation.sidePadding)
                        .constrainAs(topActionBar) {
                            top.linkTo(parent.top, margin = 10.dp)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        val intent = Intent(context, AppActivity::class.java)
                        startActivity(intent)
                        finish()
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Close,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "关闭专注页返回待办页面"
                        )
                    }
                    Row() {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Edit,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Share,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                modifier = Modifier.size(MyTheme.size.actionBarIcon),
                                imageVector = Icons.Default.Menu,
                                tint = MyTheme.colors.actionBarIcon,
                                contentDescription = null
                            )
                        }
                    }
                }
                // 显示当前时长
                Text(
                    text = getCurrentTime(nowCountTime.value),
                    modifier = Modifier
                        .constrainAs(countTimeText) {
                            centerTo(parent)
                        },
                    style = MyTheme.typography.todoListTitle.copy(
                        fontSize = 36.sp,
                        shadow = MyTheme.shadow.toDoListText
                    )
                )

                val todoNameTextMargin = with(LocalDensity.current) {
                    initialProgressBarSize.toDp() / 2 + 30.dp
                }

                // todoNameText
                Text(
                    text = todoNameText,
                    modifier = Modifier.constrainAs(todoName) {
                        top.linkTo(progressBar.bottom, margin = todoNameTextMargin)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListTitle.copy(shadow = MyTheme.shadow.toDoListText)
                )

                // todoStatusText
                Text(
                    text = if (isCount) "进行中" else "已暂停",
                    modifier = Modifier.constrainAs(todoStatusText) {
                        top.linkTo(todoName.bottom, margin = 8.dp)
                        centerHorizontallyTo(parent)
                    },
                    style = MyTheme.typography.todoListText.copy(
                        shadow = MyTheme.shadow.toDoListText,
                    )
                )

                // 控制栏
                Row(
                    modifier = Modifier
                        .constrainAs(controlBar) {
                            // 在底部
                            bottom.linkTo(parent.bottom, margin = 70.dp)
                            // 水平居中
                            centerHorizontallyTo(parent, 0.5f)
                        },
                    horizontalArrangement = Arrangement.spacedBy(15.dp)  // 图标的间隔
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Brightness5,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {
                        showMusicChooseDialog = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.LibraryMusic,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "切换背景音乐"
                        )
                    }
                    IconButton(onClick = {
                        isCount = false
                        dialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Pause,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "暂停计时"
                        )
                    }
                    IconButton(onClick = {
                        resetTime = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Refresh,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "重新计时"
                        )
                    }
                    IconButton(onClick = {
                        stopDialogVisible = true
                    }) {
                        Icon(
                            modifier = Modifier.size(MyTheme.size.actionBarIcon),
                            imageVector = Icons.Default.Stop,
                            tint = MyTheme.colors.actionBarIcon,
                            contentDescription = "停止计时"
                        )
                    }
                }

            }
        }
    }
}