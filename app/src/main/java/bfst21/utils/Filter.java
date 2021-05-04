package bfst21.utils;

import java.util.List;

public interface Filter {
    void search(String prefix);
    List<String> getSuggestions();
}
