package it.polimi.ingsw.PSP034.messages.playPhase;

import it.polimi.ingsw.PSP034.constants.Directions;
import it.polimi.ingsw.PSP034.constants.Sex;
import it.polimi.ingsw.PSP034.constants.TurnPhase;
import it.polimi.ingsw.PSP034.model.GodsRules;
import it.polimi.ingsw.PSP034.model.Player;
import it.polimi.ingsw.PSP034.model.Tile;
import it.polimi.ingsw.PSP034.model.Worker;

import java.util.ArrayList;

public class RequestAction extends PlayRequest {

    private final Directions[] possibleMaleDirections;
    private final Directions[] possibleFemaleDirections;

    public RequestAction(NextStateInfo info, Player player){
        super(info);
        Worker male = player.getWorker(Sex.MALE);
        Worker female = player.getWorker(Sex.FEMALE);

        Tile maleStartTile = male.getMyTile();
        Tile femaleStartTile = female.getMyTile();

        ArrayList<Tile> malePossibleTiles;
        ArrayList<Tile> femalePossibleTiles;

        if (info.getNextPhase() == TurnPhase.MOVE) {
            malePossibleTiles = GodsRules.availableMovingTiles(male);
            femalePossibleTiles = GodsRules.availableMovingTiles(female);
        }
        else if (info.getNextPhase() == TurnPhase.BUILD) {
            malePossibleTiles = GodsRules.availableBuildingTiles(male);
            femalePossibleTiles = GodsRules.availableBuildingTiles(female);
        }
        else
            throw new IllegalArgumentException("Action must be MOVE or BUILD");

        this.possibleMaleDirections = new Directions[malePossibleTiles.size()];
        this.possibleFemaleDirections = new Directions[femalePossibleTiles.size()];

        for(int i = 0; i < malePossibleTiles.size(); i++){
            this.possibleMaleDirections[i] = maleStartTile.directionCalculator(malePossibleTiles.get(i));
        }
        for(int i = 0; i < femalePossibleTiles.size(); i++){
            this.possibleFemaleDirections[i] = femaleStartTile.directionCalculator(femalePossibleTiles.get(i));
        }

    }



    public Directions[] getPossibleFemaleDirections() {
        return possibleFemaleDirections;
    }

    public Directions[] getPossibleMaleDirections() {
        return possibleMaleDirections;
    }

    @Override
    public TurnPhase getNextPhase() {
        return super.getNextPhase();
    }

    @Override
    public RequiredActions[] getRequiredActions(){
        return super.getRequiredActions();
    }
}