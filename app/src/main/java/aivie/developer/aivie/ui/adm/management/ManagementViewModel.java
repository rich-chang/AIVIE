package aivie.developer.aivie.ui.adm.management;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ManagementViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ManagementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}