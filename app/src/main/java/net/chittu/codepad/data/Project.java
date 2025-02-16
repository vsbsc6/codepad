package net.chittu.codepad.data;


public class Project{
    private String title;
    private String location;
    private final static String type = GENERIC;




    public String getTitle(){
        return title;
    }

    public String getType(){
        return "GENERIC";
    }

    public boolean canRun(){
        return false;
    }

    public void run(){
        throw UnsupportedAction("This project cannot run.")
    }
}