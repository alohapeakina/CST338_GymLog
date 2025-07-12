package com.example.gymlog;

import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gymlog.databinding.ActivityMainBinding;
import java.util.Locale;

/**
 * @author Andrew Lee
 * Created: 7/11/2025
 * Explanation: Mobile app to log and track gym activities
 */
public class MainActivity extends AppCompatActivity {

  ActivityMainBinding binding;

  public static final String TAG = "GYMLOG";
  String mExercise = "";
  double mWeight = 0.0;
  int mReps = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    binding.logDisplayTextView.setMovementMethod(new ScrollingMovementMethod());

    binding.logButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getInformationFromDisplay();
        updateDisplay();
      }
    });
  }

  private void updateDisplay(){
    String currentInfo = binding.logDisplayTextView.getText().toString();
    Log.d(TAG,"current info: " + currentInfo);
    String newDisplay = String.format(Locale.US,"Execise:%s%nWeight:%.2f%nReps:%d%n=-=-=-=%n%s",mExercise,mWeight,mReps,currentInfo);

    binding.logDisplayTextView.setText(newDisplay);
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