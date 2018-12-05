package com.techart.reporter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.techart.reporter.constants.Constants;
import com.techart.reporter.constants.FireBaseUtils;
import com.techart.reporter.model.Comment;
import com.techart.reporter.utils.NumberUtils;
import com.techart.reporter.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mCommentList;
    private EditText mEtComment;
    private String post_key;
    private Boolean isSent;
    private String postType;
    private String postName;
    private String time;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        FireBaseUtils.mDatabaseComment.keepSynced(true);
        FireBaseUtils.mDatabaseStory.keepSynced(true);


        post_key = getIntent().getStringExtra(Constants.POST_KEY);
        postName = getIntent().getStringExtra(Constants.POST_TITLE);
        postType = getIntent().getStringExtra(Constants.POST_TYPE);
        setTitle("Comments on "+ postName);
        tvEmpty = findViewById(R.id.tv_empty);
        mCommentList = findViewById(R.id.comment_recyclerview);
        mCommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CommentActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mCommentList.setLayoutManager(linearLayoutManager);
        init();
        initCommentSection();
    }

    private void initCommentSection() {
        Query commentsQuery = FireBaseUtils.mDatabaseComment.child(post_key).orderByChild(Constants.TIME_CREATED);
        FirebaseRecyclerAdapter<Comment, CommentHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                Comment.class, R.layout.item_comment, CommentHolder.class, commentsQuery)
        {
            @Override
            protected void populateViewHolder(CommentHolder viewHolder, final Comment model, int position) {
                final String comment_key = getRef(position).getKey();
                tvEmpty.setVisibility(View.GONE);
                if (model.getAuthorUrl() != null){
                    setVisibility(model.getAuthorUrl(),viewHolder);
                }

                    viewHolder.authorTextView.setText(getResources().getString(R.string.reporter));


                if (model.getTimeCreated() != null){
                    time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }
                viewHolder.commentTextView.setText(model.getCommentText());

                if (model.getReplies() != null && model.getReplies() != 0){
                    viewHolder.tvViewReplies.setVisibility(View.VISIBLE);
                    viewHolder.tvViewReplies.setText(getString(R.string.replies, NumberUtils.setUsualPlurality(model.getReplies(),"reply")));
                    viewHolder.tvViewReplies.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent replyIntent = new Intent(CommentActivity.this,ReplyActivity.class);
                            replyIntent.putExtra(Constants.POST_KEY,post_key);
                            replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                            replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                            replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                            replyIntent.putExtra(Constants.TIME_CREATED,time);
                            replyIntent.putExtra(Constants.POST_TYPE,postType);
                            startActivity(replyIntent);
                        }
                    });
                }
                viewHolder.tvReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyIntent = new Intent(CommentActivity.this,ReplyActivity.class);
                        replyIntent.putExtra(Constants.POST_KEY,post_key);
                        replyIntent.putExtra(Constants.COMMENT_KEY,comment_key);
                        replyIntent.putExtra(Constants.POST_AUTHOR,model.getAuthor());
                        replyIntent.putExtra(Constants.COMMENT_TEXT,model.getCommentText());
                        replyIntent.putExtra(Constants.TIME_CREATED,time);
                        replyIntent.putExtra(Constants.POST_TYPE,postType);
                        startActivity(replyIntent);
                    }
                });
            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);
    }

    private void setVisibility(String url, CommentHolder viewHolder) {
        if (FireBaseUtils.getUiD() != null && FireBaseUtils.getUiD().equals(url)){
            viewHolder.commentTextView.setBackground(getResources().getDrawable(R.drawable.tv_inner_background_accent));
        }
    }

    private void init() {
        mEtComment = findViewById(R.id.et_comment);
        findViewById(R.id.iv_send).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send:
                sendComment();
        }
    }

    private void sendComment() {
        final String comment = mEtComment.getText().toString().trim();
        isSent = false;
        if (!comment.isEmpty())
        {
            final ProgressDialog progressDialog = new ProgressDialog(CommentActivity.this);
            progressDialog.setMessage("Sending comment..");
            progressDialog.setCancelable(true);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            FireBaseUtils.mDatabaseComment.child(post_key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isSent)
                    {
                        DatabaseReference newComment = FireBaseUtils.mDatabaseComment.child(post_key).push();
                        Map<String,Object> values = new HashMap<>();
                        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
                        values.put(Constants.POST_AUTHOR,FireBaseUtils.getAuthor());
                        values.put(Constants.COMMENT_TEXT,comment);
                        values.put(Constants.REPLIES,0);
                        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
                        newComment.setValue(values);
                        isSent = true;
                        progressDialog.dismiss();
                        //onCommentSent();
                        mEtComment.setText("");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(this,"Nothing to send",Toast.LENGTH_LONG ).show();
        }
    }

    //Handled by cloud functions
    /*
    private void storyCommentCount() {
        FireBaseUtils.mDatabaseStory.child(post_key).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Story story = mutableData.getValue(Story.class);
                if (story == null) {
                    return Transaction.success(mutableData);
                }
                story.setNumComments(story.getNumComments() + 1 );
                // Set value and report transaction success
                mutableData.setValue(story);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }*/

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == RESULT_OK){
            post_key = data.getStringExtra(Constants.POST_KEY);
            postName = data.getStringExtra(Constants.POST_TITLE);
            postType = data.getStringExtra(Constants.POST_TYPE);
        }
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView timeTextView;
        public TextView tvReply;
        public TextView tvViewReplies;

        public CommentHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tvAuthor);
            timeTextView = itemView.findViewById(R.id.tvTime);
            tvReply = itemView.findViewById(R.id.tvReply);
            tvViewReplies = itemView.findViewById(R.id.tv_view_replies);
            commentTextView = itemView.findViewById(R.id.tvComment);
        }
    }

}
