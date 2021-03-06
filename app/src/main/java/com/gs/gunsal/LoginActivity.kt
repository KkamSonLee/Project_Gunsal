package com.gs.gunsal

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.mikephil.charting.utils.Utils.init
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.FitnessStatusCodes
import com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_CUMULATIVE
import com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.gs.gunsal.dataClass.BodyDataDetail
import com.gs.gunsal.dataClass.UserData
import com.gs.gunsal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth
    //google client
    private lateinit var googleSignInClient: GoogleSignInClient

    //private const val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //btn_googleSignIn.setOnClickListener (this) // 구글 로그인 버튼
        binding.setProfile.setOnClickListener {signIn()}

        //Google 로그인 옵션 구성. requestIdToken 및 Email 요청
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("895742237394-pii5o2pv9ed6ieeri5nr3echckntl46r.apps.googleusercontent.com")
            //'R.string.default_web_client_id' 에는 본인의 클라이언트 아이디를 넣어주시면 됩니다.
            //저는 스트링을 따로 빼서 저렇게 사용했지만 스트링을 통째로 넣으셔도 됩니다.
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //firebase auth 객체
        firebaseAuth = FirebaseAuth.getInstance()

    }

    // onStart. 유저가 앱에 이미 구글 로그인을 했는지 확인
    public override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account!==null){ // 이미 로그인 되어있을시 바로 메인 액티비티로 이동
            toMainActivity(firebaseAuth.currentUser)
        }
    } //onStart End

    // onActivityResult
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("com.gs.gunsal.LoginActivity", "Google sign in failed", e)
            }
        }
    } // onActivityResult End

    // firebaseAuthWithGoogle
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("com.gs.gunsal.LoginActivity", "firebaseAuthWithGoogle:" + acct.id!!)

        //Google SignInAccount 객체에서 ID 토큰을 가져와서 Firebase Auth로 교환하고 Firebase에 인증
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.w("com.gs.gunsal.LoginActivity", "firebaseAuthWithGoogle 성공", task.exception)
                    toMainActivity(firebaseAuth.currentUser)
                } else {
                    Log.w("com.gs.gunsal.LoginActivity", "firebaseAuthWithGoogle 실패", task.exception)
                    Toast.makeText(this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }// firebaseAuthWithGoogle END


    // toMainActivity
    fun toMainActivity(user: FirebaseUser?) {
        if(user != null) { // MainActivity 로 이동
            FirebaseRepository.getUserInfo(user)
            FirebaseRepository.userDataListener = object : FirebaseRepository.OnUserDataListener {
                override fun onUserDataCaught(userData: UserData, isFirst: Boolean) {
                    Log.d("toMainActivity->onUserDataCaught", "success")
                    FirebaseRepository.getBodyData(userData.user_id)
                    FirebaseRepository.bodyDataListener =
                        object : FirebaseRepository.OnBodyDataListener {
                            override fun onBodyDataCaught(bodyDataDetail: BodyDataDetail) {
                                if(bodyDataDetail.height < 1.0 || bodyDataDetail.weight < 1.0){
                                    Log.d("toMainActivity-> onBodyDataCaught", "NO BODY INFO")
                                    val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                                    intent.putExtra("USER_ID", user.uid)
                                    startActivity(intent)
                                    finish()
                                }
                                else{
                                    Log.d("toMainActivity-> onBodyDataCaught", "ALL INFO STATUS GOOD. GO TO MAIN")
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    intent.putExtra("USER_ID", userData.user_id)
                                    intent.putExtra("USER_NICK_NAME", userData.nick_name)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                }

                override fun onUserDataUncaught(user: FirebaseUser) {
                    Log.i("toMainActivity-> onUserDataUncaught", "success")
                    FirebaseRepository.enrollUser(user.uid.toString(), user.displayName.toString(), 0)
                    FirebaseRepository.getTotalData(user.uid, FirebaseRepository.getCurrentDate())
                    val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                    intent.putExtra("USER_ID", user.uid)
                    startActivity(intent)
                    finish()
                }
            }
        }
    } // toMainActivity End

    // signIn
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() { // 로그아웃
        // Firebase sign out
        firebaseAuth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
            //updateUI(null)
        }
    }
}