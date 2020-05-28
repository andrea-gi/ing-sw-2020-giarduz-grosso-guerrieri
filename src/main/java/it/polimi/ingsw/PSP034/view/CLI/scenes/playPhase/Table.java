package it.polimi.ingsw.PSP034.view.CLI.scenes.playPhase;

import it.polimi.ingsw.PSP034.constants.PlayerColor;
import it.polimi.ingsw.PSP034.constants.Constant;
import it.polimi.ingsw.PSP034.constants.Directions;
import it.polimi.ingsw.PSP034.constants.Sex;
import it.polimi.ingsw.PSP034.view.CLI.printables.*;
import it.polimi.ingsw.PSP034.view.CLI.printables.arrangements.HorizontalArrangement;
import it.polimi.ingsw.PSP034.view.CLI.printables.arrangements.VerticalArrangement;
import it.polimi.ingsw.PSP034.view.CLI.scenes.Scene;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Table extends Scene{
    VerticalArrangement all;

    Font title;
    HorizontalArrangement boardANDRight;

    ViewBoard board;
    VerticalArrangement rightSide;

    HorizontalArrangement alignedCards;
    VerticalArrangement request;

    PlayerBox[] cards;
    Spacer spaceThirdCard;

    Dialog question;
    Message message;
    Spacer emptyRequest;
    HorizontalArrangement textBoxANDAnswer;

    Spacer answerIndentation;
    Message answer;
    TextBox textBox;
    ArrayList<RegexCondition> regex;
    boolean requiredAnswer;


    public Table(String[] gods, String[] players, PlayerColor[] colors, String currentPlayer){
        all = new VerticalArrangement();

        title = new Font("   ");
        boardANDRight = new HorizontalArrangement();
        all.addObjects(title, boardANDRight);
        all.setCentreAlignment();
        all.setBorder(1);

        board = new ViewBoard();
        rightSide = new VerticalArrangement();
        boardANDRight.addObjects(board, rightSide);
        boardANDRight.setTopAlignment();

        alignedCards = new HorizontalArrangement();
        request = new VerticalArrangement();
        rightSide.addObjects(alignedCards, request);
        rightSide.setCentreAlignment();
        rightSide.setBorder(1);

        cards = new PlayerBox[gods.length];
        for(int i = 0; i < gods.length; i++){
            cards[i] = new PlayerBox(players[i], gods[i],colors[i]);
        }
        spaceThirdCard = new Spacer(cards[0].getWidth()/2, 1);
        if(gods.length == 3)
            alignedCards.addObjects(cards);
        else
            alignedCards.addObjects(spaceThirdCard, cards[0], cards[1], spaceThirdCard);
        alignedCards.setTopAlignment();
        alignedCards.setBorder(1);

        message = new Message("", -1);
        message.setVisible(false);
        question = new Dialog("", -1,1, "");
        question.setVisible(false);
        emptyRequest = new Spacer(alignedCards.getWidth(), board.getHeight()-alignedCards.getHeight());
        textBoxANDAnswer = new HorizontalArrangement();
        textBoxANDAnswer.setVisible(false);
        request.addObjects(message, question, emptyRequest, textBoxANDAnswer);

        answerIndentation = new Spacer(5, 1);
        answer = new Message("", -1);
        textBox = new TextBox(1);
        textBoxANDAnswer.addObjects(answerIndentation, answer, textBox);
        textBoxANDAnswer.setBottomAlignment();

        boardANDRight.setBorder(super.getFrameWidth()-board.getWidth()-rightSide.getWidth());

        regex = null;

        requiredAnswer = false;
    }


    @Override
    public String show() {
        super.clearFrame();

        super.printMain(all);

        if(!requiredAnswer){
            return null;
        }else{
            if(regex == null){
                textBox.waitAnswer();
                return null;
            }else{
                return textBox.waitAnswer(regex.toArray(new RegexCondition[0]));
            }
        }
    }

    public void updateBoard(boolean[][] dome, int[][] building, PlayerColor[][] color, Sex[][] sex, boolean showNumbers, String currentPlayer){
        for(int y = 0; y < Constant.DIM; y++){
            for(int x = 0; x < Constant.DIM; x++){
                board.updateTile(x, y, building[x][y], dome[x][y], color[x][y], sex[x][y]);
            }
        }

        if(showNumbers)
            board.showNumbers();
        else
            board.hideNumbers();

        requiredAnswer = false;

    }

    public void updatePlaceWorker(boolean[][] dome, int[][] building, PlayerColor[][] color, Sex[][] sex, Sex worker){
        setTitle("workers setup");

        updateBoard(dome, building, color, sex, true, null);

        int freeTiles = 0;
        for(int y = 0; y < Constant.DIM; y++){
            for(int x = 0; x < Constant.DIM; x++){
                if(!dome[x][y] &&  color[x][y]==null)
                    freeTiles++;
            }
        }

        String[] options = new String[freeTiles];
        for (int n = 1; n<=freeTiles; n++){
            options[n-1] = Integer.toString(n);
        }


        setQuestion(new Dialog("Select the tiles your worker will start from:", -1,5, options));

        String workerSex = worker.name().substring(0,1).toUpperCase() + worker.name().substring(1).toLowerCase();
        setAnswer(new Message( workerSex + " Worker :" , -1));

        regex = new ArrayList<>();
        StringBuilder rule;
        if(freeTiles <= 9)
            rule = new StringBuilder("^[1-" + freeTiles + "]$");
        else{
            rule = new StringBuilder("^([1-9]");
            int x;
            for(x = 19; x < freeTiles; x+=10){
                rule.append("|").append(x / 10).append("[0-9]");
            }
            rule.append("|").append(x / 10).append("[0-").append(freeTiles % 10).append("])$");
        }


        regex.add(new RegexCondition(rule.toString(), "Invalid selection."));
    }

    public void updateOtherPlacing(String playerName){
        setTitle("Workers setup");
        setMessage(new Message(playerName + " is placing Workers", -1));
        textBoxANDAnswer.setVisible(false);
        requiredAnswer = false;
    }

    public void updateSelectWorker(String action) {
        setQuestion(new Dialog("Select which Worker you want to " + action + ":", -1, 1, "Male (" + Sex.MALE.toString() + ")", "Female (" + Sex.FEMALE.toString() + ")"));

        setAnswer(new Message("Your choice :", -1));

        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-2]$", "Invalid selection."));
    }

    public void updateMove(Sex sex, Directions[] possibleDirections, boolean hasChoice){
        String[] options = directionsToOptions(possibleDirections, hasChoice);

        setQuestion(new Dialog("Where do you want to move your " + sex.name().toLowerCase() + " (" + sex.toString() + ") Worker?", -1, 3, options));

        setAnswer(new Message("Your move :", -1));

        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-" + (possibleDirections.length+1) + "]$", "Invalid selection."));
    }

    public void updateBuild(Sex sex, Directions[] possibleDirections, boolean hasChoice){
        String[] options = directionsToOptions(possibleDirections, hasChoice);

        setQuestion(new Dialog("Where do you want to build with your " + sex.name().toLowerCase() + " (" + sex.toString() + ") Worker?", -1, 3, options));

        setAnswer(new Message("Your build :", -1));

        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-" + (possibleDirections.length+1) + "]$", "Invalid selection."));
    }

    public void updatePower(){
        int pos = request.getObjects().indexOf(question);
        request.removeObjects(question);
        question = new Dialog("Do you want to use your god's power?", -1, 1, "Yes", "No");
        question.setVisible(true);
        request.insertObject(pos, question);
        emptyRequest.setVisible(false);
        message.setVisible(false);

        pos = textBoxANDAnswer.getObjects().indexOf(answer);
        textBoxANDAnswer.removeObjects(answer);
        answer = new Message("Your choice :", -1);
        textBoxANDAnswer.insertObject(pos, answer);

        pos = textBoxANDAnswer.getObjects().indexOf(textBox);
        textBoxANDAnswer.removeObjects(textBox);
        textBox = new TextBox(question.getWidth()-answer.getWidth());
        textBoxANDAnswer.insertObject(pos, textBox);
        textBoxANDAnswer.setVisible(true);

        requiredAnswer = true;

        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-2]$", "Invalid selection."));
    }

    public void updateStart(){
        setTitle("your turn");

        setEmptyRequest();
    }

    public void updateOtherStarting(String playerName){
        setTitle(playerName + "'s turn");
        setEmptyRequest();
        requiredAnswer = false;
    }

    public void updateEnd(){
        setTitle("Your turn ended");

        setEmptyRequest();
    }

    public void updateDefeat(@NotNull String winnerName, @NotNull String[] losersNames) throws NullPointerException{
        if(winnerName.equals("")){
            PlayerColor loserColor = null;
            for(PlayerBox card : cards){
                if(card.getPlayerName().equals(losersNames[0])){
                    loserColor = card.getColor();
                    PlayerBox newCard = new PlayerBox(card.getPlayerName(), card.getGodName(), null);
                    changeCard(card, newCard);
                }
            }

            LoserDrawing loserDrawing;
            if(loserColor != null)
                loserDrawing = new LoserDrawing(loserColor);
            else
                throw new NullPointerException(losersNames[0] + "can't loose as there's no such player still playing");
            request.insertObject(request.getObjects().indexOf(message), loserDrawing);

            setMessage(new Message("Oh, no! You lost...", -1));
            setQuestion(new Dialog("Do you want to exit the game or keep watching?", -1, 1, "Exit", "Keep watching"));

        }else{
            PlayerColor[] loserColors = new PlayerColor[losersNames.length];
            PlayerColor winnerColor = null;
            for(PlayerBox card : cards){
                if(card.getPlayerName().equals(winnerName))
                    winnerColor = card.getColor();
                else{
                    for(int i = 0; i < losersNames.length; i++){
                        if(card.getPlayerName().equals(losersNames[i]))
                            loserColors[i] = card.getColor();
                    }
                }
            }
            OtherWinnerDrawing drawing;

            for(int i = 0; i < loserColors.length; i++){
                if(loserColors[i] == null)
                    throw new NullPointerException(losersNames[i] + "can't loose as there's no such player still playing");
            }
            if(winnerColor == null)
                throw new NullPointerException(winnerName + "can't win as there's no such player still playing");

            drawing = new OtherWinnerDrawing(winnerColor, loserColors);

            request.insertObject(request.getObjects().indexOf(message), drawing);
            setMessage(new Message("Oh, no! " + winnerName + " won...", -1));
            setQuestion(new Dialog("Do you want to exit the game or play again?", -1, 1, "Exit", "Play again"));

        }
        message.setVisible(true);
        setAnswer(new Message("Your choice", -1));
        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-2]$", "Invalid selection"));
    }

    public void updateRemovePlayer(String loser) throws NullPointerException{
        PlayerColor loserColor = null;
        for(PlayerBox card : cards){
            if(card.getPlayerName().equals(loser)){
                PlayerBox newCard = new PlayerBox(card.getPlayerName(), card.getGodName(), null);
                changeCard(card, newCard);
            }
        }
        if(loser == null)
            throw new NullPointerException(loser + " can't loose as there is no such player still playing");

        setMessage(new Message(loser + " lost! You are one step closer to victory!", -1));

        setAnswer(new Message("Enter to continue", -1));
        requiredAnswer = true;
        regex = null;
    }

    public void updateWin(String winnerName) throws NullPointerException{
        PlayerColor winnerColor = null;
        for(PlayerBox card : cards){
            if(card.getPlayerName().equals(winnerName)){
                winnerColor = card.getColor();
            }
        }
        if(winnerColor == null)
            throw new NullPointerException(winnerName + " can't win as there is no such player still playing");

        WinnerDrawing drawing = new WinnerDrawing(winnerColor);
        request.insertObject(request.getObjects().indexOf(message), drawing);
        setMessage(new Message("YAY! YOU WIN!", -1));
        setQuestion(new Dialog("Do you want to exit the game or play again?", -1, 1, "Exit", "Play again"));
        message.setVisible(true);

        setAnswer(new Message("Your choice", -1));
        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-2]$", "Invalid selection"));
    }


    public void updateClearRequest(){
        setEmptyRequest();
    }

    private void setTitle(String newTitle){
        int pos = all.getObjects().indexOf(title);
        all.removeObjects(title);
        title = new Font(newTitle);
        all.insertObject(pos, title);
    }

    private void setQuestion(Dialog newQuestion){
        int pos = request.getObjects().indexOf(question);
        request.removeObjects(question);
        question = newQuestion;
        question.setVisible(true);
        request.insertObject(pos, question);
        emptyRequest.setVisible(false);
        message.setVisible(false);
    }

    private void setMessage(Message newMessage){
        int pos = request.getObjects().indexOf(message);
        request.removeObjects(message);
        message = newMessage;
        message.setVisible(true);
        request.insertObject(pos, message);
        emptyRequest.setVisible(false);
        question.setVisible(false);
    }

    private void setEmptyRequest(){
        emptyRequest.setVisible(true);
        message.setVisible(false);
        question.setVisible(false);

        textBoxANDAnswer.setVisible(false);

        requiredAnswer = false;
    }

    private void setAnswer(Message newAnswer){
        int pos = textBoxANDAnswer.getObjects().indexOf(answer);
        textBoxANDAnswer.removeObjects(answer);
        answer = newAnswer;
        textBoxANDAnswer.insertObject(pos, answer);

        pos = textBoxANDAnswer.getObjects().indexOf(textBox);
        textBoxANDAnswer.removeObjects(textBox);
        textBox = new TextBox(question.getWidth()-answerIndentation.getWidth()-answer.getWidth());
        textBoxANDAnswer.insertObject(pos, textBox);
        textBoxANDAnswer.setVisible(true);

        requiredAnswer = true;
    }

    private String[] directionsToOptions(Directions[] possibleDirections, boolean hasChoice) {
        String[] options;
        if(hasChoice) {
            options = new String[possibleDirections.length + 1];
            for (int i = 0; i < possibleDirections.length; i++) {
                options[i] = possibleDirections[i].name();
            }
            options[options.length - 1] = "Back";
        }else{
            options = new String[possibleDirections.length];
            for (int i = 0; i < possibleDirections.length; i++) {
                options[i] = possibleDirections[i].name();
            }
        }
        return options;
    }

    private void changeCard(PlayerBox oldCard, PlayerBox newCard){
        int pos = alignedCards.getObjects().indexOf(oldCard);
        alignedCards.removeObjects(oldCard);
        oldCard = newCard;
        alignedCards.insertObject(pos, oldCard);
    }
}
