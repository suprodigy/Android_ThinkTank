package com.boostcamp.jr.thinktank.model;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;

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

    private KeywordObserver() {}

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
        mRealm = Realm.getDefaultInstance();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(KeywordItem.class, item.getId())
                        .setName(item.getName())
                        .setRelation(item.getRelation())
                        .setCount(1);
            }
        });

        mRealm.close();
    }

    public void update(final KeywordItem item) {
        mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
        mRealm.close();
    }

    public KeywordItem getKeywordByName(String name) {
        mRealm = Realm.getDefaultInstance();
        KeywordItem ret = mRealm.where(KeywordItem.class).equalTo("name", name).findFirst();
        mRealm.close();
        return ret;
    }

    public OrderedRealmCollection<KeywordItem> selectAll() {
        mRealm = Realm.getDefaultInstance();
        OrderedRealmCollection<KeywordItem> ret = mRealm.where(KeywordItem.class).findAllSorted("id");
        mRealm.close();
        return ret;
    }

    public KeywordItem getCopiedObject(KeywordItem src) {
        mRealm = Realm.getDefaultInstance();
        KeywordItem ret = mRealm.copyFromRealm(src);
        mRealm.close();
        return ret;
    }

    // 가장 많이 언급된 KeywordItem의 이름을 반환
    public String getKeywordNameThatHasMaxCount() {
        mRealm = Realm.getDefaultInstance();

        if (mRealm.where(KeywordItem.class).findAll().size() != 0) {
            KeywordItem keywordItem =
                    mRealm.where(KeywordItem.class).findAllSorted("count", Sort.DESCENDING).first();

            mRealm.close();
            return keywordItem.getName();
        } else {
            mRealm.close();
            return null;
        }

    }

}
