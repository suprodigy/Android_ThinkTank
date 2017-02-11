package com.boostcamp.jr.thinktank.model;

/**
 * Created by jr on 2017-02-12.
 */

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by jr on 2017-02-09.
 */

public class ThinkItem extends RealmObject {

    @PrimaryKey
    private String id;
    private String content;
    private RealmList<KeywordItem> keywords;

    public ThinkItem() {
        this(UUID.randomUUID());
    }

    private ThinkItem(UUID id) {
        this.id = id.toString();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public ThinkItem setContent(String content) {
        this.content = content;
        return this;
    }

    public RealmList<KeywordItem> getKeywords() {
        return keywords;
    }

    public ThinkItem setKeywords(RealmList<KeywordItem> keywords) {
        this.keywords = keywords;
        return this;
    }

}
