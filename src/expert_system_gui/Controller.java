package expert_system_gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;


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
    ListView<String> IndividualList;
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
    @FXML
    TextField TextFieldDataProperty;
    @FXML
    ComboBox<String> ComboBoxRestriction;
    @FXML
    Button ButtonResetDataProperty, ButtonAddDataProperty;


    public void initialize()
    {
        // classHierarchy.setRoot(new TreeItem<>("Thing"));
        TextFieldDataProperty.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if (!newValue.matches("\\d*"))
                TextFieldDataProperty.setText(oldValue);
        });

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
            if (file.exists())
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

                ComboBoxSelectObjectProperty.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfObjectProperties()));
                ComboBoxSelectDataProperty.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfDataProperties()));
                IndividualList.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfIndividuals()));

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
    private void saveOntologyClick(ActionEvent event)
    {
        try
        {
            Main.getInstance().ontology.saveOntology();
        } catch (Throwable e)
        {

        }
    }

    @FXML
    private void ObjectPropertySelected(ActionEvent event)
    {
        String property = ComboBoxSelectObjectProperty.getValue();
        if (property != null)
        {
            ComboBoxSelectClass.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfClassesInRangeOfObjectProperties(property)));
        }
    }


    @FXML
    private void DataPropertySelected(ActionEvent event)
    {
        String property = ComboBoxSelectDataProperty.getValue();
        if (property != null)
        {
            if (Main.getInstance().ontology.hasDataPropertyNumericRange(property))
            {
                ComboBoxRestriction.getItems().clear();
                ComboBoxRestriction.getItems().add("=");
                ComboBoxRestriction.getItems().add(">=");
                ComboBoxRestriction.getItems().add("<=");
                ComboBoxRestriction.getItems().add(">");
                ComboBoxRestriction.getItems().add("<");

                ComboBoxRestriction.getSelectionModel().select(0);
            }
        }
    }

    @FXML
    private void ButtonResetObjectPropertyClick(ActionEvent event)
    {
        ObjectPropertiesList.setItems(FXCollections.observableArrayList());
        ComboBoxSelectObjectProperty.getSelectionModel().clearSelection();
        ComboBoxSelectClass.getSelectionModel().clearSelection();

        if (Main.getInstance().ontology.getOntology() != null)
            ComboBoxSelectObjectProperty.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfObjectProperties()));

        if (ObjectPropertiesList.getItems().isEmpty() && DataPropertiesList.getItems().isEmpty())
            IndividualList.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfIndividuals()));
    }

    @FXML
    private void ButtonAddObjectPropertyClick(ActionEvent event)
    {
        if (ComboBoxSelectClass.getValue() != null && ComboBoxSelectObjectProperty.getValue() != null)
        {
            ObjectPropertiesList.getItems().add(ComboBoxSelectObjectProperty.getValue() + " " + ComboBoxSelectClass.getValue());
            ComboBoxSelectObjectProperty.getItems().remove(ComboBoxSelectObjectProperty.getValue());
            ComboBoxSelectObjectProperty.getSelectionModel().select(0);

            List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(ObjectPropertiesList.getItems(), DataPropertiesList.getItems());
            IndividualList.setItems(FXCollections.observableArrayList(list));
        }
    }

    @FXML
    private void ButtonAddDataPropertyClick(ActionEvent event)
    {
        if (ComboBoxRestriction.getValue() != null && ComboBoxSelectDataProperty.getValue() != null && TextFieldDataProperty.getText() != null)
        {
            DataPropertiesList.getItems().add(ComboBoxSelectDataProperty.getValue() + " " + ComboBoxRestriction.getValue() + " " + TextFieldDataProperty.getText());
            ComboBoxSelectDataProperty.getItems().remove(ComboBoxSelectDataProperty.getValue());
            ComboBoxSelectDataProperty.getSelectionModel().select(0);

            List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(ObjectPropertiesList.getItems(), DataPropertiesList.getItems());
            IndividualList.setItems(FXCollections.observableArrayList(list));
        }
    }

    @FXML
    private void ButtonResetDataPropertyClick(ActionEvent event)
    {
        DataPropertiesList.setItems(FXCollections.observableArrayList());
        ComboBoxSelectDataProperty.getSelectionModel().clearSelection();
        ComboBoxRestriction.getSelectionModel().clearSelection();
        TextFieldDataProperty.clear();

        if (Main.getInstance().ontology.getOntology() != null)
            ComboBoxSelectDataProperty.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfDataProperties()));

        if (ObjectPropertiesList.getItems().isEmpty() && DataPropertiesList.getItems().isEmpty())
            IndividualList.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfIndividuals()));
    }


    @FXML
    private void exitClick(ActionEvent event)
    {
        Platform.exit();
    }

}
