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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

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
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("richc", "signInWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                Log.i("richc", "getDisplayName: " + user.getDisplayName());
                                Log.i("richc", "getPhotoUrl: " + user.getPhotoUrl().toString());
                                Toast.makeText(LoginActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
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
