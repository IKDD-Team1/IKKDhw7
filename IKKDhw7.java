public class IKKDhw7
{
	public static void main( String[] args )
	{
		String filePath = args[0];
		float probabilty = Float.parseFloat( args[1] );
		InfluenceMax influenceMax = new InfluenceMax();
		int[] checkNodes = { 0, 12, 190 };
		long excutionTime = 0;

		try
		{
			excutionTime = System.currentTimeMillis() * -1;

			System.out.print( "Building network...  " );
			influenceMax.buildNetwork( filePath );
			System.out.println( "Done" );

			influenceMax.DFSGreedyIC( 10, probabilty, 1000 );

			excutionTime += System.currentTimeMillis();

			System.out.println( "Excution time: " + ((float)excutionTime/1000.0f) + "seconds" );
		}
		catch ( Exception e )
		{
			System.out.println( e.toString() );
		}
	}
}