package muc_15_01_14.lab3;

/**
 * Created by Sebastian on 30.05.2015.
 */
public interface OnStreamUpdateListener {
    public void onUpdate(Person person);
    public void onEOF();
}
