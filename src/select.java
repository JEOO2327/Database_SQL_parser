import java.util.ArrayList;

import com.sleepycat.je.Database;


public class select {

	//SUCCESS
	public static final int PRINT_SELECT = 6;

	//FAIL
	public static final int SELECT_TABLE_EXISTENCE_ERROR = -61;
	public static final int	SELECT_COLUMN_RESOLVE_ERROR = -62;

	
	public String[] selectRecord(ArrayList<String> tname_list, ArrayList<String> tnewname_list, ArrayList<String> table_period_list, ArrayList<String> colname_list, 
			ArrayList<String> cnewname_list, ArrayList<String> predicate_list, ArrayList<String> predicate_info_list, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
		where wheremethod = new where();
		
		ArrayList<String> ResultRecordTuples = new ArrayList<String>();
		String recordTuple;		
		
		
		String[] result = new String[2];
		result[0] = String.valueOf(PRINT_SELECT);
		result[1] = null;
		
		int intval = -1;
		
		int colname_size = -1;
		
		String key = null;
		String data = null;
		
		for(String tmpTname : tname_list)
		{
			key = tmpTname + "/exist";
			data = dbmanage.getDataFromDB(key, myDatabase);
			if(data == null)
			{
				result[0] = String.valueOf(SELECT_TABLE_EXISTENCE_ERROR);
				result[1] = tmpTname;
				return result;
			}
		}

		if(predicate_list.isEmpty())
		{ // no where clause
			wheremethod.RecordTupleList(tname_list, ResultRecordTuples, myDatabase);
			intval = 0;
		}
		else
			intval = wheremethod.whereClause(tname_list, tnewname_list, predicate_list, predicate_info_list, ResultRecordTuples, myDatabase);
		
//		System.out.println("intval : " + intval);
		if(intval == 0)
			result[0] = String.valueOf(PRINT_SELECT);
		else
		{
			result[0] = String.valueOf(intval);
			return result;
		}
		

		result = PrintAttributeName(tname_list, tnewname_list, table_period_list, colname_list, cnewname_list, myDatabase);
		
		if(result[0].equals(String.valueOf(SELECT_COLUMN_RESOLVE_ERROR)) )
			return result;
		else if(result[0].equals(String.valueOf(SELECT_TABLE_EXISTENCE_ERROR)) )
			return result;
		
		
		
		for(String tmp : ResultRecordTuples)
		{
			recordTuple = tmp;
			PrintSelectedRecord(tname_list, tnewname_list, table_period_list, colname_list, recordTuple, myDatabase);
		}

		
		
		/*
		ArrayList<String> record_list = new ArrayList<String>();
		
		print_select(table_period_list, colname_list, cnewname_list, record_list);
		
		*/

		colname_size = colname_list.size();
		
		if(!ResultRecordTuples.isEmpty())
		{
			for(int i = 0; i < colname_size; i++)
			{
				System.out.print("+---------------");
			}
			System.out.println("+");
		}
		
		
		return result;
	}

	public String[] PrintAttributeName(ArrayList<String> tname_list, ArrayList<String> tnewname_list, ArrayList<String> table_period_list, ArrayList<String> colname_list, ArrayList<String> cnewname_list, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();

		ArrayList<String> Line = new ArrayList<String>();		
		
		int colnameSize = 0;
		int tname_nums = 0;
		
		int colCount = 0;
		
		int tnewIndex = -1;		
		
		String result[] = new String[2];
		
		result[0] = String.valueOf(PRINT_SELECT);
		result[1] = null;
		
		String cnameToPrint = null;
		
		String key = null;
		String data = null;
		
		String colname = null;
		
		String tname = null;
		String tnewname = null;
		
		String tmp = null;
		
		ArrayList<String> LineTwo = new ArrayList<String>();
		
		Line.add("+");
		Line.add("|");
		Line.add("+");
		
		
		colnameSize = colname_list.size();
		tname_nums = tname_list.size();
		
		if(colname_list.get(0).equals("*"))
		{
			colname_list.remove(0);
			cnewname_list.remove(0);
			table_period_list.remove(0);
			
			for(int i = 0; i < tname_nums; i++)
			{
				tname = tname_list.get(i);
				tnewname = tnewname_list.get(i);
				key = tname + "/Colcount";
				data = dbmanage.getDataFromDB(key, myDatabase);
				colCount = Integer.parseInt(data);

				for(int j = 1; j <= colCount; j++)
				{
					key = tname + "/col" + String.valueOf(j);
					colname = dbmanage.getDataFromDB(key, myDatabase);
					
					table_period_list.add(tnewname);
					colname_list.add(colname);
					
					tmp = Line.get(0);
					Line.set(0, tmp + "---------------+");

					tmp = Line.get(2);
					Line.set(2, tmp + "---------------+");
					
				}
			}
			
			
			System.out.println(Line.get(0));
			
			System.out.print("|");
			for(int i = 0; i < colname_list.size(); i++)
				System.out.printf("%15s|", colname_list.get(i).toUpperCase());
			System.out.println();
			System.out.println(Line.get(2));
			
			result[0] = String.valueOf(PRINT_SELECT);
			return result;
		}
		else
		{
			colnameSize = colname_list.size();
			
			for(int i = 0; i < colnameSize; i++)
			{
				cnameToPrint = "";
				tnewname = table_period_list.get(i);
				
				if(tnewname.equals("null"))
				{

					tnewname = FindTnameOfCol(tname_list, tnewname_list, colname_list.get(i), myDatabase);
					
					if(tnewname.equals("null"))
					{
						result[0] = String.valueOf(SELECT_COLUMN_RESOLVE_ERROR);
						result[1] = colname_list.get(i);
						return result;
					}
					
					table_period_list.set(i, tnewname);
				}
				else
				{
					if(tnewname_list.contains(tnewname))
					{
						tnewIndex = tnewname_list.indexOf(tnewname);
						tname = tname_list.get(tnewIndex);
					}
					else
					{
						result[0] = String.valueOf(SELECT_TABLE_EXISTENCE_ERROR);
						result[1] = tnewname;
						return result;
					}

					
					key = tname + "/cols/" + colname_list.get(i);
					data = dbmanage.getDataFromDB(key, myDatabase);
					
					if(data == null)
					{
						result[0] = String.valueOf(SELECT_COLUMN_RESOLVE_ERROR);
						result[1] = tnewname + "." + colname_list.get(i);
						return result;
					}
					else
					{
						cnameToPrint = tnewname + "."; 
					}
				}
				
				if(cnewname_list.get(i).equals(colname_list.get(i)))
					cnameToPrint = cnameToPrint + colname_list.get(i);
				else
					cnameToPrint = cnewname_list.get(i);
				
				
				if(tnewname_list.contains(tnewname))
				{
					tnewIndex = tnewname_list.indexOf(tnewname);
					
					tname = tname_list.get(tnewIndex);
					
					LineTwo.add(cnameToPrint);

					tmp = Line.get(0);
					Line.set(0, tmp + "---------------+");

					
					
					tmp = Line.get(2);
					Line.set(2, tmp + "---------------+");
				}
				else
				{
					result[0] = String.valueOf(SELECT_COLUMN_RESOLVE_ERROR);
					result[1] = cnameToPrint;
					return result;
				}
			}
			
			System.out.println(Line.get(0));
			
			System.out.print("|");
			for(int i = 0; i < LineTwo.size(); i++)
				System.out.printf("%15s|", LineTwo.get(i).toUpperCase());
			System.out.println();
			System.out.println(Line.get(2));

			result[0] = String.valueOf(PRINT_SELECT);
			return result;
		}
	}
	
	public String FindTnameOfCol(ArrayList<String> tname_list, ArrayList<String> tnewname_list, String columnName, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();

		String key = null;
		String data = null;
		
		String retTnewname = null;
		
		int tnameNums = -1;
		
		tnameNums = tname_list.size();
		
		for(int i = 0; i < tnameNums; i++)
		{
			key = tname_list.get(i) + "/cols/" + columnName;
			data = dbmanage.getDataFromDB(key, myDatabase);
			
			if(data == null)
				continue;
			else
			{
				if(retTnewname == null)
					retTnewname = tnewname_list.get(i);
				else
					return "null";
			}
		}
		
		if(retTnewname != null)
			return retTnewname;
		
		return "null";
	}
	
	public String PrintSelectedRecord(ArrayList<String> tname_list, ArrayList<String> tnewname_list
			, ArrayList<String> table_period_list, ArrayList<String> colname_list, String recordTuple, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
		where wheremethod = new where();

		String key = null;
		String recordData = null;
		
		String tname = null;
		String record_Index = null;
		
		String table = null;
		String col = null;
		
		int IndexOfTname = -1;
		int colcount = -1;
		
		ArrayList<String> ParsedRecordTuple = new ArrayList<String>();
		
		wheremethod.ParsingRecordTuple(recordTuple, ParsedRecordTuple);
		
		if(ParsedRecordTuple.isEmpty())
			return "error";
			
		colcount = colname_list.size();
		
		
		
		for(int i = 0; i < colcount; i++)
		{
			table = table_period_list.get(i);
			col = colname_list.get(i);
					
			if(table.equals("null"))
				table = FindTnameOfCol(tname_list, tnewname_list, col, myDatabase);
			
			
			if(tnewname_list.contains(table))
			{
				IndexOfTname = tnewname_list.indexOf(table);
			}
			else
				System.out.println("something wrong at 378 in select - tname_list have to contain table");

			record_Index = ParsedRecordTuple.get(IndexOfTname);
			tname = tname_list.get(IndexOfTname);
			
			key = tname + "/" + record_Index + "/" + col;
			recordData = dbmanage.getDataFromDB(key, myDatabase);

			if(recordData.equals("'null"))
				recordData = "null";
			
			System.out.printf("|%15s", recordData.toUpperCase());
		}
		System.out.println("|");

		
		
		return "Success";
		
	}
}

