package muc_15_01_14.lab3;

/**
 * Created by David on 26.05.2015.
 */
public class Person {

    private String user;
    private int orientation;

    public Person(String user, int orientation){
        this.user = user;
        this.orientation = orientation;
    }

    public String getUser(){
        return user;
    }

    public int getOrientation(){
        return orientation;
    }
}
