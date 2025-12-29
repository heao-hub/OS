package experiment03;

import java.util.Scanner;

public class InputValidator {
    private static Scanner scanner = new Scanner(System.in);

    /**
     * 读取正整数
     * @param prompt 提示信息
     * @return 有效的正整数
     */
    public static int readPositiveInteger(String prompt) {
        int num;
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("输入不能为空，请重新输入！");
                    continue;
                }
                num = Integer.parseInt(input);
                if (num <= 0) {
                    System.out.println("请输入必须是正整数，请重新输入！");
                } else {
                    return num;
                }
            } catch (NumberFormatException e) {
                System.out.println("输入格式错误，请输入有效的整数！");
            }
        }
    }

    /**
     * 读取非负整数
     * @param prompt 提示信息
     * @return 有效的非负整数
     */
    public static int readNonNegativeInteger(String prompt) {
        int num;
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("输入不能为空，请重新输入！");
                    continue;
                }
                num = Integer.parseInt(input);
                if (num < 0) {
                    System.out.println("输入不能为负数，请重新输入！");
                } else {
                    return num;
                }
            } catch (NumberFormatException e) {
                System.out.println("输入格式错误，请输入有效的整数！");
            }
        }
    }

    /**
     * 读取字符串
     * @param prompt 提示信息
     * @return 非空字符串
     */
    public static String readNonEmptyString(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("输入不能为空，请重新输入！");
            } else {
                return input;
            }
        }
    }

    /**
     * 读取指定范围内的整数
     * @param prompt 提示信息
     * @param min 最小值
     * @param max 最大值
     * @return 指定范围内的整数
     */
    public static int readIntegerInRange(String prompt, int min, int max) {
        int num;
        while (true) {
            num = readNonNegativeInteger(prompt);
            if (num >= min && num <= max) {
                return num;
            } else {
                System.out.println("输入必须在" + min + "-" + max + "之间，请重新输入！");
            }
        }
    }
}