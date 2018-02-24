package expert_system_gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class DeleteController
{
    @FXML
    AnchorPane DeleteIndividualWindow;
    @FXML
    ListView<String> IndividualListToDelete;

    List<String> individualToRemove = new ArrayList<>();

    public void initialize()
    {
        List<String> list = Main.getInstance().ontology.listOfIndividuals();
        IndividualListToDelete.setItems(FXCollections.observableArrayList(list));
    }


    public void individualListToDeleteItemSelected(MouseEvent mouseEvent)
    {

    }

    public void ButtomDeleteClick(ActionEvent actionEvent)
    {
        if (!IndividualListToDelete.getItems().isEmpty())
        {
            if (!IndividualListToDelete.getSelectionModel().isEmpty())
            {
                String individual = IndividualListToDelete.getSelectionModel().getSelectedItem();
                individualToRemove.add(individual);
                IndividualListToDelete.getItems().remove(individual);
            }
        }
    }

    public void ButtonOkClick(ActionEvent actionEvent)
    {
        for(String individual:individualToRemove)
        {
            Main.getInstance().ontology.deleteIndividual(individual);
        }

        List<String> list = Main.getInstance().ontology.listOfInstancesForSpecifiedData(Controller.getInstance().ObjectPropertiesList.getItems(), Controller.getInstance().DataPropertiesList.getItems());
        Controller.getInstance().IndividualList.setItems(FXCollections.observableArrayList(list));

       DeleteIndividualWindow.getScene().getWindow().hide();
    }

    public void ButtonCancelClick(ActionEvent actionEvent)
    {
        DeleteIndividualWindow.getScene().getWindow().hide();
    }
}
