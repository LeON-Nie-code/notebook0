package com.example.notebook0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forget_password extends AppCompatActivity {

    private EditText mforgetpassword;
    private Button mpassword_recover_button;
    private TextView mgobacktologin;

    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        mforgetpassword=findViewById(R.id.forgetpassword);
        mpassword_recover_button=findViewById(R.id.recoverbutton);
        mgobacktologin=findViewById(R.id.gotologin);

        firebaseAuth = FirebaseAuth.getInstance();

        mgobacktologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(forget_password.this,MainActivity.class);
                startActivity(intent);
            }
        });
        
        mpassword_recover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mforgetpassword.getText().toString().trim();
                if(mail.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Enter you mail first", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //TODO

                    firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(), "Mail Sent, You can recover your password using mail", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(forget_password.this, MainActivity.class));
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Email is wrong", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });
    }
}