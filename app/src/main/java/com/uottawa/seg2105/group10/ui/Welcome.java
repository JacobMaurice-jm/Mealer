package com.uottawa.seg2105.group10.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uottawa.seg2105.group10.R;
import com.uottawa.seg2105.group10.backend.Cook;

public class Welcome extends AppCompatActivity {

    private TextView typeText;
    private TextView isSuspended;
    private FirebaseAuth mAuth;
    private FirebaseFirestore dBase;
        private DocumentSnapshot document;
    private Button logOffButt;
    private Button homepageButt;
    private static final String TAG = "Welcome";

    @Override
    // Turns off the android back button => User cannot go back to login page unless logged out
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load welcome activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // initialize TextView and Button
        typeText = findViewById(R.id.userTypeText);
        logOffButt = findViewById(R.id.logOffButt);
        homepageButt = findViewById(R.id.homepageButt);
        isSuspended = findViewById(R.id.isSuspended);

        // get instances of Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        dBase = FirebaseFirestore.getInstance();


        // create reference to current user document
        DocumentReference userDoc = dBase.collection("users").document(user.getUid());
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    document = task.getResult();
                    // if user specific document exists,
                    // set text field to display user type (Client, Cook, or Admin)
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        typeText.setText(document.getString("type"));

                        if(document.contains("isSuspended")) {
                            if(Boolean.TRUE.equals(document.getBoolean("isSuspended"))){

                                // I created additional string resources: temp_suspend_message, and perm_suspend_message
                                isSuspended.setText(R.string.general_suspend_message);

                                Cook thisCook = (Cook) document.get("user");
                                String endDate = thisCook.getSuspensionEnd(); // TODO: display this mf

                            }
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        // Logs Firebase user out and launches Main activity
        logOffButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Welcome.this, MainActivity.class));

            }

        });

        homepageButt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                DocumentReference docRef = dBase.collection("users").document(user.getUid());
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                        String type = documentSnapshot.getString("type");
                        if(type == "Admin"){
                            startActivity(new Intent(Welcome.this, AdminHome.class));
                        }
                        else{
                            startActivity(new Intent(Welcome.this, Menu.class));
                        }
                });

        }

    });

        homepageButt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                DocumentReference docRef = dBase.collection("users").document(user.getUid());
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    String type = documentSnapshot.getString("type");
                    if(type == "Admin"){
                        startActivity(new Intent(Welcome.this, AdminHome.class));
                    }
                    else{
                        startActivity(new Intent(Welcome.this, Menu.class));
                    }
                });

            }

        });
    }
}