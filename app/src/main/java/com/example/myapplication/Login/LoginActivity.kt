package com.example.myapplication.Login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.DTO.UserinfoDTO
import com.example.myapplication.Main.Activity.MainActivity
import com.example.myapplication.R
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    // Firebase 인증 객체 생성
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var callbackManager: CallbackManager = CallbackManager.Factory.create()
    val GOOGLE_LOGIN_CODE = 9001
    private val TAG = "LoginActivity"

    lateinit var login_id: String
    lateinit var login_pw: String

    private lateinit var googleSignInclient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        //facebook SDK 앱 활성화 지원 도구 호출
        FacebookSdk.sdkInitialize(applicationContext);
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login)

        // 파이어베이스 인증 객체 선언
        auth = FirebaseAuth.getInstance()

        /*
        //ViewPager 전용 List (메인 장식 화면)
        val listImage = ArrayList<Int>()
        listImage.add(R.drawable.이미지 파일 명)
        listImage.add(R.drawable.ic_launcher_foreground)

        //ViewPager Adapter 등록
        val loginimagefragmentAdapter = LogInImageFragmentAdapter(supportFragmentManager)
        login_viewPager.adapter = loginimagefragmentAdapter

        (미완)
        */
        // Google 로그인 환경설정
        // default_web_client Direct : "375254490249-d375254490249-dj1mu3rp7mstcts3gnlbs48ngco7g8m9.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("375254490249-dj1mu3rp7mstcts3gnlbs48ngco7g8m9.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInclient = GoogleSignIn.getClient(this, gso)
        database = Firebase.database.reference // 쓸지 말지 아직 미정

        bt_login.setOnClickListener {
            signIn()
        }
        bt_signup.setOnClickListener {
            signUp()
        }
        signin_googleButton.setOnClickListener {
            google_login()
        }
        facebookSignInBtn.setOnClickListener {
            facebook_login()
        }

        // 로그인이 됐다면 카카오 자동 로그인(로그인 유지)
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                Toast.makeText(this, "토튼 정보 보기 실패", Toast.LENGTH_SHORT).show()
            } else if (tokenInfo != null) {
                Toast.makeText(this, "토큰 정보 보기 성공", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }
        // 카카오 로그인 에러
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 된(동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT)
                            .show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않음(android key hash", Toast.LENGTH_SHORT)
                            .show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                    }
                    else -> { //Unknown
                        Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (token != null) {
                Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            }
        }
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인 아니면 카카오 계정으로 로그인
        kakao_login_button.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

    }

    // 구글 로그인
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == GOOGLE_LOGIN_CODE && resultCode == Activity.RESULT_OK)
        if (requestCode == GOOGLE_LOGIN_CODE) {
//            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try { // 구글 로그인 성공
                // result.isSuccessful이 문제

                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)

//                if (result.isSuccessful) {
////                    val account = result.getResult(ApiException::class.java)
//                    val account = task.getResult(ApiException::class.java)!!
//                    firebaseAuthWithGoogle(account.idToken!!)
//                }
            } catch (e: ApiException) {
                // 구글 로그인 실패
                Log.w("googleloginfail", "Google sign in failed", e)
            }
        }
    }

//    자동 로그인
//    override fun onStart() {
//        super.onStart()
//        var currentUser = auth.currentUser
//        if (currentUser != null) {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
//    }

    // 텍스트 객체에서 받아온 파라미터가 있는지 없는지 검사
    fun isValidId(): Boolean {
        if (login_id.isEmpty())
            return false
        else
            return true
    }

    fun isValidPw(): Boolean {
        if (login_pw.isEmpty())
            return false
        else
            return true
    }

    //일반 로그인
    fun signIn() {
        login_id = login_Id.text.toString()
        login_pw = login_Pw.text.toString()
        if (isValidId() && isValidPw())
            loginrUser(login_id, login_pw)
    }

    //회원 가입
    fun signUp() {
        login_id = login_Id.text.toString()
        login_pw = login_Pw.text.toString()
        if (isValidId() && isValidPw())
            createUser(login_id, login_pw)
    }

    // 구글 로그인 처리
    fun google_login() {
        val signInIntent = googleSignInclient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    // 페이스북 로그인
    fun facebook_login() {

        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    FirebaseAuthWithFacebook(result?.accessToken)
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                }
            })
    }

    fun FirebaseAuthWithFacebook(token: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userInfoDTO = UserinfoDTO()
                val uemail = FirebaseAuth.getInstance().currentUser!!.email
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val phonenumber = FirebaseAuth.getInstance().currentUser!!.phoneNumber
                userInfoDTO.userEmail = uemail.toString()
                userInfoDTO.UID = uid
                userInfoDTO.signUpdate = SimpleDateFormat("yyyyMMdd").format(Date())
                FirebaseFirestore.getInstance().collection("userid").document(uid)
                    .set(userInfoDTO)
                facebook_sign()
            }
        }
    }

    //페이스북 로그인 처리
    fun facebook_sign() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Add_LoginActivity::class.java))
            this.finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userInfoDTO = UserinfoDTO()
                    val uemail = FirebaseAuth.getInstance().currentUser!!.email
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val phonenumber =FirebaseAuth.getInstance().currentUser
                    userInfoDTO.userEmail = uemail.toString()
                    userInfoDTO.UID = uid
                    userInfoDTO.signUpdate = SimpleDateFormat("yyyyMMdd").format(Date())

                    FirebaseFirestore.getInstance().collection("userid").document(uid)
                        .set(userInfoDTO)
                    google_signIn()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        var credential = GoogleAuthProvider.getCredential(idToken, null)
//        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                val userInfoDTO = UserinfoDTO()
//                val uemail = FirebaseAuth.getInstance().currentUser!!.email
//                val uid = FirebaseAuth.getInstance().currentUser!!.uid
//                userInfoDTO.userEmail = uemail.toString()
//                userInfoDTO.userId = uid
//                userInfoDTO.signUpdate = SimpleDateFormat("yyyyMMdd").format(Date())
//                FirebaseFirestore.getInstance().collection("userid").document(uid)
//                    .set(userInfoDTO)
//                google_signIn()
//            } else {
//                Log.w(TAG, "signInWithCredential:failure", task.exception)
//            }
//        }
//    }

    fun google_signIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Add_LoginActivity::class.java))
            this.finish()
        }
    }



    // 구글 로그인 인증
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {// 구글 로그인 인증 성공
                    val userInfoDTO = UserinfoDTO()
                    val uemail = FirebaseAuth.getInstance().currentUser!!.email
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    userInfoDTO.userEmail = uemail.toString()
                    userInfoDTO.UID = uid
                    userInfoDTO.signUpdate = SimpleDateFormat("yyyyMMdd").format(Date())
                    FirebaseFirestore.getInstance().collection("userid").document(uid)
                        .set(userInfoDTO)
                    Toast.makeText(this, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
            }
    }


    // 일반 로그인
    fun loginrUser(login_id: String, login_pw: String) {
        auth.signInWithEmailAndPassword(login_id, login_pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인 성공입니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
    }

    //회원가입
    fun createUser(login_id: String, login_pw: String) {
        val timeStamp = SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Date())
        val imageFileName = "JPEG_"+timeStamp+"_.png"
        if (login_Id.text.toString().length == 0 || login_Pw.text.toString().length == 0) {
            Toast.makeText(this, "email 혹은 페스워드를 반드시 입력하세요,", Toast.LENGTH_SHORT).show()
        } else {
            auth.createUserWithEmailAndPassword(login_id, login_pw)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "환영합니다", Toast.LENGTH_SHORT).show()

                        // 회원가입이 성공하면 firestore email 및 uid 저장
                        val userInfoDTO = UserinfoDTO()
                        val uemail = FirebaseAuth.getInstance().currentUser!!.email
                        val uPW = login_Pw.text.toString()
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid

                        userInfoDTO.userEmail = uemail.toString()
                        userInfoDTO.UID = uid
                        userInfoDTO.userPassword = uPW
                        userInfoDTO.signUpdate = timeStamp


                        // database.child("userid").child(uid).setValue(userInfoDTO)
                        //파이어스토어
                        // FirebaseFirestore.getInstance().collection("userid").document("uid")
                        //.set(userInfoDTO)
                        FirebaseFirestore.getInstance().collection("userid").document(uid)
                            .set(userInfoDTO)
                        val intent = Intent(this, Add_LoginActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        //입력필드 초기화
                        login_Id?.setText("")
                        login_Pw?.setText("")
                        login_Id.requestFocus()
                    }
                }

        }
    }

}

