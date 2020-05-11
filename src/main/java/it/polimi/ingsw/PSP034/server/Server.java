package it.polimi.ingsw.PSP034.server;

import it.polimi.ingsw.PSP034.constants.Color;
import it.polimi.ingsw.PSP034.constants.Constant;
import it.polimi.ingsw.PSP034.controller.Controller;
import it.polimi.ingsw.PSP034.controller.IController;
import it.polimi.ingsw.PSP034.messages.Answer;
import it.polimi.ingsw.PSP034.messages.Request;
import it.polimi.ingsw.PSP034.messages.gameOverPhase.GameOverAnswer;
import it.polimi.ingsw.PSP034.messages.playPhase.PlayAnswer;
import it.polimi.ingsw.PSP034.messages.serverConfiguration.*;
import it.polimi.ingsw.PSP034.messages.setupPhase.SetupAnswer;
import it.polimi.ingsw.PSP034.view.printables.ANSI;
import it.polimi.ingsw.PSP034.view.scenes.serverConfiguration.AlreadyStarted;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class managing the server. Its main thread (started using {@link Server#run()}, creates a new thread
 * listening to incoming socket connections. Then, it manages the messages received by clients sequentially.
 */
public class Server implements Runnable{
    private static class AnswerEncapsulated{
        private final Answer message;
        private final IClientConnection connection;

        AnswerEncapsulated(Answer message, IClientConnection connection) {
            this.message = message;
            this.connection = connection;
        }
    }

    private ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newFixedThreadPool(12);

    private final ArrayList<IClientConnection> waitingConnections = new ArrayList<>();

    private final ArrayList<IClientConnection> activeConnections = new ArrayList<>();

    private final ArrayList<Color> chosenColors = new ArrayList<>();
    private final ArrayList<String> chosenNames = new ArrayList<>();

    private boolean gameStarted = false;
    private boolean gameEnded = false; // true if game ended correctly (with a winner)
    private boolean firstConnection = true;
    private boolean canStartSetup = false;
    private int chosenPlayerNumber = Constant.MAXPLAYERS;
    private int port;

    private final IController controller;

    private final BlockingQueue<AnswerEncapsulated> queue = new ArrayBlockingQueue<>(16);

    private final Object consoleLock = new Object();

    /**
     * Instantiates a new server listening to a given port. It also creates a new {@link IController}.
     * @param port A valid port (0 to 65535). For best results, be aware of TCP well-known ports.
     */
    public Server(int port){
        IController temporaryController;
        try {
            this.port = port;
            this.serverSocket = new ServerSocket(port);
            temporaryController = new Controller(this);
        } catch (IOException e){
            temporaryController = null;
            System.err.println("Cannot open server socket");
            System.exit(1);
        }
        controller = temporaryController;
    }

    boolean playerNumberFlag = true;
    /**
     * Sets the number of player synchronously. Can be done only once.
     * @param chosenPlayerNumber Player number chosen by the selected client (2 or 3).
     */
    protected synchronized boolean setPlayerNumber(int chosenPlayerNumber){
        if (playerNumberFlag) {
            playerNumberFlag = false;
            this.chosenPlayerNumber = chosenPlayerNumber;
            setCanStartSetup(true);
            checkAndBeginSetup();
            return true;
        }
        return false;
    }

    /**
     * Synchronously checks if player number has already been set and setup can start
     * @return {@code true} if setup can start, {@code false} otherwise.
     */
    protected synchronized boolean canStartSetup() {
        return canStartSetup;
    }

    private synchronized int getChosenPlayerNumber(){
        return chosenPlayerNumber;
    }

    private synchronized void setCanStartSetup(boolean canStartSetup){
        this.canStartSetup = canStartSetup;
    }

    private synchronized void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    /**
     * Synchronously checks if game has already started
     * @return {@code true} if game has started, {@code false} otherwise.
     */
    protected synchronized boolean isGameStarted(){
        return this.gameStarted;
    }

    /**
     * Checks if player number has already been set and, if there are enough players, starts the registration.
     */
    protected synchronized void checkAndBeginSetup(){
        if (isGameStarted()) {
            deregisterWaitingList();
            return; // in case the function is called more than once
        }

        if (waitingConnections.size() >= getChosenPlayerNumber() && canStartSetup()) {

            setGameStarted(true);

            activeConnections.addAll(waitingConnections.subList(0, chosenPlayerNumber));
            waitingConnections.removeAll(activeConnections);

            for(IClientConnection connection : activeConnections){
                if (connection.equals(activeConnections.get(0))) {
                    controller.addModelObserver(connection);
                    connection.asyncSend(new RequestNameColor(chosenNames.toArray(new String[0]),
                            Color.getRemainingColors(chosenColors.toArray(new Color[0]))));
                }
                else
                    connection.asyncSend(new RequestServerConfig(ServerInfo.WELCOME_WAIT));
            }

            deregisterWaitingList();
        }
    }

    /**
     * Registers a new connection to the waiting list.
     * @param connection Reference to the connection being registered.
     */
    protected synchronized void registerConnection(IClientConnection connection){
        waitingConnections.add(connection);
    }

    /**
     * Deregisters a connection previously registered using {@link Server#deregisterConnection(IClientConnection)}
     * @param connection Reference to the connection being deregistered.
     */
    protected synchronized void deregisterConnection(IClientConnection connection){
        if (waitingConnections.contains(connection)){
            if (isGameStarted()){
                connection.asyncSend(new RequestServerConfig(ServerInfo.ALREADY_STARTED));
                connection.closeConnection();
                waitingConnections.remove(connection);
            }
            else if (!canStartSetup() && waitingConnections.indexOf(connection) == 0){
                /*connection.closeConnection();*/
                waitingConnections.remove(connection);
                waitingConnections.get(0).asyncSend(new RequestServerConfig(ServerInfo.REQUEST_PLAYER_NUMBER));
            }
            else {
                /*connection.closeConnection();*/
                waitingConnections.remove(connection);
            }
        } else if (activeConnections.contains(connection)){
            // TODO
        }
    }

    private synchronized void deregisterWaitingList(){
        for(IClientConnection deregistered : waitingConnections) {
            // TODO -- invio messaggio
            deregisterConnection(deregistered);
        }
        waitingConnections.clear();
    }


    private void acceptConnections(){
        executor.execute(() -> {
            while (true) {
                try {
                    Socket newSocket = serverSocket.accept();
                    // TODO -- gestire connessioni in eccesso
                    ClientHandler socketConnection;
                    if (firstConnection) {
                        socketConnection = new ClientHandler(newSocket, this, true);
                        firstConnection = false;
                    } else {
                        socketConnection = new ClientHandler(newSocket, this, false);
                    }
                    executor.submit(socketConnection);
                    printInfoConsole("Added new player, temporary ID is: " + socketConnection.getName());
                } catch (IOException e) {
                    System.err.println("Connection Error!");
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    /**
     * Synchronously adds a message to the {@link BlockingQueue}
     * @param message Message being added
     * @param connection Connection that added the message
     */
    protected synchronized void addMessage(Answer message, IClientConnection connection){
        queue.offer(new AnswerEncapsulated(message, connection));
    }

    private synchronized boolean registerPlayer(IClientConnection connection, String name, Color color){
        printInfoConsole(connection.getName() + " wants to register as " + name + ", using "
                + connection.getDebugColor() + color + ANSI.reset +" workers.");
        if (!chosenNames.contains(name) && !chosenColors.contains(color)) {
            controller.addPlayer(name, color);
            chosenNames.add(name);
            chosenColors.add(color);
            connection.setName(name);
            connection.setDebugColor(color);
            printInfoConsole("Successfully registered "+name+", using "+color+" workers.");

            int indexPlayer = activeConnections.indexOf(connection);
            if (indexPlayer < getChosenPlayerNumber() - 1) {
                connection.asyncSend(new RequestServerConfig(ServerInfo.SUCCESSFULLY_ADDED));
                activeConnections.get(indexPlayer + 1).asyncSend(new RequestNameColor(chosenNames.toArray(new String[0]),
                        Color.getRemainingColors(chosenColors.toArray(new Color[0]))));
            } else {
                controller.handleGamePhase();
            }
            return true;
        }
        printInfoConsole("Either the name or color requested were not available.");
        return false;
    }

    /**
     * Manages a message received by a client. If the received message is a {@link AnswerServerConfig},
     * it will be managed directly in this method, otherwise it will call a controller method.
     * @param message Message to be managed
     * @param connection Message sender
     */
    private synchronized void manageMessage(Answer message, IClientConnection connection){
        printInfoConsole(ANSI.FG_bright_blue + "Received: " + ANSI.reset + message.getClass().getSimpleName()
                + " by: " + connection.getDebugColor() + connection.getName() + ANSI.reset);

        if (message instanceof AnswerServerConfig){
            manageServerConfig((AnswerServerConfig) message, connection);
        }
        else if (message instanceof PlayAnswer || message instanceof SetupAnswer || message instanceof GameOverAnswer){
            controller.getMessageManager().handleMessage(message, connection.getName());
        }
    }


    private synchronized void manageServerConfig(AnswerServerConfig message, IClientConnection connection){
        boolean validMessage;
        if (message instanceof AnswerNumber) {
            if(setPlayerNumber(((AnswerNumber) message).getPlayerNumber())){
                if (canStartSetup() && !isGameStarted()){
                    connection.asyncSend(new RequestServerConfig(ServerInfo.LOBBY)); //TODO -- messaggio di attesa altri giocatori
                }
            } else{
                // TODO -- gestire messaggio player number errato (?) vedi setPlayerNumber
            }
        }
        else if (message instanceof AnswerNameColor) {
            AnswerNameColor answerNameColor = (AnswerNameColor) message;
            validMessage = registerPlayer(connection, answerNameColor.getName(), answerNameColor.getColor());
            if (!validMessage){
                // TODO -- come gestisco l'errore? dovrei inviare una notifica di errore
                connection.asyncSend(new RequestNameColor(chosenNames.toArray(new String[0]),
                        Color.getRemainingColors(chosenColors.toArray(new Color[0]))));
            }
        }
    }

    /**
     * Sends a message to a given player. See {@link IClientConnection#asyncSend(Request)} for further information.
     * Should be executed by the same thread that manages the message.
     * If either the player does not exists or the message in {@code null}, method does nothing.
     * @param player Player name
     * @param message Message being sent
     */
    public synchronized void asyncSendTo(String player, Request message){
        if (message == null)
            return;
        for (IClientConnection connection : activeConnections){
            if(connection.getName().equals(player))
                connection.asyncSend(message);
        }
    }

    /**
     * Prints synchronously a debug info in the console.
     * @param string Info to be printed
     */
    protected void printInfoConsole(String string){
        synchronized (consoleLock){
            System.out.println(string);
        }
    }


    @Override
    public void run() {
        acceptConnections();
        printInfoConsole("Server started. Listening to socket connections on port: " + port);
        AnswerEncapsulated message;
        while(true){
            try {
                message = queue.take();
                manageMessage(message.message, message.connection);
            } catch (InterruptedException e){
                // TODO -- interrupted
            }
        }
    }
}
