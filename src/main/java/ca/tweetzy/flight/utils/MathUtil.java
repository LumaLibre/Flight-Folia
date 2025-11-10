/*
 * Flight
 * Copyright 2022 Kiran Hart
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ca.tweetzy.flight.utils;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Date Created: April 06 2022
 * Time Created: 4:35 p.m.
 *
 * @author Kiran Hart
 */
public final class MathUtil {

    /**
     * Holds all valid roman numbers
     */
    private static final NavigableMap<Integer, String> romanNumbers = new TreeMap<>();

    // Load the roman numbers
    static {
        romanNumbers.put(1000, "M");
        romanNumbers.put(900, "CM");
        romanNumbers.put(500, "D");
        romanNumbers.put(400, "CD");
        romanNumbers.put(100, "C");
        romanNumbers.put(90, "XC");
        romanNumbers.put(50, "L");
        romanNumbers.put(40, "XL");
        romanNumbers.put(10, "X");
        romanNumbers.put(9, "IX");
        romanNumbers.put(5, "V");
        romanNumbers.put(4, "IV");
        romanNumbers.put(1, "I");
    }

    /**
     * If the string can be parsed as an integer
     *
     * @param s The string to check
     *
     * @return true if it's an integer
     */
    public static boolean isInt(final String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * If the string can be parsed as a double
     *
     * @param s The string to check
     *
     * @return true if it's a double
     */
    public static boolean isDouble(final String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Return a roman number representation of the given number
     *
     * @param number to be converted
     *
     * @return converted number
     */
    public static String toRoman(final int number) {
        if (number == 0)
            return "0";

        final int literal = romanNumbers.floorKey(number);

        if (number == literal)
            return romanNumbers.get(number);

        return romanNumbers.get(literal) + toRoman(number - literal);
    }

    /**
     * Evaluate the given expression
     *
     * @param expression math expression
     *
     * @return the calculated result
     */
    public static double calculate(final String expression) {
        class Parser {
            int pos = -1, c;

            void eatChar() {
                c = ++pos < expression.length() ? expression.charAt(pos) : -1;
            }

            void eatSpace() {
                while (Character.isWhitespace(c))
                    eatChar();
            }

            double parse() {
                eatChar();

                final double v = parseExpression();

                if (c != -1)
                    throw new CalculatorException("Unexpected: " + (char) c);

                return v;
            }

            double parseExpression() {
                double v = parseTerm();

                for (; ; ) {
                    eatSpace();

                    if (c == '+') { // addition
                        eatChar();
                        v += parseTerm();
                    } else if (c == '-') { // subtraction
                        eatChar();
                        v -= parseTerm();
                    } else
                        return v;

                }
            }

            double parseTerm() {
                double v = parseFactor();

                for (; ; ) {
                    eatSpace();

                    if (c == '/') { // division
                        eatChar();
                        v /= parseFactor();
                    } else if (c == '*' || c == '(') { // multiplication
                        if (c == '*')
                            eatChar();
                        v *= parseFactor();
                    } else
                        return v;
                }
            }

            double parseFactor() {
                double v;
                boolean negate = false;

                eatSpace();

                if (c == '+' || c == '-') { // unary plus & minus
                    negate = c == '-';
                    eatChar();
                    eatSpace();
                }

                if (c == '(') { // brackets
                    eatChar();
                    v = parseExpression();
                    if (c == ')')
                        eatChar();
                } else { // numbers
                    final StringBuilder sb = new StringBuilder();

                    while (c >= '0' && c <= '9' || c == '.') {
                        sb.append((char) c);
                        eatChar();
                    }

                    if (sb.length() == 0)
                        throw new CalculatorException("Unexpected: " + (char) c);

                    v = Double.parseDouble(sb.toString());
                }
                eatSpace();
                if (c == '^') { // exponentiation
                    eatChar();
                    v = Math.pow(v, parseFactor());
                }
                if (negate)
                    v = -v; // unary minus is applied after exponentiation; e.g. -3^2=-9
                return v;
            }
        }
        return new Parser().parse();
    }

    public static final class CalculatorException extends RuntimeException {

        public CalculatorException(final String message) {
            super(message);
        }
    }
}


