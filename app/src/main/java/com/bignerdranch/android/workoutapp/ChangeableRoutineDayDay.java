package com.bignerdranch.android.workoutapp;

import com.bignerdranch.android.workoutapp.model.RoutineDay;

/* Created this interface because of the issues I had with EditRoutineActivity and the ViewPager that loads the template routine days
   for each Routine that are hosted on EditRoutineFragment. I needed callbacks for the Fragments, but couldn't put this interface
   in EditRoutineActivity because of cyclical inheritance (I believe that was the error), since EditRoutineFragment already defines an
   interface that EditRoutineActivity implements. So EditRoutineFragment implements this interface so that when we get the Fragments
   hosted on the Activity (through keeping a list in the Activity that gets modified through Callback methods defined in the
   EditRouinteFragment.Callbacks interface), we can get the RoutineDay that is loaded on the Fragments and modify them.
 */
public interface ChangeableRoutineDayDay {
    public RoutineDay setRoutineDayDay(int dayNumber);
    public RoutineDay getRoutineDay();
}
