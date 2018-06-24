package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HelpController {

    @FXML
    Button helpClose;

    @FXML
    private void onHelpClose() {
        Stage stage = (Stage) helpClose.getScene().getWindow();
        stage.close();
    }
}
