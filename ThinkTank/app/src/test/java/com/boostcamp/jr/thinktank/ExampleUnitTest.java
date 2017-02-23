package com.boostcamp.jr.thinktank;

import android.support.v4.util.Pair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {

        PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(10,
                new Comparator<Pair<Integer, Integer>>(){
                    @Override
                    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                        return o2.second.compareTo(o1.second);
                    }
                });

        List<Pair<Integer, Integer>> list = new ArrayList<>();

        for (int i=0; i<10; i++) {
            Pair<Integer, Integer> pair = new Pair<>(i, 1);
            list.add(pair);
        }

        for (int i=9; i>=0; i--) {
            pq.offer(list.get(i));
        }

        while (!pq.isEmpty()) {
            Pair<Integer, Integer> now = pq.poll();

            System.out.println(now.first);
        }

    }
}