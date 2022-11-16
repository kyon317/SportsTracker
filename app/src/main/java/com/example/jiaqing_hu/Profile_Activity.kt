package com.example.jiaqing_hu

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/* Profile Activity based on MyRuns1 */
class Profile_Activity : AppCompatActivity() {

    private lateinit var imageview: ImageView
    private lateinit var imgUri: Uri
    private lateinit var imgUri_tmp: Uri
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var albumResult: ActivityResultLauncher<Intent>
    private var name_input:String = ""
    private var email_input:String = ""
    private var phone_input:String = ""
    private var class_input:String = ""
    private var major_input:String = ""
    private var gender_option:Int = -1
    private lateinit var name_input_curr: EditText
    private lateinit var email_input_curr: EditText
    private lateinit var phone_input_curr: EditText
    private lateinit var class_input_curr: EditText
    private lateinit var major_input_curr: EditText

    /* Define OnCreate func when main activity is started/reconstructed:
    * 1. Use temp file to handle any unsaved photos;
    * 2. When reconstructed, should use temp photo if available;
    * 3. Process results from camera launch activity;
    * 4. Check for permissions.
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initialize(savedInstanceState)

        imageview = findViewById<ImageView>(R.id.imageView)

        val imgFile = File(getExternalFilesDir(null),"profile.jpg")
        var imgFile_tmp = File(getExternalFilesDir(null),"profile_tmp.jpg")
        imgUri = FileProvider.getUriForFile(this,"com.example.jiaqing_hu",imgFile)
        imgUri_tmp = FileProvider.getUriForFile(this,"com.example.jiaqing_hu",imgFile_tmp)


        cameraResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bitmap_tmp = Util.getBitmap(this, imgUri_tmp)
                imageview.setImageBitmap(bitmap_tmp)
            } else {
                val bitmap = Util.getBitmap(this, imgUri)
                imageview.setImageBitmap(bitmap)
            }
        }
        albumResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                imgUri_tmp = it.data?.data!!
                // reference: https://stackoverflow.com/questions/49091392/android-save-image-uri-in-internal-storage
                val input: InputStream? = this.getContentResolver().openInputStream(imgUri_tmp)
                val inputStream = BitmapFactory.decodeStream(input)
                val file: File = File(getExternalFilesDir(null),"profile_tmp.jpg")
                val outputStream = FileOutputStream(file)
                inputStream.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.flush();
                outputStream.close();
                imageview.setImageURI(imgUri_tmp)
            } else {
                val bitmap = Util.getBitmap(this, imgUri)
                imageview.setImageURI(imgUri)
            }
        }
        if (imgFile_tmp.exists()){
//            val bitmap = Util.getBitmap(this, imgUri_tmp)
            imageview.setImageURI(imgUri_tmp)
        }
        else if (imgFile.exists()){
            imageview.setImageURI(imgUri)
        }

        Util.checkPermissions(this)

    }

    /* Recover data when the main activity is restored:
    * 1. Read data in savedInstanceState, assign them to each field;
    * 2. If savedInstanceState is null, read persistent data from SharedPreferences;
    * 3. Update UI.
    * */
    fun initialize(savedInstanceState: Bundle?){
        if (savedInstanceState != null) {
            name_input = (savedInstanceState.get("name") as String?).toString()
            email_input = (savedInstanceState.get("email") as String?).toString()
            phone_input = (savedInstanceState.get("phone") as String?).toString()
            class_input = (savedInstanceState.get("class") as String?).toString()
            major_input = (savedInstanceState.get("major") as String?).toString()
            gender_option = (savedInstanceState.get("gender") as Int?)!!
        }else{
            Log.d(ContentValues.TAG, "initialize: isNull")
            val sharedPreferences = getSharedPreferences("AppSharedPref", MODE_PRIVATE)
            name_input = sharedPreferences.getString("name","").toString()
            email_input = sharedPreferences.getString("email","").toString()
            phone_input = sharedPreferences.getString("phone","").toString()
            class_input = sharedPreferences.getString("class","").toString()
            major_input = sharedPreferences.getString("major","").toString()
            gender_option = sharedPreferences.getInt("gender",-1)
        }
        name_input_curr = findViewById<EditText>(R.id.name_input)
        email_input_curr = findViewById<EditText>(R.id.email_input)
        phone_input_curr = findViewById<EditText>(R.id.phone_input)
        class_input_curr = findViewById<EditText>(R.id.class_input)
        major_input_curr = findViewById<EditText>(R.id.major_input)

        if (gender_option!=-1){
            when(gender_option){
                1->
                {
                    val male_btn = findViewById<RadioButton>(R.id.male_btn)
                    male_btn.isChecked = true
                }
                0->{
                    val female_btn = findViewById<RadioButton>(R.id.female_btn)
                    female_btn.isChecked = true
                }
            }
        }
        name_input_curr.setText(name_input)
        email_input_curr.setText(email_input)
        phone_input_curr.setText(phone_input)
        class_input_curr.setText(class_input)
        major_input_curr.setText(major_input)
    }

    /* Define activities when the change button is clicked:
    * 1. Launch camera activity;
    * 2. Depending on the result, either keep or discard img_tmp
    * 3. If OK set imageview to img_tmp
    * */
    fun onChangePhotoClicked(view: View){
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Pick Profile Picture")
        builder.setPositiveButton("Open Camera",
            DialogInterface.OnClickListener { dialog, which -> camera(view) })
        builder.setNegativeButton("Select from Gallery",
            DialogInterface.OnClickListener { dialog, which -> album(view) })
        val alert: android.app.AlertDialog? = builder.create()
        if (alert != null) {
            alert.show()
        }
    }
    fun camera(view: View){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri_tmp)
        cameraResult.launch(intent)
    }
    fun album(view: View){
        val intent2 = Intent(Intent.ACTION_PICK)
        intent2.type = "image/*"
        albumResult.launch(intent2)
    }

    /* Define activities when the Save button is clicked :
    * 1. Save all the data user has input;
    * 2. Store data using SharedPreferences;
    * 3. Store img_tmp as img.
    * */
    fun onSaveButtonClicked(view: View){
        name_input = name_input_curr.text.toString()
        email_input = email_input_curr.text.toString()
        phone_input = phone_input_curr.text.toString()
        class_input = class_input_curr.text.toString()
        major_input = major_input_curr.text.toString()
        // Save input
        val sharedPreferences = getSharedPreferences("AppSharedPref", MODE_PRIVATE)
        val newEdit = sharedPreferences.edit()
        newEdit.putString("name", name_input)
        newEdit.putString("email", email_input)
        newEdit.putString("phone", phone_input)
        newEdit.putString("class", class_input)
        newEdit.putString("major", major_input)
        newEdit.putInt("gender",gender_option)
        Log.d(ContentValues.TAG, "onSaveButtonClicked: $gender_option")
        newEdit.apply();
        // Save img
        val imgFile = File(getExternalFilesDir(null),"profile.jpg")
        val imgFile_tmp = File(getExternalFilesDir(null),"profile_tmp.jpg")
        if (imgFile_tmp.exists()){
            imgFile_tmp.renameTo(imgFile)
            imgFile_tmp.delete()
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    /* Define activities when the Cancel button is clicked :
    * 1. Discard all the data user has input;
    * 2. Discard temp photo file.
    * */
    fun onCancelButtonClicked(view: View){
        val imgFile_tmp = File(getExternalFilesDir(null),"profile_tmp.jpg")
        if (imgFile_tmp.exists()){
            imgFile_tmp.delete()
        }
        finish()
    }

    /* Define activities when the Radio button is clicked :
    * 1. Uncheck other options;
    * 2. Update gender variable.
    * */
    fun onRadioButtonClicked(view: View){
        Log.d(ContentValues.TAG, "onRadioButtonClicked: view checked")
        val female_btn = findViewById<RadioButton>(R.id.female_btn)
        val male_btn = findViewById<RadioButton>(R.id.male_btn)
        if (view is RadioButton){
            val checked = view.isChecked
            when(view.id){
                R.id.male_btn ->
                    if (checked){
                        gender_option = 1
                        female_btn.isChecked = false
                        Log.d(ContentValues.TAG, "onRadioButtonClicked: $gender_option")
                    }
                R.id.female_btn ->
                    if (checked){
                        gender_option = 0
                        male_btn.isChecked = false
                        Log.d(ContentValues.TAG, "onRadioButtonClicked: $gender_option")
                    }
            }
        }
    }

    /* Save current state when main activity is destroyed :
    * 1. Save all user input to outState bundle;
    * */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("name",name_input)
        outState.putString("email",email_input)
        outState.putString("phone",phone_input)
        outState.putString("class",class_input)
        outState.putString("major",major_input)
        outState.putInt("gender",gender_option)
    }

}