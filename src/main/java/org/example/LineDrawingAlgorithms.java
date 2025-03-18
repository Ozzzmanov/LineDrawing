package org.example;

import java.util.ArrayList;
import java.util.List;

public class LineDrawingAlgorithms {

    // Алгоритм DDA для малювання ліній
    public static List<Point> dda(Line line) {
        List<Point> points = new ArrayList<>();

        float x1 = line.getStart().getX();
        float y1 = line.getStart().getY();
        float x2 = line.getEnd().getX();
        float y2 = line.getEnd().getY();

        float dx = x2 - x1; // Різниця координат по X
        float dy = y2 - y1; // Різниця координат по Y

        float steps = Math.max(Math.abs(dx), Math.abs(dy)); // Визначення кількості кроків
        float xIncrement = dx / steps; // Зміна координати X на кожному кроці
        float yIncrement = dy / steps; // Зміна координати Y на кожному кроці

        float x = x1;
        float y = y1;

        points.add(new Point(Math.round(x), Math.round(y))); // Додаємо початкову точку

        for (int i = 0; i < steps; i++) {
            x += xIncrement; // Оновлюємо X
            y += yIncrement; // Оновлюємо Y
            points.add(new Point(Math.round(x), Math.round(y))); // Додаємо нову точку
        }

        return points;
    }

    // Алгоритм Брезенхема для малювання ліній
    public static List<Point> bresenham(Line line) {
        List<Point> points = new ArrayList<>();

        int x1 = Math.round(line.getStart().getX());
        int y1 = Math.round(line.getStart().getY());
        int x2 = Math.round(line.getEnd().getX());
        int y2 = Math.round(line.getEnd().getY());

        int dx = Math.abs(x2 - x1); // Абсолютна різниця координат по X
        int dy = Math.abs(y2 - y1); // Абсолютна різниця координат по Y

        int sx = x1 < x2 ? 1 : -1; // Напрямок руху по X
        int sy = y1 < y2 ? 1 : -1; // Напрямок руху по Y

        int err = dx - dy; // Початкова похибка
        int e2;

        while (true) {
            points.add(new Point(x1, y1)); // Додаємо поточну точку

            if (x1 == x2 && y1 == y2) { // Перевірка завершення алгоритму
                break;
            }

            e2 = 2 * err; // Подвоєна похибка
            if (e2 > -dy) { // Корекція по X
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) { // Корекція по Y
                err += dx;
                y1 += sy;
            }
        }

        return points;
    }
}
