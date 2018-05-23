package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringDef;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineDayTable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;


@Entity(tableName = ExerciseTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = RoutineDay.class,
                parentColumns = RoutineDayTable.Cols.ROUTINE_DAY_ID,
                childColumns = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID,
                onDelete = CASCADE,
                onUpdate = RESTRICT)},
        indices = { @Index(value = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID)}
        )
public class Exercise implements Copyable<Exercise> {

    public static final String REPPED = "Repped";
    public static final String TIMED = "Timed";

    public static final int MAX_SETS = 5;
    public static final int DEFAULT_SETS = 3;

    @RestrictTo(LIBRARY_GROUP)
    @StringDef({NEWLINE_NORMAL, NEWLINE_HTML, NEWLINE_NULL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NewlineCharacter {}

    public static final String NEWLINE_NORMAL = "\n";
    public static final String NEWLINE_HTML = "<br/>";
    public static final String NEWLINE_NULL = "";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_ID)
    private int id;

    @ColumnInfo(name = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID)
    private int routineDayId;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NAME)
    private String name;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NUM)
    private int number; // is the number of the exercise in the routine - might not be needed

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NUM_SETS)
    private int targetNumberSets;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_TYPE)
    private String type;

    @Ignore
    private List<Set> sets = null;


    @Ignore
    public Exercise() {
        this.sets = new ArrayList<>();
    }

    public Exercise (int id, int routineDayId, String name, int number, int targetNumberSets, String type) {
        //mNumberSets = 3;
        this.id = id;
        this.routineDayId = routineDayId;
        this.name = name;
        this.number = number;
        this.targetNumberSets = targetNumberSets;
        this.type = type;

        this.sets = new ArrayList<>();
        //initializeSets();
    }

    @Ignore
    public Exercise (int routineDayId, String name, int number, int targetNumberSets, String type) {
        this.routineDayId = routineDayId;
        this.name = name;
        this.number = number;
        this.targetNumberSets = targetNumberSets;
        this.type = type;

        this.sets = new ArrayList<>();

    }

    @Override
    public Exercise createCopy() {
        return new Exercise(this.routineDayId, this.name, this.number, this.targetNumberSets, this.type);
    }

    @Override
    public Exercise createDeepCopy() {
        Exercise exercise = this.createCopy();
        for (Set exerciseSet : this.getSets()) {
            exercise.addSet(exerciseSet.createCopy());
        }

        return exercise;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoutineDayId() {
        return this.routineDayId;
    }

    public void setRoutineDayId(int routineDayId) {
        this.routineDayId = routineDayId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getTargetNumberSets() {
        return targetNumberSets;
    }

    public void setTargetNumberSets(int targetNumberSets) {
        this.targetNumberSets = targetNumberSets;
    }

    public String getType() {
        return this.type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public List<Set> getSets() {
        return this.sets;
    }

    public Set getSet (int id) {
        for (Set set : this.sets) {
            if (set.getId() == id) {
                return set;
            }
        }
        return null;
    }

    public void addSet (Set set) {
        this.sets.add(set);
    }

    public void addSets (List<Set> sets) {
        this.sets.addAll(sets);
    }

    @Override
    public String toString() {
        String str = "";

        str += "Exercise: " + getId() + ", Name: " + getName() + "\n" +
                "\t" + "Parent RoutineDay Id: " + getRoutineDayId() + ", Exercise#: " + getNumber() + "\n" +
                "\t" + "Target # sets: " + getTargetNumberSets() + ", Type: " + getType() + "\n";
        if (sets != null) {
            for (Set set : sets) {
                str += set.toString();
            }
        }

        return str;
    }

    // Static method to make sure a given string is a valid exercise type (should maybe be keeping types in a final List?)
    public static boolean isValidExerciseType(String type) {
        return type.equals(Exercise.REPPED) || type.equals(Exercise.TIMED);
    }

    // TODO: This function hardcodes the set weights in pounds - eventually need to add flexibility for switch between pounds and kg and also TimedSets
    // If we want the returned String to be multiline, set multilineSeparator = Exercise.NEWLINE_NORMAL or Exercise.NEWLINE_HTML - if not set it to Exercise.NEWLINE_NULL
    public static String createExerciseDetailsString (Exercise exercise, @Exercise.NewlineCharacter String multilineSeparator) {
        List<Set> exerciseSets = exercise.getSets();
        String detailsString = "";
        StringBuilder sb = new StringBuilder(30);
        int targetNumSets = exercise.getTargetNumberSets();

        if (exercise.getType().equals(Exercise.REPPED)) {
            int maxTargetWeight = 0;
            int previousSetTargetWeight = 0;
            //int maxTargetReps = 0;
            int maxActualReps = 0;
            int previousSetTargetReps = 0;

            boolean allSetsSameTargetWeight = true; // flag to determine if all sets in exercise are at the same target weight
            boolean allSetsSameTargetReps = true; // flag to determine if all sets in exercise are at the same target reps
            boolean allSetsSuccessfullyCompleted = true; // flag to determine if all sets were successful (aka we hit our target number of reps)
            int skippedSetCount = 0; // integer to keep track of how many sets were skipped - if this count equals the number of sets, then we know the exercise was completely skipped
            boolean exerciseSkipped = true; // flag to determine of the exercise was completely skipped

            // Loop through each reppedSet in the exercise
            for (Set reppedSet : exerciseSets) {
                int setTargetWeight = reppedSet.getTargetWeight();
                int setTargetReps = ((ReppedSet) reppedSet).getTargetMeasurement();
                int setActualReps = ((ReppedSet) reppedSet).getActualMeasurement();

                // Logic to get the exercise's highest weight set
                if (setTargetWeight > maxTargetWeight) {
                    maxTargetWeight = setTargetWeight;
                }
                // If at any point, the current set targetWeight is not equal to the previous set targetWeight, then we know all sets are not the same target weight
                if (setTargetWeight != previousSetTargetWeight && exerciseSets.indexOf(reppedSet) > 0) { // The index check is because we don't want this performed on the first set since there isn't a previous one at that point
                    allSetsSameTargetWeight = false;
                }
                // Logic to get the exercise's highest actual reps set
                if (setActualReps > maxActualReps) {
                    maxActualReps = setActualReps;
                }
                if (setTargetReps != previousSetTargetReps && exerciseSets.indexOf(reppedSet) > 0) {
                    allSetsSameTargetReps = false;
                }
                // If at any point, the current set targetReps is not equal to the set actualReps, then we know that all of the exercise's sets were not completed successfully
                if (setActualReps != setTargetReps) {
                    allSetsSuccessfullyCompleted = false;
                }

                previousSetTargetWeight = setTargetWeight;
                previousSetTargetReps = setTargetReps;

                // If the setActualReps equals ReppedSet.ACTUAL_REPS_NULL the set has been skipped, so we increment our skippedSetCount
                if (setActualReps == ReppedSet.ACTUAL_REPS_NULL) {
                    skippedSetCount++;
                    sb.append("‒/");
                    continue; // We want to go back to the top of the for loop since the code below that adds to detailsString will double count the set if we don't

                }

                sb.append(setActualReps);
                sb.append("/");
            }

            sb.append(multilineSeparator.equals(Exercise.NEWLINE_NULL) ? "  " : multilineSeparator);
            sb.append(maxTargetWeight);
            sb.append("lb");  // TODO: change hardcode of lb (pounds) here

            detailsString = sb.toString();

            if ((allSetsSameTargetWeight && allSetsSameTargetReps && allSetsSuccessfullyCompleted) || (targetNumSets == 1 && skippedSetCount == 0)) {
                detailsString = targetNumSets + "x" + maxActualReps + (multilineSeparator.equals(Exercise.NEWLINE_NULL) ? " " : multilineSeparator) + maxTargetWeight + "lbs";  // TODO: change hardcode of lbs (pounds) here
            } else if (targetNumSets == skippedSetCount) {
                detailsString = "Skipped";
            }
        } else if (exercise.getType().equals(Exercise.TIMED)) {
            // Some other logic here for when we implement timed sets - a lot of it will be similar to the above, so we'll have to make the above code more abstract eventually
        }

        return detailsString;
    }
}
