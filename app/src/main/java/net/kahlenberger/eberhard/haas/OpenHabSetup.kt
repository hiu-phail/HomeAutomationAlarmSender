package net.kahlenberger.eberhard.haas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_open_hab_setup.*
import net.kahlenberger.eberhard.haas.helpers.PasswordManagement

@RequiresApi(Build.VERSION_CODES.M)
class OpenHabSetup : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_hab_setup)

        val pref = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)
        val restUrl = pref.getString(getString(R.string.resturl_key), "")
        val itemName = pref.getString(getString(R.string.item_key), "")
        val username = pref.getString("username", "")
        var password = ""
        val encryptedPassword = pref.getString("encryptedPassword", "")
        try {
            if (encryptedPassword.isNotEmpty()) {
                password = PasswordManagement().decrypt(applicationContext, encryptedPassword)
            }
        } catch (ex: Exception)  {
            Toast.makeText(applicationContext, ex.localizedMessage, Toast.LENGTH_LONG)
        }

        editURL.setText(restUrl)
        editUsername.setText(username)
        editPassword.setText(password)
        editItemName.setText(itemName)
        editItemName.onSubmit { saveSettings(btnSave) }
        showError(getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE))
    }

    override fun onResume() {
        super.onResume()
        showError(getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE))
    }

    private fun showError(pref:SharedPreferences)
    {
        val error = pref.getString(getString(R.string.error_key),"")
        if (error != "")
            textViewError.text = getString(R.string.openhabError_text,error)
        else
            textViewError.text = ""
    }


    fun saveSettings(view: View) {
        val restUrl = (findViewById<EditText>(R.id.editURL)).text.toString()
        val username = (findViewById<EditText>(R.id.editUsername)).text.toString()
        val password = (findViewById<EditText>(R.id.editPassword)).text.toString()
        val encryptedPassword = PasswordManagement().encrypt(this.applicationContext, password)
        val itemName = (findViewById<EditText>(R.id.editItemName)).text.toString()
        val pref: SharedPreferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)
        val edit = pref.edit()
        if (edit != null)
        {
            edit.putString(getString(R.string.resturl_key),restUrl)
            edit.putString("username", username)
            edit.putString("encryptedPassword", encryptedPassword)
            edit.putString(getString(R.string.item_key),itemName)
            edit.apply()
            if (restUrl == "" || itemName == "")
                Toast.makeText(view.context,getString(R.string.toast_receiverDisabled),Toast.LENGTH_LONG).show()
            else
                Toast.makeText(view.context,getString(R.string.toast_receiverConfigured), Toast.LENGTH_LONG).show()
        }
    }
    fun callManageAlarmApps(view: View){
        val i = Intent(view.context, ManageAlarmApps::class.java)
        startActivity(i)
    }
    fun EditText.onSubmit(func: () -> Unit){
        setOnEditorActionListener{_,actionId,_ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                func()
            }
            true
        }
    }
}
