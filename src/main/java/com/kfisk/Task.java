/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kfisk;

/**
 *
 * @author kotteletfisk
 */
public class Task {

    public String title;
    public boolean isCompleted;

    // Default constructor which Jackson needs to deserialize JSON
    public Task(){}

    public Task(String title, boolean isCompleted) {
        this.title = title;
        this.isCompleted = isCompleted;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof Task)) {
            return false;
        }

        var t = (Task) o;
        return t.title.equals(this.title) && t.isCompleted == this.isCompleted;
    }
}
