package com.boostcamp.jr.thinktank.model;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by jr on 2017-02-23.
 */

public class RandomThink extends RealmObject {

    @PrimaryKey
    private String id;
    private String content;
    private RealmList<RandomKeyword> keywords;
    private Date createdDate;

    public RandomThink() { this(UUID.randomUUID()); }

    private RandomThink(UUID id) {
        this.id = id.toString();
        this.createdDate = new Date();
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public RandomThink setContent(String content) {
        this.content = content;
        return this;
    }

    public RealmList<RandomKeyword> getKeywords() {
        return keywords;
    }

    public RandomThink setKeywords(RealmList<RandomKeyword> keywords) {
        this.keywords = keywords;
        return this;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

}
