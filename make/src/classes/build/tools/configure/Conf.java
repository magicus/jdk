package build.tools.configure;

import java.util.HashMap;
import java.util.Map;

public class Conf {
    /* All output variables goes into the map */
    private final Map<String, String> map = new HashMap<>();

    public String get(String variable) {
        return map.get(variable);
    }

    public void put(String variable, String value) {
        map.put(variable, value);
    }
}
