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

    private Realm mRealm;

    public static ThinkObserver get() {
        if (sThinkObserver == null) {
            sThinkObserver = new ThinkObserver();
        }
        return sThinkObserver;
    }

    private ThinkObserver() {}

    public void insert(final ThinkItem item) {
        mRealm = Realm.getDefaultInstance();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObject(ThinkItem.class, item.getId())
                        .setContent(item.getContent())
                        .setKeywords(item.getKeywords());
            }
        });

        KeywordManager.get().updateKeywordRelation(item);

        mRealm.close();
    }

    public void update(final ThinkItem item) {
        mRealm.close();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });

        KeywordManager.get().updateKeywordRelation(item);

        mRealm.close();
    }

    public void delete(ThinkItem item) {
        mRealm = Realm.getDefaultInstance();

        final ThinkItem ItemToDelete = mRealm.where(ThinkItem.class)
                .equalTo("id", item.getId()).findFirst();

        KeywordObserver observer = KeywordObserver.get();
        RealmList<KeywordItem> list = ItemToDelete.getKeywords();
        for(KeywordItem keyword : list) {
            KeywordItem temp = observer.getCopiedObject(keyword);
            temp.setCount(temp.getCount()-1);
            observer.update(temp);
        }

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ItemToDelete.deleteFromRealm();
            }
        });

        mRealm.close();
    }

    public OrderedRealmCollection<ThinkItem> selectAll() {
        mRealm = Realm.getDefaultInstance();

        OrderedRealmCollection<ThinkItem> ret =
                mRealm.where(ThinkItem.class).findAllSorted("dateUpdated", Sort.DESCENDING);

        mRealm.close();
        return ret;
    }

    public ThinkItem getCopiedObject(ThinkItem src) {
        mRealm = Realm.getDefaultInstance();

        ThinkItem ret = mRealm.copyFromRealm(src);

        mRealm.close();
        return ret;
    }

}
