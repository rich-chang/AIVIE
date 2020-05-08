package aivie.developer.aivie.presenter.account;

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

public class LoginActivity extends AppCompatActivity implements LoginContract.LoginView {

    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPresenter = new LoginPresenter(this, new LoginRepository());
        showSpString();
    }

    private void showSpString() {
        TextView textViewNeedAccount = findViewById(R.id.textViewNeedAccount);
        SpannableString sp1 = new SpannableString(getResources().getString(R.string.need_account));
        SpannableString sp2 = new SpannableString(getResources().getString(R.string.need_account_sign_up));
        sp2.setSpan(new ForegroundColorSpan(Color.BLUE), 0, sp2.length(), 0);
        sp2.setSpan(new UnderlineSpan(), 0, sp2.length(), 0);
        textViewNeedAccount.setText(TextUtils.concat(sp1, " ", sp2));
    }

    private EditText getUsernameViewById() {
        return (EditText) findViewById(R.id.username);
    }

    private EditText getPasswordViewById() {
        return (EditText) findViewById(R.id.password);
    }

    public void btnLogIn (View view) {
        loginPresenter.clickLogin(getUsernameViewById().getText().toString(), getPasswordViewById().getText().toString());
    }

    public  void goSignupActivity (View view) {
        loginPresenter.clickGoToSignup();
    }

    /**
     * LoginContract.LoginView interface override
     * */

    @Override
    public void ToastLoginResultMsg(String msg) {
        Log.d(Constant.TAG, "ToastLoginResultMsg: "+msg);
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgress() {
        findViewById(R.id.progressBarLogin).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        findViewById(R.id.progressBarLogin).setVisibility(View.GONE);
    }

    @Override
    public void enableLoginBtn() {
        findViewById(R.id.login).setEnabled(true);
    }

    @Override
    public void disableLoginBtn() {
        findViewById(R.id.login).setEnabled(false);
    }

    @Override
    public void enableNeedAccount() {
        findViewById(R.id.textViewNeedAccount).setEnabled(true);
    }

    @Override
    public void disableNeedAccount() {
        findViewById(R.id.textViewNeedAccount).setEnabled(false);
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
