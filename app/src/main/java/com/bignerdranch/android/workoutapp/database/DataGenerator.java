/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bignerdranch.android.workoutapp.database;

import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    private static final String[] routine_names = new String[]{
            "SL 5x5", "Aesthetic Workout"};
    private static final String[][][] routine_days = new String[][][] {
            { { "Squat", "Overhead Press", "Deadlift", "Weighted Chinups", "Barbell Curls", "Bent Over Flys" },
              { "Squat", "Bench Press", "Barbell Row", "Skullcrushers", "Shoulder Raises" }},
            { { "Squats", "Machine Chest Press", "Dumbbell Row", "Military Press", "Close Grip Bench Press", "Dumbbell Curls" },
              { "Leg Extensions", "Pec Dec", "Lat Pull Down", "Side Lateral Raise", "Cable Tricep Extensions", "EZ Bar Preacher Curls" },
              { "Deadlift", "Leg Press", "Bench Press", "Machine Shoulder Press", "Dumbbell Skullcrushers", "Chin Ups" }}
    };
    private static final int[] number_sets_options = new int[] {
            2, 3, 4, 5
    };
    private static final int[] target_weight_options = new int[] {
            60, 90, 135, 180, 225, 275
    };
    private static final int[] number_reps_options = new int[] {
            5, 8, 10, 12, 15
    };


    public static List<Routine> generateRoutines() {
        List<Routine> routines = new ArrayList<>(2);
        Random rnd = new Random();

        for (int i = 0; i < routine_names.length; i++) {
            Routine routine = new Routine();

            routine.setName(routine_names[i]);
            routine.setDateCreated(new GregorianCalendar(2017, rnd.nextInt(12)+1, rnd.nextInt(27)+1).getTime());
            routine.setId(i + 1);

            routines.add(routine);
        }

        return routines;
    }

    public static List<RoutineDay> generateRoutineDays (final List<Routine> routines) {
        List<RoutineDay> routineDays = new ArrayList<>();
        Random rnd = new Random();

        for (Routine routine : routines) {
            int numDaysInRoutine = routine_days[routines.indexOf(routine)].length;
            int currentDayNumber = 1;

            for (int i = 0; i < 20; i++) {
                RoutineDay routineDay = new RoutineDay();

                routineDay.setId(1000 + 20*routines.indexOf(routine) + i);
                routineDay.setRoutineId(routine.getId());
                routineDay.setDayNumber(currentDayNumber);
                routineDay.setDate(new GregorianCalendar(2018, routines.indexOf(routine)+1, i+1).getTime());
                routineDay.setCompleted(true);

                routineDays.add(routineDay);
                currentDayNumber = (i%numDaysInRoutine) + 1;
            }
        }

        return routineDays;
    }

    public static List<Exercise> generateExercises (final List<RoutineDay> routineDays, final List<Routine> routines) {
        List<Exercise> exercises = new ArrayList<>();
        Random rnd = new Random();

        for (RoutineDay routineDay : routineDays) {
            int routineId = routineDay.getRoutineId();
            int routineIndex = 0;

            for (Routine routine : routines) {
                if (routineId == routine.getId()) {
                    routineIndex = routines.indexOf(routine);
                }
            }

            int upper = routine_days[routineIndex][routineDay.getDayNumber()-1].length;
            for (int i = 0; i < upper; i++) {
                Exercise exercise = new Exercise();

                exercise.setId(2000 + upper*routineDays.indexOf(routineDay) + i);
                exercise.setRoutineDayId(routineDay.getId());
                exercise.setName(routine_days[routineIndex][routineDay.getDayNumber()-1][i]);
                exercise.setNumber(i+1);
                exercise.setType(Exercise.REPPED);

                exercises.add(exercise);
            }
        }

        return exercises;
    }

    public static List<ReppedSet> generateReppedSets (final List<Exercise> exercises) {
        List<ReppedSet> reppedSets = new ArrayList<>();
        Random rnd = new Random();

        for (Exercise exercise : exercises) {
            int numSetsInExercise = rnd.nextInt(number_sets_options.length);

            int upper = number_sets_options[numSetsInExercise];
            for (int i=0; i < upper; i++) {
                ReppedSet reppedSet = new ReppedSet();

                reppedSet.setId(3000 + upper*exercises.indexOf(exercise) + i);
                reppedSet.setExerciseId(exercise.getId());
                reppedSet.setSetNumber(i+1);
                reppedSet.setTargetWeight(target_weight_options[rnd.nextInt(target_weight_options.length)]);
                reppedSet.setTargetMeasurement(number_reps_options[rnd.nextInt(number_reps_options.length)]);
                reppedSet.setActualMeasurement(reppedSet.getTargetMeasurement() - rnd.nextInt(4));


                reppedSets.add(reppedSet);

            }
        }

        return reppedSets;
    }
}