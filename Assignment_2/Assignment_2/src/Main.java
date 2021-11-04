import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * Runs a GA of a shredded document and outputs the best deshredded document found
 *
 * Run: Enter new name of file to store data,
 * enter name of shredded document,
 * enter all the parameters requested, probability in percentage
 *
 * @author Harmandeep Mangat 6021109
 */

public class Main {
    int popSize;
    Chromosome[] initialPopulation;
    double bestGenerationFitness = 10000000;
    double bestFitness = 10000000;
    int[] bestGenerationSolutionChromosome;
    double averagePopulationFitness;
    public Main(){
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter new file name to store data: ");
            String fileName = scanner.nextLine();
            File obj = new File(fileName);
            while (!obj.createNewFile()) {
                System.out.print("Enter new file name: ");
                fileName = scanner.nextLine();
                obj = new File(fileName);
            }
            FileWriter writer = new FileWriter(fileName);
            BufferedWriter myWriter = new BufferedWriter(writer);

            geneticAlgorithm(myWriter);
            myWriter.close();
            writer.close();
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Runs a genetic algorithm
     */
    private void geneticAlgorithm(BufferedWriter myWriter) throws IOException {
        Scanner scanner;

        while (true) {
            long seed = new Random().nextLong();
            Random random = new Random(seed);
            scanner = new Scanner(System.in);

            System.out.print("Enter File Name of shredded document or exit to exit: ");
            String fileName = scanner.nextLine();
            if (fileName.equals("exit")) break;
            myWriter.append("File Name: " + fileName);
            myWriter.newLine();

            System.out.print("Enter Population Size: ");
            popSize = scanner.nextInt();
            myWriter.append("Population Size: " + popSize);
            myWriter.newLine();

            System.out.print("Enter generation Span: ");
            int generationSpan = scanner.nextInt();
            myWriter.append("Generation Span: " + generationSpan);
            myWriter.newLine();

            System.out.print("Enter Crossover probability: ");
            double crossoverProbability = scanner.nextDouble();
            myWriter.append("Crossover Probability: " + crossoverProbability);
            myWriter.newLine();

            System.out.print("Enter Mutation Crossover probability: ");
            double mutationProbability = scanner.nextDouble();
            myWriter.append("Mutation Probability: " + mutationProbability);
            myWriter.newLine();

            scanner = new Scanner(System.in);
            System.out.print("Choose Crossover; 1 (order crossover), 2 (Uniform Crossover), 3 (Modified Ordered Crossover): ");
            int crossType = scanner.nextInt();
            boolean OXType = false;
            boolean UXType = false;
            boolean SXType = false;
            switch (crossType) {
                case 2:
                    UXType = true;
                    System.out.println("Crossover Type: Uniform Crossover");
                    System.out.println();

                    myWriter.append("Crossover Type: Uniform Crossover");
                    myWriter.newLine();
                    break;

                case 3:
                    SXType = true;
                    System.out.println("Crossover Type: Modified Ordered Crossover");
                    System.out.println();

                    myWriter.append("Crossover Type: Modified Ordered Crossover");
                    myWriter.newLine();
                    break;
                default:
                    OXType = true;
                    System.out.println("Crossover Type: Order Crossover");
                    System.out.println();

                    myWriter.append("Crossover Type: Order Crossover");
                    myWriter.newLine();
            }

            System.out.println("Random Number Seed: "+ seed);
            myWriter.append("Random Number Seed:" + seed);
            myWriter.newLine();

            initialPopulation = new Chromosome[popSize];
            char[][] shreddedDocument = FitnessCalculator.getShreddedDocument(fileName);
            setInitialPopulation(shreddedDocument, random);
            bestGenerationFitness(myWriter);
            setAveragePopulationFitness(myWriter);

            for (int i = 0; i <= generationSpan; i++) {
                initialPopulation = tournament(random);
                if (OXType) {
                    initialPopulation = orderCrossover(shreddedDocument, crossoverProbability, random);
                } else if (UXType){
                    initialPopulation = uniformCrossover(shreddedDocument, crossoverProbability, random);
                } else if (SXType) {
                    initialPopulation = modifiedOrderedCrossover(shreddedDocument, crossoverProbability, random);
                }
                System.out.println();
                myWriter.newLine();

                for (int j = 0; j < popSize; j++) {
                    initialPopulation[j].chromosome = inversionMutation(initialPopulation[j].chromosome,
                            mutationProbability, random);
                    initialPopulation[j].fitness = FitnessCalculator.fitness(shreddedDocument,
                            initialPopulation[j].chromosome);
                }
                elitism();
                bestGenerationFitness(myWriter);
                setAveragePopulationFitness(myWriter);
                System.out.println();
            }
            System.out.println("Best Fitness: " + bestFitness);
            System.out.print("Best Chromosome: ");

            myWriter.append("Best Fitness: " + bestFitness);
            myWriter.newLine();

            myWriter.append("Best Chromosome: ");
            print(bestGenerationSolutionChromosome, myWriter);

            char[][] unshredded = FitnessCalculator.unshred(shreddedDocument, bestGenerationSolutionChromosome);
            FitnessCalculator.prettyPrint(unshredded, myWriter);
            bestFitness = 10000000;
        }
        scanner.close();
    }

    /**
     * Creates the initial population
     * @param shreddedDocument the array holding the shredded document
     */
    private void setInitialPopulation(char[][] shreddedDocument, Random random) {
        for (int i = 0; i < popSize; i++) {
            int[] chromo = new int[15];
            for (int j = 0; j < 15 ; j++) {
                int gene = random.nextInt(15);
                while (checkDuplicate(chromo,j,gene)) {
                    gene = random.nextInt(15);
                }
                chromo[j] = gene;
            }
            Chromosome chromosome = new Chromosome(chromo, FitnessCalculator.fitness(shreddedDocument, chromo));
            initialPopulation[i] = chromosome;
        }
    }

    /**
     * Finds the best fitness of the generation
     */
    private void bestGenerationFitness(BufferedWriter myWriter) throws IOException {
        for (int i = 0; i < popSize; i++) {
            if (initialPopulation[i].fitness < bestGenerationFitness) {
                bestGenerationFitness = initialPopulation[i].fitness;
                setBestFitness(i);
            }
        }
        System.out.println("Best Generation Fitness: " + bestGenerationFitness);
        myWriter.append("Best Generation Fitness: "+ bestGenerationFitness);
        myWriter.newLine();
        bestGenerationFitness = 10000000;
    }

    /**
     * Sets the best fitness of the GA
     * @param i index of the array
     */
    private void setBestFitness(int i) {
        if (bestGenerationFitness < bestFitness) {
            bestFitness = bestGenerationFitness;
            bestGenerationSolutionChromosome = initialPopulation[i].chromosome;
        }
    }

    /**
     * Calculates the average population fitness
     * @param myWriter
     */
    private void setAveragePopulationFitness(BufferedWriter myWriter) throws IOException {
        double totalFitness = 0;
        for (int i = 0; i < popSize; i++) {
            totalFitness = totalFitness + initialPopulation[i].fitness;
        }
        averagePopulationFitness = totalFitness/popSize;
        System.out.println("Average Population Fitness: " + averagePopulationFitness);
        myWriter.append("Average Population Fitness: " + averagePopulationFitness);
        myWriter.newLine();    }

    /**
     * checks to see if the gene already exists
     * decides if the gene can be inserted into the chromosome
     * @param chromosome array holding the genes that don't change
     * @param currentSize size of the chromosome array
     * @param gene the gene to be transferred to the child
     * @return true if gene is already in the chromosome, false if not
     */
    private boolean checkDuplicate(int[] chromosome, int currentSize, int gene) {
        for (int i = 0; i < currentSize ; i++) {
            if (chromosome[i] == gene) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does a tournament selection with k = 2, k represents the number of genes to compare
     * @param random
     * @return new population
     */
    private Chromosome[] tournament(Random random) {
        Chromosome[] temp = new Chromosome[popSize];
        for (int i = 0; i < popSize; i++) {
            int index1 = random.nextInt(popSize);
            int index2 = random.nextInt(popSize);
            while (index2 == index1) index2 = random.nextInt(popSize);
            if (initialPopulation[index1].fitness <= initialPopulation[index2].fitness) {
                temp[i] = initialPopulation[index1];
            } else {
                temp[i] = initialPopulation[index2];
            }
        }
        return temp;
    }

    /**
     * Does a order crossover and replaces the parents with the children
     * @param shreddedDocument the array holding the shredded document
     * @param probability probability rate of crossover
     * @param random
     * @return new population
     */
    private Chromosome[] orderCrossover(char[][] shreddedDocument, double probability, Random random) {
        int[] temp;
        Chromosome[] newArray = new Chromosome[popSize];
        Chromosome chromosome;
        if (probability == 0) return initialPopulation;

        for (int i = 0; i < initialPopulation.length; i++) {
            int index1 = random.nextInt(100);
            int index2 = random.nextInt(100);

            double percentage = Math.random()*100;

            while (probability < percentage || index1 == index2) {
                index1 = random.nextInt(100);
                index2 = random.nextInt(100);
                percentage = Math.random();
            }
            int[] parent1 = initialPopulation[index1].chromosome;
            int[] parent2 = initialPopulation[index2].chromosome;

            int starting = random.nextInt(15);
            int length = random.nextInt(15);
            while ((starting+length) > 15 || (starting+length) == 0 || (starting-length) == 0 || length == 0) {
                starting = random.nextInt(15);
                length = random.nextInt(15);
            }
            temp = sequentialElements(length, starting, parent1);
            int[] child1 = createChildOX(parent2,temp, starting);

            chromosome = new Chromosome(child1,FitnessCalculator.fitness(shreddedDocument,child1));
            newArray[i] = chromosome;

            temp = sequentialElements(length, starting, parent2);
            int[] child2 = createChildOX(parent1, temp, starting);

            chromosome = new Chromosome(child2,FitnessCalculator.fitness(shreddedDocument,child2));
            newArray[i+1] = chromosome;

            i++;
        }
        return newArray;
    }

    /**
     * Creates an array holding the sequential elements to use for order crossover
     * @param length size of the array that is going to hold the genes that don't change
     * @param starting the index of the starting constant gene
     * @param chromo the chromosome
     * @return sequential element array
     */
    private int[] sequentialElements(int length, int starting, int[] chromo) {
        int[] temp = new int[length];
        for (int i = 0; i < length; i++) {
            temp[i] = chromo[starting];
            starting+=1;
        }
        return temp;
    }

    /**
     * creates the child using order crossover
     * @param parent the parent chromosome that wasn't used to create the sequential elements array
     * @param temp the array holding the genes that didn't change
     * @param starting the index of where the temp array began from the other parent
     * @return Child
     */
    private int[] createChildOX(int[] parent, int[] temp, int starting) {
        int[] child = new int[15];
        int index = 0;
        for (int i = 0; i < child.length; i++) {
            if (i == starting) {
                for (int j = 0; j < temp.length; j++) {
                    child[index] = temp[j];
                    index++;
                }
            }
            if (!checkDuplicate(temp,temp.length,parent[i])) {
                child[index] = parent[i];
                index++;
            }
        }
        return child;
    }

    /**
     * Does a Uniform Crossover and replaces the parents
     * @param shreddedDocument the array holding the shredded document
     * @param probability probability rate of the crossover
     * @param random
     * @return new population
     */
    private Chromosome[] uniformCrossover(char[][] shreddedDocument, double probability, Random random) {
        if (probability == 0) return initialPopulation;

        Chromosome[] newArray = new Chromosome[popSize];
        Chromosome chromosome;
        int size = 0;
        int[] mask = new int[15];
        for (int i = 0; i < initialPopulation.length; i++) {
            for (int j = 0; j < mask.length; j++) {
                mask[j] = random.nextInt(2);
                if (mask[j] == 1) size++;
            }
            int index1 = random.nextInt(100);
            int index2 = random.nextInt(100);

            double percentage = Math.random()*100;

            while (probability < percentage || index1 == index2) {
                index1 = random.nextInt(100);
                index2 = random.nextInt(100);
                percentage = Math.random();
            }

            int[] parent1 = initialPopulation[index1].chromosome;
            int[] parent2 = initialPopulation[index2].chromosome;

            int[] temp = unchangedGenesUOX(parent1, mask, size);
            int[] child1 = createChildUOX(mask,parent1,parent2,temp);

            chromosome = new Chromosome(child1,FitnessCalculator.fitness(shreddedDocument,child1));
            newArray[i] = chromosome;


            temp = unchangedGenesUOX(parent2, mask, size);
            int[] child2 = createChildUOX(mask,parent2,parent1,temp);

            chromosome = new Chromosome(child2,FitnessCalculator.fitness(shreddedDocument,child2));
            newArray[i+1] = chromosome;

            i++;
        }
        return newArray;
    }

    /**
     * prints the chromosome
     * @param array the chromosome
     */
    private void print(int[] array, BufferedWriter myWriter) throws IOException {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            myWriter.append(array[i] + " ");
        }
        System.out.println();
        myWriter.newLine();
    }

    /**
     * Using the mask, figures out the unchanged genes from the parent and creates an array holding those
     * unchanged genes
     * @param parent one of the parents to use the mask on
     * @param mask an array of 1s and 0s, where 1 represents the genes to keep and 0, to change
     * @param size number of 1s in the mask
     * @return array holding unchanged genes
     */
    private int[] unchangedGenesUOX(int[] parent, int[] mask, int size) {
        int[] unchangedGenes = new int[size];
        int index = 0;
        for (int i = 0; i < parent.length; i++) {
            if (mask[i] == 1) {
                unchangedGenes[index] = parent[i];
                index++;
            }
        }
        return unchangedGenes;
    }

    /**
     * creates the child using uniform crossover
     * @param mask array of 1s and 0s
     * @param parent1 the parent the mask was used on
     * @param parent2 the parent the mask wasn't used on
     * @param temp the array holding the genes that don't change
     * @return child
     */
    private int[] createChildUOX(int[] mask, int[] parent1, int[] parent2, int[] temp) {
        int index = 0;
        int index2 = 0;
        int[] child = new int[15];
        for (int i = 0; i < 15; i++) {
            if (mask[i] == 1) {
                child[index] = parent1[i];
                index++;
            } else {
                while (index2 < 15 && checkDuplicate(temp,temp.length,parent2[index2])) {
                    index2++;
                }
                if (index2 < 15) {
                    child[index] = parent2[index2];
                    index++;
                    index2++;
                }
            }
            if (index == 15) break;
        }
        return child;
    }

    /**
     * Does an inversion mutation on a child
     *
     * Randomly picks sequential elements and reverse the order
     * @param child the chromosome to apply mutation
     * @param probability probability rate of mutation
     * @param random
     * @return mutated child 
     */
    private int[] inversionMutation(int[] child, double probability, Random random) {
        if (probability == 0) return child;
        double percentage = Math.random() * 100;

        if (probability >= percentage) {
            int starting = random.nextInt(15);
            int length = random.nextInt(15);
            while ((starting+length) > 15 || (starting+length) == 0 || (starting-length) == 0) {
                starting = random.nextInt(15);
                length = random.nextInt(15);
            }
            int temp;
            int endPoint = starting + length -1;
            for (int i = starting; i < (starting+length); i++) {
                temp = child[endPoint];
                child[endPoint] = child[i];
                child[i] = temp;
                endPoint--;
                if (endPoint <= i) break;
            }
        }
        return child;
    }

    /**
     * replaces the chromosome that has the worst fitness in the new generation with the chromosome that has the
     * best fitness from the old generation
     */
    private void elitism () {
        int indexOfWorst = 0;
        double worstFitness = 0;
        for (int i = 0; i < popSize; i++) {
            if (initialPopulation[i].fitness > worstFitness) {
                worstFitness = initialPopulation[i].fitness;
                indexOfWorst = i;
            }
        }
        initialPopulation[indexOfWorst].chromosome = bestGenerationSolutionChromosome;
        initialPopulation[indexOfWorst].fitness = bestFitness;
    }

    /**
     * modified ordered crossover
     * implements female mate choice, where a female picks the male with the best genes from a group
     * @param shreddedDocument the array holding the shredded document
     * @param probability probability rate of the crossover
     * @param random
     * @return
     */
    private Chromosome[] modifiedOrderedCrossover(char[][] shreddedDocument, double probability, Random random) {
        int[] temp;
        Chromosome[] newArray = new Chromosome[popSize];
        Chromosome chromosome;
        if (probability == 0) return initialPopulation;

        for (int i = 0; i < initialPopulation.length; i++) {
            int index1 = random.nextInt(100);

            double percentage = Math.random()*100;

            while (probability < percentage) {
                index1 = random.nextInt(100);
                percentage = Math.random();
            }
            int[] parent1 = initialPopulation[index1].chromosome;
            int[] parent2 = bestMaleMate(random);

            int starting = random.nextInt(15);
            int length = random.nextInt(15);
            while ((starting+length) > 15 || (starting+length) == 0 || (starting-length) == 0 || length == 0) {
                starting = random.nextInt(15);
                length = random.nextInt(15);
            }
            temp = sequentialElements(length, starting, parent1);
            int[] child1 = createChildOX(parent2,temp, starting);

            chromosome = new Chromosome(child1,FitnessCalculator.fitness(shreddedDocument,child1));
            newArray[i] = chromosome;

            temp = sequentialElements(length, starting, parent2);
            int[] child2 = createChildOX(parent1, temp, starting);

            chromosome = new Chromosome(child2,FitnessCalculator.fitness(shreddedDocument,child2));
            newArray[i+1] = chromosome;

            i++;
        }
        return newArray;
    }

    /**
     * out of three possible chromosomes that could potentially go through a crossover, choose the best one
     * @param random
     * @return best chromosome to do a crossover with
     */
    private int[] bestMaleMate(Random random) {
        int[] bestMale = new int[0];
        double currentBestMaleFitness = -1;
        for (int i = 0; i < 3; i++) {
            Chromosome prospectMate = initialPopulation[random.nextInt(100)];
            if (currentBestMaleFitness == -1) {
                bestMale = prospectMate.chromosome;
                currentBestMaleFitness = prospectMate.fitness;
            } else if (prospectMate.fitness < currentBestMaleFitness){
                bestMale = prospectMate.chromosome;
                currentBestMaleFitness = prospectMate.fitness;
            }
        }
        return bestMale;
    }

    public static void main (String[] args) {Main m = new Main();}
}
