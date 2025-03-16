package com.example.upi_demo

import android.app.Activity
import android.content.Intent
import android.media.tv.AdResponse
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.net.toUri

/*Sample code to open UPI payments app from this application. */

class MainActivity : AppCompatActivity() {
    private val UPI_PAYMENT_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val etUpiID : EditText = findViewById(R.id.etUpiID)
        val etAmount : EditText = findViewById(R.id.etAmount)
        val etTxnNote  : EditText = findViewById(R.id.etTxnNote)

        val btnPayment : Button = findViewById(R.id.btnPay)
        btnPayment.setOnClickListener{
            initiateUPIPayment(etAmount.text.toString(),etUpiID.text.trim().toString(),etTxnNote.text.toString())
        }
    }

    private fun initiateUPIPayment(amt : String, upiId : String, txnNote : String){
        val uri = "upi://pay".toUri().buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("tn", txnNote)
            .appendQueryParameter("am", amt)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri

        try{
            startActivityForResult(intent,UPI_PAYMENT_REQUEST )
        }catch (e : Exception){
            Toast.makeText(this, "No UPI apps found. Please Install UPI app.",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPI_PAYMENT_REQUEST){
            if(resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER){
                val response = data?.getStringExtra("response")
                response?.let { parseUpiResponse(it) } ?: run {
                    Toast.makeText(this,"Payment Failed or Cancelled",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Payment Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseUpiResponse(response: String){
        val responseParams = response.split("&")
        var status = ""

        for(param in responseParams){
            val keyValue = param.split("=")
            if (keyValue.size == 2 && keyValue[0] == "status"){
                status = keyValue[1]
            }
        }

        when(status.lowercase()){
            "success" -> Toast.makeText(this,"Payment Successful",Toast.LENGTH_SHORT).show()
            "Failure","failed" -> Toast.makeText(this,"Payment Failed", Toast.LENGTH_SHORT).show()
            "pending" -> Toast.makeText(this,"Payment Pending",Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this,"Unknown Response",Toast.LENGTH_SHORT).show()
        }
    }
}

