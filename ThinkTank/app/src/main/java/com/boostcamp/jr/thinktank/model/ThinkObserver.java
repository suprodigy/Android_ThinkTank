package com.boostcamp.jr.thinktank.model;

import com.boostcamp.jr.thinktank.manager.KeywordManager;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.Sort;

/**
 * Created by jr on 2017-02-09.
 */

public class ThinkObserver {

    private static final String TAG = "ThinkObserver";

    private static ThinkObserver sThinkObserver;

    public static ThinkObserver get() {
        if (sThinkObserver == null) {
            sThinkObserver = new ThinkObserver();
        }
        return sThinkObserver;
    }

    private ThinkObserver() {}

    public void insert(final ThinkItem item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(ThinkItem.class, item.getId())
                        .setContent(item.getContent())
                        .setKeywords(item.getKeywords());
            }
        });

        KeywordManager.get().updateKeywordRelation(item);

    }

    public void update(final ThinkItem item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });

        KeywordManager.get().updateKeywordRelation(item);

    }

    public void delete(ThinkItem item) {
        Realm realm = Realm.getDefaultInstance();

        final ThinkItem ItemToDelete = realm.where(ThinkItem.class)
                .equalTo("id", item.getId()).findFirst();

        KeywordObserver observer = KeywordObserver.get();
        RealmList<KeywordItem> list = ItemToDelete.getKeywords();
        for(KeywordItem keyword : list) {
            KeywordItem temp = observer.getCopiedObject(keyword);
            temp.setCount(temp.getCount()-1);
            observer.update(temp);
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ItemToDelete.deleteFromRealm();
            }
        });

    }

    public OrderedRealmCollection<ThinkItem> selectAll() {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(ThinkItem.class).findAllSorted("dateUpdated", Sort.DESCENDING);
    }

    public ThinkItem getCopiedObject(ThinkItem src) {
        Realm realm = Realm.getDefaultInstance();

        return realm.copyFromRealm(src);
    }

}
