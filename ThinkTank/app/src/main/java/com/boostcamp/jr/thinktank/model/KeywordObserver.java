package com.boostcamp.jr.thinktank.model;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

/**
 * Created by jr on 2017-02-11.
 */

public class KeywordObserver {

    private static final String TAG = "KeywordObserver";

    private static KeywordObserver sKeywordObserver;

    private Realm mRealm;

    public static KeywordObserver get() {
        if (sKeywordObserver == null) {
            sKeywordObserver = new KeywordObserver();
        }
        return sKeywordObserver;
    }

    private KeywordObserver() {
        mRealm = Realm.getDefaultInstance();
    }

    // auto_increment 적용된 id값 자동 반환
    public int getKeyId() {
        Number currentMaxId = mRealm.where(KeywordItem.class).max("id");

        int nextId;
        if(currentMaxId == null) {
            nextId = 0;
        } else {
            nextId = currentMaxId.intValue() + 1;
        }

        return nextId;
    }

    public void insert(final KeywordItem item) {
        mRealm.executeTransaction(new Realm.Transaction() {
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
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
    }

    public KeywordItem getKeywordByName(String name) {
        return mRealm.where(KeywordItem.class).equalTo("name", name).findFirst();
    }

    public OrderedRealmCollection<KeywordItem> selectAll() {
        return mRealm.where(KeywordItem.class).findAllSorted("id");
    }

    public KeywordItem getCopiedObject(KeywordItem src) {
        return mRealm.copyFromRealm(src);
    }

}
