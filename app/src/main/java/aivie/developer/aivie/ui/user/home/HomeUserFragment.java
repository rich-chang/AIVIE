package aivie.developer.aivie.ui.user.home;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import aivie.developer.aivie.HomeAdmActivity;
import aivie.developer.aivie.util.Constant;
import aivie.developer.aivie.LoginActivity;
import aivie.developer.aivie.R;
import aivie.developer.aivie.util.NotificationPublisher;

import static android.content.Context.MODE_PRIVATE;

public class HomeUserFragment extends Fragment {

    private HomeUserViewModel homeViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedpreferences;
    private ImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewRole;
    private TextView textViewSiteId;
    private TextView textViewSiteDoctor;
    private TextView textViewSiteSC;
    private TextView textViewSitePhoneNum;
    private TextView textViewStudyName;
    private TextView textViewVisitPlan;
    private String firstName;
    private String lastName;
    private String studyName;
    private String role;
    private String SiteId;
    private String SiteDoctor;
    private String SiteSC;
    private String SitePhone;
    private ArrayList<String> visitPlan = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeUserViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home_user, container, false);
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
        sharedpreferences = getActivity().getSharedPreferences(Constant.SP_NAME, MODE_PRIVATE);

        imageViewAvatar = root.findViewById(R.id.imageViewAvatar);
        textViewName = root.findViewById(R.id.textViewName);
        textViewRole = root.findViewById(R.id.textViewRole);
        textViewSiteId = root.findViewById(R.id.textViewSiteId);
        textViewSiteDoctor = root.findViewById(R.id.textViewSiteDoctor);
        textViewSiteSC = root.findViewById(R.id.textViewSiteSC);
        textViewSitePhoneNum = root.findViewById(R.id.textViewSitePhoneNum);
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
                        SiteId = (String) documentUser.get("SiteId");
                        SiteDoctor = (String) documentUser.get("SiteDoctor");
                        SiteSC = (String) documentUser.get("SiteSC");
                        SitePhone = (String) documentUser.get("SitePhone");

                        UpdateUI();

                        final DocumentReference docRefRole = (DocumentReference) documentUser.get(getString(R.string.firestore_users_role));
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

                                    for (int i=0; i<visitsDate.size(); i++) {

                                        Timestamp tm = visitsDate.get(i);
                                        Date date = tm.toDate();

                                        SimpleDateFormat sfdFull = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
                                        visitPlan.add(sfdFull.format(date));

                                        if (!sharedpreferences.getBoolean(Constant.SP_KEY_INIT_REMINDER, false)) {

                                            /// Schedule notification ///
                                            long delayInMs = date.getTime() - new Date().getTime();
                                            if (Constant.DEBUG)
                                                Log.d(Constant.TAG, "Delay in milli :: " + delayInMs);
                                            scheduleNotification(
                                                    getNotification("Your doctor appointment " + visitPlan.get(i)),
                                                    (int) delayInMs,
                                                    i, i);
                                        }
                                    }

                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putBoolean(Constant.SP_KEY_INIT_REMINDER, true).apply();

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

        textViewSiteId.setText(SiteId);
        textViewSiteDoctor.setText(SiteDoctor);
        textViewSiteSC.setText(SiteSC);
        textViewSitePhoneNum.setText(SitePhone);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_home_user_menu, menu);
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

    private void scheduleNotification(Notification notification, int delay, int requestCode, int notificationId) {

        long futureInMillis = SystemClock.elapsedRealtime() + delay;

        Intent notificationIntent = new Intent(getActivity(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getActivity(),
                requestCode,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {

        Notification.Builder builder = new Notification.Builder(getActivity());

        builder.setContentTitle("Visit Reminder");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(false);
        builder.setGroup(Constant.GROUP_VISIT_REMINDER);
        builder.setDefaults(Notification.DEFAULT_ALL);

        return builder.build();

        /*
        //Step3. 透過 Notification.Builder 來建構 notification，
                //並直接使用其.build() 的方法將設定好屬性的 Builder 轉換
                //成 notification，最後開始將顯示通知訊息發送至狀態列上。
                Notification notification
                   = new Notification.Builder(MainActivity.this)
                   .setContentIntent(appIntent)
                   .setSmallIcon(R.drawable.ic_launcher) // 設置狀態列裡面的圖示（小圖示）　　
                   .setLargeIcon(BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.ic_launcher)) // 下拉下拉清單裡面的圖示（大圖示）
                   .setTicker("notification on status bar.") // 設置狀態列的顯示的資訊
                   .setWhen(System.currentTimeMillis())// 設置時間發生時間
                   .setAutoCancel(false) // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                   .setContentTitle("Notification Title") // 設置下拉清單裡的標題
                   .setContentText("Notification Content")// 設置上下文內容
                   .setOngoing(true)      //true使notification變為ongoing，用戶不能手動清除// notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
                   .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
//                 .setDefaults(Notification.DEFAULT_VIBRATE) //使用默認手機震動提示
//                 .setDefaults(Notification.DEFAULT_SOUND) //使用默認聲音提示
//                 .setDefaults(Notification.DEFAULT_LIGHTS) //使用默認閃光提示
//                 .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND) //使用默認閃光提示 與 默認聲音提示

                   .setVibrate(vibrate) //自訂震動長度
//                 .setSound(uri) //自訂鈴聲
//                 .setLights(0xff00ff00, 300, 1000) //自訂燈光閃爍 (ledARGB, ledOnMS, ledOffMS)

                   .build();

                // 將此通知放到通知欄的"Ongoing"即"正在運行"組中
                notification.flags = Notification.FLAG_ONGOING_EVENT;

                // 表明在點擊了通知欄中的"清除通知"後，此通知不清除，
                // 經常與FLAG_ONGOING_EVENT一起使用
                notification.flags = Notification.FLAG_NO_CLEAR;

                //閃爍燈光
                notification.flags = Notification.FLAG_SHOW_LIGHTS;

                // 重複的聲響,直到用戶響應。
                notification.flags = Notification.FLAG_INSISTENT; /


                // 把指定ID的通知持久的發送到狀態條上.
                mNotificationManager.notify(0, notification);

                // 取消以前顯示的一個指定ID的通知.假如是一個短暫的通知，
                // 試圖將之隱藏，假如是一個持久的通知，將之從狀態列中移走.
//              mNotificationManager.cancel(0);

                //取消以前顯示的所有通知.
//              mNotificationManager.cancelAll();
        */
    }
}
