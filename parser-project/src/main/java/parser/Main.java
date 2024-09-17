package parser;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Parser parser = new Parser();
        Generator generator = new Generator();
        char choice;

        printMenu();

        do {
            System.out.print("\nВыберите вариант: ");
            choice = scanner.next().charAt(0);

            switch (choice) {
                case '1':
                    printSlesh();
                    System.out.print("Введите цепочку для проверки (через пробел): ");
                    scanner.nextLine();  // Очистка буфера
                    String inputText = scanner.nextLine();
                    List<String> input = Arrays.asList(inputText.split("\\s+"));
                    boolean isValid = parser.parse(input);
                    System.out.println("Цепочка " + (isValid ? "принадлежит" : "не принадлежит") + " языку.");
                    printSlesh();
                    break;

                case '2':
                    printSlesh();
                    String generated = generator.generate();
                    System.out.println("Сгенерированная цепочка: " + generated);
                    printSlesh();
                    break;

                case '0':
                    printSnow();
                    System.out.println("Работа программы завершена.");
                    printSnow();
                    break;

                case 'm':
                    printMenu();
                    break;

                default:
                    System.out.println("Неверный выбор. Пожалуйста, выберите снова.");
            }
        } while (choice != '0');

        scanner.close();
    }

    public static void printSnow() {
        System.out.println("********************************************");
    }

    public static void printSlesh() {
        System.out.println("////////////////////////////////////////////");
    }

    public static void printMenu() {
        printSnow();
        System.out.println("\t\t   MENU");
        printSnow();
        System.out.println("[M] - Напечатать MENU");
        System.out.println("[1] - Ввести цепочку для проверки");
        System.out.println("[2] - Сгенерировать цепочку");
        System.out.println("[0] - Завершить работу программы");
        printSnow();
    }
}
