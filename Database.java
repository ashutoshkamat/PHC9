package com.example.admin.janitor;

import com.google.firebase.firestore.FirebaseFirestore;

public class Database {
    static FirebaseFirestore db;

     static void init()
    {
        db=FirebaseFirestore.getInstance();
    }

}
