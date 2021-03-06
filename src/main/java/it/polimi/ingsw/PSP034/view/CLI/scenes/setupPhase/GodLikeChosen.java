package it.polimi.ingsw.PSP034.view.CLI.scenes.setupPhase;

import it.polimi.ingsw.PSP034.view.CLI.printables.Font;
import it.polimi.ingsw.PSP034.view.CLI.printables.Message;
import it.polimi.ingsw.PSP034.view.CLI.printables.arrangements.VerticalArrangement;
import it.polimi.ingsw.PSP034.view.CLI.scenes.Scene;

/**
 * This class creates the scene to be printed to communicate to the user which player has been chosen as most godlike player.
 */
public class GodLikeChosen extends Scene {
    private final String godLikePlayer;

    private final VerticalArrangement va1;

    private final Font title;
    private final Message godlikeMessage;

    /**
     * Creates the scene and organizes the objects.
     *
     * @param player God-like player name.
     */
    public GodLikeChosen(String player){
        this.godLikePlayer = player;

        va1 = new VerticalArrangement();
        va1.setCentreAlignment();
        va1.setBorder(1);

        title = new Font("god like player");
        godlikeMessage = new Message(godLikePlayer + " has been chosen as the most god-like player! Such a lucky player, but do not worry, you will have better luck next time!", title.getWidth());
        va1.addObjects(title, godlikeMessage);
    }

    /**
     * {@inheritDoc}
     * @return {@code null}
     */
    @Override
    public String show() {
        super.clearFrame();

        super.printMain(va1);

        return null;
    }
}
