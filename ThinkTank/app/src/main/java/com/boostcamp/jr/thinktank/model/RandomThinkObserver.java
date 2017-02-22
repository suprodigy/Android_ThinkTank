package com.boostcamp.jr.thinktank.model;

import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.Sort;

/**
 * Created by jr on 2017-02-23.
 */

public class RandomThinkObserver {

    private static RandomThinkObserver sObserver;

    public static RandomThinkObserver get() {
        if (sObserver == null) {
            sObserver = new RandomThinkObserver();
        }
        return sObserver;
    }

    private RandomThinkObserver() {}

    public void insert(final RandomThink item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(RandomThink.class, item.getId())
                        .setContent(item.getContent())
                        .setKeywords(item.getKeywords());
            }
        });
    }

    public void update(final RandomThink item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });
    }

    public void delete(final RandomThink item) {
        Realm realm = Realm.getDefaultInstance();

        final RandomThink itemToDelete = realm.where(RandomThink.class)
                .equalTo("id", item.getId()).findFirst();

        RandomKeywordObserver keywordObserver = RandomKeywordObserver.get();
        RealmList<RandomKeyword> keywords = itemToDelete.getKeywords();
        for (RandomKeyword keyword : keywords) {
            RandomKeyword temp = keywordObserver.getCopiedObject(keyword);
            temp.setCount(temp.getCount()-1);
            keywordObserver.update(temp);
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                itemToDelete.deleteFromRealm();
            }
        });
    }

    public OrderedRealmCollection<RandomThink> selectAll() {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(RandomThink.class).findAllSorted("createdDate", Sort.DESCENDING);
    }

    public RandomThink getCopiedObject(RandomThink item) {
        Realm realm = Realm.getDefaultInstance();

        return realm.copyFromRealm(item);
    }

    public OrderedRealmCollection<RandomThink> selectThatHasId(String keyword) {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(RandomThink.class).equalTo("keywords.name", keyword)
                .findAllSorted("createdDate", Sort.DESCENDING);
    }

    public RandomThink selectItemThatHasId(String id) {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(RandomThink.class).equalTo("id", id).findFirst();
    }

    public OrderedRealmCollection<RandomThink> selectByDate(Date date) {
        Realm realm = Realm.getDefaultInstance();

        Date nextDay = new Date(date.getYear(), date.getMonth(), date.getDate()+1);

        return realm.where(RandomThink.class)
                .greaterThanOrEqualTo("createdDate", date)
                .lessThan("createdDate", nextDay)
                .findAllSorted("createdDate", Sort.DESCENDING);
    }

}
