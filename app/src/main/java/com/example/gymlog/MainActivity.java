package com.example.gymlog;

import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gymlog.database.GymLogRepository;
import com.example.gymlog.database.entities.GymLog;
import com.example.gymlog.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author Andrew Lee
 * Created: 7/11/2025
 * Explanation: Mobile app to log and track gym activities
 */
public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private GymLogRepository repository;

  public static final String TAG = "GYMLOG";
  String mExercise = "";
  double mWeight = 0.0;
  int mReps = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

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

  private void insertGymLogRecord(){
    if (mExercise.isEmpty()){
      return;
    }
    GymLog log = new GymLog(mExercise, mWeight, mReps);
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