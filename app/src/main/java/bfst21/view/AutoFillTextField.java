package bfst21.view;

import javafx.beans.NamedArg;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.LinkedList;
import java.util.List;

/**
 * A TextField with the ability to show a list of clickable suggestions.
 * If a suggestion is clicked, the text will be transferred to the TextField.
 */
public class AutoFillTextField extends TextField {

    private final ContextMenu popup;
    private final int maxEntries;
    private boolean suggest;

    /**
     * Creates an AutoFillTextField with the specified number of max suggestions
     * per call to the {@link AutoFillTextField#suggest(List)} method.
     *
     * @param maxEntries the maximum number of suggestions the TextField can show.
     */
    public AutoFillTextField(@NamedArg("maxEntries") int maxEntries) {
        popup = new ContextMenu();
        this.maxEntries = maxEntries;
        suggest = true;
        setListeners();
    }

    /**
     * Shows a list of possible suggestions as a ContextMenu right under the TextField if {@link AutoFillTextField#suggest} is true.
     *
     * @param suggestions a list of Strings containing the suggestions to show.
     */
    public void suggest(List<String> suggestions) {
        popup.getItems().clear();

        if (suggest) {
            if (!suggestions.isEmpty()) {
                addToPopup(suggestions);
                if (!popup.isShowing()) popup.show(this, Side.BOTTOM, 0, 0);
            } else popup.hide();
        }
    }

    /**
     * Prepares the TextField with Listeners for defining
     * when to show the suggestion popup.
     */
    private void setListeners() {
        textProperty().addListener(((observable, oldValue, newValue) -> {
            String entered = getText();
            if (entered == null || entered.isEmpty()) popup.hide();
        }));

        focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue && !getText().isBlank())
                popup.show(this, Side.BOTTOM, 0, 0);  //If focus is gained and field is not empty
            else popup.hide();
        }));
    }

    /**
     * Creates the popup's entries and only displays between 1 and {@link AutoFillTextField#maxEntries}
     * only showing the smallest amount possible.
     *
     * @param suggestions a List of Strings containing the entries to show in the suggestion popup.
     */
    private void addToPopup(List<String> suggestions) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int count = Math.min(suggestions.size(), maxEntries);

        for (int i = 0; i < count; i++) {
            String entry = suggestions.get(i);
            Label label = new Label(entry);

            CustomMenuItem item = new CustomMenuItem(label, true);
            menuItems.add(item);

            item.setOnAction(e -> {
                setText(entry);
                positionCaret(entry.length());
                popup.hide();
            });
        }

        popup.getItems().clear();
        popup.getItems().addAll(menuItems);
    }

    /**
     * Sets whether to show the suggestions popup or not.
     *
     * @param suggest true to show suggestions in popup else false.
     */
    public void setSuggest(boolean suggest) {
        this.suggest = suggest;
    }
}