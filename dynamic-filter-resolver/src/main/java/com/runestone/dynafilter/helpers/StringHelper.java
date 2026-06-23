package com.runestone.dynafilter.helpers;

public class StringHelper {

    public static String formatPath(String[] paths) {
        if (paths == null) {
            return null;
        }
        return String.join(", ", paths);
    }

}
