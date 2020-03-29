package PSP034.model;

import java.util.ArrayList;

public class Game {
    private Board board;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private ArrayList<String> godsList;

    public Game(){
        players = new ArrayList<Player>();
        board = new Board();
        currentPlayer = null;
        // godsList ???
    }

    /**Returns the number of ???active??? players*/
    public int getPlayerNumber(){
        return players.size();
    }

    private void setCurrentPlayer(Player player){
        currentPlayer = player;
    }

    public Player getCurrentPlayer(){
        return currentPlayer;
    }

    public void addPlayer(Player player){ // exception ???
        players.add(player);
        if (players.size() == 1){
            setCurrentPlayer(player);
        }
    }

    /** Removes a deleted player from the list of players */
    public void removePlayer(Player player){ // exception ??
        if(player.getName().equals(currentPlayer.getName()))
            setNextPlayer();
        player.remove();
        players.remove(player);
    }

    public ArrayList<String> getGodsList(){
        return new ArrayList<>(godsList);
    }

    /** Sets the next player among ArrayList<Players> players, which is already in the right turn order.*/
    public void setNextPlayer(){
        int index = players.indexOf(currentPlayer);
        int nextIndex = ((index + 1) % players.size());
        setCurrentPlayer(players.get(nextIndex));
    }

    public Board getBoard(){
        return board;
    }
}
