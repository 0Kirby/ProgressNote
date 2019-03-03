package com.xqjtqy.progressnote.noteData;

public class DataItem {
    private int id;
    private String title;
    private String body;
    private String date;

    public DataItem(String title, String body, String date) {
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public DataItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}