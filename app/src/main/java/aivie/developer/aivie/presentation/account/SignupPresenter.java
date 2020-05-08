package aivie.developer.aivie.presentation.account;

import android.content.Context;
import android.content.Intent;

public class SignupPresenter implements SignupContract.SignupUserActions {

    private SignupRepository signupRepository;
    private SignupContract.SignupView signupView;

    SignupPresenter(SignupContract.SignupView signupView, SignupRepository signupRepository) {
        this.signupView = signupView;
        this.signupRepository = signupRepository;
    }

    @Override
    public void getInputUserFullName(String firstName, String lastName) {
        signupRepository.setUserFullName(firstName, lastName);
    }

    @Override
    public void clickSignup(String username, String password) {

        signupView.showProgress();
        signupView.disableSignupBtn();
        signupView.disableHaveAccount();
        final String[] result = {""};

        signupRepository.userSignup((Context) signupView, username, password, new SignupContract.SignupCallback() {
            @Override
            public void onSuccess(String resultMsg) {
                result[0] = resultMsg;
            }

            @Override
            public void onFailure(String resultMsg) {
                signupView.ToastLoginResultMsg(resultMsg);
                signupView.hideProgress();
                signupView.enableSignupBtn();
                signupView.enableHaveAccount();
            }

            @Override
            public void onComplete() {

                signupRepository.createTempUserDataInFireDB(new SignupContract.CreateTempDataCallback() {
                    @Override
                    public void onSuccess() {
                        signupView.ToastLoginResultMsg(result[0]);
                        clickGoToLogin();
                    }

                    @Override
                    public void onFailure() {
                        signupView.ToastLoginResultMsg(result[0]);
                    }

                    @Override
                    public void onComplete() {
                        signupView.hideProgress();
                        signupView.enableSignupBtn();
                        signupView.enableHaveAccount();
                    }
                });
            }
        });
    }

    @Override
    public void clickGoToLogin() {
        Intent intent = new Intent((Context) signupView, LoginActivity.class);
        ((Context) signupView).startActivity(intent);
    }
}
