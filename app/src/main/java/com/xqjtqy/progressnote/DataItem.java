package com.xqjtqy.progressnote;

public class DataItem {
    private String title;
    private String body;
    private String date;

    public DataItem(String title, String body, String date){
        this.title=title;
        this.body=body;
        this.date=date;
    }

    public String getTitle() {
        return title;
    }

    public String getBody(){
        return body;
    }

    public String getDate(){
        return date;
    }

}