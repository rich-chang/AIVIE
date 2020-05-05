package aivie.developer.aivie.ui.user;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import aivie.developer.aivie.R;
import aivie.developer.aivie.util.Constant;

public class AdverseEventsUserFragment extends Fragment {

    private AdverseEventsUserViewModel mViewModel;
    private ArrayList<Boolean> adverseEvents = new ArrayList<Boolean>();


    public static AdverseEventsUserFragment newInstance() {
        return new AdverseEventsUserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.adverse_events_user_fragment, container, false);

        for (int i=0; i< Constant.adverse_events_count; i++) {
            adverseEvents.add(false);
        }
        Log.i(Constant.TAG, adverseEvents.toString());

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AdverseEventsUserViewModel.class);
        // TODO: Use the ViewModel
    }

}
