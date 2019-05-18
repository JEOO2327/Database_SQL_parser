import com.sleepycat.je.Database;
import java.util.ArrayList;
import java.util.Collections;


public class delete {
	public static final int PRINT_DELETE = 5;

	public static final int NO_SUCH_TABLE = -22;

	//DELETE
	public static final int DELETE_REFERENTIAL_INTEGRITY_PASSED = -51;

	
	public String[] deleteRecord(String tname, ArrayList<String> predicate_list, ArrayList<String> predicate_info_list, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
		where wheremethod = new where();
		
		ArrayList<String> tnameList = new ArrayList<String>();
		ArrayList<String> newtnameList = new ArrayList<String>();
		
		int whereRet = -1;
		
		String[] result = new String[2];
		result[0] = String.valueOf(PRINT_DELETE);
		result[1] = "0";
		
		int delete_count = 0;
		int undelete_count = 0;
		
		int colcount = -1;
		
	
		String tmpkey = null;
		String tmpdata = null;
		int tmpint = -1;
		
		int recordNums = -1;
		
		
		ArrayList<String> record_list = new ArrayList<String>(); // request to delete
		ArrayList<Integer> record_Index_list = new ArrayList<Integer>(); // record index which is deleted

		
		tnameList.add(tname);
		newtnameList.add(tname);
		
		tmpkey = tname + "/exist";
		tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
		if(tmpdata == null)
		{	// no table
			result[0] = String.valueOf(NO_SUCH_TABLE);
			result[1] = null;
			return result;
		}
		// get colcount
		tmpkey = tname + "/Colcount";
		tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
		if(tmpdata == null)
		{	// no column - imposible
			colcount = 0;
		}
		else
			colcount = Integer.parseInt(tmpdata);

		// get recordNums
		tmpkey = tname + "/recordnums";
		tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
		if(tmpdata == null)
		{	// no record
			recordNums = 0;
		}
		else
			recordNums = Integer.parseInt(tmpdata);
		
		System.out.println("predicate_list : " + predicate_list.size());
		if(!predicate_list.isEmpty())
		{
			whereRet = wheremethod.whereClause(tnameList, newtnameList, predicate_list, predicate_info_list, record_list, myDatabase);
			
			if(whereRet == 0)
				result[0] = String.valueOf(PRINT_DELETE);
			else
				result[0] = String.valueOf(whereRet);

		}
		else
		{
			// if no where -> adding all record to record_list
			for(int i = 1; i <= recordNums; i++)
			{	// record_list : have records to delete
				record_list.add("record" + String.valueOf(i));
			}
		}
		
		
		// delete records in record_list
		for(int i = 0; i < record_list.size(); i++)
		{
			if(deleteSpecificRecord(tname, record_list.get(i) , colcount, myDatabase) == -1)
			{
				
				undelete_count++;
				result[0] = String.valueOf(DELETE_REFERENTIAL_INTEGRITY_PASSED);
				result[1] = String.valueOf(undelete_count);
			}
			else
			{
				// decrease recordnums
				tmpkey = tname + "/recordnums";
				tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
				tmpint = Integer.parseInt(tmpdata) - 1;
				if(tmpint == 0)
					dbmanage.deleteData(tmpkey, myDatabase);
				else
				{
					tmpdata = String.valueOf(tmpint);
					dbmanage.putDataToDB(tmpkey, tmpdata, myDatabase);	
				}
				
				tmpdata = record_list.get(i);
				tmpint = Integer.parseInt(tmpdata.substring(6));
				record_Index_list.add(tmpint);
				
				delete_count++;
			}
		}

		
		if(!record_Index_list.isEmpty())
			CompressRecordIndex(tname, colcount, record_Index_list, recordNums, myDatabase);
		
		
		if(undelete_count != 0)
			result[1] = String.valueOf(delete_count) + "/" + String.valueOf(undelete_count);
		else
			result[1] = String.valueOf(delete_count);
		
		return result;
	}
	
	public int deleteSpecificRecord(String tname, String record_index, int colcount, Database myDatabase)
	{

		dbmanager dbmanage = new dbmanager();

		String key = null;
		String tmpkey = null;
		String tmpdata = null;
		String colname = null;		
		
		String ref_table = null;
		String ref_record = null;
		
		String RrecordIndex = null;
		
		int ref_tablenum = -1;
		int ref_count = -1;
		
		// get referred count
		tmpkey = tname + "/records/" + record_index + "/refer/referredcount";
		tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
		if(tmpdata != null)
		{	// referred count > 0 => can't delete this record
			if(Integer.parseInt(tmpdata) > 0)
			{
				return -1;
			}
			else
			{
				dbmanage.deleteData(tmpkey, myDatabase);
			}	
		}
		
		for(int i = 1; i <= colcount; i++)
		{
			tmpkey = tname + "/col" + String.valueOf(i);
			colname = dbmanage.getDataFromDB(tmpkey, myDatabase);	// [colname]
			key = tname + "/" + record_index + "/" + colname;
			// delete data in db - [tname] / record[index] / [colname]
			dbmanage.deleteData(key, myDatabase);					
		}
		tmpkey = tname + "/records/" + record_index + "/refer/tablenumber";
		tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
		ref_tablenum = Integer.parseInt(tmpdata);
		
		//delete data in db - [tname] / records / record[index] / refer / tablenumber
		dbmanage.deleteData(tmpkey, myDatabase);
		for(int i = 1; i <= ref_tablenum; i++)
		{
			key = tname + "/records/" + record_index + "/refer/table" + String.valueOf(i);
			ref_table = dbmanage.getDataFromDB(key, myDatabase);
			ref_record = dbmanage.getDataFromDB(key + "/frecord", myDatabase);
			
			// delete data in db - [tname] / records / record[index] / refer / table[i]
			dbmanage.deleteData(key, myDatabase);
			// delete data in db - [tname] / records / record[index] / refer / table[i] / frecord
			dbmanage.deleteData(key + "/frecord", myDatabase);
			
			dbmanage.deleteData(tname + "/records/" + record_index + "/refer/tables/" + ref_table, myDatabase);
			
			
			// decrease rtname's referred count
			tmpkey = ref_table + "/records/" + ref_record + "/refer/referredcount";
			tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
			if(tmpdata == null)
				System.out.println("something wrong in thirdhw 523 referredcount never be null");
			else if(Integer.parseInt(tmpdata) == 0)
				System.out.println("something wrong in thirdhw 526 referredcount never be 0");
			else
			{// decrease referred count because we successfully delete record
				ref_count = Integer.parseInt(tmpdata) - 1;
				tmpdata = String.valueOf(ref_count);
				dbmanage.putDataToDB(tmpkey, tmpdata, myDatabase);
				
				key = ref_table + "/records/" + ref_record + "/referred/";

				tmpkey = key + "findRrec/" + tname + "/" + record_index;
				RrecordIndex = dbmanage.getDataFromDB(tmpkey, myDatabase);
				dbmanage.deleteData(tmpkey, myDatabase);
				
				if(RrecordIndex != null)
				{ // it should't be null
					tmpkey = key + RrecordIndex + "/tname";
					dbmanage.deleteData(tmpkey, myDatabase);
					tmpkey = key + RrecordIndex + "/record";
					dbmanage.deleteData(tmpkey, myDatabase);
				}		
			}			
		}
		
		tmpkey = tname + "/records/" + record_index + "/referred/RrecNum";
		dbmanage.deleteData(tmpkey, myDatabase);
		
		return 0;
	}
	
	public void CompressRecordIndex(String tname, int colcount, ArrayList<Integer> deletedIndex, int max_recordIndex, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
		
		int targetIndex = 0;
		int emptyIndex = 0;
		
		int RrecNumber = -1;
		
		int ref_tables = 0;
		String key = null;
		String data = null;
		String key2 = null;
		
		String RrecNum = null;
		String tableIndex = null;
		
		String tmpTname = null;
		String tmpRecord = null;
		
		String tmpkey = null;
		String tmpdata = null;
		
		
		String colname = null;
		String rtname = null;
		String frecord = null;
		
		String newkey = null;
		
		
		Collections.sort(deletedIndex);
		targetIndex = deletedIndex.get(0) + 1;
		emptyIndex = deletedIndex.get(0);
		while(targetIndex <= max_recordIndex)
		{
			if(deletedIndex.contains(targetIndex) == true)
			{
				targetIndex++;
				continue;
			}
			//rtname / records / record
			ref_tables = 0;
			for(int j = 1; j <= colcount; j++)
			{
				key = tname + "/col" + String.valueOf(j);
				colname = dbmanage.getDataFromDB(key, myDatabase);
				
				// get record value
				key = tname + "/record" + String.valueOf(targetIndex) + "/" + colname;
				data = dbmanage.getDataFromDB(key, myDatabase);
				dbmanage.deleteData(key, myDatabase);
				
				newkey = tname + "/record" + String.valueOf(emptyIndex) + "/" + colname;
				dbmanage.putDataToDB(newkey, data, myDatabase);
			}
			// get ref tablenum
			key = tname + "/records/record" + String.valueOf(targetIndex) + "/refer/tablenumber";
			data = dbmanage.getDataFromDB(key, myDatabase);
			dbmanage.deleteData(key, myDatabase);
			if(data != null)
			{
				ref_tables = Integer.parseInt(data);
				newkey = tname + "/records/record" + String.valueOf(emptyIndex) + "/refer/tablenumber";
				dbmanage.putDataToDB(newkey, data, myDatabase);
			}
			else
				ref_tables = 0;
			
			for(int j = 1; j <= ref_tables; j++)
			{
				key = tname + "/records/record" + String.valueOf(targetIndex) + "/refer/table" + String.valueOf(j);
				rtname = dbmanage.getDataFromDB(key, myDatabase);
				dbmanage.deleteData(key, myDatabase);
				
				//  [tname] / records / record[ei] / refer / table[tn], [rtname]
				newkey = tname + "/records/record" + String.valueOf(emptyIndex) + "/refer/table" + String.valueOf(j);
				dbmanage.putDataToDB(newkey, rtname, myDatabase);

				// [tname] / records / record1 / refer / tables / [rtname1] , table1
				key = tname + "/records/record" + String.valueOf(targetIndex) + "/refer/tables/" + rtname;
				tmpdata = dbmanage.getDataFromDB(key, myDatabase);
				dbmanage.deleteData(key, myDatabase);
				newkey = tname + "/records/record" + String.valueOf(emptyIndex) + "/refer/tables/" + rtname;
				dbmanage.putDataToDB(newkey, tmpdata, myDatabase);

				// [tname] / records / record1 / refer / table[j] / frecord , table1
				key = tname + "/records/record" + String.valueOf(targetIndex) + "/refer/table" + String.valueOf(j) + "/frecord";
				frecord = dbmanage.getDataFromDB(key, myDatabase);
				dbmanage.deleteData(key, myDatabase);

				newkey = tname + "/records/record" + String.valueOf(emptyIndex) + "/refer/table" + String.valueOf(j) + "/frecord";
				dbmanage.putDataToDB(newkey, frecord, myDatabase);
				
				// get Rrec[?]
				key = rtname + "/records/" + frecord + "/referred/findRrec/" + tname + "/record" + String.valueOf(targetIndex);
				tmpdata = dbmanage.getDataFromDB(key, myDatabase);
				dbmanage.deleteData(key, myDatabase);
				newkey = rtname + "/records/" + frecord + "/referred/findRrec/" + tname + "/record" + String.valueOf(emptyIndex);
				dbmanage.putDataToDB(newkey, tmpdata, myDatabase); 
				newkey = rtname + "/records/" + frecord + "/referred/" + tmpdata + "/record";
				dbmanage.putDataToDB(newkey, "record" + String.valueOf(emptyIndex), myDatabase);
				
			}
			key = tname + "/records/record" + String.valueOf(targetIndex) + "/refer/referredcount";
			data = dbmanage.getDataFromDB(key, myDatabase);
			if(data != null)
			{
				dbmanage.deleteData(key, myDatabase);
				
				newkey = tname + "/records/record" + String.valueOf(emptyIndex) + "/refer/referredcount";
				dbmanage.putDataToDB(newkey, data, myDatabase);
			}
////////////////////////////
			key = tname + "/records/record" + String.valueOf(targetIndex) + "/referred/";
			newkey = tname + "/records/record" + String.valueOf(emptyIndex) + "/referred/";
			tmpkey = key + "RrecNum";
			RrecNum = dbmanage.getDataFromDB(tmpkey, myDatabase);
			dbmanage.deleteData(tmpkey, myDatabase);

			if(RrecNum != null)
			{

				tmpkey = newkey + "RrecNum";
				dbmanage.putDataToDB(tmpkey, RrecNum, myDatabase);
				
				RrecNumber = Integer.parseInt(RrecNum);
				for(int j = 1; j <= RrecNumber; j++)
				{
					tmpkey = key + "Rrec" + String.valueOf(j) + "/tname";
					// get key + Rrec1 / tname , [tname]
					tmpTname = dbmanage.getDataFromDB(tmpkey, myDatabase);


					if(tmpTname == null)
						continue;
					else
					{						
						// delete & put  key + Rrec1 / tname , [tname]
						dbmanage.deleteData(tmpkey, myDatabase);
						tmpkey = newkey + "Rrec" + String.valueOf(j) + "/tname";
						dbmanage.putDataToDB(tmpkey, tmpTname, myDatabase);

						// get & delete & put key + Rrec1 / record , record[i]
						tmpkey = key + "Rrec" + String.valueOf(j) + "/record";

						tmpRecord = dbmanage.getDataFromDB(tmpkey, myDatabase);
						dbmanage.deleteData(tmpkey, myDatabase);

						tmpkey = newkey + "Rrec" + String.valueOf(j) + "/record";
						dbmanage.putDataToDB(tmpkey, tmpRecord, myDatabase);
						
						// get & delete & put key + findRrec / [tname] / record[i] , Rrec[j]
						tmpkey = key + "findRrec/" + tmpTname + "/" + tmpRecord;

						tmpdata = dbmanage.getDataFromDB(tmpkey, myDatabase);
						dbmanage.deleteData(tmpkey, myDatabase);

						tmpkey = newkey + "findRrec/" + tmpTname + "/" + tmpRecord;
						dbmanage.putDataToDB(tmpkey, tmpdata, myDatabase);

						///
						key2 = tmpTname + "/records/" + tmpRecord + "/refer/";
						
						tmpkey = key2 + "tables/" + tname;
						tableIndex = dbmanage.getDataFromDB(tmpkey, myDatabase);
						
						tmpkey = key2 + tableIndex + "/frecord";
						dbmanage.putDataToDB(tmpkey, "record" + String.valueOf(emptyIndex), myDatabase);
						
					}
				}
				
			}
					
			
			emptyIndex++;
			targetIndex++;
		}
	}
}
