package aivie.developer.aivie.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;

import aivie.developer.aivie.HomeActivity;
import aivie.developer.aivie.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    String userId;
    String displayName;
    String photoUri;
    String studyName;
    private ArrayList<String> visitPlan = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        HomeActivity activity = (HomeActivity)getActivity();
        Bundle result = activity.getHomeActivityData();

        userId = result.getString("UserID");
        displayName = result.getString("DisplayName");
        photoUri = result.getString("PhotoUrl");
        studyName = result.getString("PatientOfStudy");
        visitPlan = result.getStringArrayList("VisitPlan");

        Log.i("richc", "HomeFrag Visits Plan: " + visitPlan.toString());

        TextView textViewDisplayName = root.findViewById(R.id.textViewDisplayName);
        TextView textViewStudyName = root.findViewById(R.id.textViewStudyName);
        textViewDisplayName.setText(displayName);
        textViewStudyName.setText(studyName);

        /// Add date into linear layout
        LinearLayout llVisitPlan = (LinearLayout) root.findViewById(R.id.linearLayoutVisitDate);
        for (int i=0; i<visitPlan.size(); i++) {
            TextView textViewVisitPlan = new TextView(getActivity());
            textViewVisitPlan.setText(visitPlan.get(i));
            textViewVisitPlan.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            llVisitPlan.addView(textViewVisitPlan);
        }

        return root;
    }
}
