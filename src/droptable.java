import com.sleepycat.je.Database;

public class droptable {
	public static final int PRINT_SYNTAX_ERROR = 0;
	public static final int SUCCESS_DROP_TABLE = 2;
	
	//FAIL	
	public static final int DROP_REFERENCED_TABLE_ERROR = -21;
	public static final int NO_SUCH_TABLE = -22;


	public int Drop(String tname , Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
	    int q = SUCCESS_DROP_TABLE;
		String rTables = dbmanage.getDataFromDB(tname + "/refered/tableCount" , myDatabase);
		int rTableNums = -1;

		int fNums = -1;
		int tempInt = -1;

		String key = "";
		String data = "";
		String tmpdata = "";
		String tmpkey = "";
		String tmpkey_ = "";
		String tableExist = dbmanage.getDataFromDB(tname + "/exist" , myDatabase);

		int Colcount = -1;
		int Forcount = -1;
		int FColcount = -1;
		int rColcount = -1;
		int pColcount = -1;

		int tnums = -1;
		
		if( tableExist == null )
			return NO_SUCH_TABLE;

		if( rTables != null)
		{
		  	rTableNums = Integer.parseInt(rTables);
			if(rTableNums > 0)
				return DROP_REFERENCED_TABLE_ERROR;
		}

		key = tname + "/Forcount";
		data = dbmanage.getDataFromDB(key , myDatabase);		
		if(data != null)
		{
			fNums = Integer.parseInt(data);
			for(int i = 1; i <= fNums; i++)
			{
			 //get refering tables, and decrease their rTableNums 
				key = tname + "/for" + String.valueOf(i) + "/refer";
				data = dbmanage.getDataFromDB(key , myDatabase); //[rtable]

				
				key = data + "/refered/tableCount";
				
				data = dbmanage.getDataFromDB(key , myDatabase);
				
				if(data == null)
				{
				   	System.out.println("index i : " + i );
    				  System.out.println("fNums : " + fNums );
				   	System.out.println("Something wrong at printDrop. this table's referedCount never can be null");
					return 0; //syntaxError
				} 
				tempInt = Integer.parseInt(data);
				dbmanage.putDataToDB(key, String.valueOf(tempInt - 1) , myDatabase);			  
			}
		}

// get # of table's column 
		key = tname + "/Colcount";
		data = dbmanage.getDataFromDB(key , myDatabase);
		Colcount = Integer.parseInt(data);
// get # of table's foreign key
		key = tname + "/Forcount";
		data = dbmanage.getDataFromDB(key , myDatabase);
		if(data != null)
			Forcount = Integer.parseInt(data);
		else
			Forcount = -1;
// get # of table's primary key column
		key = tname + "/pri/Colcount";
		data = dbmanage.getDataFromDB(key , myDatabase);
		if(data != null)
			pColcount = Integer.parseInt(data);
		else
			pColcount = -1;

//delete col
		for(int i = 1; i <= Colcount; i++)
		{
		  key = tname + "/col" + String.valueOf(i);
		  
		  tmpdata = dbmanage.getDataFromDB(key , myDatabase);
		  tmpkey = tname + "/cols/" + tmpdata;
		  dbmanage.deleteData(tmpkey , myDatabase);
		  dbmanage.deleteData(key , myDatabase);
		  dbmanage.deleteData(key + "/type" , myDatabase);
		  dbmanage.deleteData(key + "/size" , myDatabase);
		  dbmanage.deleteData(key + "/null" , myDatabase);
		  dbmanage.deleteData(key + "/for" , myDatabase);
		  dbmanage.deleteData(key + "/pri" , myDatabase);
		}
//delete pri
		for(int i =1; i <= pColcount; i++)
		{
			key = tname + "/pri/col" + String.valueOf(i);
			tmpdata = dbmanage.getDataFromDB(key , myDatabase);
			tmpkey = tname + "/pris/" + tmpdata;
			dbmanage.deleteData(key , myDatabase);
			dbmanage.deleteData(tmpkey , myDatabase);
		}
//delete for
		for(int i = 1; i <= Forcount; i++)
		{
			key = tname + "/for" + String.valueOf(i);
			data = dbmanage.getDataFromDB(key + "/Colcount" , myDatabase);
			FColcount = Integer.parseInt(data);
//delete fcol
			for(int j = 1; j <= FColcount; j++)
			{
				tmpkey = key + "/col" + String.valueOf(j);
				tmpdata = dbmanage.getDataFromDB(tmpkey , myDatabase);
				tmpkey_ = tname + "/fors/" + tmpdata;
				dbmanage.deleteData(tmpkey_ , myDatabase);
				
				dbmanage.deleteData(tmpkey , myDatabase);
			}

			key = tname + "/for" + String.valueOf(i);
			data = dbmanage.getDataFromDB(key + "/refer/rColcount" , myDatabase);
			rColcount = Integer.parseInt(data);
//delete rcol			
			for(int j = 1; j <= rColcount; j++)
			{
				tmpkey = key + "/refer/rcol" + String.valueOf(j);
				dbmanage.deleteData(tmpkey , myDatabase);
			}

		  	dbmanage.deleteData(key + "/refer" , myDatabase);
		  	dbmanage.deleteData(key + "/Colcount" , myDatabase);
		  	dbmanage.deleteData(key + "/refer/rColcount" , myDatabase);
		  	
		}

		dbmanage.deleteData(tname + "/refered/tableCount" , myDatabase);
		dbmanage.deleteData(tname + "/Colcount" , myDatabase);
		dbmanage.deleteData(tname + "/Forcount" , myDatabase);
		dbmanage.deleteData(tname + "/pri/Colcount" , myDatabase);
		dbmanage.deleteData(tname + "/exist" , myDatabase);

		data = dbmanage.getDataFromDB("table/nums" , myDatabase);
		tnums = Integer.parseInt(data);			

		if(tnums > 1)
		{
		 //decrease table/nums 
			dbmanage.putDataToDB("table/nums", String.valueOf(tnums - 1) , myDatabase);
			
			for(int i = 1; i <= tnums; i++)
			{
				key = "table-list/table" + String.valueOf(i);
				data = dbmanage.getDataFromDB(key , myDatabase);
				if(tname.equals(data))
				{
				  //change last element with this table & delete last element
					tmpkey = "table-list/table" + String.valueOf(tnums);
					data = dbmanage.getDataFromDB(tmpkey , myDatabase);
					dbmanage.putDataToDB(key, data , myDatabase);
					dbmanage.deleteData(tmpkey , myDatabase);
					
					break;
				}
			}
		}
		else
		{// when only one table exist
			dbmanage.deleteData("table/nums" , myDatabase);
			dbmanage.deleteData("table-list/table1" , myDatabase);
		}
		return q;
	}

}
