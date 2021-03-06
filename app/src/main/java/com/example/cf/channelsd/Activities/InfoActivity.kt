package com.example.cf.channelsd.Activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.cf.channelsd.Data.User
import com.example.cf.channelsd.Interfaces.ProfileInterface
import com.example.cf.channelsd.R
import com.example.cf.channelsd.Utils.ApiUtils
import kotlinx.android.synthetic.main.activity_additional_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class InfoActivity : AppCompatActivity() {

    private val profileInterface: ProfileInterface = ApiUtils.apiProfile
    private var user: User? = null
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_additional_info)
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val preferences: SharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
        user = User(
                preferences.getString("session_key_pref", ""),
                preferences.getString("username_pref", ""),
                preferences.getString("email_pref", ""),
                preferences.getString("userType_pref", ""),
                preferences.getString("firstName_pref", ""),
                preferences.getString("lastName_pref", ""),
                preferences.getString("bio_pref", ""),
                preferences.getString("profile_pic_pref", ""),
                preferences.getString("profile_vid_pref", ""),
                preferences.getString("profile_thumbnail_pref", "")
        )
        val firstNameInput: EditText = input_first_name
        val lastNameInput: EditText = input_last_name
        val bioInput: EditText = input_bio

        if (user!!.firstName == "" && user!!.lastName == "" && user!!.bio == "") {
            complete_profile_info.visibility = View.VISIBLE
        } else {
            complete_profile_info.visibility = View.INVISIBLE
        }
        firstNameInput.setText(user?.firstName, TextView.BufferType.EDITABLE)
        lastNameInput.setText(user?.lastName, TextView.BufferType.EDITABLE)
        bioInput.setText(user?.bio, TextView.BufferType.EDITABLE)
        confirm_btn.setOnClickListener {
            if (checkTextFields() == 3) {
                val username: String = preferences.getString("username_pref", "")
                val firstName: String = input_first_name.text.toString()
                val lastName: String = input_last_name.text.toString()
                val bio: String = input_bio.text.toString()
                sendPost(username, firstName, lastName, bio)
            }
        }
        cancel_btn.setOnClickListener {
            finish()
        }
    }

    private fun editTextLength(editText: EditText): Int {
        return editText.text.toString().length
    }

    private fun toastMessage(message: String) {
        val toast: Toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        val toastView: View = toast.view
        val toastMessage: TextView = toastView.findViewById(android.R.id.message)
        toastMessage.textSize = 20F
        toastMessage.setPadding(4, 4, 4, 4)
        toastMessage.setTextColor(Color.parseColor("#790e8b"))
        toastMessage.gravity = Gravity.CENTER
        toastView.setBackgroundColor(Color.YELLOW)
        toastView.setBackgroundResource(R.drawable.round_button1)
        toast.show()
    }

    private fun checkTextFields(): Int {
        var checked = 0
        if (editTextLength(input_first_name) > 0) {
            checked++
        } else {
            popUpError("This field cannot be blank.", input_first_name)
        }
        if (editTextLength(input_last_name) > 0) {
            checked++
        } else {
            popUpError("This field cannot be blank.", input_last_name)
        }
        if (editTextLength(input_bio) > 0) {
            checked++
        } else {
            popUpError("This field cannot be blank.", input_bio)
        }
        return checked
    }

    private fun popUpError(message: String, editText: EditText) {
        editText.error = message
    }

    private fun sendPost(username: String, firstName: String, lastName: String, bio: String) {
        profileInterface.sendAdditionalInfo(username, firstName, lastName, bio).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                Log.e(ContentValues.TAG, "Unable to get to API." + t?.message)
                if (t?.message == "unexpected end of steam") {
                    sendPost(username, firstName, lastName, bio)
                }
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                if (response!!.isSuccessful) {
                    toastMessage("Profile updated")
                    val userInfo = response.body()
                    val firstNameNew = userInfo?.firstName
                    val lastNameNew = userInfo?.lastName
                    val bioNew = userInfo?.bio
                    val preferences: SharedPreferences = getSharedPreferences("MYPREFS", Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = preferences.edit()
                    editor.putString("firstName_pref", firstNameNew)
                    editor.putString("lastName_pref", lastNameNew)
                    editor.putString("bio_pref", bioNew)
                    editor.apply()
                    val i = Intent(this@InfoActivity, ProfileActivity::class.java)

                    startActivity(i)
                    overridePendingTransition(0, 0)
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
        })
    }
}