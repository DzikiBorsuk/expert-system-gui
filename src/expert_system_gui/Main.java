package expert_system_gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{

    private static Main mainInstance;


    public static Main getInstance() {
        return mainInstance;
    }


    public Ontology ontology;
    public ConstraintManager constraints;


    @Override
    public void start(Stage primaryStage) throws Exception
    {
        System.out.println(System.getProperty("java.library.path"));
        mainInstance = this;
        Parent root = FXMLLoader.load(getClass().getResource("main_window.fxml"));
        Scene scene = new Scene(root, 1280, 720);

        primaryStage.setTitle("OWL expert system");
        primaryStage.setScene(scene);
        primaryStage.show();

        ontology = new Ontology();
        constraints = new ConstraintManager();
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
