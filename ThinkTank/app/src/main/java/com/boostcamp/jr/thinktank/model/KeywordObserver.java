package com.boostcamp.jr.thinktank.model;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by jr on 2017-02-11.
 */

public class KeywordObserver {

    private static final String TAG = "KeywordObserver";

    private static KeywordObserver sKeywordObserver;

    public static KeywordObserver get() {
        if (sKeywordObserver == null) {
            sKeywordObserver = new KeywordObserver();
        }
        return sKeywordObserver;
    }

    private KeywordObserver() {}

    // auto_increment 적용된 id값 자동 반환
    public int getKeyId() {
        Realm realm = Realm.getDefaultInstance();

        Number currentMaxId = realm.where(KeywordItem.class).max("id");

        int nextId;
        if(currentMaxId == null) {
            nextId = 0;
        } else {
            nextId = currentMaxId.intValue() + 1;
        }

        return nextId;
    }

    public void insert(final KeywordItem item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(KeywordItem.class, item.getId())
                        .setName(item.getName())
                        .setRelation(item.getRelation())
                        .setCount(1);
            }
        });

    }

    public void update(final KeywordItem item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
    }

    public KeywordItem getKeywordByName(String name) {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(KeywordItem.class).equalTo("name", name).findFirst();
    }

    public OrderedRealmCollection<KeywordItem> selectAllOrderById() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(KeywordItem.class).findAllSorted("id");
    }

    public OrderedRealmCollection<KeywordItem> selectAllOrderByName() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(KeywordItem.class).findAllSorted("name");
    }

    public List<String> getMostUsedKeyword() {
        List<String> ret = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();

        OrderedRealmCollection<KeywordItem> keywordItems
                = realm.where(KeywordItem.class).findAllSorted("count", Sort.DESCENDING);

        int i = 0;
        for (KeywordItem keywordItem : keywordItems) {
            if (i >= 5) break;
            ret.add(keywordItem.getName());
            i++;
        }

        return ret;
    }

    public KeywordItem getCopiedObject(KeywordItem src) {
        Realm realm = Realm.getDefaultInstance();
        return realm.copyFromRealm(src);
    }

    // 가장 많이 언급된 KeywordItem의 이름을 반환
    public String getKeywordNameThatHasMaxCount() {
        Realm realm = Realm.getDefaultInstance();

        if (realm.where(KeywordItem.class).findAll().size() != 0) {
            KeywordItem keywordItem =
                    realm.where(KeywordItem.class).findAllSorted("count", Sort.DESCENDING).first();

            return keywordItem.getName();
        } else {
            return null;
        }

    }

    public List<String> getAllKeywordNames() {
        List<String> ret = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();

        OrderedRealmCollection<KeywordItem> keywordItems = selectAllOrderById();

        for (KeywordItem item : keywordItems) {
            ret.add(item.getName());
        }

        return ret;
    }

}
