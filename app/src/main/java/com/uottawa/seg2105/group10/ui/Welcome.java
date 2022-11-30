package com.uottawa.seg2105.group10.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uottawa.seg2105.group10.R;
import com.uottawa.seg2105.group10.backend.Cook;
import com.uottawa.seg2105.group10.ui.clientView.MealSearch;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Welcome extends AppCompatActivity {

    private TextView typeText;
    private TextView isSuspended;
    private TextView suspensionDeets;
    private FirebaseAuth mAuth;
    private FirebaseFirestore dBase;
    private DocumentReference userDoc;

    private final DocumentSnapshot[] userSnapshot = new DocumentSnapshot[1];
    private final DocumentReference[] purchaseRef = new DocumentReference[1];
    private Button logOffButt;
    private Button homepageButt;
    private ImageButton profileButt;
    private static final String TAG = "Welcome";
    private String type;


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
        suspensionDeets = findViewById(R.id.suspensionDetails);
        profileButt = findViewById(R.id.profileButt);

        // get instances of Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        dBase = FirebaseFirestore.getInstance();

        // create reference to current user document
        userDoc = dBase.collection("users").document(user.getUid());

        userDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userSnapshot[0] = task.getResult();
                // if user specific document exists,
                // set text field to display user type (Client, Cook, or Admin)
                if (userSnapshot[0].exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + userSnapshot[0].getData());
                    type = userSnapshot[0].getString("type");
                    typeText.setText(type);
                    switch(type){
                        case "Admin":
                            homepageButt.setText(R.string.adminNextButtText);
                            profileButt.setVisibility(View.GONE);
                            break;
                        case "Cook":
                            homepageButt.setText(R.string.cookNextButtText);
                            break;
                        case "Client":
                            startPurchaseStatusListener();
                            homepageButt.setText(R.string.clientNextButtText);
                            break;
                    }

                    if(userSnapshot[0].contains("isSuspended")) {
                        if(Boolean.TRUE.equals(userSnapshot[0].getBoolean("isSuspended"))){
                            isSuspended.setText(R.string.general_suspend_message);
                            userDoc.collection("userObject").document("Cook").get().addOnSuccessListener(snapshot -> {
                                Cook thisCook = snapshot.toObject(Cook.class);
                                String endDate = thisCook.getSuspensionEnd();
                                // Displaying suspension message for both indefinite and temporary
                                if (endDate == null)
                                    suspensionDeets.setText(R.string.perm_suspend_message);
                                else {
                                    String msg = "Your suspension will be lifted by " + LocalDateTime.parse(endDate).truncatedTo(ChronoUnit.HOURS);
                                    suspensionDeets.setText(msg);
                                }
                                // Suspended cooks can no longer access the full application. They only have the option to log-off!
                                homepageButt.setVisibility(View.GONE);
                                profileButt.setVisibility(View.GONE);
                            });
                        }
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

        // Logs Firebase user out and launches Main activity
        logOffButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Welcome.this, MainActivity.class));
                finish();
            }
        });

        // Sends Cook to their homepage to view their menu.
        homepageButt.setOnClickListener(view -> {
            switch (type) {
                case "Admin":
                    startActivity(new Intent(Welcome.this, AdminHome.class));
                    break;
                case "Cook":
                    startActivity(new Intent(Welcome.this, Menu.class));
                    break;
                case "Client":
                    Intent intent = new Intent(Welcome.this, MealSearch.class);
                    intent.putExtra("TYPE", "Cook");
                    startActivity(intent);
                    break;
            }
        });

        profileButt.setOnClickListener(view -> {
            Intent intent = new Intent(Welcome.this, Profile.class);
            intent.putExtra("TYPE", type);
            intent.putExtra("UID", userSnapshot[0].getId());
            startActivity(intent);
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        /*dBase.collection("purchases").orderBy("requestTime").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<DocumentSnapshot> purchases = task.getResult().getDocuments();
                    for(DocumentSnapshot snapshot:purchases){
                        if(snapshot.getString("clientUID").equals(document.getId()) || snapshot.getString("cookUID").equals(document.getId())){
                            purchaseRef[0] = snapshot.getReference();
                            return;
                        }
                    }
                }
            }
        });*/

    }

    /*private boolean startNotifications() {
        // Calling method to initialize notification channel
        createNotificationChannel();
        if (purchaseRef[0] == null) return false;
        purchaseRef[0].addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                if ((snapshot.get("status") == "PENDING") || snapshot.get("complaint") != null) return;

                Intent intent = new Intent(this, MealSearch.class); //Should be ClientHome.class but it currently dne
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CLIENT_STATUS_CHANGE")
                        .setSmallIcon(R.drawable.real_logo)
                        .setContentTitle("Status Change")
                        .setContentText("Open Mealer to see more.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
        return true;
    }*/

    public void startPurchaseStatusListener() {

        //.orderBy("docID", Query.Direction.DESCENDING).limit(1)
        if (userSnapshot[0] != null) {
            String meow = userSnapshot[0].getId();
            dBase.collectionGroup(userSnapshot[0].getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                purchaseRef[0] = queryDocumentSnapshots.getDocuments().get(0).getReference();
                createNotificationChannel();
                if (purchaseRef[0] == null) return;
                purchaseRef[0].addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        if ((snapshot.get("status") == "PENDING") || snapshot.get("complaint") != null) return;

                        Intent intent = new Intent(this, MealSearch.class); //Should be ClientHome.class but it currently dne
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CLIENT_STATUS_CHANGE")
                                .setSmallIcon(R.drawable.real_logo)
                                .setContentTitle("Status Change")
                                .setContentText("Open Mealer to see more.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });

                Log.w(TAG,  purchaseRef[0].getId().toString());
            }).addOnFailureListener(e -> {
                Log.w(TAG, "mians :(");
            });
        } else Log.w(TAG, "No purchases exist for you :(");
    }

    public boolean setPurchaseRef(DocumentReference doc) {
        if (doc == null) return false;
        purchaseRef[0] = doc;
        return true;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CLIENT_STATUS_CHANGE", "Client Purchase", importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}