package it.polimi.ingsw.PSP034.controller;

import it.polimi.ingsw.PSP034.constants.Color;
import it.polimi.ingsw.PSP034.messages.Answer;
import it.polimi.ingsw.PSP034.server.Server;

public interface IController {
    void setMessageManager(Server server);
    void addPlayer(String name, Color color);
    void handleGamePhase();
    void handleMessage(Answer message, String sender);
}
