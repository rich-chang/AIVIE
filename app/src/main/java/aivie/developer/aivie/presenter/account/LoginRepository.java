package aivie.developer.aivie.presenter.account;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import aivie.developer.aivie.R;
import aivie.developer.aivie.util.Constant;

class LoginRepository {

    private static final String FIRE_COLLECTION = "users";
    private static final String FIRE_COLUMN_ICF_SIGNED = "eICF_Signed";
    private static final String FIRE_COLUMN_ROLE = "Role";
    private static final String FIRE_COLUMN_ID = "Id";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String userRole;

    LoginRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = "";
        userRole = "";
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

                        getUserProfileCallback.onSuccess(documentUser);
                    } else {
                    }
                } else {
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

                        userRole = (String) documentUserRole.get(FIRE_COLUMN_ID);
                        Log.i(Constant.TAG, userRole);

                        getUserRoleCallback.onSuccess();
                    } else {
                    }
                } else {
                }
            }
        });
    }

    boolean isIcfSigned(DocumentSnapshot documentUser) {
        boolean isSigned = false;
        if (Objects.requireNonNull(documentUser).exists()) {
            isSigned = (boolean) documentUser.get(FIRE_COLUMN_ICF_SIGNED);
        }
        return isSigned;
    }

    String userRole() {
        return userRole;
    }

    String userId() {
        return userId;
    }
}
