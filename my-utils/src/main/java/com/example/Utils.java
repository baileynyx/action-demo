// src/main/java/com/example/Utils.java
package com.example;

public class Utils {
    public static String greet(final String name) {
        return "Hello, " + name + "!";
    }

    public static void main(final String[] args) {
        System.out.println(greet("World"));
    }
}
