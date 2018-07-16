# WorkoutApp
This is my first attempt at developing an Android app. I made this mostly for learning purposes, however it's simple layout and usage is something I've wanted in a workout app.

This is a bare bones workout app that allows you to add any number of routines that consist of workout days, which consist of exercises, which consist of sets, etc.

The UI is similar to the SL 5x5 app that is available for both Android and iOS. Of all the workout apps I've used, I've enjoyed the SL 5x5 app the most due to its design. However, it is limited in use as it was created around one workout routine.

### Usage
Clone the repository and import the root folder into your IDE (tested on Android Studio), then run project.
The app was created with a **Min SDK Version of 19** and **Target SDK Version of 27**.

### Data Generation
There is code within the buildatabase(..) method of database/AppDatabase.java to prepopulate the database. If you'd like to use it, uncomment the section of code where the comment "Generate the data for pre-population" is. AppDatabase contains a MutableLiveData boolean field that RecentWorkoutsFragment.java observes. The MutableLiveData field posts a true value once the database has been created, at which point RecentWorkoutsFragment will refresh its Views.

### Built With
The app is built using an MVC architecture, and uses [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room) to manage the database.

### Potential Future Features

* Ability to undo deletion of exercises/workout days/routines
* Addition of timed exercises as opposed to only repped
* Timer in between sets

### Screenshots

<img src="https://github.com/sbearben/WorkoutApp/blob/screenshots/WorkoutApp-Recent_Workouts.png?raw=true" width="30%"> . <img src="https://github.com/sbearben/WorkoutApp/blob/screenshots/WorkoutApp-Routine_List.png?raw=true" width="30%"> . <img src="https://github.com/sbearben/WorkoutApp/blob/screenshots/WorkoutApp-Routine_History.png?raw=true" width="30%">
</br>
<img src="https://github.com/sbearben/WorkoutApp/blob/screenshots/WorkoutApp-Workout_Day.png?raw=true" width="30%"> . <img src="https://github.com/sbearben/WorkoutApp/blob/screenshots/WorkoutApp-Edit_Routine.png?raw=true" width="30%"> . <img src="https://github.com/sbearben/WorkoutApp/blob/screenshots/WorkoutApp-New_Exercise_Dialog.png?raw=true" width="30%">

### License
Copyright (C) 2018 Armon Khosravi

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
