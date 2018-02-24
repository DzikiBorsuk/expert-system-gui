package expert_system_gui;


import javafx.scene.control.TreeItem;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLFacet;
import uk.ac.manchester.cs.jfact.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;


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
        //reasonerFactory = new JFactFactory();
    }


    public void loadOntologyFromFile(File file) throws OWLOntologyCreationException, OWLParserException
    {
        if (ontology != null)
            ontologyManager.removeOntology(ontology);

        ontology = ontologyManager.loadOntologyFromOntologyDocument(file);
        if (ontology.getOntologyID().getOntologyIRI().isPresent())
            ontologyIRI = ontology.getOntologyID().getOntologyIRI().get();
        ///reasoner = reasonerFactory.createReasoner(ontology);
        thing = ontologyManager.getOWLDataFactory().getOWLThing();

    }

    public void saveOntology() throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        if (ontology != null)
        {
            ontologyManager.saveOntology(ontology);
        }
    }

    public void saveOntologyToFile(File file) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        if (ontology != null)
        {
            ontologyManager.saveOntology(ontology, IRI.create(file.toURI()));
        }
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
                    String str = getShortForm(child.getIRI());
                    root.getChildren().add(new TreeItem<>(str));
                    printOntologyToTreeView(root.getChildren().get(i), child);
                    i++;

                }
            }
        }
    }

    public List<String> listOfObjectProperties()
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
                //String str = child.getNamedProperty().getIRI().getShortForm();
                String str = getShortForm(child.getNamedProperty().getIRI());
                if (!list.contains(str))
                    list.add(str);
            }
        }

        reasoner.dispose();
        return list;
    }

    public List<String> listOfDataProperties()
    {
        reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        OWLDataProperty topProperty = ontologyManager.getOWLDataFactory().getOWLTopDataProperty();
        OWLDataProperty bottomProperty = ontologyManager.getOWLDataFactory().getOWLBottomDataProperty();
        ArrayList<String> list = new ArrayList<>();

        for (OWLDataProperty child : reasoner.getSubDataProperties(topProperty, false).getFlattened())
        {
            if (!reasoner.getSubDataProperties(child).isEmpty() && reasoner.getSubDataProperties(child, true).containsEntity(bottomProperty))
            {
                //String str = child.getNamedProperty().getIRI().getShortForm();
                String str = getShortForm(child.getIRI());
                if (!list.contains(str))
                    list.add(str);
            }
        }

        reasoner.dispose();
        return list;
    }

    public List<String> listOfObjectPropertiesOfIndividual(String Individual)
    {
        OWLNamedIndividual ind = ontologyManager.getOWLDataFactory().getOWLNamedIndividual(getEntityIRI(Individual));
        Iterator<OWLClassAssertionAxiom> assertionsIterator = ontology.classAssertionAxioms(ind).iterator();
        ArrayList<String> list = new ArrayList<>();
        while (assertionsIterator.hasNext())
        {
            OWLClassAssertionAxiom assertionAxiom = assertionsIterator.next();
            OWLObjectProperty property = assertionAxiom.objectPropertiesInSignature().iterator().next();
            OWLClass cl = assertionAxiom.classesInSignature().iterator().next();
            String propertyName = getShortForm(property.getIRI());
            String className = getShortForm(cl.getIRI());
            list.add(propertyName + " " + className);
        }

        return list;
    }

    public List<String> listOfDataPropertiesOfIndividual(String Individual)
    {
        OWLNamedIndividual ind = ontologyManager.getOWLDataFactory().getOWLNamedIndividual(getEntityIRI(Individual));
        Iterator<OWLDataPropertyAssertionAxiom> assertionsIterator = ontology.dataPropertyAssertionAxioms(ind).iterator();
        ArrayList<String> list = new ArrayList<>();

        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        while (assertionsIterator.hasNext())
        {
            OWLDataPropertyAssertionAxiom assertionAxiom = assertionsIterator.next();
            OWLDataProperty property = assertionAxiom.dataPropertiesInSignature().iterator().next();
            String propertyName = getShortForm(property.getIRI());
            String value = reasoner.getDataPropertyValues(ind, property).iterator().next().getLiteral();
            list.add(propertyName + " = " + value);
        }

        return list;
    }


    public List<String> listOfClassesInRangeOfObjectProperties(String ObjectPropertyShortForm)
    {
        reasonerFactory = new StructuralReasonerFactory();// new JFactFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        IRI propertyIRI = IRI.create(ontologyIRI.getIRIString() + "#" + ObjectPropertyShortForm);
        ArrayList<String> list = new ArrayList<>();

        OWLClass Nothing = ontologyManager.getOWLDataFactory().getOWLNothing();

        OWLObjectPropertyExpression property = ontologyManager.getOWLDataFactory().getOWLObjectProperty(propertyIRI);


        for (OWLClass child : reasoner.getObjectPropertyRanges(property, true).getFlattened())
        {
            for (OWLClass child2 : reasoner.getSubClasses(child, false).getFlattened())
            {
                if (!reasoner.getSubClasses(child2).isEmpty() && reasoner.getSubClasses(child2, true).containsEntity(Nothing))
                {
                    //String str = child2.getIRI().getShortForm();
                    String str = getShortForm(child2.getIRI());
                    if (!list.contains(str))
                        list.add(str);
                }
            }
        }
        reasoner.dispose();
        return list;
    }

    public List<String> listOfIndividuals()
    {
        reasonerFactory = new StructuralReasonerFactory();// new JFactFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        ArrayList<String> list = new ArrayList<>();
        for (OWLNamedIndividual ind : ontology.getIndividualsInSignature())
        {
            list.add(getShortForm(ind.getIRI()));
        }
        return list;
    }

    public List<String> listOfInstancesForSpecifiedData(List<String> ObjectPropertiesList, List<String> DataPropertiesList)
    {
        OWLClass temp = ontologyManager.getOWLDataFactory().getOWLClass(ontologyIRI + "#" + "temp");
        Set<OWLClassExpression> arguments = new HashSet<>();
        //arguments.add(temp);

        for (String str : ObjectPropertiesList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - class

            OWLObjectProperty property = ontologyManager.getOWLDataFactory().getOWLObjectProperty(ontologyIRI + "#" + splitStr[0]);
            OWLClass cl = ontologyManager.getOWLDataFactory().getOWLClass(ontologyIRI + "#" + splitStr[1]);

            arguments.add(dataFactory.getOWLObjectAllValuesFrom(property, cl));

        }

        for (String str : DataPropertiesList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - restriction, 2 - value

            OWLDataProperty property = ontologyManager.getOWLDataFactory().getOWLDataProperty(getEntityIRI(splitStr[0]));
            OWLDatatype dataType = getOWLDatatype(splitStr[0]);
            OWLLiteral value = ontologyManager.getOWLDataFactory().getOWLLiteral(splitStr[2], dataType);
            Set<OWLFacetRestriction> facetRestriction = new HashSet<>();
            switch (splitStr[1])
            {
                case ">":
                    facetRestriction.add(ontologyManager.getOWLDataFactory().getOWLFacetRestriction(OWLFacet.MIN_EXCLUSIVE, value));
                    break;
                case "<":
                    facetRestriction.add(ontologyManager.getOWLDataFactory().getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, value));
                    break;
                case ">=":
                    facetRestriction.add(ontologyManager.getOWLDataFactory().getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, value));
                    break;
                case "<=":
                    facetRestriction.add(ontologyManager.getOWLDataFactory().getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, value));
                    break;
                case "=":
                    facetRestriction.add(ontologyManager.getOWLDataFactory().getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, value));
                    facetRestriction.add(ontologyManager.getOWLDataFactory().getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, value));
                    break;
            }

            OWLDatatypeRestriction restriction = ontologyManager.getOWLDataFactory().getOWLDatatypeRestriction(dataType,
                    facetRestriction);

            arguments.add(dataFactory.getOWLDataSomeValuesFrom(property, restriction));

        }
        OWLClassExpression expr = ontologyManager.getOWLDataFactory().getOWLObjectIntersectionOf(arguments);
        OWLAxiom tempAxiom = dataFactory.getOWLEquivalentClassesAxiom(temp, expr);

        ontologyManager.addAxiom(ontology, tempAxiom);

        //reasonerFactory = new JFactFactory(); //I don't know why but Fact++ is not working for Data Properties
        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        ArrayList<String> list = new ArrayList<>();


        for (OWLNamedIndividual ind : reasoner.getInstances(temp, true).getFlattened())
        {
            list.add(getShortForm(ind.getIRI()));
        }


        ontology.removeAxiom(tempAxiom);

        reasoner.dispose();
        return list;
    }


    public void addNewIndividual(String IndividualName, List<String> ObjectRestrictionList, List<String> DataRestrictionList)
    {
        OWLIndividual newIndividual = ontologyManager.getOWLDataFactory().getOWLNamedIndividual(getEntityIRI(IndividualName));
        Set<OWLAxiom> axioms = new HashSet<>();

        for (String str : ObjectRestrictionList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - class

            OWLObjectProperty property = ontologyManager.getOWLDataFactory().getOWLObjectProperty(getEntityIRI(splitStr[0]));
            OWLClass cl = ontologyManager.getOWLDataFactory().getOWLClass(getEntityIRI(splitStr[1]));
            OWLClassExpression classExpression = ontologyManager.getOWLDataFactory().getOWLObjectAllValuesFrom(property, cl);
            OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(classExpression, newIndividual);
            axioms.add(assertionAxiom);
        }

        for (String str : DataRestrictionList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - restriction, 2 - value

            OWLDataProperty property = ontologyManager.getOWLDataFactory().getOWLDataProperty(getEntityIRI(splitStr[0]));
            OWLDatatype datatype = getOWLDatatype(splitStr[0]);
            OWLLiteral value = ontologyManager.getOWLDataFactory().getOWLLiteral(splitStr[2], datatype);
            OWLDataPropertyAssertionAxiom assertionAxiom = ontologyManager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(property, newIndividual, value);
            axioms.add(assertionAxiom);
        }


        ontologyManager.addAxioms(ontology, axioms);

    }


    public List<String> listOfInstancesOfClass(OWLClass cl)
    {
        reasonerFactory = new JFactFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        ArrayList<String> list = new ArrayList<>();

        for (OWLNamedIndividual ind : reasoner.getInstances(cl, true).getFlattened())
        {
            list.add(getShortForm(ind.getIRI()));
        }

        reasoner.dispose();
        return list;
    }

    public boolean classSetIsSatisfiable(List<String> listOfClass)
    {
        OWLClass temp = ontologyManager.getOWLDataFactory().getOWLClass(getEntityIRI("temp"));
        Set<OWLAxiom> axioms = new HashSet<>();
        for(String str : listOfClass)
        {
            OWLClass cl = ontologyManager.getOWLDataFactory().getOWLClass(getEntityIRI(str));
            OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(temp,cl);
            axioms.add(subClassOfAxiom);
        }
        ontologyManager.addAxioms(ontology,axioms);
        boolean satisfable=false;
        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        if(reasoner.isSatisfiable(temp))
            satisfable=true;

        OWLEntityRemover remover = new OWLEntityRemover(ontology);
        temp.accept(remover);
        remover.getChanges().forEach(removeAxiom -> ontologyManager.applyChange(removeAxiom));
        remover.reset();

        return satisfable;
    }

    public boolean hasDataPropertyNumericRange(String property)
    {
        OWLDatatype data = getOWLDatatype(property);
        return (data.isInteger() || data.isFloat() || data.isBoolean());
    }

    private OWLDatatype getOWLDatatype(String dataProperty)
    {
        OWLDataProperty property = ontologyManager.getOWLDataFactory().getOWLDataProperty(getEntityIRI(dataProperty));
        Set<OWLDatatype> dataTypes = new HashSet<>();
        //long line
        ontology.dataPropertyRangeAxioms(property).forEach(owlDataPropertyRangeAxiom -> owlDataPropertyRangeAxiom.getRange().datatypesInSignature().forEach(dataTypes::add));
        OWLDatatype out = null;
        for (OWLDatatype data : dataTypes)
        {
            out = data;
        }
        return out;
    }

    private String getShortForm(IRI objectIRI)  //original getShortForm() is removing numbers
    {
        String str = objectIRI.getIRIString();
        return str.replaceAll(ontologyIRI.getIRIString() + "#", "");
    }

    private IRI getEntityIRI(String entity)
    {
        return IRI.create(ontologyIRI + "#" + entity);
    }

    public boolean containsEntity(String entity)
    {
        if (ontology != null)
            return ontology.containsEntityInSignature(getEntityIRI(entity));
        else
            return false;
    }

    public void deleteIndividual(String entity)
    {
        OWLEntityRemover remover = new OWLEntityRemover(ontology);
        OWLNamedIndividual ind = ontologyManager.getOWLDataFactory().getOWLNamedIndividual(getEntityIRI(entity));
        ind.accept(remover);
        remover.getChanges().forEach(removeAxiom -> ontologyManager.applyChange(removeAxiom));
        remover.reset();
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
