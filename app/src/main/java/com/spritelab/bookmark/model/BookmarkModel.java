package com.spritelab.bookmark.model;

public class BookmarkModel {
    private int id;
    private String title;
    private String date;

    public BookmarkModel(int id, String title, String date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public BookmarkModel(String title, String date) {
        this.title = title;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
