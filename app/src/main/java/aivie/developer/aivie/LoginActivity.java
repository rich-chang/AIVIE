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
import android.widget.Button;
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
    Button loginButton;
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

        loginButton = findViewById(R.id.login);
    }

    public void logIn (View view) {

        loginButton.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Log.i("richc", "Login-user is null");
        } else {
            Log.i("richc", "Login-user: " + user.getUid());
        }

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

                            ///// Get data of user from Firestore database /////
                            DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

                            docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        final DocumentSnapshot documentUser = task.getResult();
                                        if (documentUser.exists()) {
                                            Log.d("richc", "DocumentSnapshot data: " + documentUser.getData());

                                            // Get user birthday
                                            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
                                            Timestamp tsBirthday = (Timestamp) documentUser.get(getString(R.string.firestore_users_birthday));
                                            Date dateBirthday = tsBirthday.toDate();
                                            birthday = sfd.format(dateBirthday);

                                            ///// Get PatientOfStudy of user from Firestore database/////
                                            DocumentReference docRefStudy = (DocumentReference) documentUser.getData().get(getString(R.string.firestore_users_patient_of_study));
                                            docRefStudy.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                    if (task.isSuccessful()) {

                                                        Toast.makeText(LoginActivity.this, "Welcome " + displayName, Toast.LENGTH_LONG).show();

                                                        DocumentSnapshot documentVisit = task.getResult();
                                                        if (documentVisit.exists()) {

                                                            loginButton.setEnabled(true);

                                                            studyName = documentVisit.get(getString(R.string.firestore_studies_study_title)).toString();

                                                            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
                                                            List<Timestamp> visitsDate = (List<Timestamp>) documentVisit.getData().get(getString(R.string.firestore_studies_visit_plan));

                                                            for (int i=0; i<visitsDate.size(); i++) {
                                                                Timestamp tm = (Timestamp) visitsDate.get(i);
                                                                Date date = tm.toDate();
                                                                visitPlan.add(sfd.format(date).toString());
                                                            }

                                                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                            intent.putExtra("UserID", userId);
                                                            intent.putExtra("DisplayName", displayName);
                                                            intent.putExtra("PhotoUrl", photoUri);
                                                            intent.putExtra("Birthday", birthday);
                                                            intent.putExtra("PatientOfStudy", studyName);
                                                            intent.putStringArrayListExtra("VisitPlan", (ArrayList<String>) visitPlan);
                                                            startActivity(intent);

                                                        } else {
                                                            Log.d("richc", "No such document");

                                                            loginButton.setEnabled(true);
                                                        }
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.d("richc", "No such document");

                                            loginButton.setEnabled(true);
                                        }
                                    } else {
                                        Log.d("richc", "get failed with ", task.getException());

                                        loginButton.setEnabled(true);
                                    }
                                }
                            });
                        } else {
                            Log.w("richc", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.\r\n", Toast.LENGTH_LONG).show();

                            loginButton.setEnabled(true);
                        }
                    }
                });
    }

    public  void goSignupActivity (View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }
}
