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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String userId;
    String displayName;
    String photoUri;
    String birthday;
    String studyName;
    List<String> visitPlan = new ArrayList<String>();

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

                            userId = user.getUid();
                            displayName = user.getDisplayName();
                            photoUri = user.getPhotoUrl().toString();

                            Toast.makeText(LoginActivity.this, "Welcome " + displayName, Toast.LENGTH_LONG).show();

                            ///// Get user data from Firestore database /////
                            DocumentReference docRefUser = db.collection("users").document(userId);
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

                                                            List<Timestamp> visitsDate = (List<Timestamp>) documentVisit.getData().get("visits");
                                                            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
                                                            Log.i("richc", "Visits Date: " + visitsDate);

                                                            for (int i=0; i<visitsDate.size(); i++) {
                                                                Timestamp tm = (Timestamp) visitsDate.get(i);
                                                                Date date = tm.toDate();
                                                                visitPlan.add(sfd.format(date).toString());
                                                            }
                                                            Log.i("richc", "Visits Plan: " + visitPlan);

                                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                            intent.putExtra("UserID", userId);
                                                            intent.putExtra("DisplayName", displayName);
                                                            intent.putExtra("PhotoUrl", photoUri);
                                                            intent.putExtra("PatientOfStudy", studyName);
                                                            intent.putStringArrayListExtra("VisitPlan", (ArrayList<String>) visitPlan);
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

    public  void goSignupActivity (View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }
}
