
public class printmessage {
	  public static final int PRINT_SYNTAX_ERROR = 0;
	  public static final int SUCCESS_CREATE_TABLE = 1;
	  public static final int SUCCESS_DROP_TABLE = 2;
	  public static final int SUCCESS_DESC = 3;
	  public static final int PRINT_INSERT = 4;
	  public static final int PRINT_DELETE = 5;
	  public static final int PRINT_SELECT = 6;
	  public static final int SUCCESS_SHOW_TABLES = 7;

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
		
		public static final int DROP_REFERENCED_TABLE_ERROR = -21;
		public static final int NO_SUCH_TABLE = -22;
		
		public static final int SHOW_TABLES_NO_TABLE = -71;

	public static void printMessage(int q, String str)
	{
		switch(q)
	    {
	    	case PRINT_SYNTAX_ERROR:
	      		System.out.println("Syntax error");
	      	break;
	    	case SUCCESS_CREATE_TABLE:
	      		System.out.println("\'" + str + "\' table is created");
	      	break;
	    	case SUCCESS_DROP_TABLE:
	    	  	System.out.println("\'" + str + "\' table is dropped");
	      	break;
	    	case SUCCESS_DESC:      
	      		//System.out.println("\'DESC\' requested");
	      	break;
	    	case PRINT_INSERT:
	      		System.out.println("\'INSERT\' requested");
	      	break;
	    	case PRINT_DELETE:
	    	  	System.out.println("\'DELETE\' requested");
	      	break;
	    	case PRINT_SELECT:
	    	  	System.out.println("\'SELECT\' requested");
	      	break;   	
	    	case SUCCESS_SHOW_TABLES:
	      		//System.out.println("\'SHOW TABLES\' requested");
	      	break;

//FAIL TO CREATE TABLE
			case TABLE_EXISTENCE_ERROR:
	      		System.out.println("Create table has failed: table with the same name already exists");
			break;
			case DUPLICATE_COLUMN_DEF_ERROR:
	      		System.out.println("Create table has failed: column definition is duplicated");
			break;
			case DUPLICATE_PRIMARY_KEY_ERROR:
	      		System.out.println("Create table has failed: primary key definition is duplicated");
			break;
			case CHAR_LENGTH_ERROR:
	      		System.out.println("Char length should be over 0");
			break;
			
// FAIL TO CREATE TABLE BECAUSE OF REFERENCE			
			case REFERENCE_TYPE_ERROR:
	      		System.out.println("Create table has failed: foreign key references wrong type");
			break;
			case REFERENCE_NON_PRIMARY_KEY_ERROR:
	      		System.out.println("Create table has failed: foreign key references non primary key column");
			break;
			case REFERENCE_COLUMN_EXISTENCE_ERROR:
	      		System.out.println("Create table has failed: foreign key references non existing column");
			break;
			case REFERENCE_TABLE_EXISTENCE_ERROR:
	      		System.out.println("Create table has failed: foreign key references non existing table");
			break;
			case NON_EXISTING_COLUMN_DEF_ERROR:
	      		System.out.println("Create table has failed: \'" + str + "\' does not exists in column definition");
			break;
			case DUPLICATE_FOREIGN_KEY_ERROR:
	      		System.out.println("Create table has failed: same foreign key defined multiple time");
			break;
//FAIL TO DROP TABLE
			case DROP_REFERENCED_TABLE_ERROR:
	      		System.out.println("Drop table has failed: \'" + str + "\' is referenced by other table");
			break;
			case NO_SUCH_TABLE:
	      		System.out.println("No such table");
			break;
			
//FAIL TO SHOW TABLES
			case SHOW_TABLES_NO_TABLE:
	      		System.out.println("There is no table");
			break;
			
	    }
	    System.out.print("DB_2016-10586> ");
	}
}
