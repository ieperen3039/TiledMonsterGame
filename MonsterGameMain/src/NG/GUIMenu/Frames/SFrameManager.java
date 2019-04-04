package NG.GUIMenu.Frames;

import NG.Engine.Game;
import NG.Engine.Version;
import NG.GUIMenu.BaseLF;
import NG.GUIMenu.Frames.Components.SComponent;
import NG.GUIMenu.Frames.Components.SFrame;
import NG.GUIMenu.SToolBar;
import NG.GUIMenu.ScreenOverlay;
import NG.InputHandling.KeyMouseCallbacks;
import NG.InputHandling.MouseTools.MouseTool;
import NG.Tools.Logger;
import org.joml.Vector2ic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Objects of this class can manage an in-game window system that is behaviourally similar to classes in the {@link
 * javax.swing} package. New {@link SFrame} objects can be added using {@link #addFrame(SFrame)}.
 * @author Geert van Ieperen. Created on 20-9-2018.
 */
public class SFrameManager implements GUIManager {
    private Game game;
    /** the first element in this list has focus */
    private Deque<SFrame> frames;
    private SComponent modalSection;

    private SFrameLookAndFeel lookAndFeel;
    private SToolBar toolBar = null;

    public SFrameManager() {
        this.frames = new ArrayDeque<>();
    }

    @Override
    public void init(Game game) throws Version.MisMatchException {
        this.game = game;
        lookAndFeel = new BaseLF();
        lookAndFeel.init(game);
    }

    @Override
    public void draw(ScreenOverlay.Painter painter) {
        assert hasLookAndFeel();

        frames.removeIf(SFrame::isDisposed);
        lookAndFeel.setPainter(painter);

        Iterator<SFrame> itr = frames.descendingIterator();
        while (itr.hasNext()) {
            final SFrame f = itr.next();

            if (f.isVisible()) {
                f.validateLayout();
                f.draw(lookAndFeel, f.getPosition());
            }
        }

        if (modalSection != null) {
            modalSection.validateLayout();
            modalSection.draw(lookAndFeel, modalSection.getScreenPosition());
        }

        if (toolBar != null) {
            toolBar.draw(lookAndFeel, null);
        }
    }

    @Override
    public void addFrame(SFrame frame) {
        frame.validateLayout();

        int toolbarHeight = toolBar == null ? 0 : toolBar.getHeight();
        int x = 50;
        int y = 50 + toolbarHeight;

        // reposition frame not to overlap other frames (greedy)
        for (Iterator<SFrame> iterator = frames.descendingIterator(); iterator.hasNext(); ) {
            SFrame other = iterator.next();
            if (other.isDisposed() || other.isMinimized() || !other.isVisible()) continue;

            Vector2ic otherPos = other.getScreenPosition();

            if (otherPos.x() == x && otherPos.y() == y) {
                x += 20;
                y += 20; // MS windows-style
            }
        }

        addFrame(frame, x, y);
    }

    @Override
    public void addFrame(SFrame frame, int x, int y) {
        // if the frame was already visible, still add it to make it focused.
        frames.remove(frame);

        boolean success = frames.offerFirst(frame);
        if (!success) {
            Logger.DEBUG.print("Too much subframes opened, removing the last one");
            frames.removeLast().dispose();
            frames.addFirst(frame);
        }

        frame.setPosition(x, y);
    }

    @Override
    public void focus(SFrame frame) {
        if (frame.isDisposed()) {
            throw new NoSuchElementException(frame + " is disposed");
        }

        // even if the frame was not opened, show it
        frame.setVisible(true);

        // no further action when already focused
        if (frames.peekFirst().equals(frame)) return;

        boolean success = frames.remove(frame);
        if (!success) {
            throw new NoSuchElementException(frame + " was not part of the window");
        }

        frames.addFirst(frame);
    }

    @Override
    public void setLookAndFeel(SFrameLookAndFeel lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
    }

    @Override
    public boolean hasLookAndFeel() {
        return lookAndFeel != null;
    }

    @Override
    public void setModalListener(SComponent listener) {
        modalSection = listener;
    }

    @Override
    public void setToolBar(SToolBar toolBar) {
        this.toolBar = toolBar;
    }

    @Override
    public void cleanup() {
        game.get(KeyMouseCallbacks.class).removeListener(this);
        frames.forEach(SFrame::dispose);
        frames.clear();
    }

    @Override
    public boolean covers(int xSc, int ySc) {
        if (toolBar != null && toolBar.contains(xSc, ySc)) {
            return true;
        }

        if (modalSection != null && modalSection.contains(xSc, ySc)) {
            return true;
        }

        for (SFrame frame : frames) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean checkMouseClick(MouseTool tool, final int xSc, final int ySc) {
        // check modal dialogues
        if (modalSection != null) {
            Vector2ic mPos = modalSection.getScreenPosition();

            if (xSc >= mPos.x() && ySc >= mPos.y()) {
                if (xSc <= mPos.x() + modalSection.getWidth()) {
                    if (ySc <= mPos.y() + modalSection.getHeight()) {
                        tool.apply(modalSection, xSc, ySc);
                    }
                }
            }

            modalSection = null;
            return true;
        }

        // check toolbar
        if (toolBar != null) {
            if (toolBar.contains(xSc, ySc)) {
                tool.apply(toolBar, xSc, ySc);
                return true;
            }
        }

        // check all frames, starting from the front-most frame
        for (SFrame frame : frames) {
            if (frame.isVisible() && frame.contains(xSc, ySc)) {
                focus(frame);
                int xr = xSc - frame.getX();
                int yr = ySc - frame.getY();
                SComponent component = frame.getComponentAt(xr, yr);

                tool.apply(component, xSc, ySc);
                return true; // only for top-most frame
            }
        }

        return false;
    }
}
