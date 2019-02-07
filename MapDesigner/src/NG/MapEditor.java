package NG;

import NG.DataStructures.Generic.Color4f;
import NG.Engine.Version;
import NG.GameMap.GameMap;
import NG.GameMap.MapGeneratorMod;
import NG.GameMap.SimpleMapGenerator;
import NG.GameMap.TileDirectoryReader;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.ScreenOverlay.Frames.Components.*;
import NG.ScreenOverlay.Frames.GUIManager;
import NG.ScreenOverlay.SToolBar;
import NG.Settings.Settings;
import NG.Tools.Directory;
import NG.Tools.Logger;
import NG.Tools.Toolbox;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapEditor {
    private static final String MAP_FILE_EXTENSION = ".mgm";
    private static final int BUTTON_MIN_WIDTH = 300;
    private static final int BUTTON_MIN_HEIGHT = 50;

    private final RenderLoop renderloop;
    private final DecoyGame game;

    public MapEditor() {
        Settings settings = new Settings();
        settings.ISOMETRIC_VIEW = true;
        settings.DYNAMIC_SHADOW_RESOLUTION = 0;
        settings.STATIC_SHADOW_RESOLUTION = 0;

        game = new DecoyGame("MonsterGame Map designer", settings);
        renderloop = new RenderLoop(settings.TARGET_FPS);
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

        game.lights().addDirectionalLight(new Vector3f(1, 1, 1), Color4f.WHITE, 0.5f);
    }

    private static void searchTiles(Path path) {
        File file = path.toFile();
        Logger.INFO.print("Searching in " + file + " for map tiles");
        TileDirectoryReader.readDirectory(file);
    }

    private SToolBar getFileToolbar(GLFWWindow window) {
        SToolBar mainMenu = new SToolBar(game);
        window.addSizeChangeListener(() -> mainMenu.setSize(window.getWidth(), 0));

        mainMenu.addButton("Create new Map", this::createNew, BUTTON_MIN_WIDTH);
        mainMenu.addButton("Load Map", this::loadMap, BUTTON_MIN_WIDTH);
        mainMenu.addButton("Save Map", this::saveMap, BUTTON_MIN_WIDTH);

        mainMenu.addSeparator();
        mainMenu.addButton("Exit editor", renderloop::stopLoop, BUTTON_MIN_WIDTH);

        return mainMenu;
    }

    private void saveMap() {
        Frame saveFrame = new Frame();
        FileDialog fileDialog = new FileDialog(saveFrame, "Save Map", FileDialog.SAVE);
        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(MAP_FILE_EXTENSION));
        fileDialog.setDirectory(Directory.workDirectory().toString());
        fileDialog.setVisible(true);
        String selectedFile = fileDialog.getFile();

        if (selectedFile != null) {
            try {
                GameMap map = game.map();

                FileOutputStream out = new FileOutputStream(selectedFile);
                DataOutput output = new DataOutputStream(out);

                game.getVersion().writeToFile(output);
                map.writeToFile(output);

            } catch (IOException e) {
                Logger.ERROR.print(e);
            }
        }
    }

    private void loadMap() {
        Frame loadFrame = new Frame();
        FileDialog fileDialog = new FileDialog(loadFrame, "Load Map", FileDialog.LOAD);
        fileDialog.setFilenameFilter((dir, name) -> name.endsWith(MAP_FILE_EXTENSION));
        fileDialog.setDirectory(Directory.workDirectory().toString());
        fileDialog.setVisible(true);
        String selectedFile = fileDialog.getFile();

        if (selectedFile != null) {
            try {
                loadFrame.dispose();
                GameMap map = game.map();

                FileInputStream in = new FileInputStream(selectedFile);
                DataInput input = new DataInputStream(in);

                Version fileVersion = Version.getFromInputStream(input);
                if (!fileVersion.equals(game.getVersion())) {
                    Logger.INFO.print("Reading file with version " + fileVersion);
                }

                map.readFromFile(input);

            } catch (IOException e) {
                Logger.ERROR.print(e);
            }
        }
    }

    private void createNew() {
        SFrame newMapFrame = new SFrame("New Map Settings", 200, 200, true);
        MapGeneratorMod generator = new SimpleMapGenerator(Toolbox.random.nextInt());

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
                    new ModifiableIntegerPanel(i -> properties.put(prop, i), prop, initialValue),
                    mpos.add(0, 1)
            );
        }

        // generate button
        mainPanel.add(new SFiller(0, 50), mpos.add(0, 1));
        SButton generate = new SButton("Generate", BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        mainPanel.add(generate, mpos.add(0, 1));


        generate.addLeftClickListener(() -> {
            // initialize generator
            generator.init(game);
            int xSize = Integer.parseInt(xSizeSelector.getSelected());
            int ySize = Integer.parseInt(ySizeSelector.getSelected());
            generator.setXSize(xSize + 1); // heightmap is 1 larger
            generator.setYSize(ySize + 1);

            game.map().generateNew(generator);
            // set camera to middle of map
            Vector3f cameraFocus = game.map().getPosition(new Vector2f(xSize / 2f, ySize / 2f));
            float initialZoom = (xSize + ySize);
            Vector3f cameraEye = new Vector3f(cameraFocus).add(-initialZoom, -initialZoom * 0.8f, initialZoom);
            game.camera().set(cameraFocus, cameraEye);

            newMapFrame.dispose();
        });

        newMapFrame.setMainPanel(mainPanel);
        game.gui().addFrame(newMapFrame);
    }

    private void start() {
        game.window().open();
        renderloop.run();
    }

    public static void main(String[] args) throws Exception {
        MapEditor mapEditor = new MapEditor();
        mapEditor.init();
        mapEditor.start();
    }

    private static class ModifiableIntegerPanel extends SPanel {
        private static final int ADD_BUTTON_HEIGHT = 50;
        private static final int ADD_BUTTON_WIDTH = 80;
        private static final int VALUE_SIZE = 150;
        private final Consumer<Integer> onUpdate;
        private final STextArea valueDisplay;

        private int value;

        public ModifiableIntegerPanel(Consumer<Integer> onUpdate, String name, int initialValue) {
            super(8, 1);
            this.onUpdate = onUpdate;
            this.valueDisplay = new STextArea(String.valueOf(initialValue), ADD_BUTTON_HEIGHT, VALUE_SIZE, false);
            this.value = initialValue;

            add(new STextArea(name, 0, true), new Vector2i(0, 0));
            add(new SButton("-100", () -> addToValue(-100), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(1, 0));
            add(new SButton("-10", () -> addToValue(-10), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(2, 0));
            add(new SButton("-1", () -> addToValue(-1), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(3, 0));
            add(valueDisplay, new Vector2i(4, 0));
            add(new SButton("+1", () -> addToValue(1), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(5, 0));
            add(new SButton("+10", () -> addToValue(10), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(6, 0));
            add(new SButton("+100", () -> addToValue(100), ADD_BUTTON_WIDTH, ADD_BUTTON_HEIGHT), new Vector2i(7, 0));
        }

        private void addToValue(Integer i) {
            value += i;
            onUpdate.accept(value);
            valueDisplay.setText(String.valueOf(value));
        }
    }
}
