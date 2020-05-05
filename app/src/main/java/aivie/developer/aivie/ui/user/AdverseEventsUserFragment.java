package aivie.developer.aivie.ui.user;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import aivie.developer.aivie.R;

public class AdverseEventsUserFragment extends Fragment {

    private AdverseEventsUserViewModel mViewModel;

    public static AdverseEventsUserFragment newInstance() {
        return new AdverseEventsUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.adverse_events_user_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AdverseEventsUserViewModel.class);
        // TODO: Use the ViewModel
    }

}
