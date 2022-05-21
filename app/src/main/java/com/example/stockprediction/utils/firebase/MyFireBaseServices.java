package com.example.stockprediction.utils.firebase;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.stockprediction.activites.SplashActivity;
import com.example.stockprediction.objects.Prediction;
import com.example.stockprediction.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class MyFireBaseServices {

    private final String DB_URL = "https://stockprediction-b3afb-default-rtdb.europe-west1.firebasedatabase.app/";
    // ################# Firebase keys #################:
    private final String MY_USERS = "users";
    private final String PREDICTIONS = "predictions";
    // ################# Firebase cloud messaging keys #################:
    private final String PREDICTION_NOTIFICATIONS_TOPIC = "notification";

    // ################# CallBacks #################:
    public interface FB_Request_Callback<T> {
        void OnSuccess(T result);

        void OnFailure(Exception e);
    }

    public interface FBImageReady_Callback {
        void imageUriCallback(String imageUri);
    }

    // #############################################
    private FirebaseUser firebaseUser;   // Current login user
    private static MyFireBaseServices instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;


    public static MyFireBaseServices getInstance() {
        return instance;
    }

    private MyFireBaseServices() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance(DB_URL);
    }

    public static void Init() {
        if (instance == null) {
            Log.d("pttt", "Init: MyFireBaseServices");
            instance = new MyFireBaseServices();
        }
    }

    // AUTH AND USER METHODS:
    public boolean login() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        boolean loginSuccess;
        if (firebaseUser == null) {
            loginSuccess = false;
            Log.d("pttt", "login: failed");
        } else {
            setFirebaseUser(firebaseUser);
            Log.d("pttt", "Uid = " + firebaseUser.getUid()
                    + "\nDisplayName = " + firebaseUser.getDisplayName()
                    + "\nEmail = " + firebaseUser.getEmail()
                    + "\nPhoneNumber = " + firebaseUser.getPhoneNumber()
                    + "\nPhotoUrl = " + firebaseUser.getPhotoUrl());
            loginSuccess = true;
        }
        return loginSuccess;
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public void signOut(Activity source) {
        this.firebaseAuth.signOut();
        Intent intent = new Intent(source, SplashActivity.class);
        source.startActivity(intent);
        source.finish();
    }

    public void saveUserToFireBase(User user) {
        if (firebaseUser.getUid() != null) {
            saveObject(MY_USERS, user.getUid(), user);
            Log.d("pttt", "saveUserToFireBase: ");
        }
    }

    private <T> void saveObject(String preferenceKey, String key, T obj) {
        DatabaseReference myRef = database.getReference(preferenceKey);
        myRef.child(key).setValue(obj);
    }


    private <T> void saveJsonObject(String preferenceKey, String key, T obj) {
        Gson gson = new Gson();
        String objJson = gson.toJson(obj);
        DatabaseReference myRef = database.getReference(preferenceKey);
        myRef.child(key).setValue(objJson);
    }

    private <T> T loadJsonObject(String objJson, Class<T> classType) {
        Gson gson = new Gson();
        T object = gson.fromJson(objJson, classType);
        return object;
    }

    private void loadObjectFromFireBase(String preferenceKey, String key, ValueEventListener valueEventListener) {
        DatabaseReference myRef = database.getReference(preferenceKey);
        myRef.child(key).addListenerForSingleValueEvent(valueEventListener);
    }

    private void listenObjectFromFireBase(String preferenceKey, String key, ValueEventListener valueEventListener) {
        DatabaseReference myRef = database.getReference(preferenceKey);
        myRef.child(key).addValueEventListener(valueEventListener);
    }


    public void loadUserFromFireBase(FB_Request_Callback<User> valueEventListener) {
        if(firebaseUser != null)
            loadObjectFromFireBase(MY_USERS, firebaseUser.getUid(), new MyValueEventListener<User>(User.class, valueEventListener));
    }

    // Image load\store methods:
    public void savePhotoToStorage(String key, Uri uri, FBImageReady_Callback FBImageReady_Callback) {
        if (firebaseUser.getUid() != null) {
            StorageReference storageRef = storage.getInstance().getReference();
            StorageReference mountainImagesRef = storageRef.child("image/" + key);
            UploadTask uploadTask = mountainImagesRef.putFile(uri);
            uploadTask.addOnFailureListener(e -> {
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d("pttt", "savePhotoToStorageUri: successed ");
                getImageUri(taskSnapshot, FBImageReady_Callback);
            });
        }
    }

    private void getImageUri(UploadTask.TaskSnapshot taskSnapshot, FBImageReady_Callback FBImageReady_Callback) {
        if (taskSnapshot.getMetadata() != null) {
            if (taskSnapshot.getMetadata().getReference() != null) {
                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                result.addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d("pttt", "imageUrl= " + imageUrl);
                    if (FBImageReady_Callback != null) {
                        FBImageReady_Callback.imageUriCallback(imageUrl);
                    }
                });
            }
        }
    }

    public User updateUserPhotoInFireStore(String userPhoto, User user) {
        user.setImageUrl(userPhoto);
        saveUserToFireBase(user);
        return user;
    }

    public void getUserPhotoFromFireStore(FBImageReady_Callback FBImageReady_Callback) {
        loadUserFromFireBase(new FB_Request_Callback<User>() {
            @Override
            public void OnSuccess(User result) {
                if (FBImageReady_Callback != null && result != null) {
                    FBImageReady_Callback.imageUriCallback(result.getImageUrl());
                }
            }

            @Override
            public void OnFailure(Exception e) {
                Log.d("pttt", "OnFailure: exception: " + e);
            }
        });
    }

    public HashMap<String, ArrayList<Prediction>> listenPredictions(FB_Request_Callback<HashMap<String, ArrayList<Prediction>>> fb_request_callback) {
        HashMap<String, ArrayList<Prediction>> dailyPredictions = new HashMap<String, ArrayList<Prediction>>();
        DatabaseReference myRef = database.getReference(PREDICTIONS);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    //Log.d("my_firebase_services", "onDataChange: snapshot= "+snapshot);
                    for (DataSnapshot data : snapshot.getChildren()) { // days
                        if (data != null) {
                            String key = data.getKey();
                            ArrayList<Prediction> predictions = new ArrayList<Prediction>();
                            for (DataSnapshot pridictionsSnapshot : data.getChildren()) { // index of predictions
                                Prediction prediction = new Prediction();
                                if(pridictionsSnapshot.child("actual").getValue() != null)
                                    prediction.setActualValue(Double.parseDouble(pridictionsSnapshot.child("actual").getValue().toString()));
                                prediction.setDependencies(pridictionsSnapshot.child("dependencies"));
                                prediction.setPoints(Double.parseDouble(pridictionsSnapshot.child("points").getValue().toString()));
                                prediction.setPrecision(Double.parseDouble(pridictionsSnapshot.child("precision").getValue().toString()));
                                prediction.setTarget(pridictionsSnapshot.child("target"));
                                predictions.add(prediction);

                            }
                            dailyPredictions.put(key, predictions);
                        }
                    }
                }
                Log.d("my_firebase_services", "onDataChange: snapshot= "+dailyPredictions);
                fb_request_callback.OnSuccess(dailyPredictions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fb_request_callback.OnFailure(error.toException());
            }
        });
        return dailyPredictions;
    }

    // Cloud messaging:
    public void registerPredictionTopic(OnCompleteListener onCompleteListener) {
        Log.d("my_firebase_services", "registerPredictionTopic: "+PREDICTION_NOTIFICATIONS_TOPIC);
        if(onCompleteListener != null) {
            registerTopic(PREDICTION_NOTIFICATIONS_TOPIC,onCompleteListener);
        } else {
            registerTopic(PREDICTION_NOTIFICATIONS_TOPIC);
        }

    }
    public void unregisterPredictionTopic() {
        Log.d("my_firebase_services", "unregisterPredictionTopic: "+PREDICTION_NOTIFICATIONS_TOPIC);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(PREDICTION_NOTIFICATIONS_TOPIC);
    }

    private void registerTopic(String topic, OnCompleteListener onCompleteListener) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(onCompleteListener);
    }
    private void registerTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
    private void sendMessage(String topic, OnCompleteListener onCompleteListener) {
//        Message message = Message.builder()
//                .putData("score", "850")
//                .putData("time", "2:45")
//                .setTopic(topic)
//                .build();
    }


    private class MyValueEventListener<T> implements com.google.firebase.database.ValueEventListener {
        private final Class<T> classType;
        private FB_Request_Callback fb_request_callback;
        public MyValueEventListener(Class<T> classType,FB_Request_Callback fb_request_callback) {
            this.classType = classType;
            this.fb_request_callback = fb_request_callback;
        }

        public Class<T> getType() {
            return classType;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot != null) {
                try {
                    T value = snapshot.getValue(classType);
                    Log.d("pttt", "Value is: "+ value);
                    fb_request_callback.OnSuccess(value);
                } catch (Exception exception) {
                    Log.e("pttt", "Failed to get value!, snapshot: " + snapshot + "\nException = "+exception);
                    fb_request_callback.OnFailure(exception);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            fb_request_callback.OnFailure(error.toException());
        }
    }
}