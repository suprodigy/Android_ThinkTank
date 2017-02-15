package com.boostcamp.jr.thinktank.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jr on 2017-02-15.
 */

public class ResponseFromNaver {

    @SerializedName("display")
    private int count;

    @SerializedName("items")
    List<Item> items;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private class Item {

        @SerializedName("title")
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

    }
}
