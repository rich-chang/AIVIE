package aivie.developer.aivie.presentation.account;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import aivie.developer.aivie.R;
import aivie.developer.aivie.util.Constant;

public class SignupRepository {

    private static final String FIRE_COLLECTION_USERS = "users";
    private static final String FIRE_COLLECTION_GENDER = "gender";
    private static final String FIRE_COLLECTION_RACE = "race";
    private static final String FIRE_COLLECTION_ETHNICITY = "ethnicity";
    private static final String FIRE_COLLECTION_ROLES = "roles";
    private static final String FIRE_COLLECTION_STUDIES = "studies";
    private static final String FIRE_COLLECTION_ICF = "icf";

    private static final String FIRE_COLUMN_DISPLAYNAME = "DisplayName";
    private static final String FIRE_COLUMN_FIRSTNAME = "FirstName";
    private static final String FIRE_COLUMN_LASTNAME = "LastName";
    private static final String FIRE_COLUMN_DOB = "DOB";
    private static final String FIRE_COLUMN_GENDER = "Gender";
    private static final String FIRE_COLUMN_RACE = "Race";
    private static final String FIRE_COLUMN_ETHICITY = "Ethnicity";
    private static final String FIRE_COLUMN_SUBJECTNUM = "SubjectNumber";
    private static final String FIRE_COLUMN_ROLE = "Role";
    private static final String FIRE_COLUMN_PATIENT_OF_STUDY = "PatientOfStudy";
    private static final String FIRE_COLUMN_EICF = "eICF";
    private static final String FIRE_COLUMN_EICF_SIGNED = "eICF_Signed";
    private static final String FIRE_COLUMN_SITE_ID = "SiteId";
    private static final String FIRE_COLUMN_SITE_DOCTOR = "SiteDoctor";
    private static final String FIRE_COLUMN_SITE_SC = "SiteSC";
    private static final String FIRE_COLUMN_SITE_PHONE = "SitePhone";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String lastName;
    private String firstName;

    SignupRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = "";
    }

    void setUserFullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    void userSignup(Context context, String username, String password, final SignupContract.SignupCallback signupCallback) {

        if (username.equals("") || password.equals("")) {
            signupCallback.onFailure("Email or password can't be empty.");
            signupCallback.onComplete();
            return;
        }

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userId = mAuth.getCurrentUser().getUid();
                            signupCallback.onSuccess("Success");
                        } else {
                            signupCallback.onFailure(String.format("Signup failure : %s", task.getException()));
                        }
                        signupCallback.onComplete();
                    }
                });
    }

    void createTempUserDataInFireDB(final SignupContract.CreateTempDataCallback createTempDataCallback) {

        Map<String, Object> userData = new HashMap<>();

        userData.put(FIRE_COLUMN_DISPLAYNAME, lastName);
        userData.put(FIRE_COLUMN_FIRSTNAME, firstName);
        userData.put(FIRE_COLUMN_LASTNAME, lastName);
        userData.put(FIRE_COLUMN_DOB, new Timestamp(new Date()));

        userData.put(FIRE_COLUMN_GENDER, db.collection(FIRE_COLLECTION_GENDER).document("UNKNOWN"));
        userData.put(FIRE_COLUMN_RACE, db.collection(FIRE_COLLECTION_RACE).document("O"));
        userData.put(FIRE_COLUMN_ETHICITY, db.collection(FIRE_COLLECTION_ETHNICITY).document("UNKNOWN"));

        userData.put(FIRE_COLUMN_SUBJECTNUM, "UNKNOWN");
        userData.put(FIRE_COLUMN_ROLE, db.collection(FIRE_COLLECTION_ROLES).document("PT"));
        userData.put(FIRE_COLUMN_PATIENT_OF_STUDY, db.collection(FIRE_COLLECTION_STUDIES).document("000001"));
        userData.put(FIRE_COLUMN_EICF, db.collection(FIRE_COLLECTION_ICF).document("0001"));
        userData.put(FIRE_COLUMN_EICF_SIGNED, false);

        userData.put(FIRE_COLUMN_SITE_ID, "S001");
        userData.put(FIRE_COLUMN_SITE_DOCTOR, "Steven Jackson");
        userData.put(FIRE_COLUMN_SITE_SC, "Kelly");
        userData.put(FIRE_COLUMN_SITE_PHONE, "+886-2-12345678");

        db.collection(FIRE_COLLECTION_USERS).document(userId).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (Constant.DEBUG) Log.d(Constant.TAG, "DocumentSnapshot successfully written!");
                        createTempDataCallback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (Constant.DEBUG) Log.w(Constant.TAG, "Error writing document", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        createTempDataCallback.onComplete();
                    }
                });

    }
}
