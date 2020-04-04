package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    Button signupButton;
    private ProgressBar pbSignup;
    String firstName;
    String lastName;
    String displayName;
    private String photoUriString = "https://ui-avatars.com/api/?size=80&rounded=true&background=0D8ABC&color=fff&name=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Implement customized string
        TextView textView = findViewById(R.id.textViewHaveAccount);
        SpannableString sp1 = new SpannableString(getResources().getString(R.string.have_account));
        SpannableString sp2 = new SpannableString(getResources().getString(R.string.have_account_log_in));
        sp2.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp2.length(), 0);
        sp2.setSpan(new UnderlineSpan(), 0, sp2.length(), 0);
        textView.setText(TextUtils.concat(sp1, " ", sp2));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        signupButton = findViewById(R.id.signup);
        pbSignup = findViewById(R.id.progressBarSignup);
    }

    public void signUp (View view) {

        signupButton.setEnabled(false);
        pbSignup.setVisibility(view.VISIBLE);

        EditText editTextFirstName = findViewById(R.id.firstName);
        EditText editTextLastName = findViewById(R.id.lastName);
        EditText editTextDisplayName = findViewById(R.id.displayname);
        EditText editTextEmail = findViewById(R.id.username);
        EditText editTextPassword = findViewById(R.id.password);

        firstName = editTextFirstName.getText().toString();
        lastName = editTextLastName.getText().toString();
        displayName = editTextDisplayName.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        logInToFirebaseByEmail(displayName, email, password);
    }

    public void logInToFirebaseByEmail (final String displayName, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if (DEBUG) Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .setPhotoUri(Uri.parse(photoUriString + displayName))
                                    .build();

                            user.updateProfile(profileUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                //signupButton.setEnabled(true);
                                                pbSignup.setVisibility(View.GONE);

                                                if (DEBUG) Log.d(TAG, "User profile updated.");
                                                Toast.makeText(SignupActivity.this, "Create account successfully", Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });

                            createTempDataInFirestore(user.getUid());

                        } else {
                            // If sign in fails, display a message to the user.
                            if (DEBUG) Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.\r\n", Toast.LENGTH_LONG).show();

                            signupButton.setEnabled(true);
                            pbSignup.setVisibility(View.GONE);
                        }
                    }
                });
    }

    public void createTempDataInFirestore (String userId) {

        Map<String, Object> userData = new HashMap<>();

        userData.put(getString(R.string.firestore_users_display_name), displayName);
        userData.put(getString(R.string.firestore_users_first_name), firstName);
        userData.put(getString(R.string.firestore_users_last_name), lastName);
        userData.put(getString(R.string.firestore_users_birthday), new Timestamp(new Date()));
        userData.put(getString(R.string.firestore_users_gender), db.collection(getString(R.string.firestore_gender)).document("0"));
        userData.put(getString(R.string.firestore_users_role), db.collection(getString(R.string.firestore_roles)).document("0"));
        userData.put(getString(R.string.firestore_users_patient_of_study), db.collection(getString(R.string.firestore_studies)).document("000001"));
        userData.put(getString(R.string.firestore_users_signed_icf), db.collection(getString(R.string.firestore_icf)).document("0001"));

        if (DEBUG) Log.i(TAG, userData.toString());

        db.collection(getString(R.string.firestore_users)).document(userId).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        if (DEBUG) Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (DEBUG) Log.w(TAG, "Error writing document", e);
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

    public void goLoginActivity (View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true); // disable going back to the MainActivity
    }
}
