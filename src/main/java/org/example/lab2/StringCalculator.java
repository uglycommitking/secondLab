package org.example.lab2;

import java.util.*;

public class StringCalculator {

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
                throw new IllegalArgumentException("Неизвестная переменная: " + identifier);
            } else {
                i++;
            }
        }

        return expression;
    }

    public static Set<String> extractVariableNames(String expression) {
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
                foundVariables.add(identifier.toString());
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

    private int operatorPrecedence(String operator) {
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> -1;
        };
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