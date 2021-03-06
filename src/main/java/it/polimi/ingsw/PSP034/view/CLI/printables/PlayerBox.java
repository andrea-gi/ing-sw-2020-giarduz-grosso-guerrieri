package it.polimi.ingsw.PSP034.view.CLI.printables;

import it.polimi.ingsw.PSP034.constants.PlayerColor;
import it.polimi.ingsw.PSP034.view.GodDescription;

import java.util.ArrayList;

/**
 * Represents a card that contains information about a player.
 */
public class PlayerBox extends PrintableObject{
    private final String playerName;
    private final String godName;
    private String godPower;
    private final PlayerColor color;

    /**
     * Creates the card. The color of the player is used to color the border of the card.
     * @param playerName Name of the player.
     * @param godName God the player is playing with. This name will be used to retrieve the description of the power of the god.
     * @param color Color of the player.
     */
    public PlayerBox(String playerName, String godName, PlayerColor color){
        super();
        this.playerName = playerName;
        this.godName = godName;
        this.godPower = GodDescription.getPower(godName);
        this.color = color;
        ArrayList<String> constructionArray = new ArrayList<>();

        String BG_color = this.color != null? color.getBG_color() : ANSI.reset;
        constructionArray.add(BG_color+"╔══════════════════════════════════════╗"+ANSI.reset);
        constructionArray.add(BG_color+"║"+ANSI.reset+"                                      "+BG_color+"║"+ANSI.reset+"\033["+((40+playerName.length())/2)+"D"+playerName);
        constructionArray.add(BG_color+"╠══════════════════════════════════════╣"+ANSI.reset);
        constructionArray.add(BG_color+"║"+ANSI.reset+"                                      "+BG_color+"║"+ANSI.reset+"\033["+((40+godName.length())/2)+"D"+godName);
        constructionArray.add(BG_color+"╟"+ANSI.reset+"──────────────────────────────────────"+BG_color+"╢"+ANSI.reset);
        int start = 0;
        int end = 36;
        while(end < godPower.length()){
            while(godPower.charAt(end) != ' '){
                end--;
            }
            constructionArray.add(BG_color+"║"+ANSI.reset+"                                      "+BG_color+"║"+ANSI.reset+"\033[38D"+godPower.substring(start, end));
            start = end+1;
            end = start + 36;
        }
        if(start <= godPower.length()){
            constructionArray.add(BG_color+"║"+ANSI.reset+"                                      "+BG_color+"║"+ANSI.reset+"\033[38D"+godPower.substring(start));
        }
        constructionArray.add(BG_color+"╚══════════════════════════════════════╝"+ANSI.reset);
        super.setObjectSize(constructionArray.size());
        for(int i = 0; i< constructionArray.size(); i++){
            super.setObjectLine(i, constructionArray.get(i));
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGodName() {
        return godName;
    }

    public PlayerColor getColor() {
        return color;
    }
}
