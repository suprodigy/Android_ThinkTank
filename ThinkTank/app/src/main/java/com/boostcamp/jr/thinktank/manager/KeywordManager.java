package com.boostcamp.jr.thinktank.manager;

import android.util.Pair;
import android.util.SparseArray;

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
            KeywordItem item = new KeywordItem()
                    .setName(name)
                    .setRelation(new byte[mCount/8]);
            KeywordObserver.get().insert(item);
            insertIntoMap(name, item.getId());
            isConnected.add(new ArrayList<Boolean>());
            mCount++;
        }
    }

    private void insertIntoMap(String name, int id) {
        mKeyMap.put(name, id);
        mIndexMap.put(id, name);
        mCountMap.put(name, 1);
    }

    public void updateKeywordRelation(ThinkItem thinkItem) {
        RealmList<KeywordItem> keywords = thinkItem.getKeywords();

        for (int i=0; i<keywords.size()-1; i++) {
            KeywordItem item = KeywordObserver.get().getCopiedObject(keywords.get(i));
            int idx1 = item.getId();

            // relation update
            byte[] relation = item.getRelation();
            for (int j=i+1; j<keywords.size(); j++) {
                // 해당 키워드의 relation update
                String opponentName = keywords.get(j).getName();
                int idx2 = mKeyMap.get(opponentName);
                relation = updateByteArray(relation, idx2);

                // 반대 키워드의 relation update
                KeywordItem opponent = KeywordObserver.get().getKeywordByName(opponentName);
                opponent = KeywordObserver.get().getCopiedObject(opponent);
                byte[] opponentRelation = opponent.getRelation();
                opponentRelation = updateByteArray(opponentRelation, idx1);
                opponent.setRelation(opponentRelation);
                KeywordObserver.get().update(opponent);

                // isConnected 업데이트
                connect(idx1, idx2);
            }

            item.setRelation(relation);
            KeywordObserver.get().update(item);
        }
    }

    private boolean isKeywordExist(String name) {
        return mKeyMap.containsKey(name);
    }

    private byte[] updateByteArray(byte[] relation, int idx) {
        // 만약 지금 가지고 있는 배열로 관계 표현이 어렵다면 byte 추가
        while (relation.length <= idx/8) {
            ArrayList<Byte> temp = TypeCastingUtil.toByteArrayList(relation);
            temp.add((byte)0);
            relation = TypeCastingUtil.toByteArray(temp);
        }
        relation[idx/8] |= (1 << (idx%8));
        return relation;
    }

    private void connect(int idx1, int idx2) {
        while(isConnected.size() <= Math.max(idx1, idx2)) {
            isConnected.add(new ArrayList<Boolean>());
        }
        while(isConnected.get(idx1).size() <= idx2) {
            isConnected.get(idx1).add(false);
        }
        while(isConnected.get(idx2).size() <= idx1) {
            isConnected.get(idx2).add(false);
        }
        isConnected.get(idx1).set(idx2, true);
        isConnected.get(idx2).set(idx1, true);
    }


    /**
     *
     * @param keywordStarted : 시작되는 키워드의 이름,
     *        minMaxCount : 키워드가 언급된 Count의 최대값, 최소값
     * @return 탐색 순서대로 keyword의 이름과 count를 Pair로 만들어 반환
     *
     * PriorityQueue를 이용해 탐색 - 많이 언급된 Keyword에게 우선순위를 줌.
     * 최대 25개의 키워드만 반환하기 때문에 Time Complexity 가벼움.
     *
     */
    public List<Pair<String, Integer>> getKeywordByBFS(String keywordStarted) {

        if (!isKeywordExist(keywordStarted)) {
            return null;
        }

        List<Pair<String, Integer>> ret = new ArrayList<>();

        boolean[] checked = new boolean[mCount];
        PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(1,
                new Comparator<Pair<Integer, Integer>>(){
                    @Override
                    public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                        return o2.second.compareTo(o1.second);
                    }
                });

        int startIdx = mKeyMap.get(keywordStarted);

        checked[startIdx] = true;
        pq.offer(new Pair<>(startIdx, mCountMap.get(keywordStarted)));

        int cnt = 0;
        while (cnt < 25 && pq.size() != 0) {
            Pair<Integer, Integer> now = pq.poll();

            if (now.second == 0) {
                continue;
            }

            cnt++;
            ret.add(new Pair<>(mIndexMap.get(now.first), now.second));

            ArrayList<Boolean> path = isConnected.get(now.first);

            int len = path.size();

            for (int i = 0; i < len; i++) {
                if (path.get(i) && !checked[i]) {
                    checked[i] = true;
                    pq.offer(new Pair<>(i, mCountMap.get(mIndexMap.get(i))));
                }
            }
        }

        return ret;
    }

    public Pair<Integer, Integer> getMinMaxCount(List<Pair<String, Integer>> keywordList) {
        int min = -1, max = -1;

        for(Pair<String, Integer> keywordInfo : keywordList) {

            if (min == -1 || max == -1) {
                if (min == -1) {
                    min = keywordInfo.second;
                }
                if (max == -1) {
                    max = keywordInfo.second;
                }
            }

            if (keywordInfo.second > max) {
                max = keywordInfo.second;
            } else if (keywordInfo.second < min) {
                min = keywordInfo.second;
            }
        }

        return new Pair<>(min, max);
    }

    public void updateCountMap(KeywordItem item) {
        String keywordName = item.getName();

        mCountMap.put(keywordName, mCountMap.get(keywordName) -1);
    }

}
