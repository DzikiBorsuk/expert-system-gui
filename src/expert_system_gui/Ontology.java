package expert_system_gui;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.*;
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

    private OWLClass thing;

    public Ontology()
    {
        ontologyManager = OWLManager.createOWLOntologyManager();
        dataFactory = ontologyManager.getOWLDataFactory();
        reasonerFactory = new StructuralReasonerFactory();
    }


    public void loadOntologyFromFile(File file) throws OWLOntologyCreationException, OWLParserException
    {
        if (ontology != null)
            ontologyManager.removeOntology(ontology);

        ontology = ontologyManager.loadOntologyFromOntologyDocument(file);
        ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        reasoner = reasonerFactory.createReasoner(ontology);
        thing = ontologyManager.getOWLDataFactory().getOWLThing();
    }

    public void printOntologyToTreeView(TreeItem<String> root)
    {
        reasoner = reasonerFactory.createReasoner(ontology);
        printOntologyToTreeView(root, thing);
        reasoner.dispose();
    }

    private void printOntologyToTreeView(TreeItem<String> root, OWLClass cl)
    {
        if (reasoner.isSatisfiable(cl))
        {
            int i = 0;
            for (OWLClass child : reasoner.getSubClasses(cl, true).getFlattened())
            {
                if (!child.equals(cl) && reasoner.isSatisfiable(child))
                {
                    root.getChildren().add(new TreeItem<>(child.getIRI().getShortForm()));
                    printOntologyToTreeView(root.getChildren().get(i), child);
                    i++;

                }
            }
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
