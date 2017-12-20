import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.rhythm.Rhythm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class handles all music generating algorithms
 */
public class MusicGenerator {

                    /* -- Configuration -- */
    public static final int NUMBER_OF_CHORDS = 16;//number of required chords in chordSequence & melody
    public static final int NUMBER_OF_GENERATED_CHORDS = 8;//number of chords that will be actually generated


                    /* -- Fields -- */

    ArrayList<Particle> chords;//chord sequence
    ArrayList<Particle> melody;//melody sequence


                    /* -- Instance Methods -- */

    /**
     * T
     * @param sequenceLength
     * @throws Exception
     */
    public void createChordSequence(int sequenceLength) throws Exception {//number of chords in sequence. It can differ from number of chords

        chords = new ArrayList<>(sequenceLength);

        //Setting configuration for Particle class to generate proposed chords for chord sequence
        Particle.initializeForChord();
        for (int i = 0; i < sequenceLength; i++) {//each iteration produces several chords in particle.
            double[] newSequencePart;
            int fitness = 0;
            do {
//                Particle.initializeForChord();
                Flock chordFlock = new Flock();
                chordFlock.PSO1(Flock.NUMBER_OF_ITERATIONS);
                newSequencePart = chordFlock.getBest();
                fitness = chordFlock.globelBestFitness;
            } while (Particle.calculateGoodChords(newSequencePart) < Particle.getNumOfChords() || fitness < 0);
            //checks if #good chords = total # of chords
            Particle newChord = new Particle(newSequencePart);
            newChord.setFitness(fitness);
            chords.add(newChord);
            Particle.previousChords.add(newChord);
        }
    }

    /**
     * This method creates melody
     * @param sequenceLength
     */
    public void createMelodySequence(int sequenceLength) throws Exception {
        Particle.initializeForMelody();
        melody = new ArrayList<>(sequenceLength);

        for (int i = 0; i < sequenceLength; i++) {
            double[] newSequencePart;
            int fitness = 0;
            do {
                Flock melodyFlock = new Flock();
                melodyFlock.PSO2(Flock.NUMBER_OF_ITERATIONS);
                newSequencePart = melodyFlock.getBest();
                fitness = melodyFlock.globelBestFitness;
            }while(fitness<35);
            Particle newMelodyChord = new Particle(newSequencePart);
            newMelodyChord.setFitness(fitness);
            Particle.previousNotes.add(newMelodyChord);
            melody.add(newMelodyChord);
        }

    }

    public static void recordMusic(ArrayList<Particle> chords, ArrayList<Particle> melody, int tempo, int index) throws IOException {
        StringBuilder musicString = new StringBuilder();
        musicString.append("I[Piano] ");
        for (int j = 0; j < 2; j++) {

            for (int i = 0; i < NUMBER_OF_GENERATED_CHORDS; i++) {
                Particle current = chords.get(i);
                musicString.append((int) Math.round(current.getPosition()[0]) + "q+" + (int)Math.round(current.getPosition()[1])
                        + "q+" + (int)Math.round(current.getPosition()[2]) + "q ");

            }
        }
        musicString.deleteCharAt(musicString.length()-1);
        Pattern pattern = new Pattern(musicString.toString()).setVoice(0).setTempo(tempo);
        musicString.setLength(0);
        musicString.append("V1 I[Piano] ");
        for (int j = 0; j < 2; j++) {

            for (int i = 0; i < NUMBER_OF_GENERATED_CHORDS; i++) {
                Particle current = melody.get(i);
                musicString.append((int)Math.round( current.getPosition()[0]) + "i " + (int)Math.round(current.getPosition()[1]) + "i ");
            }
        }
        musicString.deleteCharAt(musicString.length()-1);
        pattern.add(musicString.toString());
        String recordName = "music" + index;

        String midiFileNameEnd = ".mid";


        Rhythm rhythm = new Rhythm().addLayer("````````````````");
        rhythm.setLength(2);
        pattern.add(rhythm);

        //writing patter to txt
        BufferedWriter bw = new BufferedWriter(new FileWriter("music/" + recordName + "pattern.txt"));
        bw.write(pattern.toString());
        bw.close();

        //writing pattern to mid
        try {
            MidiFileManager.savePatternToMidi(pattern, new File("music/" + recordName + midiFileNameEnd));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


                   /* -- Class Methods --*/

    public static void main(String[] args) throws Exception {
        //Generating chords
        long startTime = System.currentTimeMillis();
        MusicGenerator mg = new MusicGenerator();
        System.out.println("Generating chords...");
        mg.createChordSequence(NUMBER_OF_GENERATED_CHORDS);
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("I generated chord sequence in " + estimatedTime + " millis!");

        //Generating melody
        System.out.println("Generating melody...");
        startTime = System.currentTimeMillis();
        mg.createMelodySequence(NUMBER_OF_GENERATED_CHORDS);
        estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println("I generated melody in " + estimatedTime + " millis!");
        recordMusic(mg.chords, mg.melody, 150, 1);
    }
}
