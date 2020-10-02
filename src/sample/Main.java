package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Stack;

public class Main extends Application {
    Rectangle selectedpiece;
    VBox source, destination, auxiliary;
    Paint temp;
    private Stack<Rectangle> undoStack;
    private Stack<VBox> moveStack;
    private static final int NUM_RECTANGLE = 7;
    private final Color []rectColors = {Color.rgb(251,235,251), Color.rgb(242,195,243), Color.rgb(231,143,233), Color.rgb(219,88,222),
            Color.rgb(173,34,176), Color.rgb(124,24,126), Color.rgb(82,16,84), Color.rgb(57,12,58)};
    private GridPane root;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        InitializeGame(mainStage);
    }
    public void InitializeGameContent(){
        undoStack = new Stack<Rectangle>();
        moveStack = new Stack<VBox>();
        Button btnUndo = new Button("Undo");
        btnUndo.setOnAction(actionEvent -> undo());
        root.add(btnUndo,1,1);

        Button Solve = new Button("Solve");
        Solve.setOnAction(actionEvent -> solver(NUM_RECTANGLE,source,auxiliary,destination));
        root.add(Solve,2,1);

        Button restart = new Button("Restart");
        restart.setOnAction(actionEvent -> restart());
        root.add(restart,3,1);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("3","4","5","6","7","8"); // number of discs between 3 - 8
        comboBox.setValue("5");
        //comboBox.setEditable(true);
        root.add(comboBox, 4, 1);
    }

    private void restart(){
        source.getChildren().clear();
        destination.getChildren().clear();
        auxiliary.getChildren().clear();

        InitializeTowers();
    }

    private void InitializeTowers(){
        source = new VBox();
        source.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
        source.setAlignment(Pos.BOTTOM_CENTER);
        source.setPrefHeight(980);
        source.setPrefWidth(500);
        source.setSpacing(1);
        source.setPadding(new Insets(0, 20, 10, 20));
        DragAndDropHandler(source);

        destination = new VBox();
        destination.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        destination.setAlignment(Pos.BOTTOM_CENTER);
        destination.setPrefHeight(500);
        destination.setPrefWidth(500);
        destination.setSpacing(1);
        destination.setPadding(new Insets(0, 20, 10, 20));
        DragAndDropHandler(destination);

        auxiliary = new VBox();
        auxiliary.setBackground(new Background(new BackgroundFill(Color.LIGHTSALMON, CornerRadii.EMPTY, Insets.EMPTY)));
        auxiliary.setAlignment(Pos.BOTTOM_CENTER);
        auxiliary.setPrefHeight(500);
        auxiliary.setPrefWidth(500);
        auxiliary.setSpacing(1);
        auxiliary.setPadding(new Insets(0, 20, 10, 20));
        DragAndDropHandler(auxiliary);

        for (int i = 0;i < NUM_RECTANGLE;i++){
            Rectangle disc = new Rectangle(250 + 50*i, 50);
            disc.setFill(rectColors[i]);
            disc.setStroke(Color.BLACK);
            source.getChildren().add(disc);
        }

        root.getChildren().add(source);
        root.getColumnConstraints().add(new ColumnConstraints(500));
        GridPane.setColumnIndex(source, 0);
        root.getChildren().add(destination);
        GridPane.setColumnIndex(destination, 5);
        root.getChildren().add(auxiliary);
        GridPane.setColumnIndex(auxiliary, 10);
    }

    private void InitializeGame(Stage mainStage){
        mainStage = new Stage();
        root =  new GridPane();
        Scene scene = new Scene(root,1500,900);
        mainStage.setScene(scene);
        mainStage.setTitle("Tower of Hanoi");
        mainStage.show();

        InitializeGameContent();
        InitializeTowers();

    }




    private void undo() {
        if(!undoStack.isEmpty()){
            Node node = undoStack.pop();
            VBox StartTower = moveStack.pop();
            ((VBox)StartTower).getChildren().remove(0);
            VBox DestTower = moveStack.pop();
            ((VBox)DestTower).getChildren().add(0,node);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No moves to undo.");
            alert.showAndWait();
        }
    }

    public void solver(int discs, VBox Source, VBox Auxiliary, VBox Destination){
        if (discs == 1){
            Node node = Source.getChildren().remove(0);
            Destination.getChildren().add(0,node);
        }
        else {
            solver(discs - 1, Source, Destination, Auxiliary);
            Node node = Source.getChildren().remove(0);
            Destination.getChildren().add(0,node);
            solver(discs - 1, Auxiliary, Source, Destination);
        }
    }

    public void DragAndDropHandler(Node tower) {
        tower.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        final Object source = event.getSource();
                        if (source instanceof VBox){
                            ObservableList<Node> discs = ((VBox) source).getChildrenUnmodifiable();
                            if (discs.size() != 0){
                                Node topDisc = discs.get(0);
                                Dragboard db = ((VBox) source).startDragAndDrop(TransferMode.MOVE);
                                ClipboardContent content = new ClipboardContent();
                                content.putString(String.valueOf(((Rectangle) topDisc).getWidth())); // put the width of the top disc in the content
                                content.putHtml(String.valueOf(((Rectangle) topDisc).getFill())); // put the color of the top disc in the content (I'm using the HTML string)
                                content.putUrl(String.valueOf(((Rectangle) topDisc).getId())); // put the id of the top disc in the content (I'm using the URL string)
                                db.setContent(content); //set the clip board content in the dragboard object
                                event.consume();
                            }
                        }
                    }
                }
        );

        tower.setOnDragOver(
                new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent dragEvent) {
                        double sourceTopDisc = Double.parseDouble(dragEvent.getDragboard().getString());
                        double destinationTopDisc = 999999;
                        ObservableList<Node> discs = ((VBox)tower).getChildrenUnmodifiable(); // get list of the discs in the destination tower
                        if (discs.size()!=0){ // if there is any
                            destinationTopDisc = ((Rectangle)discs.get(0)).getWidth(); // assign the width of the top disc for validation
                        }
                        if (dragEvent.getGestureSource() != tower && sourceTopDisc<destinationTopDisc) { // if the destination is not the source and the top disc of source smaller of the destination top disc
                            dragEvent.acceptTransferModes(TransferMode.MOVE); // accept transfer
                        }
                        dragEvent.consume();
                    }
                }
        );

        tower.setOnDragDropped(new EventHandler<DragEvent>() { // if drag has dropped on the destination tower
            public void handle(DragEvent dragEvent) {
                Dragboard db = dragEvent.getDragboard();
                boolean success = false; // to inform successful drop
                if (db.hasString() && dragEvent.getGestureSource() != tower) {
                    Rectangle disc = new Rectangle(Double.parseDouble(db.getString()), 50);
                    disc.setFill(Color.valueOf(db.getHtml()));
                    disc.setStroke(Color.BLACK);
                    moveStack.push((VBox) dragEvent.getGestureSource());
                    moveStack.push((VBox)tower);
                    undoStack.push(disc);
                    ((VBox)tower).getChildren().add(0,disc);
                    success = true;
                }
                dragEvent.setDropCompleted(success); // inform successful drop
                dragEvent.consume();
            }});

        tower.setOnDragDone(new EventHandler<DragEvent>() { // when the drop is done remove the top disc from the source tower
            public void handle(DragEvent dragEvent) {
                if (dragEvent.getTransferMode() == TransferMode.MOVE) {
                    ((VBox)tower).getChildren().remove(0);
                }
                dragEvent.consume();
            }});


    }
}