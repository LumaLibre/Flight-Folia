package ca.tweetzy.flight.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public final class TranslationEntry {

    final String key;
    final String[] contents;

    public String string() {
        return contents[0];
    }

    public List<String> list() {
        return List.of(contents);
    }
}
