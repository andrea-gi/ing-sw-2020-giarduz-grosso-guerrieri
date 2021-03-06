package it.polimi.ingsw.PSP034.view.CLI.scenes.serverConfiguration;

import it.polimi.ingsw.PSP034.constants.PlayerColor;
import it.polimi.ingsw.PSP034.view.CLI.printables.*;
import it.polimi.ingsw.PSP034.view.CLI.printables.arrangements.HorizontalArrangement;
import it.polimi.ingsw.PSP034.view.CLI.printables.arrangements.VerticalArrangement;
import it.polimi.ingsw.PSP034.view.CLI.scenes.Scene;

/**
 * This class creates the scene to be printed when the user has to choose the color he/she wants to play with.
 */
public class ColorChoice extends Scene {
    private final VerticalArrangement va1;

    private final Font title;
    private final VerticalArrangement va2;

    private final PlayerColor[] availableColors;
    private final Dialog selectColor;
    private final HorizontalArrangement ha1;
    private final Message color;
    private final TextBox colorPicker;

    /**
     * Creates the scene and organizes the objects.
     * @param availableColors The colors that the user can choose.
     */
    public ColorChoice(PlayerColor[] availableColors){
        va1 = new VerticalArrangement();
        va1.setCentreAlignment();
        va1.setBorder(1);

        title = new Font("Color choice");
        va2 = new VerticalArrangement();
        va2.setLeftAlignment();
        va2.setBorder(1);
        va1.addObjects(title, va2);

        this.availableColors = availableColors;
        String[] options = new String[availableColors.length];
        for(int i = 0; i < availableColors.length; i++){
            options[i] = availableColors[i].getBG_color()+availableColors[i].name()+ANSI.reset;
        }
        selectColor = new Dialog("Select the color you want to play with:", title.getWidth(), 1, options);
        ha1 = new HorizontalArrangement();
        ha1.setBottomAlignment();
        ha1.setBorder(1);
        va2.addObjects(selectColor, ha1);

        color = new Message("Your choice :", -1);
        colorPicker = new TextBox(selectColor.getWidth()-color.getWidth()-ha1.getBorder());
        ha1.addObjects(color, colorPicker);
    }

    /**
     * {@inheritDoc}
     * @return The position of chosen color in the array. The user input is 1-based.
     */
    @Override
    public String show(){
        super.clearFrame();

        super.printMain(va1);

        return colorPicker.waitAnswer(new RegexCondition("^[1-" + availableColors.length + "]$", "Invalid input."));
    }
}
