import java.io.UnsupportedEncodingException;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Cursor;

import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;



public class dbmanager {
	public void putDataToDB(String keyString, String dataString, Database myDatabase)
	{
		int exist = 0;
		
		if(getDataFromDB(keyString, myDatabase) != null)
			exist = 1;

		if(exist == 1)
			deleteData(keyString, myDatabase);	

		// Open Cursor
		Cursor cursor = null;
		cursor = myDatabase.openCursor(null,null);

		try
		{ 
			DatabaseEntry key = new DatabaseEntry( keyString.getBytes("UTF-8"));
			DatabaseEntry data = new DatabaseEntry( dataString.getBytes("UTF-8") );
		  	cursor.put(key, data);
		} catch(DatabaseException de)
		{
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		// closing cursor
		cursor.close();		
	}

	
	public String getDataFromDB(String keyString, Database myDatabase)
	{
	  	String dataString = null;
		try
		{ 
			DatabaseEntry key = new DatabaseEntry(keyString.getBytes("UTF-8"));
			DatabaseEntry data = new DatabaseEntry();
			if ( myDatabase.get(null, key, data, LockMode.DEFAULT) ==
				OperationStatus.SUCCESS)
			{
				byte[] retData = data.getData();
				String foundData = new String(retData, "UTF-8");

				dataString = foundData;
			}
			else
			{
				dataString = null;
			}
		}catch(UnsupportedEncodingException e) {
	  		e.printStackTrace();
		}

		return dataString;
	} 
	
	public void deleteData(String keyString, Database myDatabase)
	{
	  	int exist = 0;
	  	 	
		if(getDataFromDB(keyString, myDatabase) != null)
			exist = 1;

		
	  	if(exist == 1)
		{	  		
	  		try {
			  	DatabaseEntry theKey = new DatabaseEntry(keyString.getBytes("UTF-8"));
			  	myDatabase.delete(null, theKey);
			} catch (Exception e) {
			}
		} 	
	}
}
