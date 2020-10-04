package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.Stack;

public class Main extends Application {
    VBox source, aux, dest; //The three towers for the game
    TextField NUM_MOVES_DESC;
    private Stack<Rectangle> undoRectStack, redoRectStack;
    private Stack<VBox> undoMoveStack, redoMoveStack;
    private ComboBox<String> DiscsNumChanger;
    private int NUM_RECTANGLE = 5; //Starts the game with 5 discs
    private int NUM_MOVES = 0; //Starts the game with 0 number of moves
    private final Color []rectColors = {Color.rgb(255,0,0),Color.rgb(255,128,0),Color.rgb(255,255,0),
            Color.rgb(128,255,0),Color.rgb(0,255,0),Color.rgb(0,255,128),
            Color.rgb(0,255,255),Color.rgb(0,128,255),Color.rgb(0,0,255),Color.rgb(127,0,255)};
    private GridPane root;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        InitializeGame(mainStage);
    }

    public void InitializeGameContent(){
        undoRectStack = new Stack<Rectangle>();
        undoMoveStack = new Stack<VBox>();
        Button btnUndo = new Button("Undo");
        btnUndo.setOnAction(actionEvent -> undo());

        redoRectStack = new Stack<Rectangle>();
        redoMoveStack = new Stack<VBox>();
        Button btnRedo = new Button("Redo");
        btnRedo.setOnAction(actionEvent -> redo());

        Button Solve = new Button("Solve");
        Solve.setOnAction(actionEvent -> solver(NUM_RECTANGLE,source, aux, dest));

        Button restart = new Button("Restart");
        restart.setOnAction(actionEvent -> restart());

        HBox box = new HBox(5);
        box.getChildren().addAll(btnUndo, btnRedo,Solve,restart);

        NUM_MOVES_DESC = new TextField();
        NUM_MOVES_DESC.setText("Move No.: " + NUM_MOVES);
        NUM_MOVES_DESC.setEditable(false);

        HBox box2 = new HBox(5);
        box2.getChildren().addAll(NUM_MOVES_DESC);
        root.add(box, 1, 2);
        root.add(box2, 1, 3);

        DiscsNumChanger = new ComboBox<String>();
        DiscsNumChanger.getItems().addAll("3","4","5","6","7","8");
        DiscsNumChanger.setValue("5");
        DiscsNumChanger.setPromptText("Number of discs: ");
        root.add(DiscsNumChanger, 1, 4);
        UpdateNumberOfDiscs();
    }

    private void restart(){
        source.getChildren().clear();
        aux.getChildren().clear();
        dest.getChildren().clear();
        NUM_MOVES = 0;

        InitializeTowers();
        UpdateNumberOfMoves();
        CheckIfGameEnds();
    }

    private void InitializeTowers(){
        source = new VBox();
        source.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        source.setAlignment(Pos.BOTTOM_CENTER);
        source.setPrefHeight(900);
        source.setPrefWidth(500);
        source.setStyle("-fx-border-style: solid;" + "-fx-border-width: 5;" + "-fx-border-color: gray");
        DragAndDropHandler(source);

        aux = new VBox();
        aux.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        aux.setAlignment(Pos.BOTTOM_CENTER);
        aux.setPrefWidth(500);
        aux.setStyle("-fx-border-style: solid;" + "-fx-border-width: 5;" + "-fx-border-color: gray");
        DragAndDropHandler(aux);

        dest = new VBox();
        dest.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        dest.setAlignment(Pos.BOTTOM_CENTER);
        dest.setPrefWidth(500);
        dest.setStyle("-fx-border-style: solid;" + "-fx-border-width: 5;" + "-fx-border-color: gray");
        DragAndDropHandler(dest);

        for (int i = 0;i < NUM_RECTANGLE;i++){
            Rectangle disc = new Rectangle(200 + 50*i, 50);
            disc.setFill(rectColors[i]);
            disc.setStroke(Color.BLACK);
            source.getChildren().add(disc);
        }

        root.getChildren().add(source);
        GridPane.setColumnIndex(source, 0);
        root.getChildren().add(aux);
        GridPane.setColumnIndex(aux, 1);
        root.getChildren().add(dest);
        GridPane.setColumnIndex(dest, 2);
    }

    private void InitializeGame(Stage mainStage){
        mainStage = new Stage();
        root =  new GridPane();
        Scene scene = new Scene(root,1500,800);
        mainStage.setScene(scene);
        mainStage.setTitle("Tower of Hanoi");
        mainStage.setResizable(false);
        mainStage.show();

        Label label = new Label ("Destination");
        label.setFont(new Font("Arial", 30));
        label.setTranslateX(175);
        root.add(label,2,1);

        Label label2 = new Label("Start");
        label2.setFont(new Font("Arial", 30));
        label2.setTranslateX(200);
        root.add(label2, 0,1);

        InitializeGameContent();
        InitializeTowers();
    }

    /*
     * The method is the implementation when the user clicks the undo button.
     * If there are moves to undo, the undoRectStack should be non-empty.
     * Since undoRectStack and undoMoveStack are stacks, the pop() method will give the information of the last move.
     * Using these information, undo() removes the latest moved disc from its sent tower and bring it to where it was previously in.
     * redoRectStack and redoMoveStack will store the undid disc and the change in tower for the redo function.
     * Since there is one less move, undo() updates the displayed number of moves done to the user.
     *
     * If there are no moves inside the undo stack, send an alert to the user.
     */
    private void undo() {
        if(!undoRectStack.isEmpty()){
            Node disc = undoRectStack.pop();
            VBox StartTower = undoMoveStack.pop();
            VBox DestTower = undoMoveStack.pop();

            ((VBox)StartTower).getChildren().remove(0);
            ((VBox)DestTower).getChildren().add(0,disc);
            redoRectStack.push((Rectangle) disc);
            redoMoveStack.push((VBox)DestTower);
            redoMoveStack.push((VBox)StartTower);
            NUM_MOVES--;
            UpdateNumberOfMoves();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No moves to undo.");
            alert.showAndWait();
        }
    }

    /*
     * The method is the implementation when the user clicks the undo button.
     * Since redoRectStack and redoMoveStack are stacks, the pop() method will give the information of the last made undo.
     * Using these information, redo() removes the latest moved disc from its sent tower and bring it to where it was previously in.
     * undoRectStack and undoMoveStack will store the redid disc and the change in tower for the redo function.
     * Since there is one less move, undo() updates the displayed number of moves done to the user.
     *
     * If there are no moves inside the undo stack, send an alert to the user.
     */
    private void redo() {
        if(!redoMoveStack.isEmpty()){
            Node node = redoRectStack.pop();
            undoRectStack.push((Rectangle) node);

            VBox redoDestTower = redoMoveStack.pop();
            VBox redoStartTower = redoMoveStack.pop();
            undoMoveStack.push((VBox)redoStartTower);
            undoMoveStack.push((VBox)redoDestTower);

            ((VBox)redoStartTower).getChildren().remove(0);
            ((VBox)redoDestTower).getChildren().add(0,node);
            NUM_MOVES++;
            UpdateNumberOfMoves();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No moves to redo.");
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
        CheckIfGameEnds2();
    }

    private void CheckIfGameEnds2(){
        if (dest.getChildren().size() == NUM_RECTANGLE){
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Game is done." +
                    "\nGame done in " + (int)(Math.pow(2,NUM_RECTANGLE)-1) + " moves, which is the optimal number of moves." + "\nGame will now be restarted.");
            alert.showAndWait();
            restart();
        }
    }

    //https://stackoverflow.com/questions/44413649/javafx-how-to-update-text-of-dynimically-created-textfields-inside-gridpane
    public void UpdateNumberOfMoves(){
        NUM_MOVES_DESC.setText("Moves No.: " + NUM_MOVES);
    }

    private void CheckIfGameEnds(){
        if (dest.getChildren().size() == NUM_RECTANGLE){
            if (NUM_MOVES + 1 ==  Math.pow(2,NUM_RECTANGLE)){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Game is done." +
                        "\nGame done in " + NUM_MOVES + " moves, which is the optimal number of moves." + "\nGame will now be restarted.");
                alert.showAndWait();
            }
            else  {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Game is done." +
                        "\nGame done in " + NUM_MOVES + " moves, which can be done in less moves." + "\nGame will now be restarted.");
                alert.showAndWait();
            }
            restart();
        }
    }
    //https://stackoverflow.com/questions/40838376/javafx-combobox-valueproperty-addlistenernew-changelistenerstring-progres
    /*
     *If the number of total discs is changed from the DiscsNumChanger comboBox,
     * setup a new game with the new total number of discs.
     */
    private void UpdateNumberOfDiscs(){
        DiscsNumChanger.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                if (!newValue.isEmpty()){
                    int chosen;
                    NUM_RECTANGLE = Integer.parseInt(newValue);
                    restart();
                }
            }
        });
    }

    public void DragAndDropHandler(Node tower) {
        /*
         * setOnDragDetected handles the event when a user initiates a drag motion on the mouse/mouse-pad.
         * If the clicked object is a disc inside one of the towers, it stores the information of that disc into the clipboard.
         * */
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
                        content.putString(String.valueOf(((Rectangle) topDisc).getWidth()));
                        content.putHtml(String.valueOf(((Rectangle) topDisc).getFill()));
                        db.setContent(content);
                        event.consume();
                    }
                }
            }});

        /*
         * setOnDragOver handles the event where the user drags the clicked disc into another tower.
         * startTowerTopDiscWidth is the width of the dragged disc.
         * destTowerTopDiscWidth is the width of the top-most disc where the user is dragging towards.
         * If the user has brought the disc into a different tower and the top-most disc of the new tower is wider than the dragged disc,
         * set the transfer mode of the disc into the new tower.
         *
         * It is necessary to initialize destTowerTopDiscWidth to be a number larger than the smallest disc in the game for the initial game state where the other two towers are empty.
         * Without initializing the value, the condition destTowerTopDiscWidth cannot be checked when dragging to an empty tower,
         * as there are no width to get using destTowerTopDiscWidth = ((Rectangle)discs.get(0)).getWidth()
         * */
        tower.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                double startTowerTopDiscWidth = Double.parseDouble(dragEvent.getDragboard().getString());
                double destTowerTopDiscWidth = 14051997;
                ObservableList<Node> discs = ((VBox)tower).getChildrenUnmodifiable();
                if (discs.size()!=0){
                    destTowerTopDiscWidth = ((Rectangle)discs.get(0)).getWidth();
                }
                if (dragEvent.getGestureSource() != tower && startTowerTopDiscWidth<destTowerTopDiscWidth) {
                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                }
                dragEvent.consume();
            }});

        /*
         * setOnDragDropped handles the event where the user drops a valid disc into a new tower.
         * setOnDragDropped takes the required information of the transferred disc from the clipboard and
         * sends the disc into the new tower.
         * If this is successful, setOnDragDropped will save the information of the moving disc and the direction where it is moving)
         * into the stack undoRectStack and undoMoveStack respectively.
         * setOnDragDropped also increases the number of moves by one and updates this information to the user.
         */
        tower.setOnDragDropped(new EventHandler<DragEvent>() { // if drag has dropped on the destination tower
            public void handle(DragEvent dragEvent) {
                Dragboard db = dragEvent.getDragboard();
                boolean success = false;
                if (db.hasString() && dragEvent.getGestureSource() != tower) {
                    Rectangle disc = new Rectangle(Double.parseDouble(db.getString()), 50);
                    disc.setFill(Color.valueOf(db.getHtml()));
                    disc.setStroke(Color.BLACK);
                    undoMoveStack.push((VBox) dragEvent.getGestureSource());
                    undoMoveStack.push((VBox)tower);
                    undoRectStack.push(disc);
                    ((VBox)tower).getChildren().add(0,disc);
                    NUM_MOVES++;
                    UpdateNumberOfMoves();

                    success = true;
                }
                dragEvent.setDropCompleted(success);
                dragEvent.consume();
            }});

        /*
         * When the dragging is completed, setOnDragDone will remove the moved disc from the source tower.
         * Then check if the move done will complete the game.
         */
        tower.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent dragEvent) {
                if (dragEvent.getTransferMode() == TransferMode.MOVE) {
                    ((VBox)tower).getChildren().remove(0);
                    CheckIfGameEnds();
                }
                dragEvent.consume();
            }});
    }
}