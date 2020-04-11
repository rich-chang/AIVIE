package aivie.developer.aivie;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class HomeUserActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, 
                R.id.navigation_profile,
                R.id.navigation_dashboard,
                R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        /*
        Intent intent = getIntent();
        userId = intent.getStringExtra("UserID");
        displayName = intent.getStringExtra("DisplayName");
        photoUri = intent.getStringExtra("PhotoUrl");
        studyName = intent.getStringExtra("PatientOfStudy");
        visitPlan = getIntent().getStringArrayListExtra("VisitPlan");
        */
    }

    /*
    public Bundle getHomeActivityData() {

        Bundle bundle = new Bundle();
        bundle.putString("UserID", userId);
        bundle.putString("DisplayName", displayName);
        bundle.putString("PhotoUrl", photoUri);
        bundle.putString("PatientOfStudy", studyName);
        bundle.putStringArrayList("VisitPlan", visitPlan);

        return bundle;
    }
    */

    @Override
    public void onBackPressed() {
        // empty so nothing happens
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
