package com.example.gymlog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gymlog.database.GymLogRepository;
import com.example.gymlog.database.entities.GymLog;
import com.example.gymlog.database.entities.User;
import com.example.gymlog.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Andrew Lee
 * Created: 7/11/2025
 * Explanation: Mobile app to log and track gym activities
 */
public class MainActivity extends AppCompatActivity {

  private static final String MAIN_ACTIVITY_USER_ID = "com.example.gymlog.MAIN_ACTIVITY_USER_ID";
  private ActivityMainBinding binding;
  private GymLogRepository repository;

  public static final String TAG = "GYMLOG";
  String mExercise = "";
  double mWeight = 0.0;
  int mReps = 0;

  //TODO: Add login information
  private int loggedInUserId = -1;
  private User user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    loginUser();
    invalidateOptionsMenu();

    if(loggedInUserId == -1){
      Intent intent = LoginActivity.loginIntentFactory(getApplicationContext());
      startActivity(intent);
    }


    repository = GymLogRepository.getRepository(getApplication());
    binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());
    updateDisplay();
    binding.logButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getInformationFromDisplay();
        insertGymLogRecord();
        updateDisplay();
      }
    });
  }

  private void loginUser() {
    //TODO: Make login method functional
    user = new User("Andrew","password");
    loggedInUserId = getIntent().getIntExtra(MAIN_ACTIVITY_USER_ID,-1);
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
    //TODO: Finish logout method
    startActivity(LoginActivity.loginIntentFactory(getApplicationContext()));
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

  private void updateDisplay(){
    ArrayList<GymLog> allLogs = repository.getAllLogs();
    if( allLogs.isEmpty() ){
      binding.logDisplayTextView.setText(R.string.nothing_to_show_time_to_hit_the_gym);
    }
    StringBuilder sb = new StringBuilder();
    for (GymLog log : allLogs){
      sb.append(log);
    }
    binding.logDisplayTextView.setText(sb.toString());
  }

  private void getInformationFromDisplay(){
    mExercise = binding.exerciseInputEditText.getText().toString();
    try {
      mWeight = Double.parseDouble(binding.weightInputEditText.getText().toString());
    } catch (NumberFormatException e) {
      Log.d(TAG,"Error reading value from Weight edit text.");
    }
    try {
      mReps = Integer.parseInt(binding.repInputEditText.getText().toString());
    } catch (NumberFormatException e) {
      Log.d(TAG,"Error reading value from reps edit text.");
    }

  }
}