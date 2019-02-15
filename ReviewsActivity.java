package com.example.admin.janitor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReviewsActivity extends AppCompatActivity {

    String feedbacks[]=new String[10];
    int count=0;
    String branch = new String();
    ArrayAdapter<String> adapter;
    ListView lw;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_layout);
        lw = findViewById(R.id.list_view);

        for(int i=0; i<10; i++)
        {
            feedbacks[i]=new String("");
        }
        adapter=new ArrayAdapter<String>(this,R.layout.subitem,R.id.subitemview,feedbacks);
        lw.setAdapter(adapter);

        branch = getSharedPreferences(getPackageName() + "branchfile", Context.MODE_PRIVATE).getString("branchfile", "null");


        FirebaseDatabase.getInstance().getReference().child(branch).child("Feedback").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                count=0;
                for(DataSnapshot ds:  dataSnapshot.getChildren())
                {
                    feedbacks[count]=(count+1)+")  "+new String(ds.child("add_fdbck").getValue().toString());
                    ++count;
                }
                adapter.notifyDataSetChanged();

                FirebaseDatabase.getInstance().getReference().child(branch).child("Feedback").removeEventListener(this);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }



}
