package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RaceSelectionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button buttonConfirm;
    private ProgressBar pbConfirm;
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
        pbConfirm = findViewById(R.id.progressBarConfirm);

        addListenerOnButton();
        updateUI();
    }

    public void addListenerOnButton() {

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if(Constant.DEBUG) Log.i(Constant.TAG, "Login-user: " + user.getUid());

            DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(mAuth.getCurrentUser().getUid());

            docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        Map<String, Object> userData = new HashMap<>();

                        userData.put(getString(R.string.firestore_users_race), db.collection(getString(R.string.firestore_race)).document(race_db_document.get(selectedIndex)));
                        if (Constant.DEBUG) Log.i(Constant.TAG, userData.toString());

                        db.collection(getString(R.string.firestore_users))
                                .document(mAuth.getCurrentUser().getUid()).set(userData, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {

                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (Constant.DEBUG) Log.d(Constant.TAG, "DocumentSnapshot successfully written!");

                                        pbConfirm.setVisibility(View.GONE);

                                        Intent intent = new Intent(getApplicationContext(), HomeUserActivity.class);
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

        } else {
            if(Constant.DEBUG) Log.i(Constant.TAG, "Login-user is null");
        }
    }

    private void updateUI() {

        race.add(getResources().getString(R.string.race_white));
        race_desc.add(getResources().getString(R.string.race_white_desc));
        race_db_document.add(getResources().getString(R.string.firestore_race_white));

        race.add(getResources().getString(R.string.race_asian));
        race_desc.add(getResources().getString(R.string.race_asian_desc));
        race_db_document.add(getResources().getString(R.string.firestore_race_asian));

        race.add(getResources().getString(R.string.race_black));
        race_desc.add(getResources().getString(R.string.race_black_desc));
        race_db_document.add(getResources().getString(R.string.firestore_race_black));

        race.add(getResources().getString(R.string.race_hawaiian));
        race_desc.add(getResources().getString(R.string.race_hawaiian_desc));
        race_db_document.add(getResources().getString(R.string.firestore_race_hawaiian));

        race.add(getResources().getString(R.string.race_alaska));
        race_desc.add(getResources().getString(R.string.race_alaska_desc));
        race_db_document.add(getResources().getString(R.string.firestore_race_alaska));

        for (int i = 0; i < radioGroup .getChildCount(); i++) {

            // Implement customized string
            SpannableString sp1 = new SpannableString(race.get(i));
            SpannableString sp2 = new SpannableString(race_desc.get(i));

            /*
            if (i == 3) {
                sp1.setSpan(new RelativeSizeSpan(1.1f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (i == 4) {
                sp1.setSpan(new RelativeSizeSpan(1.2f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sp1.setSpan(new RelativeSizeSpan(1.4f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            */

            sp1.setSpan(new RelativeSizeSpan(1.1f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp1.setSpan(new StyleSpan(Typeface.BOLD), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //sp1.setSpan(new UnderlineSpan(), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp2.setSpan(new RelativeSizeSpan(0.8f), 0, sp2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, sp2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((RadioButton) radioGroup.getChildAt(i)).setText(TextUtils.concat(sp1, "\r\n", sp2));
        }
    }

    @Override
    public void onBackPressed() {
        // empty so nothing happens
    }
}
