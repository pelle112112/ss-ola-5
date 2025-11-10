/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.kfisk;

import io.javalin.http.Context;

/**
 *
 * @author kotteletfisk
 */
public class RequestValidator {

    public Task validateBody(Context ctx) {
        return ctx.bodyValidator(Task.class)
                    .check(task -> task.title.length() > 0, "Title must be a least one character")
                    .get();
    }
}
