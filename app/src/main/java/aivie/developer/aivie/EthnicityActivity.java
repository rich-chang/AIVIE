package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aivie.developer.aivie.util.Constant;

public class EthnicityActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button buttonConfirm;
    private ProgressBar pbConfirm;
    ArrayList<String> ethnicity_db_document = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethnicity);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserID");

        radioGroup = findViewById(R.id.radioGroupEthnicity);
        buttonConfirm = findViewById(R.id.buttonConfirm);
        pbConfirm = findViewById(R.id.progressBarConfirm);

        ethnicity_db_document.add(getString(R.string.firestore_ethnicity_h));
        ethnicity_db_document.add(getString(R.string.firestore_ethnicity_n));

        addListenerOnButton();
    }

    @Override
    public void onBackPressed() {
        // empty so nothing happens
    }

    public void addListenerOnButton() {

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userId == null) {
                    return;
                }

                radioGroup.setEnabled(false);
                buttonConfirm.setEnabled(false);
                pbConfirm.setVisibility(View.VISIBLE);

                int selectedResId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedResId);
                int selectedIndex = radioGroup.indexOfChild(radioButton);

                updateToFireStore(selectedIndex);
            }
        });
    }

    private void updateToFireStore (final Integer selectedIndex) {

        DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    Map<String, Object> userData = new HashMap<>();

                    userData.put(getString(R.string.firestore_users_ethnicity),
                            db.collection(getString(R.string.firestore_ethnicity)).document(ethnicity_db_document.get(selectedIndex)));

                    if (Constant.DEBUG) Log.i(Constant.TAG, userData.toString());

                    db.collection(getString(R.string.firestore_users))
                            .document(userId).set(userData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (Constant.DEBUG) Log.d(Constant.TAG, "DocumentSnapshot successfully written!");

                                    pbConfirm.setVisibility(View.GONE);

                                    Intent intent = new Intent(getApplicationContext(), HomeAdmActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {

                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (Constant.DEBUG) Log.w(Constant.TAG, "Error writing document", e);
                                }
                            });
                }
            }
        });
    }
}
