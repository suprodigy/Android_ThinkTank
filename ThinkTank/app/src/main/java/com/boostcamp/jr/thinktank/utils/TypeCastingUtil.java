package com.boostcamp.jr.thinktank.utils;

import java.util.ArrayList;

/**
 * Created by jr on 2017-02-11.
 */

public class TypeCastingUtil {

    public static byte[] toByteArray(ArrayList<Byte> bytes) {
        byte[] ret = new byte[bytes.size()];
        for (int i=0; i<ret.length; i++) {
            ret[i] = bytes.get(i);
        }
        return ret;
    }

    public static ArrayList<Byte> toByteArrayList(byte[] bytes) {
        ArrayList<Byte> ret = new ArrayList<>();
        for (int i=0; i<bytes.length; i++) {
            ret.add(bytes[i]);
        }
        return ret;
    }

    public static ArrayList<Boolean> byteArrayToBoolArray(byte[] relation) {
        ArrayList<Boolean> ret = new ArrayList<>();

        for(int i=0; i<relation.length * 8; i++) {
            byte temp = relation[i/8];
            if ((temp & (1 << (i%8))) != 0) {
                ret.add(true);
            } else {
                ret.add(false);
            }
        }

        return ret;
    }
}
