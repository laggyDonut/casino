package de.edvschuleplattling.irgendwieanders.model;

public class Transaction {
    private int id;
    private Useraccount useraccount;
    private Game game;
    private String type;  //muss später noch eine ENUM werden
    private int amount;
    private String status; //muss später ENUM werden
}
