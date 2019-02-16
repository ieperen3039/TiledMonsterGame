package NG;

import NG.ActionHandling.MouseTools.DefaultMouseTool;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Version;
import NG.Entities.Cube;
import NG.GameMap.*;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Material;
import NG.Rendering.MatrixStack.SGL;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shaders.MaterialShader;
import NG.Rendering.Shapes.FileShapes;
import NG.ScreenOverlay.Frames.Components.*;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.SToolBar;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import NG.Tools.Vectors;
import org.joml.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import static NG.Tools.Vectors.toVector2i;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapEditor {
    private static final Version EDITOR_VERSION = new Version(0, 2);
    private static final String MAP_FILE_EXTENSION = "mgm";
    private static final int BUTTON_MIN_WIDTH = 200;
    private static final int BUTTON_MIN_HEIGHT = 80;

    private final RenderLoop renderloop;
    private final DecoyGame game;
    private final JFileChooser saveMapDialog;
    private final JFileChooser loadTileDialog;
    private final JFileChooser loadMapDialog;

    public MapEditor() {
        Settings settings = new Settings();
        settings.ISOMETRIC_VIEW = true;
        settings.DYNAMIC_SHADOW_RESOLUTION = 0;
        settings.STATIC_SHADOW_RESOLUTION = 0;

        renderloop = new RenderLoop(settings.TARGET_FPS) {
            @Override // override exception handler
            protected void exceptionHandler(Exception ex) {
                display(ex);
            }
        };

        game = new DecoyGame("MonsterGame Map designer", renderloop, settings);

        loadMapDialog = new JFileChooser(Directory.savedMaps.getDirectory());
        loadMapDialog.setDialogTitle("Load map");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Map files", MAP_FILE_EXTENSION);
        loadMapDialog.setApproveButtonText("Load");
        loadMapDialog.setFileFilter(filter);

        saveMapDialog = new JFileChooser(Directory.savedMaps.getDirectory());
        saveMapDialog.setDialogTitle("Save map");
        saveMapDialog.setApproveButtonText("Save");

        loadTileDialog = new JFileChooser(Directory.mapTileModels.getDirectory());
        loadTileDialog.setDialogTitle("Load Tiles");
        FileNameExtensionFilter filter2 = new FileNameExtensionFilter("Tile files", "obj");
        loadTileDialog.setApproveButtonText("Load");
        loadTileDialog.setFileFilter(filter2);
    }

    public void init() throws Exception {
        game.init();
        renderloop.init(game);
        renderloop.addHudItem(game.gui()::draw);

        GLFWWindow window = game.window();
        GUIManager gui = game.gui();
        SToolBar files = getFileToolbar(window);
        gui.setToolBar(files);

        searchTiles(Directory.mapTileModels.getPath());

        BigArrow bigArrow = new BigArrow(new Vector3f());
        game.entities().addEntity(bigArrow);

        game.inputHandling().addMousePositionListener((xPos, yPos) -> {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            Vectors.windowCoordToRay(game, xPos, window.getHeight() - yPos, origin, direction);
            GameMap map = game.map();
            Vector3f position = map.intersectWithRay(origin, direction);

            Vector3i coordinate = map.getCoordinate(position);
            position.set(map.getPosition(coordinate.x, coordinate.y));
            position.z = coordinate.z + 3;

            map.setHighlights(toVector2i(coordinate));
            bigArrow.setPosition(position);
        });

        game.inputHandling().setMouseTool(new CoordinatePopupTool());

        game.lights().addDirectionalLight(new Vector3f(1, 1, 1), Color4f.WHITE, 0.5f);
    }

    private static void searchTiles(Path path) {
        File file = path.toFile();
        Logger.INFO.print("Searching in " + file + " for map tiles");
        FileMapTileReader.readDirectory(file);
    }

    private SToolBar getFileToolbar(GLFWWindow window) {
        SToolBar mainMenu = new SToolBar(game, false);
        window.addSizeChangeListener(() -> mainMenu.setSize(window.getWidth(), 0));

        mainMenu.addButton("Create new Map", this::createNew, 250);
        mainMenu.addButton("Load Map", this::loadMap, BUTTON_MIN_WIDTH);
        mainMenu.addButton("Save Map", this::saveMap, BUTTON_MIN_WIDTH);
        mainMenu.addButton("Load Tiles", this::loadTiles, BUTTON_MIN_WIDTH);

        mainMenu.addSeparator();
        mainMenu.addButton("Exit editor", renderloop::stopLoop, BUTTON_MIN_WIDTH);

        return mainMenu;
    }

    private void loadTiles() {
        int result = showDialog(loadTileDialog);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File[] selectedFiles = loadTileDialog.getSelectedFiles();
        int[] inc = new int[]{0};

        int nrOfFiles = selectedFiles.length;
        if (nrOfFiles > 0) {
            Supplier<String> progressBar = () -> "Loading tile " + inc[0] + "/" + nrOfFiles;
            Logger.printOnline(progressBar);

            for (File file : selectedFiles) {
                FileMapTileReader.readDirectory(file);
                inc[0]++;
            }

            Logger.removeOnlinePrint(progressBar);
        }
    }

    private void saveMap() {
        int result = showDialog(saveMapDialog);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File selectedFile = saveMapDialog.getSelectedFile();

        if (selectedFile != null) {
            String name = selectedFile.getAbsolutePath();
            boolean hasExtension = name.endsWith(MAP_FILE_EXTENSION);
            String nameWithExtension = name + "." + MAP_FILE_EXTENSION;

            Supplier<String> saveNotify = () -> "Saving file " + name + "...";

            new Thread(() -> {
                Logger.printOnline(saveNotify);

                try (FileOutputStream fileOut = hasExtension ?
                        new FileOutputStream(selectedFile) :
                        new FileOutputStream(nameWithExtension)
                ) {
                    DataOutput output = new DataOutputStream(fileOut);

                    EDITOR_VERSION.writeToFile(output);
                    game.writeStateToFile(output);

                    Logger.INFO.print("Saved file " + (hasExtension ? selectedFile : nameWithExtension));

                } catch (IOException e) {
                    display(e);
                }

                Logger.removeOnlinePrint(saveNotify);
            }, "Save map thread").start();
        }
    }

    private static void display(Exception e) {
        Logger.ERROR.print(e);
        String[] title = {
                "I Blame Menno", "You're holding it wrong", "This title is at random",
                "You can't blame me for this", "Something Happened", "Oops!", "stuff's broke lol",
                "Look at what you have done", "Please ignore the following message", "Congratulations!"
        };
        int rng = Toolbox.random.nextInt(title.length);

        JOptionPane.showMessageDialog(null, e.getClass() + ":\n" + e.getMessage(), title[rng], JOptionPane.ERROR_MESSAGE);
    }

    private void loadMap() {
        SwingUtilities.invokeLater(() -> {
            int result = showDialog(loadMapDialog);

            if (result != JFileChooser.APPROVE_OPTION) return;
            File selectedFile = loadMapDialog.getSelectedFile();

            if (selectedFile != null) {
                Supplier<String> loadNotify = () -> "Loading file " + selectedFile + "...";
                Logger.printOnline(loadNotify);

                try {
                    FileInputStream fs = new FileInputStream(selectedFile);
                    DataInput input = new DataInputStream(fs);

                    Version fileVersion = new Version(input);
                    Logger.INFO.print("Reading file " + selectedFile + " with version " + fileVersion);

                    game.readStateFromFile(input);

                    Vector2ic size = game.map().getSize();
                    setCameraToMiddle(size.x(), size.y());
                    Logger.INFO.print("Loaded map of size " + Vectors.toString(size));

                } catch (IOException e) {
                    display(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Logger.removeOnlinePrint(loadNotify);
            }
        });
    }

    private int showDialog(JFileChooser dialog) {
        game.window().setMinimized(true);
        renderloop.pause();

        int result = dialog.showSaveDialog(null);

        renderloop.unPause();
        game.window().setMinimized(false);
        return result;
    }

    private void createNew() {
        SFrame newMapFrame = new SFrame("New Map Settings", 200, 200, true);
        MapGeneratorMod generator = new SimpleMapGenerator(Toolbox.random.nextInt());
        Supplier<String> processDisplay = () -> "Generating heightmap : " + generator.heightmapProgress() * 100 + "%";
        Logger.printOnline(processDisplay);

        final int ROWS = 10;
        final int COLS = 3;
        Vector2i mpos = new Vector2i(1, 0);

        SPanel mainPanel = new SPanel(COLS, ROWS);
        mainPanel.add(new SFiller(10, 10), new Vector2i(0, 0));
        mainPanel.add(new SFiller(10, 10), new Vector2i(COLS - 1, ROWS - 1));

        // size selection
        SPanel sizeSelection = new SPanel(0, 0, 4, 1, false, false);
        sizeSelection.add(new STextArea("Size", 0, true), new Vector2i(0, 0));
        SDropDown xSizeSelector = new SDropDown(game, 100, 60, 1, "16", "32", "64", "128");
        sizeSelection.add(xSizeSelector, new Vector2i(1, 0));
        sizeSelection.add(new STextArea("X", 0, true), new Vector2i(2, 0));
        SDropDown ySizeSelector = new SDropDown(game, 100, 60, 1, "16", "32", "64", "128");
        sizeSelection.add(ySizeSelector, new Vector2i(3, 0));
        mainPanel.add(sizeSelection, mpos.add(0, 1));

        // other properties
        Map<String, Integer> properties = generator.getProperties();
        for (String prop : properties.keySet()) {
            int initialValue = properties.get(prop);
            mainPanel.add(
                    new SModifiableIntegerPanel(i -> properties.put(prop, i), prop, initialValue),
                    mpos.add(0, 1)
            );
        }

        // generate button
        mainPanel.add(new SFiller(0, 50), mpos.add(0, 1));
        SButton generate = new SButton("Generate", BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        mainPanel.add(generate, mpos.add(0, 1));


        generate.addLeftClickListener(() -> {
            Logger.printOnline(processDisplay);
            // initialize generator
            generator.init(game);
            int xSize = Integer.parseInt(xSizeSelector.getSelected());
            int ySize = Integer.parseInt(ySizeSelector.getSelected());
            generator.setXSize(xSize + 1); // heightmap is 1 larger
            generator.setYSize(ySize + 1);

            boolean success = TileThemeSet.PLAIN.loadAndWait(game);
            assert success;

            game.map().generateNew(generator); // hangs

            setCameraToMiddle(xSize, ySize);
            Logger.removeOnlinePrint(processDisplay);

            newMapFrame.dispose();
        });

        newMapFrame.setMainPanel(mainPanel);
        game.gui().addFrame(newMapFrame);
    }

    private void setCameraToMiddle(int xSize, int ySize) {
        Vector3f cameraFocus = game.map().getPosition(new Vector2f(xSize / 2f, ySize / 2f));
        // set camera to middle of map
        float initialZoom = (xSize + ySize);
        Vector3f cameraEye = new Vector3f(cameraFocus).add(-initialZoom, -initialZoom * 0.8f, initialZoom);
        game.camera().set(cameraFocus, cameraEye);
    }

    public void start() {
        game.window().open();
        renderloop.run();
        game.inputHandling().cleanup();
        game.window().close();
    }

    public static void main(String[] args) {
        try {
            MapEditor mapEditor = new MapEditor();
            mapEditor.init();
            mapEditor.start();
            Logger.INFO.print("Editor has stopped.");

        } catch (Exception ex) {
            display(ex);
        }
    }

    private class BigArrow extends Cube {

        private static final float SIZE = 2;

        public BigArrow(Vector3f position) {
            super(position);
        }

        @Override
        public void draw(SGL gl) {
            gl.pushMatrix();
            {
                gl.translate(position);
                gl.scale(SIZE, SIZE, -SIZE);

                if (gl.getShader() instanceof MaterialShader) {
                    ((MaterialShader) gl.getShader()).setMaterial(Material.ROUGH, Color4f.RED);
                }

                gl.render(FileShapes.ARROW, this);
            }
            gl.popMatrix();
        }

        public void setPosition(Vector3fc targetPosition) {
            position.set(targetPosition);
        }
    }

    private class CoordinatePopupTool extends DefaultMouseTool {
        @Override
        public void apply(Vector2fc position) {
            GameMap map = game.map();
            Vector3i coordinate = map.getCoordinate(new Vector3f(position, 0));
            int x = coordinate.x;
            int y = coordinate.y;

            SFrame window = new SFrame("Tile at (" + x + ", " + y + ")");

            if (map instanceof TileMap) {
                TileMap tileMap = (TileMap) map;

                MapTile.Instance tileData = tileMap.getTileData(x, y);
                SPanel elements = new SPanel(1, 5);

                STextArea typeDist = new STextArea("Type: " + tileData.type.name, BUTTON_MIN_HEIGHT, true);
                elements.add(typeDist, new Vector2i(0, 0));

                // TODO make this live
                STextArea heightDisp = new STextArea("Height: " + map.getHeightAt(x, y), BUTTON_MIN_HEIGHT, true);
                elements.add(heightDisp, new Vector2i(0, 1));


                Runnable switchAction = () -> {
                    MapTile.Instance instance = tileData.cycle(1);
                    tileMap.setTile(x, y, instance);
                    typeDist.setText("Type: " + tileData.type.name);
                    heightDisp.setText("Height: " + map.getHeightAt(x, y));
                };
                elements.add(new SButton("Cycle tile type", switchAction, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT), new Vector2i(0, 3));

                window.setMainPanel(elements);
            }

            window.pack();
            game.gui().addFrame(window);
        }
    }
}
