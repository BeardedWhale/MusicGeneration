import java.util.Arrays;
import java.util.Random;

/**
 *  This class provides an abstraction of flock for Particle Swarm Optimization PSO1
 *
 */
public class Flock {
                        /* -- Fields --*/

    Particle[] particles;
    static int SIZE = 1500;//flock SIZE
    private boolean isSorted;//flags if flock is sorted
    static int NUMBER_OF_ITERATIONS = 10; //by default
    int currentIteration;
    /**
     * global best particle
     */
    double[] globalBest;
    int globelBestFitness;

                        /* -- Constructor -- */
    public Flock(){
        this.SIZE = SIZE;

        particles = new Particle[SIZE];
        //creating flock of random particles
        Random random = new Random();
        for (int i = 0; i < SIZE; i++) {
            double[] notes = new double[Particle.getDIMENSIONS()];
            for (int j = 0; j < Particle.getDIMENSIONS(); j++) {
                double note = (double) ((random.nextInt(Particle.getMaxVal() - Particle.getMinVal()) + Particle.getMinVal()));
                notes[j] = note;
            }
            particles[i] = new Particle(notes);
        }

        globalBest = getBest();
    }


                        /* -- Methods --*/

    /**
     * THis method is used to generate chord sequence
     * It deffers from PSO2 only by using different fittness functions for particles
     *For each particle
     Initialize particle (usually random position and velocity)
     END
     Do
     For each particle
     Calculate fitness value
     If the fitness value is better than the best fitness value (pBest) in history
     set current value as the new pBest
     End
     Choose the particle with the best fitness value of all the particles as the gBest For each particle
     Calculate particle velocity according equation (1)
     Update particle position according equation (2)
     End
     While maximum iterations or minimum error criteria is not attained
     */
    public void PSO1(int iterations) throws Exception {
        NUMBER_OF_ITERATIONS = iterations;

        globalBest = getBest();//set the best in particle as global best
        while(currentIteration< NUMBER_OF_ITERATIONS) {

            //for each particle in the flock
            for (int i = 0; i < SIZE; i++) {
                if (i == 0){
                    particles[i].chordSequenceFitness();//update sequence fitness
                    particles[i].setBestFitness(particles[i].getFitness());//set initial fitness as best fitness
                }
                //Calculate particle velocity according equation
                //Update particle position according equation
                int stopcycle = 0;
                do {
                    stopcycle++;
                    particles[i].updateVelocity(globalBest);
                    particles[i].updatePosition();
                    particles[i].chordSequenceFitness();
                }while (!particles[i].isValid&&stopcycle<100);


                //updates info about globalBest
                setGlobalBest(getBest());

            }
            isSorted = false;
            setGlobalBest(getBest());
            currentIteration++;
            if (bestNumberOfChords()==Particle.NUM_OF_CHORDS) break;
        }
    }


    /**
     * This method is used to generate melody sequence
     * This method has the same logic as a PSO1 but it uses different fitness functions for particles
     * @param iterations
     */
    public void PSO2(int iterations) throws Exception {
        NUMBER_OF_ITERATIONS = iterations;

        globalBest = getBest();//set the best in particle as global best
        while(currentIteration< NUMBER_OF_ITERATIONS) {

            //for each particle in the flock
            for (int i = 0; i < SIZE; i++) {
                if (i == 0){
                    particles[i].melodyFitness();//update sequence fitness
                    particles[i].setBestFitness(particles[i].getFitness());//set initial fitness as best fitness
                }
                //Calculate particle velocity according equation
                //Update particle position according equation
                int stopcycle = 0;
                do {
                    stopcycle++;
                    particles[i].updateVelocity(globalBest);
                    particles[i].updatePosition();
                    particles[i].melodyFitness();
                }while (!particles[i].isValid&&stopcycle<100);
                //breaks if it cannot generate true valid chord.
                //It cannot happen as we always normalize vector

                //updates info about globalBest
                setGlobalBest(getBest());

            }
            isSorted = false;
            setGlobalBest(getBest());
            currentIteration++;

        }
    }


    /**
     *
     * @return SIZE of the particle
     */
    public int size(){
        return particles.length;
    }

    public void printFlock(int border){

        if (border > SIZE) border = SIZE; //if border is wrong
        for (int i = 0; i < border; i++) {
            System.out.println();
            System.out.println(particles[i]);
        }
    }

    public int bestNumberOfChords() {
        int goodChords = 0;
        double[] position = globalBest;
        for (int i = 0; i < Particle.DIMENSIONS; i += 3) {

            int firstNote = (int) Math.round(position[i]);
            int secondNote = (int) Math.round(position[i + 1]);
            int thirdNote = (int) Math.round(position[i + 2]);

            int tonalityOffset = Particle.TONALITY % 12;//how far from the beginning  of octave is tonality

            if (Particle.IS_MINOR) {//conditions for minor tonality
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


                        /* -- Getters&Setters --*/

    public double[] getGlobalBest() {
            return globalBest;
    }

    public void setGlobalBest(double[] globalBest) {
        this.globalBest = globalBest;
    }
    /**
     *
     * @param index
     * @return particle from flock
     */
    public Particle get(int index){
        return particles[index];
    }
    /**
     * sets a given chord in flock at given index
     * @param index index where to set a chord
     * @param particle
     */
    public void set(int index, Particle particle){
        particles[index] = particle;
    }
    public double[] getBest(){
        int fitness = 0;
        double[] best = globalBest;
        for (int i = 0; i < SIZE; i++) {
            if (particles[i].getFitness()>=fitness){
                best = particles[i].getPosition();
                fitness = particles[i].getFitness();
            }
        }
        globelBestFitness = fitness;
        return best;
    }


                        /* -- Override -- */

    @Override
    public String toString() {
        return "Flock{" +
                "particles=" + Arrays.toString(particles) +
                ", SIZE=" + SIZE +
                ", isSorted=" + isSorted +
                ", globalBest=" + Arrays.toString(globalBest) +
                '}';
    }

}