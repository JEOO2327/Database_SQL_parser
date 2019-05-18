import java.util.ArrayList;

import com.sleepycat.je.Database;

public class where {
	//WHERE CLAUSE
	public static final int WHERE_INCOMPARABLE_ERROR = -400;
	public static final int WHERE_TABLE_NOT_SPECIFIED = -401;
	public static final int WHERE_COLUMN_NOT_EXIST = -402;
	public static final int WHERE_AMBIGUOUS_REFERENCE = -403;

	
	public int whereClause(ArrayList<String> tname_list, ArrayList<String> tnewname_list, 
			ArrayList<String> predicate_list, ArrayList<String> predicate_info_list, ArrayList<String> ResultRecordTuple, Database myDatabase)
	{
		int result = 0;
		int recordTupleSize = -1;
		ArrayList<String> allRecordTuple = new ArrayList<String>();
		String recordTuple;
		
		ArrayList<String> info_list;
		ArrayList<String> list;
		
		int BTresult = 0;
		
		RecordTupleList(tname_list, allRecordTuple, myDatabase);
				
		if(allRecordTuple != null)
		{
			recordTupleSize = allRecordTuple.size();
			
			for(int i = 0; i < recordTupleSize; i++)
			{
				info_list = new ArrayList<String>();
				info_list.addAll(predicate_info_list);

				
				list = new ArrayList<String>();
				list.addAll(predicate_list);

				
				recordTuple = allRecordTuple.get(i);
				BTresult = RecordBooleanTest(tname_list, tnewname_list, list, info_list, recordTuple, myDatabase);
				
				if(BTresult == 1)
					ResultRecordTuple.add(recordTuple);
				else if(BTresult == -1)
					continue;
				else
					return BTresult;	
			}
		}
		
		return result;
	}
	
	public void RecordTupleList(ArrayList<String> tname_list, ArrayList<String> allRecordTuple, Database myDatabase)
	{
		int tname_size = -1;
		
		if(tname_list.isEmpty())
			return;
		else
			tname_size = tname_list.size();

		for(int i = 0; i < tname_size; i++)
		{
			RecordAdder(allRecordTuple, tname_list.get(i), myDatabase);
		}
	}
	
	public void RecordAdder(ArrayList<String> allRecordTuple, String tname, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();

		String key = null;
		String data = null;
		int recordnums = -1;
		int tupleNums = -1;
		
		String val = null;		
		
		key = tname + "/" + "recordnums";
		data = dbmanage.getDataFromDB(key, myDatabase);
		if(data == null)
		{
			if(allRecordTuple.isEmpty())
				allRecordTuple.add("null");
			else
			{
				tupleNums = allRecordTuple.size();
				
				for(int i = 0; i < tupleNums; i++)
				{
					val = allRecordTuple.get(i);
					allRecordTuple.set(i, val + "/null");
				}
			}
		}
		else
		{
			recordnums = Integer.parseInt(data);
			
			if(allRecordTuple.isEmpty())
			{
				for(int i = 1; i <= recordnums; i++)
				{
					allRecordTuple.add("record" + String.valueOf(i));
				}
			}
			else
			{
				tupleNums = allRecordTuple.size();
				for(int i = 0; i < tupleNums; i++)
				{
					val = allRecordTuple.get(i);
					for(int j = 1; j <= recordnums; j++)
					{
						if(j == 1)
						{
							allRecordTuple.set(i, val + "/record" + String.valueOf(j));							
						}
						else
						{
							allRecordTuple.add(val + "/record" + String.valueOf(j));
						}
					}
				}
			}
		}
	}
	
	public int RecordBooleanTest(ArrayList<String> tname_list, ArrayList<String> tnewname_list, 
			ArrayList<String> predicate_list, ArrayList<String> predicate_info_list, String recordTuple,Database myDatabase)
	{
		String result = null;
		int ret = -4000;
		ArrayList<String> ParsedTuple = new ArrayList<String>();
				
		ParsingRecordTuple(recordTuple, ParsedTuple);
		result = ParsePredicate(tname_list, tnewname_list, ParsedTuple, predicate_list, predicate_info_list, myDatabase);
		
		
		
		switch (result) {
		case "true":
			ret = 1;
			break;
		case "false":
			ret = -1;		
			break;
		case "-400":	//WHERE_AMBIGUOUS_REFERENCE
			ret = -400;
			break;
		case "-401":	//WHERE_COLUMN_NOT_EXIST
			ret = -401;
			break;
		case "-402":	//WHERE_INCOMPARABLE_ERROR
			ret = -402;
			break;
		case "-403":	//WHERE_TABLE_NOT_SPECIFIED
			ret = -403;
			break;
		default:
			System.out.println("error : " + result);
			break;
		}
		
		return ret;
	}
	
	public void ParsingRecordTuple(String recordTuple, ArrayList<String> ParsedTuple)
	{
		String[] StrToList = recordTuple.split("/");
		
		for(int i = 0; i < StrToList.length; i++)
			ParsedTuple.add(StrToList[i]);
	}
	
	public String ParsePredicate(ArrayList<String> tname_list, ArrayList<String> tnewname_list, ArrayList<String> recordTuple, 
			ArrayList<String> predicate_list, ArrayList<String> predicate_info_list, Database myDatabase)
	{
		int LOPcount = 0;
		int predicate_list_size = -1;
		int size = -1;
		int opIndex = -1;
		
		int parencheck = 0;
		
		ArrayList<String> Left_list = new ArrayList<String>();
		ArrayList<String> Left_info_list = new ArrayList<String>();
		ArrayList<String> Right_list = new ArrayList<String>();
		ArrayList<String> Right_info_list = new ArrayList<String>();

		String item = null;
		String item_info = null;
		
		String parenTestResult = null;
		
		String COperator = null;
		String NOperator = null;
		String LOperator = null;
		String left_result = null;
				
		if(predicate_info_list.isEmpty())
			return "error184";
		else
			predicate_list_size = predicate_info_list.size();
		
		for(int i = 0; i < predicate_list_size; i++)
		{
			item = predicate_info_list.get(i);
			if(item.equals("LOP"))
				LOPcount++;
		}
		
		if(LOPcount == 0)
		{
			parenTestResult = CheckParen(predicate_list, predicate_info_list);
			
			while(parenTestResult.equals("Paren"))
			{
				size = predicate_list.size();
				
				predicate_list.remove(size-1);
				predicate_info_list.remove(size-1);
				predicate_list.remove(0);
				predicate_info_list.remove(0);
				
				parenTestResult = CheckParen(predicate_list, predicate_info_list);
			}
			
			size = predicate_list.size();
			if(predicate_info_list.contains("COMPOP"))
			{
				opIndex = predicate_info_list.indexOf("COMPOP");
				for(int i = 0; i < opIndex; i++)
				{
					Left_list.add(predicate_list.get(i));
					Left_info_list.add(predicate_info_list.get(i));
				}
				COperator = predicate_list.get(opIndex);
				for(int i = opIndex + 1; i < size; i++)
				{
					Right_list.add(predicate_list.get(i));
					Right_info_list.add(predicate_info_list.get(i));
				}
				return CompOperation(tname_list, tnewname_list, recordTuple, Left_list, Left_info_list, COperator, Right_list, Right_info_list, myDatabase);
			}
			else if(predicate_info_list.contains("NOP"))
			{
				opIndex = predicate_info_list.indexOf("NOP");
				for(int i = 0; i < opIndex; i++)
				{
					Left_list.add(predicate_list.get(i));
					Left_info_list.add(predicate_info_list.get(i));
				}
				NOperator = predicate_list.get(opIndex);

				return NullOperation(tname_list, tnewname_list, recordTuple, Left_list, Left_info_list, NOperator, myDatabase);
			}
			else
			{
				System.out.println("something wrong at where 243");
				return "error243";
			}
		}
		else
		{
			parenTestResult = CheckParen(predicate_list, predicate_info_list);

			while(parenTestResult.equals("Paren"))
			{
				size = predicate_list.size();
				
				predicate_list.remove(size-1);
				predicate_info_list.remove(size-1);
				predicate_list.remove(0);
				predicate_info_list.remove(0);
				
				parenTestResult = CheckParen(predicate_list, predicate_info_list);
			}

			predicate_list_size = predicate_list.size();
			
			parencheck = 0;
			LOperator = null;
			for(int i = 0; i < predicate_list_size; i++)
			{
				item = predicate_list.get(i);
				item_info = predicate_info_list.get(i);
				
//				System.out.println("291 item " + String.valueOf(i) + " : " + item);
				
				if(item_info.equals("LP"))
					parencheck++;
				else if(item_info.equals("RP"))
					parencheck--;
				
				if(LOperator == null)
				{
					if(parencheck == 0 && predicate_info_list.get(i).equals("LOP"))
					{
						LOperator = item;
					}
					else
					{
						Left_list.add(item);
						Left_info_list.add(item_info);
					}
				}
				else
				{
					Right_list.add(item);
					Right_info_list.add(item_info);
				}
			}
			
			left_result = ParsePredicate(tname_list, tnewname_list, recordTuple, Left_list, Left_info_list, myDatabase);

			if( "true".equals(left_result) )
			{
				if("OR".equals(LOperator))
					return "true";
				else
					return ParsePredicate(tname_list, tnewname_list, recordTuple, Right_list, Right_info_list, myDatabase);
			}
			else if( "false".equals(left_result) )
			{
				if("AND".equals(LOperator))
					return "false";
				else
					return ParsePredicate(tname_list, tnewname_list, recordTuple, Right_list, Right_info_list, myDatabase);
			}
			else
				return left_result;
		}
	}
	
	public String CompOperation(ArrayList<String> tname_list, ArrayList<String> tnewname_list, ArrayList<String> recordTuple,
			ArrayList<String> Left_list, ArrayList<String> Left_info_list, String COperator,
			ArrayList<String> Right_list, ArrayList<String> Right_info_list, Database myDatabase)
	{
		String val1 = null;
		String val2 = null;
		
		String type1 = null;
		String type2 = null;
		

		ArrayList<String> result1 = new ArrayList<String>();
		ArrayList<String> result2 = new ArrayList<String>();
		
		String returnval = null;
		
		returnval = ParsingTnamePeriodColumn(tname_list, tnewname_list, recordTuple, Left_list, Left_info_list, result1, myDatabase);
		if (returnval.equals("success"))
		{
			val1 = result1.get(0);
			type1 = result1.get(1);
		}
		else
			return returnval;

		returnval = ParsingTnamePeriodColumn(tname_list, tnewname_list, recordTuple, Right_list, Right_info_list, result2, myDatabase);
		if (returnval.equals("success"))
		{
			val2 = result2.get(0);
			type2 = result2.get(1);
		}
		else
			return returnval;
		
		if(!type1.equals(type2))
		{
			return String.valueOf(WHERE_INCOMPARABLE_ERROR);
		}
		
		
/*		System.out.println("364");
		
		System.out.println("val1 : " + val1);
		System.out.println("val2 : " + val2);
*/
		
		switch (COperator) {
		case "<":
			if(type1.equals("int"))
			{
				if(Integer.parseInt(val1) < Integer.parseInt(val2))
					return "true";
				else
					return "false";
			}
			else
			{
				if(val1.compareTo(val2) < 0)
					return "true";
				else
					return "false";
			}
		case "<=":
			if(type1.equals("int"))
			{
				if(Integer.parseInt(val1) <= Integer.parseInt(val2))
					return "true";
				else
					return "false";
			}
			else
			{
				if(val1.compareTo(val2) <= 0)
					return "true";
				else
					return "false";
			}			
		case ">":
			if(type1.equals("int"))
			{
				if(Integer.parseInt(val1) > Integer.parseInt(val2))
					return "true";
				else
					return "false";
			}
			else
			{
				if(val1.compareTo(val2) > 0)
					return "true";
				else
					return "false";
			}
			
		case ">=":
			if(type1.equals("int"))
			{
				if(Integer.parseInt(val1) >= Integer.parseInt(val2))
					return "true";
				else
					return "false";
			}
			else
			{
				if(val1.compareTo(val2) >= 0)
					return "true";
				else
					return "false";
			}
		case "!=":
			if(type1.equals("int"))
			{
				if(Integer.parseInt(val1) != Integer.parseInt(val2))
					return "true";
				else
					return "false";
			}
			else
			{
				if(val1.compareTo(val2) != 0)
					return "true";
				else
					return "false";
			}	
		case "=":			
			if(type1.equals("int"))
			{
				if(Integer.parseInt(val1) == Integer.parseInt(val2))
					return "true";
				else
					return "false";
			}
			else
			{
				if(val1.compareTo(val2) == 0)
					return "true";
				else
					return "false";
			}

		default:
			break;
		}
		
		
		return "false";
	}
	public String NullOperation(ArrayList<String> tname_list, ArrayList<String> tnewname_list, ArrayList<String> recordTuple,
			ArrayList<String> list, ArrayList<String> info_list, String NOperator, Database myDatabase)
	{
		String nullresult = null;
		String notnullresult = null;
		ArrayList<String> result = new ArrayList<String>();

		String value = null;
		
		String returnval = null;

		if(NOperator.equals("is null"))
		{
			nullresult = "true";
			notnullresult = "false";
		}
		else if(NOperator.equals("is not null"))
		{
			nullresult = "false";
			notnullresult = "true";
		}
		else
		{		
			System.out.println("Something wrong at where 393 - null operator should be is null or is not null");
		}

		
		returnval = ParsingTnamePeriodColumn(tname_list, tnewname_list, recordTuple, list, info_list, result, myDatabase);

		if (returnval.equals("success"))
		{
			value = result.get(0);
		}
		else
			return returnval;
				
		if("'null".equals(value))
			return nullresult;
		else
			return notnullresult;
	}
	
	
	public String ParsingTnamePeriodColumn(ArrayList<String> tname_list, ArrayList<String> tnewname_list, ArrayList<String> recordTuple,
			ArrayList<String> list, ArrayList<String> info_list, ArrayList<String> result, Database myDatabase)
	{
		dbmanager dbmanage = new dbmanager();
		int tnameIndex = -1;
		
		
		String tnewname = null;
		String tname = null;
		String colname = null;
		String firstItem = null;
		String record = null;
		String colnum = null;

		String key = null;
		String data = null;
		
		String value = null;
		String type = null;
		String charsize = null;
		
		firstItem = info_list.get(0);
		if(firstItem.equals("tname_period"))
		{
			tnewname = list.get(0);
			if(tnewname_list.contains(tnewname))
			{
				tnameIndex = tnewname_list.indexOf(tnewname);
				tname = tname_list.get(tnameIndex);
				record = recordTuple.get(tnameIndex);
				if(info_list.get(1).equals("column_name"))
				{
					colname = list.get(1);
					key = tname + "/cols/" + colname;
					colnum = dbmanage.getDataFromDB(key, myDatabase);
					if(colnum == null)
						System.out.println("Something wrong at where 525 - colnum should not be null");
				}
				else
				{
					System.out.println("Something wrong at where 530 - null operation should have column after table name");
					return "error530";
				}
			}
			else
			{
				return String.valueOf(WHERE_TABLE_NOT_SPECIFIED);
			}
		}
		else if(firstItem.equals("column_name"))
		{
			colname = list.get(0);


			colnum = null;
			for(String table : tname_list)
			{
				key = table + "/cols/" + colname;
				data = dbmanage.getDataFromDB(key, myDatabase);
				if(data != null)
				{
					if(colnum == null)
					{	
						colnum = data;
						tname = table;
					}
					else
						return String.valueOf(WHERE_AMBIGUOUS_REFERENCE);
				}
			}
			if(tname != null)
				record = recordTuple.get(tname_list.indexOf(tname));
			else
				return String.valueOf(WHERE_COLUMN_NOT_EXIST);
			
			if(colnum == null)
				return String.valueOf(WHERE_COLUMN_NOT_EXIST);
		}
		else if(firstItem.equals("int"))
		{
			type = "int";
			value = list.get(0);
			
			result.add(value);
			result.add(type);
			return "success";
		}
		else if(firstItem.equals("char"))
		{
			type = "char";
			value = list.get(0);
			charsize = String.valueOf(value.length());
			
			result.add(value.substring(1, value.length() - 1));
			result.add(type);
			result.add(charsize);
			return "success";
		}
		else if(firstItem.equals("date"))
		{
			type = "date";
			value = list.get(0);
			
			result.add(value);
			result.add(type);
			return "success";
		}		
		else
		{
			System.out.println("firstItem : " + firstItem);
			System.out.println("Something wrong at where 566 - operation should have table or column or value in first item");
			return "error566";
		}
		
		if(record.equals("null"))
			return "false";
		
		key = tname + "/" + record + "/" + colname;
		value = dbmanage.getDataFromDB(key, myDatabase);
		
		result.add(value);

		key = tname + "/" + colnum + "/type";
		type = dbmanage.getDataFromDB(key, myDatabase);

		result.add(type);
		
		if("char".equals(type))
		{
			key = tname + "/" + colnum + "/size";
			charsize = dbmanage.getDataFromDB(key, myDatabase);
			result.add(charsize);
		}
		
		
		return "success";
	}
	
	public String CheckParen(ArrayList<String> predicate_list, ArrayList<String> predicate_info_list)
	{
		int size = -1;
		int parenStatus = 0;
		String item = null;
		
		if(predicate_info_list.isEmpty())
			return "error602";
		else
			size = predicate_info_list.size();
		
		for(int i = 0; i < size; i++)
		{
			item = predicate_info_list.get(i);
			
			if(i == 0 && !item.equals("LP"))
				return "notParen";
			
			if(item.equals("LP"))
			{
				if(i == 0)
				{
					parenStatus = 1;
				}
				else
				{
					parenStatus++;
				}
			}
			else if(item.equals("RP"))
			{
				parenStatus--;

				if(i == size - 1)
				{
					if(parenStatus == 0)
						return "Paren";
					else
						return "notParen";
				}
				else
				{
					if(parenStatus == 0)
						return "notParen";
				}
			}
		}
		return "error642";
	}
}
