package it.polimi.ingsw.PSP034.view.scenes.playPhase;

import it.polimi.ingsw.PSP034.constants.Color;
import it.polimi.ingsw.PSP034.constants.Constant;
import it.polimi.ingsw.PSP034.constants.Directions;
import it.polimi.ingsw.PSP034.constants.Sex;
import it.polimi.ingsw.PSP034.view.printables.*;
import it.polimi.ingsw.PSP034.view.printables.arrangements.HorizontalArrangement;
import it.polimi.ingsw.PSP034.view.printables.arrangements.VerticalArrangement;
import it.polimi.ingsw.PSP034.view.printables.godcards.GodCard;
import it.polimi.ingsw.PSP034.view.scenes.Scene;

import java.util.ArrayList;

public class Table extends Scene{
    VerticalArrangement all;

    Font title;
    HorizontalArrangement boardANDRight;

    ViewBoard board;
    VerticalArrangement rightSide;

    HorizontalArrangement alignedCards;
    VerticalArrangement request;

    GodCard[] cards;
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


    public Table(String[] gods){
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

        cards = new GodCard[gods.length];
        for(int i = 0; i < gods.length; i++){
            cards[i] = new GodCard(gods[i]);
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

    public void updateBoard(boolean[][] dome, int[][] building, Color[][] color, Sex[][] sex, boolean showNumbers, String currentPlayer){
        if(currentPlayer != null) {
            int pos = all.getObjects().indexOf(title);
            all.removeObjects(title);
            title = new Font(currentPlayer + "'s turn");
            all.insertObject(pos, title);
        }

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

    public void updatePlaceWorker(boolean[][] dome, int[][] building, Color[][] color, Sex[][] sex, Sex worker){
        int pos = all.getObjects().indexOf(title);
        all.removeObjects(title);
        title = new Font("workers setup");
        all.insertObject(pos, title);
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

        pos = request.getObjects().indexOf(question);
        request.removeObjects(question);
        question = new Dialog("Select the tiles your worker will start from:", -1,5, options);
        question.setVisible(true);
        request.insertObject(pos, question);
        emptyRequest.setVisible(false);
        message.setVisible(false);

        pos = textBoxANDAnswer.getObjects().indexOf(answer);
        textBoxANDAnswer.removeObjects(answer);
        answer = new Message( worker.name() + " Worker :", -1);
        textBoxANDAnswer.insertObject(pos, answer);

        pos = textBoxANDAnswer.getObjects().indexOf(textBox);
        textBoxANDAnswer.removeObjects(textBox);
        textBox = new TextBox(question.getWidth()-answer.getWidth());
        textBoxANDAnswer.insertObject(pos, textBox);
        textBoxANDAnswer.setVisible(true);

        requiredAnswer = true;

        regex = new ArrayList<>();
        String rule;
        if(freeTiles <= 9)
            rule = "^[1-" + freeTiles + "]$";
        else{
            rule = "^([1-9]";
            int x;
            for(x = 19; x < freeTiles; x+=10){
                rule += "|" + x/10 + "[0-9]";
            }
            rule += "|" + x/10 + "[0-" + freeTiles%10 + "])$";
        }

        regex.add(new RegexCondition(rule, "Invalid selection."));
    }

    public void updateSelectWorker() {
        int pos = request.getObjects().indexOf(question);
        request.removeObjects(question);
        question = new Dialog("Select which Worker you want to move:", -1, 1, "Male (" + Sex.MALE.toString() + ")", "Female (" + Sex.FEMALE.toString() + ")");
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

    public void updateMove(Sex sex, Directions[] possibleDirections, boolean hasChoice){
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

        int pos = request.getObjects().indexOf(question);
        request.removeObjects(question);
        question = new Dialog("Where do you want to move your " + sex.name().toLowerCase() + " (" + sex.toString() + ") Worker?", -1, 3, options);
        question.setVisible(true);
        request.insertObject(pos, question);
        emptyRequest.setVisible(false);
        message.setVisible(false);

        pos = textBoxANDAnswer.getObjects().indexOf(answer);
        textBoxANDAnswer.removeObjects(answer);
        answer = new Message("Your move :", -1);
        textBoxANDAnswer.insertObject(pos, answer);

        pos = textBoxANDAnswer.getObjects().indexOf(textBox);
        textBoxANDAnswer.removeObjects(textBox);
        textBox = new TextBox(question.getWidth()-answer.getWidth());
        textBoxANDAnswer.insertObject(pos, textBox);
        textBoxANDAnswer.setVisible(true);

        requiredAnswer = true;

        regex = new ArrayList<>();
        regex.add(new RegexCondition("^[1-" + (possibleDirections.length+1) + "]$", "Invalid selection."));
    }

    public void updateBuild(Sex sex, Directions[] possibleDirections, boolean hasChoice){
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

        int pos = request.getObjects().indexOf(question);
        request.removeObjects(question);
        question = new Dialog("Where do you want to build with your " + sex.name().toLowerCase() + " (" + sex.toString() + ") Worker?", -1, 3, options);
        question.setVisible(true);
        request.insertObject(pos, question);
        emptyRequest.setVisible(false);
        message.setVisible(false);

        pos = textBoxANDAnswer.getObjects().indexOf(answer);
        textBoxANDAnswer.removeObjects(answer);
        answer = new Message("Your build :", -1);
        textBoxANDAnswer.insertObject(pos, answer);

        pos = textBoxANDAnswer.getObjects().indexOf(textBox);
        textBoxANDAnswer.removeObjects(textBox);
        textBox = new TextBox(question.getWidth()-answer.getWidth());
        textBoxANDAnswer.insertObject(pos, textBox);
        textBoxANDAnswer.setVisible(true);

        requiredAnswer = true;

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
        int pos = all.getObjects().indexOf(title);
        all.removeObjects(title);
        title = new Font("your turn");
        all.insertObject(pos, title);

        updateClearRequest();
    }

    public void updateClearRequest(){
        emptyRequest.setVisible(true);
        message.setVisible(false);
        question.setVisible(false);

        textBoxANDAnswer.setVisible(false);

        requiredAnswer = false;
    }
}
