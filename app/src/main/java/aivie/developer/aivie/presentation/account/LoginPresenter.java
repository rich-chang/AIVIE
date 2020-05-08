package aivie.developer.aivie.presentation.account;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.firestore.DocumentSnapshot;

import aivie.developer.aivie.HomeAdmActivity;
import aivie.developer.aivie.HomeUserActivity;
import aivie.developer.aivie.IcfActivity;
import aivie.developer.aivie.SignupActivity;

public class LoginPresenter implements LoginContract.LoginUserActions {

    private LoginRepository loginRepository;
    private LoginContract.LoginView loginView;

    LoginPresenter(LoginContract.LoginView loginView, LoginRepository loginRepository) {
        this.loginView = loginView;
        this.loginRepository = loginRepository;
    }

    @Override
    public void clickLogin(String username, String password) {

        loginView.showProgress();
        loginView.disableLoginBtn();
        loginView.disableNeedAccount();

        loginRepository.userLogin((Context) loginView, username, password, new LoginContract.LoginCallback() {
            @Override
            public void onSuccess(String resultMsg) {
                //loginView.ToastLoginResultMsg(resultMsg);
            }

            @Override
            public void onFailure(String resultMsg) {
                //loginView.ToastLoginResultMsg(resultMsg);
            }

            @Override
            public void onComplete() {

                loginRepository.getUserProfile(new LoginContract.GetUserProfileCallback() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentUser) {

                        final boolean isIcfSigned = loginRepository.isIcfSigned(documentUser);

                        loginRepository.getUserRole(documentUser, new LoginContract.GetUserRoleCallback() {
                            @Override
                            public void onSuccess() {

                                loginView.hideProgress();
                                loginView.enableLoginBtn();
                                loginView.enableNeedAccount();

                                switch (loginRepository.userRole()) {
                                    case "PT":
                                        if (isIcfSigned) {
                                            goToUserHome();
                                        } else {
                                            goToSignICF();
                                        }
                                        break;
                                    case "SC":
                                        goToAdmHome();
                                        break;
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void clickGoToSignup() {
        Intent intent = new Intent((Context) loginView, SignupActivity.class);
        ((Context) loginView).startActivity(intent);
    }

    private void goToSignICF() {
        Intent intent = new Intent((Context) loginView, IcfActivity.class);
        intent.putExtra("UserID", loginRepository.userId());
        ((Context) loginView).startActivity(intent);
    }

    private void goToUserHome() {
        Intent intent = new Intent((Context) loginView, HomeUserActivity.class);
        ((Context) loginView).startActivity(intent);
    }

    private void goToAdmHome() {
        Intent intent = new Intent((Context) loginView, HomeAdmActivity.class);
        ((Context) loginView).startActivity(intent);
    }
}
