package it.polimi.ingsw.PSP034.view.GUI;

import it.polimi.ingsw.PSP034.client.Client;
import it.polimi.ingsw.PSP034.client.RequestManager;
import it.polimi.ingsw.PSP034.constants.PlayerColor;
import it.polimi.ingsw.PSP034.constants.Sex;
import it.polimi.ingsw.PSP034.messages.Request;
import it.polimi.ingsw.PSP034.messages.SlimBoard;
import it.polimi.ingsw.PSP034.messages.clientConfiguration.AnswerIP;
import it.polimi.ingsw.PSP034.messages.clientConfiguration.ErrorMessage;
import it.polimi.ingsw.PSP034.messages.clientConfiguration.RequestIP;
import it.polimi.ingsw.PSP034.messages.serverConfiguration.RequestNameColor;
import it.polimi.ingsw.PSP034.messages.serverConfiguration.RequestServerConfig;
import it.polimi.ingsw.PSP034.messages.setupPhase.*;
import it.polimi.ingsw.PSP034.model.Player;
import it.polimi.ingsw.PSP034.view.CLI.scenes.playPhase.Table;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;


public class GUIRequestHub extends RequestManager {
    private GUIController currentController;
    private static GUIRequestHub instance;


    private GUIRequestHub(){
    }

    public static GUIRequestHub getInstance(){
        if (instance == null){
            instance = new GUIRequestHub();
        }
        return instance;
    }

    public void setCurrentController(GUIController currentController) {
        this.currentController = currentController;
    }

    public GUIController getCurrentController() {
        return currentController;
    }

    private void craftRequestIP(RequestIP request){
        Platform.runLater(()->{ScenePath.setNextScene(currentController.getPane().getScene(), ScenePath.SERVER_LOGIN);});
    }

    private void craftRequestServer(RequestServerConfig request){
        Scene scene = currentController.getPane().getScene();
        switch (request.getInfo()){

            case REQUEST_NAME_COLOR:
                RequestNameColor requestNameColor = (RequestNameColor) request;
                Platform.runLater(()->{
                    ScenePath.setNextScene(scene, ScenePath.NAME_COLOR);
                    ((NameColorController) currentController).update(requestNameColor.getAlreadyChosenNames(), requestNameColor.getAvailableColors());
                });
                break;

            case REQUEST_PLAYER_NUMBER:
                Platform.runLater(()->{ScenePath.setNextScene(scene, ScenePath.NUMBER_OF_PLAYERS);});
                break;

            case LOBBY:
                Platform.runLater(()->{
                    ScenePath.setNextScene(scene, ScenePath.WAITING);
                    ((WaitingController) currentController).setMyTitle("You are in the Lobby now");
                    ((WaitingController) currentController).setMyMessage("Wait for the other players to connect...");
                });
                break;

            case WELCOME_WAIT:
                Platform.runLater(()->{
                    ScenePath.setNextScene(scene, ScenePath.WAITING);
                    ((WaitingController) currentController).setMyTitle("Welcome in a new game");
                    ((WaitingController) currentController).setMyMessage("Wait for the other player to finish registration...");
                    });
                break;

            case SUCCESSFULLY_ADDED:
                Platform.runLater(()->{
                    ScenePath.setNextScene(scene, ScenePath.WAITING);
                    ((WaitingController) currentController).setMyTitle("Successfully Added!");
                    ((WaitingController) currentController).setMyMessage("You have been successfully added to the game. Wait!");
                });
                break;

            case CARDS_CHOICE_WAIT:
                Platform.runLater(()->{
                    ScenePath.setNextScene(scene, ScenePath.WAITING);
                    ((WaitingController) currentController).setMyTitle("Your choice has been registered.");
                    ((WaitingController) currentController).setMyMessage("Wait for the other players to make their choice");
                });
                break;

            case ALREADY_STARTED:
                Platform.runLater(()->{ScenePath.setDialog((Stage)scene.getWindow(),"Game Started",
                        "Oops, seems like the game has already started without you, sorry! :(");});
        }
    }

    private void craftRequestSetup(SetupRequest request){
        Scene scene = currentController.getPane().getScene();
        if (request instanceof GodLikeInfo){
            GUIController previousController = currentController;
            String godLikePlayer = ((GodLikeInfo) request).getGodLikePlayer();
            Platform.runLater(()->
            {
                ScenePath.setDialog((Stage)scene.getWindow(),"God like Player",
                    godLikePlayer + " has been chosen as the most god-like player. Such a lucky player!");
                currentController = previousController;
            });
        }

        else if(request instanceof RequestCardsChoice) {
            RequestCardsChoice requestCardsChoice = (RequestCardsChoice) request;
            Platform.runLater(()->{
                ScenePath.setNextScene(scene, ScenePath.CHOOSE_GODS);
                ((ChooseGodsController) currentController).update(requestCardsChoice.getPlayerNumber());
            });
        }

        else if (request instanceof RequestPersonalGod) {
            RequestPersonalGod requestPersonalGod = (RequestPersonalGod) request;
            Platform.runLater(()->{
                ScenePath.setNextScene(scene, ScenePath.PERSONAL_GOD);
                ((PersonalGodController) currentController).update(requestPersonalGod.getPossibleGods(), requestPersonalGod.getAlreadyChosenGods());
            });
        }

        else if (request instanceof RequestFirstPlayer) {
            RequestFirstPlayer requestFP = (RequestFirstPlayer) request;
            Platform.runLater(()->{
                ScenePath.setNextScene(scene, ScenePath.FIRST_PLAYER);
                ((FirstPlayerController) currentController).update(requestFP.getPlayers());
            });
        }

        /*else if (request instanceof FirstPlayerInfo){
            String firstPlayer = ((FirstPlayerInfo) request).getFirstPlayer();
            GUIController previousController = currentController;
            Platform.runLater(()->{
                ScenePath.setDialog((Stage)scene.getWindow(), "First Player",
                        firstPlayer + " has been chosen as first player! Is this a good strategy? We'll see...");
                currentController = previousController;
            });
        }*/

        else if (request instanceof RequestPlaceWorker) {
            Platform.runLater(()->{
                String workerSex = ((RequestPlaceWorker) request).getSex() == Sex.MALE ? "male" : "female";
                ((TableController) currentController).setMyTitle("Your turn");
                ((TableController) currentController).setMyMessage("Place your " + workerSex +  " worker.");
                ((TableController) currentController).updatePlaceWorker(((RequestPlaceWorker) request).getSex(), PlayerColor.BLUE, ((RequestPlaceWorker) request).getSlimBoard().getSex());
            });
        }

        else if (request instanceof  ReceivedWorkerChoice){
            Platform.runLater(()->{

            });
        }

        else if(request instanceof InitializeBoard){
            Platform.runLater(()->{
                SlimBoard slim = ((InitializeBoard) request).getSlimBoard();
                ScenePath.setNextScene(scene, ScenePath.TABLE);
                //((TableController) currentController).disableBoard();
                ((TableController) currentController).updateCards(slim.getGodsList(), slim.getPlayersList(), slim.getColorsList());
                ((TableController) currentController).updateAndSaveBoard(slim);
            });
        }

        else if (request instanceof InfoIsPlacing){
            Platform.runLater(()->{
                //((TableController) currentController).disableBoard();
                ((TableController) currentController).setMyTitle(((InfoIsPlacing) request).getPlayer() + "'s turn");
                ((TableController) currentController).setMyMessage(((InfoIsPlacing) request).getPlayer() + " is placing workers.");
            });
        }

        //TODO -- decidere se va bene
    }

    //TODO -- unificare con cli?
    void createConnection(AnswerIP answer){
        new Thread(() -> {
            Client client = new Client(this, answer.getIp(), answer.getPort());
            client.startConnection(); // TODO -- assicurarsi che la connessione sia stata creata
            Thread runClient = new Thread(client);
            runClient.start();
        }
        ).start();
    }

    @Override
    public void handleRequest(Request message) {
        if (message instanceof RequestIP)
            craftRequestIP((RequestIP) message);
        else if (message instanceof RequestServerConfig)
            craftRequestServer((RequestServerConfig) message);
        else if (message instanceof SetupRequest)
            craftRequestSetup((SetupRequest) message);
        else if (message instanceof SlimBoard){
            Platform.runLater(()->{
                ((TableController) currentController).updateAndSaveBoard((SlimBoard) message);
            });
        }
    }

    @Override
    public void showError(ErrorMessage error) {
        //TODO
    }
}
