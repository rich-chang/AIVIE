package aivie.developer.aivie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RaceSelectionActivity extends AppCompatActivity {

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseFirestore db;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button buttonConfirm;
    ArrayList<String> race_desc = new ArrayList<String>();
    ArrayList<String> race = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_selection);

        db = FirebaseFirestore.getInstance();

        radioGroup = findViewById(R.id.radioGroup);
        buttonConfirm = findViewById(R.id.buttonConfirm);

        addListenerOnButton();
        updateUI();
    }

    public void addListenerOnButton() {

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioButton = (RadioButton) findViewById(selectedId);

                Toast.makeText(RaceSelectionActivity.this, radioButton.getText(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateUI() {

        race.add(getResources().getString(R.string.race_white));
        race_desc.add(getResources().getString(R.string.race_white_desc));
        race.add(getResources().getString(R.string.race_asian));
        race_desc.add(getResources().getString(R.string.race_asian_desc));
        race.add(getResources().getString(R.string.race_black));
        race_desc.add(getResources().getString(R.string.race_black_desc));
        race.add(getResources().getString(R.string.race_hawaiian));
        race_desc.add(getResources().getString(R.string.race_hawaiian_desc));
        race.add(getResources().getString(R.string.race_alaska));
        race_desc.add(getResources().getString(R.string.race_alaska_desc));

        for (int i = 0; i < radioGroup .getChildCount(); i++) {

            // Implement customized string
            SpannableString sp1 = new SpannableString(race.get(i));
            SpannableString sp2 = new SpannableString(race_desc.get(i));

            if (i == 3) {
                sp1.setSpan(new RelativeSizeSpan(1.1f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (i == 4) {
                sp1.setSpan(new RelativeSizeSpan(1.2f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sp1.setSpan(new RelativeSizeSpan(1.4f), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            sp1.setSpan(new StyleSpan(Typeface.BOLD), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp1.setSpan(new UnderlineSpan(), 0, sp1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, sp2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((RadioButton) radioGroup.getChildAt(i)).setText(TextUtils.concat(sp1, "\r\n", sp2));
        }

    }
}
