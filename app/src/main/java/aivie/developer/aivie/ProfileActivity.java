package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

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
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import aivie.developer.aivie.util.Constant;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String userId;

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

    private String subjectNum;
    private String signedIcfName;
    private boolean isIcfSigned = false;
    private String firstName;
    private String lastName;
    private String displayName;
    private String dateOfBirth;
    private String gender;
    private String race;
    private String ethnicity;

    private final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /// DOB selection ///
        editTextdateOfBirth = (EditText) findViewById(R.id.dateOfBirth);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateOfBirthAndAge(null);

                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.yyyy_MM_dd), Locale.US);
                updateDateOfBirthToFirestore(sdf.format(myCalendar.getTime()));
            }
        };
        editTextdateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(v.getContext(),
                        date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        editTextSubjectNum = findViewById(R.id.subjectNum);
        editTextSignedICF = findViewById(R.id.signed_icf);
        editTextIsIcfSigned = findViewById(R.id.icf_signature);
        editTextLastName = findViewById(R.id.lastName);
        editTextFirstName = findViewById(R.id.firstName);
        editTextDisplayName = findViewById(R.id.displayName);
        editTextAge = findViewById(R.id.age);


        // Listener for Ethnicity
        editTextGender = findViewById(R.id.gender);
        editTextGender.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTextGender.setEnabled(false);
                updateGender();
                return false;
            }
        });

        // Listener for Race
        editTextRace = findViewById(R.id.race);
        editTextRace.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTextRace.setEnabled(false);
                updateRace();
                return false;
            }
        });

        // Listener for Ethnicity
        editTextEthnicity = findViewById(R.id.ethnicity);
        editTextEthnicity.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTextEthnicity.setEnabled(false);
                updateEthnicity();
                return false;
            }
        });

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserID");

        db = FirebaseFirestore.getInstance();

        showUserInfo();
    }

    @Override
    public void onResume() {
        super.onResume();

        // fetch updated data
        showUserInfo();
    }

    private void showUserInfo () {

        editTextGender.setEnabled(true);
        editTextRace.setEnabled(true);
        editTextEthnicity.setEnabled(true);

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
            dobString = sdf.format(myCalendar.getTime());
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

    private void updateDateOfBirthToFirestore(final String dobString) {

        DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    // Get user birthday
                    SimpleDateFormat sfd = new SimpleDateFormat(getString(R.string.yyyy_MM_dd));
                    Date dateBirthday = new Date();

                    try {
                        dateBirthday = (Date)sfd.parse(dobString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Map<String, Object> userData = new HashMap<>();
                    userData.put(getString(R.string.firestore_users_birthday), dateBirthday);

                    db.collection(getString(R.string.firestore_users))
                            .document(userId)
                            .set(userData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (Constant.DEBUG) Log.d(Constant.TAG, "DOB successfully written Firebase!");

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {

                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (Constant.DEBUG) Log.w(Constant.TAG, "Error writing DOB to Firebase", e);
                                }
                            });
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

    private void updateGender () {
        Intent intent = new Intent(this, GenderActivity.class);
        intent.putExtra("UserID", userId);
        startActivity(intent);
    }

    private void updateRace () {
        Intent intent = new Intent(this, RaceSelectionActivity.class);
        intent.putExtra("UserID", userId);
        startActivity(intent);
    }

    private void updateEthnicity () {
        Intent intent = new Intent(this, EthnicityActivity.class);
        intent.putExtra("UserID", userId);
        startActivity(intent);
    }
}
