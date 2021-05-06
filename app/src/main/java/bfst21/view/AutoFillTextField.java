package bfst21.view;

import bfst21.utils.AddressFilter;
import bfst21.utils.Filter;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AutoFillTextField extends TextField {
    private Filter filter;
    private final ContextMenu popup;
    private static final int MAX_ENTRIES = 10;

    private List<String> searchResult;

    public AutoFillTextField() {
        popup = new ContextMenu();
        filter = new AddressFilter();
        searchResult = new ArrayList<>();
        setListeners();
    }

    private void setListeners() {
        textProperty().addListener(((observable, oldValue, newValue) -> {
            String entered = getText();

            if(entered == null || entered.isEmpty()) popup.hide();
            else {
                filter.search(getText());
                searchResult = filter.getSuggestions();

                if(!searchResult.isEmpty()) {
                    addToPopup(searchResult);

                    if(!popup.isShowing()) popup.show(this, Side.BOTTOM, 0, 0);
                } else popup.hide();
            }
        }));

        focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue && !getText().isBlank()) popup.show(this, Side.BOTTOM, 0, 0);  //If focus is gained show and field is not empty
            else popup.hide();
        }));
    }

    private void addToPopup(List<String> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        int count = Math.min(searchResult.size(), MAX_ENTRIES);

        for (int i = 0; i < count; i++) {
            String entry = searchResult.get(i);
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

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
