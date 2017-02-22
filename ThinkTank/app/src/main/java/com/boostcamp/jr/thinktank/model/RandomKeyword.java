package com.boostcamp.jr.thinktank.model;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by jr on 2017-02-23.
 */

public class RandomKeyword extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private int count;

    public RandomKeyword() {
        UUID id = UUID.randomUUID();
        this.id = id.toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RandomKeyword setName(String name) {
        this.name = name;
        return this;
    }

    public int getCount() {
        return count;
    }

    public RandomKeyword setCount(int count) {
        this.count = count;
        return this;
    }
}
