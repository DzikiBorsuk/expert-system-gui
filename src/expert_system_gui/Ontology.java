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
import uk.ac.manchester.cs.jfact.*;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

import java.io.File;
import java.util.ArrayList;


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
        reasonerFactory = new JFactFactory();
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
        reasonerFactory = new StructuralReasonerFactory();
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

    public ArrayList<String> listOfObjectProperties()
    {
        reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        OWLObjectProperty topProperty = ontologyManager.getOWLDataFactory().getOWLTopObjectProperty();
        OWLObjectProperty bottomProperty = ontologyManager.getOWLDataFactory().getOWLBottomObjectProperty();
        ArrayList<String> list = new ArrayList<>();

        for (OWLObjectPropertyExpression child : reasoner.getSubObjectProperties(topProperty, false).getFlattened())
        {
            if (!reasoner.getSubObjectProperties(child).isEmpty() && reasoner.getSubObjectProperties(child, true).containsEntity(bottomProperty))
            {
                String str = child.getNamedProperty().getIRI().getShortForm();
                if (!list.contains(str))
                    list.add(str);
            }
        }

        reasoner.dispose();
        return list;
    }

    public ArrayList<String> listOfClassesInRangeOfObjectProperties(String ObjectPropertyShortForm)
    {
        reasonerFactory =new StructuralReasonerFactory();// new JFactFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        IRI propertyIRI = IRI.create(ontologyIRI.getIRIString() + "#" + ObjectPropertyShortForm);
        ArrayList<String> list = new ArrayList<>();

        OWLClass Nothing = ontologyManager.getOWLDataFactory().getOWLNothing();

        OWLObjectPropertyExpression property = ontologyManager.getOWLDataFactory().getOWLObjectProperty(propertyIRI);


        for (OWLClass child : reasoner.getObjectPropertyRanges(property, true).getFlattened())
        {


            for(OWLClass child2 : reasoner.getSubClasses(child,false).getFlattened())
            {
                 if (!reasoner.getSubClasses(child2).isEmpty() && reasoner.getSubClasses(child2, true).containsEntity(Nothing))
                 {
                String str = child2.getIRI().getShortForm();
                 if (!list.contains(str))
                list.add(str);
                 }
            }
        }


        return list;
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
