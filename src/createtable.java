import java.util.ArrayList;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import java.io.UnsupportedEncodingException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.je.Database;

public class createtable {

	public static final int PRINT_SYNTAX_ERROR = 0;
	public static final int SUCCESS_CREATE_TABLE = 1;
	
	//FAIL
	public static final int TABLE_EXISTENCE_ERROR = -11;
	public static final int DUPLICATE_COLUMN_DEF_ERROR = -12;
	public static final int DUPLICATE_PRIMARY_KEY_ERROR = -13;
	public static final int CHAR_LENGTH_ERROR = -14;
	
	public static final int REFERENCE_TYPE_ERROR = -15;
	public static final int REFERENCE_NON_PRIMARY_KEY_ERROR = -16;
	public static final int REFERENCE_COLUMN_EXISTENCE_ERROR = -17;
	public static final int REFERENCE_TABLE_EXISTENCE_ERROR = -18;
	public static final int NON_EXISTING_COLUMN_DEF_ERROR = -19;
	
	public static final int DUPLICATE_FOREIGN_KEY_ERROR = -100; // ONE COLUMN USED DUPLICATE FOREIGN KEY DEF
	


	public String[] CreatingTable(String tname, ArrayList<String > slist, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
	  	String[] result = new String[2];
		int q = -1; //return value
		result[0] = null;
		result[1] = tname;
				      
	    int colnums = 1;
	
	    int prExist = 0;
		int prCols = 0;
	
		int forCols = 0;
	    int fors = 0;
	
	    int refTset = 0;
	    int refCols = 0;
	    
		char status = '/';
	    String keyinit = tname + "/" + "col";
	
	    String keycol = keyinit + String.valueOf(colnums); // tname/col1
	    String keytype = keycol + "/" + "type";	// tname/col1/type
	    String keynull = keycol + "/" + "null"; // tname/col1/null
	    String keysize = keycol + "/" + "size"; // tname/col1/size
	
	
	//keys for primary key
	    String keypri = tname + "/" + "pri";
		String keypriCol = keypri + "/" + "col";
	
	//keys for foreign key
		String keyfor = tname + "/" + "for";
		String keyforCol = keyfor + "/" + "col";
	
	//keys for reference    
	    String keyReferTable = keyfor + "/" + "refer";
		String keyReferCol = keyReferTable + "/" + "col";
	
		Map<String,String > map = new HashMap<String,String >();
	
	    String sum = "";
	
		String StrNum = "0";
		int tableNums = 0;
	
		String key;
		String data;
	
		String tmpkey;
	
	
		slist.add("/");
	    
		for( String s : slist ) {
		  	sum = sum + " " + s;
			switch(s)
		  	{
		  	  	case " " :
		  	  	break;
		  	  	case "/" :
		  	  		switch(status)
			  	  	{
						case 'p' : // Finished saving primary keys in map
							status = '/';
							// saving primary key attributes number
							map.put(keypri + "/Colcount", String.valueOf(prCols)); // key~ : tname/pri/Colcount						
						break;
						
						case 'r' : // Finished saving foreign keys in map
							status = '/';
							// saving referencing cols number
							map.put(keyReferTable + "/rColcount", String.valueOf(refCols)); // ex) tname/for1/refer/rColcount
						break;
						
						case '0' : // Finished saving one Column in map, let's prepare for nextColumn		  	  	  
				  	  	   	status = '/';
				  	  		colnums++;
							keycol = keyinit + String.valueOf(colnums);
							keytype = keycol + "/" + "type";	// keytype : tname/col[colnums]/type
							keynull = keycol + "/" + "null";	// keynull : tname/col[colnums]/null
							keysize = keycol + "/" + "size";	// keysize : tname/col[colnums]/size
						break;
	
						default :
							System.out.println("Something wrong at / and status is : " + status);
						break;	
					}
		  	  	break;
				case "primaryKey" :
					if(prExist == 0) // check primary key is already Exist
					{
					   	status = 'p';
						prExist = 1; 
						prCols = 0; // the number of primary key attribute
					}else {
					  	result[0] = String.valueOf(DUPLICATE_PRIMARY_KEY_ERROR);
					  	result[1] =	tname;
						return result;
					}
				break;
				case "foreignKey" :
					status = 'f';
					fors++; // the number of foreign key
					forCols = 0; // the number of attributes
					keyfor = tname + "/" + "for" + String.valueOf(fors); // ex) tname/for1
					
				break;
				case "references" :
	
					// saving foreignKey attributes number
					map.put(keyfor + "/Colcount", String.valueOf(forCols)); // ex) tname/for1/Colcount
									
					status = 'r';
					refTset = 1; // set reference table name
					refCols = 0; // the number of attributes which are referenced
					keyReferTable = keyfor + "/" + "refer"; // ex) tname/for1/refer				
				break;
				case "char" :
					status = 'c'; // c : char size check
					// 튜플을 map에 넣기
					map.put(keytype,"char"); // keytype : tname/col[i]/type				
				break;
				
				case "date" :
					status = 'n'; //n : null check
					// 튜플을 map에 넣기
					map.put(keytype,"date"); // keytype : tname/col[i]/type
				break;
							
				case "int" :
					status = 'n'; //n : null check
					// 튜플을 map에 넣기
					map.put(keytype,"int"); // keytype : tname/col[i]/type
				break;
				default :
					switch(status)
					{
				  	  	case '/' :	// s is not primary key and not foreign key, s is column
				  	  		status = 't'; // t : next string will be type
							// 튜플을 map에 넣기
	
						map.put(keycol,s); // keycol : tname/col[i]
						if(map.get(tname + "/cols/" + s) == null )
							map.put(tname + "/cols/" + s , "col" + colnums);
						else
						{
						  	result[0] = String.valueOf(DUPLICATE_COLUMN_DEF_ERROR);
					  		result[1] =	tname;
						 
							return result;
						}
				  	  	break;
				  	  	
						case 'p' :
							prCols++;
							keypriCol = keypri + "/" + "col" + String.valueOf(prCols); // tname/pri/col[prcols]
							map.put(keypriCol,s);
						break;
						
						case 'f' :
							forCols++;
							keyforCol = keyfor + "/" + "col" + forCols;	// ex) tname/for1/col1
							// 튜플을 Map에 넣기
							map.put(keyforCol,s);
	
							tmpkey = tname + "/fors/" + s; // tname / fors / [colname]
							if(map.get(tmpkey) != null)
							{
								result[0] = String.valueOf(DUPLICATE_FOREIGN_KEY_ERROR);
								result[1] = tname;  
							   	return result;
							}
							map.put(tmpkey, "exist");
							
						break;
						
						case 'r' :
							if(refTset == 1) // set refer table name
							{
								String tempString = dbmanage.getDataFromDB( s + "/exist"  , myDatabase);						  
								if ( "True".equals(tempString))  
								{
									// put 'refer table name' To map
									map.put(keyReferTable,s);
									refTset = 0;
								}
								else
								{
							 	  	result[0] = String.valueOf(REFERENCE_TABLE_EXISTENCE_ERROR);
					  				result[1] =	tname;
					
									return result;
								}
							}
							else
							{
							  	//check reference column is exist
								if(dbmanage.getDataFromDB(map.get(keyReferTable) + "/cols/" + s , myDatabase) == null)
								{
					  		 	  	result[0] = String.valueOf(REFERENCE_COLUMN_EXISTENCE_ERROR);
					  				result[1] =	tname;
					 
							  		return result;
							  	}
							  	//check reference column is primary key 
								else if(dbmanage.getDataFromDB(map.get(keyReferTable) + "/pris/" + s , myDatabase) == null)
								{
									result[0] = String.valueOf(REFERENCE_NON_PRIMARY_KEY_ERROR);
			  						result[1] =	tname;
					 
									return result;
								}
								refCols++;
								keyReferCol = keyReferTable + "/" + "rcol" + String.valueOf(refCols); // ex) tname/for1/refer1/rcol1
								// put reference column to map
								map.put(keyReferCol,s);
							}
						break;
						case 'c' :
							status = 'n'; // n : null check
							// 튜플을 map에 넣기
							if( Integer.parseInt(s) <  1)
							{
								result[0] = String.valueOf(CHAR_LENGTH_ERROR);
								result[1] = tname;   
								return result;
							}
							map.put(keysize,s); // keysize : tname/col[i]/size
						break;
						case 'n' :
							status = '0';
							// 튜플을 Map에 넣기
							map.put(keynull,s); // keynull : tname/col[i]/null
						break;			
						default :
							System.out.println("Something wrong at default default.  s is : " + s + "  and status is : " + status);
						break;					
					}
				break;					
			}
		}
	
		// at the last s which is '/' colnums is incremented 
		colnums = colnums - 1;
	
	//		System.out.println("sum is : " + sum);
	
		// saving column number
		map.put(tname + "/Colcount", String.valueOf(colnums)); // ex) tname/Colcount
	
		// saving foreignKey number
		map.put(tname + "/Forcount", String.valueOf(fors)); // ex) tname/Forcount
	
	
	
	
	
	
		int priColcount = -1;
		int ForCount = -1;
		String tempCount;
	
		tempCount = map.get(tname + "/pri/Colcount");
		if( tempCount != null)
		{  
			priColcount = Integer.parseInt(tempCount);
		}
		tempCount = map.get(tname + "/Forcount");
		if( tempCount != null)
		{  
			ForCount = Integer.parseInt(tempCount);
		}
	
		// check all primary key Exist, and set Notnull
		for(int j = 1; j <= priColcount; j++)
		{
		    String temps = "";
		 	key = tname + "/pri/col" + String.valueOf(j);
		 	data = map.get(key);
	
			//table_a / cols / [pcol1] => col[i]
			temps = map.get(tname + "/cols/" + data);
	
			if(temps == null)
			{
				result[0] = String.valueOf(NON_EXISTING_COLUMN_DEF_ERROR);
				result[1] = data;   
				return result;
			}
			else
			{
				key = tname + "/" + temps + "/null"; // table_a / col[i] / null
				map.put(key, "notNull");
				
				key = tname + "/" + temps + "/pri";
				map.put(key, "True");			  
			}	
		}
	
	
	
		for(int j = 1; j <= ForCount; j++)
		{
		  	// tname/for1/refer
		  	String forTableName = map.get(tname + "/for"+String.valueOf(j) + "/refer");
	
		  	String rcolNum = "";
		  	String colNum = "";
		  	String tmmmpS = "";
	
			//check F_Colcount == RColcount
			String Colcount = map.get(tname + "/for" + String.valueOf(j) + "/Colcount");
			String Rcount = map.get(tname + "/for" + String.valueOf(j) + "/refer/rColcount");
	
			String colstr = "";
			String rcolstr = "";
	
			String colKey = "";
			String rKey ="";
	
			String type_ = "";
			String size_ = "";
	
			tmmmpS = dbmanage.getDataFromDB(forTableName + "/pri/Colcount" , myDatabase);
				
			if(tmmmpS != null && dbmanage.getDataFromDB(forTableName + "/pri/Colcount" , myDatabase).equals(Rcount) == false)
			{
				result[0] = String.valueOf(REFERENCE_NON_PRIMARY_KEY_ERROR);
				result[1] = tname;
				return result;
			}
			else if(tmmmpS == null)
			{
				result[0] = String.valueOf(REFERENCE_NON_PRIMARY_KEY_ERROR);
				result[1] = tname;
				return result;
			}
	
	
			
			if( Colcount != null && Colcount.equals(Rcount) )
		  	{
				for(int l = 1; l <= Integer.parseInt(Colcount); l++) 
				{
				  	// tname/for1/col1
					key = tname + "/for"+String.valueOf(j) + "/col"+String.valueOf(l);
					colstr = map.get(key);
	
					//tname / cols / [colName]
					colNum = map.get(tname + "/cols/" + colstr);
	
					if(colNum == null)
					{
						result[0] = String.valueOf(NON_EXISTING_COLUMN_DEF_ERROR);
						result[1] = colstr; 
						return result;					  
					}
					
					// tname / col[i]
					colKey = tname + "/" + colNum;
	
					// tname / col[i] / for
					key = colKey + "/for";
					map.put(key, "True");
					
	
	
					//r
				    // tname/for1/refer/rcol1
					key = tname + "/for"+String.valueOf(j) + "/refer/rcol"+String.valueOf(l);
					rcolstr = map.get(key);
	
					// [rtname] / cols / [rcol]
					key = forTableName + "/cols/" + rcolstr;
					
					rcolNum = dbmanage.getDataFromDB(key , myDatabase);
	
					// [rtname] / col[k]
					rKey = forTableName + "/" + rcolNum;
	
					type_ = map.get(colKey + "/type");
	
								
					if( type_.equals( dbmanage.getDataFromDB(rKey + "/type" , myDatabase) ) )
					{
						if( "char".equals(type_) )
						{
							size_ = map.get(colKey + "/size");
							if( size_.equals( dbmanage.getDataFromDB(rKey + "/size"  , myDatabase) ) == false ) // Csize != Rsize
							{
								result[0] = String.valueOf(REFERENCE_TYPE_ERROR);
								result[1] = tname; 
								return result;
							}
						}
					}
					else // Ctype != Rtype
					{
						result[0] = String.valueOf(REFERENCE_TYPE_ERROR);
						result[1] = tname; 
						return result;					  
						  
					}
					
				}
			}
			else //Colcount != rCount
			{
				result[0] = String.valueOf(REFERENCE_TYPE_ERROR);
				result[1] = tname; 
				return result;
			}
		}
				
	
	
	
	
	
	
	
	
	
		
	
	
		//EXISTENCE TEST
		//check table name is already Exist
		key = tname + "/exist"; // table_a/exist
		String checkExist = dbmanage.getDataFromDB(key , myDatabase);
		if(checkExist != null)	// already exist -> Error!
		{
			q = TABLE_EXISTENCE_ERROR;
			result[0] = String.valueOf(q);
			result[1] = tname;   
			return result;
		}
		else // null -> not Exist
		{
			q = SUCCESS_CREATE_TABLE;
			dbmanage.putDataToDB(key, "True" , myDatabase);
		} // set table_a/exist, True
	
	/////////////test Complete
	
		// get # of exist tables		
		key = "table/nums";	
		StrNum = "1";
	
		if(dbmanage.getDataFromDB(key , myDatabase) == null)
		{
			dbmanage.putDataToDB(key, "1" , myDatabase);
			tableNums = 1;			
		}
		else
		{
			StrNum = dbmanage.getDataFromDB(key , myDatabase);
			tableNums = Integer.parseInt(StrNum) + 1;
			dbmanage.putDataToDB(key, String.valueOf(tableNums) , myDatabase);			
		}
	
		// set table-list / table[index] , [tname]
		key = "table-list/table" + String.valueOf(tableNums);
		dbmanage.putDataToDB(key, tname , myDatabase);
	
		//Put '# of columns' To DB
		key = tname + "/Colcount";	
		dbmanage.putDataToDB( key, map.get(key)  , myDatabase);
		
		//Put '# of Foreign Key' To DB
		key = tname + "/Forcount";
		if(map.get(key) != null)
			dbmanage.putDataToDB( key, map.get(key)  , myDatabase);
	
		//Put '# of Primary Key Attributes' To DB
		key = tname + "/pri/Colcount";
		if(map.get(key) != null)
			dbmanage.putDataToDB( key, map.get(key)  , myDatabase);
			
	
	
	
	// saving map data to DB
	
		for(int j = 1; j <= priColcount; j++)
		{
		 	key = tname + "/pri/col" + String.valueOf(j);
		 	data = map.get(key);
		  	dbmanage.putDataToDB( key, data  , myDatabase);
	
	
		  	key = tname + "/pris/" + data;
		 	dbmanage.putDataToDB( key, "exist"  , myDatabase); 	
		}
	
		for(int j = 1; j <= ForCount; j++)
		{
	
			//Put 'foreign key j's # of attributes' To DB
			key = tname + "/for" + String.valueOf(j) + "/Colcount";	
			dbmanage.putDataToDB( key, map.get(key)  , myDatabase);
		  
			for(int l = 1; l <= Integer.parseInt(map.get(tname + "/for" + String.valueOf(j) + "/Colcount")); l++) 
			{
			  	// tname/for1/col1
				key = tname + "/for"+String.valueOf(j) + "/col"+String.valueOf(l);
				data = map.get(key);
			  	dbmanage.putDataToDB( key, data  , myDatabase);
			  	
			  	
			  	//added hw3 0512 - 9.1 [tname]/fortable/[colname] , [refering tablename]
			  	key = tname +"/fortable/" + data;
			  	tmpkey = tname + "/for" + String.valueOf(j) + "/refer";
			  	data = map.get(tmpkey);
			  	dbmanage.putDataToDB(key, data, myDatabase);
			}
	
			// tname/for1/refer
			key = tname + "/for"+String.valueOf(j) + "/refer";
			data = map.get(key); // refering table
		  	dbmanage.putDataToDB( key, data  , myDatabase);
	/*
				// [refertable] / referLists / [tname], True
			  	key = data + "/referLists/" + tname;
			  	dbmanage.putDataToDB( key, "True");
	*/
			// [refertable] / refered / tableCount, [tablenums]++
		  	key = data + "/refered/tableCount";
		  	data = dbmanage.getDataFromDB(key , myDatabase);
		  	if( data == null )
		  		dbmanage.putDataToDB( key, "1" , myDatabase);
		  	else
		  		dbmanage.putDataToDB( key, String.valueOf(Integer.parseInt(data) + 1) , myDatabase);	
	
			//Put 'foreign key j's # of Reference attributes' To DB
			key = tname + "/for" + String.valueOf(j) + "/refer/rColcount";	
			dbmanage.putDataToDB( key, map.get(key) , myDatabase );
							
			for(int k = 1; k <= Integer.parseInt(map.get(tname + "/for" + String.valueOf(j) + "/refer/rColcount")); k++) 
			{
			    // tname/for1/refer/rcol2
				key = tname + "/for"+String.valueOf(j) + "/refer/rcol"+String.valueOf(k);
				data = map.get(key);
			  	dbmanage.putDataToDB( key, data  , myDatabase);
			  	
			  	//3rd hw added - 8.1
			  	String tmpdata = null;
				tmpdata = data; // tname/for1/refer/rcol2
			  	
				key = tname + "/for"+String.valueOf(j) + "/col"+String.valueOf(k); // get tname/for1/col2			  	
				data = map.get(key);
				
				// tname / forcols / [colname] , [rcolname] 
				key = tname + "/forcols/" + data;
			  	dbmanage.putDataToDB( key, tmpdata  , myDatabase);
			  	
			  	
			}
		}
	
		for(int j = 1; j <= colnums; j++)
		{
		  	String types = map.get(tname + "/col" + String.valueOf(j) + "/type");
	
			// tname/col1
			key = tname + "/col" + String.valueOf(j);
			data = map.get(key);
		  	dbmanage.putDataToDB( key, data , myDatabase );
	
	
		  	key = tname + "/cols/" + data;
		  	dbmanage.putDataToDB( key, "col" + String.valueOf(j) , myDatabase ); 
	
			key = tname + "/col" + String.valueOf(j) + "/type";
			data = map.get(key);
		  	dbmanage.putDataToDB( key, data , myDatabase );
	
			if("char".equals(types))
			{
				key = tname + "/col" + String.valueOf(j) + "/size";
				data = map.get(key);
			  	dbmanage.putDataToDB( key, data , myDatabase );
			}
	
			key = tname + "/col" + String.valueOf(j) + "/null";
			data = map.get(key);
		  	dbmanage.putDataToDB( key, data , myDatabase);
	
			key = tname + "/col" + String.valueOf(j) + "/pri";
			data = map.get(key);
		  	if(data != null)
				dbmanage.putDataToDB(key,data , myDatabase);			
			key = tname + "/col" + String.valueOf(j) + "/for";
			data = map.get(key);
		  	if(data != null)
		  		dbmanage.putDataToDB(key,data , myDatabase);
			
		}
		
		

		
		
		
		q = SUCCESS_CREATE_TABLE; 
	
		result[0] = String.valueOf(q);
		result[1] = tname;   
		return result;

	}
}
