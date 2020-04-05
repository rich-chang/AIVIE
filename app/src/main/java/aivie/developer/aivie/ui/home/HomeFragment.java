package aivie.developer.aivie.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import aivie.developer.aivie.HomeActivity;
import aivie.developer.aivie.LoginActivity;
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

        // Add option menu
        setHasOptionsMenu(true);

        HomeActivity activity = (HomeActivity)getActivity();
        Bundle result = activity.getHomeActivityData();

        userId = result.getString("UserID");
        displayName = result.getString("DisplayName");
        photoUri = result.getString("PhotoUrl");
        studyName = result.getString("PatientOfStudy");
        visitPlan = result.getStringArrayList("VisitPlan");

        TextView textViewDisplayName = root.findViewById(R.id.textViewDisplayName);
        TextView textViewStudyName = root.findViewById(R.id.textViewStudyTitle);
        textViewDisplayName.setText(displayName);
        textViewStudyName.setText(studyName);

        TextView textViewVisitPlan = root.findViewById(R.id.textViewVisitPlan);;
        StringBuilder sb = new StringBuilder("");
        textViewVisitPlan.setText("");
        for (int i=0; i<visitPlan.size(); i++) {
            sb.append(visitPlan.get(i) + "\r\n");
        }
        textViewVisitPlan.setText(sb.toString());

        ImageView imageViewAvatar = (ImageView)root.findViewById(R.id.imageViewAvatar);
        Glide.with(this).load(photoUri).into(imageViewAvatar);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.log_out:

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                ((Activity) getActivity()).overridePendingTransition(0, 0); //means no Animation in transition.

                break;
        }
        return true;
    }
}
