package com.example.gymlog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gymlog.database.GymLogRepository;
import com.example.gymlog.database.entities.GymLog;
import com.example.gymlog.database.entities.User;
import com.example.gymlog.databinding.ActivityMainBinding;
import com.example.gymlog.viewHolders.GymLogAdapter;
import com.example.gymlog.viewHolders.GymLogViewModel;
import java.util.ArrayList;

/**
 * @author Andrew Lee
 * Created: 7/11/2025
 * Explanation: Mobile app to log and track gym activities
 */
public class MainActivity extends AppCompatActivity {

  private static final String MAIN_ACTIVITY_USER_ID = "com.example.gymlog.MAIN_ACTIVITY_USER_ID";
  static final String SHARED_PREFERENCE_USERID_KEY = "com.example.gymlog.SHARED_PREFERENCE_USERID_KEY";
  static final String SAVED_INSTANCE_STATE_USERID_KEY = "com.example.gymlog.SAVED_INSTANCE_STATE_USERID_KEY";
  private static final int LOGGED_OUT = -1;
  private ActivityMainBinding binding;
  private GymLogRepository repository;
  private GymLogViewModel gymLogViewModel;

  public static final String TAG = "GYMLOG";
  String mExercise = "";
  double mWeight = 0.0;
  int mReps = 0;

  private int loggedInUserId = -1;
  private User user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    gymLogViewModel = new ViewModelProvider(this).get(GymLogViewModel.class);

    RecyclerView recyclerView = binding.logDisplayRecyclerView;
    final GymLogAdapter adapter = new GymLogAdapter(new GymLogAdapter.GymLogDiff());
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    repository = GymLogRepository.getRepository(getApplication());
    loginUser(savedInstanceState);

    gymLogViewModel.getAllLogsById(loggedInUserId).observe(this,gymLogs -> {
      adapter.submitList(gymLogs);
    });

    //User is not logged in at this point, go to login screen
    if(loggedInUserId == -1){
      Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
      startActivity(intent);
    }

    updateSharedPreference();
  }


  private void loginUser(Bundle savedInstanceState) {
    //Check shared preference for logged in user read from the file
    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),
        Context.MODE_PRIVATE);

    loggedInUserId = sharedPreferences.getInt(getString(R.string.preference_userid_key),LOGGED_OUT);

    if (loggedInUserId == LOGGED_OUT && savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE_STATE_USERID_KEY)){
      loggedInUserId = savedInstanceState.getInt(SAVED_INSTANCE_STATE_USERID_KEY,LOGGED_OUT);
    }
    if(loggedInUserId == LOGGED_OUT){
      loggedInUserId = getIntent().getIntExtra(MAIN_ACTIVITY_USER_ID,LOGGED_OUT);
    }
    if(loggedInUserId == LOGGED_OUT){
      return;
    }
    LiveData<User> userObserver = repository.getUserByUserId(loggedInUserId);
    userObserver.observe(this,user -> {
      this.user = user;
      if(this.user != null){
        invalidateOptionsMenu();
      }
    });
  }

  @Override
  protected void onSaveInstanceState(@Nullable Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(SAVED_INSTANCE_STATE_USERID_KEY,loggedInUserId);
    updateSharedPreference();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.logout_menu,menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.logoutMenuItem);
    item.setVisible(true);
    if(user == null){
      return false;
    }
    item.setTitle(user.getUsername());
    item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
        showLogoutDialog();
        return false;
      }
    });
    return true;
  }

  private void showLogoutDialog(){
    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
    final AlertDialog alertDialog = alertBuilder.create();

    alertBuilder.setMessage("Logout?");

    alertBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        logout();
      }
    });

    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        alertDialog.dismiss();
      }
    });

    alertBuilder.create().show();

  }

  private void logout() {

    loggedInUserId = LOGGED_OUT;
    updateSharedPreference();
    getIntent().putExtra(MAIN_ACTIVITY_USER_ID,LOGGED_OUT);

    startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
  }

  private void updateSharedPreference(){
    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
        Context.MODE_PRIVATE);
    SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
    sharedPrefEditor.putInt(getString(R.string.preference_userid_key),loggedInUserId);
    sharedPrefEditor.apply();
  }

  static Intent mainActivityIntentFactory(Context context, int userID){
    Intent intent = new Intent(context, MainActivity.class);
    intent.putExtra(MAIN_ACTIVITY_USER_ID,userID);
    return intent;
  }

  private void insertGymLogRecord(){
    if (mExercise.isEmpty()){
      return;
    }
    GymLog log = new GymLog(mExercise, mWeight, mReps, loggedInUserId);
    repository.insertGymLog(log);
  }

  private void getInformationFromDisplay(){
    mExercise = binding.exerciseInputEditText.getText().toString();
    try {
      mWeight = Double.parseDouble(binding.weightInputEditText.getText().toString());
    } catch (NumberFormatException e) {
      Log.d(TAG,"Error reading value from weight edit text.");
    }
    try {
      mReps = Integer.parseInt(binding.repInputEditText.getText().toString());
    } catch (NumberFormatException e) {
      Log.d(TAG,"Error reading value from reps edit text.");
    }

  }
}