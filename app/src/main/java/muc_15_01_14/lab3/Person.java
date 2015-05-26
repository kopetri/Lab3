package muc_15_01_14.lab3;

/**
 * Created by David on 26.05.2015.
 */
public class Person {

    private String user;
    private int orientation;
    private long age;

    public Person(String user, int orientation, long age){
        this.user = user;
        this.orientation = orientation;
        this.age = age;
    }

    public String getUser(){
        return user;
    }

    public int getOrientation(){
        return orientation;
    }

    public long getAge(){
        return age;
    }


}
