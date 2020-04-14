package aivie.developer.aivie.ui.user.profile;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import aivie.developer.aivie.R;

public class ProfileUserFragment extends Fragment {

    private ProfileUserViewModel profileViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText editTextSubjectNum;
    private EditText editTextSignedICF;
    private EditText editTextIsIcfSigned;
    private EditText editTextLastName;
    private EditText editTextFirstName;
    private EditText editTextDisplayName;
    private EditText editTextdateOfBirth;
    private EditText editTextAge;
    private EditText editTextGender;
    private EditText editTextRace;
    private EditText editTextEthnicity;

    private String signedIcfName;
    private boolean isIcfSigned = false;
    private String subjectNum;
    private String firstName;
    private String lastName;
    private String displayName;
    private String dateOfBirth;
    private String gender;
    private String race;
    private String ethnicity;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileUserViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile_user, container, false);
        final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        editTextSubjectNum = root.findViewById(R.id.subjectNum);
        editTextSignedICF = root.findViewById(R.id.signed_icf);
        editTextIsIcfSigned = root.findViewById(R.id.icf_signature);
        editTextLastName = root.findViewById(R.id.lastName);
        editTextFirstName = root.findViewById(R.id.firstName);
        editTextDisplayName = root.findViewById(R.id.displayName);
        editTextdateOfBirth = root.findViewById(R.id.dateOfBirth);
        editTextAge = root.findViewById(R.id.age);
        editTextGender = root.findViewById(R.id.gender);
        editTextEthnicity = root.findViewById(R.id.ethnicity);
        editTextRace = root.findViewById(R.id.race);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        showUserInfo();

        return root;
    }

    private void showUserInfo () {

        String userId = mAuth.getCurrentUser().getUid();

        if (userId == null) {
            // Put default data on screen
        } else {
            getUserProfileFromFirestore(userId);
        }
    }

    private void getUserProfileFromFirestore (String userId) {

        final DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    final DocumentSnapshot documentUser = task.getResult();
                    if (documentUser.exists()) {

                        subjectNum = (String) documentUser.get(getString(R.string.firestore_users_subject_num));
                        lastName = (String) documentUser.get(getString(R.string.firestore_users_last_name));
                        firstName = (String) documentUser.get(getString(R.string.firestore_users_first_name));
                        displayName = (String) documentUser.get(getString(R.string.firestore_users_display_name));
                        isIcfSigned = (boolean) documentUser.get(getString(R.string.firestore_users_eicf_signed));

                        // Get user birthday
                        SimpleDateFormat sfd = new SimpleDateFormat(getString(R.string.yyyy_MM_dd));
                        Timestamp tsBirthday = (Timestamp) documentUser.get(getString(R.string.firestore_users_birthday));
                        Date dateBirthday = tsBirthday.toDate();
                        dateOfBirth = sfd.format(dateBirthday);

                        UpdateUI();

                        DocumentReference docRefGender = (DocumentReference) documentUser.get(getString(R.string.firestore_users_gender));
                        docRefGender.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    DocumentSnapshot documentGender = task.getResult();
                                    gender = (String) documentGender.get("Title");

                                    UpdateUI();
                                }
                            }
                        });

                        DocumentReference docRefRace = (DocumentReference) documentUser.get(getString(R.string.firestore_users_race));
                        docRefRace.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    DocumentSnapshot documentRace = task.getResult();
                                    race = (String) documentRace.get("Title");

                                    UpdateUI();
                                }
                            }
                        });

                        DocumentReference docRefEthnicity = (DocumentReference) documentUser.get(getString(R.string.firestore_users_ethnicity));
                        docRefEthnicity.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentEthnicity = task.getResult();
                                    ethnicity = (String) documentEthnicity.get("Title");

                                    UpdateUI();
                                }
                            }
                        });

                        DocumentReference docRefIcf = (DocumentReference) documentUser.get(getString(R.string.firestore_users_eicf));
                        docRefIcf.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentEthnicity = task.getResult();
                                    signedIcfName = (String) documentEthnicity.get("Id");

                                    UpdateUI();
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    private void UpdateUI () {

        if (subjectNum != null) editTextSubjectNum.setText(subjectNum);
        if (signedIcfName != null) editTextSignedICF.setText(signedIcfName);
        if (lastName != null) editTextLastName.setText(lastName);
        if (firstName != null) editTextFirstName.setText(firstName);
        if (displayName != null) editTextDisplayName.setText(displayName);
        if (gender != null) editTextGender.setText(gender);
        if (race != null) editTextRace.setText(race);
        if (ethnicity != null) editTextEthnicity.setText(ethnicity);

        if (isIcfSigned) {
            editTextIsIcfSigned.setText(getString(R.string.icf_has_signed));
        } else {
            // Implement customized string
            SpannableString sp = new SpannableString(getResources().getString(R.string.icf_has_not_signed));
            sp.setSpan(new ForegroundColorSpan(Color.RED), 0, sp.length(), 0);
            editTextIsIcfSigned.setText(sp);
        }

        updateDateOfBirthAndAge(dateOfBirth);
    }

    private void updateDateOfBirthAndAge (String dobString) {

        if (dobString == null) {
            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.yyyy_MM_dd), Locale.US);
            dobString = sdf.format("1900-01-01");
        }
        editTextdateOfBirth.setText(dobString);

        // Also update Age
        editTextAge.setText(Integer.valueOf(updateAge(dobString)).toString());
    }

    private int updateAge (String dobString) {

        // Construct DOB by input string
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.yyyy_MM_dd));
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (age < 0)
            age = 0;

        return age;
    }
}
