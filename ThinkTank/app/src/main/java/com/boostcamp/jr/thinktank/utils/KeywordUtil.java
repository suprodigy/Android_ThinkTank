package com.boostcamp.jr.thinktank.utils;

/**
 * Created by jr on 2017-02-12.
 */

public class KeywordUtil {

    public static String removeTag(String keyword) {
        int startIdx = 0;
        try {
            while (keyword.charAt(startIdx) == '#') {
                startIdx++;
            }
            return keyword.substring(startIdx);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

}
