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
import java.util.List;

import aivie.developer.aivie.Constant;
import aivie.developer.aivie.LoginActivity;
import aivie.developer.aivie.R;

public class HomeUserFragment extends Fragment {

    private HomeUserViewModel homeViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewRole;
    private TextView textViewStudyName;
    private TextView textViewVisitPlan;
    private String firstName;
    private String lastName;
    private String studyName;
    private String role;
    private ArrayList<String> visitPlan = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeUserViewModel.class);
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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imageViewAvatar = root.findViewById(R.id.imageViewAvatar);
        textViewName = root.findViewById(R.id.textViewName);
        textViewRole = root.findViewById(R.id.textViewRole);
        textViewStudyName = root.findViewById(R.id.textViewStudyTitle);
        textViewVisitPlan = root.findViewById(R.id.textViewVisitPlan);;

        showUserInfo();

        return root;
    }

    private void showUserInfo () {

        String userId = mAuth.getCurrentUser().getUid();

        if (userId == null) {
            // Put default data on screen
        } else {

            Glide.with(this).load(mAuth.getCurrentUser().getPhotoUrl()).into(imageViewAvatar);

            getUserProfileFromFirestore(userId);
        }
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
                                    if(Constant.DEBUG) Log.d(Constant.TAG, "task get failed with ", task.getException());
                                }
                            }
                        });


                        DocumentReference docRefStudy = (DocumentReference) documentUser.getData().get(getString(R.string.firestore_users_patient_of_study));
                        docRefStudy.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    DocumentSnapshot documentStudy = task.getResult();
                                    studyName = (String) documentStudy.get(getString(R.string.firestore_studies_title));

                                    List<Timestamp> visitsDate = (List<Timestamp>) documentStudy.getData().get(getString(R.string.firestore_studies_visit_plan));

                                    SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
                                    for (int i=0; i<visitsDate.size(); i++) {
                                        Timestamp tm = (Timestamp) visitsDate.get(i);
                                        Date date = tm.toDate();
                                        visitPlan.add(sfd.format(date).toString());
                                    }

                                    UpdateUI();
                                } else {
                                    if(Constant.DEBUG) Log.d(Constant.TAG, "task get failed with ", task.getException());
                                }
                            }
                        });

                    } else {
                        if(Constant.DEBUG) Log.d(Constant.TAG, "No such document");
                    }
                } else {
                    if(Constant.DEBUG) Log.d(Constant.TAG, "task get failed with ", task.getException());
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

        if (studyName != null) textViewStudyName.setText(studyName);

        StringBuilder sb = new StringBuilder("");
        textViewVisitPlan.setText("");
        for (int i=0; i<visitPlan.size(); i++) {
            sb.append(visitPlan.get(i) + "\r\n");
        }
        textViewVisitPlan.setText(sb.toString());
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
