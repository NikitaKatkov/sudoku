package ru.katsoft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Sudoku {
    // размер поля, по умолчанию 9*9
    private final int SIZE_OF_FIELD = 25;
    // размер квадрата
    private final int SIZE_OF_SQUARE = (int)Math.sqrt(SIZE_OF_FIELD);
    // максимальное количесвто полных обходов поля без добавления новых значений при нерекурсивном решении
    private final int MAX_OPERATION_COUNT_WITHOUT_CHANGES = 3;
    // максимальное количество полных обходов поля при нерекурсивном решении
    private final int MAX_OPERATION_COUNT = SIZE_OF_FIELD * SIZE_OF_FIELD;
    // путь к файлу с заданием  - по умолчанию находится в папке с проектом
    private String taskPath;
    // путь к файлу с решением - по умолчанию находится в папке с проектом
    private String resultPath;
    // направление обхода поля для рекурсивного решения
    private boolean direction = true;
    // основная структура данных
    private boolean[][][] field;
    // дополнительная структура данных для временного хранения кандидатов в группе
    private HashMap<String, Integer> tempGroup = new HashMap<>();
    // дополнительная структура для временного хранения - ей можно заменить хэш-таблицу
    private Container[] candidatesAmount = new Container[SIZE_OF_FIELD];
    //дополнительная переменная для хранения ячейки - конкретно эта чтобы не передавать ее из мутода в метод - так кода меньше
    private boolean[] cell = new boolean[SIZE_OF_FIELD + 1];
    // дополнительная переменная для работы со строками
    private StringBuilder tempString = new StringBuilder("");



    // конструкторы
    Sudoku() {
        if (Math.ceil(Math.sqrt(SIZE_OF_FIELD)) != SIZE_OF_SQUARE || SIZE_OF_FIELD <= 1) {
            System.out.println("Недопустимый размер игрового поля!");
            return;
        }
        this.field = new boolean[SIZE_OF_FIELD][SIZE_OF_FIELD][SIZE_OF_FIELD+1];
        for(int line = 0; line < SIZE_OF_FIELD; line++) {
            candidatesAmount[line] = new Container();
            for(int row = 0; row < SIZE_OF_FIELD; row++) {
                //каждый раз создаем новый массив, так как это ссылочный тип, а в каждой ячейке должен находиться
                // собственный экземпляр массива, а не копия
                boolean[] cell = new boolean[SIZE_OF_FIELD + 1];
                this.field[line][row] = cell;
            }
        }
    }

    Sudoku(String str) {
        if (str.equals("test")) {
            this.field = new boolean[SIZE_OF_FIELD][SIZE_OF_FIELD][SIZE_OF_FIELD+1];
            for(int line = 0; line < SIZE_OF_FIELD; line++) {
                candidatesAmount[line] = new Container();
                for(int row = 0; row < SIZE_OF_FIELD; row++) {
                    //каждый раз создаем новый массив, так как это ссылочный тип, а в каждой ячейке должен находиться
                    // собственный экземпляр массива, а не копия
                    boolean[] cell = new boolean[SIZE_OF_FIELD + 1];
                    this.field[line][row] = cell;
                }
            }
            this.taskPath = "tests\\taskTest.txt";
            this.resultPath = "tests\\resultTest.txt";
        } else {
            System.out.println("Неизвестный параметр для конструктора Sudoku()");
        }
    }

    // сеттеры/геттеры
    void setTaskPath(String taskPath) {this.taskPath = taskPath;}

    void setResultPath(String resultPath) {this.resultPath = resultPath;}

    int getElement(int line, int row) {
        if (field[line][row][0]) {
            for (int index = 1; index <= SIZE_OF_FIELD; index++) {
                if (field[line][row][index]) return index;
            }
        }
        return -1;
    }


    // ВВОД-ВЫВОД

    // инициализация поля
    // функция добавления начальных условий в структуру данных возваращает true, если данные считаны успешно
    boolean initializeField() throws IOException {
        //числа берутся из текствого файла, в каждой строке которого через пробел записаны строка, столбец и число в ячейке
        List<String> lines;
        try {//вызывает исключение при наличии русских букв - это нормально
            lines = Files.readAllLines(Paths.get(taskPath), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.out.println("Ошибка при открытии файла с исходными данными");
            return false;
        }
        //если поле задано не полностью, инициализация невозможна
        if (lines.size() != SIZE_OF_FIELD) return false;
        //изначально в каждой клетке возможно поставить любое число
        //установка следующих значений перенесена из конструктора в данный метод с целью, чтобы при каждом его вызове
        //игровое поле полностью очищалось от старых згначений
        for(int line = 0; line < SIZE_OF_FIELD; line++) {
            for (int row = 0; row < SIZE_OF_FIELD; row++) {
                field[line][row][0] = false;
                for (int i = 1; i <= SIZE_OF_FIELD; i++) {
                    field[line][row][i] = true;
                }
            }
        }
        int line = 0, row = 0, value;
        for (String str : lines) {
            if (isStringCorrect(str)) {
                String[] values = str.split(" ");
                for (String strValue : values) {
                    value = Integer.parseInt(strValue);
                    if (checkValue(value)  && value <= SIZE_OF_FIELD) {
                        if (value != 0) {
                            boolean[] cell = new boolean[SIZE_OF_FIELD + 1];//для записи сведений об одной ячейке в таблицу
                            cell[0] = true; //ставим флаг, что значение в этой ячейке уже точно известно
                            cell[value] = true; //помечаем в массиве ту позицию, которой соответствует введенное число
                            this.field[line][row] = cell;
                        }
                    } else {
                        System.out.println("В строке " + (line+1) + " и столбце " + (row+1) + " обнаружено недопустимое значение: " + value);
                        return false;
                    }
                    row++;
                    if (row >= SIZE_OF_FIELD) {
                        row = 0;
                        break;
                    }
                }
                line++;
                if (line >= SIZE_OF_FIELD) break;
            } else {
            System.out.println("В строке \"" + str + "\" входного файла обнаружена ошибка");
            return false;
            }
        }
        update();
        return true;
    }

    // печать игрового поля в консоль
    void printField() {
        int value = 1;
        StringBuilder separator = new StringBuilder();
        int separatorLength = (2*SIZE_OF_SQUARE + 1) + (int)Math.sqrt(2*SIZE_OF_SQUARE + 1)*SIZE_OF_FIELD;
        for (int i = 0; i < separatorLength; i++) {
            separator.append("-");
        }
        for (int line = 0; line < SIZE_OF_FIELD; line++) {
            if (line % SIZE_OF_SQUARE == 0) System.out.println(separator);
            for(int row = 0; row <SIZE_OF_FIELD; row++) {
                if (row % SIZE_OF_SQUARE == 0) System.out.print("| ");
                if(field[line][row][0]) { // если в ней есть точное значение, печатаем его, иначе символ "."
                    while(!field[line][row][value]) value++; //находим точное значение
                    if (Math.log10(SIZE_OF_FIELD) > 1 && Math.log10(value) < 1) System.out.print(" "); // для ровной печати поля
                    System.out.print(value + " ");
                    value = 1;
                } else {
                    if (Math.log10(SIZE_OF_FIELD) > 1) System.out.print(" "); // для ровной печати
                    System.out.print(". ");
                }
            }
            System.out.println("|"); //перевод строки
        }
        System.out.println(separator);
    }

    // печать ответа в файл
    void printResultToFile() {
        try {
            File result = new File(resultPath);
            result.createNewFile();
            FileOutputStream fOutput = new FileOutputStream(result, false);
            int i = 1, n;
            String toWrite = "";
            for (boolean[][] line : field) { //просматриваем строку
                for(boolean[] cell : line) { //просматриваем ячейку
                    while(!cell[i]) i++; //находим точное значение
                    toWrite += i + " ";
                    i = 1;
                }
                n = 0;
                while (n < toWrite.length()) {
                    fOutput.write(toWrite.charAt(n++));
                }
                fOutput.write('\r'); //переход на новую строку
                fOutput.write('\n');
                toWrite = "";
            }
        } catch (IOException ex) {
            System.out.println("Не удалось записать результат в файл");
        }
    }


    // ОСНОВНЫЕ ФУНКЦИИ

    // рекурсивное решение судоку
    void solveRec() { //перегрузка
        //можно начинать перебор не с самого начала, а с ячейки, в которой меньше всего кандидатов
        solveRec(0,0);
    }

    private boolean solveRec(int line, int row) {
        //возвращает false, если при данном числе в текущей ячейке невозможно найти решение
        //direction отвечает за то, в прямом или в обратном порядке функция проходит по игровому полю, 1 = прямой, 0 = обратный
        if (checkField() && isGameWon()) {
            printField();
            return true;
        }
        if (!field[line][row][0]) {
            boolean[] cell = getCandidates(line, row);
            List<Integer> candidates = getIntegerCandidates(line, row);
            for (int candidate : candidates) {
                setValue(line, row, candidate);
                direction = true;//идем вперед
                if (solveRec(getNextLine(line, row), getNextRow(row))){
                    direction = true;
                    return direction;
                }
            }
            direction = false; //нет кандидатов, поэтому идем назад по игровому полю
            field[line][row] = cell;
            localUpdate(line, row);
            return direction;
        } else if (direction){ //для пропуска точно заполненных ячеек
            return solveRec(getNextLine(line, row), getNextRow(row));
        } else { //для возврата в предыдущуюю ячейку
            return direction;
        }
    }

    // решение судоку  - launcher
    void solve() {
        int wasFieldChanged = 0;
        boolean  gameOver = false, exit = false; // флаг изменения конфигурации игрового поля за итерацию цикла (для недопущения бесконечного цикла)
        int operationWithoutChangesCount = 0, overallOperationCount = 0;
        while(!gameOver && overallOperationCount < MAX_OPERATION_COUNT && (wasFieldChanged > 0 || operationWithoutChangesCount <= MAX_OPERATION_COUNT_WITHOUT_CHANGES)) {
            // цикл использования специальных функций
            wasFieldChanged = 0;
            for (int line = 0; line < SIZE_OF_FIELD; line++) {
                if (nakedSet(line, 0, "line")){ //|| hiddenSet(line, line, "line")) {// параметр row здесь не важен, можно поставить что угодно
                    wasFieldChanged++;
                    operationWithoutChangesCount = 0;
                    exit = true;
                    break;
                }
                for (int row = 0; row < SIZE_OF_FIELD; row++) {
                    if (getNumberOfCandidates(line, row) == 0) System.out.println("нет кандидатов в ячейке " + line + " " + row);
                    if (line == 0) {
                        if (nakedSet(0, row, "row")){ //|| hiddenSet(line, row, "row")) { // параметр line не важен -//-
                            wasFieldChanged++;
                            operationWithoutChangesCount = 0;
                            exit = true;
                            break;
                        }
                    }
                    if (setTheOnlyPossibleValue(line, row) || lastHero(line, row, "line") || lastHero(line, row, "row") || lastHero(line, row, "square")) {
                        wasFieldChanged++;
                        operationWithoutChangesCount = 0;
                        exit = true;
                        break;
                    }
                }
            }
//             отдельный цикл для проверки на наличие "голых n-ок" в квадратах - в общем цикле его вызывать неудобно,
//             слишком перегруженно получается
//            for (int i = 0; i < SIZE_OF_FIELD; i += SIZE_OF_SQUARE) {
//                if (exit) break;
//                for (int j = 0; j < SIZE_OF_FIELD; j += SIZE_OF_SQUARE) {
//                    if(nakedSet(i, j, "square")) {
//                        wasFieldChanged++;
//                        operationWithoutChangesCount = 0;
//                        exit = true;
//                        break;
//                    }
//                }
//            }

            exit = false;
            gameOver = isGameWon();
            operationWithoutChangesCount++;
            overallOperationCount++;
        }
        if (gameOver) {
            System.out.println("\r\nНайдено однозначное решение!");
            printField();
        } else {
            System.out.println("\r\nВот все, что удалось сделать встроенными нерекурсивными методами :(");
            printField();
            System.out.println("\r\nПробуем решить оставшуюся часть рекурсивно :");

            for (int i = 0; i < SIZE_OF_FIELD; i++) {
                for (int j = 0; j < SIZE_OF_FIELD; j++) {
                    if (getNumberOfCandidates(i, j) == 0) {
                        field[i][j] = getCandidates(i, j);
                        System.out.println("перед рекурсией добавлено значение");
                    }
                }
            }
            solveRec();
        }
    }


    // СПЕЦИАЛЬНЫЕ МЕТОДЫ РЕШЕНИЯ БЕЗ РЕКУРСИИ

    // подстановка единственно возможного решения в ячейку
    private boolean setTheOnlyPossibleValue(int line, int row) {
        if (field[line][row][0]) return false; //значение в ячейке уже точно определено
        int value = isTheOnlyCandidate(getCandidates(line, row));
        if (value > 0) {
            setValue(line, row, value);
            return true;
        }
        return false;
    }

    // нахождение уникального для группы кандидата
    boolean lastHero(int _line, int _row, String group) {
        if (field[_line][_row][0]) return false; //значение в ячейке уже точно определено
        boolean[] groupCandidates = new boolean[SIZE_OF_FIELD + 1];
        switch (group) {
            case "line": //последний возможный вариант в строке
                for (int row = 0; row < SIZE_OF_FIELD; row++) {
                    if (row != _row) {
                        if (field[_line][row][0]) {
                            groupCandidates = addBooleanValuesUnchecked(groupCandidates, field[_line][row]);
                        } else {
                            groupCandidates = addBooleanValuesUnchecked(groupCandidates, getCandidates(_line, row));
                        }
                    }
                }
                inverseBoolean(groupCandidates);
                break;
            case "row": //последний возможный вариант в столбце
                for (int line = 0; line < SIZE_OF_FIELD; line++) {
                    if (line != _line) {
                        if(field[line][_row][0]) {
                            groupCandidates = addBooleanValuesUnchecked(groupCandidates, field[line][_row]);
                        } else {
                            groupCandidates = addBooleanValuesUnchecked(groupCandidates, getCandidates(line, _row));
                        }
                    }
                }
                inverseBoolean(groupCandidates);
                break;
            case "square": //последний возможный вариант в квадрате
                int[] coordinates = getSquareStartCoordinates(_line, _row);
                for (int line = coordinates[0]; line < coordinates[0] + SIZE_OF_SQUARE; line++) {
                    for (int row = coordinates[1]; row < coordinates[1] + SIZE_OF_SQUARE; row++) {
                        if ((line != _line | row != _row)) {
                            if(field[line][row][0]) {
                                groupCandidates = addBooleanValuesUnchecked(groupCandidates, field[line][row]);
                            } else {
                                groupCandidates = addBooleanValuesUnchecked(groupCandidates, getCandidates(line, row));
                            }
                        }
                    }
                }
                inverseBoolean(groupCandidates);
                break;
        }
        int position = isTheOnlyCandidate(groupCandidates);
        if (position > 0 && field[_line][_row][position]) {
            setValue(_line, _row, position);
            return true;
        }
        return false;
    }

    // голая n-ка - сокращение числа кандидатов в остальных ячейках группы
    boolean nakedSet(int _line, int _row, String group) {
        String cell;
        boolean[] toDelete;
        boolean result = false;
        int lineStart = 0, rowStart = 0, lineEnd = 0, rowEnd = 0;
        switch (group) {
            case "line":
                lineStart = lineEnd = _line;
                rowStart = 0;
                rowEnd = SIZE_OF_FIELD - 1;
                break;
            case "row":
                lineStart = 0;
                lineEnd = SIZE_OF_FIELD - 1;
                rowStart = rowEnd = _row;
                break;
            case "square":
                lineStart = getSquareStartCoordinates(_line, _row)[0];
                lineEnd = lineStart + SIZE_OF_SQUARE - 1;
                rowStart = getSquareStartCoordinates(_line, _row)[1];
                rowEnd = rowStart + SIZE_OF_SQUARE - 1;
                break;
        }

        for (int line = lineStart; line <= lineEnd; line++) {
            for (int row = rowStart; row <= rowEnd; row++) {
                cell = boolean2String(field[line][row]);
                if (tempGroup.putIfAbsent(cell, 1) != null) { // если такой элемент уже есть в таблице
                    tempGroup.replace(cell, tempGroup.get(cell) + 1); // увеличим количество его вхождений в группу
                }
            }
        }

        if (tempGroup.size() < SIZE_OF_FIELD) { //если есть хотя бы одна повторяющаяся ячейка
            for (Map.Entry entry : tempGroup.entrySet()) {
                // если количество клеток, в которых одинаковый список кандидатов, совпадает с числом кандидатов в списке,
                // удалим из оставшихся ячеек группы всех кандидатов из этого списка
                if ((int) entry.getValue() > 1 && (int) entry.getValue() == getNumberOfCandidates(entry.getKey().toString())) {
                    toDelete = string2Boolean(entry.getKey().toString());
                    for (int line = lineStart; line <= lineEnd; line++) {
                        for (int row = rowStart; row <= rowEnd; row++) {
                            if (!field[line][row][0] && !Arrays.equals(field[line][row], toDelete)) {
                                int numberOfDeleted = substractBooleanValues(field[line][row], toDelete);
                                if (numberOfDeleted > 0) {
                                    result = true;
                                }
                            }
                        }
                    }
                    tempGroup.clear();
                    return result;
                }
            }
        }
        tempGroup.clear();
        return result;
    }

    // скрытая n-а - сокращение числа кандидатов в данных ячейках группы
    boolean hiddenSet (int line, int row, String group) {
        List<int[]> coordinates = getHiddenCoordinates(line, row, group, false);
        if (coordinates != null & getIntegerCandidates(cell).size() > 1) {
            for (int[] lineAndRow : coordinates) {
                for (int index = 0; index <= SIZE_OF_FIELD; index++) {
                    field[lineAndRow[0]][lineAndRow[1]][index] = cell[index];
                }
            }
            for (int i = 0; i <= SIZE_OF_FIELD; i++) cell[i] = false;
            return true;
        } else {
            for (int i = 0; i <= SIZE_OF_FIELD; i++) cell[i] = false;
            return false;
        }
    }

    // получение координат ячеек, в которых нужно произвести очистку кандидатов - для запуска hiddenSet
    private List<int[]> getHiddenCoordinates(int _line, int _row, String group, boolean finalCall) {
        //finalCall отвечает за то, является ли данный вызов функции последним (для рекурсии) - макс глубина 2, это не страшно
        List<Integer> duplicates = new ArrayList<>();
        int beginOfSequence = 0, endOfSequence = 0, duplicateCounter = 0;
        List<int[]> coordinates = new ArrayList<>();
        candidatesAmount = getCandidatesAmount(_line, _row, group); // сколько раз каждое из чисел присутствует в списке кандидатов
        Arrays.sort(candidatesAmount); // сортируем по возрастанию значения - количества повторений кандидата в группе

        for (int index = 0; index < SIZE_OF_FIELD - 1; index++) { // идем по списку candidatesAmount
            if(candidatesAmount[index].value == candidatesAmount[index + 1].value) {
                endOfSequence = index + 1;
            } else if (endOfSequence - beginOfSequence > 0 && candidatesAmount[beginOfSequence].value > 1
                    && candidatesAmount[beginOfSequence].value == endOfSequence - beginOfSequence + 1) {
                // если в найденной серии количество повторений кандидатов больше 1, а число таких кандидатов равно длине серии, мы нашли то, что нужно
                break;
            } else {
                beginOfSequence = endOfSequence = index + 1; // пропускаем всех предыдущих кандидатов, идем дальше по списку
            }
        }
        if (endOfSequence <= beginOfSequence) return null;
        for (int i = beginOfSequence; i <= endOfSequence; i++) {
            duplicates.add(candidatesAmount[i].key); //добавим в список все значения (кандидаты), которые повторяются одинаковое количество раз
        }
        if (duplicates.size() > 1)
            duplicateCounter = candidatesAmount[beginOfSequence].value; //сколько раз повторяется каждый из кандидатов

        int coincedenceCounter = 0;
        //определим границы обхода группы для столбца, строки и квадрата
        int lineStart = 0, rowStart = 0, lineEnd = 0, rowEnd = 0;
        switch (group) {
            case "line":
                lineStart = lineEnd = _line;
                rowStart = 0;
                rowEnd = SIZE_OF_FIELD - 1;
                break;
            case "row":
                lineStart = 0;
                lineEnd = SIZE_OF_FIELD - 1;
                rowStart = rowEnd = _row;
                break;
            case "square":
                lineStart = getSquareStartCoordinates(_line, _row)[0];
                lineEnd = lineStart + SIZE_OF_SQUARE - 1;
                rowStart = getSquareStartCoordinates(_line, _row)[1];
                rowEnd = rowStart + SIZE_OF_SQUARE - 1;
                break;
        }

        //пройдем по всем элементам группы и запомним позиции тех, у которых есть duplicateCounter элементов true из duplicates
        for (int line = lineStart; line <= lineEnd; line++) {
            for (int row = rowStart; row <= rowEnd; row++) { //бходим всю группу
                for (int candidate : duplicates) {//обходим конкретную ячейку
                    if (field[line][row][candidate]) coincedenceCounter++;
                }
                if (coincedenceCounter >= duplicateCounter) coordinates.add(new int[]{line, row});
                coincedenceCounter = 0;
            }
        }
        boolean isCandidateInEveryCellFromCoordinates = true;
        for (int candidate : duplicates) {
            //добавим кандидата в cell, если он есть во всех ячейках из coordinates
            for (int[] lineAndRow : coordinates) {
                if(!field[lineAndRow[0]][lineAndRow[1]][candidate]) {
                    isCandidateInEveryCellFromCoordinates = false;
                    break;
                }
            }
            if (isCandidateInEveryCellFromCoordinates) cell[candidate] = true; //в этой ячейке записаны только кандидаты из скрытой n-ки - остальных из выбранных ячеек нужно удалить
        }
        // теперь в coordinates лежат координаты ячеек, в каждой из которых есть кандидаты из duplicates
        if (coordinates.size() < duplicateCounter) {
            // если клеток столько же, сколько и совпадающих кандидатов в них, совершаем обход клеток с координатами
            // из coordinates и удаляем из них всех кандидатов, кроме присутствующих в cell
            return null;
        }

        // если стоит флаг finalCall, проверка на нахождение сразу в двух группах уже не требуется, возвращаем список
        if (finalCall) return coordinates;

        // проверим, лежат ли данные ячейки одновременно еще и в другой общей группе (например, две ячейки могут лежать внутри одной строки и квадрата)
        // если так, получим результат выполнения функции для второй группы и сравним с первой - для удаления кандидатов они должны совпасть
        boolean areAllCellsInSameLine = true, areAllCellsInSameRow = true, areAllCellsInSameSquare = true;
        if (coordinates.size() > SIZE_OF_SQUARE) { //ячейки уже в одном квадрате, значит в одной линии может быть не больше чем размер квадрата ячеек
            areAllCellsInSameLine = areAllCellsInSameRow = areAllCellsInSameSquare = false;
        } else {
            switch (group) {
                case "square":
                    int lineCoord = coordinates.get(0)[0], rowCoord = coordinates.get(0)[1];
                    for (int[] lineAndRow : coordinates) {
                        if (areAllCellsInSameLine && lineCoord != lineAndRow[0]) { // проверка на одну и ту же линию
                            areAllCellsInSameLine = false;
                        }
                        if (areAllCellsInSameRow && rowCoord != lineAndRow[1]) { // проверка на один и тот же столбец
                            areAllCellsInSameRow = false;
                        }
                    }
                    break;
                default: //для элементов в одном столбце и строке
                    areAllCellsInSameRow = areAllCellsInSameLine = false;
                    int[] squareCoord = getSquareStartCoordinates(coordinates.get(0)[0], coordinates.get(0)[1]);
                    for (int[] lineAndRow : coordinates) {
                        if (areAllCellsInSameSquare && (squareCoord[0] != getSquareStartCoordinates(lineAndRow[0], lineAndRow[1])[0]
                                || squareCoord[1] != getSquareStartCoordinates(lineAndRow[0], lineAndRow[1])[1])) {
                            areAllCellsInSameSquare = false;
                        }
                    }
            }
        }

        // если ячейки лежат сразу в двух группах, получим список coordinates из второй и сравним с ранее вычесленным из первой
        if (areAllCellsInSameLine && areAllCellsInSameRow)
            System.out.println("Что-то не то: клетки в одном ряду и строке - возможно, клетка единственная, это нужно исключить");
        List<int[]> extraCoordinates;
        if (group == "square" && areAllCellsInSameLine) {
            extraCoordinates = getHiddenCoordinates(coordinates.get(0)[0], coordinates.get(0)[1], "line", true);
        } else if (group == "square" && areAllCellsInSameRow) {
            extraCoordinates = getHiddenCoordinates(coordinates.get(0)[0], coordinates.get(0)[1], "row", true);
        } else if (areAllCellsInSameSquare) {
            extraCoordinates = getHiddenCoordinates(coordinates.get(0)[0], coordinates.get(0)[1], "square", true);
        } else {
            return coordinates;
        }

        // теперь сравним coordinates и extraCoordinates
        if (isEqual(coordinates, extraCoordinates)) return coordinates;
        return null;
    }


    // ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ

    // получение всех чисел в строке
    private boolean[] getNumbersInLine(int line) {
        boolean[] result = new boolean[SIZE_OF_FIELD + 1];
        result[0] = true;
        for(int row = 0; row < SIZE_OF_FIELD; row++) {
            result = addBooleanValues(result, field[line][row]);
        }
        return result;
    }

    // получение всех чисел в столбце
    private boolean[] getNumbersInRow(int row) {
        boolean[] result = new boolean[SIZE_OF_FIELD + 1];
        result[0] = true;
        for(int line = 0; line < SIZE_OF_FIELD; line++) {
            result = addBooleanValues(result, field[line][row]);
        }
        return result;
    }

    // получение всех чисел в квадрате для известных координат ячейки
    private boolean[] getNumbersInSquare(int _line, int _row) {
        //координаты левого верхнего угла квадрата, которому принадлежит клетка
        int[] coordinates = getSquareStartCoordinates(_line, _row);
        int lineStart = coordinates[0];
        int rowStart = coordinates[1];
        boolean[] result = new boolean[SIZE_OF_FIELD + 1];
        result[0] = true;
        for (int line = lineStart; line < lineStart + SIZE_OF_SQUARE; line++) {
            for (int row = rowStart; row < rowStart + SIZE_OF_SQUARE; row++) {
                result = addBooleanValues(result, field[line][row]);
            }
        }
        return result;
    }

    // подбор кандидатов по строке, столбцу и квадрату для конкретной ячейки
    private boolean[] getCandidates(int line, int row) {
        if (field[line][row][0]) return field[line][row]; //точно заполненную ячейку просто возвращаем

        boolean[] result;
        result = addBooleanValuesUnchecked(getNumbersInLine(line), getNumbersInRow(row));
        result = addBooleanValuesUnchecked(result, getNumbersInSquare(line, row));
        inverseBoolean(result);
        result[0] = false;
        return result;
    }

    // подбор кандидатов,  возвращает массив кандидатов в десятичной системе счисления
    private List<Integer> getIntegerCandidates(int line, int row) {
        return getIntegerCandidates(getCandidates(line, row));
    }

    private List<Integer> getIntegerCandidates(boolean[] candidates) {
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i < candidates.length; i++) {
            if (candidates[i]) result.add(i);
        }
        return result;
    }

    // получение количества каждого из кандидатов в группе
    private Container[] getCandidatesAmount(int _line, int _row, String group) {
        for (int i = 0; i < SIZE_OF_FIELD; i++) {
            candidatesAmount[i].value = 0;// число повторений кандидата
            candidatesAmount[i].key = i + 1; // кандидат
        }
        int lineStart = 0, lineEnd = 0, rowStart = 0, rowEnd = 0;
        switch (group) {
            case "line":
                lineStart = lineEnd = _line;
                rowStart = 0;
                rowEnd = SIZE_OF_FIELD - 1;
                break;
            case "row":
                lineStart = 0;
                lineEnd = SIZE_OF_FIELD - 1;
                rowStart = rowEnd = _row;
                break;
            case "square":
                int[] coordinates = getSquareStartCoordinates(_line, _row);
                lineStart = coordinates[0];
                lineEnd = lineStart + SIZE_OF_SQUARE - 1;
                rowStart = coordinates[1];
                rowEnd = rowStart + SIZE_OF_SQUARE - 1;
                break;
        }
        for (int line = lineStart; line <= lineEnd; line++) {
            for (int row = rowStart; row <= rowEnd; row++) {
                for (int index = 1; index <= SIZE_OF_FIELD; index++) {
                    if (field[line][row][index]) {
                        candidatesAmount[index - 1].value++;
                    }
                }
            }
        }
        return candidatesAmount;
    }

    // проверка, есть ли единственный кандидат для заполнения ячейки
    private int isTheOnlyCandidate(boolean[] candidates) {
        //возвращает индекс элемента true, если он единственный, и -1 в противном случае

        //если возвращает ноль, значит решение зашло в тупик и вариантов нет - для рекурсии
        int index = 0;
        for(int i = 1; i <= SIZE_OF_FIELD; i++) {
            if (candidates[i] & index == 0) {
                index = i;
            }
            else if (candidates[i] & index > 0) return -1;
        }
        return index;
    }

    // получение количества кандидатов в ячейке
    private int getNumberOfCandidates(int line, int row) {
        int numberOfCandidates = 0;
        if (field[line][row][0]) return -1;// в ячейке уже есть точное значение
        for (boolean candidate: field[line][row]) {
            if (candidate) numberOfCandidates++;
        }
        return numberOfCandidates;
    }

    private int getNumberOfCandidates(String cell) {
        //работает со строкой длины SIZE_OF_FIELD - не учитывает флаг точного заполнения
        int numberOfCandidates = 0;
        for (char candidate: cell.toCharArray()){
            if (candidate == '1') numberOfCandidates++;
        }
        return numberOfCandidates;
    }

    private int getNumberOfCandidates(boolean[] cell) {
        int counter = 0;
        for (int i = 1; i <= SIZE_OF_FIELD; i++) {
            if (cell[i]) counter++;
        }
        return counter;
    }

    // получение координат левого верхнего угла квадрата
    private int[] getSquareStartCoordinates(int line, int row) {
        while(line % SIZE_OF_SQUARE != 0) line--;
        while (row % SIZE_OF_SQUARE != 0) row--;
        return new int[]{line, row};
    }

    // получение координат следующей ячейки для рекурсивного обхода
    private int getNextLine(int line, int row) {
        if ((row + 1) % SIZE_OF_FIELD == 0) return line + 1;
        return line;
    }

    private int getNextRow(int row) {
        if ((row + 1) % SIZE_OF_FIELD == 0) return 0;
        return row + 1;
    }

    // обновление кандидатов во всех ячейках поля - нелья использовать в сочетании с функциями, убирающими лишних кандидатов (lastHero, nakedSet и т.д.)
    private void update() {
        for (int i = 0; i < 2; i++) {//два раза полностью обходим поле, чтобы учесть все изменения
            for (int line = 0; line < SIZE_OF_FIELD; line++) {
                for (int row = 0; row < SIZE_OF_FIELD; row++) {
                    field[line][row] = getCandidates(line, row);
                    if (getNumberOfCandidates(line, row) == 0) {
                        System.out.println("Метод update обнаружил, что в ячейке " + line + " " + row + "отсутствуют кандидаты");
                        return;
                    }
                }
            }
        }
    }

    // обновление кандидатов в группах, куда входит текущая ячейка
    private void localUpdate(int _line, int _row) {
        // обход строки
        for (int row = 0; row < SIZE_OF_FIELD; row++) {
            if (!field[_line][row][0]) field[_line][row] = getCandidates(_line, row);
        }

        // обход столбца
        for (int line = 0; line < SIZE_OF_FIELD; line++) {
            if (!field[line][_row][0]) field[line][_row] = getCandidates(line, _row);
        }

        //обход квадрата
        int[] coordinates = getSquareStartCoordinates(_line, _row);
        for (int line = coordinates[0]; line < coordinates[0] + SIZE_OF_SQUARE; line++) {
            for (int row = coordinates[1]; row < coordinates[1] + SIZE_OF_SQUARE; row++) {
                if (!field[line][row][0]) field[line][row] = getCandidates(line, row);
            }
        }
    }

    // удаление только что добавленного кандидата из всех групп, в которые входит ячейка с ним
    private void smartLocalUpdate(int _line, int _row, int value) {
        // обход строки
        for (int row = 0; row < SIZE_OF_FIELD; row++) {
            if (!field[_line][row][0]) field[_line][row][value] = false;
        }

        // обход столбца
        for (int line = 0; line < SIZE_OF_FIELD; line++) {
            if (!field[line][_row][0]) field[line][_row][value] = false;
        }

        //обход квадрата
        int[] coordinates = getSquareStartCoordinates(_line, _row);
        for (int line = coordinates[0]; line < coordinates[0] + SIZE_OF_SQUARE; line++) {
            for (int row = coordinates[1]; row < coordinates[1] + SIZE_OF_SQUARE; row++) {
                if (!field[line][row][0]) field[line][row][value] = false;
            }
        }
    }

    // проверка строки из файла с условием на корректность
    private boolean isStringCorrect(String str) {
        //минимальная длина строки фиксирована
        if(str.isEmpty()) return false;
        //если нет разделителя - пробела
        if (!str.contains(" ")) return false;
        //если пробелов в строке не SIZE_OF_FIELD - 1 или в строке присутствуют буквы
        if (checkChars(str) != SIZE_OF_FIELD - 1) return false;
        //если отсутствует одно или несколько чисел
        if (str.replaceAll(" ", "").length() < SIZE_OF_FIELD) return false;

        return true;
    }

    // получение числа пробелов в строке и одновременная проверка на наличие букв
    private int checkChars(String str) {
        //функция производит сразу две проверки, так как она по очереди просматривает все символы строки - немного избыточный
        //для одного метода функционал компенсируется отсутствием необходимости дважды пробегать по строке с разными целями
        char[] chars = str.toCharArray();
        int result = 0;
        for (char ch : chars) {
            if (ch == ' ') result++;
            else if((int)ch < 48 || (int)ch > 57) {
                return -1; //если не пробельный символ не является числом
            }
        }
        return result;
    }

    // получение значений на поле и их координат - для тестов
    String getInfo() {
        StringBuffer data = new StringBuffer();
        int i = 1;
        for (int line = 0; line < SIZE_OF_FIELD; line++) { //просматриваем строку
            for(int row = 0; row < SIZE_OF_FIELD; row++) { //просматриваем ячейку
                if(field[line][row][0]) { //если в ней есть точное значение, печатаем его, иначе символ "."
                    while (!field[line][row][i]) i++; //находим точное значение
                    data.append(i + " ");
                    i = 1;
                } else {
                    data.append("0 ");
                }
            }
            data.replace(data.length() - 1, data.length(), "\r\n");
        }
        return data.toString();
    }

    // сложение булевых векторов (используется для ячеек, в которых несколько кандидатов) - логическое "ИЛИ"
    private boolean[] addBooleanValuesUnchecked(boolean[] result, boolean[] cell) {
        for (int i = 1; i < result.length; i++) {
            if (cell[i]) {
                result[i] = true;
            }
        }
        return result;
    }

    // сложение булевых векторов (используется для точно заполненных ячеек) [0 1 1 1]
    private boolean[] addBooleanValues(boolean[] result, boolean[] cell) {
        if (result[0] & cell[0]) {
            addBooleanValuesUnchecked(result, cell);
        }
        return result;
    }

    // вычитание булевых векторов [0 0 1 0]
    private int substractBooleanValues(boolean[] result, boolean[] cell) { // возвращает число измененных координат
        // из result вычитаем cell, ответ записываем в result
        int counter = 0;
        for (int i = 1; i <= SIZE_OF_FIELD; i++) {
            if (result[i] && cell[i]) {
                result[i] = false;
                counter++;
            }
        }
        return counter;
    }

    // проверка, все ли числа присутствуют в группе
    private boolean areAllNumbersPresent(boolean[] numbers) {
        for (int i = 1; i <= SIZE_OF_FIELD; i++) {
            if (!numbers[i]) return false;
        }
        return true;
    }

    // проверка, можно ли добавить на поле число из файла с условием
    private boolean checkValue(int value) {
        return (value >= 0 && value <= SIZE_OF_FIELD);
    }

    // инвертирование булева вектора
    private boolean[] inverseBoolean(boolean[] result) {
        for (int i = 0; i < result.length; i++) {
            result[i] = !result[i];
        }
        return result;
    }

    // установить точное значение в ячейке
    private void setValue(int line, int row, int value) {
        for (int i = 0; i <= SIZE_OF_FIELD; i++) field[line][row][i] = false;
        field[line][row][0] = field[line][row][value] = true;
        smartLocalUpdate(line, row, value);
    }

    // получить ссылку на конкретную ячейку поля
    boolean[] getValue(int line, int row) {
        return field[line][row];
    }

    // получение десятичного числа, соответствующего двоичному (булеву) вектору
    private String boolean2String(boolean[] cell) {
        // нулевой элемент ячейки, отвечающий за ее точное заполнение, здесь не важен
        for (int i = 1; i <= SIZE_OF_FIELD; i++) {
            tempString.append(cell[i]? 1 : 0);
        }
        String result = tempString.toString();
        tempString.delete(0, tempString.length()); // очистка для последующего использования
        return result;
    }

    // получение булева вектора из числа
    private boolean[] string2Boolean(String cell) {
        boolean[] result = new boolean[SIZE_OF_FIELD + 1];
        for (int i = 0; i < SIZE_OF_FIELD; i++) {
            result[i+1] = cell.toCharArray()[i] == '1';
        }
        return result;
    }

    // сравнение списка массивов Int[]
    private boolean isEqual(List<int[]> first, List<int[]> second) {
        if (first == null || second == null) return false;
        if (first.size() != second.size()) return false;
        for (int i = 0; i < first.size(); i++) {
            if (first.get(i)[0] != second.get(i)[0] || first.get(i)[1] != second.get(i)[1]) return false;
        }
        return true;
    }

    // проверка поля на заполненность
    boolean checkField() {
        for(int line = 0; line < SIZE_OF_FIELD; line++) {
            for (int row = 0; row < SIZE_OF_FIELD; row++) {
                //так как в нулевой ячейке каждой клетки стоит флаг, означающий правильность заполнения и наличие только
                //одного кандидата в данной клетке, достаточно проверить только его самого
                if(!field[line][row][0]) return false;
            }
        }
        return true;
    }

    // проверка заполненного поля на корректность полученного решения
    boolean isGameWon() {
        boolean[] group = new boolean[SIZE_OF_FIELD + 1];
        group[0] = true;
        //проверка строк
        for (int line = 0; line < SIZE_OF_FIELD; line++) {
            group = addBooleanValues(group, getNumbersInLine(line));
            if(!areAllNumbersPresent(group)) return false;
            //сбросим значения во временной переменной во избежание ошибок
            for(int i = 1; i <= SIZE_OF_FIELD; i++) group[i] = false;
        }


        //проверка столбцов
        for (int row = 0; row < SIZE_OF_FIELD; row++) {
            group = addBooleanValues(group, getNumbersInRow(row));
            if(!areAllNumbersPresent(group)) return false;
            for(int i = 1; i <= SIZE_OF_FIELD; i++) group[i] = false;
        }


        //проверка квадратов
        for (int lineStart = 0; lineStart < SIZE_OF_FIELD; lineStart += SIZE_OF_SQUARE) {
            for (int rowStart = 0; rowStart < SIZE_OF_FIELD; rowStart += SIZE_OF_SQUARE) {
                group = addBooleanValues(group, getNumbersInSquare(lineStart, rowStart));
                if(!areAllNumbersPresent(group)) return false;
                for(int i = 1; i <= SIZE_OF_FIELD; i++) group[i] = false;
            }
        }

        return true;
    }
}