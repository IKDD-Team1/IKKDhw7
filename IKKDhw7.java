public class IKKDhw7
{
	public static void main( String[] args )
	{
		// Input: java IKKDhw7 <network_file> <# of seed to find>
		// <iterations to do> <probability>
		String filePath = args[0];
		int numOfSeed = Integer.parseInt( args[1] );
		int iterations = Integer.parseInt( args[2] );
		float probabilty = Float.parseFloat( args[3] );
		InfluenceMax influenceMax = new InfluenceMax();
		int[] checkNodes = { 0, 1, 2, 3 };
		long excutionTime = 0;

		try
		{
			excutionTime = System.currentTimeMillis() * -1;

			System.out.print( "Building network...  " );
			influenceMax.buildNetwork( filePath );
			System.out.println( "Done" );

			influenceMax.DFSGreedyIC( numOfSeed, probabilty, iterations );

			excutionTime += System.currentTimeMillis();

			System.out.println( "Excution time: " + ((float)excutionTime/1000.0f) + " seconds" );
		}
		catch ( Exception e )
		{
			System.out.println( e.toString() );
		}
	}
}