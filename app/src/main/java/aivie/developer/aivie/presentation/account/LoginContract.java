package aivie.developer.aivie.presentation.account;

import com.google.firebase.firestore.DocumentSnapshot;

public interface LoginContract {

    interface LoginView {

        void ToastLoginResultMsg(String msg);

        void showProgress();

        void hideProgress();

        void enableLoginBtn();

        void disableLoginBtn();

        void enableNeedAccount();

        void disableNeedAccount();
    }

    interface LoginUserActions {

        void clickLogin(String username, String password);

        void clickGoToSignup();
    }

    interface LoginCallback {

        void onSuccess(String resultMsg);

        void onFailure(String msg);

        void onComplete();
    }

    interface GetUserProfileCallback {

        void onSuccess(DocumentSnapshot documentUser);
    }

    interface GetUserRoleCallback {

        void onSuccess();
    }
}
