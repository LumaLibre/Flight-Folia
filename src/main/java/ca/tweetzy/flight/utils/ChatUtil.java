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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * Date Created: April 10 2022
 * Time Created: 1:48 p.m.
 *
 * @author Kiran Hart
 *         https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/
 */
public final class ChatUtil {

    private static final int CENTER_PX = 154;

    /**
     * It takes an enum and returns a string with the first letter of each word capitalized
     *
     * @param enumeration The enumeration to capitalize.
     *
     * @return The name of the enumeration in all caps.
     */
    public static String capitalizeFully(final Enum<?> enumeration) {
        return capitalizeFully(enumeration.name());
    }

    /**
     * It takes a string, formats it, and then capitalizes it
     *
     * @param string The string to capitalize, may be null
     *
     * @return The string with the first letter of each word capitalized.
     */
    public static String capitalizeFully(final String string) {
        return WordUtils.capitalizeFully(format(string));
    }

    /**
     * Capitalize the first letter of the given string.
     *
     * @param enumeration The enumeration to capitalize.
     *
     * @return The name of the enumeration.
     */
    public static String capitalize(final Enum<?> enumeration) {
        return capitalize(enumeration.name());
    }
    

    /**
     * It takes a string, formats it, and then capitalizes the first letter
     *
     * @param string The string to capitalize.
     *
     * @return The first letter of the string is capitalized.
     */
    public static String capitalize(final String string) {
        return StringUtils.capitalize(format(string));
    }

    /**
     * It takes a string and returns a string with the first letter capitalized and the rest of the letters lowercase
     *
     * @param enumeration The enumeration to format.
     *
     * @return The name of the enumeration.
     */
    public static String format(final Enum<?> enumeration) {
        return format(enumeration.name());
    }

    /**
     * It takes a string, makes it lowercase, replaces underscores with spaces, and trims the string
     *
     * @param string The string to format.
     *
     * @return The string is being returned in lower case and the underscores are being replaced with spaces.
     */
    public static String format(final String string) {
        return string.toLowerCase().replace("_", " ").trim();
    }

    /**
     * It takes a string, and adds spaces to the beginning of each line until the string is centered
     *
     * @param message The message you want to center.
     *
     * @return A string with the message centered.
     */
    public static String centerMessage(String message) {
        String[] lines = Common.colorize(message).split("\n", 40);
        int totalLength = 0;
        for (String line : lines) {
            totalLength += line.length() + 50; // Estimate: line + spaces + newline
        }
        StringBuilder returnMessage = new StringBuilder(totalLength);

        for (String line : lines) {
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;

            char[] chars = line.toCharArray();
            for (char c : chars) {
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l';
                } else {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                    messagePxSize++;
                }
            }
            int toCompensate = CENTER_PX - messagePxSize / 2;
            int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
            int spacesNeeded = Math.max(0, toCompensate / spaceLength);
            for (int i = 0; i < spacesNeeded; i++) {
                returnMessage.append(" ");
            }
            returnMessage.append(line).append("\n");
        }

        return returnMessage.toString();
    }

    enum DefaultFontInfo {

        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == SPACE) return this.getLength();
            return this.length + 1;
        }

        // Cache for O(1) lookup instead of O(n) iteration
        private static final java.util.Map<Character, DefaultFontInfo> CHAR_TO_FONT = new java.util.HashMap<>();
        
        static {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                CHAR_TO_FONT.put(dFI.getCharacter(), dFI);
            }
        }
        
        public static DefaultFontInfo getDefaultFontInfo(char c) {
            return CHAR_TO_FONT.getOrDefault(c, DEFAULT);
        }
    }

}
