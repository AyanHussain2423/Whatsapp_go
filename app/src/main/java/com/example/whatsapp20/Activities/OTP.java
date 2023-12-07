package com.example.whatsapp20.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp20.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OtpTextView;

public class OTP extends AppCompatActivity {
    Button button3;
    String otpid;
    FirebaseAuth mAuth;
    String number;
    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        OtpTextView otpView = findViewById(R.id.otp_view);
         TextView phnume = findViewById(R.id.phNumber1);
        number = getIntent().getStringExtra("mobile").toString();
        sendVerifaction(number);


       button3 = findViewById(R.id.button3);
       button3.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (otpView.getOTP().toString().isEmpty())
                   Toast.makeText(getApplicationContext(),"Blank field can not proceed ",Toast.LENGTH_LONG).show();
               else if (otpView.getOTP().toString().length()!=6) {
                   Toast.makeText(getApplicationContext()," can not proceed ",Toast.LENGTH_LONG).show();
               }
               else{
                   varifycode(otpView.getOTP().toString());

               }
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
            Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s,
                               @NonNull PhoneAuthProvider.ForceResendingToken token)
        {
            super.onCodeSent(s, token);
            verificationId = s;
            OtpTextView otpTextView = findViewById(R.id.otp_view);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);// keyboard
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0 );// keyboard forcefully opne
            otpTextView.requestFocusOTP();
        }
    };

    private void varifycode(String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signinbycredintial(credential);
    }

    private void signinbycredintial(PhoneAuthCredential credential)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Login Succesfull",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(OTP.this, SetupProfile.class);

                            startActivity(intent);
                            finishAffinity();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Login Failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
