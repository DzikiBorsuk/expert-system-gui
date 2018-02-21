package expert_system_gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.ArrayList;

public class Controller
{

    @FXML
    TreeView<String> classHierarchy;

    @FXML
    AnchorPane MainWindow;

    @FXML
    ListView<String> ObjectPropertiesList;

    @FXML
    ListView<String> DataPropertiesList;

    @FXML
    ComboBox<String> ComboBoxSelectObjectProperty;

    @FXML
    ComboBox<String> ComboBoxSelectDataProperty;

    @FXML
    ComboBox<String> ComboBoxSelectClass;

    @FXML
    Button ButtonResetObjectProperty;

    @FXML
    Button ButtonAddObjectProperty;


    public void initialize()
    {
        // classHierarchy.setRoot(new TreeItem<>("Thing"));
    }

    @FXML
    private void loadOntologyClick(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load ontology file");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("OWL files (*.owl)", "*.owl");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File("."));
        Window window = MainWindow.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        //File file = new File("C:\\Users\\0bobe\\Desktop\\");

        if (file != null)
        {
            if (file.exists() == true)
            {
                try
                {
                    Main.getInstance().ontology.loadOntologyFromFile(file.getAbsoluteFile());
                } catch (Throwable e)
                {
                    e.getCause();
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(null);
                    alert.setHeaderText(null);
                    alert.setContentText("Could not load ontology.");
                    alert.showAndWait();

                }
                TreeItem<String> rootItem = new TreeItem<>("Thing");
                classHierarchy.setRoot(rootItem);

                Main.getInstance().ontology.printOntologyToTreeView(rootItem);

                ArrayList<String> list = Main.getInstance().ontology.listOfObjectProperties();

                ObservableList<String> ObjectPropertiesList = FXCollections.observableArrayList(list);

                ComboBoxSelectObjectProperty.setItems(ObjectPropertiesList);


            } else
            {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("File does not exist.");
                alert.setHeaderText(null);
                alert.setContentText("\"" + file.getAbsolutePath() + "\" does not exist");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void ObjectPropertySelected(ActionEvent event)
    {
        String property = ComboBoxSelectObjectProperty.getValue();
        ArrayList<String> list = Main.getInstance().ontology.listOfClassesInRangeOfObjectProperties(property);
        ObservableList<String> inRange = FXCollections.observableArrayList(list);
        ComboBoxSelectClass.setItems(inRange);
    }

    @FXML
    private void ButtonResetObjectPropertyClick(ActionEvent event)
    {
        ObjectPropertiesList.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void ButtonAddObjectPropertyClick(ActionEvent event)
    {
        if (ComboBoxSelectClass.getValue() != null && ComboBoxSelectObjectProperty.getValue() != null)
            ObjectPropertiesList.getItems().add(ComboBoxSelectObjectProperty.getValue() + " " + ComboBoxSelectClass.getValue());
    }

    @FXML
    private void exitClick(ActionEvent event)
    {
        Platform.exit();
    }

}
