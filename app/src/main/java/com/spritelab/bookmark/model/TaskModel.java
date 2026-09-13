package com.spritelab.bookmark.model;

import java.util.ArrayList;
import java.util.List;

public class TaskModel {
    private int id;
    private String title;
    private String date;
    private List<TaskPointModel> points;

    public TaskModel(int id, String title, String date, List<TaskPointModel> points) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.points = points != null ? points : new ArrayList<>();
    }

    public TaskModel(String title, String date, List<TaskPointModel> points) {
        this.title = title;
        this.date = date;
        this.points = points != null ? points : new ArrayList<>();
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

    public List<TaskPointModel> getPoints() {
        return points;
    }

    public void setPoints(List<TaskPointModel> points) {
        this.points = points;
    }
}
