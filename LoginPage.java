package com.example.admin.janitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginPage extends AppCompatActivity {

    EditText loginid, passwd;
    Button login;
    String log, pas;
    DocumentReference myRef;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginid = (EditText) findViewById(R.id.loginid);
        passwd = (EditText) findViewById(R.id.passwd);
        login = (Button)findViewById(R.id.login);
        Database.init();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log = loginid.getText().toString();
                pas = passwd.getText().toString();
                myRef = Database.db.collection("JanitorLogin").document(log);
                myRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            if(pas.equals(documentSnapshot.get("pass"))==true)
                            {
                                SharedPreferences sp = getSharedPreferences(getPackageName()+"branchfile", Context.MODE_PRIVATE);
                                SharedPreferences.Editor edit = sp.edit();
                                edit.putString("branchfile",documentSnapshot.get("branch").toString());
                                edit.apply();

                                Toast.makeText(LoginPage.this,"Login successful",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(LoginPage.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                                Toast.makeText(LoginPage.this,"Incorrect password",Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(LoginPage.this,"Invalid ID",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
