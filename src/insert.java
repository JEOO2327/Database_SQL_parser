import com.sleepycat.je.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class insert {

	//INSERT
	public static final int PRINT_INSERT = 4;

	public static final int NO_SUCH_TABLE = -22;
	public static final int INSERT_TYPE_MISMATCH_ERROR = -41;
	public static final int	INSERT_COLUMN_NON_NULLABLE_ERROR = -42;
	public static final int	INSERT_COLUMN_EXISTENCE_ERROR = -43;
	public static final int	INSERT_DUPLICATE_PRIMARYKEY_ERROR = -44;
	public static final int	INSERT_REFERENTIAL_INTEGRITY_ERROR = -45;

	public String[] insertRecord(String tname, ArrayList<String> col_list,ArrayList<String> val_list, ArrayList<String> val_type, Database myDatabase)
	{		
		dbmanager dbmanage = new dbmanager();

		String[] result = new String[2];
		result[0] = String.valueOf(PRINT_INSERT);
		result[1] = null;
		
		String table_exist_check = null;
		String col_index = null;
		String tmpStr = null;
		String tmpSize = null;
		int tmpint = -1;
		
		int tcol_size = -1;
		
		int col_list_size = -1;
		
		Map<String,String > map = new HashMap<String,String >();

		
		
		String key = null;
		String data = null;
		
		String tmpkey = null;
		String tmpdata = null;
		
		String rtname = null;
		String input_value = null;
				
		int recordNums = -1;
		int duplicateCounter = 0;
		int primaryNums = -1;
		int foreignNums = -1;
		int forcolNums = -1;
		int forrecordNums = -1;
		int success_flag = -1;
		
		int RrecNumber = -1;
		String RrecNum = null;
		
		int count = 0;
		
		int refer_check_counter = 0;
		
		int nullCount = -1;
		
		int mapint = -1;
				
		String mapdata = null;
		String mapkey = null;
		
//check table existence
		table_exist_check = dbmanage.getDataFromDB(tname + "/exist", myDatabase);
		
		
			
		if( table_exist_check == null )	//table not exist
		{
			result[0] = String.valueOf(NO_SUCH_TABLE);
			result[1] = null;
			
			return result;
		}
		
		//get [tname]'s # of column
		tmpStr = dbmanage.getDataFromDB(tname + "/Colcount", myDatabase);
		tcol_size = Integer.parseInt(tmpStr);
		
//check type mismatch
		//put # of column to col_list_size
		if( col_list.isEmpty() ) // no column input
		{
			col_list_size = tcol_size;
			
			for(int i = 1; i <= col_list_size; i++)
			{
				tmpStr = dbmanage.getDataFromDB(tname + "/col" + String.valueOf(i), myDatabase);
				
				col_list.add(tmpStr);
			}
		}
		//check column not exist
		else
		{
			col_list_size = col_list.size();
			for(int i = 0; i < col_list_size; i++)
			{
				String col = col_list.get(i);
				col_index = null;
				col_index = dbmanage.getDataFromDB(tname + "/cols/" + col, myDatabase);

				if(col_index == null) //column not exist
				{
					result[0] = String.valueOf(INSERT_COLUMN_EXISTENCE_ERROR);
					result[1] = col;
					
					return result;				
				}
			}
		}
		
		//check # of value is 0 => type mismatch error
		if(val_list.isEmpty())
		{
			result[0] = String.valueOf(INSERT_TYPE_MISMATCH_ERROR);
			result[1] = null;
			
			return result;						
		}	
		else if( col_list_size != val_list.size()) // # of column is not equal to # of value => type mismatch error
		{
			result[0] = String.valueOf(INSERT_TYPE_MISMATCH_ERROR);
			result[1] = null;
			
			return result;						
		}

		
		// assign null to value not assigned 
		if ( col_list.size() != tcol_size) // some columns are not assigned
		{
			for(int i = 1; i <= tcol_size; i++)
			{
				tmpStr = dbmanage.getDataFromDB(tname + "/col" + String.valueOf(i), myDatabase);
				
				if(!col_list.contains(tmpStr))
				{
					col_list.add(tmpStr);
					val_list.add("'null");
					val_type.add("null");
					// put null to val_list and val_type
				}
			}
		}
		//여기서 부터는 col_list, val_list, val_type은 tcol_size와 크기가 같음( 다 채워넣었음)
		
		//check column nullable & type mismatch
		
		for(int i = 0; i < tcol_size; i++)
		{
			if ( "'null".equals(val_list.get(i)) )
			{
				//check nullable
				col_index = dbmanage.getDataFromDB(tname + "/cols/" + col_list.get(i), myDatabase);
				tmpStr = dbmanage.getDataFromDB(tname + "/" + col_index +"/null", myDatabase);
				System.out.println("206 : " + tmpStr);
				if( "notNull".equals(tmpStr))
				{
					result[0] = String.valueOf(INSERT_COLUMN_NON_NULLABLE_ERROR);
					result[1] = col_list.get(i);
					return result;
				}
			}
			else
			{
				col_index = dbmanage.getDataFromDB(tname + "/cols/" + col_list.get(i), myDatabase);
				tmpStr = dbmanage.getDataFromDB(tname + "/" + col_index +"/type", myDatabase);
			
				if( val_type.get(i).equals(tmpStr) ) // type is equal
				{
					if( "char".equals(tmpStr) ) // type is char string
					{
						data = val_list.get(i);
						
						tmpint = data.length() - 1;
						if( tmpint > 1)
							data = data.substring(1, tmpint); // 'inputstring' -> inputstring
						else
							data = "";
						
						// get max size of string
						tmpSize = dbmanage.getDataFromDB(tname + "/" + col_index +"/size", myDatabase);
						tmpint = Integer.parseInt(tmpSize);
						if( data.length() > tmpint)
						{
							tmpStr = data.substring(0, tmpint);
							val_list.set(i, tmpStr);
						}
						else
						{
							val_list.set(i, data);
						}
					}
				}
				else // type is not equal
				{
					result[0] = String.valueOf(INSERT_TYPE_MISMATCH_ERROR);
					result[1] = null;
					return result;	
				}	
			}
		}
		
		//check duplicate primary key 

		data = dbmanage.getDataFromDB(tname + "/recordnums", myDatabase);
		if(data == null)
			recordNums = 0;
		else
			recordNums = Integer.parseInt(data);		
		
		key = tname + "/pri/Colcount"; // [tname]/pri/Colcount - # of primary key
		tmpStr = dbmanage.getDataFromDB(key, myDatabase);
		if(tmpStr == null)
			primaryNums = 0;
		else
			primaryNums = Integer.parseInt(tmpStr);		
		
		if(primaryNums != 0)
		{
			for(int j = 1; j <= recordNums; j++)
			{
				duplicateCounter = 0;
				for(int i = 1; i <= primaryNums; i++)
				{
					tmpkey = tname + "/pri/col" + String.valueOf(i);
					data = dbmanage.getDataFromDB(tmpkey, myDatabase); // get primary colname
					tmpint = col_list.indexOf(data); // get index
					
					tmpkey = tname + "/record" + String.valueOf(j) + "/" + data;
					tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
					
					if(val_list.get(tmpint).equals(tmpdata))
					{
						duplicateCounter++;
					}
				}
				if(duplicateCounter == primaryNums)
				{
					result[0] = String.valueOf(INSERT_DUPLICATE_PRIMARYKEY_ERROR);
					result[1] = null;
					return result;
				}
			}
		}		
		// check referential integrity error
		
		tmpkey = tname + "/Forcount";
		tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
		if(tmpdata == null)
			foreignNums = 0;
		else
			foreignNums = Integer.parseInt(tmpdata);
		
		// all tables referred by [tname]
		for(int i = 1; i <= foreignNums; i++)
		{
			mapkey = tname + "/records/record" + String.valueOf(recordNums+1) +"/refer/table" + String.valueOf(i);
			mapint = -1;
			mapdata = null;
			
			// get referring table name
			tmpkey = tname + "/for" + String.valueOf(i) + "/refer";
			rtname = dbmanage.getDataFromDB(tmpkey, myDatabase);
						
			// get # of foreign key attribute
			tmpkey = tname + "/for" + String.valueOf(i) + "/Colcount";
			tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
			if(tmpdata == null)
				forcolNums = 0;
			else
				forcolNums = Integer.parseInt(tmpdata);
			
			// get ref table's # of record 
			tmpkey = rtname + "/recordnums";
			tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
			if(tmpdata == null)
			{	//no records in ref table
				forrecordNums = 0;
				success_flag = -1;
			}
			else
			{
				success_flag = 0;
				forrecordNums = Integer.parseInt(tmpdata);
			}

			
			//check null
			nullCount = 0;

			for(int k = 1; k <= forcolNums; k++)
			{
				tmpkey = tname + "/for" + String.valueOf(i) + "/col" + String.valueOf(k);
				data = dbmanage.getDataFromDB(tmpkey, myDatabase); // [colname]
				tmpint = col_list.indexOf(data);
				input_value = val_list.get(tmpint); // input value
				if(input_value.equals("'null"))
				{
					nullCount++;
					break;
				}
			}	
			if(nullCount > 0)
				success_flag = 1;

			
			if(success_flag == -1)
			{
				System.out.println("ERROR AT 360");
				// referential integrity error ( referring table has no record and input record is not null )
				result[0] = String.valueOf(INSERT_REFERENTIAL_INTEGRITY_ERROR);
				result[1] = null;
				return result;		
			}
			
			// input is not null and referring table has some records
			if(success_flag != 1)
			{
				// all records in foreign table
				for(int j = 1; j <= forrecordNums; j++)
				{
					nullCount = 0;
					refer_check_counter = 0;
					
					// all attributes in foreign table's record - we will compare our input value with this record
					for(int k = 1; k <= forcolNums; k++)
					{
						tmpkey = tname + "/for" + String.valueOf(i) + "/col" + String.valueOf(k);
						data = dbmanage.getDataFromDB(tmpkey, myDatabase); // [colname]
						tmpint = col_list.indexOf(data);
						input_value = val_list.get(tmpint); // input value

						tmpkey = tname + "/for" + String.valueOf(i) + "/refer/rcol" + String.valueOf(k);
						data = dbmanage.getDataFromDB(tmpkey, myDatabase);
						//check input value is exist in referring table
						tmpkey = rtname + "/record" + String.valueOf(j) + "/" + data;
						tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
						
						// input value is exist in referring table
						if(input_value.equals(tmpdata))
							refer_check_counter++;
						else
							break;	// break in order to compare with next record
					}
					if(refer_check_counter == forcolNums)
					{	// all foreign key attribute values are exist in one of the foreign table's records
						success_flag = 1;
	
						mapint = j;
						mapdata = String.valueOf(mapint);
						map.put(mapkey, mapdata);
						
						break;
					}
				}	
			}
			if(success_flag != 1)
			{
				System.out.println("ERROR AT 408");
				// referential integrity error ( value is not exist in referring table )
				result[0] = String.valueOf(INSERT_REFERENTIAL_INTEGRITY_ERROR);
				result[1] = null;
				return result;	
			}
		}
//insert record into db
		//get record nums
		data = dbmanage.getDataFromDB(tname + "/recordnums", myDatabase);
		if(data == null)
		{
			recordNums = 1;
			
			// put data to db : [tname] / recordnums , [recordNums] (= "1")
			tmpdata = String.valueOf(recordNums);
			dbmanage.putDataToDB(tname + "/recordnums", tmpdata, myDatabase);
		}
		else
		{
			recordNums = Integer.parseInt(data);
			recordNums++;

			// put data to db : [tname] / recordnums , [recordNums]
			tmpdata = String.valueOf(recordNums);
			dbmanage.putDataToDB(tname + "/recordnums", tmpdata, myDatabase);
		}
		
		// put data to db : [tname] / record[rn] / [colname1] , [value1]
		for(int i = 0; i < tcol_size; i++)
		{
			key = tname + "/record" + String.valueOf(recordNums) + "/" + col_list.get(i);
			data = val_list.get(i);
			dbmanage.putDataToDB(key , data, myDatabase);
		}
		
		count = 0;

		// all tables referred by [tname]
		for(int i = 1; i <= foreignNums; i++)
		{
			// get referring table name
			tmpkey = tname + "/for" + String.valueOf(i) + "/refer";
			rtname = dbmanage.getDataFromDB(tmpkey, myDatabase);
			
			mapkey = tname + "/records/record" + String.valueOf(recordNums) + "/refer/table" + String.valueOf(i);

			data = map.get(mapkey);
			if( data == null )
			{	// do nothing
				continue;
			}
			else
			{
				count++;
				key = tname + "/records/record" + String.valueOf(recordNums) + "/refer/table" + String.valueOf(count);

				// put data to db : [tname] / records / record[rn] / refer / table[count] , [rtname]
				dbmanage.putDataToDB(key, rtname , myDatabase);
				// put data to db : [tname] / records / record[rn] / refer / table[count] / frecord , [data] - index of referring record
				dbmanage.putDataToDB(key + "/frecord", "record" + data, myDatabase);
				
				tmpkey = tname + "/records/record" + String.valueOf(recordNums) + "/refer/tables/" + rtname;
				
				// put data to db : [tname] / records / record[rn] / refer / tables / [rtname] , table[count]
				dbmanage.putDataToDB(tmpkey, "table" + String.valueOf(count), myDatabase);
//			
				key = rtname + "/records/record" + data + "/referred/";
				tmpkey = key + "RrecNum";
				RrecNum = dbmanage.getDataFromDB(tmpkey, myDatabase);
				
				if(RrecNum == null)
					RrecNumber = 1;
				else
					RrecNumber = Integer.parseInt(RrecNum) + 1;
				dbmanage.putDataToDB(tmpkey, String.valueOf(RrecNumber), myDatabase);
				
				tmpkey = key + "Rrec" + String.valueOf(RrecNumber) + "/tname";
				dbmanage.putDataToDB(tmpkey, tname, myDatabase);
				
				tmpkey = key + "Rrec" + String.valueOf(RrecNumber) + "/record";
				dbmanage.putDataToDB(tmpkey, "record" + String.valueOf(recordNums), myDatabase);
				
				tmpkey = key + "findRrec/" + tname + "/record" + String.valueOf(recordNums);
				dbmanage.putDataToDB(tmpkey, "Rrec" + String.valueOf(RrecNumber), myDatabase);
//		
				// put data to db : [rtname] / records / record[index of referring record] / refer / referredcount , [this.value]++
				tmpkey = rtname + "/records/record" + data + "/refer/referredcount";
				tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
				if(tmpdata == null)	
					dbmanage.putDataToDB(tmpkey, "1", myDatabase);
				else
				{
					tmpint = Integer.parseInt(tmpdata) + 1;
					dbmanage.putDataToDB(tmpkey, String.valueOf(tmpint), myDatabase);
				}
			}
		}			

		tmpkey = tname + "/records/record" + String.valueOf(recordNums) + "/refer/tablenumber";
		// put data to db : [tname] / records / record[rn] / refer / tablenumber, [count] - count : # of foreign key( not null )
		dbmanage.putDataToDB(tmpkey, String.valueOf(count), myDatabase);

		
		return result;
	}
	
}
