import com.sleepycat.je.Database;

public class tablequery {
	public static final int PRINT_SYNTAX_ERROR = 0;
	public static final int SUCCESS_DESC = 3;
	public static final int SUCCESS_SHOW_TABLES = 7;
	
	//FAIL
	public static final int NO_SUCH_TABLE = -22;
	
	public static final int SHOW_TABLES_NO_TABLE = -71;
	

	public int printShowTable(Database myDatabase)
	{		
		dbmanager dbmanage = new dbmanager();
	    int q = SUCCESS_SHOW_TABLES;

		int tableNum = -1;
		// get # of table
		String data = dbmanage.getDataFromDB("table/nums" , myDatabase);

		if( data == null )
		{
			return SHOW_TABLES_NO_TABLE;		  
		}
		tableNum = Integer.parseInt(data);

		System.out.println("----------------");
		for(int i = 1; i <= tableNum; i++)
		{
		  
		  data = dbmanage.getDataFromDB("table-list/table" + String.valueOf(i) , myDatabase);
		  System.out.println(data);
		}
		System.out.println("----------------");

		return q;
	}
	
	public int printDesc(String tname, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
		int q = SUCCESS_DESC;
	    String colnum = dbmanage.getDataFromDB(tname + "/Colcount" , myDatabase);
	    String data = "";

		if(colnum == null)
			return NO_SUCH_TABLE;
		
	    
		System.out.println("desc table name : " + tname);

		System.out.println("-------------------------------------------------");
		System.out.println("table_name [" + tname + "]");
		System.out.println("column_name		type		null		key");


		for(int i = 1; i <= Integer.parseInt(colnum); i++)
		{
			String keyFrame = tname + "/col" + String.valueOf(i);
			String checkBothPF = "";
		  
			data = dbmanage.getDataFromDB(keyFrame , myDatabase); 	 
			System.out.print(data);
			System.out.print("                  ");

			data = dbmanage.getDataFromDB(keyFrame + "/type" , myDatabase); 	 
			System.out.print(data);
			if( "char".equals(data) )
			{
				data = dbmanage.getDataFromDB(keyFrame + "/size" , myDatabase);
				System.out.print(" ( " + data + " ) ");
			  
			}
			System.out.print("                  ");
			
			data = dbmanage.getDataFromDB(keyFrame + "/null" , myDatabase);
			if( "notNull".equals(data))	 
				System.out.print("N");
			else
				System.out.print("Y");
			System.out.print("                  ");

			data = dbmanage.getDataFromDB(keyFrame + "/pri" , myDatabase);
			if( "True".equals(data) )
			{ 
				System.out.print("PRI");
				checkBothPF = "/";
			}
			data = dbmanage.getDataFromDB(keyFrame + "/for" , myDatabase);
			if( "True".equals(data) )
			{ 
				System.out.print(checkBothPF + "FOR");
			}
			
			
			System.out.println();
		}
		System.out.println("-------------------------------------------------");	
	    return q;
	}

	

}
