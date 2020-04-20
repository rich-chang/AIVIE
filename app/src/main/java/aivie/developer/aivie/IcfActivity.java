package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import aivie.developer.aivie.util.Constant;
import aivie.developer.aivie.util.FileDownloader;

public class IcfActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String fileUrl;
    private String fileName;
    private String userId;
    private Button reviewICF;
    private Button signICF;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icf);

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserID");

        reviewICF = findViewById(R.id.buttonReviewICF);
        signICF = findViewById(R.id.buttonSignICF);
        progressBar = findViewById(R.id.progressBarConfirm);

        // Implement customized string
        TextView textViewLogout = findViewById(R.id.textViewLogout);
        SpannableString sp1 = new SpannableString("Log out");
        sp1.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp1.length(), 0);
        textViewLogout.setText(sp1);
    }

    public void logOut(View view) {

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void reviewICF(View view) {

        reviewICF.setEnabled(false);
        signICF.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Get a non-default Storage bucket
        storage = FirebaseStorage.getInstance(Constant.FIREBASE_STORAGE_INST);
        storageRef = FirebaseStorage.getInstance().getReference();

        if (Constant.DEBUG) Log.d(Constant.TAG, storage.toString());
        if (Constant.DEBUG) Log.d(Constant.TAG, storageRef.toString());

        ActivityCompat.requestPermissions(IcfActivity.this, PERMISSIONS, 112);

        try {
            getFileFromFirebase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void signICF(View view) {

        reviewICF.setEnabled(false);
        signICF.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Intent intent = new Intent(getApplicationContext(), SignatureActivity.class);
        intent.putExtra("UserID", userId);
        startActivity(intent);

        progressBar.setVisibility(View.GONE);
        reviewICF.setEnabled(true);
        signICF.setEnabled(true);
    }

    private void getFileFromFirebase() throws IOException {

        StorageReference icfFolderRef = storageRef.child("ICF");
        StorageReference icfdocRef = storageRef.child("ICF/ICF0001.pdf");

        fileName = icfdocRef.getName();

        icfdocRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Got the download URL for 'users/me/profile.png'
                fileUrl = uri.toString();
                if (Constant.DEBUG) Log.d(Constant.TAG, fileName + " : " + fileUrl);

                downloadFile();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void viewPDF() {
        if (Constant.DEBUG) Log.v(Constant.TAG, "view() Method invoked ");

        if (!hasPermissions(IcfActivity.this, PERMISSIONS)) {

            if (Constant.DEBUG) Log.v(Constant.TAG, "download() Method DON'T HAVE PERMISSIONS ");
            Toast.makeText(getApplicationContext(), "You don't have read access !", Toast.LENGTH_LONG).show();
        } else {

            File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File pdfFile = new File(d, fileName);
            if (Constant.DEBUG) Log.v(Constant.TAG, "view() Method pdfFile " + pdfFile.getAbsolutePath());

            Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            if (Constant.DEBUG) Log.v(Constant.TAG, "view() Method path " + path);

            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {

                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(IcfActivity.this, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Your code
                    reviewICF.setEnabled(true);
                    signICF.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
        if (Constant.DEBUG) Log.v(Constant.TAG, "view() Method completed ");
    }

    public void downloadFile() {
        if (Constant.DEBUG) Log.v(Constant.TAG, "download() Method invoked ");

        if (!hasPermissions(IcfActivity.this, PERMISSIONS)) {
            if (Constant.DEBUG) Log.v(Constant.TAG, "download() Method DON'T HAVE PERMISSIONS ");
            Toast.makeText(getApplicationContext(), "You don't have write access !", Toast.LENGTH_LONG).show();

            progressBar.setVisibility(View.GONE);
            reviewICF.setEnabled(true);
            signICF.setEnabled(true);
        } else {
            if (Constant.DEBUG) Log.v(Constant.TAG, "download() Method HAVE PERMISSIONS ");

            //new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");
            new DownloadFile().execute(fileUrl, fileName);
        }
        if (Constant.DEBUG) Log.v(Constant.TAG, "download() Method completed ");
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            if (Constant.DEBUG) Log.v(Constant.TAG, "doInBackground() Method invoked ");

            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(folder, fileName);

            if (Constant.DEBUG) Log.v(Constant.TAG, "doInBackground() pdfFile invoked " + pdfFile.getAbsolutePath());
            if (Constant.DEBUG) Log.v(Constant.TAG, "doInBackground() pdfFile invoked " + pdfFile.getAbsoluteFile());

            try {
                pdfFile.createNewFile();
                if (Constant.DEBUG) Log.v(Constant.TAG, "doInBackground() file created" + pdfFile);
            } catch (IOException e) {
                e.printStackTrace();
                if (Constant.DEBUG) Log.e(Constant.TAG, "doInBackground() error" + e.getMessage());
                if (Constant.DEBUG) Log.e(Constant.TAG, "doInBackground() error" + Arrays.toString(e.getStackTrace()));
            }

            FileDownloader.downloadFile(fileUrl, pdfFile);
            if (Constant.DEBUG) Log.v(Constant.TAG, "doInBackground() file download completed");

            viewPDF();

            return null;
        }

        protected void onPostExecute(Long result) {
            if (Constant.DEBUG) Log.d(Constant.TAG, "Downloaded " + result + " bytes");
        }
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true); // disable going back to the MainActivity
    }
}
