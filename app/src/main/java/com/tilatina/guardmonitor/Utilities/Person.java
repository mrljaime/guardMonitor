package com.tilatina.guardmonitor.Utilities;

/**
 * Created by jaime on 19/04/16.
 */
public class Person {
    String title;

    public Person(){
    }

    public Person(String title) {
        this.title = title;
    }

    public Person setTile(String title) {
        this.title = title;

        return this;
    }

    public String getTitle(){
        if (title != null) {
            return title;
        } else {
            return null;
        }
    }
}
