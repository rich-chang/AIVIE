package aivie.developer.aivie.ui.adm.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import aivie.developer.aivie.R;

public class HomeAdmFragment extends Fragment {

    private HomeAdmViewModel homeAdmViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeAdmViewModel =
                ViewModelProviders.of(this).get(HomeAdmViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home_adm, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeAdmViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
