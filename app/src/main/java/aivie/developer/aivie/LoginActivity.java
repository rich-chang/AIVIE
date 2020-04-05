package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button loginButton;
    private TextView textViewNeedAccount;
    private ProgressBar pbLogin;
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
        textViewNeedAccount = findViewById(R.id.textViewNeedAccount);
        SpannableString sp1 = new SpannableString(getResources().getString(R.string.need_account));
        SpannableString sp2 = new SpannableString(getResources().getString(R.string.need_account_sign_up));
        sp2.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp2.length(), 0);
        sp2.setSpan(new UnderlineSpan(), 0, sp2.length(), 0);
        textViewNeedAccount.setText(TextUtils.concat(sp1, " ", sp2));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.login);
        pbLogin = findViewById(R.id.progressBarLogin);
    }

    public void logIn (View view) {

        loginButton.setEnabled(false);
        textViewNeedAccount.setEnabled(false);
        pbLogin.setVisibility(view.VISIBLE);

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            if(DEBUG) Log.i(TAG, "Login-user is null");
        } else {
            if(DEBUG) Log.i(TAG, "Login-user: " + user.getUid());
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
                            if(DEBUG) Log.d(TAG, "signInWithEmail:success");

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
                                            if(DEBUG) Log.d(TAG, "DocumentSnapshot data: " + documentUser.getData());

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

                                                            pbLogin.setVisibility(View.GONE);

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
                                                            if(DEBUG) Log.d(TAG, "No such document");

                                                            loginButton.setEnabled(true);
                                                            textViewNeedAccount.setEnabled(true);
                                                            pbLogin.setVisibility(View.GONE);
                                                        }
                                                    }
                                                }
                                            });
                                        } else {
                                            if(DEBUG) Log.d(TAG, "No such document");

                                            loginButton.setEnabled(true);
                                            textViewNeedAccount.setEnabled(true);
                                            pbLogin.setVisibility(View.GONE);
                                        }
                                    } else {
                                        if(DEBUG) Log.d(TAG, "get failed with ", task.getException());

                                        loginButton.setEnabled(true);
                                        textViewNeedAccount.setEnabled(true);
                                        pbLogin.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            if(DEBUG) Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.\r\n", Toast.LENGTH_LONG).show();

                            loginButton.setEnabled(true);
                            textViewNeedAccount.setEnabled(true);
                            pbLogin.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public  void goSignupActivity (View view) {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true); // disable going back to the MainActivity
    }
}
