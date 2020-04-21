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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import aivie.developer.aivie.util.Constant;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button loginButton;
    private TextView textViewNeedAccount;
    private ProgressBar pbLogin;
    private String userId;

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

        updateUiControl(true);
    }

    public void logIn (View view) {

        updateUiControl(false);

        EditText editTextEmail = findViewById(R.id.username);
        EditText editTextPassword = findViewById(R.id.password);
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (email.equals("") || password.equals("")) {

            Toast.makeText(LoginActivity.this, "A username and password is required.", Toast.LENGTH_SHORT).show();
        } else {

            logInToFirebaseByEmail(email, password);
        }
    }

    public void logInToFirebaseByEmail (String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            if(Constant.DEBUG) Log.d(Constant.TAG, "signInWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            userId = user.getUid();
                            if(Constant.DEBUG) Log.i(Constant.TAG, "Login User: " + userId);

                            DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);
                            docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        DocumentSnapshot documentUser = task.getResult();
                                        if (documentUser.exists()) {

                                            /// Check if PT signed ICF ///
                                            final boolean[] isIcfSigned = {false};

                                            isIcfSigned[0] = (boolean) documentUser.get(getString(R.string.firestore_users_eicf_signed));
                                            Log.d(Constant.TAG, Boolean.toString(isIcfSigned[0]));

                                            /// Check Role ///
                                            final DocumentReference docRefRole = (DocumentReference) documentUser.get(getString(R.string.firestore_users_role));
                                            docRefRole.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {

                                                        DocumentSnapshot documentRace = task.getResult();
                                                        String role = (String) documentRace.get(getString(R.string.firestore_column_id));
                                                        Log.d(Constant.TAG, "Role: " + role);

                                                        Intent intent = null;
                                                        switch (role) {
                                                            case "SC":

                                                                intent = new Intent(getApplicationContext(), HomeAdmActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                                break;

                                                            case "PT":

                                                                if (isIcfSigned[0]) {
                                                                    Log.d(Constant.TAG, "ICF Signed");

                                                                    intent = new Intent(getApplicationContext(), HomeUserActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Log.d(Constant.TAG, "ICF is NOT Signed");

                                                                    intent = new Intent(getApplicationContext(), IcfActivity.class);
                                                                    intent.putExtra("UserID", userId);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                                break;

                                                            case "UNKNOWN":
                                                            default:
                                                                Log.d(Constant.TAG, "User role is unknown");
                                                        }
                                                    } else {
                                                        updateUiControl(true);
                                                    }
                                                }
                                            });
                                        } else {
                                            updateUiControl(true);
                                        }
                                    } else {
                                        updateUiControl(true);
                                    }
                                }
                            });

                            /*
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
                                            if(Constant.DEBUG) Log.d(Constant.TAG, "DocumentSnapshot data: " + documentUser.getData());



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

                                                            studyName = documentVisit.get(getString(R.string.firestore_column_title)).toString();

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
                                                            intent.putExtra("PatientOfStudy", studyName);
                                                            intent.putStringArrayListExtra("VisitPlan", (ArrayList<String>) visitPlan);
                                                            startActivity(intent);

                                                        } else {
                                                            if(Constant.DEBUG) Log.d(Constant.TAG, "No such document");

                                                            loginButton.setEnabled(true);
                                                            textViewNeedAccount.setEnabled(true);
                                                            pbLogin.setVisibility(View.GONE);
                                                        }
                                                    }
                                                }
                                            });
                                        } else {
                                            if(Constant.DEBUG) Log.d(Constant.TAG, "No such document");

                                            loginButton.setEnabled(true);
                                            textViewNeedAccount.setEnabled(true);
                                            pbLogin.setVisibility(View.GONE);
                                        }
                                    } else {
                                        if(Constant.DEBUG) Log.d(Constant.TAG, "get failed with ", task.getException());

                                        loginButton.setEnabled(true);
                                        textViewNeedAccount.setEnabled(true);
                                        pbLogin.setVisibility(View.GONE);
                                    }
                                }
                            });

                            */

                        } else {
                            if(Constant.DEBUG) Log.w(Constant.TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.\r\n", Toast.LENGTH_LONG).show();

                            updateUiControl(true);
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
        finish();
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true); // disable going back to the MainActivity
    }

    private void updateUiControl (boolean allowUserControl) {

        if (allowUserControl) {

            loginButton.setEnabled(true);
            textViewNeedAccount.setEnabled(true);
            pbLogin.setVisibility(View.GONE);
        } else {

            loginButton.setEnabled(false);
            textViewNeedAccount.setEnabled(false);
            pbLogin.setVisibility(View.VISIBLE);
        }
    }
}
