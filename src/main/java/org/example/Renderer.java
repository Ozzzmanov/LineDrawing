package org.example;

import static org.lwjgl.opengl.GL11.*;
import java.util.List;

public class Renderer {
    private Grid grid;
    private boolean useBresenham = true;  // За замовчуванням використовується алгоритм Брезенхема
    private boolean showAlgorithmPoints = true;  // Показувати точки, обчислені алгоритмом
    private long startTime;
    private long endTime;
    private long duration;

    public Renderer() {
        this.grid = new Grid();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void drawLine(Line line) {
        glLineWidth(3.0f);
        glBegin(GL_LINES);
        glColor3f(line.getColor()[0], line.getColor()[1], line.getColor()[2]);
        glVertex2f(line.getStart().getX(), line.getStart().getY());
        glVertex2f(line.getEnd().getX(), line.getEnd().getY());
        glEnd();

        drawPoint(line.getStart(), 5.0f);
        drawPoint(line.getEnd(), 5.0f);
    }

    public void drawRasterizedLine(Line line) {
        if (showAlgorithmPoints) {
            List<Point> points;
            startTime = System.nanoTime();
            if (useBresenham) {
                points = LineDrawingAlgorithms.bresenham(line);
            } else {
                points = LineDrawingAlgorithms.dda(line);
            }
            endTime = System.nanoTime();
            duration = endTime - startTime;
            drawGridCells(points, line.getColor());
        }
    }

    public void drawPoint(Point point, float size) {
        glPointSize(size);
        glBegin(GL_POINTS);
        glColor3f(1, 1, 1);
        glVertex2f(point.getX(), point.getY());
        glEnd();
    }

    public void drawGrid(float orthoWidth, float orthoHeight) {
        List<Point> gridPoint = Grid.gridInit(orthoWidth, orthoHeight);
        glColor4f(0, 1, 0, 0.2f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        float cellSize = 1.0f;

        for (Point point : gridPoint) {
            glBegin(GL_QUADS);
            float x = point.getX();
            float y = point.getY();
            float margin = 0.1f;

            glVertex2f(x - cellSize / 2 + margin, y - cellSize / 2 + margin);
            glVertex2f(x + cellSize / 2 - margin, y - cellSize / 2 + margin);
            glVertex2f(x + cellSize / 2 - margin, y + cellSize / 2 - margin);
            glVertex2f(x - cellSize / 2 + margin, y + cellSize / 2 - margin);
            glEnd();
        }

        glDisable(GL_BLEND);
    }

    public void drawGridCells(List<Point> points, float[] color) {
        glColor4f(0, 0, 1, 1f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (Point point : points) {
            float cellSize = 1.0f;
            glBegin(GL_QUADS);
            float x = point.getX();
            float y = point.getY();
            float margin = 0.1f;

            glVertex2f(x - cellSize / 2 + margin, y - cellSize / 2 + margin);
            glVertex2f(x + cellSize / 2 - margin, y - cellSize / 2 + margin);
            glVertex2f(x + cellSize / 2 - margin, y + cellSize / 2 - margin);
            glVertex2f(x - cellSize / 2 + margin, y + cellSize / 2 - margin);
            glEnd();
        }

        glDisable(GL_BLEND);

        for (Point point : points) {
            drawPoint(point, 4.0f);
        }
    }

    public long getDuration() {
        return duration;
    }

    public void toggleAlgorithm() {
        useBresenham = !useBresenham;
    }

    public String getCurrentAlgorithm() {
        return useBresenham ? "Bresenham" : "DDA";
    }

    public void toggleAlgorithmPointsVisibility() {
        showAlgorithmPoints = !showAlgorithmPoints;
    }

    public boolean areAlgorithmPointsVisible() {
        return showAlgorithmPoints;
    }
}