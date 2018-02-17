package expert_system_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;

public class Controller
{

    @FXML
    TreeView<String> classHierarchy;

    @FXML
    private void loadOntologyClick(ActionEvent event)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load ontology file");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("OWL files (*.owl)", "*.owl");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(new Stage());

        Main.getInstance().ontology.loadOntologyFromFile(file);

        //TreeView<String> root = new TreeView<>("Thing");

        IRI ontologyIRI = Main.getInstance().ontology.getOntology().getOntologyID().getOntologyIRI().get();
        IRI documentIRI = Main.getInstance().ontology.getOntologyManager().getOntologyDocumentIRI(Main.getInstance().ontology.getOntology());
        System.out.println(ontologyIRI == null ? "anonymous" : ontologyIRI
                .toQuotedString());
        System.out.println("    from " + documentIRI.toQuotedString());


    }

}
