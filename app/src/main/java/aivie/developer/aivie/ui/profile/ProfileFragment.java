package aivie.developer.aivie.ui.profile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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

import aivie.developer.aivie.BuildConfig;
import aivie.developer.aivie.HomeActivity;
import aivie.developer.aivie.R;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView textViewSubjectNum;
    private TextView textViewLastName;
    private TextView textViewFirstName;
    private TextView textViewDisplayName;
    private EditText editTextdateOfBirth;
    private TextView textViewAge;
    private TextView textViewGender;
    private TextView textViewRace;
    private TextView textViewEthnicity;
    private String userId;
    private String subjectNum;
    private String firstName;
    private String lastName;
    private String displayName;
    private String dateOfBirth;
    private String gender;
    private String race;
    private String ethnicity;
    private final Calendar myCalendar = Calendar.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /// DOB selection ///
        editTextdateOfBirth = (EditText) root.findViewById(R.id.dateOfBirth);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.d(TAG, "onDateSet");
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateOfBirthAndAge(null);
            }
        };
        editTextdateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog((HomeActivity)getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        HomeActivity activity = (HomeActivity)getActivity();
        Bundle result = activity.getHomeActivityData();
        userId = result.getString("UserID");

        textViewSubjectNum = root.findViewById(R.id.subjectNum);
        textViewLastName = root.findViewById(R.id.lastName);
        textViewFirstName = root.findViewById(R.id.firstName);
        textViewDisplayName = root.findViewById(R.id.displayName);
        textViewAge = root.findViewById(R.id.age);
        textViewGender = root.findViewById(R.id.gender);
        textViewRace = root.findViewById(R.id.race);
        textViewEthnicity = root.findViewById(R.id.ethnicity);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        getUserProfileFromFirestore(userId);

        return root;
    }

    private void getUserProfileFromFirestore (String userId) {

        DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

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
                    }
                }
            }
        });

    }

    private void UpdateUI () {

        if (subjectNum != null) textViewSubjectNum.setText(subjectNum);
        if (lastName != null) textViewLastName.setText(lastName);
        if (firstName != null) textViewFirstName.setText(firstName);
        if (displayName != null) textViewDisplayName.setText(displayName);
        if (gender != null) textViewGender.setText(gender);
        if (race != null) textViewRace.setText(race);
        if (ethnicity != null) textViewEthnicity.setText(ethnicity);

        updateDateOfBirthAndAge(dateOfBirth);
    }

    private void updateDateOfBirthAndAge (String dobString) {

        if (dobString == null) {

            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.yyyy_MM_dd), Locale.US);
            dobString = sdf.format(myCalendar.getTime());
        }
        editTextdateOfBirth.setText(dobString);

        // Also update Age
        String age = getString(R.string.age) + " : " + updateAge(dobString);
        textViewAge.setText(age);
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
