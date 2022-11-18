package com.example.jiaqing_hu

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE


class ManualDialog: DialogFragment(), DialogInterface.OnClickListener {
    companion object{
        var DIALOG_KEY = "dialog"
        var DIALOG_ID = -1
    }
    private val calendar = Calendar.getInstance()
    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {

            Toast.makeText(activity, "saved", Toast.LENGTH_SHORT).show()
        } else if (item == DialogInterface.BUTTON_NEGATIVE) {
            Toast.makeText(activity, "discarded", Toast.LENGTH_SHORT).show()
        }
    }
    /* Create different dialogs based on dialogID, return a dialog object.
    * 0. DatePicker dialog
    * 1. TimePicker dialog
    * 2. Duration dialog
    * 3. Distance dialog
    * 4. Calories dialog
    * 5. Heart Rate dialog
    * 6. Comment dialog
    * */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        val callingActivity = activity as ManualActivity?
        val bundle = arguments
        val dialogId = bundle?.getInt(DIALOG_KEY)
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = requireActivity().layoutInflater.inflate(R.layout.alert_dialog, null)
        when(dialogId){
            0-> {  // Date
                ret = DatePickerDialog(
                    view.context,
                    DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        calendar.set(year,month,dayOfMonth)
                        callingActivity?.dateSet(calendar)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

            }
            1 ->{   // Time
                ret = TimePickerDialog(
                    view.context,
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        calendar.set(HOUR_OF_DAY,hourOfDay)
                        calendar.set(MINUTE, minute)
                        callingActivity?.timeSet(calendar)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )

            }
            2 ->{   // Duration
                builder.setView(view)
                builder.setTitle("Duration")
                builder.setPositiveButton("ok",DialogInterface.OnClickListener {
                        dialog, id ->
                    val editText = view.findViewById<EditText>(R.id.alert_input).text.toString()
                    var input:Double = 0.0
                    if (editText.isNotEmpty())input = editText.toDouble()
                    callingActivity?.durationSet(input)
                } )
                builder.setNegativeButton("cancel", this)
                ret = builder.create()
            }
            3 ->{   // Distance
                builder.setView(view)
                builder.setTitle("Distance")
                builder.setPositiveButton("ok",DialogInterface.OnClickListener {
                        dialog, id ->

                    val editText = view.findViewById<EditText>(R.id.alert_input).text.toString()
                    var input:Double = 0.0
                    if (editText.isNotEmpty())input = editText.toDouble()
                    callingActivity?.distanceSet(input)
                } )
                builder.setNegativeButton("cancel", this)
                ret = builder.create()

            }
            4 ->{   // Calories
                builder.setView(view)
                builder.setTitle("Calories")
                builder.setPositiveButton("ok",DialogInterface.OnClickListener {
                        dialog, id ->
                    val editText = view.findViewById<EditText>(R.id.alert_input).text.toString()
                    var input:Double = 0.0
                    if (editText.isNotEmpty())input = editText.toDouble()
                    callingActivity?.caloriesSet(input)
                } )
                builder.setNegativeButton("cancel", this)
                ret = builder.create()
            }
            5 ->{   // Heart Rate
                builder.setView(view)
                builder.setTitle("Heart Rate")
                builder.setPositiveButton("ok",DialogInterface.OnClickListener {
                        dialog, id ->
                    val editText = view.findViewById<EditText>(R.id.alert_input).text.toString()
                    var input:Double = 0.0
                    if (editText.isNotEmpty())input = editText.toDouble()
                    callingActivity?.heartRateSet(input)
                } )
                builder.setNegativeButton("cancel", this)
                ret = builder.create()
            }
            6 ->{   // Comment
                builder.setView(view)
                builder.setTitle("Comment")
                val editText = view.findViewById<EditText>(R.id.alert_input)
                editText.inputType = InputType.TYPE_CLASS_TEXT
                editText.hint = "How did it go? Notes here."
                builder.setPositiveButton("ok", this)
                builder.setNegativeButton("cancel", this)
                ret = builder.create()
            }
        }
        return ret
    }

}

