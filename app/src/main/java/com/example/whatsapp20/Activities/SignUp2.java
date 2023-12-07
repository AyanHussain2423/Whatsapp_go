package com.example.whatsapp20.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapp20.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignUp2 extends AppCompatActivity {
 Button button2;
 EditText num;
 String optid;
 String verificationId;
 FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sign_up2);
        findViewById(R.id.editTextNumber);
        findViewById(R.id.button2);
        mAuth = FirebaseAuth.getInstance();
        num = findViewById(R.id.editTextNumber);
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v -> {
                if (!num.getText().toString().trim().isEmpty()){
                    if ((num.getText().toString().trim()).length() == 10){
                        String number = num.getText().toString();
                        Intent intent2 = new Intent(getApplicationContext(), OTP.class);
                        intent2.putExtra("mobile",num.getText().toString());
                        startActivity(intent2);
                    }
                    else {
                        Toast.makeText(SignUp2.this,"Please enter correct number",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SignUp2.this,"Enter mobile number",Toast.LENGTH_SHORT).show();
                }
            });
        }

    private void sendVerifaction(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private  PhoneAuthProvider.OnVerificationStateChangedCallbacks
    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            final String code =credential.getSmsCode();
            if (code!=null){
                varifycode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getApplicationContext(),"verifaction failed",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s,
                @NonNull PhoneAuthProvider.ForceResendingToken token)
        {
            super.onCodeSent(s, token);
            verificationId = s;
        }
    };

    private void varifycode(String code) {
    }
}
