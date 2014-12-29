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

		for ( int seedIndex = 0; seedIndex < numOfSeeds; ++seedIndex )
		{
			// Initialize all F(S) to be 0.
			for ( int i = 0; i < allFofS.length; ++i )
				allFofS[i] = 0;

			System.out.println( "Finding " + ( seedIndex+1 ) + "th seed..." );

			// Evaluation total influence f(S) by Monte Carlo simulation
			for ( int i = 0; i < iterations; ++i )
			{
				if ( i % 100 == 0 )
				{
					System.out.println( i + "\titerations completed" );
				}

				// Pre-decide propagation probability
				preDecideProbability( probability );
				// Check if the node in the graph is accessiable from the seed set
				boolean[] accessiableFromSeed = getAccessibleGraph( IDofSeeds, seedIndex );

				for ( int candidateID = 0; candidateID < network_.size(); ++candidateID )
				{
//					if ( candidateID % 100 == 0 )
//						System.out.println( candidateID + " nodes tested." );

					if ( selected[ candidateID ] ) continue;

					if ( !accessiableFromSeed[ candidateID ] )
						allFofS[ candidateID ] += ICModel( candidateID );
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
	 */
	private void preDecideProbability( float probability )
	{
		Random random = new Random( System.currentTimeMillis() );
		int randomThreshold = (int)( probability * 100.0f );

		for ( int sourceNodeID = 0; sourceNodeID < network_.size(); ++sourceNodeID )
		{
			Vector< Boolean > accessible = new Vector< Boolean >();
			int numOfNeighbors = network_.get( sourceNodeID ).neighbors.size();

			for ( int i = 0; i < numOfNeighbors; ++i )
			{
				if ( random.nextInt( 100 ) < randomThreshold )
					accessible.add( true );
				else
					accessible.add( false );
			}

			Node node = new Node();
			node.neighbors = network_.get( sourceNodeID ).neighbors;
			node.accessible = accessible;
			network_.set( sourceNodeID, node );
		}
	}

	/* Get the nodes that are accssible from the specified nodes in the graph.
	 */
	private boolean[] getAccessibleGraph( int[] selectedSeeds, int seedIDNow )
	{
		boolean[] active = new boolean[ network_.size() ];
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
			Node node = network_.get( newActiveNode.pop() );
			Integer targetNeighborID;

			for ( int j = 0; j < node.neighbors.size(); ++j )
			{
				targetNeighborID = node.neighbors.get( j );

				if ( node.accessible.get(j).booleanValue() &&
					!active[ targetNeighborID ] )
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
	private int ICModel( int candidateID )
	{
		boolean[] active = new boolean[ network_.size() ];
		Stack< Integer > newActiveNode = new Stack< Integer >();
		int numOfActiveNodes = 0;

		// Active the candidate seed and push to the stack
		active[ candidateID ] = true;
		newActiveNode.push( candidateID );
		++numOfActiveNodes;

		// IC Model
		while ( !newActiveNode.empty() )
		{
			Node node = network_.get( newActiveNode.pop() );
			Integer targetNeighborID;

			for ( int j = 0; j < node.neighbors.size(); ++j )
			{
				targetNeighborID = node.neighbors.get( j );

				if ( node.accessible.get(j).booleanValue() &&
					!active[ targetNeighborID ] )
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
	public Vector< Boolean > accessible;	// Is this neighbor accessible?

}	// end of class Node