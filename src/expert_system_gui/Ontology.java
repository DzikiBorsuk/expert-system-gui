package expert_system_gui;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.io.File;

public class Ontology
{
    private OWLOntology ontology;

    private OWLReasoner reasoner;

    private OWLReasonerFactory reasonerFactory;

    private OWLDataFactory dataFactory;

    private IRI ontologyIRI;

    private OWLOntologyManager ontologyManager;

    private ShortFormProvider shortForm;



    public void loadOntologyFromFile(File file)
    {
        try
        {
           ontologyManager = OWLManager.createOWLOntologyManager();
            dataFactory = ontologyManager.getOWLDataFactory();
            ontology = ontologyManager.loadOntologyFromOntologyDocument(file);
            reasonerFactory = new StructuralReasonerFactory();
            ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        }
        catch(Throwable e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("Could not load ontology");
            alert.showAndWait();
        }

    }

    public OWLOntology getOntology()
    {
        return ontology;
    }

    public OWLReasoner getReasoner()
    {
        return reasoner;
    }

    public OWLOntologyManager getOntologyManager()
    {
        return ontologyManager;
    }
}
