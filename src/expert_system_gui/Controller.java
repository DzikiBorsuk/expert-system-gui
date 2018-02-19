package expert_system_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.semanticweb.owlapi.model.*;

import java.io.File;

public class Controller
{

    @FXML
    TreeView<String> classHierarchy;

    @FXML
    AnchorPane MainWindow;


    public void initialize() {
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
        File file = fileChooser.showOpenDialog(window).getAbsoluteFile();
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

            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("File does not exist.");
                alert.setHeaderText(null);
                alert.setContentText("\""+file.getAbsolutePath() + "\" does not exist");
                alert.showAndWait();

            }
        }
    }

}
