package com.example.myapplication.Main.Activity


//import com.example.myapplication.Main.Fragment.ChatFragment.ChatFragment
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DTO.StatusDTO
import com.example.myapplication.DTO.UserinfoDTO
import com.example.myapplication.Main.Fragment.BoardFragment.BoardFragment
import com.example.myapplication.Main.Fragment.BoardFragment.Recent.repo.Repo
import com.example.myapplication.Main.Fragment.ChatFragment.ChatFragment
import com.example.myapplication.Main.Fragment.ChatFragment.ChatRepo
import com.example.myapplication.Main.Fragment.HomeFragment.HomeFragment
import com.example.myapplication.Main.Fragment.MapFragment.MapFragment
import com.example.myapplication.Main.Fragment.MapFragment.MapRepo
import com.example.myapplication.Main.Fragment.Search.SearchFragment
import com.example.myapplication.R
import com.example.myapplication.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frag_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    // MainActivity가 가지고 있는 멤버 변수 선언
    private lateinit var homeFragment: HomeFragment
    private lateinit var mapFragment: MapFragment
    private lateinit var boardFragment: BoardFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var settingFragment: SettingFragment
    private var repo = Repo.StaticFunction.getInstance()
    private var maprepo = MapRepo.StaticFunction.getInstance()
    private var chatRepo = ChatRepo.StaticFunction.getInstance()

    init {
        Log.e("MAIM 엑티비티 실행", "init")
    }

    companion object {
        const val TAG: String = "로그"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavi.setOnNavigationItemSelectedListener(onBottomNavItemSelectedListener)
        // repo.createOnlinstatus()
        Log.e("메인 엑티비티 실행 현황", "onCreate:TODO : 데이터 확인할때 확인작업")
        homeFragment = HomeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.frame_container, homeFragment).commit()
        // searchview 클릭 리스너 넣기

    }


    override fun onPause() {
        super.onPause()
        Log.e("메인 엑티비티 실행 현황", "onPause: TODO : 데이터 확인할때 확인작업")
        repo.upDateOnlineState("offline")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("메인 엑티비티 실행 현황", "onRestart: 데이터 확인할때 확인 작업")

    }

    override fun onResume() {
        super.onResume()
        Log.e("메인 엑티비티 실행 현황", "onResume: 데이터 확인할때 확인 작업")
        repo.upDateOnlineState("online")
        repo.getUserInfo()
        repo.getboarddata()
        repo.getboardUid()
        maprepo.LoadLocation()
        chatRepo.CheckChattingRoom()
        chatRepo.getUserStatus()

//        lifecycleScope.launch(Dispatchers.IO) {
//            repo.getUserInfo()
//            repo.getboarddata()
//            repo.getboardUid()
//            maprepo.LoadLocation()
//            chatRepo.CheckChattingRoom()
//        }
    }


    //바텀 네비게이션 아이템 클릭 리스너

    private val onBottomNavItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.action_home -> {
                    homeFragment = HomeFragment.newInstance()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, homeFragment).commit()
                }
                R.id.action_Map -> {
                    mapFragment = MapFragment.newInstance()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, mapFragment).commit()
                }
                R.id.action_board -> {
                    //repo.getboarddata()
                    //repo.getboardUid()
                    // 사진을 가져올 수 있는지 확인 하는 작업
                    boardFragment = BoardFragment.newInstance()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, boardFragment).commit()
                }
                R.id.action_chat -> {
                    chatFragment = ChatFragment.newInstance()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, chatFragment).commit()
                }
                R.id.action_setting -> {
                    settingFragment = SettingFragment.newInstance()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, settingFragment).commit()
                }
            }
            true
        }
}