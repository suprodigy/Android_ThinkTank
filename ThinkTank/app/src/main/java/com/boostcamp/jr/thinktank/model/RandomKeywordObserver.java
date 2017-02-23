package com.boostcamp.jr.thinktank.model;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

/**
 * Created by jr on 2017-02-23.
 */

public class RandomKeywordObserver {

    private static RandomKeywordObserver sObserver;

    public static RandomKeywordObserver get() {
        if (sObserver == null) {
            sObserver = new RandomKeywordObserver();
        }
        return sObserver;
    }

    private RandomKeywordObserver(){}

    public void insert(final RandomKeyword item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(RandomKeyword.class, item.getId())
                        .setName(item.getName())
                        .setCount(1);
            }
        });
    }

    public void update(final RandomKeyword item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
    }

    public void createOrUpdate(String keywordName) {
        Realm realm = Realm.getDefaultInstance();

        RandomKeyword keyword = realm.where(RandomKeyword.class).equalTo("name", keywordName).findFirst();

        if (keyword == null) {
            insert(new RandomKeyword().setName(keywordName));
        } else {
            update(keyword.setCount(keyword.getCount() + 1));
        }
    }

    public RandomKeyword getKeywordByName(String name) {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(RandomKeyword.class).equalTo("name", name).findFirst();
    }

    public OrderedRealmCollection<RandomKeyword> selectAllOrderByName() {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(RandomKeyword.class)
                .notEqualTo("count", 0)
                .findAllSorted("name");
    }

    public RandomKeyword getCopiedObject(RandomKeyword src) {
        Realm realm = Realm.getDefaultInstance();
        return realm.copyFromRealm(src);
    }

    public List<String> getAllKeywordNames() {
        List<String> ret = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();

        OrderedRealmCollection<RandomKeyword> keywordItems = selectAllOrderByName();

        for (RandomKeyword item : keywordItems) {
            ret.add(item.getName());
        }

        return ret;
    }
}
