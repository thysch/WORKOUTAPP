package com.example.workoutlogger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SetAdapterTest {

    @Mock
    AddedExercisesAdapter.OnDataChangedListener mockListener;

    private Exercise makeExercise(String type) {
        Exercise exercise = new Exercise();
        exercise.type = type;
        return exercise;
    }

    @Test
    public void testViewTypeWeight() {
        Exercise exercise = makeExercise("WEIGHT");
        List<WorkoutSet> sets = List.of(new WorkoutSet());

        SetAdapter adapter = new SetAdapter(sets, exercise, mockListener);
        assertEquals(1, adapter.getItemViewType(0));
    }

    @Test
    public void testViewTypeCardio() {
        Exercise exercise = makeExercise("CARDIO");
        List<WorkoutSet> sets = List.of(new WorkoutSet());

        SetAdapter adapter = new SetAdapter(sets, exercise, mockListener);
        assertEquals(2, adapter.getItemViewType(0));
    }

    @Test
    public void testRemoveSet() {
        Exercise exercise = makeExercise("WEIGHT");

        List<WorkoutSet> sets = new ArrayList<>();
        sets.add(new WorkoutSet());
        sets.add(new WorkoutSet());

        SetAdapter adapter = new SetAdapter(sets, exercise, mockListener);

        sets.remove(0);
        adapter.notifyDataChanged();

        assertEquals(1, adapter.getItemCount());
        verify(mockListener, times(1)).onDataChanged();
    }

    @Test
    public void testSetWeightUpdates() {
        WorkoutSet set = new WorkoutSet();
        set.weight = 0;
        set.weight = Float.parseFloat("150");

        assertEquals(150f, set.weight, 0.0);
    }

    @Test
    public void testSetRepsUpdates() {
        WorkoutSet set = new WorkoutSet();
        set.plannedReps = "8";

        assertEquals("8", set.plannedReps);
    }
}
