package NG.Living;

import NG.Tools.Toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author Geert van Ieperen created on 7-4-2019.
 */
public class SoulDescription {
    public Map<Type, Float> importance = new HashMap<>();
    public Map<Type, Emotion.Translation> stimulusEffects = new HashMap<>();
    public Emotion.ECollection emotions = null;
    public EnumMap<Emotion, Float> emotionValues = new EnumMap<>(Emotion.class);

    public String name = "Generic Monster";

    public SoulDescription(File description) throws IOException {
        try (Scanner reader = new Scanner(new FileInputStream(description))) {
            while (reader.hasNext()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty() || line.charAt(0) == '#') continue; // ignore comments and blank lines

                switch (line) {
                    case "stimulus:":
                        // sets importance and stimulusEffects
                        readStimulusValues(reader);
                        break;
                    case "emotion:":
                        // sets emotions
                        emotions = new Emotion.ECollection(reader);
                        break;
                    case "value:":
                        // sets emotionValue
                        readEmotionValues(reader);
                        break;
                    case "name:":
                        name = reader.nextLine().trim();
                    default:
                        throw new IllegalStateException("Unexpected input: " + line);
                }

            }
        } catch (Exception ex) {
            String message = "Error while loading file";
            throw new IOException(message, ex);
        }
    }

    private void readStimulusValues(Scanner reader) {
        Pattern colonMark = Pattern.compile(":");
        String line;
        while (!(line = reader.nextLine()).equals("end")) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            String[] elts = Toolbox.WHITESPACE_PATTERN.split(line.trim());

            Type slt = Stimulus.getByName(elts[0]);
            float value = Float.parseFloat(elts[1]);
            importance.put(slt, value);

            Emotion.Translation mapping = new Emotion.Translation();
            for (int i = 2; i < elts.length; i++) {
                String[] pair = colonMark.split(elts[i]);
                Emotion emotion = Emotion.valueOf(pair[0]);
                int change = Integer.parseInt(pair[1]);

                mapping.set(emotion, change);
            }

            stimulusEffects.put(slt, mapping);
        }
    }

    private void readEmotionValues(Scanner reader) {
        String line;
        while (!(line = reader.nextLine()).equals("end")) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            String[] elts = Toolbox.WHITESPACE_PATTERN.split(line.trim());

            Emotion emotion = Emotion.valueOf(elts[0]);
            float value = Float.parseFloat(elts[1]);
            emotionValues.put(emotion, value);
        }
    }
}
