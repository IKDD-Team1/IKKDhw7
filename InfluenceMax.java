import java.io.*;
import java.util.Scanner;
import java.util.Stack;
import java.util.Random;
import java.util.Vector;

public class InfluenceMax
{
	private Vector< Node > network_ = new Vector< Node >();

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

			System.out.println( "Finding " + ( seedIndex+1 ) + "th seed..." );

			// Evaluation total influence f(S) by Monte Carlo simulation
			for ( int i = 0; i < iterations; ++i )
			{
				// Pre-decide propagation probability
				trimmed_network = preDecideProbability( probability );
				// Check if the node in the graph is accessiable from the seed set
				boolean[] accessiableFromSeed =
					getAccessibleGraph( trimmed_network, IDofSeeds, seedIndex );

				for ( int candidateID = 0; candidateID < network_.size(); ++candidateID )
				{
					if ( selected[ candidateID ] ) continue;

					if ( !accessiableFromSeed[ candidateID ] )
						allFofS[ candidateID ] += ICModel( trimmed_network, candidateID );
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

			System.out.println( "The " + ( seedIndex+1 ) + "th seed node is ID: " + theMaxCandidateID );
			selected[ theMaxCandidateID ] = true;

			resultFofS += theMaxFofS / iterations;
		}

		System.out.println( "Total influence f(S) : " + resultFofS );

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

		for ( int sourceNodeID = 0; sourceNodeID < network_.size(); ++sourceNodeID )
		{
			int numOfNeighbors = network_.get( sourceNodeID ).neighbors.size();
			Vector< Integer > neighbors = network_.get( sourceNodeID ).neighbors;
			Vector< Integer > trimmed_neighbors = new Vector< Integer >();

			for ( int i = 0; i < numOfNeighbors; ++i )
			{
				if ( random.nextInt( 100 ) < randomThreshold )
					trimmed_neighbors.add( neighbors.get(i) );
			}

			Node trimmed_node = new Node();
			trimmed_node.neighbors = trimmed_neighbors;
			trimmed_network.add( trimmed_node );
		}

		return trimmed_network;
	}

	/* Get the nodes that are accssible from the specified nodes in the graph.
	 */
	private boolean[] getAccessibleGraph( Vector< Node > network, int[] selectedSeeds, int seedIDNow )
	{
		boolean[] active = new boolean[ network.size() ];
		Stack< Integer > newActiveNode = new Stack< Integer >();

		if ( seedIDNow == 0 )
			return active;

		for ( int i = 0; i < seedIDNow; ++i )
		{
			active[ selectedSeeds[i] ] = true;
			newActiveNode.push( selectedSeeds[i] );
		}

		// DFS
		while ( !newActiveNode.empty() )
		{
			Node node = network.get( newActiveNode.pop() );
			Integer targetNeighborID;

			for ( int j = 0; j < node.neighbors.size(); ++j )
			{
				targetNeighborID = node.neighbors.get( j );

				if ( !active[ targetNeighborID ] )
				{
					active[ targetNeighborID ] = true;
					newActiveNode.push( targetNeighborID );
				}
			}
		}

		return active;
	}

	/* Get the F(S), S = { candidate node }, using pre-decidec IC Model.
	 */
	private int ICModel( Vector< Node > network, int candidateID )
	{
		boolean[] active = new boolean[ network.size() ];
		Stack< Integer > newActiveNode = new Stack< Integer >();
		int numOfActiveNodes = 0;

		// Active the candidate seed and push to the stack
		active[ candidateID ] = true;
		newActiveNode.push( candidateID );
		++numOfActiveNodes;

		// IC Model
		while ( !newActiveNode.empty() )
		{
			Node node = network.get( newActiveNode.pop() );
			Integer targetNeighborID;

			for ( int j = 0; j < node.neighbors.size(); ++j )
			{
				targetNeighborID = node.neighbors.get( j );

				if ( !active[ targetNeighborID ] )
				{
					active[ targetNeighborID ] = true;
					newActiveNode.push( targetNeighborID );
					++numOfActiveNodes;
				}
			}
		}

		return numOfActiveNodes;

	}

}	// end of class InfluenceMax

class Node
{
	public Vector< Integer > neighbors;

}	// end of class Node