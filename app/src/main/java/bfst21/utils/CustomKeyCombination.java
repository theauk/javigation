package bfst21.utils;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a subclass of {@link KeyCombination} which allows any combination of keys instead of only function keys plus a normal key.
 * It must observe a JavaFX Node in order to keep track of which keys has been pressed/released.
 */
public class CustomKeyCombination extends KeyCombination {
    public static final List<KeyCode> keyCodes = new ArrayList<>();
    private final List<KeyCode> needed;
    private static boolean targetSet;

    /**
     * Constructor which adds the specified KeyCodes to a List representing the key combination.
     *
     * @param codes the list of KeyCodes which corresponds to the key combination.
     */
    public CustomKeyCombination(KeyCode... codes) {
        needed = Arrays.asList(codes);
    }

    /**
     * Checks if the specified keys has been pressed for the specific key combination.
     *
     * @param e the KeyEvent associated with the key press.
     * @return true if it is a match, false if not.
     */
    @Override
    public boolean match(KeyEvent e) {
        if(!targetSet) throw new IllegalStateException("CustomKeyCombination must have a single shared target to observe. Use method \"setTarget\" to set a Node target.");
        return keyCodes.containsAll(needed);
    }

    /**
     * Sets the target Node to be observed shared between all instances of this class
     * and attaches two KeyEvent listeners to add/remove key presses/releases to
     * the specific key combinations needed key codes.
     *
     * @param node the Node to be observed.
     */
    public static void setTarget(Node node) {
        targetSet = true;
        node.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if(!keyCodes.contains(e.getCode())) keyCodes.add(e.getCode());
        });
        node.addEventFilter(KeyEvent.KEY_RELEASED, e -> keyCodes.remove(e.getCode()));
    }
}