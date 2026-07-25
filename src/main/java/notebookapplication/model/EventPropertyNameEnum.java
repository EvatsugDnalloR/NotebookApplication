package notebookapplication.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Enum representing property change event types for the notebook application.
 * Each enum constant maps to a unique string identifier used in property change events.
 */
public enum EventPropertyNameEnum {
    PAGE_RENAME("0"),
    SET_CONTENT("1"),
    GROUP_RENAME("2"),
    ADD_PAGE("3"),
    ADD_GROUP("4"),
    SWITCH_TO_PAGE("5"),
    SWITCH_TO_GROUP("6"),
    REMOVE_GROUP("7"),
    REMOVE_PAGE("8"),
    LOAD_NOTEBOOK("9"),
    MOVE_GROUP("a"),
    MOVE_PAGE("b");

    public final String propertyName;

    EventPropertyNameEnum(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    /** Static map for efficient lookup of enum values by their property name strings.  */
    private static final Map<String, EventPropertyNameEnum> BY_PROPERTY_NAME = new HashMap<>();

    static {
        for (EventPropertyNameEnum e : values()) {
            BY_PROPERTY_NAME.put(e.propertyName, e);
        }
    }

    /**
     * Retrieves an EventPropertyNameEnum constant by its property name string.
     *
     * @param propertyName the string representation of the property name
     * @return the corresponding EventPropertyNameEnum, or null if not found
     */
    public static EventPropertyNameEnum fromPropertyName(String propertyName) {
        return BY_PROPERTY_NAME.get(propertyName);
    }
}