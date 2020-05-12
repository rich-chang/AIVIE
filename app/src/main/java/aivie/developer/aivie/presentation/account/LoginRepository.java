package aivie.developer.aivie.presentation.account;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import aivie.developer.aivie.R;
import aivie.developer.aivie.domain.model.UserProfileDetail;
import aivie.developer.aivie.util.Constant;

class LoginRepository {

    private static final String FIRE_COLLECTION = "users";
    private static final String FIRE_COLUMN_ID = "Id";
    private static final String FIRE_COLUMN_TITLE = "Title";
    private static final String FIRE_COLUMN_FIRSTNAME = "FirstName";
    private static final String FIRE_COLUMN_LASTNAME = "LastName";
    private static final String FIRE_COLUMN_DISPLAYNAME = "DisplayName";
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
    private String firstName;
    private String lastName;
    private String displayName;
    private String dateOfBirth;
    private String gender;
    private String race;
    private String ethnicity;
    private String subjectNum;
    private String role;
    private String patientOfStudy;
    private boolean isIcfSigned;
    private String siteId;
    private String siteDoctor;
    private String siteSC;
    private String sitePhone;

    LoginRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    void userLogin(Context context, String username, String password, final LoginContract.LoginCallback loginCallback) {

        if (username.equals("") || password.equals("")) {
            loginCallback.onFailure("Email or password can't be empty.");
            loginCallback.onComplete();
        }

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userId = mAuth.getCurrentUser().getUid();
                            loginCallback.onSuccess("signInWithEmail:success");
                        } else {
                            Log.i(Constant.TAG, String.format("signInWithEmail:failure %s", task.getException()));
                            loginCallback.onFailure(String.format("signInWithEmail:failure %s", task.getException()));
                        }
                        loginCallback.onComplete();
                    }
                });
    }

    void getUserProfile(final LoginContract.GetUserProfileCallback getUserProfileCallback) {

        DocumentReference docRefUser = db.collection(FIRE_COLLECTION).document(userId);
        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUser = task.getResult();
                    if (Objects.requireNonNull(documentUser).exists()) {

                        lastName = (String) documentUser.get(FIRE_COLUMN_LASTNAME);
                        firstName = (String) documentUser.get(FIRE_COLUMN_FIRSTNAME);
                        displayName = (String) documentUser.get(FIRE_COLUMN_DISPLAYNAME);

                        subjectNum = (String) documentUser.get(FIRE_COLUMN_SUBJECTNUM);
                        isIcfSigned = (boolean) documentUser.get(FIRE_COLUMN_EICF_SIGNED);

                        // Get user birthday
                        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
                        Timestamp tsBirthday = (Timestamp) documentUser.get(FIRE_COLUMN_DOB);
                        Date dateBirthday = tsBirthday.toDate();
                        dateOfBirth = sfd.format(dateBirthday);

                        siteId = (String) documentUser.get(FIRE_COLUMN_SITE_ID);
                        siteDoctor = (String) documentUser.get(FIRE_COLUMN_SITE_DOCTOR);
                        siteSC = (String) documentUser.get(FIRE_COLUMN_SITE_SC);
                        sitePhone = (String) documentUser.get(FIRE_COLUMN_SITE_PHONE);

                        getUserProfileCallback.onSuccess(documentUser);
                    } else {
                    }
                } else {
                }
            }
        });
    }

    void getUserGender(DocumentSnapshot documentUser, final  LoginContract.GetUserGenderCallback getUserGenderCallback) {

        DocumentReference docRefGender = (DocumentReference) documentUser.get(FIRE_COLUMN_GENDER);
        docRefGender.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUserGender = task.getResult();
                    if(Objects.requireNonNull(documentUserGender).exists()) {
                        gender = (String) documentUserGender.get(FIRE_COLUMN_TITLE);

                        getUserGenderCallback.onSuccess();
                    }
                }
            }
        });
    }

    void getUserRace(DocumentSnapshot documentUser, final  LoginContract.GetUserRaceCallback getUserRaceCallback) {

        DocumentReference docRefRace = (DocumentReference) documentUser.get(FIRE_COLUMN_RACE);
        docRefRace.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUserRace = task.getResult();
                    if(Objects.requireNonNull(documentUserRace).exists()) {
                        race = (String) documentUserRace.get(FIRE_COLUMN_TITLE);

                        getUserRaceCallback.onSuccess();
                    }
                }
            }
        });
    }

    void getUserEthinicity(DocumentSnapshot documentUser, final  LoginContract.GetUserEthnicityCallback getUserEthnicityCallback) {

        DocumentReference docRefRace = (DocumentReference) documentUser.get(FIRE_COLUMN_ETHICITY);
        docRefRace.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUserEthnicity = task.getResult();
                    if(Objects.requireNonNull(documentUserEthnicity).exists()) {
                        ethnicity = (String) documentUserEthnicity.get(FIRE_COLUMN_TITLE);

                        getUserEthnicityCallback.onSuccess();
                    }
                }
            }
        });
    }

    void getUserStudy(DocumentSnapshot documentUser, final  LoginContract.GetUserStudyCallback getUserStudyCallback) {

        DocumentReference docRefStudy = (DocumentReference) documentUser.get(FIRE_COLUMN_PATIENT_OF_STUDY);
        docRefStudy.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUserStudy = task.getResult();
                    if(Objects.requireNonNull(documentUserStudy).exists()) {
                        patientOfStudy = (String) documentUserStudy.get(FIRE_COLUMN_TITLE);

                        getUserStudyCallback.onSuccess();
                    }
                }
            }
        });
    }

    void getUserRole(DocumentSnapshot documentUser, final LoginContract.GetUserRoleCallback getUserRoleCallback ) {

        DocumentReference docRefRole = (DocumentReference) documentUser.get(FIRE_COLUMN_ROLE);
        docRefRole.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUserRole = task.getResult();
                    if(Objects.requireNonNull(documentUserRole).exists()) {

                        role = (String) documentUserRole.get(FIRE_COLUMN_ID);
                        Log.i(Constant.TAG, role);

                        getUserRoleCallback.onSuccess();
                    } else {
                    }
                } else {
                }
            }
        });
    }

    boolean isIcfSigned() {
        return isIcfSigned;
    }

    String getUserRole() {
        return role;
    }

    String getUserId() {
        return userId;
    }

    UserProfileDetail initUserProfileDetail() {

        return new UserProfileDetail(firstName, lastName, displayName, dateOfBirth,
                gender, race, ethnicity, subjectNum, role,
                patientOfStudy, isIcfSigned, siteId, siteDoctor, siteSC, sitePhone);
    }
}
