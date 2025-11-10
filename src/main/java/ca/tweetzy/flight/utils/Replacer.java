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

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Date Created: April 10 2022
 * Time Created: 4:02 p.m.
 *
 * @author Kiran Hart
 */
@UtilityClass
public final class Replacer {

    private final Pattern TOKEN_PATTERN = Pattern.compile("([%{])([^%{}]+)([%}])");

    private String replaceTokens(String text, Map<String, Object> valuesByKey) {
        StringBuilder output = new StringBuilder();
        Matcher tokenMatcher = TOKEN_PATTERN.matcher(text);

        int cursor = 0;
        while (tokenMatcher.find()) {
            int tokenStart = tokenMatcher.start();
            output.append(text.substring(cursor, tokenStart));

            String key = tokenMatcher.group(2);
            if (valuesByKey.containsKey(key)) {
                output.append(valuesByKey.get(key));
            } else {
                output.append(tokenMatcher.group());
            }

            cursor = tokenMatcher.end();
        }
        output.append(text.substring(cursor));

        return output.toString();
    }

    public String replaceVariables(String text, Object... replacements) {
        final Map<String, Object> map = new HashMap<>();

        if (replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                map.put((String) replacements[i], replacements[i + 1]);
            }
        }

        return replaceTokens(text, map);
    }

    public List<String> replaceVariables(final List<String> list, Object... replacements) {
        return list.stream().map(item -> replaceVariables(item, replacements)).toList();
    }
}
