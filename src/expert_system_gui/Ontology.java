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
import org.semanticweb.owlapi.vocab.OWLFacet;
//import uk.ac.manchester.cs.jfact.*;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;


public class Ontology
{
    private OWLOntology ontology;

    private OWLReasoner reasoner;

    private OWLReasonerFactory reasonerFactory;

    private OWLDataFactory dataFactory;

    private IRI ontologyIRI;

    private final OWLOntologyManager ontologyManager;

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
        dataFactory = ontologyManager.getOWLDataFactory();
    }

    public void saveOntology() throws OWLOntologyStorageException
    {
        if (ontology != null)
        {
            ontologyManager.saveOntology(ontology);
        }
    }

    public void saveOntologyToFile(File file) throws OWLOntologyStorageException
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
        printOntologyToTreeView(root, dataFactory.getOWLThing());
        reasoner.dispose();
    }

    private void printOntologyToTreeView(TreeItem<String> root, OWLClass cl)
    {
        if (reasoner.isSatisfiable(cl))
        {
            int i = 0;
            Iterator<OWLClass> classIterator = reasoner.subClasses(cl, true).iterator();
            while (classIterator.hasNext())
            {
                OWLClass subClass = classIterator.next();

                if (!subClass.equals(cl) && reasoner.isSatisfiable(subClass))
                {
                    String str = getShortForm(subClass.getIRI());
                    root.getChildren().add(new TreeItem<>(str));
                    printOntologyToTreeView(root.getChildren().get(i), subClass);
                    i++;

                }
            }
        }
    }

    public List<String> listOfObjectProperties()
    {
        reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        OWLObjectProperty topProperty = dataFactory.getOWLTopObjectProperty();
        OWLObjectProperty bottomProperty = dataFactory.getOWLBottomObjectProperty();
        ArrayList<String> list = new ArrayList<>();

        reasoner.subObjectProperties(topProperty, false).forEach(subObjectProperty ->
        {
            if (!reasoner.getSubObjectProperties(subObjectProperty).isEmpty() && reasoner.getSubObjectProperties(subObjectProperty, true).containsEntity(bottomProperty))
            {
                String str = getShortForm(subObjectProperty.getNamedProperty().getIRI());
                if (!list.contains(str))
                    list.add(str);
            }
        });

        reasoner.dispose();
        return list;
    }

    public List<String> listOfDataProperties()
    {
        reasonerFactory = new StructuralReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        OWLDataProperty topProperty = dataFactory.getOWLTopDataProperty();
        OWLDataProperty bottomProperty = dataFactory.getOWLBottomDataProperty();
        ArrayList<String> list = new ArrayList<>();

        reasoner.subDataProperties(topProperty, false).forEach(subDataProperty ->
        {
            if (!reasoner.getSubDataProperties(subDataProperty).isEmpty() && reasoner.getSubDataProperties(subDataProperty, true).containsEntity(bottomProperty))
            {
                //String str = child.getNamedProperty().getIRI().getShortForm();
                String str = getShortForm(subDataProperty.getIRI());
                if (!list.contains(str))
                    list.add(str);
            }
        });

        reasoner.dispose();
        return list;
    }

    public List<String> listOfObjectPropertiesOfIndividual(String Individual)
    {
        OWLNamedIndividual ind = dataFactory.getOWLNamedIndividual(getEntityIRI(Individual));
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
        OWLNamedIndividual ind = dataFactory.getOWLNamedIndividual(getEntityIRI(Individual));
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

        OWLClass Nothing = dataFactory.getOWLNothing();

        OWLObjectPropertyExpression property = dataFactory.getOWLObjectProperty(propertyIRI);

        reasoner.objectPropertyRanges(property, true).forEach(propertyRange -> reasoner.subClasses(propertyRange, false).forEach(subClass ->
        {
            if (!reasoner.getSubClasses(subClass).isEmpty() && reasoner.getSubClasses(subClass, true).containsEntity(Nothing))
            {
                //String str = child2.getIRI().getShortForm();
                String str = getShortForm(subClass.getIRI());
                if (!list.contains(str))
                    list.add(str);
            }
        }));
        reasoner.dispose();
        return list;
    }

    public List<String> listOfIndividuals()
    {
        reasonerFactory = new StructuralReasonerFactory();// new JFactFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        ArrayList<String> list = new ArrayList<>();
        ontology.individualsInSignature().forEach(individual -> list.add(getShortForm(individual.getIRI())));
        return list;
    }

    public List<String> listOfInstancesForSpecifiedData(List<String> ObjectPropertiesList, List<String> DataPropertiesList)
    {
        OWLClass temp = dataFactory.getOWLClass(ontologyIRI + "#" + "temp");
        Set<OWLClassExpression> arguments = new HashSet<>();
        //arguments.add(temp);

        for (String str : ObjectPropertiesList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - class

            OWLObjectProperty property = dataFactory.getOWLObjectProperty(ontologyIRI + "#" + splitStr[0]);
            OWLClass cl = dataFactory.getOWLClass(ontologyIRI + "#" + splitStr[1]);

            arguments.add(dataFactory.getOWLObjectAllValuesFrom(property, cl));

        }

        for (String str : DataPropertiesList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - restriction, 2 - value

            OWLDataProperty property = dataFactory.getOWLDataProperty(getEntityIRI(splitStr[0]));
            OWLDatatype dataType = getOWLDatatype(splitStr[0]);
            OWLLiteral value = dataFactory.getOWLLiteral(splitStr[2], dataType);
            Set<OWLFacetRestriction> facetRestriction = new HashSet<>();
            switch (splitStr[1])
            {
                case ">":
                    facetRestriction.add(dataFactory.getOWLFacetRestriction(OWLFacet.MIN_EXCLUSIVE, value));
                    break;
                case "<":
                    facetRestriction.add(dataFactory.getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, value));
                    break;
                case ">=":
                    facetRestriction.add(dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, value));
                    break;
                case "<=":
                    facetRestriction.add(dataFactory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, value));
                    break;
                case "=":
                    facetRestriction.add(dataFactory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, value));
                    facetRestriction.add(dataFactory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, value));
                    break;
            }

            OWLDatatypeRestriction restriction = dataFactory.getOWLDatatypeRestriction(dataType,
                    facetRestriction);

            arguments.add(dataFactory.getOWLDataSomeValuesFrom(property, restriction));

        }
        OWLClassExpression expr = dataFactory.getOWLObjectIntersectionOf(arguments);
        OWLAxiom tempAxiom = dataFactory.getOWLEquivalentClassesAxiom(temp, expr);

        ontologyManager.addAxiom(ontology, tempAxiom);

        //reasonerFactory = new JFactFactory(); //I don't know why but Fact++ is not working for Data Properties
        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        ArrayList<String> list = new ArrayList<>();

        reasoner.instances(temp,true).forEach(individual -> list.add(getShortForm(individual.getIRI())));

        ontology.removeAxiom(tempAxiom);

        reasoner.dispose();
        return list;
    }


    public void addNewIndividual(String IndividualName, List<String> ObjectRestrictionList, List<String> DataRestrictionList)
    {
        OWLIndividual newIndividual = dataFactory.getOWLNamedIndividual(getEntityIRI(IndividualName));
        Set<OWLAxiom> axioms = new HashSet<>();

        for (String str : ObjectRestrictionList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - class

            OWLObjectProperty property = dataFactory.getOWLObjectProperty(getEntityIRI(splitStr[0]));
            OWLClass cl = dataFactory.getOWLClass(getEntityIRI(splitStr[1]));
            OWLClassExpression classExpression = dataFactory.getOWLObjectAllValuesFrom(property, cl);
            OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(classExpression, newIndividual);
            axioms.add(assertionAxiom);
        }

        for (String str : DataRestrictionList)
        {
            String[] splitStr = str.trim().split("\\s+");//0 - property, 1 - restriction, 2 - value

            OWLDataProperty property = dataFactory.getOWLDataProperty(getEntityIRI(splitStr[0]));
            OWLDatatype datatype = getOWLDatatype(splitStr[0]);
            OWLLiteral value = dataFactory.getOWLLiteral(splitStr[2], datatype);
            OWLDataPropertyAssertionAxiom assertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(property, newIndividual, value);
            axioms.add(assertionAxiom);
        }

        Stream<OWLAxiom> axiomStream = axioms.stream();
        ontologyManager.addAxioms(ontology, axiomStream);

    }


    public List<String> listOfInstancesOfClass(OWLClass cl)
    {
        //reasonerFactory = new JFactFactory();
        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        ArrayList<String> list = new ArrayList<>();

        reasoner.instances(cl,true).forEach(individual ->list.add(getShortForm(individual.getIRI())));

        reasoner.dispose();
        return list;
    }

    public boolean isClassSetSatisfiable(List<String> listOfClass)
    {
        OWLClass temp = dataFactory.getOWLClass(getEntityIRI("temp"));
        Set<OWLAxiom> axioms = new HashSet<>();
        for (String str : listOfClass)
        {
            OWLClass cl = dataFactory.getOWLClass(getEntityIRI(str));
            OWLSubClassOfAxiom subClassOfAxiom = dataFactory.getOWLSubClassOfAxiom(temp, cl);
            axioms.add(subClassOfAxiom);
        }

        Stream<OWLAxiom> axiomStream = axioms.stream();

        ontologyManager.addAxioms(ontology, axiomStream);
        boolean satisfable = false;
        reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        if (reasoner.isSatisfiable(temp))
            satisfable = true;

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
        OWLDataProperty property = dataFactory.getOWLDataProperty(getEntityIRI(dataProperty));
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
        return ontology != null && ontology.containsEntityInSignature(getEntityIRI(entity));
    }

    public boolean containsIndividual(String individual)
    {
        List<String> individualList = listOfIndividuals();
        return individualList.contains(individual);
    }

    public void deleteIndividual(String entity)
    {
        OWLEntityRemover remover = new OWLEntityRemover(ontology);
        OWLNamedIndividual ind = dataFactory.getOWLNamedIndividual(getEntityIRI(entity));
        ind.accept(remover);
        remover.getChanges().forEach(removeAxiom -> ontologyManager.applyChange(removeAxiom));
        remover.reset();
    }

    public OWLOntology getOntology()
    {
        return ontology;
    }

}