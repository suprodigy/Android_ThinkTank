package com.boostcamp.jr.thinktank.manager;

import android.util.Pair;
import android.util.SparseArray;
import android.widget.TextView;

import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.utils.TypeCastingUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;

/**
 * Created by jr on 2017-02-11.
 */

public class KeywordManager {

    private static KeywordManager sAnalyzer;

    private Map<String, Integer> mKeyMap = new HashMap<>();
    private Map<String, Integer> mCountMap = new HashMap<>();
    private SparseArray<String> mIndexMap = new SparseArray<>();
    private ArrayList<ArrayList<Boolean>> isConnected = new ArrayList<>();
    private int mCount;

    private KeywordManager() { init(); }

    public static KeywordManager get() {
        if (sAnalyzer == null) {
            sAnalyzer = new KeywordManager();
        }
        return sAnalyzer;
    }

    private void init() {
        OrderedRealmCollection<KeywordItem> results = KeywordObserver.get().selectAll();
        mCount = results.size();

        for(KeywordItem keyword :  results) {
            mKeyMap.put(keyword.getName(), keyword.getId());
            mIndexMap.put(keyword.getId(), keyword.getName());
            mCountMap.put(keyword.getName(), keyword.getCount());
            byte[] temp = keyword.getRelation();
            isConnected.add(TypeCastingUtil.byteArrayToBoolArray(temp));
        }
    }

    public void createOrUpdateKeyword(String name) {
        if (isKeywordExist(name)) {
            KeywordItem item = KeywordObserver.get().getKeywordByName(name);
            item = KeywordObserver.get().getCopiedObject(item);
            item.setCount(item.getCount()+1);
            KeywordObserver.get().update(item);
            mCountMap.put(name, mCountMap.get(name) + 1);
        } else {
            insertIntoMap(name, mCount);
            KeywordObserver.get().insert(new KeywordItem()
                    .setName(name)
                    .setRelation(new byte[mCount/8]));
            mCount++;
        }
    }

    private void insertIntoMap(String name, int count) {
        mKeyMap.put(name, count);
        mIndexMap.put(count, name);
        mCountMap.put(name, 1);
    }

    public void updateKeywordRelation(ThinkItem thinkItem) {
        RealmList<KeywordItem> keywords = thinkItem.getKeywords();

        for (int i=0; i<keywords.size(); i++) {
            KeywordItem item = KeywordObserver.get().getCopiedObject(keywords.get(i));
            int idx1 = mKeyMap.get(item.getName());

            // relation update
            byte[] relation = item.getRelation();
            for (int j=0; j<keywords.size()-1; j++) {
                // 해당 키워드의 relation update
                String opponentName = keywords.get(j).getName();
                int idx2 = mKeyMap.get(opponentName);
                relation = updateByteArray(relation, idx2);

                // 반대 키워드의 relation update
                String name = mIndexMap.get(idx2);
                KeywordItem opponent = KeywordObserver.get().getKeywordByName(name);
                opponent = KeywordObserver.get().getCopiedObject(opponent);
                byte[] opponentRelation = opponent.getRelation();
                opponentRelation = updateByteArray(opponentRelation, idx1);
                opponent.setRelation(opponentRelation);
                KeywordObserver.get().update(opponent);

                // isConnected 업데이트
                connect(idx1, idx2);
            }

            KeywordObserver.get().update(item);
        }
    }

    private boolean isKeywordExist(String name) {
        return mKeyMap.containsKey(name);
    }

    private byte[] updateByteArray(byte[] relation, int idx) {
        // 만약 지금 가지고 있는 배열로 관계 표현이 어렵다면 byte 추가
        while (relation.length < idx/8) {
            ArrayList<Byte> temp = TypeCastingUtil.toByteArrayList(relation);
            temp.add((byte)0);
            relation = TypeCastingUtil.toByteArray(temp);
        }
        relation[idx/8] |= (1 << (idx%8));
        return relation;
    }

    private void connect(int idx1, int idx2) {
        while(isConnected.size() < Math.max(idx1, idx2)) {
            isConnected.add(new ArrayList<Boolean>());
        }
        while(isConnected.get(idx1).size() < idx2) {
            isConnected.get(idx1).add(false);
        }
        while(isConnected.get(idx2).size() < idx1) {
            isConnected.get(idx2).add(false);
        }
        isConnected.get(idx1).set(idx2, true);
        isConnected.get(idx2).set(idx1, true);
    }

    public void getKeywordByBFS(String keywordStarted, List<TextView> textViews) {

        boolean[] checked = new boolean[mCount];
        PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(0,
                new Comparator<Pair<Integer, Integer>>(){
                    @Override
                    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                        return o2.second.compareTo(o1.second);
                    }
                });

        int startIdx = mKeyMap.get(keywordStarted);
        checked[startIdx] = true;
        pq.offer(new Pair<>(startIdx, mCountMap.get(keywordStarted)));

        while (pq.size() != 0) {
            Pair<Integer, Integer> now = pq.poll();

            ArrayList<Boolean> path = isConnected.get(now.first);

            int len = path.size();

            for(int i=0; i<len; i++) {
                if (path.get(i) && !checked[i]) {
                    checked[i] = true;
                    pq.offer(new Pair<>(i, mCountMap.get(mIndexMap.get(i))));
                }
            }
        }

    }

}
