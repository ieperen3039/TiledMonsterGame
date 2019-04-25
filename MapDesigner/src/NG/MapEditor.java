package NG;

import NG.Camera.Camera;
import NG.DataStructures.Generic.Color4f;
import NG.Engine.Version;
import NG.GUIMenu.Frames.Components.*;
import NG.GUIMenu.Frames.GUIManager;
import NG.GUIMenu.SToolBar;
import NG.GameMap.*;
import NG.GameState.GameLights;
import NG.InputHandling.MouseToolCallbacks;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Rendering.GLFWWindow;
import NG.Rendering.RenderLoop;
import NG.Settings.Settings;
import NG.Tools.*;
import org.joml.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Geert van Ieperen created on 6-2-2019.
 */
public class MapEditor {
    private static final Version EDITOR_VERSION = new Version(0, 2);
    private static final String MAP_FILE_EXTENSION = "mgm";
    private static final int BUTTON_MIN_WIDTH = 180;
    private static final int BUTTON_MIN_HEIGHT = 80;

    private final TileCycleTool tileCycleTool = new TileCycleTool();
    private final BlockModificationTool blockModificationTool = new BlockModificationTool();

    private final RenderLoop renderloop;
    private final DecoyGame game;

    private final JFileChooser saveMapDialog;
    private final JFileChooser loadTileDialog;
    private final JFileChooser loadMapDialog;
    private BlockMap blockMap;

    public MapEditor() {
        Settings settings = new Settings();
        settings.ISOMETRIC_VIEW = true;
        settings.DYNAMIC_SHADOW_RESOLUTION = 0;
        settings.STATIC_SHADOW_RESOLUTION = 0;

        renderloop = new RenderLoop(settings.TARGET_FPS) {
            @Override // override exception handler
            protected void exceptionHandler(Exception ex) {
                Toolbox.display(ex);
            }
        };

        game = new DecoyGame("MonsterGame Map designer", renderloop, settings);
        blockMap = new BlockMap();
        game.setGameMap(blockMap);

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
    }

    public void init() throws Exception {
        game.init();

        GLFWWindow window = game.get(GLFWWindow.class);
        GUIManager gui = game.get(GUIManager.class);
        SToolBar files = getFileToolbar(window);
        gui.setToolBar(files);

        game.get(MouseToolCallbacks.class).setMouseTool(blockModificationTool);
        game.get(GameLights.class).addDirectionalLight(new Vector3f(1, 1.5f, 4f), Color4f.WHITE, 0.4f);
    }

    private SToolBar getFileToolbar(GLFWWindow window) {
        SToolBar mainMenu = new SToolBar(game, false);

        mainMenu.addButton("Generate Random", this::createNew, 250);
        mainMenu.addButton("Transform Tiles", this::generateTileMap, 250);
        mainMenu.addButton("Load Map", this::loadMap, BUTTON_MIN_WIDTH);
        mainMenu.addButton("Save Map", this::saveMap, BUTTON_MIN_WIDTH);
        mainMenu.addButton("Load Tiles", this::loadTiles, BUTTON_MIN_WIDTH);

        mainMenu.addSeparator();
        mainMenu.addButton("Exit editor", renderloop::stopLoop, BUTTON_MIN_WIDTH);

        return mainMenu;
    }

    private void generateTileMap() {
        SFrame transformDialog = new SFrame("Tile map generator");

        SButton generate = new SButton("Generate Tilemap", () -> {
            TileThemeSet.PLAIN.load();
            int chunkSize = game.get(Settings.class).CHUNK_SIZE;
            GameMap newMap = new TileMap(chunkSize);
            try {
                newMap.init(game);
                newMap.generateNew(new CopyGenerator(blockMap));
                game.setGameMap(newMap);

                blockModificationTool.dispose();
                game.get(MouseToolCallbacks.class).setMouseTool(tileCycleTool);

            } catch (Exception ex) {
                Toolbox.display(ex);
            }
        }, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        generate.setGrowthPolicy(true, false);

        SButton back = new SButton("Back to Blockmap", () -> {
            game.setGameMap(blockMap);
            tileCycleTool.dispose();
            game.get(MouseToolCallbacks.class).setMouseTool(blockModificationTool);

        }, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
        back.setGrowthPolicy(true, false);

        transformDialog.setMainPanel(SPanel.column(
                generate,
                back
        ));

        transformDialog.pack();
        game.get(GUIManager.class).addFrame(transformDialog);
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
                    MapTiles.readFile(null, file.toPath());

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
                    DataOutput output = new DataOutputStream(fileOut);
                    Storable.write(output, game.get(GameMap.class));

                    Logger.INFO.print("Saved file " + (hasExtension ? selectedFile : nameWithExtension));

                } catch (IOException e) {
                    Toolbox.display(e);
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

                GameMap newMap = game.get(GameMap.class);

                if (newMap instanceof BlockMap) {
                    blockMap = (BlockMap) newMap;
                }

                Vector2ic size = newMap.getSize();
                setCameraToMiddle(size.x(), size.y());
                Logger.INFO.print("Loaded map of size " + Vectors.toString(size));

            } catch (Exception e) {
                Toolbox.display(e);
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

    private void createNew() {
        SFrame newMapFrame = new SFrame("New Map Settings", 200, 200, true);
        MapGeneratorMod generator = new SimpleMapGenerator(Toolbox.random.nextInt());

        final int ROWS = 10;
        final int COLS = 3;
        Vector2i mpos = new Vector2i(1, 0);
        GUIManager gui = game.get(GUIManager.class);

        SPanel mainPanel = new SPanel(COLS, ROWS);
        mainPanel.add(new SFiller(10, 10), new Vector2i(0, 0));
        mainPanel.add(new SFiller(10, 10), new Vector2i(COLS - 1, ROWS - 1));

        // size selection
        SPanel sizeSelection = new SPanel(0, 0, 4, 1, false, false);
        sizeSelection.add(new STextArea("Size", 0), new Vector2i(0, 0));
        SDropDown xSizeSelector = new SDropDown(gui, 100, 60, 1, "16", "32", "64", "128");
        sizeSelection.add(xSizeSelector, new Vector2i(1, 0));
        sizeSelection.add(new STextArea("X", 0), new Vector2i(2, 0));
        SDropDown ySizeSelector = new SDropDown(gui, 100, 60, 1, "16", "32", "64", "128");
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

        Supplier<String> processDisplay = () -> "Generating heightmap : " + generator.heightmapProgress() * 100 + "%";

        generate.addLeftClickListener(() -> {
            Logger.printOnline(processDisplay);
            // initialize generator
            generator.init(game);
            int xSize = Integer.parseInt(xSizeSelector.getSelected());
            int ySize = Integer.parseInt(ySizeSelector.getSelected());
            generator.setXSize(xSize + 1); // heightmap is 1 larger
            generator.setYSize(ySize + 1);

            TileThemeSet.PLAIN.load();

            game.get(GameMap.class).generateNew(generator);

            setCameraToMiddle(xSize, ySize);
            Logger.removeOnlinePrint(processDisplay);

            newMapFrame.dispose();
        });

        newMapFrame.setMainPanel(mainPanel);
        gui.addFrame(newMapFrame);
    }

    private void setCameraToMiddle(int xSize, int ySize) {
        Vector3f cameraFocus = game.get(GameMap.class).getPosition(new Vector2f(xSize / 2f, ySize / 2f));
        // set camera to middle of map
        float initialZoom = (xSize + ySize);
        Vector3f cameraEye = new Vector3f(cameraFocus).add(-initialZoom, -initialZoom * 0.8f, initialZoom);
        game.get(Camera.class).set(cameraFocus, cameraEye);
    }

    public void start() {
        game.get(GLFWWindow.class).open();
        renderloop.run();
        game.get(GLFWWindow.class).close();
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

    private class BlockModificationTool extends DefaultMouseTool {
        private SFrame window;
        private Integer selectionSize;

        @Override
        public void apply(Vector3fc position) {
            Vector2ic wPos = null;
            boolean recycle = true;

            if (window == null) {
                window = new SFrame("No tile selected");
                recycle = false;

            } else if (window.isDisposed()) {
                wPos = window.getScreenPosition();
                window = new SFrame("No tile selected");
                recycle = false;
            }

            Vector2i coordinate = blockMap.getCoordinate(position);
            int x = coordinate.x;
            int y = coordinate.y;

            window.setTitle("Block at (" + x + ", " + y + ")");

            selectionSize = 1;
            incSelect(x, y, 0);

            SPanel mainPanel = SPanel.column(
                    SPanel.column(
                            new SNamedValue("Height", () -> blockMap.getHeightAt(x, y), BUTTON_MIN_HEIGHT), // supplier is expensive
                            new SButton("increase", () -> incTile(x, y, 1), BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT),
                            new SButton("decrease", () -> incTile(x, y, -1), BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT)
                    ),
                    SPanel.column(
                            new SNamedValue("Selection range", () -> selectionSize, BUTTON_MIN_HEIGHT),
                            new SButton("increase", () -> incSelect(x, y, 1), BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT),
                            new SButton("decrease", () -> incSelect(x, y, -1), BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT)
                    )
            );

            window.setMainPanel(mainPanel);

            if (!recycle) {
                if (wPos == null) {
                    game.get(GUIManager.class).addFrame(window);
                } else {
                    game.get(GUIManager.class).addFrame(window, wPos.x(), wPos.y());
                }
            }
        }

        /**
         * not necessarily a circle
         * @param r number of tiles including middle
         * @return a collection of tiles, the given radius around the given coordinate
         */
        private Vector2ic[] getCircleOf(int x, int y, int r) {
            float hRadius = (r - 1) / 2f;
            Vector2ic[] result = new Vector2ic[r * r];

            int i = 0;
            for (int u = (int) (x - hRadius); u <= x + hRadius; u++) {
                for (int v = (int) (y - hRadius); v <= y + hRadius; v++) {
                    result[i++] = new Vector2i(u, v);
                }
            }

            return result;
        }

        private void incTile(int x, int y, int change) {
            Vector2ic[] tiles = getCircleOf(x, y, selectionSize);

            for (Vector2ic tile : tiles) {
                int tx = tile.x();
                int ty = tile.y();
                blockMap.setTile(tx, ty, blockMap.getHeightAt(tx, ty) + change);
            }
        }

        private void incSelect(int x, int y, int v) {
            selectionSize += v;
            blockMap.setHighlights(getCircleOf(x, y, selectionSize));
        }

        public void dispose() {
            if (window != null) window.dispose();
        }
    }

    private class TileCycleTool extends DefaultMouseTool {
        private SFrame window;

        @Override
        public void apply(Vector3fc position) {
            GameMap map = game.get(GameMap.class);
            assert (map instanceof TileMap);

            Vector2i coordinate = map.getCoordinate(position);
            int x = coordinate.x;
            int y = coordinate.y;
            map.setHighlights(coordinate);

            if (window != null) window.dispose();
            window = new SFrame("Tile at (" + x + ", " + y + ")");

            TileMap tileMap = (TileMap) map;
            MapTile.Instance tileData = tileMap.getTileData(x, y);
            SPanel elements = new SPanel(1, 5);

            STextArea typeDist = new STextArea("Type: " + tileData.type.name, BUTTON_MIN_HEIGHT);
            elements.add(typeDist, new Vector2i(0, 0));

            STextArea heightDisp = new STextArea("Height: " + map.getHeightAt(x, y), BUTTON_MIN_HEIGHT);
            elements.add(heightDisp, new Vector2i(0, 1));

            Runnable switchAction = () -> {
                MapTile.Instance instance = tileData.cycle(1);
                tileMap.setTile(x, y, instance);
                typeDist.setText("Type: " + tileData.type.name);
                heightDisp.setText("Height: " + map.getHeightAt(x, y));
            };
            elements.add(new SButton("Cycle tile type", switchAction, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT), new Vector2i(0, 3));

            window.setMainPanel(elements);

            window.pack();
            game.get(GUIManager.class).addFrame(window);
        }

        public void dispose() {
            if (window != null) window.dispose();
        }
    }
}
