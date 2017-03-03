package ru.katsoft;

public class Container implements Comparable<Container>{
    int key = 0;
    int value = 0;

    @Override
    public int compareTo(Container c) {
        return value - c.value;
    }
}
