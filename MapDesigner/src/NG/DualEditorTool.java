package NG;

import NG.Engine.Game;
import NG.GUIMenu.Frames.Components.*;
import NG.GUIMenu.Frames.GUIManager;
import NG.GameMap.*;
import NG.InputHandling.MouseTools.DefaultMouseTool;
import NG.Rendering.GLFWWindow;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static NG.MapEditor.BUTTON_MIN_HEIGHT;
import static NG.MapEditor.BUTTON_MIN_WIDTH;

/**
 * @author Geert van Ieperen created on 5-5-2019.
 */
class DualEditorTool extends DefaultMouseTool {
    private static final float SELECTION_STEP_SIZE = 0.4f;
    private SFrame blockWindow;
    private SFrame tileWindow;
    private float selectionSize;
    private BlockMap blockMap;
    private TileMap tileMap;
    private final Map<GameMap, Consumer<Vector3fc>> funcTable;
    private final GameMap[] order;
    private GameMap currentMap;

    public DualEditorTool(Game game, BlockMap blockMap, TileMap tileMap) {
        super(game);
        funcTable = Map.of(
                blockMap, this::applyBlockmap,
                tileMap, this::applyTileMap
        );

        order = new GameMap[]{blockMap, tileMap};
        this.blockMap = blockMap;
        this.tileMap = tileMap;
    }

    @Override
    public void onClick(int button, int x, int y) {
        setButton(button);

        if (game.get(GUIManager.class).checkMouseClick(this, x, y)) return;

        // invert y for transforming to model space (inconsistency between OpenGL and GLFW)
        y = game.get(GLFWWindow.class).getHeight() - y;

        for (GameMap map : order) {
            currentMap = map;
            if (map.checkMouseClick(this, x, y)) return;
        }
    }

    @Override
    public void apply(Vector3fc position) {
        funcTable.get(currentMap).accept(position);
    }

    public void applyBlockmap(Vector3fc position) {

        Vector2i coordinate = blockMap.getCoordinate(position);
        int x = coordinate.x;
        int y = coordinate.y;

        selectionSize = 1;
        incSelect(x, y, 0);

        final int BUTTON_SIZE = BUTTON_MIN_HEIGHT;
        final int GAP_SIZE = 20;

        SPanel mainPanel = SPanel.column(
                SPanel.row(
                        new SNamedValue("Height", () -> blockMap.getHeightAt(x, y), BUTTON_MIN_HEIGHT),
                        new SFiller(GAP_SIZE, 0),
                        new SButton("+", () -> incTile(x, y, 1), BUTTON_SIZE, BUTTON_SIZE),
                        new SButton("-", () -> incTile(x, y, -1), BUTTON_SIZE, BUTTON_SIZE)
                ),
                SPanel.row(
                        new SNamedValue("Selection range", () -> String.format("%1.01f", selectionSize), BUTTON_MIN_HEIGHT),
                        new SFiller(GAP_SIZE, 0),
                        new SButton("+", () -> incSelect(x, y, SELECTION_STEP_SIZE), BUTTON_SIZE, BUTTON_SIZE),
                        new SButton("-", () -> incSelect(x, y, -SELECTION_STEP_SIZE), BUTTON_SIZE, BUTTON_SIZE)
                )
        );

        if (blockWindow == null) {
            blockWindow = new SFrame("No tile selected");
            game.get(GUIManager.class).addFrame(blockWindow);

        } else if (blockWindow.isDisposed()) {
            Vector2ic wPos = blockWindow.getScreenPosition();
            blockWindow = new SFrame("No tile selected");
            game.get(GUIManager.class).addFrame(blockWindow, wPos.x(), wPos.y());
        }

        blockWindow.setTitle("Block at (" + x + ", " + y + ")");
        blockWindow.setMainPanel(mainPanel);
    }

    /**
     * not necessarily a circle
     * @param r number of tiles including middle
     * @return a collection of tiles, the given radius around the given coordinate
     */
    private Vector2ic[] getCircleOf(int x, int y, float r) {
        float rSq = r * r;
        Vector2ic[] result = new Vector2ic[(int) (4 * rSq)];

        int i = 0;
        for (int u = (int) -r; u <= r; u++) {
            for (int v = (int) -r; v <= r; v++) {
                if (u * u + v * v < rSq) {
                    result[i++] = new Vector2i(u + x, v + y);
                }
            }
        }

        return Arrays.copyOf(result, i);
    }

    private void incTile(int x, int y, int change) {
        Vector2ic[] tiles = getCircleOf(x, y, selectionSize);

        for (Vector2ic tile : tiles) {
            int tx = tile.x();
            int ty = tile.y();
            blockMap.setTile(tx, ty, blockMap.getHeightAt(tx, ty) + change);
        }

        recalculateTileMap(tiles);
    }

    private void incSelect(int x, int y, float v) {
        selectionSize += v;
        blockMap.setHighlights(getCircleOf(x, y, selectionSize));
    }

    private void recalculateTileMap(Vector2ic... tiles) {
        // TODO only change tiles that are given
        tileMap.generateNew(new CopyGenerator(blockMap));
    }

    public void applyTileMap(Vector3fc position) {
        GUIManager gui = game.get(GUIManager.class);

        Vector2i coordinate = tileMap.getCoordinate(position);
        int x = coordinate.x;
        int y = coordinate.y;
        tileMap.setHighlights(coordinate);

        if (tileWindow != null) tileWindow.dispose();
        tileWindow = new SFrame("Tile at (" + x + ", " + y + ")");

        MapTile tileType = tileMap.getTileData(x, y).type;
        List<MapTile> mapTileList = MapTiles.getByOrientationBits(tileType.fit);

        SNamedValue typeDist = new SNamedValue("Type", () -> tileMap.getTileData(x, y).type.name, BUTTON_MIN_HEIGHT);
        SNamedValue heightDisp = new SNamedValue("Height", () -> tileMap.getHeightAt(x, y), BUTTON_MIN_HEIGHT);
        SNamedValue orientationDisp = new SNamedValue("Orientation", () -> getOrientation(x, y), BUTTON_MIN_HEIGHT);

        SDropDown tileSelection;
        if (mapTileList.isEmpty()) {
            tileSelection = new SDropDown(gui, BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT, 0, "<No applicable tiles>");
        } else {
            tileSelection = new SDropDown(gui, BUTTON_MIN_HEIGHT, BUTTON_MIN_WIDTH, tileType, mapTileList);
        }

        SPanel elements = SPanel.column(
                typeDist,
                heightDisp,
                orientationDisp,
                tileSelection
        );

        tileWindow.setMainPanel(elements);

        tileWindow.pack();
        gui.addFrame(tileWindow);
    }

    public String getOrientation(int x, int y) {
        int height = tileMap.getHeightAt(x, y);
        return String.format("(%d, %d, %d, %d)",
                blockMap.getHeightAt(x, y) - height,
                blockMap.getHeightAt(x + 1, y) - height,
                blockMap.getHeightAt(x + 1, y + 1) - height,
                blockMap.getHeightAt(x, y + 1) - height
        );
    }

    public void dispose() {
        if (blockWindow != null) blockWindow.dispose();
    }
}
