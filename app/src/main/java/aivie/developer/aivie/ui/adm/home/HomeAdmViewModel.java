package aivie.developer.aivie.ui.adm.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeAdmViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeAdmViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}