package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String photoUriString = "https://ui-avatars.com/api/?size=128&rounded=true&background=0D8ABC&color=fff&name=";

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
    }

    public void signUp (View view) {

        EditText editTextDisplayName = findViewById(R.id.displayname);
        EditText editTextEmail = findViewById(R.id.username);
        EditText editTextPassword = findViewById(R.id.password);

        String displayName = editTextDisplayName.getText().toString();
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
                            Log.d("richc", "createUserWithEmail:success");

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
                                                Log.d("richc", "User profile updated.");
                                            }
                                        }
                                    });

                            createTempDataInFirestore(user.getUid());

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("richc", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.\r\n", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void createTempDataInFirestore (String userId) {

        List<String> tempFirstName = Arrays.asList("Adam", "Emily", "Jasper", "Leana", "Lily", "Bowen", "Dimitra", "Emre");
        List<String> tempLastName = Arrays.asList("Harris", "Petit", "Ingvaldsen", "Meunier", "Wood", "Stinger", "Barendrecht", "Okumuş");

        Map<String, Object> userData = new HashMap<>();

        userData.put("FirstName", tempFirstName.get(new Random().nextInt(tempFirstName.size())));
        userData.put("LastName", tempLastName.get(new Random().nextInt(tempLastName.size())));
        userData.put("Birthday", new Timestamp(new Date()));
        userData.put("Gender", db.collection("gender").document("0"));
        userData.put("Role", db.collection("roles").document("0"));
        userData.put("PatientOfStudy", db.collection("studies").document("000001"));
        userData.put("SignedICF", db.collection("icf").document("0001"));

        Log.i("richc", userData.toString());

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("richc", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("richc", "Error writing document", e);
                    }
                });
    }

    public void goLoginActivity (View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
}
