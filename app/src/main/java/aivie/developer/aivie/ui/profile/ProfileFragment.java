package aivie.developer.aivie.ui.profile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import aivie.developer.aivie.BuildConfig;
import aivie.developer.aivie.FileDownloader;
import aivie.developer.aivie.HomeActivity;
import aivie.developer.aivie.IcfActivity;
import aivie.developer.aivie.LoginActivity;
import aivie.developer.aivie.R;
import aivie.developer.aivie.RaceSelectionActivity;

public class ProfileFragment extends Fragment {

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private ProfileViewModel profileViewModel;
    private static boolean DEBUG = BuildConfig.DEBUG;
    private static String TAG = "richc";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private EditText editTextSubjectNum;
    private EditText editTextSignedICF;
    private EditText editTextLastName;
    private EditText editTextFirstName;
    private EditText editTextDisplayName;
    private EditText editTextdateOfBirth;
    private EditText editTextAge;
    private EditText editTextGender;
    private EditText editTextRace;
    private EditText editTextEthnicity;
    private String icfFileUrl;
    private String icfFileName;
    private String signedIcfName;
    private String subjectNum;
    private String firstName;
    private String lastName;
    private String displayName;
    private String dateOfBirth;
    private String gender;
    private String race;
    private String ethnicity;
    private final Calendar myCalendar = Calendar.getInstance();

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /// DOB selection ///
        editTextdateOfBirth = (EditText) root.findViewById(R.id.dateOfBirth);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Log.d(TAG, "onDateSet");
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateOfBirthAndAge(null);
            }
        };
        editTextdateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog((HomeActivity)getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Listener for Race
        editTextRace = root.findViewById(R.id.race);
        editTextRace.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updateRace();
                return false;
            }
        });

        // Listener for Signed ICF
        editTextSignedICF = root.findViewById(R.id.signed_icf);
        editTextSignedICF.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    getFileFromFirebase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        editTextSubjectNum = root.findViewById(R.id.subjectNum);
        editTextLastName = root.findViewById(R.id.lastName);
        editTextFirstName = root.findViewById(R.id.firstName);
        editTextDisplayName = root.findViewById(R.id.displayName);
        editTextAge = root.findViewById(R.id.age);
        editTextGender = root.findViewById(R.id.gender);
        editTextEthnicity = root.findViewById(R.id.ethnicity);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance("gs://clinical-trials-772d5.appspot.com").getReference();

        showUserInfo();

        return root;
    }

    private void showUserInfo () {

        String userId = mAuth.getCurrentUser().getUid();

        if (userId == null) {
            // Put default data on screen
        } else {
            getUserProfileFromFirestore(userId);
        }
    }

    private void getUserProfileFromFirestore (String userId) {

        DocumentReference docRefUser = db.collection(getString(R.string.firestore_users)).document(userId);

        docRefUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    final DocumentSnapshot documentUser = task.getResult();
                    if (documentUser.exists()) {

                        subjectNum = (String) documentUser.get(getString(R.string.firestore_users_subject_num));
                        lastName = (String) documentUser.get(getString(R.string.firestore_users_last_name));
                        firstName = (String) documentUser.get(getString(R.string.firestore_users_first_name));
                        displayName = (String) documentUser.get(getString(R.string.firestore_users_display_name));

                        // Get user birthday
                        SimpleDateFormat sfd = new SimpleDateFormat(getString(R.string.yyyy_MM_dd));
                        Timestamp tsBirthday = (Timestamp) documentUser.get(getString(R.string.firestore_users_birthday));
                        Date dateBirthday = tsBirthday.toDate();
                        dateOfBirth = sfd.format(dateBirthday);

                        UpdateUI();

                        DocumentReference docRefGender = (DocumentReference) documentUser.get(getString(R.string.firestore_users_gender));
                        docRefGender.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    DocumentSnapshot documentGender = task.getResult();
                                    gender = (String) documentGender.get("Title");

                                    UpdateUI();
                                }
                            }
                        });

                        DocumentReference docRefRace = (DocumentReference) documentUser.get(getString(R.string.firestore_users_race));
                        docRefRace.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {

                                    DocumentSnapshot documentRace = task.getResult();
                                    race = (String) documentRace.get("Title");

                                    UpdateUI();
                                }
                            }
                        });

                        DocumentReference docRefEthnicity = (DocumentReference) documentUser.get(getString(R.string.firestore_users_ethnicity));
                        docRefEthnicity.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentEthnicity = task.getResult();
                                    ethnicity = (String) documentEthnicity.get("Title");

                                    UpdateUI();
                                }
                            }
                        });

                        DocumentReference docRefSignedICF = (DocumentReference) documentUser.get(getString(R.string.firestore_users_signed_icf));
                        docRefSignedICF.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentEthnicity = task.getResult();
                                    signedIcfName = (String) documentEthnicity.get("Title");

                                    UpdateUI();
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    private void UpdateUI () {

        if (subjectNum != null) editTextSubjectNum.setText(subjectNum);
        if (signedIcfName != null) editTextSignedICF.setText(signedIcfName);
        if (lastName != null) editTextLastName.setText(lastName);
        if (firstName != null) editTextFirstName.setText(firstName);
        if (displayName != null) editTextDisplayName.setText(displayName);
        if (gender != null) editTextGender.setText(gender);
        if (race != null) editTextRace.setText(race);
        if (ethnicity != null) editTextEthnicity.setText(ethnicity);

        updateDateOfBirthAndAge(dateOfBirth);
    }

    private void updateDateOfBirthAndAge (String dobString) {

        if (dobString == null) {

            SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.yyyy_MM_dd), Locale.US);
            dobString = sdf.format(myCalendar.getTime());
        }
        editTextdateOfBirth.setText(dobString);

        // Also update Age
        editTextAge.setText(Integer.valueOf(updateAge(dobString)).toString());
    }

    private int updateAge (String dobString) {

        // Construct DOB by input string
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.yyyy_MM_dd));
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (age < 0)
            age = 0;

        return age;
    }

    private void updateRace () {
        Intent intent = new Intent((HomeActivity)getActivity(), RaceSelectionActivity.class);
        startActivity(intent);
    }

    private void getFileFromFirebase() throws IOException {

        StorageReference docRefICF = storageRef.child("ICF/ICF0001.pdf");

        icfFileName = docRefICF.getName();

        docRefICF.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Got the download URL for 'users/me/profile.png'
                icfFileUrl = uri.toString();
                if (DEBUG) Log.d(TAG, icfFileName + " : " + icfFileUrl);

                downloadFile();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void viewICF () {
        //Intent intent = new Intent((HomeActivity)getActivity(), IcfActivity.class);
        //startActivity(intent);

        if (DEBUG) Log.v(TAG, "view() Method invoked ");

        if (!hasPermissions(getActivity(), PERMISSIONS)) {

            if (DEBUG) Log.v(TAG, "view() Method DON'T HAVE PERMISSIONS ");
            Toast.makeText(getActivity(), "You don't have read access !", Toast.LENGTH_LONG).show();
        } else {

            File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File pdfFile = new File(d, icfFileName);
            if (DEBUG) Log.v(TAG, "view() Method pdfFile " + pdfFile.getAbsolutePath());

            Uri path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
            if (DEBUG) Log.v(TAG, "view() Method path " + path);

            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "No Application available to view PDF", Toast.LENGTH_SHORT).show();
            }
        }
        if (DEBUG) Log.v(TAG, "view() Method completed ");
    }

    public void downloadFile() {
        if (DEBUG) Log.v(TAG, "download() Method invoked ");

        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            if (DEBUG) Log.v(TAG, "download() Method DON'T HAVE PERMISSIONS ");
            Toast.makeText(getActivity(), "You don't have write access !", Toast.LENGTH_LONG).show();
        } else {
            if (DEBUG) Log.v(TAG, "download() Method HAVE PERMISSIONS ");

            //new DownloadFile().execute("http://maven.apache.org/maven-1.x/maven.pdf", "maven.pdf");
            new DownloadFile().execute(icfFileUrl, icfFileName);
        }
        if (DEBUG) Log.v(TAG, "download() Method completed ");
    }

    private class DownloadFile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            if (DEBUG) Log.v(TAG, "doInBackground() Method invoked ");

            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(folder, fileName);

            if (DEBUG) Log.v(TAG, "doInBackground() pdfFile invoked " + pdfFile.getAbsolutePath());
            if (DEBUG) Log.v(TAG, "doInBackground() pdfFile invoked " + pdfFile.getAbsoluteFile());

            try {
                pdfFile.createNewFile();
                if (DEBUG) Log.v(TAG, "doInBackground() file created" + pdfFile);
            } catch (IOException e) {
                e.printStackTrace();
                if (DEBUG) Log.e(TAG, "doInBackground() error" + e.getMessage());
                if (DEBUG) Log.e(TAG, "doInBackground() error" + Arrays.toString(e.getStackTrace()));
            }

            FileDownloader.downloadFile(fileUrl, pdfFile);
            if (DEBUG) Log.v(TAG, "doInBackground() file download completed");

            viewICF();

            return null;
        }

        protected void onPostExecute(Long result) {
            if (DEBUG) Log.d(TAG, "Downloaded " + result + " bytes");
        }
    }
}
