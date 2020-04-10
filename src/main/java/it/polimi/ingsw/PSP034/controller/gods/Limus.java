package it.polimi.ingsw.PSP034.controller.gods;

import it.polimi.ingsw.PSP034.constants.Sex;
import it.polimi.ingsw.PSP034.constants.TurnPhase;
import it.polimi.ingsw.PSP034.controller.GodsRules;
import it.polimi.ingsw.PSP034.controller.IRules;
import it.polimi.ingsw.PSP034.model.Player;
import it.polimi.ingsw.PSP034.model.Tile;
import it.polimi.ingsw.PSP034.model.Worker;

public class Limus extends GodsRules {
    public Limus(IRules decoratedRules, Player player){
        super(decoratedRules, player);
    }

    @Override
    public TurnPhase nextState(TurnPhase currentPhase) {
        return super.nextState(currentPhase);
    }

    @Override
    public Boolean executeState(TurnPhase currentPhase, Worker worker, Tile tile, Boolean choice) {
        return super.executeState(currentPhase, worker, tile, choice);
    }

    @Override
    public boolean validMove(Worker worker, Tile destinationTile) {
        return super.validMove(worker, destinationTile);
    }

    @Override
    public boolean validBuild(Worker worker, Tile buildingTile) {
        if(getPlayer().isOwner(worker)){
            return super.validBuild(worker, buildingTile);
        }else{
            if(buildingTile.getBuilding() <= 2) {
                if (buildingTile.isNeighbouringTile(getPlayer().getWorker(Sex.MALE).getMyTile()))
                    return false;
                if (buildingTile.isNeighbouringTile(getPlayer().getWorker(Sex.FEMALE).getMyTile()))
                    return false;
            }
            return validBuildRecursive(worker, buildingTile);
        }
    }
}