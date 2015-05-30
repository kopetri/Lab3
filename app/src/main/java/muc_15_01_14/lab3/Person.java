package muc_15_01_14.lab3;

import android.graphics.Color;

/**
 * Created by David on 26.05.2015.
 */
public class Person {

    private String user;
    private int orientation;
    private long age;
    private int color;

    public Person(String user, int orientation, long age) {
        this.user = user;
        this.orientation = orientation;
        this.age = age;
        this.color =-1;
    }

    public String getUser() {
        return user;
    }

    public int getOrientation() {
        return orientation;
    }

    public long getAge() {
        return age;
    }

    public void setColor(int c) {
        this.color = c;
    }

    public int getColor() {
        if (color == -1) {
            return R.color.p_unknown;
        } else {
            return color;
        }
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

}
