package com.techart.reporter.constants;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.techart.reporter.R;
import com.techart.reporter.model.Story;

import java.util.HashMap;
import java.util.Map;

/**
 * Has com.techart.reporter.constants for Fire base variable names
 * Created by Kelvin on 11/09/2017.
 */

public final class FireBaseUtils {
   public static DatabaseReference mDatabaseStory = FirebaseDatabase.getInstance().getReference().child(Constants.STORY_KEY);
   public static DatabaseReference mDatabaseComment = FirebaseDatabase.getInstance().getReference().child(Constants.COMMENTS_KEY);
   public static DatabaseReference mDatabaseReplies = FirebaseDatabase.getInstance().getReference().child(Constants.REPLIES_KEY);
   public static DatabaseReference mDatabaseViews = FirebaseDatabase.getInstance().getReference().child(Constants.VIEWS_KEY);
   public static DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child(Constants.USERS);
   public static FirebaseAuth mAuth  = FirebaseAuth.getInstance();

    public static StorageReference mStorageReports = FirebaseStorage.getInstance().getReference();


    private FireBaseUtils()  {

    }

    public static void subscribeTopic(final String postKey) {
        FirebaseMessaging.getInstance().subscribeToTopic(postKey);
    }

    @NonNull
    public static String getAuthor(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.isAnonymous()) {
            return "Reporter";
        } else {
            return user.getDisplayName();
        }
    }


    @NonNull
    public static String getUiD() {
        return mAuth.getCurrentUser().getUid();
    }



    public static void setPostViewed(final String post_key, final ImageView btViewed)
    {
        mDatabaseViews.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (FireBaseUtils.mAuth.getCurrentUser() != null && dataSnapshot.child(FireBaseUtils.getUiD()).hasChild(Constants.AUTHOR_URL)) {
                    btViewed.setImageResource(R.drawable.ic_visibility_24px);
                } else {
                    btViewed.setImageResource(R.drawable.ic_visibility_24px);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





    public static void updateStatus(String status, String post_key) {
        mDatabaseStory.child(post_key).child(Constants.STORY_STATUS).setValue(status);
    }

    public static void updateCategory(String category, String post_key) {
        mDatabaseStory.child(post_key).child(Constants.STORY_CATEGORY).setValue(category);
    }



     public static void onStoryViewed(String post_key) {
            mDatabaseStory.child(post_key).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Story story = mutableData.getValue(Story.class);
                    if (story == null) {
                        return Transaction.success(mutableData);
                    }
                    story.setNumViews(story.getNumViews() + 1 );
                    // Set value and report transaction success
                    mutableData.setValue(story);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                }
            });
     }

    public static void addStoryView(Story model, String post_key) {
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.AUTHOR_URL,mAuth.getCurrentUser().getUid());
        values.put(Constants.USER, FireBaseUtils.getAuthor());
        values.put(Constants.STORY_TITLE, model.getTitle());
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        mDatabaseViews.child(post_key).child(FireBaseUtils.getUiD()).setValue(values);
    }

    public static void deleteStory(final String post_key)
    {
        mDatabaseStory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(post_key))  {
                    mDatabaseStory.child(post_key).removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    public static void deleteComment(final String post_key)
    {
        mDatabaseComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(post_key).hasChild(Constants.MESSAGE))  {
                    mDatabaseComment.child(post_key).removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
