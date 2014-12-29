import java.io.*;
import java.util.Scanner;
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

}	// end of class InfluenceMax

class Node
{
	public Vector< Integer > neighbors;

}	// end of class Node