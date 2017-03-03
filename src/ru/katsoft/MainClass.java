package ru.katsoft;

import java.io.IOException;

public class MainClass {
    public static void main(String[] args) {
        // аргументы командной строки: путь к файлу с заданием, путь к файлу с ответом
        String taskPath = "task9.txt";
        String resultPath = "result.txt";
        if (args.length == 2) {
            taskPath = args[0];
            resultPath = args[1];
        } else if (args.length != 0) {
            System.out.println("Недопустимое количество аргументов командной строки (возможны либо 2, либо 0)");
            return;
        }
        Sudoku game = new Sudoku();
        game.setTaskPath(taskPath);
        game.setResultPath(resultPath);
        try {
            if(game.initializeField()) {
                System.out.println("Условие:");
                game.printField();
                long startTime = System.currentTimeMillis();
                game.solve();
                System.out.println("\r\nПотрачено времени: " + (System.currentTimeMillis() - startTime) + " мс");
                game.printResultToFile();
            } else {
                System.out.println("Ошибка при инициализации игры. Возможно, размеры выбранного поля не совпадают с зафиксированными в игре. Держитесь там!");
            }
        } catch (IOException ex) {
            System.out.println("Не удалось открыть файл с условием при инициализации игры");
        }


    }
}
