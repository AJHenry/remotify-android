package xyz.andrewh.remotify;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class NotificationServiceListener extends NotificationListenerService {

    private DatabaseReference notificationRef, packageRef, connectionRef;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private final String TAG = "NOTIFICATIONLISTENER";
    private ArrayList<String> applicationList;
    private DatabaseHelper databaseHelper;
    private boolean clientIsConnected = false;
    private StorageReference storageRef;

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "xyz.andrewh.remotify";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_done_white_24dp)
                        .setContentTitle("Remotify Notification Listener")
                        .setContentText(messageBody)
                        .setOngoing(true)
                        .setPriority(Notification.PRIORITY_MIN)
                        .setVisibility(Notification.VISIBILITY_SECRET)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Remotify Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(858, notificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        notificationRef = database.getReference("notification");
        packageRef = database.getReference("package");
        connectionRef = database.getReference("onlinestate");


        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference();


        Log.d(TAG, "STARTED SERVICE");

        //Get the database
        databaseHelper = new DatabaseHelper(this);

        mAuth = FirebaseAuth.getInstance();

        applicationList = readApplicationList();

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.wtf(TAG, "User not logged in");
        }


        //Establish connection listener
        connectionRef.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Value changed for online state old: " + clientIsConnected + " new: " + dataSnapshot.getValue(Boolean.class));
                clientIsConnected = dataSnapshot.getValue(Boolean.class);

                if (clientIsConnected) {
                    sendNotification("Browser connected");
                } else {
                    sendNotification("Browser not connected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        //Need to establish the connection listener

    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //Don't bother doing any work if the user is not online
        if (!clientIsConnected) {
            return;
        }


        //Application Icon
        String packageName = sbn.getPackageName();
        String encodedPackageName = packageName.replace(".", "-");
        Notification notification = sbn.getNotification();
        int id = sbn.getId();
        String tag = "";
        try {
            tag = sbn.getTag().toString();
        } catch (NullPointerException e) {
        }
        String title = notification.extras.getString("android.title", "");
        String text = notification.extras.getString("android.text", "");
        String bigText = notification.extras.getString("android.bigText", "");
        Drawable icon = null;
        Drawable largeIcon = null;
        try {
            icon = getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Large Icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Icon largeIconIcon = notification.getLargeIcon();
            if (largeIconIcon != null) {
                largeIcon = largeIconIcon.loadDrawable(this);
            }
        }


        Log.d(TAG, title);
        Log.d(TAG, text);
        Log.d(TAG, currentUser.getUid());

        if (checkNotificationSource(packageName)) {

            //First
            //Upload icon image
            appImageUpload(icon, encodedPackageName);

            DatabaseReference uniqueNotificationRef = notificationRef.child(currentUser.getUid()).child(encodedPackageName).push();
            String notificationId = uniqueNotificationRef.getKey();


            NotificationModel notify = new NotificationModel();
            notify.setText(text);
            notify.setBigText(bigText);
            notify.setTitle(title);
            notify.setPackageName(packageName);
            notify.setId(id);
            notify.setTag(tag);
            notify.setNotificationId(notificationId);
            notify.setTimestamp(System.currentTimeMillis());

            //Check to see if the notification has a large icon
            //Add it to firebase storage if it does
            if (largeIcon != null) {
                notify.setHasLargeIcon(true);
                notificationImageUpload(largeIcon, encodedPackageName, notificationId);
            } else {
                notify.setHasLargeIcon(false);
            }


            if (bigText != null || bigText.length() == 0) {
                notify.setBigNotification(false);
            } else {
                notify.setBigNotification(true);
            }


            //Set the notification
            uniqueNotificationRef.setValue(notify)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Text Write successful");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Text Write failure " + e);
                        }
                    });


            Log.d(TAG, "Notification from " + packageName);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        //The client isn't connect, don't bother
        if(!clientIsConnected){
            return;
        }

        //Get the tag and id to grab it from firebase
        //Application Icon
        String packageName = sbn.getPackageName();
        String encodedPackageName = packageName.replace(".", "-");
        final int id = sbn.getId();
        String tag = "";
        try {
            tag = sbn.getTag().toString();
        } catch (NullPointerException e) {
        }

        final DatabaseReference packageRef = notificationRef.child(currentUser.getUid()).child(encodedPackageName);

        Query uniqueNotificationRef = packageRef.orderByChild("tag").equalTo(tag);
        final String finalTag = tag;
        uniqueNotificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Found notifications matching tag "+ finalTag);
                for(DataSnapshot notification : dataSnapshot.getChildren()){
                    Log.d(TAG, "Found 1 "+notification);
                    NotificationModel checkNotification = notification.getValue(NotificationModel.class);
                    if(checkNotification.getId() == id){

                        //Now we need to remove it
                        packageRef.child(checkNotification.getNotificationId()).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "Successfully removed notification");
                                    }
                                });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private boolean checkNotificationSource(String packageName) {
        return applicationList.contains(packageName);
    }

    private ArrayList<String> readApplicationList() {
        ArrayList<String> appList = databaseHelper.findAllPackages();
        if (appList.isEmpty()) {
            Log.d(TAG, "App list is empty");
        }
        for (String s : appList) {
            Log.d(TAG, s);
        }
        return appList;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "service ended");
        super.onDestroy();

    }

    private void appImageUpload(final Drawable icon, final String encodedPackageName) {

        final StorageReference imageRef = storageRef.child("images").child(currentUser.getUid()).child(encodedPackageName + ".png");

        //Let's first check to see if the image exists
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "Application icon already exists");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Application icon does not exist yet");

                //File upload
                try {
                    if (icon != null) {
                        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] data = baos.toByteArray();


                        UploadTask uploadTask = imageRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d(TAG, "Failed to upload icon image ");
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "Successfully uploaded image");
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void notificationImageUpload(Drawable icon, String encodedPackageName, String notificationId) {
        //Icon
        try {
            if (icon != null) {
                Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] data = baos.toByteArray();

                StorageReference imageRef = storageRef.child("images").child(currentUser.getUid()).child(encodedPackageName).child(notificationId + ".png");

                UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "Failed to upload icon image ");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Successfully uploaded image");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}