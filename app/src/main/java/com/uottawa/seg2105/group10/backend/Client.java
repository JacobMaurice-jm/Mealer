package com.uottawa.seg2105.group10.backend;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class Client extends User {

    private String ccNumber, ccHolderName, expiryDate, cvc;
    private DocumentSnapshot document;
    private static final String TAG = "Client.java";

    public Client(DocumentReference userDoc) {
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        //these are variables from superclass
                        firstName = document.getString("firstName");
                        lastName = document.getString("lastName");
                        email = document.getString("email");
                        password = document.getString("password");
                        address = document.getString("address");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public String getType(){return "Client";}

    // Setter method for Credit Card information
    // If CC number, expiry date, and cvc are of correct format, returns true. Otherwise, returns false.
    public boolean setCC(String num, String name, String expiry, String cvc) {
        if (expiry.matches("(?:0[1-9]|1[0-2])/[0-9]{2}") &&
            (num.length() == 16) && (cvc.length() == 3)) {
            ccNumber = num;
            ccHolderName = name;
            expiryDate = expiry;
            this.cvc = cvc;
            return true;
        }
        return false;
    }
}
