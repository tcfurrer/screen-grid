package app;

import javafx.application.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.*;
import static javafx.scene.paint.Color.*;

public final class AppMain extends Application {
    private static final double STARTING_GRID_SIZE = 50;
    private static final double STARTING_LINE_WIDTH = 5;

    public static void main(String[] args) {
        launch(args);
    }

    private Pane root;
    private Canvas canvas;
    private DoubleProperty gridSize, lineWidth;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.getIcons().add(new Image(getClass().getResource("screen-grid.png").toExternalForm()));

        gridSize = new SimpleDoubleProperty(STARTING_GRID_SIZE);
        lineWidth = new SimpleDoubleProperty(STARTING_LINE_WIDTH);

        root = new StackPane();
        canvas = new Canvas();
        root.getChildren().add(canvas);
        root.widthProperty().subscribe(this::resize);
        root.heightProperty().subscribe(this::resize);
        canvas.widthProperty().subscribe(this::draw);
        canvas.heightProperty().subscribe(this::draw);
        gridSize.subscribe(this::draw);
        lineWidth.subscribe(this::draw);

        var scene = new Scene(root, 600, 400, TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
        primaryStage.setMaximized(true);
        primaryStage.requestFocus();

        root.setOnScroll(this::scroll);
        root.setOnMouseClicked(e -> Platform.exit());
    }

    private void scroll(ScrollEvent e)
    {
        if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown())
        {
            var size = gridSize.get();
            if (e.getDeltaY() > 0) {
                size += 1;
            } else if (e.getDeltaY() < 0) {
                size -= 1;
            }
            if (size < 1) size = 1;
            gridSize.set(size);
        }
        else if (!e.isShiftDown() && e.isControlDown() && !e.isAltDown())
        {
            var size = lineWidth.get();
            if (e.getDeltaY() > 0) {
                size += 1;
            } else if (e.getDeltaY() < 0) {
                size -= 1;
            }
            if (size < 1) size = 1;
            lineWidth.set(size);
        }
    }

    private void resize()
    {
        canvas.setWidth(root.getWidth());
        canvas.setHeight(root.getHeight());
    }

    private void draw()
    {
        var width = canvas.getWidth();
        var height = canvas.getHeight();

        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,width,height);
        gc.setFill(Color.rgb(0,0,0,1.0/255));
        gc.fillRect(0,0,width,height);
        gc.setStroke(BLACK);
        gc.setLineWidth(lineWidth.get());

        // Horizontal lines
        var size = gridSize.get();
        for (var y=0.0; y<height; y+=size)
        {
            gc.strokeLine(0,y,width,y);
        }

        // Vertical lines
        for (var x=0.0; x<width; x+=size)
        {
            gc.strokeLine(x,0,x,height);
        }
    }
}
