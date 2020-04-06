package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String fileUrl;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icf);

        // Get a non-default Storage bucket
        storage = FirebaseStorage.getInstance("gs://clinical-trials-772d5.appspot.com");
        storageRef = FirebaseStorage.getInstance().getReference();

        if (DEBUG) Log.d(TAG, storage.toString());
        if (DEBUG) Log.d(TAG, storageRef.toString());

        ActivityCompat.requestPermissions(IcfActivity.this, PERMISSIONS, 112);

        try {
            getFileFromFirebase();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                if (DEBUG) Log.d(TAG, fileName + " : " + fileUrl);

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
        if (DEBUG) Log.v(TAG, "view() Method invoked ");

        if (!hasPermissions(IcfActivity.this, PERMISSIONS)) {

            if (DEBUG) Log.v(TAG, "download() Method DON'T HAVE PERMISSIONS ");
            Toast.makeText(getApplicationContext(), "You don't have read access !", Toast.LENGTH_LONG).show();
        } else {

            File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File pdfFile = new File(d, fileName);
            if (DEBUG) Log.v(TAG, "view() Method pdfFile " + pdfFile.getAbsolutePath());

            Uri path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            if (DEBUG) Log.v(TAG, "view() Method path " + path);

            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(IcfActivity.this, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
            }
        }
        if (DEBUG) Log.v(TAG, "view() Method completed ");
    }

    public void downloadFile() {
        if (DEBUG) Log.v(TAG, "download() Method invoked ");

        if (!hasPermissions(IcfActivity.this, PERMISSIONS)) {
            if (DEBUG) Log.v(TAG, "download() Method DON'T HAVE PERMISSIONS ");
            Toast.makeText(getApplicationContext(), "You don't have write access !", Toast.LENGTH_LONG).show();
        } else {
            if (DEBUG) Log.v(TAG, "download() Method HAVE PERMISSIONS ");

            //new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");
            new DownloadFile().execute(fileUrl, fileName);
        }
        if (DEBUG) Log.v(TAG, "download() Method completed ");
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            if (DEBUG) Log.v(TAG, "doInBackground() Method invoked ");

            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(folder, fileName);

            if (DEBUG) Log.v(TAG, "doInBackground() pdfFile invoked " + pdfFile.getAbsolutePath());
            if (DEBUG) Log.v(TAG, "doInBackground() pdfFile invoked " + pdfFile.getAbsoluteFile());

            try {
                pdfFile.createNewFile();
                if (DEBUG) Log.v(TAG, "doInBackground() file created" + pdfFile);
            } catch (IOException e) {
                e.printStackTrace();
                if (DEBUG) Log.e(TAG, "doInBackground() error" + e.getMessage());
                if (DEBUG) Log.e(TAG, "doInBackground() error" + Arrays.toString(e.getStackTrace()));
            }

            FileDownloader.downloadFile(fileUrl, pdfFile);
            if (DEBUG) Log.v(TAG, "doInBackground() file download completed");

            viewPDF();

            return null;
        }

        protected void onPostExecute(Long result) {
            if (DEBUG) Log.d(TAG, "Downloaded " + result + " bytes");
        }
    }
}
