package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class GUI {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private long window;
    private Line line1;
    private Renderer renderer;

    public static final int WIDTH_TEXT_VIEW = 100;

    // Стан перетягування мишею
    private boolean dragging = false;
    private Point dragPoint = null;
    private Line dragLine = null;
    private boolean isStartPoint = false;

    // Координати миші в світовій системі
    private float worldMouseX, worldMouseY;

    // Параметри для проекції
    private float orthoWidth, orthoHeight;

    // Поля введення для ліній
    private final ImFloat inputX1 = new ImFloat();
    private final ImFloat inputY1 = new ImFloat();
    private final ImFloat inputX2 = new ImFloat();
    private final ImFloat inputY2 = new ImFloat();

    public GUI(long window, Line line1, Renderer renderer) {
        this.window = window;
        this.line1 = line1;
        this.renderer = renderer;

        // Налаштування обробників подій миші
        setupMouseCallbacks();
        init();
    }

    private void updateInputFields() {
        inputX1.set(line1.getStart().getX());
        inputY1.set(line1.getStart().getY());
        inputX2.set(line1.getEnd().getX());
        inputY2.set(line1.getEnd().getY());
    }

    private void setupMouseCallbacks() {
        // Обробник натискання кнопки миші
        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                glfwGetCursorPos(window, xpos, ypos);

                // Перетворення координат екрана в координати світу
                updateWorldMouseCoordinates(xpos[0], ypos[0]);

                if (action == GLFW_PRESS) {
                    // Перевіряємо, чи натиснули на точку
                    checkPointSelection();
                } else if (action == GLFW_RELEASE) {
                    // Завершуємо перетягування
                    dragging = false;
                    dragPoint = null;
                    dragLine = null;
                }
            }
        });

        // Обробник руху миші
        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            if (dragging && dragPoint != null) {
                // Перетворення координат екрана в координати світу
                updateWorldMouseCoordinates(xpos, ypos);

                // Оновлюємо позицію точки
                dragPoint.setX(worldMouseX);
                dragPoint.setY(worldMouseY);

                // Оновлюємо поля введення
                updateInputFields();
            }
        });
    }

    private void updateWorldMouseCoordinates(double xpos, double ypos) {
        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];
        glfwGetWindowSize(window, windowWidth, windowHeight);

        // Отримуємо розмір проекції
        orthoWidth = windowWidth[0] / 50.0f;  // Де 50.0f - це масштабний фактор від пікселів
        orthoHeight = windowHeight[0] / 50.0f;

        // Перетворення координат екрана в світові координати
        worldMouseX = ((float)xpos / windowWidth[0]) * orthoWidth - orthoWidth/2;
        worldMouseY = ((float)(windowHeight[0] - ypos) / windowHeight[0]) * orthoHeight - orthoHeight/2;
    }

    private void checkPointSelection() {
        float threshold = 0.5f; // Поріг відстані для вибору точки

        // Перевіряємо точки відрізка
        float distStart = distanceToPoint(worldMouseX, worldMouseY, line1.getStart().getX(), line1.getStart().getY());
        float distEnd = distanceToPoint(worldMouseX, worldMouseY, line1.getEnd().getX(), line1.getEnd().getY());

        // Знаходимо найближчу точку
        float minDist = Math.min(distStart, distEnd);

        if (minDist < threshold) {
            dragging = true;

            if (minDist == distStart) {
                dragPoint = line1.getStart();
                dragLine = line1;
                isStartPoint = true;
            } else if (minDist == distEnd) {
                dragPoint = line1.getEnd();
                dragLine = line1;
                isStartPoint = false;
            }
        }
    }

    // Евклідова відстань
    private float distanceToPoint(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void init() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        // Завантаження шрифта
        io.getFonts().clear();

        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf");
            if (fontStream != null) {
                // Тимчасове збереження шрифта
                Path tempFont = Files.createTempFile("roboto", ".ttf");
                Files.copy(fontStream, tempFont, StandardCopyOption.REPLACE_EXISTING);

                io.getFonts().addFontFromFileTTF(tempFont.toString(), 18, io.getFonts().getGlyphRangesCyrillic());
                System.out.println("Шрифт успішно завантажено");

                // Видалення тимчасового файлу
                Files.delete(tempFont);
            } else {
                System.err.println("Шрифт не знайдено в ресурсах");
            }
        } catch (IOException e) {
            System.err.println("Помилка при завантаженні шрифта: " + e.getMessage());
        }

        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 150");

        // Ініціалізація полів введення координат
        updateInputFields();
    }

    public void render() {
        // Підготовка ImGui до відображення нового кадру
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // Створюємо вікно ImGui для керування відрізком
        ImGui.begin("Керування відрізком", ImGuiWindowFlags.AlwaysAutoResize);

        ImGui.text("Координати відрізка");

        ImGui.text("Початкова точка:");
        if (ImGui.inputFloat("X1", inputX1)) {
            line1.getStart().setX(inputX1.get());
        }

        if (ImGui.inputFloat("Y1", inputY1)) {
            line1.getStart().setY(inputY1.get());
        }

        ImGui.text("Кінцева точка:");
        if (ImGui.inputFloat("X2", inputX2)) {
            line1.getEnd().setX(inputX2.get());
        }

        if (ImGui.inputFloat("Y2", inputY2)) {
            line1.getEnd().setY(inputY2.get());
        }

        // Додамо кнопки для перемикання алгоритмів
        if (ImGui.button("Перемкнути алгоритм (" + renderer.getCurrentAlgorithm() + ")")) {
            renderer.toggleAlgorithm();
        }

        boolean showPoints = renderer.areAlgorithmPointsVisible();
        if (ImGui.checkbox("Показувати точки алгоритму", new ImBoolean(showPoints))) {
            renderer.toggleAlgorithmPointsVisibility();
        }

        ImGui.text("Інформація про лінію:");
        float length = (float) Math.sqrt(
                Math.pow(line1.getEnd().getX() - line1.getStart().getX(), 2) +
                        Math.pow(line1.getEnd().getY() - line1.getStart().getY(), 2)
        );
        ImGui.text(String.format("Довжина: %.2f", length));
        ImGui.text("Час виконання: " + (renderer.getDuration() / 1_000_000.0) + " мс");
        ImGui.end();

        // Завершуємо відображення ImGui
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}