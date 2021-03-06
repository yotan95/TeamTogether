package com.example.myapplication.Main.Fragment.HomeFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.DTO.UserinfoDTO
import com.example.myapplication.DTO.Util
import com.example.myapplication.Main.Board.BoardPost
import com.example.myapplication.Main.Fragment.BoardFragment.Recent.repo.Repo
import com.example.myapplication.Main.Fragment.Search.SearchFragment
import com.example.myapplication.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.frag_home.*
import kotlinx.android.synthetic.main.frag_home.view.*
import kotlinx.coroutines.*


// 호출시 HomeFragment.newInstance() 를 이용해서 외부에서 호출
class HomeFragment : Fragment() {
    companion object {
        const val TAG: String = "로그"

        // 외부 호출시 메모리에 적제된 HomeFragment를 불러올수 있게함
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private val repo = Repo.StaticFunction.getInstance()
    private lateinit var photoAdapter: PhotoAdapter
    private var dataList = mutableListOf<DataModel>()
    private var userinfoDTO = UserinfoDTO()
    private var util = Util.StaticFunction.getInstance()
    private lateinit var hotAdapter: HotAdapter
    private val viewModel by lazy {
        ViewModelProvider(this).get(HotViewModel::class.java)
    }

    init {
        val ft : FragmentTransaction? = fragmentManager?.beginTransaction()
        lifecycleScope.launch(Dispatchers.IO) {
            var a = async {
                getUserData()
            }.await()
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1000)
                if (userinfoDTO.ProfileUrl != null) {
                    Glide.with(requireContext()).load(userinfoDTO.ProfileUrl)
                        .into(main_page_user_img)
                }
                else {
                main_page_user_img.setImageResource(R.drawable.ic_baseline_account_circle_signiture)
            }
                recommend_textView.text = "${userinfoDTO.nickname}님 오늘은 치킨 먹어요!"

                ft?.detach(HomeFragment())?.attach(HomeFragment())?.commit()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userinfoDTO = repo.returnUserInfo()
        hotAdapter = HotAdapter(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frag_home, container, false)
        view.btn_board.setOnClickListener {
            GoBorad()
        }
        return view
    }

    fun GoBorad() {
        var intent = Intent(requireActivity(), BoardPost::class.java)
        startActivity(intent)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hot_recyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hotAdapter
            observeData()
        }

        fun startActivity(activity: Activity) {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)//위 아래 애니메이션코드
        }

        val fragmentTransaction: FragmentTransaction = requireFragmentManager().beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.horizon_enter, R.anim.none)//위아래 애니메이션(62~63)

        main_search_view.setOnClickListener {
            val intent = Intent(requireContext(), SearchFragment::class.java)
            ContextCompat.startActivity(requireContext(), intent, null)
        }


        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        photoAdapter = PhotoAdapter(requireContext())
        recyclerView.adapter = photoAdapter

        dataList.add(DataModel("치킨", R.drawable.chiken))
        dataList.add(DataModel("피자", R.drawable.pizza))
        dataList.add(DataModel("떡볶이", R.drawable.ddukbokki))
        dataList.add(DataModel("파스타", R.drawable.pasta))
        dataList.add(DataModel("돈까스", R.drawable.gas))
        dataList.add(DataModel("초밥", R.drawable.sushi))
        dataList.add(DataModel("카페", R.drawable.coffeee))
        dataList.add(DataModel("햄버거", R.drawable.hamberger))
        dataList.add(DataModel("중국집", R.drawable.jjajang))
        dataList.add(DataModel("스테이크", R.drawable.steak))
        dataList.add(DataModel("고기", R.drawable.samgupsal))
        dataList.add(DataModel("곱창", R.drawable.gopchang))
        dataList.add(DataModel("육회", R.drawable.yuksushi))
        dataList.add(DataModel("보쌈", R.drawable.bossam))
        dataList.add(DataModel("닭발", R.drawable.chickenfoot))
        dataList.add(DataModel("카레", R.drawable.curry))
        dataList.add(DataModel("브리또", R.drawable.buritto))
        dataList.add(DataModel("도시락", R.drawable.dosirak))
        dataList.add(DataModel("국밥", R.drawable.gookbap))
        dataList.add(DataModel("찌개", R.drawable.gimchijjigae))
        dataList.add(DataModel("덮밥", R.drawable.dupbap))
        dataList.add(DataModel("생선 구이", R.drawable.sangsun92))
        dataList.add(DataModel("냉면", R.drawable.yesmyen))
        dataList.add(DataModel("마라탕", R.drawable.maratang))
        dataList.add(DataModel("샌드위치", R.drawable.sandwich))
        dataList.add(DataModel("라멘", R.drawable.ramyen))
        dataList.add(DataModel("찜닭", R.drawable.jjimdak))



        photoAdapter.setDataList(dataList)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeData() {
        viewModel.getHotListData().observe(viewLifecycleOwner, Observer {
            hotAdapter.setHotListDTO(it)
            hotAdapter.notifyDataSetChanged()
        })
        viewModel.getUidlistData().observe(viewLifecycleOwner, Observer {
            hotAdapter.setListUid(it)
            hotAdapter.notifyDataSetChanged()
        })
    }

    suspend fun getUserData() {
        val firestore = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val Uef = firestore.collection("userid").document(uid)
        Uef.addSnapshotListener { value, error ->
            userinfoDTO = value!!.toObject(UserinfoDTO::class.java)!!
        }

    }


    override fun onPause() {
        super.onPause()
        repo.upDateOnlineState("offline")

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        repo.upDateOnlineState("online")


    }


}