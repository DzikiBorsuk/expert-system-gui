package expert_system_gui;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Controller
{

    private static Controller mainInstance;


    public static Controller getInstance()
    {
        return mainInstance;
    }

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
    @FXML
    MenuItem SaveOntology, SaveOntologyAs;
    @FXML
    CheckMenuItem MenuItemShowSatisfiable;
    @FXML
    ListView<String> ConstraintsList;

    public void initialize()
    {
        mainInstance = this;
        // classHierarchy.setRoot(new TreeItem<>("Thing"));
        TextFieldDataProperty.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if (!newValue.matches("\\d*(\\.\\d*)?"))
            {
                TextFieldDataProperty.setText(oldValue);
            }
        });
        TextFieldDataPropertyNewIndividual.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if (!newValue.matches("\\d*(\\.\\d*)?"))
            {
                TextFieldDataPropertyNewIndividual.setText(oldValue);
            }
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

                rootItem.setExpanded(true);

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

                ObjectPropertiesList.getItems().clear();
                ObjectPropertiesListNewIndividual.getItems().clear();
                DataPropertiesList.getItems().clear();
                DataPropertiesListNewIndividual.getItems().clear();

                SaveOntology.setDisable(false);
                SaveOntologyAs.setDisable(false);

            }
            else
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
        if (event.getButton().equals(MouseButton.PRIMARY))
        {
            if (event.getClickCount() == 2)
            {
                String individual = IndividualList.getSelectionModel().getSelectedItem();
                List<String> listOfObjectProperties = Main.getInstance().ontology.listOfObjectPropertiesOfIndividual(individual);
                List<String> listOfDataProperties = Main.getInstance().ontology.listOfDataPropertiesOfIndividual(individual);

                ButtonResetObjectPropertyClick(null);
                ButtonResetDataPropertyClick(null);
                ObjectPropertiesList.setItems(FXCollections.observableArrayList(listOfObjectProperties));
                DataPropertiesList.setItems(FXCollections.observableArrayList(listOfDataProperties));
                for (String str : ObjectPropertiesList.getItems())
                {
                    String[] splitStr = str.trim().split("\\s+");

                    ComboBoxSelectObjectProperty.getItems().remove(splitStr[0]);
                }
                for (String str : DataPropertiesList.getItems())
                {
                    String[] splitStr = str.trim().split("\\s+");

                    ComboBoxSelectDataProperty.getItems().remove(splitStr[0]);
                }
                // ComboBoxSelectObjectProperty.getItems().clear();
                // ComboBoxSelectDataProperty.getItems().clear();
            }
        }
        else if (event.getButton().equals(MouseButton.SECONDARY))
        {
            if (event.getClickCount() == 2)
            {
                String individual = IndividualList.getSelectionModel().getSelectedItem();
                List<String> listOfObjectProperties = Main.getInstance().ontology.listOfObjectPropertiesOfIndividual(individual);
                List<String> listOfDataProperties = Main.getInstance().ontology.listOfDataPropertiesOfIndividual(individual);
                ButtonResetObjectPropertyNewIndividualClick(null);
                ButtonResetDataPropertyNewIndividualClick(null);
                ObjectPropertiesListNewIndividual.setItems(FXCollections.observableArrayList(listOfObjectProperties));
                DataPropertiesListNewIndividual.setItems(FXCollections.observableArrayList(listOfDataProperties));
                for (String str : ObjectPropertiesListNewIndividual.getItems())
                {
                    String[] splitStr = str.trim().split("\\s+");

                    ComboBoxSelectObjectPropertyNewIndividual.getItems().remove(splitStr[0]);
                }
                for (String str : DataPropertiesListNewIndividual.getItems())
                {
                    String[] splitStr = str.trim().split("\\s+");

                    ComboBoxSelectDataPropertyNewIndividual.getItems().remove(splitStr[0]);
                }
                TextFieldNewIndividual.setText(individual);

            }
        }
    }

    @FXML
    private void ObjectPropertySelected(ActionEvent event)
    {
        String property = ComboBoxSelectObjectProperty.getValue();
        if (property != null && !property.isEmpty())
        {
            List<String> list = Main.getInstance().ontology.listOfClassesInRangeOfObjectProperties(property);
            List<String> elementsToDelete = new ArrayList<>();
            if (!MenuItemShowSatisfiable.isSelected())
            {
                if (!ObjectPropertiesList.getItems().isEmpty())
                {
                    for (String elementToCheck : list)
                    {
                        List<String> listToCheck = new ArrayList<>();
                        for (String str : ObjectPropertiesList.getItems())
                        {
                            String[] splitStr = str.trim().split("\\s+");

                            listToCheck.add(splitStr[1]);
                        }
                        listToCheck.add(elementToCheck);

                        if (!Main.getInstance().ontology.isClassSetSatisfiable(listToCheck))
                        {
                            elementsToDelete.add(elementToCheck);
                        }
                    }

                    list.removeAll(elementsToDelete);
                }
            }

            ComboBoxSelectClass.setItems(FXCollections.observableArrayList(list));
            ComboBoxSelectClass.getItems().sort(Comparator.comparing(String::toString));
        }
    }


    @FXML
    private void DataPropertySelected(ActionEvent event)
    {
        String property = ComboBoxSelectDataProperty.getValue();
        if (property != null && !property.isEmpty())
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
        {
            IndividualList.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfIndividuals()));
        }
    }

    @FXML
    private void ButtonAddObjectPropertyClick(ActionEvent event)
    {
        if (ComboBoxSelectClass.getValue() != null && ComboBoxSelectObjectProperty.getValue() != null)
        {
            String item = ComboBoxSelectObjectProperty.getValue() + " " + ComboBoxSelectClass.getValue();
            if (!ObjectPropertiesList.getItems().contains(item))
            {
                ObjectPropertiesList.getItems().add(item);
            }
            ComboBoxSelectObjectProperty.getItems().remove(ComboBoxSelectObjectProperty.getValue());
            ComboBoxSelectObjectProperty.getSelectionModel().select(0);

            List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(ObjectPropertiesList.getItems(), DataPropertiesList.getItems());
            IndividualList.setItems(FXCollections.observableArrayList(list));
        }
    }

    @FXML
    private void ButtonAddDataPropertyClick(ActionEvent event)
    {
        if (ComboBoxRestriction.getValue() != null && ComboBoxSelectDataProperty.getValue() != null && !TextFieldDataProperty.getText().isEmpty())
        {
            String item = ComboBoxSelectDataProperty.getValue() + " " + ComboBoxRestriction.getValue() + " " + TextFieldDataProperty.getText();
            if (!DataPropertiesList.getItems().contains(item))
            {
                DataPropertiesList.getItems().add(item);
            }
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
        {
            IndividualList.setItems(
                    FXCollections.observableArrayList(
                            Main.getInstance().ontology.listOfIndividuals()));
        }
    }


    public void ButtonAddDataPropertyNewIndividualClick(ActionEvent actionEvent)
    {
        if (ComboBoxSelectDataPropertyNewIndividual.getValue() != null && !TextFieldDataPropertyNewIndividual.getText().isEmpty())
        {
            String item = ComboBoxSelectDataPropertyNewIndividual.getValue() + " = " + TextFieldDataPropertyNewIndividual.getText();
            if (!DataPropertiesListNewIndividual.getItems().contains(item))
            {
                DataPropertiesListNewIndividual.getItems().add(item);
            }
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

            if (Main.getInstance().ontology.isClassSetSatisfiable(list))
            {
                String item = ComboBoxSelectObjectPropertyNewIndividual.getValue() + " " + ComboBoxSelectClassNewIndividual.getValue();
                if (!ObjectPropertiesListNewIndividual.getItems().contains(item))
                {
                    ObjectPropertiesListNewIndividual.getItems().add(item);
                }
                ComboBoxSelectObjectPropertyNewIndividual.getItems().remove(ComboBoxSelectObjectPropertyNewIndividual.getValue());
                ComboBoxSelectObjectPropertyNewIndividual.getSelectionModel().select(0);
            }
            else
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
        if (property != null && !property.isEmpty())
        {
            List<String> list = Main.getInstance().ontology.listOfClassesInRangeOfObjectProperties(property);
            List<String> elementsToDelete = new ArrayList<>();
            if (!MenuItemShowSatisfiable.isSelected())
            {
                if (!ObjectPropertiesListNewIndividual.getItems().isEmpty())
                {
                    for (String elementToCheck : list)
                    {
                        List<String> listToCheck = new ArrayList<>();
                        for (String str : ObjectPropertiesListNewIndividual.getItems())
                        {
                            String[] splitStr = str.trim().split("\\s+");

                            listToCheck.add(splitStr[1]);
                        }
                        listToCheck.add(elementToCheck);

                        if (!Main.getInstance().ontology.isClassSetSatisfiable(listToCheck))
                        {
                            elementsToDelete.add(elementToCheck);
                        }
                    }

                    list.removeAll(elementsToDelete);
                }
            }

            ComboBoxSelectClassNewIndividual.setItems(FXCollections.observableArrayList(list));
            ComboBoxSelectClassNewIndividual.getItems().sort(Comparator.comparing(String::toString));
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
        TextFieldNewIndividual.clear();
    }

    public void ButtonAddNevIndividualClick(ActionEvent actionEvent)
    {
        if (Main.getInstance().ontology.getOntology() != null && !TextFieldNewIndividual.getText().isEmpty())
        {
            String individual = TextFieldNewIndividual.getText();
            boolean overwrite = true;
            if (Main.getInstance().ontology.containsIndividual(individual))
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
                }
                else if (result.get() == buttonTypeTwo)
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

                List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(ObjectPropertiesList.getItems(), DataPropertiesList.getItems());
                IndividualList.setItems(FXCollections.observableArrayList(list));
            }
        }
    }


    @FXML
    private void exitClick(ActionEvent event)
    {
        Platform.exit();
    }


    public void ObjectPropertiesListItemSelected(MouseEvent mouseEvent)
    {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
        {
            if (mouseEvent.getClickCount() == 2)
            {
                String item = ObjectPropertiesList.getSelectionModel().getSelectedItem();
                String[] splitStr = item.trim().split("\\s+");
                if (!ComboBoxSelectObjectProperty.getItems().contains(splitStr[0]))
                {
                    ComboBoxSelectObjectProperty.getItems().add(splitStr[0]);
                }
                ObjectPropertiesList.getItems().remove(ObjectPropertiesList.getSelectionModel().getSelectedIndex());

                List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(ObjectPropertiesList.getItems(), DataPropertiesList.getItems());
                IndividualList.setItems(FXCollections.observableArrayList(list));
            }
        }
    }

    public void DataPropertiesListItemSelected(MouseEvent mouseEvent)
    {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
        {
            if (mouseEvent.getClickCount() == 2)
            {
                String item = DataPropertiesList.getSelectionModel().getSelectedItem();
                String[] splitStr = item.trim().split("\\s+");
                if (!ComboBoxSelectDataProperty.getItems().contains(splitStr[0]))
                {
                    ComboBoxSelectDataProperty.getItems().add(splitStr[0]);
                }
                DataPropertiesList.getItems().remove(DataPropertiesList.getSelectionModel().getSelectedIndex());

                List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(ObjectPropertiesList.getItems(), DataPropertiesList.getItems());
                IndividualList.setItems(FXCollections.observableArrayList(list));
            }
        }
    }

    public void ObjectPropertiesListNewIndividualItemSelected(MouseEvent mouseEvent)
    {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
        {
            if (mouseEvent.getClickCount() == 2)
            {
                String item = ObjectPropertiesListNewIndividual.getSelectionModel().getSelectedItem();
                String[] splitStr = item.trim().split("\\s+");
                if (!ComboBoxSelectObjectPropertyNewIndividual.getItems().contains(splitStr[0]))
                {
                    ComboBoxSelectObjectPropertyNewIndividual.getItems().add(splitStr[0]);
                }
                ObjectPropertiesListNewIndividual.getItems().remove(ObjectPropertiesListNewIndividual.getSelectionModel().getSelectedIndex());
            }
        }
    }

    public void DataPropertiesListNewIndividualItemSelected(MouseEvent mouseEvent)
    {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
        {
            if (mouseEvent.getClickCount() == 2)
            {
                String item = DataPropertiesListNewIndividual.getSelectionModel().getSelectedItem();
                String[] splitStr = item.trim().split("\\s+");
                if (!ComboBoxSelectDataPropertyNewIndividual.getItems().contains(splitStr[0]))
                {
                    ComboBoxSelectDataPropertyNewIndividual.getItems().add(splitStr[0]);
                }
                DataPropertiesListNewIndividual.getItems().remove(DataPropertiesListNewIndividual.getSelectionModel().getSelectedIndex());
            }
        }
    }

    public void DeleteIndividualClick(ActionEvent actionEvent)
    {
        Parent root;
        if (Main.getInstance().ontology.getOntology() != null)
        {
            try
            {
                root = FXMLLoader.load(getClass().getResource("delete_individual.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Delete individual");
                stage.setScene(new Scene(root, 600, 571));
                Window window = MainWindow.getScene().getWindow();
                stage.initOwner(window);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ontology not loaded");
            alert.setHeaderText(null);
            alert.setContentText("Ontology not loaded");
            alert.showAndWait();
        }
    }

    public void MenuItemShowSatisfiableClicked(ActionEvent actionEvent)
    {
        ObjectPropertyNewIndividualSelected(actionEvent);
        ObjectPropertySelected(actionEvent);
    }

    public void loadConstraintsClick(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load constraints file");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
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
                    Main.getInstance().constraints.loadConstraintsFromFile(file.getAbsoluteFile());
                } catch (Throwable e)
                {
                    e.getCause();
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(null);
                    alert.setHeaderText(null);
                    alert.setContentText("Could not load constraints.");
                    alert.showAndWait();

                }
                ConstraintsList.setItems(FXCollections.observableArrayList(Main.getInstance().constraints.getConstraintsList()));
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("File does not exist.");
                alert.setHeaderText(null);
                alert.setContentText("\"" + file.getAbsolutePath() + "\" does not exist");
                alert.showAndWait();
            }
        }
    }

    public void saveConstraintsClick(ActionEvent actionEvent)
    {
        try
        {
            Main.getInstance().constraints.saveConstraints();
        } catch (IOException e)
        {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Could not save constraints.");
            alert.showAndWait();
        }
    }

    public void saveAsConstraintsClick(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save constraints file");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File("."));
        Window window = MainWindow.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        if (file != null)
        {
            try
            {
                Main.getInstance().constraints.saveConstraintsToFile(file);
            } catch (Throwable e)
            {
                e.getCause();
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(null);
                alert.setHeaderText(null);
                alert.setContentText("Could not save constraints to file.");
                alert.showAndWait();
            }
        }
    }

    public void ConstraintsListItemSelected(MouseEvent mouseEvent)
    {
    }

    public void ButtonAddConstraintClick(ActionEvent actionEvent)
    {
        String constraintName = ConstraintsList.getSelectionModel().getSelectedItem();
        if (constraintName != null)
        {
            if (Main.getInstance().constraints.isLoaded())
            {
                List<String> inputs = Main.getInstance().constraints.getInputs(constraintName);
                boolean ok = false;
                List<String> propertiesList = DataPropertiesListNewIndividual.getItems();
                HashMap<String, String> inputList = new HashMap<>();
                for (Iterator<String> it = inputs.iterator(); it.hasNext(); )
                {
                    ok = false;
                    String in = it.next();
                    for (Iterator<String> iterator = propertiesList.iterator(); iterator.hasNext(); )
                    {
                        String prop = iterator.next();
                        String[] splitStr = prop.trim().split("\\s+");
                        if (splitStr[0].equals(in))
                        {
                            inputList.put(splitStr[0], splitStr[2]);
                            ok = true;
                            break;
                        }
                    }
                }
                if (ok == false)
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle(null);
                    alert.setHeaderText(null);
                    alert.setContentText("Not enough inputs");
                    alert.showAndWait();
                }
                else
                {
                    String result = Main.getInstance().constraints.solve(constraintName, inputList);
                    String[] splitResult = result.trim().split("\\s+");
                    for (Iterator<String> iterator = propertiesList.iterator(); iterator.hasNext(); )
                    {
                        String prop = iterator.next();
                        String[] splitStr = prop.trim().split("\\s+");
                        if (splitStr[0].equals(splitResult[0]))
                        {
                            return;
                        }
                    }
                    DataPropertiesListNewIndividual.getItems().add(result);
                }
            }
        }
    }

    public void ButtonShowDescriptionClick(ActionEvent actionEvent)
    {
        String constraintName = ConstraintsList.getSelectionModel().getSelectedItem();
        String description = Main.getInstance().constraints.getDescription(constraintName);
        String content = description + System.lineSeparator() + System.lineSeparator() + "Inputs:" + System.lineSeparator();
        Iterator<String> iterator = Main.getInstance().constraints.getInputs(constraintName).iterator();
        while (iterator.hasNext())
        {
            String str = iterator.next();
            content = content + str + System.lineSeparator();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void EditConstraintsClick(ActionEvent actionEvent)
    {
        try
        {
            Parent root;
            root = FXMLLoader.load(getClass().getResource("add_constraint_window.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Edit constraints");
            stage.setScene(new Scene(root, 800, 560));
            Window window = MainWindow.getScene().getWindow();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
