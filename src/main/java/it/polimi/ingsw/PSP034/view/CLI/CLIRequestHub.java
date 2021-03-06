package it.polimi.ingsw.PSP034.view.CLI;

import it.polimi.ingsw.PSP034.constants.Directions;
import it.polimi.ingsw.PSP034.constants.Sex;
import it.polimi.ingsw.PSP034.messages.Answer;
import it.polimi.ingsw.PSP034.messages.Request;
import it.polimi.ingsw.PSP034.messages.SlimBoard;
import it.polimi.ingsw.PSP034.messages.clientConfiguration.*;
import it.polimi.ingsw.PSP034.messages.gameOverPhase.*;
import it.polimi.ingsw.PSP034.messages.playPhase.*;
import it.polimi.ingsw.PSP034.messages.serverConfiguration.RequestNameColor;
import it.polimi.ingsw.PSP034.messages.serverConfiguration.RequestServerConfig;
import it.polimi.ingsw.PSP034.messages.setupPhase.*;
import it.polimi.ingsw.PSP034.view.CLI.scenes.*;
import it.polimi.ingsw.PSP034.view.CLI.scenes.clientConfiguration.ServerAddress;
import it.polimi.ingsw.PSP034.view.CLI.scenes.clientConfiguration.ServerPort;
import it.polimi.ingsw.PSP034.view.CLI.scenes.clientConfiguration.TitleScene;
import it.polimi.ingsw.PSP034.view.CLI.scenes.playPhase.Table;
import it.polimi.ingsw.PSP034.view.CLI.scenes.serverConfiguration.*;
import it.polimi.ingsw.PSP034.view.CLI.scenes.setupPhase.*;


/**
 * This class handles al the messages that the client receives. For every message the correct scene is initialized with the parameters extracted from the message.
 */
public class CLIRequestHub {
    private Scene currScene;
    private AnswerComposer answerComposer;

    /**
     * Initializes a RequestHub with no current scene nor answer composer associated.
     */
    public CLIRequestHub(){
        currScene = null;
        answerComposer = null;
    }


    /**
     * Handles the request message creating or updating the correct scene and return the Answer message with the user input to be sent to the Server.
     * @param request Request to be handled.
     * @return Answer to be sent to the server.
     */
    public Answer newRequest(Request request){
        if(request instanceof RequestClientConfig)
            return newRequestClientConfig((RequestClientConfig) request);

        else if(request instanceof RequestServerConfig)
            return newRequestServerConfig((RequestServerConfig) request);

        else if(request instanceof SetupRequest)
            return newSetupRequest((SetupRequest) request);

        else if(request instanceof PlayRequest)
            return newPlayRequest((PlayRequest) request);

        else if(request instanceof GameOverRequest)
            return newGameOverRequest((GameOverRequest) request);

        else if(request instanceof SlimBoard){
            SlimBoard slimBoard = (SlimBoard) request;
            ((Table) currScene).updateBoard(slimBoard.getDome(), slimBoard.getBuilding(), slimBoard.getColor(), slimBoard.getSex(), false);
            currScene.show();
            return null;
        }

        else{
            return null;
        }
    }

    private Answer newRequestClientConfig(RequestClientConfig request){
        if (request instanceof TitleRequest) {
            currScene = new TitleScene();
            currScene.show();
            return null;
        }

        else if (request instanceof RequestIP){
            String[] answers = new String[2];
            currScene = new ServerAddress(((RequestIP) request).getError());
            answers[0] = currScene.show();
            currScene = new ServerPort();
            answers[1] = currScene.show();
            answerComposer = new AnswerComposer(request);
            return answerComposer.packetAnswer(answers);
        }

        else if(request instanceof ErrorMessage){
            currScene.printError(((ErrorMessage) request).getCode(), ((ErrorMessage) request).getDescription());
            return null;
        }
        return null;
    }

    private Answer newRequestServerConfig(RequestServerConfig request){
        switch (request.getInfo()){
            case REQUEST_NAME_COLOR:
                RequestNameColor castRequest = (RequestNameColor) request;
                currScene = new NameChoice(castRequest.getAlreadyChosenNames());
                String[] answers = new String[2];
                answers[0] = currScene.show();
                currScene = new ColorChoice(castRequest.getAvailableColors());
                answers[1] = currScene.show();
                answerComposer = new AnswerComposer(castRequest);
                return answerComposer.packetAnswer(answers);

            case REQUEST_PLAYER_NUMBER:
                currScene = new PlayerNumber();
                String answer = currScene.show();
                answerComposer = new AnswerComposer(request);
                return answerComposer.packetAnswer(answer);

            case LOBBY:
                currScene = new Lobby();
                currScene.show();
                return null;

            case WELCOME_WAIT:
                currScene = new WelcomeWait();
                currScene.show();
                return null;

            case SUCCESSFULLY_ADDED:
                currScene = new SuccessfullyAdded();
                currScene.show();
                return null;

            case CARDS_CHOICE_WAIT:
                currScene = new CardsChoiceWait();
                currScene.show();
                return null;

            case ALREADY_STARTED:
                currScene = new AlreadyStarted();
                currScene.show();
                return new AutoCloseAnswer();
        }
        return null;
    }

    private Answer newSetupRequest(SetupRequest request){
        String answer;
        if (request instanceof  GodLikeInfo){
            currScene = new GodLikeChosen(((GodLikeInfo) request).getGodLikePlayer());
            currScene.show();
            return null;
        }

        else if(request instanceof RequestCardsChoice) {
            currScene = new CardsChoice(((RequestCardsChoice) request).getPlayerNumber());
            answer = currScene.show();
            answerComposer = new AnswerComposer(request);
            return answerComposer.packetAnswer(answer);
        }

        else if (request instanceof RequestFirstPlayer) {
            currScene = new FirstPlayer(((RequestFirstPlayer) request).getPlayers());
            answer = currScene.show();
            answerComposer = new AnswerComposer(request);
            return answerComposer.packetAnswer(answer);
        }

        else if (request instanceof FirstPlayerInfo){
            currScene = new FirstPlayerChosen(((FirstPlayerInfo) request).getFirstPlayer());
            currScene.show();
            return null;
        }

        else if (request instanceof RequestPersonalGod) {
            RequestPersonalGod castRequest = (RequestPersonalGod) request;
            currScene = new PersonalGodChoice(castRequest.getPossibleGods(), castRequest.getAlreadyChosenGods());
            answer = currScene.show();
            answerComposer = new AnswerComposer(request);
            return answerComposer.packetAnswer(answer);
        }

        else if (request instanceof RequestPlaceWorker) {
            SlimBoard slimBoard = ((RequestPlaceWorker) request).getSlimBoard();
            ((Table) currScene).updatePlaceWorker(slimBoard.getDome(), slimBoard.getBuilding(), slimBoard.getColor(), slimBoard.getSex(), ((RequestPlaceWorker) request).getSex());
            answer = currScene.show();
            answerComposer = new AnswerComposer(request);
            return answerComposer.packetAnswer(answer);
        }

        else if (request instanceof  ReceivedWorkerChoice){
            ((Table) currScene).updateClearRequest();
            currScene.show();
            return null;
        }

        else if(request instanceof InitializeBoard){
            SlimBoard slimBoard = ((InitializeBoard) request).getSlimBoard();
            currScene = new Table(slimBoard.getGodsList(), slimBoard.getPlayersList(), slimBoard.getColorsList());
            currScene.show();
            return null;
        }

        else if(request instanceof InfoIsPlacing){
            ((Table) currScene).updateOtherPlacing(((InfoIsPlacing) request).getPlayer());
            currScene.show();
            return null;
        }
        return null;
    }

    private Answer newPlayRequest(PlayRequest request){
        String answer;
        String[] answers =  new String[2];
        if(request instanceof RequestAction){
            RequiredActions[] actions = request.getRequiredActions();
            Sex requiredSex = null;
            int workerX;
            int workerY;
            boolean hasChoice = false;
            for (int actionIndex = 0; actionIndex < actions.length; actionIndex++) {
                switch (actions[actionIndex]) {
                    case REQUEST_WORKER:
                        String nextAction = actions[actionIndex+1] == RequiredActions.REQUEST_MOVE ? "move" : "build";
                        ((Table) currScene).updateSelectWorker(nextAction);
                        answers[0] = currScene.show();
                        requiredSex = answers[0].equals("1") ? Sex.MALE : Sex.FEMALE;
                        hasChoice = true;
                        break;
                    case REQUIRED_MALE:
                        requiredSex = Sex.MALE;
                        answers[0] = "1";
                        hasChoice = false;
                        break;
                    case REQUIRED_FEMALE:
                        requiredSex = Sex.FEMALE;
                        answers[0] = "2";
                        hasChoice = false;
                        break;
                    case REQUEST_MOVE:
                        Directions[] moveDirections = requiredSex == Sex.MALE ? ((RequestAction) request).getPossibleMaleDirections() : ((RequestAction) request).getPossibleFemaleDirections();
                        workerX = requiredSex == Sex.MALE ? ((RequestAction) request).getXMale() : ((RequestAction) request).getXFemale();
                        workerY = requiredSex == Sex.MALE ? ((RequestAction) request).getYMale() : ((RequestAction) request).getYFemale();
                        if (requiredSex == null){
                            throw new NullPointerException("No Worker was selected nor imposed.");
                        }
                        ((Table) currScene).updateMove(requiredSex, workerX, workerY, moveDirections, hasChoice);
                        answers[1] = currScene.show();
                        if(Integer.parseInt(answers[1]) > moveDirections.length) {
                            actionIndex = actionIndex - 2;
                            break;
                        }else {
                            answerComposer = new AnswerComposer(request);
                            return answerComposer.packetAnswer(answers);
                        }
                    case REQUEST_BUILD:
                        Directions[] buildDirections = requiredSex == Sex.MALE ? ((RequestAction) request).getPossibleMaleDirections() : ((RequestAction) request).getPossibleFemaleDirections();
                        workerX = requiredSex == Sex.MALE ? ((RequestAction) request).getXMale() : ((RequestAction) request).getXFemale();
                        workerY = requiredSex == Sex.MALE ? ((RequestAction) request).getYMale() : ((RequestAction) request).getYFemale();
                        if (requiredSex == null){
                            throw new NullPointerException("No Worker was selected nor imposed.");
                        }
                        ((Table) currScene).updateBuild(requiredSex, workerX, workerY, buildDirections, hasChoice);
                        answers[1] = currScene.show();
                        if(Integer.parseInt(answers[1]) > buildDirections.length) {
                            actionIndex = actionIndex - 2;
                            break;
                        }else {
                            answerComposer = new AnswerComposer(request);
                            return answerComposer.packetAnswer(answers);
                        }
                }
            }
        }

        else if(request instanceof RequestBooleanChoice){
            RequiredActions[] actions = request.getRequiredActions();
            for(RequiredActions action : actions){
                if (action == RequiredActions.REQUEST_POWER) {
                    ((Table) currScene).updatePower();
                    answer = currScene.show();
                    answerComposer = new AnswerComposer(request);
                    return answerComposer.packetAnswer(answer);
                }
            }
        }

        else if(request instanceof RequestStart){
            ((Table) currScene).updateStart();
            currScene.show();
            return new AnswerStart();
        }

        else if(request instanceof InfoIsStarting){
            ((Table) currScene).updateOtherStarting(((InfoIsStarting) request).getPlayer());
            currScene.show();
            return null;
        }

        else if(request instanceof RequestEnd){
            ((Table) currScene).updateEnd();
            currScene.show();
            return null;
        }
        return null;
    }

    private Answer newGameOverRequest(GameOverRequest request){
        String answer;
        if(request instanceof PersonalDefeatRequest){
            ((Table) currScene).updateDefeat(((PersonalDefeatRequest) request).getWinner(), ((PersonalDefeatRequest) request).getLosers());
            answer = currScene.show();
            answerComposer = new AnswerComposer(request);
            Answer answerMessage = answerComposer.packetAnswer(answer);
            if(answerMessage instanceof AutoCloseAnswer){
                currScene.moveToEndScreen();
            }
            return answerMessage;
        }

        else if(request instanceof SingleLoserInfo) {
            ((Table) currScene).updateRemovePlayer(((SingleLoserInfo) request).getLoser());
            currScene.show();
            return null;
        }

        else if(request instanceof WinnerRequest){
            ((Table) currScene).updateWin(((WinnerRequest) request).getWinner());
            answer = currScene.show();
            answerComposer = new AnswerComposer(request);
            Answer answerMessage = answerComposer.packetAnswer(answer);
            if(answerMessage instanceof AutoCloseAnswer){
                currScene.moveToEndScreen();
            }
            return answerMessage;

        }

        else if(request instanceof EndByDisconnection){
            currScene.printError("S001", ((EndByDisconnection) request).getDisconnectedPlayer() + " has disconnected. Game has ended.");
            return new AutoCloseAnswer();
        }

        else{
            return null;
        }

    }
}
