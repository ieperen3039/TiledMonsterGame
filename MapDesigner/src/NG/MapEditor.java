package NG;

import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Version;
import NG.GUIMenu.Frames.Components.*;
import NG.GUIMenu.Frames.GUIManager;
import NG.GUIMenu.Menu.MainMenu;
import NG.GUIMenu.SToolBar;
import NG.GameMap.*;
import NG.InputHandling.MouseToolCallbacks;
import NG.Rendering.GLFWWindow;
import NG.Rendering.Lights.GameLights;
import NG.Rendering.RenderLoop;
import NG.Rendering.Shaders.WorldBPShader;
import NG.Settings.Settings;
import NG.Tools.*;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapEditor {
    private static final Version EDITOR_VERSION = new Version(0, 3);
    private static final String MAP_FILE_EXTENSION = "mgm";
    public static final int BUTTON_MIN_WIDTH = 180;
    public static final int BUTTON_MIN_HEIGHT = 60;

    private final DualEditorTool dualEditorTool;

    private final RenderLoop renderloop;
    private final DecoyGame game;

    private final JFileChooser saveMapDialog;
    private final JFileChooser loadTileDialog;
    private final JFileChooser loadMapDialog;
    private BlockMap blockMap;
    private TileMap tileMap;

    public MapEditor() {
        Settings settings = new Settings();
        settings.ISOMETRIC_VIEW = true;
        settings.DYNAMIC_SHADOW_RESOLUTION = 0;
        settings.STATIC_SHADOW_RESOLUTION = 0;
        settings.DEBUG_SCREEN = true;

        renderloop = new RenderLoop(settings.TARGET_FPS) {
            @Override // override exception handler
            protected void exceptionHandler(Exception ex) {
                errorDialog(ex);
            }
        };

        game = new DecoyGame("MonsterGame Map designer", renderloop, settings, EDITOR_VERSION);
        blockMap = new BlockMap(0.5f, 0.1f, 0.1f);
        tileMap = new TileMap(Settings.CHUNK_SIZE);
        game.setGameMap(tileMap);

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
        FileNameExtensionFilter filter2 = new FileNameExtensionFilter("Tile description files", "txt");
        loadTileDialog.setApproveButtonText("Load");
        loadTileDialog.setFileFilter(filter2);

        dualEditorTool = new DualEditorTool(game, blockMap, tileMap);
    }

    public void init() throws Exception {
        game.init();
        blockMap.init(game);

        // world
        renderloop.renderSequence(new WorldBPShader())
                .add(gl -> game.get(GameLights.class).draw(gl))
                .add(gl -> blockMap.draw(gl))
                .add(gl -> tileMap.draw(gl))
        ;

        GUIManager gui = game.get(GUIManager.class);
        SToolBar files = getFileToolbar();
        gui.setToolBar(files);

        game.get(MouseToolCallbacks.class).setMouseTool(dualEditorTool);
        game.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1.5f, 4f), Color4f.WHITE, 0.4f);
    }

    private SToolBar getFileToolbar() {
        SToolBar mainMenu = new SToolBar(game, false);

        mainMenu.addButton("Generate Random", this::createNew, 250);
        mainMenu.addButton("Transform Tiles", null, 250);
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
                try {
                    inc[0]++;
                    MapTiles.readTileSetFile(null, file.toPath());

                } catch (IOException ex) {
                    Logger.ERROR.print(ex);
                }
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
                    DataOutputStream output = new DataOutputStream(fileOut);
                    Storable.write(output, game.get(AbstractMap.class));

                    Logger.INFO.print("Saved file " + (hasExtension ? selectedFile : nameWithExtension));

                } catch (IOException e) {
                    errorDialog(e);
                }

                Logger.removeOnlinePrint(saveNotify);
            }, "Save map thread").start();
        }
    }

    private void loadMap() {
        int result = showDialog(loadMapDialog);

        if (result != JFileChooser.APPROVE_OPTION) return;
        File selectedFile = loadMapDialog.getSelectedFile();

        if (selectedFile != null) {
            Supplier<String> loadNotify = () -> "Loading file " + selectedFile + "...";
            Logger.printOnline(loadNotify);

            try {
                game.loadMap(selectedFile);

                GameMap newMap = game.get(AbstractMap.class);

                if (newMap instanceof BlockMap) {
                    blockMap = (BlockMap) newMap;
                }

                Vector2ic size = newMap.getSize();
                size.x();
                size.y();
                MainMenu.centerCamera(game.get(Camera.class), newMap);
                Logger.INFO.print("Loaded map of size " + Vectors.toString(size));

            } catch (Exception e) {
                errorDialog(e);
            }

            Logger.removeOnlinePrint(loadNotify);
        }
    }

    private int showDialog(JFileChooser dialog) {
        GLFWWindow window = game.get(GLFWWindow.class);
        window.setMinimized(true);
        renderloop.pause();

        int result = dialog.showDialog(null, null);

        renderloop.unPause();
        window.setMinimized(false);
        return result;
    }

    public void errorDialog(Exception ex) {
        GLFWWindow window = game.get(GLFWWindow.class);
        window.setMinimized(true);
        renderloop.pause();

        Toolbox.display(new Exception(ex));

        renderloop.unPause();
        window.setMinimized(false);
    }

    private void createNew() {
        SFrame newMapFrame = new SFrame("New Map Settings", 200, 200, true);
        MapGeneratorMod generator = new SimpleMapGenerator(Toolbox.random.nextInt());
        GUIManager gui = game.get(GUIManager.class);

        // collect map generator properties
        Map<String, Integer> properties = generator.getProperties();
        SPanel propPanel = new SPanel(1, properties.size());
        Vector2i mpos = new Vector2i(0, -1);

        for (String prop : properties.keySet()) {
            int initialValue = properties.get(prop);
            propPanel.add(
                    new SModifiableIntegerPanel(i -> properties.put(prop, i), prop, initialValue),
                    mpos.add(0, 1)
            );
        }

        Supplier<String> processDisplay = () -> "Generating heightmap : " + generator.heightmapProgress() * 100 + "%";

        // buttons
        SButton generate = new SButton("Generate", BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        SDropDown xSizeSelector = new SDropDown(gui, 100, 60, 1, "16", "32", "64", "128");
        SDropDown ySizeSelector = new SDropDown(gui, 100, 60, 1, "16", "32", "64", "128");

        generate.addLeftClickListener(() -> {
            Logger.printOnline(processDisplay);
            // initialize generator
            generator.init(game);
            int xSize = Integer.parseInt(xSizeSelector.getSelected());
            int ySize = Integer.parseInt(ySizeSelector.getSelected());
            generator.setXSize(xSize + 1); // heightmap is 1 larger
            generator.setYSize(ySize + 1);

            TileThemeSet.PLAIN.load();

            blockMap.generateNew(generator);
            tileMap.generateNew(new CopyGenerator(blockMap));

            MainMenu.centerCamera(game.get(Camera.class), tileMap);
            Logger.removeOnlinePrint(processDisplay);

            newMapFrame.dispose();
        });

        // GUI structure
        SPanel mainPanel = new SPanel(3, 3);
        mainPanel.add(new SFiller(10, 10), new Vector2i(0, 0));
        mainPanel.add(new SFiller(10, 10), new Vector2i(2, 2));

        mainPanel.add(SPanel.column(
                SPanel.row(
                        new STextArea("Size", 0),
                        xSizeSelector,
                        new STextArea("X", 0),
                        ySizeSelector
                ),
                propPanel,
                new SFiller(0, 50),
                generate
        ), new Vector2i(2, 2));

        newMapFrame.setMainPanel(mainPanel);
        gui.addFrame(newMapFrame);
    }

    public void start() {
        GLFWWindow window = game.get(GLFWWindow.class);
        window.open();
        renderloop.run();
        window.close();
        game.get(MouseToolCallbacks.class).cleanup();
    }

    public static void main(String[] args) {
        try {
            MapEditor mapEditor = new MapEditor();
            mapEditor.init();
            mapEditor.start();
            Logger.INFO.print("Editor has stopped.");

        } catch (Exception ex) {
            Toolbox.display(ex);
        }
    }
}
