package com.example.projecthub.viewModel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.example.projecthub.navigation.routes
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class authViewModel(application: Application): AndroidViewModel(application) {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("UserSession", Context.MODE_PRIVATE)


    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        val isRemembered = sharedPreferences.getBoolean("RememberMe", false)



        if (user == null || !user.isEmailVerified) {
            _authState.value = AuthState.Unauthenticated
        } else {
            val hasCompletedOnboarding = sharedPreferences.getBoolean(
                "HasCompletedOnboarding_${user.uid}", false
            )

            val hasCompletedProfileSetup= sharedPreferences.getBoolean(
                "HasCompletedProfileSetup_${user.uid}",false)

            if (!hasCompletedOnboarding) {
                _authState.value = AuthState.FirstTimeUser
            } else if(!hasCompletedProfileSetup){
                _authState.value = AuthState.ProfileSetupRequired
            }else{
                _authState.value = AuthState.Authenticated
            }
        }
    }

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or Password missing")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        if (rememberMe) {
                            sharedPreferences.edit().putString("SavedEmail", email).apply()
                        } else {
                            // Clear saved email if not checked
                            sharedPreferences.edit().remove("SavedEmail").apply()
                        }
                        // Check if user has completed onboarding
                        val hasCompletedOnboarding = sharedPreferences.getBoolean(
                            "HasCompletedOnboarding_${user.uid}", false
                        )

                        if (hasCompletedOnboarding) {
                            _authState.value = AuthState.Authenticated
                        } else {
                            _authState.value = AuthState.FirstTimeUser
                        }

                        saveLoginSession(rememberMe)
                        handleAuthenticatedUser(user.uid)
                    } else {
                        _authState.value = AuthState.Error("Please verify your email before login")
//                        auth.signOut()
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(email : String , password : String){

        if (email.isEmpty()||password.isEmpty()){
            _authState.value = AuthState.Error("Email or Password missing")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener{verifyTask->
                            if(verifyTask.isSuccessful){
                                _authState.value = AuthState.Error("Verification email sent! Please verify your email.")
                                auth.signOut()
                            }else{
                                _authState.value = AuthState.Error("Failed to send email verification")
                            }
                        }
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun resendVerificationEmail(){
        val user =  auth.currentUser
        if(user != null && !user.isEmailVerified){
            user.sendEmailVerification()
                .addOnCompleteListener{task->
                    if(task.isSuccessful){
                        _authState.value = AuthState.Error("Verification email resent. Please check your email.")
                    }else{
                        _authState.value = AuthState.Error("Failed to resend email verification")
                    }
                }
        }else{
            _authState.value = AuthState.Error("User not found or already verified")
        }
    }

    fun signout(){
        auth.signOut()
        sharedPreferences.edit()
            .remove("UserId")
            .apply()
        _authState.value = AuthState.Unauthenticated
    }

    private fun saveLoginSession(rememberMe: Boolean) {
        val user = auth.currentUser
        if (user != null) {
            sharedPreferences.edit()
                .putBoolean("RememberMe", rememberMe)
                .putString("UserId", user.uid)
                .apply()
        }
    }

    private fun clearLoginSession() {
        sharedPreferences.edit().remove("RememberMe").apply()
    }

    fun resetPassword(email: String){
        if(email.isEmpty()){
            _authState.value = AuthState.Error("Please enter your email")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener{task->
            if (task.isSuccessful){
               _authState.value =  AuthState.Error("Reset password email has been sent!!")
            }else{
                _authState.value = AuthState.Error("Error sending reset email")
            }
        }
    }




    fun completeOnboarding() {
        val user = auth.currentUser
        if (user != null) {
            sharedPreferences.edit().putBoolean("HasCompletedOnboarding_${user.uid}", true).apply()
            _authState.value = AuthState.ProfileSetupRequired
        }
    }

    fun completeProfileSetup(){
        val user = auth.currentUser
        if(user != null){
            sharedPreferences.edit().putBoolean("HasCompletedProfileSetup_${user.uid}",true).apply()
            _authState.value = AuthState.Authenticated
        }
    }
    fun getSavedEmail(): String {
        return sharedPreferences.getString("SavedEmail", "") ?: ""
    }

    private fun handleAuthenticatedUser(userId: String) {
        val hasCompletedOnboarding = sharedPreferences.getBoolean(
            "HasCompletedOnboarding_${userId}", false
        )

        if (!hasCompletedOnboarding) {
            _authState.value = AuthState.FirstTimeUser
            return
        }

        val hasCompletedProfileSetup = sharedPreferences.getBoolean(
            "HasCompletedProfileSetup_${userId}", false
        )

        if (!hasCompletedProfileSetup) {
            _authState.value = AuthState.ProfileSetupRequired
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun changePassword(oldPassword: String, newPassword: String,
    ) {
        val user = auth.currentUser

        if (user == null) {
            _authState.value = AuthState.Error("No user is currently signed in")
            return
        }

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            _authState.value = AuthState.Error("Please enter both old and new passwords")
            return
        }

        if (newPassword.length < 6) {
            _authState.value = AuthState.Error("New password must be at least 6 characters long")
            return
        }

        _authState.value = AuthState.Loading

        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Error("Password updated successfully")
                    }
                    .addOnFailureListener { e ->
                        _authState.value = AuthState.Error("Failed to update password: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Incorrect old password. Please try again or use 'Forgot Password'")
            }
    }

}

sealed class AuthState{
    object Authenticated : AuthState()
    object FirstTimeUser : AuthState()
    object ProfileSetupRequired : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message :String) : AuthState()
}