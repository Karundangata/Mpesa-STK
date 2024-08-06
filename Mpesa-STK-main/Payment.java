package com.example.finalyear;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import static android.content.ContentValues.TAG;

import com.example.finalyear.Sandbox;


import org.json.JSONException;

import java.io.IOException;

import static com.example.finalyear.GenerateValues.generateDate;
import static com.example.finalyear.GenerateValues.generatePassword;
import static com.example.finalyear.Result.ResponseCode;
import static com.example.finalyear.StkPush.stkpush;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Payment extends AppCompatActivity {
    EditText editTextPhone, editTextAmount;
    Button buttonRequest;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAmount = findViewById(R.id.editTextAmount);
        buttonRequest = findViewById(R.id.buttonRequest);
        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);


        }
        //settings;
        Sandbox.setAccess_token_url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials");
        Sandbox.setBusinessShortCode("174379");
        Sandbox.setConsumerKey("ZxLfbGFO5MMYJvGIUOcHLZ3vK0rabcGt");//enter consumer key
        Sandbox.setConsumerSecret("3GAgxyTuNBkLNgmY");//enter consumer secret
        Sandbox.setPassKey("bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919");//enter passkey
        Sandbox.setStk_push_url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest");
        Sandbox.setC2bSimulation_url("https://sandbox.safaricom.co.ke/mpesa/c2b/v1/simulate");
//254708374149
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                request(editTextAmount.getText().toString(), editTextPhone.getText().toString());
                // Update payment field in Firestore
                updateFirestorePaymentField();
            }
        });
        firestore = FirebaseFirestore.getInstance();


    }

    private boolean checkPhoneCode(String phone) {
        return phone.startsWith("254");
    }

    private boolean checkPhone(String phone) {
        return phone.length() == 12;
    }

    private boolean checkAmount(String amount) {
        return amount.isEmpty();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void request(String amount, String phone) {
        if (!checkPhoneCode(phone)) {
            editTextPhone.requestFocus();
            editTextPhone.setError("Wrong format");
        } else if (!checkPhone(phone)) {
            editTextPhone.requestFocus();
            editTextPhone.setError("Wrong format");
        } else if (checkAmount(amount)) {
            editTextAmount.requestFocus();
            editTextAmount.setError("Cannot be empty");
        } else {
            try {
                stkpush(Sandbox.businessShortCode, generatePassword(), generateDate(),
                        "CustomerPayBillOnline", amount, phone,
                        "254790118500", "174379",
                        "https://webhook.site/3d667c6d-7db7-42dc-80b4-2eaea59d960b",
                        "Final Year", "Payment for Ap");



                Toast.makeText(Payment.this, "ResponseCode:" + ResponseCode, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void updateFirestorePaymentField() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("users").document(currentUserId)
                .update("payment", "Paid")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle success
                        Log.d(TAG, "Payment status updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e(TAG, "Error updating payment status", e);
                    }
                });
    }

}
