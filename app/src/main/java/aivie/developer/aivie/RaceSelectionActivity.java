package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RaceSelectionActivity extends AppCompatActivity {

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button buttonConfirm;
    ArrayList<String> race_desc = new ArrayList<String>();
    ArrayList<String> race = new ArrayList<String>();
    ArrayList<String> race_db_document = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_selection);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        radioGroup = findViewById(R.id.radioGroup);
        buttonConfirm = findViewById(R.id.buttonConfirm);

        addListenerOnButton();
        updateUI();
    }

    public void addListenerOnButton() {

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedResId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedResId);
                int selectedIndex = radioGroup.indexOfChild(radioButton);

                Toast.makeText(RaceSelectionActivity.this, Integer.valueOf(selectedIndex).toString(), Toast.LENGTH_SHORT).show();

                updateToFireStore(selectedIndex);
            }
        });

    }

    private void updateToFireStore (final Integer selectedIndex) {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if(DEBUG) Log.i(TAG, "Login-user: " + user.getUid());

            DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(mAuth.getCurrentUser().getUid());

            docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        Map<String, Object> userData = new HashMap<>();

                        userData.put(getString(R.string.firestore_users_race), db.collection(getString(R.string.firestore_race)).document(race_db_document.get(selectedIndex)));
                        if (DEBUG) Log.i(TAG, userData.toString());

                        db.collection(getString(R.string.firestore_users))
                                .document(mAuth.getCurrentUser().getUid()).set(userData, SetOptions.merge())
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
                }
            });

        } else {
            if(DEBUG) Log.i(TAG, "Login-user is null");
        }
    }

    private void updateUI() {

        race.add(getResources().getString(R.string.race_white));
        race_desc.add(getResources().getString(R.string.race_white_desc));
        race_db_document.add(getResources().getString(R.string.race_white_db_doc));

        race.add(getResources().getString(R.string.race_asian));
        race_desc.add(getResources().getString(R.string.race_asian_desc));
        race_db_document.add(getResources().getString(R.string.race_asian_db_doc));

        race.add(getResources().getString(R.string.race_black));
        race_desc.add(getResources().getString(R.string.race_black_desc));
        race_db_document.add(getResources().getString(R.string.race_black_db_doc));

        race.add(getResources().getString(R.string.race_hawaiian));
        race_desc.add(getResources().getString(R.string.race_hawaiian_desc));
        race_db_document.add(getResources().getString(R.string.race_hawaiian_db_doc));

        race.add(getResources().getString(R.string.race_alaska));
        race_desc.add(getResources().getString(R.string.race_alaska_desc));
        race_db_document.add(getResources().getString(R.string.race_alaska_db_doc));

        for (int i = 0; i < radioGroup .getChildCount(); i++) {

            // Implement customized string
            SpannableString sp1 = new SpannableString(race.get(i));
            SpannableString sp2 = new SpannableString(race_desc.get(i));

            if (i == 3) {
                sp1.setSpan(new RelativeSizeSpan(1.1f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (i == 4) {
                sp1.setSpan(new RelativeSizeSpan(1.2f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sp1.setSpan(new RelativeSizeSpan(1.4f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            sp1.setSpan(new StyleSpan(Typeface.BOLD), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp1.setSpan(new UnderlineSpan(), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, sp2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((RadioButton) radioGroup.getChildAt(i)).setText(TextUtils.concat(sp1, "\r\n", sp2));
        }

    }
}
