package com.example.myapplication.Main.Fragment.BoardFragment.Recent.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.DTO.BoardDTO
import com.example.myapplication.DTO.UserinfoDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.frag_board.*

class Repo {
    // 초기값 백업1
    private var firestore = FirebaseFirestore.getInstance()
    private var listdata = mutableListOf<BoardDTO>()
    private var contentsuid = arrayListOf<String>()

    object StaticFunction {
        private var instance: Repo? = null
        fun getInstance(): Repo {
            if (instance == null)
                instance = Repo()
            return instance!!
        }
    }
    init {
        listdata.clear()
        contentsuid.clear()
        firestore.collection("Board").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener() { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) return@addSnapshotListener
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(BoardDTO::class.java)
                    listdata.add(item!!)
                    contentsuid.add(snapshot.id)
                }
            }

    }

    fun getboarddata(): MutableList<BoardDTO> {
//        firestore.collection("Board").orderBy("timestamp")
//            .addSnapshotListener() { querySnapshot, firebaseFirestoreException ->
//                if (querySnapshot == null) return@addSnapshotListener
//                for (snapshot in querySnapshot!!.documents) {
//                    var item = snapshot.toObject(BoardDTO::class.java)
//                    listdata.add(item!!)
        Log.e("Repo listdata ", listdata.size.toString() )
        return listdata
    }

    fun getboardUid(): ArrayList<String> {
//        firestore.collection("Board").orderBy("timestamp")
//            .addSnapshotListener() { querySnapshot, firebaseFirestoreException ->
//                if (querySnapshot == null) return@addSnapshotListener
//                for (snapshot in querySnapshot!!.documents) {
//                    contentsuid.add(snapshot.id)
//                }
//            }
        return contentsuid
    }

    fun upDateOnlineState(status : String){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val uidCollection = uid+"_status"
        val onlineStatus = UserinfoDTO.OnlineStatus()
        onlineStatus.status["Online"] = status
        val firestoreRef = firestore.collection("userid").document(uid).collection("status").document(uidCollection)
        firestoreRef.update(
            mapOf(
                "useruid" to uid,
                "status" to onlineStatus.status
            )
        )
    }
    fun createOnlinstatus(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val uidCollection = uid+"_status"
        val onlineStatus = UserinfoDTO.OnlineStatus()
        val firestoreRef = firestore.collection("userid").document(uid).collection("status").document(uidCollection)
        firestoreRef.get().addOnSuccessListener {
            if (it.exists()){}
            else{
                firestoreRef.set(onlineStatus)
            }
        }
    }

}






