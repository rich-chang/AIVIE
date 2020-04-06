package aivie.developer.aivie.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import aivie.developer.aivie.BuildConfig;
import aivie.developer.aivie.HomeActivity;
import aivie.developer.aivie.LoginActivity;
import aivie.developer.aivie.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseFirestore db;
    private TextView textViewName;
    private TextView textViewRole;
    private String userId;
    private String photoUri;
    private String firstName;
    private String lastName;
    private String studyName;
    private String role;
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
        photoUri = result.getString("PhotoUrl");
        studyName = result.getString("PatientOfStudy");
        visitPlan = result.getStringArrayList("VisitPlan");

        textViewName = root.findViewById(R.id.textViewName);
        textViewRole = root.findViewById(R.id.textViewRole);
        TextView textViewStudyName = root.findViewById(R.id.textViewStudyTitle);
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

        db = FirebaseFirestore.getInstance();

        getUserProfileFromFirestore(userId);

        return root;
    }

    private void getUserProfileFromFirestore (String userId) {

        DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentUser = task.getResult();
                    if (documentUser.exists()) {

                        lastName = (String) documentUser.get(getString(R.string.firestore_users_last_name));
                        firstName = (String) documentUser.get(getString(R.string.firestore_users_first_name));

                        UpdateUI();

                        DocumentReference docRefRole = (DocumentReference) documentUser.get(getString(R.string.firestore_users_role));
                        docRefRole.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    DocumentSnapshot documentRole = task.getResult();
                                    role = (String) documentRole.get("Title");
                                    
                                    UpdateUI();

                                } else {
                                    if(DEBUG) Log.d(TAG, "task get failed with ", task.getException());
                                }
                            }
                        });
                    } else {
                        if(DEBUG) Log.d(TAG, "No such document");
                    }
                } else {
                    if(DEBUG) Log.d(TAG, "task get failed with ", task.getException());
                }
            }
        });
    }

    private void UpdateUI () {

        if (role != null) textViewRole.setText(role);
        if (lastName != null && firstName != null) {
            String name = firstName + "  " + lastName;
            textViewName.setText(name);
        }
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
