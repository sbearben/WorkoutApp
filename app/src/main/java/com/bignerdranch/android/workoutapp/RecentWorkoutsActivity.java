package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.support.v4.app.Fragment;

import com.bignerdranch.android.workoutapp.global.AppExecutors;
import com.bignerdranch.android.workoutapp.global.DataRepository;

public class RecentWorkoutsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return RecentWorkoutsFragment.newInstance();
    }

    /**
     * Android Application class. Used for accessing singletons.
     */
    public static class BasicApp extends Application {

        private AppExecutors mAppExecutors;

        @Override
        public void onCreate() {
            super.onCreate();
            mAppExecutors = new AppExecutors();
        }

        public AppDatabase  getDatabase() {
            return AppDatabase.getInstance(this, mAppExecutors);
        }

        public DataRepository getRepository() {
            return DataRepository.getInstance(getDatabase());
        }
    }
}
