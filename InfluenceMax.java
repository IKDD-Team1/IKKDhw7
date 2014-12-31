import java.io.*;
import java.util.Scanner;
import java.util.Stack;
import java.util.Random;
import java.util.Vector;

public class InfluenceMax
{
	private Vector< Node > network_ = new Vector< Node >();
	private Vector< Integer > numOfElement_ = null;

	/* Build the network of each node from the input file.
	 */
	public void buildNetwork( String filePath )
	throws FileNotFoundException, IOException
	{
		int sourceNodeIDNow = 0;
		int sourceNodeID = -1;
		Vector< Integer > neighborsID = new Vector< Integer >();
		Scanner fileInput = new Scanner( new File( filePath ) );

		while ( fileInput.hasNextInt() )
		{
			sourceNodeID = fileInput.nextInt();

			// If it gets a new source ID, store the neighbors of the last sourceID
			// into the network.
			if ( sourceNodeID != sourceNodeIDNow )
			{
				Node node = new Node();
				node.neighbors = neighborsID;
				network_.add( node );

				neighborsID = new Vector< Integer >();

				sourceNodeIDNow = sourceNodeID;
			}

			neighborsID.add( fileInput.nextInt() );
		}

		// Store the last one node
		Node lastNode = new Node();
		lastNode.neighbors = neighborsID;
		network_.add( lastNode );
	}

	/* Print all the neighbors of the specified node to the console.
	 */
	public void printNeighbors( int[] nodeIDs )
	{
		System.out.println( "There are " + network_.size() + " nodes in the network." );
		for ( int nodeID : nodeIDs )
		{
			if ( nodeID > network_.size() )
			{
				System.out.println( "Out of range! Ignore node " + nodeID + "." );
				continue;
			}

			System.out.println( "The neighbors of node " + nodeID + " :" );
			System.out.println( network_.get( nodeID ).neighbors.toString() );
		}

	}

	/* Find out the set of seed nodes that has the max f(S) with pre-decided propagation probability.
	 * Then search the graph by DFS.
	 */
	public void DFSGreedyIC( int numOfSeeds, float probability, int iterations )
	{
		boolean[] selected = new boolean[ network_.size() ];
		int[] IDofSeeds = new int[ numOfSeeds ];
		int[] allFofS = new int[ network_.size() ];
		int resultFofS = 0;
		Vector< Node > trimmed_network = null;

		for ( int seedIndex = 0; seedIndex < numOfSeeds; ++seedIndex )
		{
			// Initialize all F(S) to be 0.
			for ( int i = 0; i < allFofS.length; ++i )
				allFofS[i] = 0;

			System.out.print( "Finding " + ( seedIndex+1 ) + "th seed...  " );

			// Evaluation total influence f(S) by Monte Carlo simulation
			for ( int i = 0; i < iterations; ++i )
			{
				// Pre-decide propagation probability
				trimmed_network = preDecideProbability( probability );
				// Check if the node in the graph is accessiable from the seed set
				// , and index them
				int[] componentID =
					getComponentID( trimmed_network, IDofSeeds, seedIndex );

				for ( int candidateID = 0; candidateID < network_.size(); ++candidateID )
				{
					if ( selected[ candidateID ] ) continue;

					if ( componentID[ candidateID ] != 0 )
						allFofS[ candidateID ] +=
						numOfElement_.get( componentID[ candidateID ] ).intValue();
				}
			}

			// Calculate the maximum and avarge the F(S)
			int theMaxCandidateID = -1;
			int theMaxFofS = 0;

			for ( int i = 0; i < allFofS.length; ++i )
				if ( allFofS[i] > theMaxFofS )
				{
					theMaxFofS = allFofS[i];
					theMaxCandidateID = i;
				}

			System.out.println( "ID: " + theMaxCandidateID );
			selected[ theMaxCandidateID ] = true;

			resultFofS += theMaxFofS / iterations;
		}

		System.out.println( "Total influence f(S) : " + resultFofS );

	}

	public void testFunction( float probability )
	{
		Vector< Node > trimmed_network = preDecideProbability( probability );

		for ( int i = 0; i < trimmed_network.size(); ++i )
		{
			System.out.println( i + " : " + trimmed_network.get(i).neighbors.toString() );
		}
		int[] selected = {};
		int[] componentID = getComponentID( trimmed_network, selected, selected.length );

		for ( int i = 0; i < componentID.length; ++i )
			System.out.println( i + " : " + componentID[i] );

		System.out.println( "Number of elements : " + numOfElement_.toString() );
	}

	/* Pre-decide the propagation probability of each eage.
	 * And return the trimmed network, which the neighbor not accessiable
	 * from the source node is removed.
	 */
	private Vector< Node > preDecideProbability( float probability )
	{
		Random random = new Random( System.currentTimeMillis() );
		int randomThreshold = (int)( probability * 100.0f );
		Vector< Node > trimmed_network = new Vector< Node >();

		// Initialize the trimmed_network
		for ( int i = 0; i < network_.size(); ++i )
			trimmed_network.add( new Node() );

		for ( int sourceNodeID = 0; sourceNodeID < network_.size(); ++sourceNodeID )
		{
			int numOfNeighbors = network_.get( sourceNodeID ).neighbors.size();
			Vector< Integer > neighbors = network_.get( sourceNodeID ).neighbors;
			Vector< Integer > trimmed_neighbors = trimmed_network.get( sourceNodeID ).neighbors;
			int neighborID = 0;

			for ( int i = 0; i < numOfNeighbors; ++i )
			{
				neighborID = neighbors.get(i).intValue();

				if ( neighborID > sourceNodeID &&
					( random.nextInt() % 100 ) < randomThreshold )
				{
					trimmed_neighbors.add( neighborID );

					Node neighborNode = trimmed_network.get( neighborID );
					neighborNode.neighbors.add( sourceNodeID );
					trimmed_network.set( neighborID, neighborNode );
				}
			}

			Node node = new Node();
			node.neighbors = trimmed_neighbors;
			trimmed_network.set( sourceNodeID, node );
		}

		return trimmed_network;
	}

	/* Get the components in the graph. The nodes having the same ID are in the same component.
	 * And the component ID of the node accessible from the seed is 0.
	 * Count the number of the emelent in each component.
	 */
	private int[] getComponentID( Vector< Node > network, int[] selectedSeeds, int seedIDNow )
	{
		int[] componentID = new int[ network.size() ];
		int componentIDNow = 0;
		int elementCounter = 0;
		Stack< Integer > newActiveNode = new Stack< Integer >();

		// Initialze the counter of the number of the node in the component
		numOfElement_ = new Vector< Integer >();

		// Initialze the component ID of each node to be -1,
		// which means the node hasn't been checked.
		for ( int i = 0; i < componentID.length; ++i )
			componentID[i] = -1;

		for ( int i = 0; i < seedIDNow; ++i )
		{
			componentID[ selectedSeeds[i] ] = 0;
			++elementCounter;
			newActiveNode.push( selectedSeeds[i] );
		}

		// DFS: Find the nodes which are accessible from the seed.
		while ( !newActiveNode.empty() )
		{
			Node node = network.get( newActiveNode.pop() );
			Integer targetNeighborID;

			for ( int j = 0; j < node.neighbors.size(); ++j )
			{
				targetNeighborID = node.neighbors.get( j );

				if ( componentID[ targetNeighborID ] == -1 )
				{
					componentID[ targetNeighborID ] = 0;
					++elementCounter;
					newActiveNode.push( targetNeighborID );
				}
			}
		}

		numOfElement_.add( elementCounter );

		// Find the other components
		for ( int i = 0; i < network.size(); ++i )
		{
			if ( componentID[i] == -1 )
			{
				elementCounter = 0;
				++componentIDNow;

				componentID[i] = componentIDNow;
				++elementCounter;
				newActiveNode.push( i );

				while( !newActiveNode.empty() )
				{
					Vector< Integer > neighbors = network.get( newActiveNode.pop() ).neighbors;
					Integer targetNeighborID;

					for ( int j = 0; j < neighbors.size(); ++j )
					{
						targetNeighborID = neighbors.get(j);

						if ( componentID[ targetNeighborID ] == -1 )
						{
							componentID[ targetNeighborID ] = componentIDNow;
							++elementCounter;
							newActiveNode.push( targetNeighborID );
						}
					}
				}

				numOfElement_.add( elementCounter );
			}
		}

		return componentID;
	}

}	// end of class InfluenceMax

class Node
{
	public Vector< Integer > neighbors;

	public Node()
	{
		neighbors = new Vector< Integer >();
	}

}	// end of class Node