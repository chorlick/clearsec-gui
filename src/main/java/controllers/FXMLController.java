package controllers;

import Tasks.GetListOfPdfsTask;
import Tasks.GetListOfYearsTask;
import entities.Case;
import entities.Year;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.OsdInterface;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FXMLController implements Initializable {

    OsdInterface osdInterface = new OsdInterface();

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private WebView webView;

    @FXML
    private TreeView osdTree;

    @FXML
    private TreeItem root = new TreeItem("Years");

    @FXML
    SplitPane mainSplitPane;

    @FXML
    TextField statusBar;

    private Map<Integer, Year> yearOsdItems = new HashMap<>();
    private Map<Year, TreeItem> treeYears = new HashMap<>();
    private Map<String, Case> treeCase = new HashMap<>();
    ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        osdTree.setRoot(root);
        GetListOfYearsTask yearsTask = new GetListOfYearsTask();
        Future<Map<Integer, Year>> years = service.submit(yearsTask);

        osdTree.setCellFactory(tree -> {
            TreeCell<String> cell = new TreeCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    TreeItem<String> treeItem = cell.getTreeItem();
                    try {
                        if (treeItem.getValue().contains("pdf")) {
                            loadPdf(treeItem);
                        } else {
                            loadPdfs(treeItem);
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return cell;
        });

        try {
            yearOsdItems = years.get();

            for (Map.Entry<Integer, Year> entry : yearOsdItems.entrySet()) {
                TreeItem item = new TreeItem(String.valueOf(entry.getKey()));
                treeYears.put(entry.getValue(), item);
                root.getChildren().add(treeYears.get(entry.getValue()));
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onMenuItemInitializeButtonAction(ActionEvent event) throws IOException {

    }

    @FXML
    private void onMenuFileClose() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void onMenuHelpAbout(Event event) throws IOException {
        Stage stage = new Stage();
        URL url = new File("src\\main\\resources\\help.fxml").toURL();
        Parent root = FXMLLoader.load(url);
        stage.setScene(new Scene(root));
        stage.setTitle("About");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();
    }

    public void loadPdfs(TreeItem item) throws ExecutionException, InterruptedException {
        if (item.getChildren().size() > 0) {
            return;
        }
        Year year = yearOsdItems.get(Integer.valueOf((String) item.getValue()));
        TreeItem treeItem = treeYears.get(year);
        statusBar.setText("Loading " + year.getUrl());
        GetListOfPdfsTask task = new GetListOfPdfsTask(year);
        Future<Map<String, Case>> future = service.submit(task);
        Map<String, Case> cases = future.get();
        for (Map.Entry<String, Case> entry : cases.entrySet()) {
            treeItem.getChildren().add(new TreeItem(entry.getKey()));
            treeCase.put(entry.getKey(), entry.getValue());
        }
        statusBar.setText(cases.size() + " cases found");
    }

    public void loadPdf(TreeItem item) throws IOException {
        System.out.println(treeCase.get(item.getValue()));
        Platform.runLater(() -> {
            try {
                webView.getEngine().loadContent(osdInterface.getCaseInformation(treeCase.get(item.getValue())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Case _case = (Case) treeCase.get(item.getValue());
        osdInterface.getPdf(_case.getUrl());
        osdInterface.parsePdf(_case.getFileName());
    }

}
