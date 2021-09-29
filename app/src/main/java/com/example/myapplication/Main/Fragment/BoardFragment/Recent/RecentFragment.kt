package com.example.myapplication.Main.Fragment.BoardFragment.Recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.Main.Fragment.BoardFragment.Recent.repo.Repo
import com.example.myapplication.R
import com.example.myapplication.ViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_recent.*

class RecentFragment : Fragment(){
    companion object {
        fun newInstance() : RecentFragment = RecentFragment()
    }
    private var boardListAdapter = BoardListAdapter()
    private var repo : Repo

    init {
        repo = Repo.StaticFunction.getInstance()
    }
    private fun getfoodlList(): ArrayList<Int> {
        return arrayListOf<Int>(R.drawable.pizza, R.drawable.coffee, R.drawable.rice)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recent,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager_food.adapter = ViewPagerAdapter(getfoodlList()) // 어댑터 생성
        viewPager_food.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로


        board_fragement_recycler_view.apply {
            var boardlistadapter: BoardListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            boardlistadapter = BoardListAdapter()
            adapter = boardlistadapter
            boradSwiprefresh.setOnRefreshListener {
                //notices.clear() // 리스트를 한 번 비워주고
                //crawler.activateBot(page) // 리스트에 값을 넣어주고
                boardListAdapter.clear()
                boardListAdapter.notifyDataSetChanged() // 새로고침 하고
                boradSwiprefresh.isRefreshing = false // 새로고침을 완료하면 아이콘을 없앤다.
            }
        }
    }
}