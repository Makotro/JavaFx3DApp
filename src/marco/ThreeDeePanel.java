package marco;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThreeDeePanel extends JPanel {

    private static final int MOVE_DURATION = 1000;
    /**
     *
     */
    private static final long serialVersionUID = 6267050563321395234L;
    private ObjectProperty<Shape3D> selectedShape = new SimpleObjectProperty<>();
    private StackPane root = new StackPane();
    // private AnchorPane root = new AnchorPane();
    private int shapeName = 0;
    // x start position for the first rollerPin
    private double rollerX = -100;
    private Group group = new Group();
    private Group conveyorGroup = new Group();
    private JFXPanel jfp = new JFXPanel();
    private ComboBox<Integer> cb = new ComboBox<>();
    private Shape3D pallet;
    private Shape3D tire;
    private Shape3D tireHole;
    private VBox vbox;
    private Label selected = new Label();
    private static final double PALLET_X_OFFSCREEN_RIGHT = 707;
    private static final double PALLET_X_OFFSCREEN_LEFT = -607;
    private static final double PALLET_X_ONSCREEN_CENTER = 85;
    private boolean moving;
    // number and size of rolling pins

    // radius of rollers, set in increments of times 2
    private int rollerPinRadius = 8;
    // amount of roller pints, make less if rollerPinRadius increases, radius 8
    // => count 40, radius 16 => count 20
    private int rollerPinCount = (int) ((double) 20 * (((double) 8 / (double) rollerPinRadius)));
    // spacing between rollers, increase if count is reduced, count 40 =>
    // spacing 20, count 20 => spacing 40
    private int rollerPinSpacing = (int) ((double) 20 / (((double) 8 / (double) rollerPinRadius)));

    /**
     * Create the panel.
     */
    public ThreeDeePanel() {
        initGUI();
    }

    /**
     * simple enum to determine the shape of a model
     *
     */
    private enum MyShape {
        ELLIPSE, BOX
    }

    private void initGUI() {

        setLayout(new BorderLayout());
        BackgroundFill bgf;
        bgf = new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
        Background bg = new Background(bgf);
        root.setBackground(bg);
        vbox = new VBox();
        vbox.setTranslateX(450);
        vbox.setVisible(false);

        generateConveyorModel();

        genSliders(vbox);
        vbox.setMaxWidth(300);

        Scene scene = new Scene(root, 0, 0, true, SceneAntialiasing.BALANCED);
        initCamera(vbox);
        vbox.requestFocus();
        vbox.getChildren().add(selected);
        jfp.setScene(scene);
        add(jfp, BorderLayout.CENTER);

    }

    public void setDebugBoxVisible(boolean visible) {
        vbox.setVisible(visible);
    }
    private void initCamera(VBox vbox) {
        SubScene sS = new SubScene(group, 1000, 700, true, SceneAntialiasing.BALANCED);
        root.getChildren().add(sS);
        root.getChildren().add(vbox);

        // vbox.setTranslateX(500);
        // rec.setFill(Color.BISQUE);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(Color.rgb(255, 255, 255, 0.6));
        // Creating Point Light
        PointLight point = new PointLight();
        point.setColor(Color.rgb(255, 255, 255, 1));
        point.setLayoutX(-400);
        point.setLayoutY(-300);
        point.setTranslateZ(-100);
        // point.getScope().add(null);
        group.getChildren().addAll(point, ambient, conveyorGroup);
        Camera camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(1500);
        camera.setTranslateZ(-1175);

        // camera.setFieldOfView(180.0);

        conveyorGroup.setTranslateX(-100);
        conveyorGroup.getTransforms().add(new Rotate(-60.0, Rotate.X_AXIS));

        sS.setCamera(camera);
        Group cameraGroup = new Group(camera);
        // cameraGroup.setTranslateZ(-975);
        // cameraGroup.getChildren().add(camera);
        group.getChildren().add(cameraGroup);
        // scene.getAntiAliasing().BALANCED;
    }

    /**
     * Generates the 3D model for the conveyor
     */
    private void generateConveyorModel() {
        // x, y, width, height, depth
        // 1st sidebar
        makeShape(89, 134, 1, 400, 30, 70, MyShape.BOX, Color.LIGHTSEAGREEN);
        // roller pins
        for (int i = 0; i < rollerPinCount; i++) {
            makeRollerPin(0, 0, 300, rollerPinRadius, 300, MyShape.ELLIPSE);
        }
        // pallet
        pallet = makeShape(PALLET_X_OFFSCREEN_RIGHT, 0, -29, 230, 230, 40, MyShape.BOX, Color.GREY);
        // init tire and bind its x-coordinate to the pallet
        tire = makeShape(pallet.getTranslateX(), 0, -65, 40, 100, 40, MyShape.ELLIPSE, Color.BLACK);
        tire.translateXProperty().bind(pallet.translateXProperty());
        tire.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        // init tirehole and bind its x-coordinate to the tire
        tireHole = makeShape(pallet.getTranslateX(), 0, -65, 41, 60, 40, MyShape.ELLIPSE, Color.DARKGRAY);
        tireHole.translateXProperty().bind(pallet.translateXProperty());
        tireHole.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        tireHole.visibleProperty().bind(tire.visibleProperty());
        // sidebar
        makeShape(89, -134, 1, 400, 30, 70, MyShape.BOX, Color.LIGHTSEAGREEN);
    }

    /**
     * debug box for manipulating the model in three dimensions, not needed in
     * production
     *
     * @param vbox the vbox to add sliders to
     */
    private void genSliders(VBox vbox) {
        HBox boxRotateGroup = getNewSlider("Rotate all: ");
        Slider sldrGroupRotate = (Slider) boxRotateGroup.getUserData();
        sldrGroupRotate.valueProperty().addListener((observable, oldValue, newValue) -> {
            conveyorGroup.setRotationAxis(Rotate.Y_AXIS);

            conveyorGroup.setRotate(newValue.doubleValue());
            System.out.println("X: " + newValue);
        });



        HBox boxRotateX = getNewSlider("Rotate x: ");
        Slider sliderRotateX = (Slider) boxRotateX.getUserData();

        sliderRotateX.valueProperty().addListener((observable, oldValue, newValue) -> {
            Rotate rotX = (Rotate) selectedShape.get().getTransforms().get(0);
            rotX.setAngle(newValue.doubleValue());
            System.out.println("X: " + newValue);
        });


        HBox boxRotateY = getNewSlider("Rotate y: ");
        Slider sliderRotateY = (Slider) boxRotateY.getUserData();

        sliderRotateY.valueProperty().addListener((observable, oldValue, newValue) -> {

            Rotate rotY = (Rotate) selectedShape.get().getTransforms().get(1);
            rotY.setAngle(newValue.doubleValue());
            System.out.println("Y: " + newValue);
        });


        HBox boxRotateZ = getNewSlider("Rotate z: ");
        Slider sliderRotateZ = (Slider) boxRotateZ.getUserData();
        sliderRotateZ.valueProperty().addListener((observable, oldValue, newValue) -> {
            Rotate rotZ = (Rotate) selectedShape.get().getTransforms().get(2);
            rotZ.setAngle(newValue.doubleValue());
            System.out.println("Z: " + newValue);
        });

        HBox boxPosX = getNewSlider("Position X: ");
        Slider sliderPosX = (Slider) boxPosX.getUserData();
        sliderPosX.valueProperty().addListener((observable, oldValue, newValue) -> {

            Shape3D rx = selectedShape.get();
            rx.setTranslateX(newValue.doubleValue());
            System.out.println("x:" + newValue);
        });

        HBox boxPositionY = getNewSlider("Position Y: ");
        Slider sliderPosY = (Slider) boxPositionY.getUserData();
        sliderPosY.valueProperty().addListener((observable, oldValue, newValue) -> {
            Shape3D ry = selectedShape.get();
            ry.setTranslateY(newValue.doubleValue());
            System.out.println("y:" + newValue);
        });

        HBox boxPositionZ = getNewSlider("Position Z: ");
        Slider sliderPosZ = (Slider) boxPositionZ.getUserData();
        sliderPosZ.valueProperty().addListener((observable, oldValue, newValue) -> {
            Shape3D rz = selectedShape.get();
            rz.setTranslateZ(newValue.doubleValue());
            System.out.println("z:" + newValue);
        });

        vbox.getChildren().addAll(boxRotateGroup, boxRotateX, boxRotateY, boxRotateZ, boxPosX, boxPositionY, boxPositionZ, cb);
        root.getChildren().stream().filter(e -> (e instanceof Shape3D)).map(e -> ((Shape3D) e))
                .collect(Collectors.toList());
        System.err.println(cb.getItems().size());
        cb.getSelectionModel().selectedItemProperty()
                .addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> {


                    Optional<Node> optional = conveyorGroup.getChildren().stream().filter(p1 -> p1 instanceof Shape3D)
                            .filter(p2 -> p2.getId().equals(newValue.toString())).findFirst();
                    Shape3D shape = null;
                    if (optional.isPresent()) {
                        shape = (Shape3D) optional.get();
                    }

                    System.out.println(shape);
                    selectedShape.set(shape);
                });

        selectedShape.addListener((observable, oldValue, newValue) -> {
            sliderRotateX.setValue(((Rotate) newValue.getTransforms().get(0)).getAngle());
            sliderRotateY.setValue(((Rotate) newValue.getTransforms().get(1)).getAngle());
            sliderRotateZ.setValue(((Rotate) newValue.getTransforms().get(2)).getAngle());
            sliderPosX.setValue(newValue.getTranslateX());
            sliderPosY.setValue(newValue.getTranslateY());
            sliderPosZ.setValue(newValue.getTranslateZ());
            selected.setText("Selected shape is a: " + newValue.toString());
        });
    }

    private HBox getNewSlider(String text) {
        HBox hbox = new HBox();
        Label label = new Label(text);
        Slider slider = new Slider();
        slider.setMin(-1000);
        slider.setMax(1000);
        slider.setValue(0);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(1);
        hbox.getChildren().addAll(label, slider);
        hbox.setUserData(slider);
        return hbox;
    }

    /**
     * for creating simple 3 shape
     * @param x x position of this shape
     * @param y y position of this shape
     * @param z z position of this shape
     * @param width width of this shape
     * @param height height of this shape
     * @param depth depth of this shape
     * @param shape shape of this shape
     * @param grey color of this shape
     * @return
     */
    private Shape3D makeShape(double x, double y, double z, double width, double height, double depth, MyShape shape,
                              Color grey) {
        Shape3D c = getShape3D(width, height, depth, shape);

        c.setId(String.valueOf(++shapeName));
        cb.getItems().add(shapeName);
        conveyorGroup.getChildren().addAll(c);
        c.setDepthTest(DepthTest.ENABLE);
        // Button rgn = new Button();
        // c.setMaterial(greyMaterial);

        // Creating PhongMaterial
        PhongMaterial material = new PhongMaterial();
        // Diffuse Color
        material.setDiffuseColor(grey);
        // Specular Color
        material.setSpecularColor(Color.RED);
        c.setMaterial(material);
        c.setTranslateX(x);
        c.setTranslateY(y);
        c.setTranslateZ(z);
        Rotate rotX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotY = new Rotate(0, Rotate.Y_AXIS);
        Rotate rotZ = new Rotate(0, Rotate.Z_AXIS);
        Translate transX = new Translate();
        // transX.setX(rollerX );
        // rollerX += 50;
        Translate transY = new Translate();
        Translate transZ = new Translate();
        selectedShape.set(c);

        c.setOnMouseClicked(e -> selectedShape.set((Shape3D) e.getTarget()));
        c.getTransforms().add(rotX);
        c.getTransforms().add(rotY);
        c.getTransforms().add(rotZ);
        c.getTransforms().add(transX);
        c.getTransforms().add(transY);
        c.getTransforms().add(transZ);
        return c;
    }

    /**
     * determine which shape to make
     * @param width width of shape or in case of ELLIPSE, it's height
     * @param height height of shape or in case of ELLIPSE, it's radius
     * @param depth depth of shape, only if a box
     * @param shape which shape to make
     * @return
     */
    private Shape3D getShape3D(double width, double height, double depth, MyShape shape) {
        Shape3D c = null;
        switch (shape) {
            case ELLIPSE:
                c = new Cylinder(height, width);
                break;
            case BOX:
                c = new Box(width, height, depth);
                break;

            default:
                break;
        }
        return c;
    }

    /**
     * Generates a shape i nthe given x/y coordinates with the given dimensions
     *
     * @param x
     *            x coordinate position for this shape
     * @param y
     *            y coordinate position for this shape
     * @param width
     *            the width of this shape
     * @param height
     *            the height of this shape
     * @param depth
     *            the depth of this shape
     * @param shape
     *            the shape of this shape, compared from MyShape enum
     * @return the generated shape
     */
    private Shape3D makeRollerPin(double x, double y, double width, double height, double depth, MyShape shape) {

        Shape3D c = getShape3D(width, height, depth, shape);

        c.setId(String.valueOf(shapeName++));
        cb.getItems().add(shapeName);
        conveyorGroup.getChildren().addAll(c);

        c.setDepthTest(DepthTest.ENABLE);
        // Button rgn = new Button();
        // c.setMaterial(greyMaterial);

        // Creating PhongMaterial
        PhongMaterial material = new PhongMaterial();
        // Diffuse Color
        material.setDiffuseColor(Color.GRAY);
        // Specular Color
        material.setSpecularColor(Color.BLACK);

        c.setMaterial(material);

        c.setLayoutX(x);
        c.setLayoutY(y);
        Rotate rotX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotY = new Rotate(0, Rotate.Y_AXIS);
        Rotate rotZ = new Rotate(0, Rotate.Z_AXIS);
        Translate transX = new Translate();
        transX.setX(rollerX);
        rollerX += rollerPinSpacing;
        Translate transY = new Translate();
        Translate transZ = new Translate();
        selectedShape.set(c);

        c.setOnMouseClicked(e -> selectedShape.set((Shape3D) e.getTarget()));
        c.getTransforms().add(rotX);
        c.getTransforms().add(rotY);
        c.getTransforms().add(rotZ);
        c.getTransforms().add(transX);
        c.getTransforms().add(transY);
        c.getTransforms().add(transZ);
        return c;
    }

    /**
     * sets only the pallet visible
     * @param vis set visible
     */
    public void setPalletVisibile(boolean vis) {
        pallet.setVisible(vis);
    }

    public void setTireVisible(boolean vis) {
        tire.setVisible(vis);
        tireHole.setVisible(vis);
    }

    /**
     * sets only the tire visible
     * @param vis set visible
     */
    public void setPalletTireVisible(boolean vis) {
        pallet.setVisible(vis);
        tire.setVisible(vis);
        tireHole.setVisible(vis);
    }

    /**
     * Updates the state if the model
     * @param palletStr name of the pallet
     * @param tireStr name of the tire
     */
    public void updateState(String palletStr, String tireStr) {
        if (moving) {
            System.out.println("stuff is moving");
            return;
        } else {
//			System.out.println("stuff is not moving, starting move");
        }
        double transX = pallet.getTranslateX();

        updateToolTips(palletStr, tireStr);
        if (transX == PALLET_X_OFFSCREEN_RIGHT && palletStr != null) {
            tire.setVisible(tireStr != null);
            String palletTUID = palletStr;
            pallet.setUserData(palletTUID);
            System.err.println("tu arrived, move in");
            movePalletIn();
        } else if (transX == PALLET_X_ONSCREEN_CENTER && palletStr == null) {
            pallet.setUserData(-1);
            System.err.println("empty cnv, send out");
            movePalletOut();
        } else if (transX == PALLET_X_ONSCREEN_CENTER) {
            // pallet already in position, check if new pallet
            String oldPalletTUID = (pallet.getUserData() == null ? "" : pallet.getUserData().toString());
            String newPalletTUID = palletStr;
            if (!oldPalletTUID.equals(newPalletTUID)) {
                // different id, move old out and new in
                movePalletOutIn(palletStr, tireStr);
                System.out.println("new thing on conveyor, move old out, new in");
            } else {
                tire.setVisible(tireStr != null);
//				System.err.println("same tu on cnv, do nothing");
                // same tu already present, do nothing
            }

        }

    }

    /**
     * moves the pallets off screen
     */
    public void movePalletOut() {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(pallet.translateXProperty(), PALLET_X_OFFSCREEN_LEFT);
        KeyFrame kf = new KeyFrame(Duration.millis(MOVE_DURATION), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(e -> {
            pallet.translateXProperty().set(PALLET_X_OFFSCREEN_RIGHT);
            moving = false;
        });
        moving = true;
        timeline.play();
    }

    /**
     * moves the pallet offscreen and back and sets the visbility of the tire depending on if a tireStr was given
     * @param palletStr name of the tire
     * @param tireStr name of the tire, if null -> invisible
     */
    public void movePalletOutIn(String palletStr, String tireStr) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(pallet.translateXProperty(), PALLET_X_OFFSCREEN_LEFT);
        KeyFrame kf = new KeyFrame(Duration.millis(MOVE_DURATION), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(e -> {
            pallet.translateXProperty().set(PALLET_X_OFFSCREEN_RIGHT);
            pallet.setUserData(palletStr.toString());
            tire.setVisible(tireStr != null);
            movePalletIn();
            moving = false;
        });
        moving = true;
        timeline.play();
    }

    /**
     * moves the pallet in from offscreen
     */
    public void movePalletIn() {
        if (!(pallet.getTranslateX() == PALLET_X_ONSCREEN_CENTER)) {
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(pallet.translateXProperty(), PALLET_X_ONSCREEN_CENTER);
            KeyFrame kf = new KeyFrame(Duration.millis(MOVE_DURATION), kv);
            timeline.getKeyFrames().add(kf);
            timeline.setOnFinished(e -> {
                moving = false;
            });
            moving = true;
            timeline.play();
        }
    }

    /**
     * set the initial state
     * @param palletStr if null, pallets is offscreen
     * @param tireStr if null, tire is invisible
     */
    public void initState(String palletStr, String tireStr) {
        if (palletStr != null) {
            tire.setVisible(tireStr != null);
            pallet.setTranslateX(PALLET_X_ONSCREEN_CENTER);
            pallet.setUserData(palletStr);
            updateToolTips(palletStr, tireStr);
        }

    }

    /**
     * updates the tooltips that are shown when hovering the object
     * @param palletStr the text for pallet
     * @param tireStr the text for tire
     */
    private void updateToolTips(String palletStr, String tireStr) {
        StringBuilder sb = new StringBuilder();
        if (palletStr != null) {
            sb.append(String.format("PalletString: %s \n", palletStr));
            Tooltip ttPallet = new Tooltip(sb.toString());
            Tooltip.install(pallet, ttPallet);
        }
        sb.setLength(0);
        if (tireStr != null) {
            sb.append(String.format("TireString: %s \n", tireStr));
            Tooltip ttTire = new Tooltip(sb.toString());
            Tooltip.install(tire, ttTire);

            Tooltip ttHole = new Tooltip(sb.toString());
            Tooltip.install(tireHole, ttHole);
        }

    }

}


