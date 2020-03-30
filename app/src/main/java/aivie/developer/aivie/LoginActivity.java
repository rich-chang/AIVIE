package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String studyName;
    List<Timestamp> visitsPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Implement customized string
        TextView textView = findViewById(R.id.textViewNeedAccount);
        SpannableString sp1 = new SpannableString(getResources().getString(R.string.need_account));
        SpannableString sp2 = new SpannableString(getResources().getString(R.string.need_account_sign_up));
        sp2.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp2.length(), 0);
        sp2.setSpan(new UnderlineSpan(), 0, sp2.length(), 0);
        textView.setText(TextUtils.concat(sp1, " ", sp2));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void logIn (View view) {

        EditText editTextEmail = findViewById(R.id.username);
        EditText editTextPassword = findViewById(R.id.password);

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        logInToFirebaseByEmail(email, password);
    }

    public void logInToFirebaseByEmail (String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Log.d("richc", "signInWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            Log.i("richc", "getDisplayName: " + user.getDisplayName());
                            Log.i("richc", "getPhotoUrl: " + user.getPhotoUrl().toString());
                            Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_LONG).show();

                            ///// Get user data from Firestore database /////
                            DocumentReference docRefUser = db.collection("users").document(user.getUid());
                            docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        DocumentSnapshot documentUser = task.getResult();
                                        if (documentUser.exists()) {
                                            Log.d("richc", "DocumentSnapshot data: " + documentUser.getData());

                                            ///// Get participatant study of user /////
                                            DocumentReference docRefStudy = (DocumentReference) documentUser.getData().get("PatientOfStudy");
                                            docRefStudy.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                    if (task.isSuccessful()) {

                                                        DocumentSnapshot documentVisit = task.getResult();
                                                        if (documentVisit.exists()) {

                                                            studyName = documentVisit.get("Name").toString();
                                                            visitsPlan = (List<Timestamp>) documentVisit.getData().get("visits");
                                                            Log.i("richc", "Study Name: " + studyName);
                                                            Log.i("richc", "Visit Plan: " + visitsPlan);

                                                            Log.d("richc", "GO TO INTENT");
                                                            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                                                            startActivity(intent);
                                                        } else {
                                                            Log.d("richc", "No such document");
                                                        }
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.d("richc", "No such document");
                                        }
                                    } else {
                                        Log.d("richc", "get failed with ", task.getException());
                                    }
                                }
                            });
                        } else {
                            Log.w("richc", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.\r\n", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void getUserDataFromFirestore (String userId) {

        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("richc", "DocumentSnapshot data: " + document.getData());

                        getStudyDataFromFirestoreByRef((DocumentReference) document.getData().get("PatientOfStudy"));

                    } else {
                        Log.d("richc", "No such document");
                    }
                } else {
                    Log.d("richc", "get failed with ", task.getException());
                }
            }
        });
    }

    public void getStudyDataFromFirestoreByRef (DocumentReference docRef) {

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentVisit = task.getResult();
                    if (documentVisit.exists()) {

                        studyName = documentVisit.get("Name").toString();
                        visitsPlan = (List<Timestamp>) documentVisit.getData().get("visits");
                        
                        Log.i("richc", "Study Name: " + studyName);
                        Log.i("richc", "Visit Plan: " + visitsPlan);
                    } else {
                        Log.d("richc", "No such document");
                    }
                }
            }
        });

    }

    public  void goSignupActivity (View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }
}
