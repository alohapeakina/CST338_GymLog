package com.example.gymlog.database;

import android.app.Application;
import android.util.Log;
import com.example.gymlog.database.entities.GymLog;
import com.example.gymlog.MainActivity;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Andrew Lee
 * created 7/11/2025
 * Explanation:
 */
public class GymLogRepository {
  private GymLogDAO gymLogDAO;
  private ArrayList<GymLog> allLogs;

  private static GymLogRepository repository;

  private GymLogRepository(Application application){
    GymLogDatabase db = GymLogDatabase.getDatabase(application);
    this.gymLogDAO = db.gymLogDAO();
    this.allLogs = (ArrayList<GymLog>) this.gymLogDAO.getALlRecords();
  }

  public static GymLogRepository getRepository(Application application){
    if (repository != null){
      return repository;
    }
    Future<GymLogRepository> future = GymLogDatabase.databaseWriteExecutor.submit(
        new Callable<GymLogRepository>() {
          @Override
          public GymLogRepository call() throws Exception {
            return new GymLogRepository(application);
          }
        }
    );
    try{
      return future.get();
    }catch (InterruptedException| ExecutionException e){
      Log.i(MainActivity.TAG,"Problem getting GymLogRepository, thread error.");
    }
    return null;
  }

  public ArrayList<GymLog> getAllLogs() {
    Future<ArrayList<GymLog>> future = GymLogDatabase.databaseWriteExecutor.submit(
        new Callable<ArrayList<GymLog>>() {
          @Override
          public ArrayList<GymLog> call() throws Exception {
            return (ArrayList<GymLog>) gymLogDAO.getALlRecords();
          }
        });
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      Log.i(MainActivity.TAG, "Problem when getting all GymLogs in the repository");
    }
    return null;
  }

  public void insertGymLog(GymLog gymLog){
    GymLogDatabase.databaseWriteExecutor.execute(()->
    {
      gymLogDAO.insert(gymLog);

    });
  }
}
