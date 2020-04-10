package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SignatureActivity extends AppCompatActivity {

    private SignaturePad mSignaturePad;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Button mClearButton;
    private Button mConfirmButton;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        storage = FirebaseStorage.getInstance(Constant.FIREBASE_STORAGE_INST);
        storageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserID");

        mClearButton = findViewById(R.id.clear_button);
        mConfirmButton = findViewById(R.id.confirm_signed_button);
        mSignaturePad = findViewById(R.id.signature_pad);

        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                //Toast.makeText(MainActivity.this, "OnStartSigning", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSigned() {
                mConfirmButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onClear() {
                mConfirmButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }
        });

        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSignaturePad.clear();
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                if (saveJpgSignatureToExtStorage(signatureBitmap)) {
                    //Toast.makeText(MainActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                    if(Constant.DEBUG) Log.d(Constant.TAG, "Signature saved!");

                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    //Toast.makeText(MainActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean saveJpgSignatureToExtStorage(Bitmap signature) {

        boolean result = false;

        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        try {
            File photo = new File(folder, String.format("Signature_%s.jpg", userId));
            saveBitmapToJPG(signature, photo);
            //scanMediaFile(photo);
            updateFileToSirebase(photo);

            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {

        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    private void updateFileToSirebase(File sourceFile) {

        Uri file = Uri.fromFile(sourceFile);
        String targetFile = getString(R.string.firebase_storage_icf_signature) + "/" + sourceFile.getName();

        StorageReference icfFolderRef = storageRef.child(targetFile);

        icfFolderRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if(Constant.DEBUG) Log.d(Constant.TAG, "Update signature file successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        if(Constant.DEBUG) Log.d(Constant.TAG, "Update signature file failed");
                    }
                });

    }
}
