package expert_system_gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Controller
{

    @FXML
    TreeView<String> classHierarchy;
    @FXML
    AnchorPane MainWindow;
    @FXML
    ListView<String> ObjectPropertiesList, ObjectPropertiesListNewIndividual;
    @FXML
    ListView<String> DataPropertiesList, DataPropertiesListNewIndividual;
    @FXML
    ListView<String> IndividualList;
    @FXML
    ComboBox<String> ComboBoxSelectObjectProperty, ComboBoxSelectObjectPropertyNewIndividual;
    @FXML
    ComboBox<String> ComboBoxSelectDataProperty, ComboBoxSelectDataPropertyNewIndividual;
    @FXML
    ComboBox<String> ComboBoxSelectClass, ComboBoxSelectClassNewIndividual;
    @FXML
    Button ButtonResetObjectProperty;
    @FXML
    Button ButtonAddObjectProperty;
    @FXML
    TextField TextFieldDataProperty, TextFieldDataPropertyNewIndividual, TextFieldNewIndividual;
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

                ComboBoxSelectObjectPropertyNewIndividual.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfObjectProperties()));
                ComboBoxSelectDataPropertyNewIndividual.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfDataProperties()));

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
            e.getCause();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Could not save ontology.");
            alert.showAndWait();
        }
    }

    @FXML
    private void saveOntologyAsClick(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save ontology file");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("OWL files (*.owl)", "*.owl");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File("."));
        Window window = MainWindow.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        if (file != null)
        {
            try
            {
                Main.getInstance().ontology.saveOntologyToFile(file);
            } catch (Throwable e)
            {
                e.getCause();
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("Could not save ontology to file.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void individualListItemSelected(MouseEvent event)
    {
        String individual = IndividualList.getSelectionModel().getSelectedItem();
        List<String> listOfObjectProperties = Main.getInstance().ontology.listOfObjectPropertiesOfIndividual(individual);
        List<String> listOfDataProperties = Main.getInstance().ontology.listOfDataPropertiesOfIndividual(individual);

        ObjectPropertiesList.setItems(FXCollections.observableArrayList(listOfObjectProperties));
        DataPropertiesList.setItems(FXCollections.observableArrayList(listOfDataProperties));
        ComboBoxSelectObjectProperty.getItems().clear();
        ComboBoxSelectDataProperty.getItems().clear();
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
        {
            ComboBoxSelectObjectProperty.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfObjectProperties()));
            ComboBoxSelectObjectProperty.getSelectionModel().select(0);
        }

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
        {
            ComboBoxSelectDataProperty.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfDataProperties()));
            ComboBoxSelectDataProperty.getSelectionModel().select(0);
        }

        if (ObjectPropertiesList.getItems().isEmpty() && DataPropertiesList.getItems().isEmpty())
            IndividualList.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfIndividuals()));
    }


    public void ButtonAddDataPropertyNewIndividualClick(ActionEvent actionEvent)
    {
        if (ComboBoxSelectDataPropertyNewIndividual.getValue() != null && TextFieldDataPropertyNewIndividual.getText() != null)
        {
            DataPropertiesListNewIndividual.getItems().add(ComboBoxSelectDataPropertyNewIndividual.getValue() + " = " + TextFieldDataPropertyNewIndividual.getText());
            ComboBoxSelectDataPropertyNewIndividual.getItems().remove(ComboBoxSelectDataPropertyNewIndividual.getValue());
            ComboBoxSelectDataPropertyNewIndividual.getSelectionModel().select(0);

        }
    }

    public void ButtonResetDataPropertyNewIndividualClick(ActionEvent actionEvent)
    {
        DataPropertiesListNewIndividual.setItems(FXCollections.observableArrayList());
        ComboBoxSelectDataPropertyNewIndividual.getSelectionModel().clearSelection();
        TextFieldDataPropertyNewIndividual.clear();

        if (Main.getInstance().ontology.getOntology() != null)
        {
            ComboBoxSelectDataPropertyNewIndividual.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfDataProperties()));
            ComboBoxSelectDataPropertyNewIndividual.getSelectionModel().select(0);
        }
    }

    public void ButtonAddObjectPropertyNewIndividualClick(ActionEvent actionEvent)
    {
        if (ComboBoxSelectClassNewIndividual.getValue() != null && ComboBoxSelectObjectPropertyNewIndividual.getValue() != null)
        {

            ArrayList<String> list = new ArrayList<>();

            for (String str : ObjectPropertiesListNewIndividual.getItems())
            {
                String[] splitStr = str.trim().split("\\s+");

                list.add(splitStr[1]);
            }
            list.add(ComboBoxSelectClassNewIndividual.getValue());

            if (Main.getInstance().ontology.classSetIsSatisfiable(list))
            {
                ObjectPropertiesListNewIndividual.getItems().add(ComboBoxSelectObjectPropertyNewIndividual.getValue() + " " + ComboBoxSelectClassNewIndividual.getValue());
                ComboBoxSelectObjectPropertyNewIndividual.getItems().remove(ComboBoxSelectObjectPropertyNewIndividual.getValue());
                ComboBoxSelectObjectPropertyNewIndividual.getSelectionModel().select(0);
            } else
            {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("Restriction is not satisfable.");
                alert.showAndWait();
            }
        }
    }

    public void ObjectPropertyNewIndividualSelected(ActionEvent actionEvent)
    {
        String property = ComboBoxSelectObjectPropertyNewIndividual.getValue();
        if (property != null)
        {
            ComboBoxSelectClassNewIndividual.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfClassesInRangeOfObjectProperties(property)));
        }
    }

    public void DataPropertyNewIndividualSelected(ActionEvent actionEvent)
    {
        //TODO: check Data type range of property
    }

    public void ButtonResetObjectPropertyNewIndividualClick(ActionEvent actionEvent)
    {
        ObjectPropertiesListNewIndividual.setItems(FXCollections.observableArrayList());
        ComboBoxSelectObjectPropertyNewIndividual.getSelectionModel().clearSelection();
        ComboBoxSelectClassNewIndividual.getSelectionModel().clearSelection();

        if (Main.getInstance().ontology.getOntology() != null)
        {
            ComboBoxSelectObjectPropertyNewIndividual.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfObjectProperties()));
            ComboBoxSelectObjectPropertyNewIndividual.getSelectionModel().select(0);
        }

    }

    public void ButtonClearTextNewIndividualClick(ActionEvent actionEvent)
    {
        TextFieldDataPropertyNewIndividual.clear();
    }

    public void ButtonAddNevIndividualClick(ActionEvent actionEvent)
    {
        if (Main.getInstance().ontology.getOntology() != null && !TextFieldNewIndividual.getText().isEmpty())
        {
            String individual = TextFieldNewIndividual.getText();
            boolean overwrite = true;
            if (Main.getInstance().ontology.containsEntity(individual))
            {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(individual + " already exist");
                alert.setHeaderText(null);
                alert.setContentText("Do you want to overwrite?");

                ButtonType buttonTypeOne = new ButtonType("Yes");
                ButtonType buttonTypeTwo = new ButtonType("No");

                alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonTypeOne)
                {
                    Main.getInstance().ontology.deleteIndividual(individual);
                } else if (result.get() == buttonTypeTwo)
                {
                    overwrite = false;
                }

            }
            if (overwrite)
            {
                Main.getInstance().ontology.addNewIndividual(individual, ObjectPropertiesListNewIndividual.getItems(), DataPropertiesListNewIndividual.getItems());
                ObjectPropertiesListNewIndividual.getItems().clear();
                DataPropertiesListNewIndividual.getItems().clear();
                TextFieldNewIndividual.clear();
                ComboBoxSelectObjectPropertyNewIndividual.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfObjectProperties()));
                ComboBoxSelectDataPropertyNewIndividual.setItems(
                        FXCollections.observableArrayList(
                                Main.getInstance().ontology.listOfDataProperties()));
            }
        }
    }


    @FXML
    private void exitClick(ActionEvent event)
    {
        Platform.exit();
    }


}
