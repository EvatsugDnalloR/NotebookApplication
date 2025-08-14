package notebookapplication.model;

import java.util.HashMap;
import java.util.Map;

public enum EventPropertyNameEnum {
    PAGE_RENAME("0"),
    SET_CONTENT("1"),
    GROUP_RENAME("2"),
    ADD_PAGE("3"),
    ADD_GROUP("4"),
    SWITCH_TO_PAGE("5"),
    SWITCH_TO_GROUP("6"),
    REMOVE_GROUP("7"),
    REMOVE_PAGE("8");

    public final String propertyName;

    EventPropertyNameEnum(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    private static final Map<String, EventPropertyNameEnum> BY_PROPERTY_NAME = new HashMap<>();

    static {
        for (EventPropertyNameEnum e : values()) {
            BY_PROPERTY_NAME.put(e.propertyName, e);
        }
    }

    public static EventPropertyNameEnum fromPropertyName(String propertyName) {
        return BY_PROPERTY_NAME.get(propertyName);
    }
}
