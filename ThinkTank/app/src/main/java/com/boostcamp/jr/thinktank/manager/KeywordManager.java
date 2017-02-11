package com.boostcamp.jr.thinktank.manager;

import android.util.SparseArray;

import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.utils.TypeCastingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;

/**
 * Created by jr on 2017-02-11.
 */

public class KeywordManager {

    private static KeywordManager sAnalyzer;

    private Map<String, Integer> mKeyMap = new HashMap<>();
    private SparseArray<String> mIndexMap = new SparseArray<>();
    private ArrayList<ArrayList<Boolean>> isConnected = new ArrayList<>();
    private int indexCount;

    private KeywordManager() { init(); }

    public static KeywordManager get() {
        if (sAnalyzer == null) {
            sAnalyzer = new KeywordManager();
        }
        return sAnalyzer;
    }

    private void init() {
        OrderedRealmCollection<KeywordItem> results = KeywordObserver.get().selectAll();
        indexCount = results.size();

        int i = 0;
        for(KeywordItem keyword :  results) {
            mKeyMap.put(keyword.getName(), i);
            mIndexMap.put(i, keyword.getName());
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
        } else {
            insertIntoMap(name, indexCount);
            indexCount++;
            KeywordObserver.get().insert(new KeywordItem()
                    .setName(name)
                    .setRelation(new byte[indexCount/8]));
        }
    }

    private void insertIntoMap(String name, int indexCount) {
        mKeyMap.put(name, indexCount);
        mIndexMap.put(indexCount, name);
    }

    public void updateKeywordRelation(ThinkItem thinkItem) {
        RealmList<KeywordItem> keywords = thinkItem.getKeywords();

        for (int i=0; i<keywords.size(); i++) {
            KeywordItem item = KeywordObserver.get().getCopiedObject(keywords.get(i));
            int itemIdx = mKeyMap.get(item.getName());

            // relation update
            byte[] relation = item.getRelation();
            for (int j=0; j<keywords.size()-1; j++) {
                // 해당 키워드의 relation update
                String opponentName = keywords.get(j).getName();
                int idx = mKeyMap.get(opponentName);
                relation = updateByteArray(relation, idx);

                // 반대 키워드의 relation update
                String name = mIndexMap.get(idx);
                KeywordItem opponent = KeywordObserver.get().getKeywordByName(name);
                opponent = KeywordObserver.get().getCopiedObject(opponent);
                byte[] opponentRelation = opponent.getRelation();
                opponentRelation = updateByteArray(opponentRelation, itemIdx);
                opponent.setRelation(opponentRelation);
                KeywordObserver.get().update(opponent);

                // isConnected 업데이트
                connect(itemIdx, idx);
            }

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
}
