package expert_system_gui;


import com.microsoft.z3.*;
import javafx.scene.control.TreeItem;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ConstraintManager
{

    private Document document;
    private File file;
    private Element root;
    private HashMap<java.lang.String, java.lang.String> inputs;

    private Context context;


    public void loadConstraintsFromFile(File file) throws DocumentException
    {
        this.file = file;
        SAXReader reader = new SAXReader();
        document = reader.read(file);
        root = document.getRootElement();
    }

    public void saveConstraints() throws IOException
    {
        if (document != null)
        {
            FileWriter writer = new FileWriter(file);
            document.write(writer);
            writer.close();
        }
    }

    public Element getRoot()
    {
        return root;
    }

    public void saveConstraintsToFile(File file) throws IOException
    {
        if (document == null)
        {
            createEmptyDocument();
        }
        this.file = file;
        saveConstraints();
    }

    public TreeItem<AddConstraintController.Pair<Element, String>> getConstraintsTree()
    {
        TreeItem<AddConstraintController.Pair<Element, String>> rootItem = new TreeItem<>(new AddConstraintController.Pair<Element, String>(root, root.getName()));
        Iterator<String> constraints = getConstraintsList().iterator();
        while (constraints.hasNext())
        {
            String str = constraints.next();
            Element constraint = getConstraint(str);
            AddConstraintController.Pair<Element, String> constr = new AddConstraintController.Pair<Element, String>(constraint,str);
            TreeItem<AddConstraintController.Pair<Element, String>> constrItem = new TreeItem<>(constr);
            rootItem.getChildren().add(constrItem);
            Element node = constraint.element("nodes").element("node");
            if(node!=null)
            {
                String nodeName = node.attributeValue("name");
                AddConstraintController.Pair<Element, String> pair = new AddConstraintController.Pair<>(node, nodeName);
                TreeItem<AddConstraintController.Pair<Element, String>> child = new TreeItem<>(pair);
                constrItem.getChildren().add(child);
                getElementNodes(child);
            }
        }
        return rootItem;
    }

    private void getElementNodes(TreeItem<AddConstraintController.Pair<Element, String>> parent)
    {

        Iterator<Element> nodeIterator = parent.getValue().getKey().elements().iterator();
        while (nodeIterator.hasNext())
        {
            Element node = nodeIterator.next();

            Attribute attribute=node.attribute(0);
            String name = null;
            if (node.attribute(0).getName().equals("name"))
            {
                name = node.attribute(0).getValue();
            } else
            {
                String dataType = node.attribute("dataType").getValue();
                if (dataType.equals("node"))
                {
                    name="node";
                } else if (dataType.equals("variable") || dataType.equals("constant"))
                {
                    name=node.getText();
                }
            }

            AddConstraintController.Pair<Element, String> pair= new AddConstraintController.Pair<>(node,name);
            TreeItem<AddConstraintController.Pair<Element, String>> child = new TreeItem<>(pair);
            parent.getChildren().add(child);
            getElementNodes(child);
        }
    }


    public boolean isLoaded()
    {
        return document != null;
    }

    public void deleteElement(Element element)
    {
        element.detach();
    }

    public java.lang.String solve(java.lang.String constraintName, HashMap<java.lang.String, java.lang.String> inputs)
    {
        this.inputs = inputs;
        Element constraint = getConstraint(constraintName);

        HashMap<java.lang.String, java.lang.String> cfg = new HashMap<>();
        cfg.put("model", "true");
        context = new Context(cfg);


        Expr var = context.mkRealConst("x");

        Expr node = getNodeFormula(getConstraintNode(constraint));

        BoolExpr eq = context.mkEq(var, node);

        Solver solver = context.mkSolver();


        solver.add(eq);

        Status checkResult = solver.check();

        //System.out.println(checkResult.toString());


        // String str = solver.getModel().toString();

        //System.out.println(str);

        Expr res = solver.getModel().eval(node, false);

        java.lang.String result = null;
        try
        {
            result = ((RatNum) res).toDecimalString(3);
        } catch (ClassCastException e)
        {
            result = ((AlgebraicNum) res).toDecimal(3);
        }

        result = result.replace("?", "");

        result = getConstraintOutput(constraint).attribute("name").getValue() + " = " + result;

        System.out.println(result);

        return result;

    }

    public List<java.lang.String> getInputs(java.lang.String constraintName)
    {
        List<java.lang.String> list = new ArrayList<>();
        Iterator<Element> it = getConstraint(constraintName).element("inputs").elementIterator("input");
        while (it.hasNext())
        {
            Element constraint = it.next();
            java.lang.String name = constraint.attribute("name").getValue();
            list.add(name);
        }
        return list;
    }

    public List<java.lang.String> getOutputs(java.lang.String constraintName)
    {
        List<java.lang.String> list = new ArrayList<>();
        Iterator<Element> it = getConstraint(constraintName).element("outputs").elementIterator("output");
        while (it.hasNext())
        {
            Element constraint = it.next();
            java.lang.String name = constraint.attribute("name").getValue();
            list.add(name);
        }
        return list;
    }

    public java.lang.String getDescription(java.lang.String constraintName)
    {
        return getConstraint(constraintName).element("description").getText();
    }

    public List<java.lang.String> getConstraintsList()
    {
        List<java.lang.String> list = new ArrayList<>();
        Iterator<Element> it = root.elementIterator("constraint");
        while (it.hasNext())
        {
            Element constraint = it.next();
            java.lang.String name = constraint.attribute("name").getValue();
            list.add(name);
        }
        return list;
    }

    public void createEmptyDocument()
    {
        document = DocumentHelper.createDocument();
        document.addElement("constraints");
        root=document.getRootElement();
    }

    private Element getConstraintNode(Element constraint)
    {
        return constraint.element("nodes").element("node");
    }

    private Element getConstraintOutput(Element constraint)
    {
        return constraint.element("outputs").element("output");
    }

    private Element getConstraint(java.lang.String constraintName)
    {
        Iterator<Element> elementIterator = root.elementIterator("constraint");
        while (elementIterator.hasNext())
        {
            Element constraint = elementIterator.next();
            if (constraint.attribute("name").getValue().equals(constraintName))
            {
                return constraint;
            }
        }
        return null;
    }


    private Expr getNodeFormula(Element node)
    {
        Element firstElement = node.element("firstElement");
        Element secondElement = node.element("secondElement");

        Expr firstElementFormula = getElementFormula(firstElement);
        Expr secondElementFormula = getElementFormula(secondElement);
        java.lang.String formulaType = node.attribute("name").getValue();
        Expr nodeFormula = null;
        switch (formulaType)
        {
            case "division":
                nodeFormula = context.mkDiv((ArithExpr) firstElementFormula, (ArithExpr) secondElementFormula);
                break;
            case "multiplication":
                nodeFormula = context.mkMul((ArithExpr) firstElementFormula, (ArithExpr) secondElementFormula);
                break;
            case "addition":
                nodeFormula = context.mkAdd((ArithExpr) firstElementFormula, (ArithExpr) secondElementFormula);
                break;
            case "subtraction":
                nodeFormula = context.mkSub((ArithExpr) firstElementFormula, (ArithExpr) secondElementFormula);
                break;
            case "root":
                ArithExpr one = context.mkReal(1);
                ArithExpr degree = context.mkDiv(one, (ArithExpr) secondElementFormula);
                nodeFormula = context.mkPower((ArithExpr) firstElementFormula, degree);
                break;
            case "exponentiation":
                nodeFormula = context.mkPower((ArithExpr) firstElementFormula, (ArithExpr) secondElementFormula);
                break;
            default:
                //Todo: exception
        }
        return nodeFormula;
    }

    public boolean isEmpty()
    {
        return document==null;
    }

    private Expr getElementFormula(Element element)
    {
        java.lang.String dataType = element.attribute("dataType").getValue();
        if (dataType.equals("node"))
        {
            return getNodeFormula(element.element("node"));
        } else
        {
            java.lang.String text = element.getText();

            java.lang.String value = null;
            if (dataType.equals("variable"))
            {
                value = inputs.get(text);
            } else if (dataType.equals("constant"))
            {
                value = text;
            }

            if (value == null)
            {
                //TODO: exception
            }

            Expr formula = null;
            formula = context.mkReal(value);

            return formula;
        }
    }

}
