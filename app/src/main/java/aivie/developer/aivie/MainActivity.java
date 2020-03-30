package aivie.developer.aivie;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCenter.start(getApplication(), "a0aeb136-9ca9-4f08-9502-f81f0d39a301", Analytics.class, Crashes.class);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);


    }
}
