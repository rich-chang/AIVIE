package aivie.developer.aivie.ui.adm.management;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import aivie.developer.aivie.HomeAdmActivity;
import aivie.developer.aivie.R;

import static aivie.developer.aivie.util.Constant.DEBUG;
import static aivie.developer.aivie.util.Constant.TAG;

public class ManagementFragment extends Fragment {

    private ManagementViewModel managementViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    ListView ptListView;
    static ArrayList<String> patients = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        managementViewModel =
                ViewModelProviders.of(this).get(ManagementViewModel.class);
        View root = inflater.inflate(R.layout.fragment_management_adm, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        managementViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ptListView = root.findViewById(R.id.ptListView);
        arrayAdapter = new ArrayAdapter<>((HomeAdmActivity) Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, patients);
        ptListView.setAdapter(arrayAdapter);
        ptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        refresh();

        return root;
    }

    private void refresh() {

        db.collection(getString(R.string.firestore_users))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (DEBUG) Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            if (DEBUG) Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
