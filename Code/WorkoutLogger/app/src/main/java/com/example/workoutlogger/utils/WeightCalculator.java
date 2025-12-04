package com.example.workoutlogger.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

public class WeightCalculator {

    private static final double[] PLATE_SIZES = {45, 25, 10, 5, 2.5};
    private static final double BARBELL_WEIGHT = 45;

    public static String calculatePlates(double totalWeight) {
        if (totalWeight <= BARBELL_WEIGHT) {
            return "Barbell only";
        }

        double weightPerSide = (totalWeight - BARBELL_WEIGHT) / 2.0;

        Map<Double, Integer> plates = new HashMap<>();
        for (double size : PLATE_SIZES) {
            int count = (int) (weightPerSide / size);
            if (count > 0) {
                plates.put(size, count);
                weightPerSide -= count * size;
            }
        }

        if (plates.isEmpty()) {
            return "Barbell only";
        }

        DecimalFormat df = new DecimalFormat("#.##");
        DecimalFormat dfWhole = new DecimalFormat("#");


        List<Double> sortedPlates = new ArrayList<>(plates.keySet());
        Collections.sort(sortedPlates, Collections.reverseOrder());

        StringBuilder result = new StringBuilder("Each side: ");
        for (int i = 0; i < sortedPlates.size(); i++) {
            double size = sortedPlates.get(i);
            int count = plates.get(size);
            if (size == (long) size) {
                result.append(count).append(" x ").append(dfWhole.format(size)).append(" lbs");
            } else {
                result.append(count).append(" x ").append(df.format(size)).append(" lbs");
            }
            if (i < sortedPlates.size() - 1) {
                result.append(", ");
            }
        }

        return result.toString();
    }
}
