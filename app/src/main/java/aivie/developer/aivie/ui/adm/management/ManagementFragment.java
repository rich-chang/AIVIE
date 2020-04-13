package aivie.developer.aivie.ui.adm.management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import aivie.developer.aivie.HomeAdmActivity;
import aivie.developer.aivie.ProfileActivity;
import aivie.developer.aivie.R;
import aivie.developer.aivie.util.Constant;

import static aivie.developer.aivie.util.Constant.DEBUG;
import static aivie.developer.aivie.util.Constant.TAG;

public class ManagementFragment extends ListFragment {

    private ManagementViewModel managementViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    static ArrayList<String> patientsList = new ArrayList<>();
    static ArrayList<String> patients = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;
    private ProgressBar pbLoading;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        managementViewModel =
                ViewModelProviders.of(this).get(ManagementViewModel.class);
        View root = inflater.inflate(R.layout.fragment_management_adm, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        patients.clear();
        patientsList.clear();
        arrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, patientsList);
        setListAdapter(arrayAdapter);

        pbLoading = root.findViewById(R.id.progressBar);

        refresh();

        return root;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO implement some logic
        Log.d(TAG, position + ": " + patients.get(position));
        String userId = patients.get(position);

        Intent intent = new Intent((HomeAdmActivity)getActivity(), ProfileActivity.class);
        intent.putExtra("UserID", userId);
        startActivity(intent);
    }

    private void refresh() {

        pbLoading.setVisibility(View.VISIBLE);

        db.collection(getString(R.string.firestore_users))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot documentUser : task.getResult()) {

                                if (DEBUG) Log.d(TAG, documentUser.getId() + " => " + documentUser.getData());
                                final String lastName = (String) documentUser.get(getString(R.string.firestore_users_last_name));
                                final String firstName = (String) documentUser.get(getString(R.string.firestore_users_first_name));
                                final String userId = (String) documentUser.getId();

                                DocumentReference docRefRole = (DocumentReference) documentUser.get(getString(R.string.firestore_users_role));
                                docRefRole.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {

                                            DocumentSnapshot documentRole = task.getResult();
                                            if ((documentRole
                                                    .get(getString(R.string.firestore_column_id)))
                                                    .equals(getString(R.string.firestore_role_pt))) {

                                                patients.add(userId);
                                                patientsList.add(firstName + " " + lastName);
                                                arrayAdapter.notifyDataSetChanged();

                                                pbLoading.setVisibility(View.GONE);
                                             }

                                        } else {
                                            if(Constant.DEBUG) Log.d(Constant.TAG, "task get failed with ", task.getException());
                                        }
                                    }
                                });
                            }
                        } else {
                            if (DEBUG) Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
