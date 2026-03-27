package org.example.lab2;

import java.util.*;

public class StringCalculator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringCalculator calculator = new StringCalculator();

        System.out.println("=== Калькулятор выражений ===");
        System.out.println("Поддерживаются: +, -, *, /, скобки, переменные, функции sin/cos/sqrt/abs");
        System.out.println("Для выхода введите: exit");
        System.out.println();

        while (true) {
            System.out.print("Введите выражение: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("До свидания!");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("Ошибка: выражение не может быть пустым.\n");
                continue;
            }

            try {
                Map<String, Double> variables = requestVariableValues(input, scanner);
                double result = calculator.evaluate(input, variables);
                System.out.printf("Результат: %s%n%n", formatResult(result));
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка в выражении: " + e.getMessage() + "\n");
            } catch (ArithmeticException e) {
                System.out.println("Математическая ошибка: " + e.getMessage() + "\n");
            }
        }

        scanner.close();
    }

    private static Map<String, Double> requestVariableValues(String expression, Scanner scanner) {
        Set<String> variableNames = extractVariableNames(expression);
        Map<String, Double> variables = new HashMap<>();

        for (String name : variableNames) {
            while (true) {
                System.out.print("Введите значение для переменной " + name + ": ");
                String valueInput = scanner.nextLine().trim();
                try {
                    variables.put(name, Double.parseDouble(valueInput));
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Некорректное значение, введите число.");
                }
            }
        }

        return variables;
    }

    private static String formatResult(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public double evaluate(String expression) {
        return evaluate(expression, Collections.emptyMap());
    }

    public double evaluate(String expression, Map<String, Double> variables) {
        expression = expression.replaceAll("\\s", "");

        if (expression.isEmpty()) {
            throw new IllegalArgumentException("Выражение пустое");
        }
        
        expression = substituteVariables(expression, variables);

        List<String> postfixTokens = infixToPostfix(expression);
        return evaluatePostfix(postfixTokens);
    }

    private String substituteVariables(String expression, Map<String, Double> variables) {
        List<String> sortedNames = new ArrayList<>(variables.keySet());
        sortedNames.sort((a, b) -> b.length() - a.length());

        for (String name : sortedNames) {
            if (isKnownFunction(name)) continue;
            double value = variables.get(name);
            String replacement = value < 0 ? "(" + value + ")" : String.valueOf(value);
            expression = expression.replaceAll("(?<![a-zA-Z0-9])" + name + "(?![a-zA-Z0-9])", replacement);
        }

        int i = 0;
        while (i < expression.length()) {
            char ch = expression.charAt(i);
            if (Character.isLetter(ch)) {
                StringBuilder identifier = new StringBuilder();
                while (i < expression.length() && Character.isLetterOrDigit(expression.charAt(i))) {
                    identifier.append(expression.charAt(i));
                    i++;
                }
                String name = identifier.toString();
                if (!isKnownFunction(name)) {
                    throw new IllegalArgumentException("Неизвестная переменная или функция: " + name);
                }
            } else {
                i++;
            }
        }

        return expression;
    }

    public static Set<String> extractVariableNames(String expression) {
        Set<String> knownFunctions = Set.of("sin", "cos", "sqrt", "abs");
        Set<String> foundVariables = new LinkedHashSet<>();

        int index = 0;
        while (index < expression.length()) {
            char currentChar = expression.charAt(index);
            if (Character.isLetter(currentChar)) {
                StringBuilder identifier = new StringBuilder();
                while (index < expression.length() && Character.isLetterOrDigit(expression.charAt(index))) {
                    identifier.append(expression.charAt(index));
                    index++;
                }
                String name = identifier.toString();
                if (!knownFunctions.contains(name)) {
                    foundVariables.add(name);
                }
            } else {
                index++;
            }
        }

        return foundVariables;
    }

    private List<String> infixToPostfix(String infixExpression) {
        List<String> outputQueue = new ArrayList<>();
        Deque<String> operatorStack = new ArrayDeque<>();
        boolean expectingOperand = true;

        int i = 0;
        while (i < infixExpression.length()) {
            char currentChar = infixExpression.charAt(i);

            if (Character.isDigit(currentChar) || (currentChar == '-' && expectingOperand)) {
                StringBuilder number = new StringBuilder();
                number.append(currentChar);
                i++;
                while (i < infixExpression.length() &&
                        (Character.isDigit(infixExpression.charAt(i)) || infixExpression.charAt(i) == '.')) {
                    number.append(infixExpression.charAt(i));
                    i++;
                }
                outputQueue.add(number.toString());
                expectingOperand = false;
                continue;
            }

            if (Character.isLetter(currentChar)) {
                StringBuilder functionName = new StringBuilder();
                while (i < infixExpression.length() && Character.isLetter(infixExpression.charAt(i))) {
                    functionName.append(infixExpression.charAt(i));
                    i++;
                }
                String name = functionName.toString();
                if (!isKnownFunction(name)) {
                    throw new IllegalArgumentException("Неизвестная функция: " + name);
                }
                operatorStack.push(name);
                expectingOperand = true;
                continue;
            }

            if (currentChar == '(') {
                operatorStack.push("(");
                expectingOperand = true;
                i++;
                continue;
            }

            if (currentChar == ')') {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                if (operatorStack.isEmpty()) {
                    throw new IllegalArgumentException("Несбалансированные скобки: лишняя ')'");
                }
                operatorStack.pop();
                if (!operatorStack.isEmpty() && isKnownFunction(operatorStack.peek())) {
                    outputQueue.add(operatorStack.pop());
                }
                expectingOperand = false;
                i++;
                continue;
            }

            if (isOperatorChar(currentChar)) {
                String operatorToken = String.valueOf(currentChar);
                while (!operatorStack.isEmpty()
                        && !operatorStack.peek().equals("(")
                        && operatorPrecedence(operatorStack.peek()) >= operatorPrecedence(operatorToken)) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(operatorToken);
                expectingOperand = true;
                i++;
                continue;
            }

            throw new IllegalArgumentException("Недопустимый символ в выражении: '" + currentChar + "'");
        }

        while (!operatorStack.isEmpty()) {
            String top = operatorStack.pop();
            if (top.equals("(")) {
                throw new IllegalArgumentException("Несбалансированные скобки: лишняя '('");
            }
            outputQueue.add(top);
        }

        return outputQueue;
    }

    private double evaluatePostfix(List<String> postfixTokens) {
        Deque<Double> operandStack = new ArrayDeque<>();

        for (String token : postfixTokens) {
            if (isNumeric(token)) {
                operandStack.push(Double.parseDouble(token));
            } else if (isKnownFunction(token)) {
                if (operandStack.isEmpty()) {
                    throw new IllegalArgumentException("Недостаточно операндов для функции: " + token);
                }
                double argument = operandStack.pop();
                operandStack.push(applyFunction(token, argument));
            } else {
                if (operandStack.size() < 2) {
                    throw new IllegalArgumentException("Недостаточно операндов для оператора: " + token);
                }
                double rightOperand = operandStack.pop();
                double leftOperand = operandStack.pop();
                operandStack.push(applyBinaryOperator(token.charAt(0), leftOperand, rightOperand));
            }
        }

        if (operandStack.size() != 1) {
            throw new IllegalArgumentException("Некорректное выражение: несбалансированные операторы и операнды");
        }

        return operandStack.pop();
    }

    private double applyFunction(String functionName, double argument) {
        return switch (functionName) {
            case "sin"  -> Math.sin(Math.toRadians(argument));
            case "cos"  -> Math.cos(Math.toRadians(argument));
            case "sqrt" -> {
                if (argument < 0) throw new ArithmeticException("Корень из отрицательного числа: " + argument);
                yield Math.sqrt(argument);
            }
            case "abs"  -> Math.abs(argument);
            default -> throw new IllegalArgumentException("Неизвестная функция: " + functionName);
        };
    }

    private int operatorPrecedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> -1;
        };
    }

    private boolean isKnownFunction(String name) {
        return name.equals("sin") || name.equals("cos") || name.equals("sqrt") || name.equals("abs");
    }

    private boolean isOperatorChar(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double applyBinaryOperator(char operator, double leftOperand, double rightOperand) {
        return switch (operator) {
            case '+' -> leftOperand + rightOperand;
            case '-' -> leftOperand - rightOperand;
            case '*' -> leftOperand * rightOperand;
            case '/' -> {
                if (rightOperand == 0) throw new ArithmeticException("Деление на ноль");
                yield leftOperand / rightOperand;
            }
            default -> throw new IllegalArgumentException("Неизвестный оператор: " + operator);
        };
    }
}