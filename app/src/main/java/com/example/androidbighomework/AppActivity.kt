package com.example.androidbighomework

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.androidbighomework.todoPage.ConcentrateStatus
import com.google.android.material.tabs.TabLayout

class AppActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var myTabLayout: TabLayout;
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        supportActionBar?.hide()

        // 初始化navigation
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        initTab()
        initPermission()
    }

    override fun onStart() {
        super.onStart()
        MyApplication.todoStatus.value = ConcentrateStatus.NotBegin
    }

    // 请求通知权限
    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    when (isGranted) {
                        true -> {
                            // 如果通过了就不管
                        }
                        false -> {
                            // 如果拒绝了就再次请求
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
            // 请求通知发布权限
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initTab() {
        // 先获取tab
        myTabLayout = findViewById(R.id.navigation_tab)
        myTabLayout.addTab(
            myTabLayout.newTab().setIcon(this.getDrawable(R.drawable.home)).setText("主页")
        )
        myTabLayout.addTab(
            myTabLayout.newTab().setIcon(this.getDrawable(R.drawable.schedule)).setText("待办")
        )
        myTabLayout.addTab(
            myTabLayout.newTab().setIcon(this.getDrawable(R.drawable.info)).setText("统计")
        )
        myTabLayout.addTab(
            myTabLayout.newTab().setIcon(this.getDrawable(R.drawable.user)).setText("我的")
        )

        // Tab选择触发
        myTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            /*
            * TODO: 家人们，页面切换在这里切换，在when里面写,切换的内容要是fragment
             */
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 选择Tab
                when (tab?.position) {
                    0 -> navController.navigate(R.id.blankFragment)
                    1 -> navController.navigate(R.id.blankFragment)
                    2 -> navController.navigate(R.id.blankFragment)
                    3 -> navController.navigate(R.id.blankFragment)

                }

//                Log.d("Tab被选中了", "位置是${tab?.position}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
}