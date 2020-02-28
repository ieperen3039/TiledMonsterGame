package NG.InputHandling;

import NG.Camera.Camera;
import NG.Core.Game;
import NG.Core.GameAspect;
import NG.GameEvent.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert van Ieperen created on 28-2-2020.
 */
public class EventCallbacks implements GameAspect {
    //    private Game game;
    private List<CameraListener> cameraListeners = new ArrayList<>();
    private List<EventListener> eventListeners = new ArrayList<>();

    @Override
    public void init(Game game) throws Exception {
//        this.game = game;
    }

    public void addCameraListener(CameraListener listener) {
        cameraListeners.add(listener);
    }

    public void removeCameraListener(CameraListener listener) {
        cameraListeners.remove(listener);
    }

    public void notifyCamera(Camera camera) {
        cameraListeners.forEach(l -> l.onChange(camera));
    }

    @Override
    public void cleanup() {

    }

    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(EventListener listener) {
        eventListeners.remove(listener);
    }

    public void notifyEvent(Event event) {
        eventListeners.forEach(l -> l.onEvent(event));
    }

    public interface EventListener {
        void onEvent(Event e);
    }

    public interface CameraListener {
        void onChange(Camera camera);
    }
}
