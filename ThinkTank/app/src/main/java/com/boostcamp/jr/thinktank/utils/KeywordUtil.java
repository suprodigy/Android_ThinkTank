package com.boostcamp.jr.thinktank.utils;

import android.util.Pair;
import android.util.SparseArray;

/**
 * Created by jr on 2017-02-12.
 */

public class KeywordUtil {

    public static SparseArray<Integer> orderMap = new SparseArray<>();

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

    public static int getOrderFromCount(int cnt) {

        if (orderMap.size() == 0) {
            int size = 5;

            int i = 0, row = size/2, col = size/2;
            orderMap.append(i, row*5 + col);

            int cntToMove = 1, s = 1;
            while (i < size * size) {
                for (int j=1; j<=cntToMove; j++) {
                    i++;
                    col += s;
                    orderMap.append(i, row*5 + col);
                }

                for (int j=1; j<=cntToMove; j++) {
                    i++;
                    row -= s;
                    orderMap.append(i, row*5 + col);
                }

                cntToMove += 1;
                s *= -1;
            }
        }

        return orderMap.get(cnt);

    }

    /**
     *
     * @param count 현재 size를 구해야 할 keyword의 count
     * @param minMaxCount: BFS의 결과로 나온 keyword의 List에서 count 최소값과 최대값
     * @return min, max에 대해 count의 상대적인 크기를 구함
     *
     * textSize는 15sp~40sp 사이의 값으로 제한
     *
     */
    public static float getTextSize(int count, Pair<Integer, Integer> minMaxCount) {

        int min = minMaxCount.first, max = minMaxCount.second;

        if (min == max) {
            return (40 + 15) / 2;
        } else {

            // y = ax + b (y: textSize, x: keyword.getCount())
            float a = 25 / (max - min);
            float b = 15 - a * min;

            return a * count + b;
        }

    }

}
