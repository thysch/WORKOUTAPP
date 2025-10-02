
### Big Picture
The 2 main types of data we will want to save are *planned workouts*, and *saved workouts*.
Planned Workouts are what you select when you "start a workout".
Saved Workouts are what is saved when you complete an exercise.

#### Example Workout
Working Sets:
- Squat - 3 sets x 5 reps - 175 pounds
- Bench Press - 3 sets x 5 reps - 155 pounds
- Deadlift - 1 set x 5 reps - 195 pounds



Preplanned Weight / Reps concept:
- Before a workout, the user has the option to select the number of reps and sets they are aiming for. 
	- The reps may be entered as a range, or as a single number
- The user also has the option to select a target weight.
	- The weight may be set to lbs or kg in the settings
- The user should have the option to enter any combination of what they choose. This could be only "3 sets". Only "200 pounds". Or "3 sets of 8 reps". Or "3 sets of 8 reps at 200 pounds". They could also enter nothing.


<br>

PREPLANNED WORKOUT DATA:

<br>
This is how the User will pre-plan a Workout by entering multiple exercises. The idea is that the user can be as specific or unspecific as they like. They should at least enter a name for the Exercise but that is all that is required. If they want, they can enter Reps, Sets, and Weight values.
<br>
E.g. (this is what will be displayed in "Workout Overview Screen"):
- **Deadlift
- **Deadlift** - *3 sets
- **Deadlift** - *3 sets of 5 reps
- **Deadlift** - *3 sets of 5 reps - 510 lbs.
- **Deadlift** - *510 lbs.
- **Deadlift** - *2 sets - 510 lbs.
- etc.
- **Treadmill Running**  *- 30 minutes
- **Treadmill Running**  *- 5 km
- **Treadmill Running**  *- 30 minutes
- **Jump Rope** *- 300 reps


<br>


Example Procedure:
1. The User is on the <mark style="background: #FFF3A3A6;">home screen</mark>.
	1. The home screen will have a section called "My Workouts"
	2. There will be a button to "Add a Workout" on the home screen (likely bottom-right).
	3. Clicking "Add a Workout" changes the screen to the "<mark style="background: #BBFABBA6;">Workout Overview Screen</mark>"
2. On the "<mark style="background: #BBFABBA6;">Workout Overview Screen</mark>", 
	1. there is a button to "Add an Exercise"
	2. There could be a button for "Workout Type" { HIIT, Yoga, Cardio}. HIIT Would have the exercises done in loops. 
	3. Will list the Exercises in the Workout. 
	4. Having sub-text of Reps, Sets, and Weight would also be good.
	5. Exercises should be able to be added, deleted, edited or moved. 
		1. The best way to do this would be a small overlay menu that opens when an exercise is held. This should have "Delete", "Edit" options
		2. Clicking and holding on an Exercise should float it up so that it can be moved.
	6. Clicking "Add an Exercise" will open the <mark style="background: #ABF7F7A6;">"Exercise Overview Screen"</mark>
3. On the <mark style="background: #ABF7F7A6;">"Exercise Overview Screen"</mark>:
	1. I picture this panel as being a Smaller Sub-panel on top of the UI. It should be clear that 
	2. There needs to be a drop-down or radio button for "Exercise Type": {Cardio, Weightlifting, (Future? :  Pilates, Stretching) }. To start, we can just have "Cardio" and "Weightlifting". ***Note: we could also just have weightlifting and a small section for "Cardio? " in the overall workout information
	3. Exercise Name Field: E.g. "Squat", "Barbell Squat", "Barbell High Squat"
	4. "Enter a Rep Range" checkmark for "Reps Field"
	5. "Reps Field":  1 integer number (no range) ,or 2 integer numbers (rep range)
	6. "Sets" Field: 1 integer value. 
	7. "Target Weight" field. *****For Now, We will not have the option to have different weights for different sets. This will have to be entered as a separate "exercise". We can have a "Duplicate" button to make this easier to do.
	8. There should be options for "Save" and "Cancel". 
		1. Save will save the exercise to the workout.
		2. Cancel will automatically cancel the exercise. We should have some "Are you sure?" Dialog.
- Hitting Cancel will go back to the "<mark style="background: #BBFABBA6;">Workout Overview Screen</mark>"

<br>
<br>
## Data Types:

#### Routine 
- contains 1 or more workouts
- Name
<br>

#### Workout
- routine ID
- contains 1 or more exercises

<br>

#### Exercise - 2 Types for now
#### ExerciseWeights
- Name*
- Reps1
- Reps2
- Sets
- Weight
- Notes

### ExerciseCardio (optional)
- Name
- Time (cardio)
- Distance (km/mi) (cardio)
- Speed (mph/kph - optional)
- Resistance Level:
- Incline (cardio)
- Calories Burned
- Notes 

<br>

\* Names should be searchable but also customizable. Previously entered names should be regarded as official "Exercises" and will record their own records.


Example Structure (Pseudo-Code):

new Routine "My Routine" {
	new Workout "My Workout" {
		 ExerciseWeight "MyExercise1" = new ExerciseWeights("Bench Press", 8, 12, 3, 155, "" );
		 ExerciseCardio "MyExercise2" = new  ExerciseCardio("Treadmill Running", 15, 0, 0, 0, "" );
		 ExerciseWeight "MyExercise3" = new ExerciseWeights("Cable Crossovers", 12, 0, 3, 30, "");
		 
	}
}

<br>
---
## Start Workout Function

After clicking on a Workout and hitting "Start Workout", the user will go to the <mark style="background: #FFB8EBA6;"><mark style="background: #FF5582A6;">"Workout View Screen"</mark></mark>  
Big Picture
- This Screen should look very similar to the <mark style="background: #BBFABBA6;">"Workout Overview Screen"</mark>
- Things to display:
-   "Total Weight Moved in Workout" - would be a cool stat. Just weight times reps for each set that you enter and add it up.
- Workout Uptime: "just counts up in time"
- Workout Start Time: "When the workout started"
- "Start Workout" button 
	- "Pause Workout" button
		- Pauses the Workout Uptime
		- Shows a Play button when it is paused
	- "Stop Workout" button.
- Back button - (Stops workout and goes to home screen)

<br>
<br>


- Clicking on an Exercise will now allow you to record data 
- Once you hit Start, you will start recording all the sets:
- It will say something like "Bench Press - Set 1/3"
   "My Previous Best: 155 lbs." "Target Weight: 160 lbs." "Target Reps: 5-8"
- There needs to be buttons to add another set or take away sets.
- Hitting an exercise which is already done could say "Do another set?" (Yes/No), which would add a set here.


<br>
<br>

### Data Type

When a workout is started, and more specifically when an "Exercise" is started from a Workout, the "Exercises" are simply expanded by repeating the "Sets" as needed. There will be an option to skip a Set or Add a Set if you want to do less or more. Therefore there is no need to save "Set_IDs". 
<br>
Completing 1 "Set" will save a new "Exercise" object in the "Workout" data table. The "SavedExercise" data type will also have types "SavedExerciseCardio", "SavedExerciseWeights" 
<br>
Example of "SavedExerciseWeights"

- Name = High Barbell Squat
- WorkoutID (workout starting timestamp)
- Reps  = 10
- // Set = 1 always so it is not included
- Weight = 45
- Notes = "Almost got 8 but last was sketchy."
- Rest Time
<br><br>
- Name = High Barbell Squat
- WorkoutID
- Reps  = 8
- Weight = 95
- Notes = ""
- Rest Time
<br><br>

- Name = High Barbell Squat
- WorkoutID
- Reps  = 5
- Weight = 135
- Notes = ""
- Rest Time
  
  

- Name = High Barbell Squat
- WorkoutID
- Reps  = 5
- Weight = 155
- Notes = ""
- Rest Time

<br>
- Name = High Barbell Squat
- WorkoutID
- Reps  = 3
- Weight = 185
- Notes = ""
- Rest Time
<br>
<br>


- Name = Bench Press
- WorkoutID
- Reps  = 7
- // Set = 1 always so it is not included
- Weight = 100
- Notes = "Almost got 8 but last was sketchy."
- Rest Time
<br><br>
- Name = Bench Press
- WorkoutID
- Reps  = 8
- Weight = 120
- Notes = ""
- Rest Time
<br>
- Name = Bench Press
- WorkoutID
- Reps  = 5
- Weight = 150
- Notes = ""
- Rest Time



<br>
- Name = Deadlift
- WorkoutID
- Reps  = 10
- Weight = 65
- Notes = "Warm Up"
- Rest Time
<br>

- Name = Deadlift
- WorkoutID
- Reps  = 8
- Weight = 95
- Notes = ""
- Rest Time
<br>

- Name = Deadlift
- WorkoutID
- Reps  = 6
- Weight = 135
- Notes = ""
- Rest Time
<br>

- Name = Deadlift
- WorkoutID
- Reps  = 5
- Weight = 185
- Notes = ""
- Rest Time
<br>


Example of SavedExerciseCardio
- Name = "Running"
- WorkoutID
- Time = 15:00
- Distance  = 3 miles
- Speed = 5 mph
- Resistance Level: 0
- Incline (cardio) = 0.0
- Calories Burned = 0
- Notes = ""






