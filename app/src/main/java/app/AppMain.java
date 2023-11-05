package app;

import javafx.application.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.scene.*;
import static javafx.scene.paint.Color.*;

public final class AppMain extends Application {
    private static final double STARTING_GRID_SIZE = 50;
    private static final double STARTING_LINE_WIDTH = 5;
    private static final Point2D STARTING_ORIGIN = Point2D.ZERO;
    private static final double STARTING_BRIGHTNESS = 0;

    public static void main(String[] args) {
        launch(args);
    }

    private Pane root;
    private Canvas canvas;
    private DoubleProperty gridSize, lineWidth, brightness;
    private ObjectProperty<Point2D> origin;
    private Point2D dragStartPoint, dragStartOrigin;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.getIcons().add(new Image(getClass().getResource("screen-grid.png").toExternalForm()));

        gridSize = new SimpleDoubleProperty(STARTING_GRID_SIZE);
        lineWidth = new SimpleDoubleProperty(STARTING_LINE_WIDTH);
        origin = new SimpleObjectProperty<>(STARTING_ORIGIN);
        brightness = new SimpleDoubleProperty(STARTING_BRIGHTNESS);

        gridSize.subscribe(this::draw);
        lineWidth.subscribe(this::draw);
        origin.subscribe(this::draw);
        brightness.subscribe(this::draw);

        root = new StackPane();
        canvas = new Canvas();
        root.getChildren().add(canvas);
        root.widthProperty().subscribe(this::resize);
        root.heightProperty().subscribe(this::resize);
        canvas.widthProperty().subscribe(this::draw);
        canvas.heightProperty().subscribe(this::draw);

        var scene = new Scene(root, 600, 400, TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
        primaryStage.setMaximized(true);
        primaryStage.requestFocus();

        root.setOnScroll(this::scroll);
        root.setOnMousePressed(this::mousePress);
        root.setOnMouseDragged(this::mouseDrag);
        root.setOnMouseReleased(this::mouseRelease);
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
        gc.setStroke(Color.gray(brightness.get()));
        gc.setLineWidth(lineWidth.get());

        // Horizontal lines
        var size = gridSize.get();
        var yStart = origin.get().getY() % size;
        for (var y=yStart; y<height; y+=size)
        {
            gc.strokeLine(0,y,width,y);
        }

        // Vertical lines
        var xStart = origin.get().getX() % size;
        for (var x=xStart; x<width; x+=size)
        {
            gc.strokeLine(x,0,x,height);
        }
    }

    private void scroll(ScrollEvent e)
    {
        if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown())
        {
            var i = gridSize.get();
            if (e.getDeltaY() > 0) {
                i += 1;
            } else if (e.getDeltaY() < 0) {
                i -= 1;
            }
            i = Math.clamp(i, Math.max(lineWidth.get()*2.0, 4), 500);
            gridSize.set(i);
        }
        else if (!e.isShiftDown() && e.isControlDown() && !e.isAltDown())
        {
            var i = lineWidth.get();
            if (e.getDeltaY() > 0) {
                i += 1;
            } else if (e.getDeltaY() < 0) {
                i -= 1;
            }
            i = Math.clamp(i, 0.25, gridSize.get()/2.0);
            lineWidth.set(i);
        }
        else if (!e.isShiftDown() && !e.isControlDown() && e.isAltDown())
        {
            var i = brightness.get();
            if (e.getDeltaY() > 0) {
                i += 1.0/20;
            } else if (e.getDeltaY() < 0) {
                i -= 1.0/20;
            }
            i = Math.clamp(i, 0.0, 1.0);
            brightness.set(i);
        }
    }

    private void mousePress(MouseEvent e)
    {
        dragStartPoint = new Point2D(e.getScreenX(), e.getScreenY());
        dragStartOrigin = origin.get();
    }

    private void mouseRelease(MouseEvent e)
    {
        dragStartPoint = null;
        dragStartOrigin = null;
        if (e.isStillSincePress()) Platform.exit();
    }

    private void mouseDrag(MouseEvent e)
    {
        if (e.isStillSincePress()) return;
        if (!e.isShiftDown() && !e.isControlDown() && !e.isAltDown())
        {
            var dragPoint = new Point2D(e.getScreenX(), e.getScreenY());
            var delta = dragPoint.subtract(dragStartPoint);
            origin.set(dragStartOrigin.add(delta));
        }
    }
}
