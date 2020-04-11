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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import aivie.developer.aivie.util.Constant;

public class SignatureActivity extends AppCompatActivity {

    private SignaturePad mSignaturePad;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Button mClearButton;
    private Button mConfirmButton;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        db = FirebaseFirestore.getInstance();
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

                    Map<String, Object> userData = new HashMap<>();
                    userData.put(getString(R.string.firestore_users_eicf_signed), true);

                    final DocumentReference docRefSignedICF = db.collection(getString(R.string.firestore_users)).document(userId);

                    db.runTransaction(new Transaction.Function<Void>() {
                        @Override
                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                            transaction.update(docRefSignedICF, getString(R.string.firestore_users_eicf_signed), true);
                            // Success
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if(Constant.DEBUG) Log.d(Constant.TAG, "Transaction success!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(Constant.DEBUG) Log.w(Constant.TAG, "Transaction failure.", e);
                        }
                    });

                    Intent intent = new Intent(getApplicationContext(), HomeUserActivity.class);
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
            updateFileToFirebase(photo);

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

    private void updateFileToFirebase(File sourceFile) {

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
