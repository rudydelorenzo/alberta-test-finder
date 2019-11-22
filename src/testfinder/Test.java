package testfinder;

import java.util.LinkedHashMap;

public class Test {
    public static LinkedHashMap<String,String> map = new LinkedHashMap() {{
        put("CLASS_1", "3");
        put("CLASS_2", "4");
        put("CLASS_3", "5");
        put("CLASS_4", "6");
        put("CLASS_5_BASIC", "7");
        put("CLASS_5_ADVANCED", "8");
        put("CLASS_6_BASIC", "9");
        put("CLASS_6_ADVANCED", "10");
    }};
    
    public static String translateClassToValue(String testClass) {
        return map.get(testClass);
    }
}