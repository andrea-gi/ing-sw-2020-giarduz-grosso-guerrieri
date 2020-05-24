package it.polimi.ingsw.PSP034.view.GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.File;

public class ServerLoginController implements GUIController{
    File file = new File("src\\main\\resources\\images\\santorini.jpg");
    Image image = new Image(file.toURI().toString());

    @FXML
    private Pane pane;
    @FXML
    private ImageView santoriniLogo;
    @FXML
    private TextField enterServerName;
    @FXML
    private TextField enterServerPort;
    @FXML
    private Button submit;

    private String serverName;
    private String serverPort;

    @Override
    public Pane getPane() {
        return pane;
    }

    public String getEnterServerName() {
        return enterServerName.getText();
    }

    public String getEnterServerPort() {
        return enterServerPort.getText();
    }

    @FXML
    private void initialize(){
        GUIRequestHub.getInstance().setCurrentController(this);
        santoriniLogo.setImage(image);
    }

    public void setSubmit(ActionEvent e){
        if (getEnterServerName().isEmpty())
            serverName = "localhost";
        else
            serverName = getEnterServerName();
        if (getEnterServerPort().isEmpty())
            serverPort = "2020";
        else
            serverPort = getEnterServerPort();
        submit.setDisable(true);
        submit.setText("SUBMITTED!");
        enterServerName.setDisable(true);
        enterServerPort.setDisable(true);
        //GUIRequestHub.getInstance().send(new AnswerIP(serverName,serverPort)); //TODO -- inviare
    }
}
