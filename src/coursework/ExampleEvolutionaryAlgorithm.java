package coursework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that extends {@link NeuralNetwork} 
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork {
	

	/**
	 * The Main Evolutionary Loop
	 */
	int chromosomePointer = 0;
	/*
	 * TODO - Replace more chomosomes from the population, e.g. 10% using for loop
	 * 		- Check reproduce if it works correctly
	 * 		- Change activation function to be Adam
	 * 		- Only replace if the new individual has better fitness score than the worst in the current population
	 * 		- Make sure the parents are different individuals
	 * 		- Try with binary tournament
	 * 		- Change mutation to use gausiom distrobution
	 * 		- Replace bigger portion of the population.
	 */
	@Override
	public void run() {		
		//Initialise a population of Individuals with random weights
		population = initialise();

		//Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */		
		
		while (evaluations < Parameters.maxEvaluations) {

			/**
			 * this is a skeleton EA - you need to add the methods.
			 * You can also change the EA if you want 
			 * You must set the best Individual at the end of a run
			 * 
			 */
			
			// Select 2 Individuals from the current population. Currently returns random Individual
			Individual parent1 = select(); 
			Individual parent2 = select();
//			System.out.print("Parent1: "+parent1.fitness);
//			System.out.print("\nParent2: "+parent2.fitness);
			
			// Avoid selecting the same parent twice
			if (parent1 == parent2) {
				System.out.print("\n\tOuch, the same parent\n");
//				parent2 = select();
			}

			// Generate a child by crossover. Not Implemented			
			ArrayList<Individual> children = reproduce(parent1, parent2);			
			
			//mutate the offspring
			mutate(children);
			
			// Evaluate the children
			evaluateIndividuals(children);			

			// Replace children in population
			replace(children);

			// check to see if the best has improved
			best = getBest();
			
			// Implemented in NN class. 
			outputStats();
			
			//Increment number of completed generations			
		}

		//save the trained network to disk
		saveNeuralNetwork();
	}

	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals) {
		for (Individual individual : individuals) {
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}


	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest() {
		best = null;
		for (Individual individual : population) {
			if (best == null) {
				best = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
			}
		}
		return best;
	}

	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise() {
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i) {
			//chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}

	/**
	 * Selection
	 * 
	 * NEEDS REPLACED with proper selection this just returns a copy of a random
	 * member of the population
	 */
	/* ROULETTE */
	private Individual rouletteSelection() {
		double fitnessSum = 0;
		for (int i = 0; i < population.size(); i++) {
			fitnessSum += 1.0 / population.get(i).fitness;
		}

		Individual parent = null;

		double randomNumber = fitnessSum * Parameters.random.nextDouble();
		fitnessSum = 0;

		for (int i = 0; i < population.size(); i++) {
			fitnessSum += 1.0 / population.get(i).fitness;
			if (fitnessSum > randomNumber) {
				parent = population.get(i);
				break;
			}
		}
		return parent;
	}

	private Individual tournamentSelection() {
		// The tournament size of 10% of the population
		// K - smaller tournament size
		int tournamentSize = (int)(population.size()*0.1);
		TreeMap<Integer, Individual> potentialParents = new TreeMap<Integer, Individual>();

		while(potentialParents.size() < tournamentSize) {
			int randomIndex = Parameters.random.nextInt(population.size());
			potentialParents.put(randomIndex, population.get(randomIndex));
		}

		double bestFitness = Integer.MAX_VALUE;
		Individual chosenParent = null;

		for(Entry<Integer, Individual> entry : potentialParents.entrySet()) {
			Individual individual = entry.getValue();
			double currentFitness = individual.fitness;

			if (chosenParent == null) {
				chosenParent = individual;
				bestFitness = currentFitness;
			} else if(bestFitness > currentFitness) {
				chosenParent = individual;
				bestFitness = currentFitness;
			}
		}

		return chosenParent;
	}

	private Individual select() {
		Individual parent = null;

		/* Selects based on the their fitness*/
//		parent = rouletteSelection();

		/* Selects based on tournament */
		parent = tournamentSelection();

		return parent.copy();
	}

	private void reproduceHelper(Individual mother, Individual father, Individual childOne, Individual childTwo, int range, int previousLayerSize, int curretLayerSize) {
		int neuronCounter = 0;
		ArrayList<Double> neuronFather = new ArrayList<Double>();
		ArrayList<Double> neuronMother = new ArrayList<Double>();
		ArrayList<Double> childOneBiases = new ArrayList<Double>();
		ArrayList<Double> childTwoBiases = new ArrayList<Double>();
		
		for (int i = chromosomePointer; i < range; i++) {
			neuronFather.add(father.chromosome[i]);
			neuronMother.add(mother.chromosome[i]);
			if ((i+1) % previousLayerSize == 0) {
				neuronCounter += 1;
				int index = (curretLayerSize - neuronCounter) * previousLayerSize + neuronCounter;
				
				if (Parameters.random.nextBoolean()) {
					for (int j = 0; j < neuronFather.size(); j++) {
						childOne.chromosome[chromosomePointer] = neuronFather.get(j);
						childTwo.chromosome[chromosomePointer] = neuronMother.get(j);
						chromosomePointer += 1;
					}
					childOneBiases.add(father.chromosome[i+index]);
					childTwoBiases.add(mother.chromosome[i+index]);
				}
				else {
					for (int j = 0; j < neuronFather.size(); j++) {
						childOne.chromosome[chromosomePointer] = neuronMother.get(j);
						childTwo.chromosome[chromosomePointer] = neuronFather.get(j);
						chromosomePointer += 1;
					}
					childOneBiases.add(mother.chromosome[i+index]);
					childTwoBiases.add(father.chromosome[i+index]);
				}
				
				neuronFather.clear();
				neuronMother.clear();
			}
		}
		
		for (int i = 0; i<childOneBiases.size(); i++) {
			childOne.chromosome[chromosomePointer] = childOneBiases.get(i);
			childTwo.chromosome[chromosomePointer] = childTwoBiases.get(i);
			chromosomePointer += 1;
		}
	}

	private void crossoverPerNeuron(ArrayList<Individual> children, Individual parent1, Individual parent2)
	{
		Individual childOne = new Individual();
		Individual childTwo = new Individual();

		chromosomePointer = 0;

		int range = Parameters.getNumHidden() * NeuralNetwork.numInput;
		reproduceHelper(parent1, parent2, childOne, childTwo, range, NeuralNetwork.numInput, Parameters.getNumHidden());

		range = parent1.chromosome.length;
		reproduceHelper(parent1, parent2, childOne, childTwo, range, Parameters.getNumHidden(), NeuralNetwork.numOutput);

		children.add(childOne.copy());
		children.add(childTwo.copy());
	}

	private void onePointCrossOver(ArrayList<Individual> children, Individual parent1, Individual parent2, int cutPoint) 
	{
		Individual childOne = new Individual();
		Individual childTwo = new Individual();

		// This assumes that parent1 and parent2 have chromosomes with the same length
		int chromosomeLength = parent1.chromosome.length;

		for(int i = 0; i < cutPoint; i++) {
			childOne.chromosome[i] = parent1.chromosome[i];
			childTwo.chromosome[i] = parent2.chromosome[i];
		}

		for(int i = cutPoint; i < chromosomeLength; i++) {
			childOne.chromosome[i] = parent2.chromosome[i];
			childTwo.chromosome[i] = parent1.chromosome[i];
		}

		children.add(childOne);
		children.add(childTwo);
	}
	/**
	 * Crossover / Reproduction
	 *
	 * NEEDS REPLACED with proper method this code just returns exact copies of the
	 * parents.
	 */
	private ArrayList<Individual> reproduce(Individual parent1, Individual parent2) {

		ArrayList<Individual> children = new ArrayList<>();

		/* Swaps the weights per neuron */
		//crossoverPerNeuron(children, parent1, parent2);

		/* One point crossover */
		// This assumes that parent1 and parent2 have chromosomes with the same length
		// K - random cutPoint
		int cutPoint = NeuralNetwork.numInput*Parameters.getNumHidden()+Parameters.getNumHidden();
		onePointCrossOver(children, parent1, parent2, cutPoint);

		/*DEBUG INF0*/
//		System.out.println("parent1: "+Arrays.toString(parent1.chromosome));
//		System.out.println("parent2: "+Arrays.toString(parent2.chromosome));
//		System.out.println("childOne After: "+Arrays.toString(children.get(0).chromosome));
//		System.out.println("childTwo After: "+Arrays.toString(children.get(1).chromosome));
//		System.exit(0);
		/***********************************/

		return children;
	} 
	
	/**
	 * Mutation
	 * 
	 * 
	 */
	float reduceFactor = 1.0f;
	private void mutate(ArrayList<Individual> individuals) {
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() > 0.85) {
					// K - smaller change
					double change = Parameters.random.nextDouble() - 0.05;
					individual.chromosome[i] += (change*reduceFactor);
				}
//				if (Parameters.random.nextDouble() < Parameters.mutateRate) {
//					if (Parameters.random.nextBoolean()) {
//						individual.chromosome[i] += (Parameters.mutateChange);
//					} else {
//						individual.chromosome[i] -= (Parameters.mutateChange);
//					}
//				}
			}
		}
//		reduceFactor*=0.9998f;
//		System.out.println(reduceFactor);
	}

	/**
	 * 
	 * Replaces the worst member of the population 
	 * (regardless of fitness)
	 * 
	 */
	private void replaceWorstRegardlessOfFitness(ArrayList<Individual> individuals)
	{
		for(Individual individual : individuals)
		{
			int idx = getWorstIndex();
			population.set(idx, individual);
		}
	}

	private int getWorstIndexFromSubpopulation(TreeMap<Integer, Individual> individuals) {
		Individual worst = null;
		int idx = -1;
		for(Entry<Integer, Individual> entry : individuals.entrySet()) {
			Integer i = entry.getKey();
			Individual individual = entry.getValue();

			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i;
			}
		}
		return idx;
	}

	private void tournamentReplacement(ArrayList<Individual> newIndividuals)
	{
		for(Individual individual : newIndividuals) {
			// The tournament size of 10% of the population
			int tournamentSize = (int)(population.size()*0.2);
			TreeMap<Integer, Individual> potentialMembersToBeReplaced = new TreeMap<Integer, Individual>();

			while(potentialMembersToBeReplaced.size() < tournamentSize) {
				int randomIndex = Parameters.random.nextInt(population.size());
				potentialMembersToBeReplaced.put(randomIndex, population.get(randomIndex));
			}

			int indexToBeReplaced = getWorstIndexFromSubpopulation(potentialMembersToBeReplaced);
			if(population.get(indexToBeReplaced).fitness > individual.fitness) {
				population.set(indexToBeReplaced, individual);
			}
		}
	}

	private void replace(ArrayList<Individual> individuals) {
		// Replace the worst one regardless of the fitness of the new member
		//replaceWorstRegardlessOfFitness(individuals);

		// Perform tournament replacement
		tournamentReplacement(individuals);
	}



	/**
	 * Returns the index of the worst member of the population
	 * @return
	 */
	private int getWorstIndex() {
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i; 
			}
		}
		return idx;
	}

	@Override
	public double activationFunction(double x) {
		if (x < -20.0) {
			return -1.0;
		} else if (x > 20.0) {
			return 1.0;
		}
		return Math.tanh(x);
	}
}
