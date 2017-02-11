package com.boostcamp.jr.thinktank.model;

import android.util.Log;

import com.boostcamp.jr.thinktank.manager.KeywordManager;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

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

    private ThinkObserver() {
        mRealm = Realm.getDefaultInstance();
    }

    public void insert(final ThinkItem item) {
        mRealm.executeTransaction(new Realm.Transaction() {
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
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(item);
            }
        });

        KeywordManager.get().updateKeywordRelation(item);
    }

    public void delete(ThinkItem item) {

        final ThinkItem ItemToDelete = mRealm.where(ThinkItem.class)
                .equalTo("id", item.getId()).findFirst();
        //Log.d(TAG, "" + item.getId());

        if(ItemToDelete != null) {
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    ItemToDelete.deleteFromRealm();
                }
            });
        } else {
            Log.d(TAG, "Item not found");
        }
    }

    public OrderedRealmCollection<ThinkItem> selectAll() {
        return mRealm.where(ThinkItem.class).findAllAsync();
    }

    public ThinkItem getCopiedObject(ThinkItem src) {
        return mRealm.copyFromRealm(src);
    }

}
