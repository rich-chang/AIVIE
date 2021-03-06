package aivie.developer.aivie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    String userId;
    String displayName;
    String photoUri;
    String studyName;
    private ArrayList<String> visitPlan = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserID");
        displayName = intent.getStringExtra("DisplayName");
        photoUri = intent.getStringExtra("PhotoUrl");
        studyName = intent.getStringExtra("PatientOfStudy");
        visitPlan = getIntent().getStringArrayListExtra("VisitPlan");

        Log.i("richc", "HomeAct Visits Plan: " + visitPlan.toString());
    }

    public Bundle getHomeActivityData() {

        Bundle bundle = new Bundle();
        bundle.putString("UserID", userId);
        bundle.putString("DisplayName", displayName);
        bundle.putString("PhotoUrl", photoUri);
        bundle.putString("PatientOfStudy", studyName);
        bundle.putStringArrayList("VisitPlan", visitPlan);

        return bundle;
    }

}
