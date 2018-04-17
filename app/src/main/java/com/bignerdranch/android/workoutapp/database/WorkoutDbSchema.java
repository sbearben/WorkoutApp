package com.bignerdranch.android.workoutapp.database;

public class WorkoutDbSchema {

    public static final class RoutineTable {

        // Name of the table in the database
        public static final String NAME = "routines";

        public static final class Cols {
            public static final String ROUTINE_ID = "routine_id";
            public static final String ROUTINE_NAME = "routine_name";
            public static final String ROUTINE_DATE_CREATED = "routine_date_created";
        }
    }

    public static final class RoutineDayTable {

        public static final String NAME = "routine_days";

        public static final class Cols {
            public static final String ROUTINE_DAY_ID = "routine_id";
            public static final String ROUTINE_DAY_NUM = "routine_day_number";
            public static final String ROUTINE_DAY_DATE = "routine_day_date_performed";
        }
    }

    public static final class ExerciseTable {

        public static final String NAME = "exercises";

        public static final class Cols {
            public static final String EXERCISE_ID = "exercise_id";
            public static final String EXERCISE_NAME = "exercise_name";
            public static final String EXERCISE_NUM = "exercise_number";
            public static final String EXERCISE_TYPE = "exercise_type";
        }
    }

    public static final class SetTable {

        public static final String NAME = "sets";

        public static final class Cols {
            public static final String SET_ID = "set_id";
            public static final String SET_NUM = "set_number";
            public static final String SET_TARGET_WEIGHT = "set_target_weight";
            public static final String SET_TARGET_MEASUREMENT = "set_target_measurement";
            public static final String SET_ACTUAL_MEASUREMENT = "set_actual_measurement";
        }
    }

    public static final class ReppedSetTable {
        public static final String NAME = "repped_sets";
    }

    public static final class TimedSetTable {
        public static final String NAME = "timed_sets";
    }


}
