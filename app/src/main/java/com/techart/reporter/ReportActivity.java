package com.techart.reporter;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techart.reporter.constants.Constants;
import com.techart.reporter.constants.FireBaseUtils;
import com.techart.reporter.utils.EditorUtils;
import com.techart.reporter.utils.ImageUtils;
import com.techart.reporter.utils.UploadUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;
import static com.techart.reporter.utils.ImageUtils.hasPermissions;

/**
 * Created by Kelvin on 30/07/2017.
 * Handles actions related to asking issueTitle
 */

public class ReportActivity extends AppCompatActivity {
    //string resources
    private String issueTitle;
    private String issueType;
    private String province;
    private String issueDescription;
    private String realPath;


    //ui components
    private TextView tvCropError;
    private Spinner spIssue;
    private Spinner spProvince;
    private ImageView ibSample;
    private EditText etQuestion;
    private EditText etDescription;
    StorageReference filePath;
    String userUrl;

    //image
    private static final int GALLERY_REQUEST = 1;
    private Uri uri;

    //Permission
    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        etQuestion = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);

        ibSample = findViewById(R.id.ib_item);

        //Issue type
        final String[] issues = getResources().getStringArray(R.array.issues);
        spIssue = findViewById(R.id.sp_issue);
        ArrayAdapter<String> issuesAdapter = new ArrayAdapter<String>(ReportActivity.this, R.layout.tv_dropdown, issues);
        issuesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issuesAdapter.notifyDataSetChanged();

        spIssue.setAdapter(issuesAdapter);
        spIssue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                issueType = issues[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Province
        final String[] provinces = getResources().getStringArray(R.array.province);
        spProvince = findViewById(R.id.sp_province);
        ArrayAdapter<String> provincesAdapter = new ArrayAdapter<String>(ReportActivity.this, R.layout.tv_dropdown, provinces);
        provincesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provincesAdapter.notifyDataSetChanged();

        spProvince.setAdapter(provincesAdapter);
        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                province = provinces[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ibSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    onGetPermission();
                }  else {
                    Intent imageIntent = new Intent();
                    imageIntent.setType("image/*");
                    imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(imageIntent,GALLERY_REQUEST);
                }
            }
        });
    }


    /**
     * requests for permission in android >= 23
     */
    @TargetApi(23)
    private void onGetPermission() {
        // only for MarshMallow and newer versions
        if(!hasPermissions(this, PERMISSIONS)){
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                onPermissionDenied();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        }
    }

    /**
     * Trigger gallery selection for a photo
     * @param requestCode
     * @param permissions permissions to be requested
     * @param grantResults granted results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        } else {
            //do something like displaying a message that he did not allow the app to access gallery and you wont be able to let him select from gallery
            onPermissionDenied();
        }
    }

    /**
     * Displays when permission is denied
     */
    private void onPermissionDenied() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            ActivityCompat.requestPermissions(ReportActivity.this, PERMISSIONS, PERMISSION_ALL);
                        }
                        if (button == DialogInterface.BUTTON_NEGATIVE) {
                            dialog.dismiss();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("YOU NEED TO ALLOW ACCESS TO MEDIA STORAGE")
                .setMessage("Without this permission you can not upload an image")
                .setPositiveButton("ALLOW", dialogClickListener)
                .setNegativeButton("DENY", dialogClickListener)
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     * @param menu menu resource to be inflated
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_post:
                if (validate()){
                    upload();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sends information to database
     * @param downloadImageUrl url of upload image
     */
    private void sendPost(String downloadImageUrl,String url) {
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.STORY_TITLE, issueTitle.toUpperCase());
        values.put(Constants.STORY_DESCRIPTION,issueDescription);
        values.put(Constants.STORY_CATEGORY, issueType);
        values.put(Constants.PROVINCE, province);
        String status = "Pending";
        values.put(Constants.STORY_STATUS, status);
        values.put(Constants.NUM_COMMENTS,0);
        values.put(Constants.NUM_VIEWS,0);
        values.put(Constants.AUTHOR_URL,FireBaseUtils.getUiD());
        values.put("imageUrl",downloadImageUrl);
        values.put(Constants.TIME_CREATED, ServerValue.TIMESTAMP);
        FireBaseUtils.mDatabaseStory.child(url).setValue(values);
        Toast.makeText(ReportActivity.this, "Item Posted",LENGTH_LONG).show();
    }

    /**
     * Uploads image to cloud storage
     */
    private void upload() {
        userUrl = FireBaseUtils.getAuthor();
        final String url = FireBaseUtils.mDatabaseStory.push().getKey();
        final ProgressDialog mProgress = new ProgressDialog(ReportActivity.this);
        mProgress.setMessage("Uploading photo, please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();
        filePath = FireBaseUtils.mStorageReports.child("Reports" + "/" + issueType +  url);
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        //uploading the image
        UploadTask uploadTask = filePath.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    sendPost(task.getResult().toString(),url);
                    mProgress.dismiss();
                    finish();

                } else {
                    // Handle failures
                    UploadUtils.makeNotification("Image upload failed",ReportActivity.this);
                }
            }
        });

    }

    /**
     * validates the entries before submission
     * @return true if successful
     */
    private boolean validate(){
        issueTitle = etQuestion.getText().toString().trim();
        issueDescription = etDescription.getText().toString().trim();
        tvCropError = (TextView) spIssue.getSelectedView();
        return  EditorUtils.dropDownValidator(issueType,getResources().getString(R.string.default_crop),tvCropError) &&
                EditorUtils.editTextValidator(issueTitle,etQuestion,"Type in the title") &&
                EditorUtils.editTextValidator(issueDescription,etDescription,"Type in the description") &&
                EditorUtils.imagePathValdator(this,realPath);
    }

    /**
     * Called upon selecting an image
     * @param requestCode
     * @param resultCode was operation successful or not
     * @param data data returned from the operation
     */
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null ) {
            uri = data.getData();
            realPath = ImageUtils.getRealPathFromUrl(this, uri);
            Uri uriFromPath = Uri.fromFile(new File(realPath));
            setImage(ibSample,uriFromPath);
        }
    }

    /**
     * inflates image into the image view
     * @param image component into which image will be inflated
     * @param uriFromPath uri of image to be inflated
     */
    private void setImage(ImageView image,Uri uriFromPath) {
        Glide.with(this)
                .load(uriFromPath)
                .centerCrop()
                .into(image);
    }

}