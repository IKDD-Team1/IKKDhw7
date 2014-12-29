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

	/* Find out the set of seed nodes that has the max f(S) using brute-force greedy algorithm.
	 * Spread by IC model.
	 */
	public void bruteGreedyIC( int numOfSeeds, float probability, int iterations )
	{
		boolean[] selected = new boolean[ network_.size() ];
		int[] IDofSeeds = new int[ numOfSeeds ];
		int resultFofS = 0;

		for ( int seedIndex = 0; seedIndex < numOfSeeds; ++seedIndex )
		{
			int theMaxFofS = 0;
			int theMaxCandidateID = -1;

			System.out.println( "Finding " + ( seedIndex+1 ) + "th seed..." );

			// Test each node excluding the previous selected nodes
			for ( int candidateNode = 0; candidateNode < network_.size(); ++candidateNode )
			{
				if ( candidateNode != 0 && candidateNode % 100 == 0 )
				{
					System.out.println( candidateNode + "\t nodes are tested." );
				}

				if ( selected[ candidateNode ] ) continue;

				int targetFofS = 0;

				// It has to test each node in many times: Monte Carlo simulation
				for ( int i = 0; i < iterations; ++i )
				{
					targetFofS += ICModel( IDofSeeds, seedIndex, candidateNode, probability );
				}

				// Average the result
				targetFofS /= iterations;

				if ( targetFofS > theMaxFofS )
				{
					resultFofS = theMaxFofS = targetFofS;
					theMaxCandidateID = candidateNode;
				}
			}

			selected[ theMaxCandidateID ] = true;
			IDofSeeds[ seedIndex ] = theMaxCandidateID;

			System.out.println( "The " + ( seedIndex+1 ) + "th seed node is ID: " + theMaxCandidateID );
		}

		System.out.println( "Total influence f(S) : " + resultFofS );

	}

	private int ICModel( int[] selectedSeeds, int seedIDNow, int candidateID, float probability )
	{
		boolean[] active = new boolean[ network_.size() ];
		Stack< Integer > newActiveSeeds = new Stack< Integer >();
		int activeNodes = 0;
		Random random = new Random( System.currentTimeMillis() );
		int randomThreshold = (int)( probability * 100.0f );

		// Active the pre-chooesd seed and push to the stack
		if ( seedIDNow != 0 )
			for ( int i = 0; i < seedIDNow; ++i )
			{
				active[ selectedSeeds[i] ] = true;
				newActiveSeeds.push( selectedSeeds[i] );
				++activeNodes;
			}

		// Active the candidate seed and push to the stack
		active[ candidateID ] = true;
		newActiveSeeds.push( candidateID );
		++activeNodes;

		// IC Model
		while ( !newActiveSeeds.empty() )
		{
			Vector< Integer > neighbors = network_.get( newActiveSeeds.pop().intValue() ).neighbors;
			Integer targetNeighborID;

			for ( int i = 0; i < neighbors.size(); ++i )
			{
				targetNeighborID = neighbors.get(i);

				if ( !active[ targetNeighborID ] &&
					random.nextInt( 100 ) < randomThreshold )
				{
					active[ targetNeighborID ] = true;
					newActiveSeeds.push( targetNeighborID );
					++activeNodes;
				}
			}
		}

		return activeNodes;
	}

}	// end of class InfluenceMax

class Node
{
	public Vector< Integer > neighbors;

}	// end of class Node