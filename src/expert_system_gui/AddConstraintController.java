package expert_system_gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import org.dom4j.Element;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

public class AddConstraintController
{
    @FXML
    private TextField ConstraintTextField;
    @FXML
    private ComboBox OperationComboBox;
    @FXML
    private ComboBox ValueComboBox;
    @FXML
    private TreeView classHierarchy;
    @FXML
    private AnchorPane EditCosntraintsWindow;
    @FXML
    private TreeView<Pair<Element, String>> ConstraintsTree;


    ConstraintManager constraintsManger;


    static public class Pair<K, V> extends AbstractMap.SimpleEntry<K, V>
    {

        public Pair(K key, V value)
        {
            super(key, value);
        }

        public Pair(Map.Entry<? extends K, ? extends V> entry)
        {
            super(entry);
        }

        @Override
        public String toString()
        {
            return getValue().toString();
        }
    }

    public void initialize()
    {
        constraintsManger = Main.getInstance().constraints;
        if(constraintsManger.isEmpty())
        {
            constraintsManger.createEmptyDocument();
        }
        TreeItem<Pair<Element, String>> rootItem = constraintsManger.getConstraintsTree();
        ConstraintsTree.setRoot(rootItem);

        ArrayList<String> operationList = new ArrayList<>()
        {{
                add("addition");
                add("subtraction");
                add("multiplication");
                add("division");
                add("exponentiation");
                add("root");
            }};

        ArrayList<String> ValueList = new ArrayList<>()
        {{
            add("constant");
            add("variable");
        }};

        OperationComboBox.setItems(FXCollections.observableArrayList(operationList));
        ValueComboBox.setItems(FXCollections.observableArrayList(ValueList));

    }

    public void AddOperationButtonClick(ActionEvent actionEvent)
    {
        Element element = ConstraintsTree.getSelectionModel().getSelectedItem().getValue().getKey();
        if (element.getName().equals("firstElement") || element.getName().equals("secondElement") || element.getName().equals("constraint"))
        {
            if(element.elements().isEmpty())
            {
                element.addElement("node").addAttribute("name",OperationComboBox.getSelectionModel().getSelectedItem().toString());
                return;
            }
        }


    }

    public void DeleteNodeButtonClick(ActionEvent actionEvent)
    {
        Element element = ConstraintsTree.getSelectionModel().getSelectedItem().getValue().getKey();
        if(element.getName().equals("node"))
        {
            constraintsManger.deleteElement(element);
            initialize();
        }
    }

    public void AddValueButtonClick(ActionEvent actionEvent)
    {
    }

    public void AddConstraintButtonClick(ActionEvent actionEvent)
    {

    }

    public void OKButtonClick(ActionEvent actionEvent)
    {
    }

    public void CancelButtonClick(ActionEvent actionEvent)
    {
        EditCosntraintsWindow.getScene().getWindow().hide();
    }
}
