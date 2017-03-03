package ru.katsoft;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

// тесты написаны для поля размером 9*9 - иначе нумерация групп немного изменяется(появляются двузначные индексы), и
// работа тестов нарушается. однако сама программа масштабируема и будет работать с большими игровыми полями(по крайней
// мере я постараюсь так сделать:), если тесты выполняются для поля размера 9*9
public class SudokuTest {
    // ТЕСТЫ, НЕ ЗАВИСЯЩИЕ ОТ РЕАЛИЗАЦИИ

    @Test
    // тест чтения данных
    public void readDataFromFileTest() {
        Sudoku game = new Sudoku("test");
        //тест с корректными данными, которые должны быть верно считаны, при этом некоторые ячейки пустые
        String expected = "0 3 4 6 7 8 9 1 0\r\n" +
        "6 0 2 1 9 5 3 0 8\r\n" +
        "1 9 0 3 4 2 0 6 7\r\n" +
        "8 5 9 0 6 0 4 2 3\r\n" +
        "4 2 6 8 0 3 7 9 1\r\n" +
        "7 1 3 0 2 0 8 5 6\r\n" +
        "9 6 0 5 3 7 0 8 4\r\n" +
        "2 0 7 4 1 9 6 0 5\r\n" +
        "0 4 5 2 8 6 1 7 0\r\n";

        game.setTaskPath("tests\\notFullFieldTest.txt");
        try {
            game.initializeField();
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест readDataFromFileTest");
        }
        String actual = game.getInfo();
        assertEquals(actual, expected);
        //тест с некорректными данными
        //допускаются только строки вида "номер_ячейки, пробел, число_для_записи"
        boolean isFieldInitialized = true; //true по умолчанию, чтобы выявить возможную ошибку при считывании
        try {
            //ошибка, присутствуют буквы;
            game.setTaskPath("tests\\letterTest.txt");
            isFieldInitialized = game.initializeField();
            assertEquals(isFieldInitialized, false);

            //ошибка, одна из строк имеет неправильную длину
            game.setTaskPath("tests\\wrongLengthTest.txt");
            isFieldInitialized = game.initializeField();
            assertEquals(isFieldInitialized, false);

            //ошибка, слишком большое значение
            game.setTaskPath("tests\\wrongValueTest.txt");
            isFieldInitialized = game.initializeField();
            assertEquals(isFieldInitialized, false);
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест readDataFromFileTest");
        }


    }

    @Test
    // тест записи данных в файл
    public void writeDataToFileTest() {
        Sudoku game = new Sudoku("test");
        // запись производится только при полностью заполненном поле, поэтому его мы возьмем из файла
        String expected = "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n" +
                "1 2 3 4 5 6 7 8 9\r\n";
        try {
            //данный тест проверяет только корректность записи, но не решения, поэтому в файл записывается просто набор чисел,
            //но не ответ на головоломку
            game.setTaskPath("tests\\fullFieldTest.txt");
            game.initializeField();
            game.printResultToFile(); //actual
            StringBuffer _actual = new StringBuffer();
            FileInputStream fInput = new FileInputStream("tests\\resultTest.txt");
            int k;
            do {
                k = fInput.read();
                if (k != -1) _actual.append((char)k);
            }while(k != -1);
            String actual = _actual.toString();
            actual = actual.replaceAll(" \r", "\r"); //уберем пробел в конце строки
            assertEquals(actual, expected);
        } catch (java.io.IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест writeDataToFileTest");
        }
    }


    // ТЕСТЫ ОСНОВНЫХ АЛГОРИТМОВ

    @Test
    // проверка поля на заполнение всех ячеек
    public void checkFieldTest() {
        //для теста требуется полностью заполненное поле, создавать его в функции долго,
        //поэтому оно хранится в отдельном файле fullFieldTest.txt
        try {
            Sudoku game = new Sudoku("test");
            boolean isFieldFilled;
            game.setTaskPath("tests\\fullFieldTest.txt");
            game.initializeField();
            isFieldFilled = game.checkField();
            assertEquals(true, isFieldFilled); //все ячейки заполнены
            game.setTaskPath("tests\\notFullFieldTest.txt");
            game.initializeField();
            isFieldFilled = game.checkField();
            assertEquals(false, isFieldFilled); //нет заполненных ячеек
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест checkFieldForErrors");
        }
    }

    @Test
    // проверка поля на правильное заполнение всех ячеек, т.е. на корректность решения
    public void isGameWonTest() {
        Sudoku game = new Sudoku("test");
        boolean test1 = false;
        boolean test2 = true;
        try {
            //файл с верно заполленным игровым полем
            game.setTaskPath("tests\\rightFullFieldTest.txt");
            game.initializeField();
            //теперь проверим, верно ли заполнены все ячейки
            test1 = game.isGameWon();
            //сравним с ожидаемым результатом
            assertEquals(true, test1);
            //далее протестируем алгоритм на неверно заполненном файле с решением
            game.setTaskPath("tests\\notRightFullFieldTest.txt");
            game.initializeField();
            //теперь проверим, верно ли заполнены все ячейки
            test2 = game.isGameWon();
            //сравним с ожидаемым результатом
            assertEquals(false, test2);
        } catch (IOException ex){
            System.out.println("Ошибка при работе с файлом: тест isGameWonTest");
        }
    }

    @Test
    // тест метода lastHero, выбирающего кандидата методом исключения
    public void lastHeroTest() {
        Sudoku game = new Sudoku("test");
        try {
            game.setTaskPath("tests\\lastHeroTest.txt");
            game.initializeField();
            //для ячейки с координатами (7,0) вызываем метод lastHero - согласно его логике в эту ячейку можно поставить только цифру 8
            game.lastHero(7,0, "square");
            assertEquals(game.getElement(7,0), 8);
            game.lastHero(0,1,"line");
            assertEquals(game.getElement(0,1), 4);
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест lastHeroTest");
        }
    }

    @Test
    // тест метода nakedSet, исключающего лишних кандидатов из ячеек группы
    public void nakedSetTest() {
        Sudoku game = new Sudoku("test");
        try {
            game.setTaskPath("tests\\nakedSetTest.txt");
            game.initializeField();
            //для нулевой линии вызываем метод, затем смотрим, были ли исключены из клеток некоторые кандидаты
            game.nakedSet(0,0, "line");
            boolean[] expected = new boolean[] {false, false, true, false, false, true, false, true, false, false};
            //проверим ячейку с координатами 0,5 - из нее должны исчезнуть кандидаты 1 и 6, а остаться - 2,5 и 7
            boolean[] actual = game.getValue(0, 5);
            assertEquals(Arrays.equals(expected, actual), true);
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест nakedSetTest");
        }
    }

    @Test
    // тест рекурсивного решения
    public void solveRecTest() {
        Sudoku game = new Sudoku("test");
        try {
            game.setTaskPath("tests\\solveRecTest.txt");
            game.initializeField();
            //для нулевой линии вызываем метод, затем смотрим, были ли исключены из клеток некоторые кандидаты
            game.solveRec();
            String expected = "6 5 1 4 2 7 9 8 3\r\n" +
                    "2 3 9 6 1 8 4 7 5\r\n" +
                    "7 4 8 5 9 3 6 2 1\r\n" +
                    "3 9 2 8 4 1 5 6 7\r\n" +
                    "4 8 5 7 6 9 3 1 2\r\n" +
                    "1 7 6 3 5 2 8 9 4\r\n" +
                    "9 1 3 2 8 4 7 5 6\r\n" +
                    "5 2 4 9 7 6 1 3 8\r\n" +
                    "8 6 7 1 3 5 2 4 9\r\n";
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    sb.append(game.getElement(i, j));
                    if (j < 8) sb.append(" ");
                    else sb.append("\r\n");
                }
            }

            assertEquals(expected, sb.toString());
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест nakedSetTest");
        }
    }


    @Test
    // тест поиска скрытых n-ок
    public void hiddenSetTest() {
        Sudoku game = new Sudoku("test");
        try {
            game.setTaskPath("tests\\hiddenSetTest.txt");
            game.initializeField();
            //для нулевой линии вызываем метод, затем смотрим, были ли исключены из клеток некоторые кандидаты
            boolean a = game.hiddenSet(0,2, "row");
            boolean[] expected = new boolean[] {false, false, true, false, true, false, false, false, false, false};
            //проверим ячейки с координатами 3, 2 и 4, 2 с ожидаемым вариантом: в них должны остаться только кандидаты 2,4
            boolean[] actual = game.getValue(3, 2);
            assertEquals(Arrays.equals(expected, actual), true); //тест не может в сравнение массивов
            actual = game.getValue(4, 2);
            assertEquals(Arrays.equals(expected, actual), true); //тест не может в сравнение массивов
        } catch (IOException ex) {
            System.out.println("Ошибка при работе с файлом: тест hiddenSetTest");
        }
    }




}
