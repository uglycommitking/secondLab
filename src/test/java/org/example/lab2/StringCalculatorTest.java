package org.example.lab2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StringCalculatorTest {

    private StringCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new StringCalculator();
    }

    @Test
    void addition() {
        assertEquals(5.0, calculator.evaluate("2+3"));
    }

    @Test
    void subtraction() {
        assertEquals(1.0, calculator.evaluate("3-2"));
    }

    @Test
    void multiplication() {
        assertEquals(6.0, calculator.evaluate("2*3"));
    }

    @Test
    void division() {
        assertEquals(2.5, calculator.evaluate("5/2"));
    }

    @Test
    void divisionByZero() {
        assertThrows(ArithmeticException.class, () -> calculator.evaluate("5/0"));
    }


    @ParameterizedTest
    @CsvSource({
        "2+3*4,    14.0",
        "10-2*3,   4.0",
        "8/2+3,    7.0",
        "2*3+4*5,  26.0"
    })
    void operatorPrecedence(String expression, double expected) {
        assertEquals(expected, calculator.evaluate(expression));
    }

    @ParameterizedTest
    @CsvSource({
        "(2+3)*4,      20.0",
        "2*(3+4),      14.0",
        "(2+3)*(4-1),  15.0",
        "((2+3))*4,    20.0"
    })
    void parentheses(String expression, double expected) {
        assertEquals(expected, calculator.evaluate(expression));
    }

    @Test
    void mismatchedOpenParenthesis() {
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate("(2+3"));
    }

    @Test
    void mismatchedCloseParenthesis() {
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate("2+3)"));
    }

    @Test
    void unaryMinus() {
        assertEquals(-3.0, calculator.evaluate("-3"));
    }

    @Test
    void unaryMinusInExpression() {
        assertEquals(-1.0, calculator.evaluate("-3+2"));
    }


    @Test
    void decimalNumbers() {
        assertEquals(0.3, calculator.evaluate("0.1+0.2"), 1e-9);
    }

    @Test
    void decimalMultiplication() {
        assertEquals(0.6, calculator.evaluate("0.2*3"), 1e-9);
    }

    @Test
    void expressionWithSpaces() {
        assertEquals(5.0, calculator.evaluate("2 + 3"));
    }

    @Test
    void expressionWithManySpaces() {
        assertEquals(14.0, calculator.evaluate("2 + 3 * 4"));
    }

    @Test
    void emptyExpression() {
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate(""));
    }

    @Test
    void expressionWithUnknownSymbol() {
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate("2@3"));
    }

    @Test
    void expressionWithUnknownFunction() {
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate("log(10)"));
    }

    @Test
    void singleVariable() {
        assertEquals(10.0, calculator.evaluate("x", Map.of("x", 10.0)));
    }

    @Test
    void variableInExpression() {
        assertEquals(7.0, calculator.evaluate("x+2", Map.of("x", 5.0)));
    }

    @Test
    void multipleVariables() {
        assertEquals(11.0, calculator.evaluate("x+y*2", Map.of("x", 3.0, "y", 4.0)));
    }

    @Test
    void negativeVariable() {
        assertEquals(-3.0, calculator.evaluate("x+2", Map.of("x", -5.0)));
    }

    @Test
    void unknownVariable() {
        assertThrows(IllegalArgumentException.class, () -> calculator.evaluate("x+2"));
    }

    @Test
    void extractVariableNames() {
        var variables = StringCalculator.extractVariableNames("x+y*sin(z)");
        assertTrue(variables.contains("x"));
        assertTrue(variables.contains("y"));
        assertTrue(variables.contains("z"));
        assertFalse(variables.contains("sin"));
    }

    @Test
    void sinOf30() {
        assertEquals(0.5, calculator.evaluate("sin(30)"), 1e-9);
    }

    @Test
    void sinOf0() {
        assertEquals(0.0, calculator.evaluate("sin(0)"), 1e-9);
    }

    @Test
    void sinOf90() {
        assertEquals(1.0, calculator.evaluate("sin(90)"), 1e-9);
    }

    @Test
    void cosOf0() {
        assertEquals(1.0, calculator.evaluate("cos(0)"), 1e-9);
    }

    @Test
    void cosOf90() {
        assertEquals(0.0, calculator.evaluate("cos(90)"), 1e-9);
    }

    @Test
    void cosOf60() {
        assertEquals(0.5, calculator.evaluate("cos(60)"), 1e-9);
    }

    @Test
    void sqrtOf4() {
        assertEquals(2.0, calculator.evaluate("sqrt(4)"), 1e-9);
    }

    @Test
    void sqrtOf9() {
        assertEquals(3.0, calculator.evaluate("sqrt(9)"), 1e-9);
    }

    @Test
    void sqrtOfNegative() {
        assertThrows(ArithmeticException.class, () -> calculator.evaluate("sqrt(-1)"));
    }

    @Test
    void absOfNegative() {
        assertEquals(5.0, calculator.evaluate("abs(-5)"), 1e-9);
    }

    @Test
    void absOfPositive() {
        assertEquals(5.0, calculator.evaluate("abs(5)"), 1e-9);
    }

    @Test
    void functionInExpression() {
        assertEquals(3.0, calculator.evaluate("sqrt(4)+1"), 1e-9);
    }

    @Test
    void nestedFunctionAndVariable() {
        assertEquals(1.0, calculator.evaluate("sin(x)", Map.of("x", 90.0)), 1e-9);
    }
}