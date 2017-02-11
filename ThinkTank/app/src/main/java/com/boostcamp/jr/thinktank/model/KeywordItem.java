package com.boostcamp.jr.thinktank.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by jr on 2017-02-09.
 */

public class KeywordItem extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private int count;
    private byte[] relation;        // 해당 비트를 id로 가지고 있는 키워드들과 연결되어 있는지를 0, 1로 표현

    public KeywordItem() {
        this(KeywordObserver.get().getKeyId());
    }

    private KeywordItem(int id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public KeywordItem setName(String name) {
        this.name = name;
        return this;
    }

    public int getCount() {
        return count;
    }

    public KeywordItem setCount(int count) {
        this.count = count;
        return this;
    }

    public byte[] getRelation() {
        return relation;
    }

    public KeywordItem setRelation(byte[] relation) {
        this.relation = relation;
        return this;
    }

}
