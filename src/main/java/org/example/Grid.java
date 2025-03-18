package org.example;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    public static List<Point> gridInit(float orthoWidth, float orthoHeight) {
        float gridSize = 1f; // Шаг сетки
        List<Point> points = new ArrayList<>();

        int columns = (int) (orthoWidth / gridSize);
        int rows = (int) (orthoHeight / gridSize);

        for (int i = -rows / 2; i <= rows / 2; i++) {
            for (int j = -columns / 2; j <= columns / 2; j++) {
                points.add(new Point(j * gridSize, i * gridSize));
            }
        }
        return points;
    }
}
