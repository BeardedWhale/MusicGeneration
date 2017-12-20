import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * This class implements a particle for Particle Swarm Optimisation PSO1
 * As this class also uses an abstraction of chords as it is used in music generation
 */
public class Particle {

                        /* -- Configuration -- */

    static int MAX_VAL = 72;
    static int MIN_VAL = 48;
    static int NUM_OF_CHORDS = 1;//number of chords in sequence
    static int CHORD_SIZE = 3;//number of notes in chord
    static int DIMENSIONS = 3; //number of notes in sequence
    private static double[] INITIAL_VELOCITY;
    static int TONALITY;
    static boolean IS_MINOR = true;
    private static int[] TONALITIES;
    private static final int TONALITY_SIZE = 8;//the SIZE of the array of tonalities

    //Coefficients
    static double m = 0.729; //momentum value
    static double c1 = 0.0804825941012691;//cognitive component
    static double c2 = 1.5789837501295394;//social component


    //this arrays are used in fitness functions in generating chords/melody
    static ArrayList<Particle> previousChords;
    static ArrayList<Particle> previousNotes;



                        /* -- Fields -- */


    //current position of current particle and it's fitness
    double[] position;
    int fitness;

    //best met position of particle
    private double[] myBest;
    private int bestFitness;

    //current velocity vector of particle
    double[] velocity;

    //is all notes in range or not
    boolean isValid = true;

                        /* -- Constructors -- */

    public Particle(double[] notes){
        this.position = notes;
        this.myBest = notes;
        this.bestFitness = getFitness();
        setVelocity(INITIAL_VELOCITY);
    }

    public Particle(double[] notes, int numOfChords){//specifies SIZE of sequence
        this.position = notes;
        this.myBest = notes;
        this.bestFitness = getFitness();
        setNumOfChords(numOfChords);
        setVelocity(INITIAL_VELOCITY);
    }

    /**
     * Copyconstructor
     * @param particle
     */
    public Particle(Particle particle){
        this.position = particle.getPosition();
        this.myBest = particle.getMyBest();
        this.setVelocity(particle.getVelocity());
    }

                        /* -- Instance Methods -- */


        /* - fitness functions for chord sequence - */

    /**
     * This function updates fitness of the hole chord sequence.
     * It computes how good chords are and how good is the sequence of chords
     */
    public void chordSequenceFitness() throws Exception {
        if (isValid) {
            fitness = chordFitness();
            int numReadyChords = previousChords.size();
            //iterating through previous Chords
            int repeatedChords = 0;
            int sameChords = 0;
            for (int i = numReadyChords -1; i > -1; i--) {

                if (this.equals(previousChords.get(i))){
                    repeatedChords++;
                    sameChords++;
                } else{
                    repeatedChords=0;
                }

            }
            if (repeatedChords>=2){
                fitness-=100000;//if there are 3 equal repeated chords
            }
            if (sameChords>numReadyChords/2) fitness-=10;//if there are a lot of such chords
            if (numReadyChords>1){
                if (this.equals(previousChords.get(numReadyChords-2)))fitness++;
                if (numReadyChords>4){
                    if (this.equals(previousChords.get(numReadyChords -4)))fitness++;
                }
            }
            if (numReadyChords>0) {
                if ((this.position[0] - previousChords.get(numReadyChords - 1).position[DIMENSIONS - 1]) < 12)
                    fitness++;
            }
        }else fitness=0;
    }

    /**
     * This method computes how good chords are
     * only for chords of SIZE 3
     * @return fitness
     */
    private int chordFitness() throws Exception {

        if (CHORD_SIZE!=3){
            throw new Exception("Wrong chord fromat!");
        }
        int chordFitness = 1;
        for (int i = 0; i < DIMENSIONS; i+=3) {
            int uniqueFit = 1;

            int firstNote = (int) Math.round(position[i]);
            int secondNote = (int) Math.round(position[i + 1]);
            int thirdNote = (int) Math.round(position[i + 2]);

            int tonalityOffset = TONALITY % 12;//how far from the beginning  of octave is tonality

            if (isInRange(firstNote)&&isInRange(secondNote)&&isInRange(thirdNote)){
                fitness++;
            }else {
                fitness-=100;
            }

            if (IS_MINOR) {//conditions for minor tonality
                if (firstNote % 12 == tonalityOffset || firstNote % 12 == tonalityOffset + 5 || firstNote % 12 == tonalityOffset + 7) {
                    chordFitness+=10;
                    uniqueFit++;
                }
                if ((secondNote - firstNote) % 12 == 3) {
                    chordFitness+=10;
                    uniqueFit++;
                }
                if ((thirdNote - secondNote) % 12 == 4) {
                    chordFitness+=10;
                    uniqueFit++;
                }
            } else {//conditions for major tonality
                if (firstNote % 12 == tonalityOffset || firstNote % 12 == tonalityOffset + 5 || firstNote % 12 == tonalityOffset + 7) {
                    chordFitness+=10;
                    uniqueFit++;
                }
                if ((secondNote - firstNote) % 12 == 4) {
                    chordFitness+=10;
                    uniqueFit++;
                }
                if ((thirdNote - secondNote) % 12 == 3) {
                    chordFitness+=10;
                    uniqueFit++;
                }

                if (uniqueFit==4){
                    chordFitness+=100;
                }
            }
        }
        return chordFitness;
    }

        /* - fitness functions for melody -*/

    /**
     * This method determines melody fitness of chords
     */
    public void melodyFitness() throws Exception {

        int currentNoteIndex = previousNotes.size();
        double[] correspondingChord = previousChords.get(currentNoteIndex).getPosition();
        if(CHORD_SIZE!=2){
            throw new Exception("Wrong chord format!");
        }
        if (TONALITIES == null){
            throw new Exception("Tonalities are not set");
        }
        if (previousChords == null){
            throw new Exception("Chord sequence is not created");
        }
        int melodyFitness = 1;

        for (int i = 0; i < DIMENSIONS; i+=2) {
            int firstNote= (int)Math.round(position[i]);
            int secondNote= (int)Math.round(position[i+1]);
            for (double note:correspondingChord) {
                //if any note from generated chords has the same octave offset
                if ((int)Math.round(note)%12 == firstNote%12){
                    melodyFitness+=10;
                    break;
                }
            }
            for (int note:TONALITIES) {
                //if second note has the same offset as the any note from tonality
                if (note%12==secondNote%12){
                    melodyFitness+=10;
                    break;
                }
            }
            if ((firstNote - secondNote) < 12)
                melodyFitness+=10;

            if (firstNote >correspondingChord[correspondingChord.length-1])melodyFitness+=5;

            if (secondNote>correspondingChord[correspondingChord.length-1])melodyFitness+=5;

            if (firstNote + 12 <correspondingChord[correspondingChord.length-1])melodyFitness+=5;

            if (secondNote + 12 <correspondingChord[correspondingChord.length-1])melodyFitness+=5;

        }
        fitness = melodyFitness;

    }

    /**
     * detects if note is tonic or not
     * @return
     */
    public boolean isTonic(double[] chord){
        int firstNote = (int) Math.round(chord[0]);
        int secondNote = (int) Math.round(chord[1]);
        int thirdNote = (int) Math.round(chord[2]);
        if (IS_MINOR) {
            if (firstNote == TONALITY && (secondNote - firstNote) == 3 &&(thirdNote - secondNote)==4){
                return true;
            }
        }else {
            if (firstNote == TONALITY && (secondNote - firstNote) == 4 &&(thirdNote - secondNote)==3){
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns i-th chord
     * @param index
     * @return
     */
    public double[] getChord(int index){
        return new double[]{position[index*3], position[index*3 +1], position[index*3 +2]};
    }


    /**
     * this method updates function according to the following formula: V[t+1]=m*V[t]
     + c1*rand[0,1]*(myBest[]-Position[t])
     + c2*rand[0,1]*(globalBest[]-Position[t])
     */
    public void updateVelocity(double[] globalBest){

        double[] newVelocity;

        newVelocity = multiplyVector(m, velocity);
        double[] myBestDiff = subtractVector(myBest, position);
        double[] globalBestDiff = subtractVector(globalBest, position);


        newVelocity = sumVector(newVelocity, multiplyVector(c1 * new Random().nextDouble(), myBestDiff));
        newVelocity = sumVector(newVelocity, multiplyVector(c2 * new Random().nextDouble(), globalBestDiff));

        setVelocity(newVelocity);
    }

    /**
     * this method updates current position according to the following formula:
     * Position[t+1] = Position[t] + V[t+1]
     */
    public void updatePosition(){
        setPosition(sumVector(position, velocity));
        //sometimes position does not appears in the given range [MIN_VAL, MAX_VAL]
        //in such cases we normalise the position vector
        if (!isValid){
            setPosition(normalizeVector(position));

        }
    }

    /**
     * if new value is not in range -> the chord is not valid
     * @param index of note(coordinate) to change
     * @param value of new position
     */
    public void setNote(int index, double value){
        if (!isInRange(value)){
            isValid = false;
        }
        position[index] = value;
    }


    private boolean isInRange(double noteValue){
        return noteValue>=MIN_VAL&&noteValue<=MAX_VAL;
    }

    /**
     * This method checks if chord index is valid
     * @param index
     * @return
     */
    private boolean isValidChordIndex(int index){
        return index>=0&&index<NUM_OF_CHORDS;
    }

    /**
     * This function calculates number of good chords in particle
     *
     * @return number of chords of given particle
     */
    public int numberOfGoodChords() {
        return calculateGoodChords(position);
    }



                        /* -- Class Methods --*/

    /**
     * sets a velocity equal to: r1*random[min;max]/dimensions
     */
    public static void setInitialVelocity(){

        Random random = new Random();
        double[] values = new double[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) {
            values[i] = random.nextDouble()*random.nextInt((int)((MAX_VAL - MIN_VAL)))/DIMENSIONS;
        }
        //as random processes a randomly distributed numbers
        //the velocity is uniformly distibuted vector
        INITIAL_VELOCITY = Arrays.copyOf(values, DIMENSIONS);

    }

    /**
     * This method calculates number of good chords in sequence of notes
     * @param position
     * @return
     */
    public static int calculateGoodChords(double[] position) {
        int goodChords = 0;
        for (int i = 0; i < DIMENSIONS; i += 3) {

            int firstNote = (int) Math.round(position[i]);
            int secondNote = (int) Math.round(position[i + 1]);
            int thirdNote = (int) Math.round(position[i + 2]);

            int tonalityOffset = TONALITY % 12;//how far from the beginning  of octave is tonality

            if (IS_MINOR) {//conditions for minor tonality
                if (firstNote % 12 == tonalityOffset || firstNote % 12 == tonalityOffset + 5 || firstNote % 12 == tonalityOffset + 7) {
                    if ((secondNote - firstNote) % 12 == 3) {
                        if ((thirdNote - secondNote) % 12 == 4) {
                            goodChords++;
                        }
                    }
                }
            } else {//conditions for major tonality
                if (firstNote % 12 == tonalityOffset || firstNote % 12 == tonalityOffset + 5 || firstNote % 12 == tonalityOffset + 7) {
                    if ((secondNote - firstNote) % 12 == 4) {
                        if ((thirdNote - secondNote) % 12 == 3) {
                            goodChords++;
                        }
                    }
                }
            }

        }


        return goodChords;
    }


    public static String toStaticString(){
        return "Particle{" +
                "m =" + m +
                ", c1 =" + c1 +
                ", c2=" + c2 +
                ", TONALITY=" + TONALITY +
                ", IS_MINOR=" + IS_MINOR +
                '}';

    }


        /* -----> for working with vectors */

    private static double[] normalizeVector(double[] vector){
        double[] normalizedVector = Arrays.copyOf(vector, DIMENSIONS);
        double vectorMagnitude = 0.0;
        for (int i = 0; i < vector.length; i++) {
            vectorMagnitude+=vector[i]*vector[i];
        }

        vectorMagnitude = Math.sqrt(vectorMagnitude);
        for (int i = 0; i < DIMENSIONS; i++) {
            normalizedVector[i] = (vector[i]/vectorMagnitude)*(Particle.getMaxVal() - Particle.getMinVal()) + Particle.getMinVal();
        }

        return normalizedVector;
    }

    /**
     * This is for working with vectors of position and velocity
     * @param constant
     * @param vector
     * @return
     */
    private static double[] multiplyVector(double constant, double[] vector){
        double[] result;
        result = Arrays.copyOf(vector, DIMENSIONS);
        for (int i = 0; i < DIMENSIONS; i++) {
            result[i] = constant*vector[i];
        }
        return result;
    }

    /**
     * subtracts vector2 from vector1
     * @param vector1
     * @param vector2
     * @return
     */
    private static double[] subtractVector(double[] vector1, double[] vector2){
        double[] result = Arrays.copyOf(vector1, vector1.length);
        for (int i = 0; i < DIMENSIONS; i++) {
            result[i] = vector1[i] - vector2[i];
        }
        return result;
    }

    /**
     * sums vector1 and vector2
     * @param vector1
     * @param vector2
     * @return
     */
    private static double[] sumVector(double[] vector1, double[] vector2) {
        double[] result = Arrays.copyOf(vector1, vector1.length);
        for (int i = 0; i < DIMENSIONS; i++) {
            result[i] = vector1[i] + vector2[i];
        }
        return result;
    }

    /**
     * //sets class configuration propriate for generating chords
     */
    public static void initializeForChord(){

        setMaxVal(72);
        setMinVal(48);
        setChordSize(3);
        setNumOfChords(1);
        setTONALITY();
        setInitialVelocity();
        previousChords = new ArrayList<>();
    }

    /**
     * //sets class configuration suitable for generating melody
     */
    public static void initializeForMelody(){
        //sets class configuration suitable for generating melody
        setMinVal(72);
        setMaxVal(96);
        setChordSize(2);
        setNumOfChords(1);
        setInitialVelocity();
        setTONALITIES();
        previousNotes = new ArrayList<>();
    }

                        /* -- Getters&Setters -- */


    public double[] getPosition(){
        return position;
    }

    private void setPosition(double[] newPosition) {
        //by default new position is valid
        isValid = true;
        for (int i = 0; i < DIMENSIONS; i++) {
            setNote(i, newPosition[i]);
        }
    }

    public int getFitness(){
        return fitness;
    }

    public double[] getMyBest(){
        return myBest;
    }

    public static int getMaxVal() {
        return MAX_VAL;
    }

    public static void setMaxVal(int maxVal) {
        MAX_VAL = maxVal;
    }

    public static int getMinVal() {
        return MIN_VAL;
    }

    public static void setMinVal(int minVal) {
        MIN_VAL = minVal;
    }

    public static int getDIMENSIONS() {
        return DIMENSIONS;
    }

    public static double getM() {
        return m;
    }

    public static void setM(double newm) {
        m = newm;
    }

    public static double getC1() {
        return c1;
    }

    public static void setC1(double newc1) {
        c1 = newc1;
    }

    public static double getC2() {
        return c2;
    }

    public static void setC2(double newc2) {
        c2 = newc2;
    }

    public double[] getVelocity() {
        return velocity;
    }

    public void setVelocity(double[] velocity) {
        this.velocity = new double[velocity.length];

        System.arraycopy(velocity, 0, this.velocity, 0, DIMENSIONS);

    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public void setBestFitness(int bestFitness) {
        this.bestFitness = bestFitness;
    }


    /**
     * This method determines which tonality will have all chords
     * The range for possible tonalities is in range:[MIN_VAL ; MAX_VAL - 12*2] (2 octaves less then all possible values)
     */
    public static void setTONALITY() {
        TONALITY = new Random().nextInt(11);
        setIS_MINOR();
    }


    private static void setIS_MINOR() {
        IS_MINOR = new Random().nextBoolean();
    }

    public static int getNumOfChords() {
        return NUM_OF_CHORDS;
    }

    /**
     * This methods sets number of chords in the particle
     * and than defines dimensions if CHORD_SIZE is already set
     * @param numOfChords
     */
    public static void setNumOfChords(int numOfChords) {
        NUM_OF_CHORDS = numOfChords;
        DIMENSIONS = NUM_OF_CHORDS*CHORD_SIZE;
    }

    public static void setChordSize(int chordSize) {
        CHORD_SIZE = chordSize;
    }

    public static ArrayList<Particle> getPreviousChords() {
        return previousChords;
    }

    public static void setTONALITIES(){
        TONALITIES = new int[TONALITY_SIZE];
        if (IS_MINOR){
            TONALITIES[0]=TONALITY;
            TONALITIES[1]=TONALITIES[0] + 2;
            TONALITIES[2]=TONALITIES[1] + 1;
            TONALITIES[3]=TONALITIES[2] + 2;
            TONALITIES[4]=TONALITIES[3] + 2;
            TONALITIES[5]=TONALITIES[4] + 1;
            TONALITIES[6]=TONALITIES[5] + 2;
            TONALITIES[7]=TONALITIES[6] + 2;

        }else{
            TONALITIES[0]=TONALITY;
            TONALITIES[1]=TONALITIES[0] + 2;
            TONALITIES[2]=TONALITIES[1] + 2;
            TONALITIES[3]=TONALITIES[2] + 1;
            TONALITIES[4]=TONALITIES[3] + 2;
            TONALITIES[5]=TONALITIES[4] + 2;
            TONALITIES[6]=TONALITIES[5] + 2;
            TONALITIES[7]=TONALITIES[6] + 1;
        }
    }

    /* -- Override -- */

    @Override
    public String toString() {
        return "Particle{" +
                "position=" + Arrays.toString(position) +
                ", velocity=" + Arrays.toString(velocity) +
                ", fitness=" + fitness +
                '}';
    }

    /**
     * Compares two Particle object
     * If all thier position values(they are rounded to int) are equal then objects er equal
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Particle)) return false;

        Particle particle = (Particle) o;
        boolean equals = true;
        for (int i = 0; i < DIMENSIONS; i++) {
            if ((int)Math.round(this.position[i])!=(int)Math.round(particle.position[i])){
                equals = false;
                break;
            }
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getPosition());
    }

}
