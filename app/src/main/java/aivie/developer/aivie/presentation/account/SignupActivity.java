package aivie.developer.aivie.presentation.account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import aivie.developer.aivie.R;
import aivie.developer.aivie.util.Constant;

public class SignupActivity extends AppCompatActivity implements SignupContract.SignupView {

    private SignupPresenter signupPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signupPresenter = new SignupPresenter(this, new SignupRepository());
        showSpString();
    }

    private void showSpString() {
        TextView textViewHaveAccount = findViewById(R.id.textViewHaveAccount);
        SpannableString sp1 = new SpannableString(getResources().getString(R.string.have_account));
        SpannableString sp2 = new SpannableString(getResources().getString(R.string.have_account_log_in));
        sp2.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp2.length(), 0);
        sp2.setSpan(new UnderlineSpan(), 0, sp2.length(), 0);
        textViewHaveAccount.setText(TextUtils.concat(sp1, " ", sp2));
    }

    private EditText getUsernameViewById() {
        return (EditText) findViewById(R.id.username);
    }

    private EditText getPasswordViewById() {
        return (EditText) findViewById(R.id.password);
    }

    private EditText getFirstNameViewById() {
        return (EditText) findViewById(R.id.firstName);
    }

    private EditText getLastNameViewById() {
        return (EditText) findViewById(R.id.lastName);
    }

    public void btnSignUp (View view) {
        signupPresenter.getInputUserFullName(getFirstNameViewById().getText().toString(), getLastNameViewById().getText().toString());
        signupPresenter.clickSignup(getUsernameViewById().getText().toString(), getPasswordViewById().getText().toString());
    }

    public void goLoginActivity (View view) {
        signupPresenter.clickGoToLogin();
    }

    /**
     * LoginContract.LoginView interface override
     * */

    @Override
    public void ToastLoginResultMsg(String msg) {
        Log.d(Constant.TAG, "ToastLoginResultMsg: "+msg);
        Toast.makeText(SignupActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgress() {
        findViewById(R.id.progressBarSignup).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        findViewById(R.id.progressBarSignup).setVisibility(View.GONE);
    }

    @Override
    public void enableSignupBtn() {
        findViewById(R.id.signup).setEnabled(true);
    }

    @Override
    public void disableSignupBtn() {
        findViewById(R.id.signup).setEnabled(false);
    }

    @Override
    public void enableHaveAccount() {
        findViewById(R.id.textViewHaveAccount).setEnabled(true);
    }

    @Override
    public void disableHaveAccount() {
        findViewById(R.id.textViewHaveAccount).setEnabled(false);
    }

    /**
     * Android native method override
     * */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        //moveTaskToBack(true); // disable going back to the MainActivity
    }
}
