/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 
 
      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 
 
      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 
 
      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
          List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Add to contact list");
                System.out.println("2. Browse contact list");
                System.out.println("3. Write a new message");
                //adding these
                System.out.println("4. Browse blocked list");
                System.out.println("5. Browse current chats");
                System.out.println("6. Create a new chat");
                System.out.println("8. Delete Account");
                //done adding new ones
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: AddToContact(authorisedUser, esql); break;
                   case 2: ListContacts(authorisedUser, esql); break;
                   case 3: NewMessage(esql); break;
                   case 4: ListBlocked(authorisedUser, esql); break;
                   case 5: BrowseChats(authorisedUser, esql); break;
                   case 6: NewChat(authorisedUser, esql); break;
                   //case 7: (esql); break;
                   case 8: DeleteAccount(authorisedUser, esql); usermenu=false; break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main
  
   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

	 //Creating empty contact\block lists for a user
	 esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
	 int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
	 int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
         
	 String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

//-----------------Main menu (after login)-------------
	//When adding a new contact, ask for the username of the new contact
	//then add that contact to the user_list of the current user
   public static void AddToContact(String authorisedUser, Messenger esql){
	   //get list_id of authorized(current) user
	   //request name of new contact from user
	   //insert into user_list_contains (new contact login, list_id)
	   try{
		 System.out.print("\tEnter login of user to add: ");
		 String userToAdd = in.readLine();
		 String query1 = String.format("SELECT U.contact_list FROM Usr U WHERE U.login = '%s'", authorisedUser);
		 List<List<String>> contact_list = esql.executeQueryAndReturnResult(query1); 
		 int i = Integer.parseInt(contact_list.get(0).get(0));
		 String query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", i, userToAdd);
		 esql.executeUpdate(query);
	   }catch(Exception e){
		   System.err.println (e.getMessage ());
	   }
   }

	//access the user's contacts and list them by name
   public static void ListContacts(String authorisedUser, Messenger esql){
	   try{
		String query = String.format("SELECT C.list_member FROM Usr U, USER_LIST_CONTAINS C WHERE U.login = '%s' AND U.contact_list = C.list_id ",authorisedUser);
		int success = esql.executeQueryAndPrintResult (query);
	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

	//Let the usr create a new message
	//create a new chat in the process
	//generate a new chat_id from function
	//**ask the user if it's a private chat or  
	//generate a new message serial id from sequence function
	//ask the user for the 
   public static void NewMessage(Messenger esql){
   } 
   
   public static void ListBlocked(String authorisedUser, Messenger esql){
      try{
		String query = String.format("SELECT C.list_member FROM Usr U, USER_LIST_CONTAINS C WHERE U.login = '%s' AND U.block_list = C.list_id ",authorisedUser);
		int success = esql.executeQueryAndPrintResult (query);
	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   
   //show a user their chats
   public static void BrowseChats(String authorisedUser, Messenger esql){
        try{
		String query = String.format("SELECT C.chat_id FROM CHAT_LIST C WHERE C.member = '%s'",authorisedUser);
		int success = esql.executeQueryAndPrintResult (query);
	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }

   }
   
   //let a user make a NewChat
   //generate new chat_id from sequence
   //ask if it's private or public 
   //set the init sender to authorisedUser
   public static void NewChat(String authorisedUser,Messenger esql){
       try{
	   int new_chat_id = esql.getCurrSeqVal("user_list_list_id_seq"); //can't get value from seq
	   String query = String.format("INSERT INTO CHAT(chat_id, chat_type, init_sender) VALUES ('%s', private, '%s')", new_chat_id, authorisedUser);
	   esql.executeUpdate(query);
	   System.out.print("\tWould you like to add more users?");
	   System.out.print("\t1. No");
	   System.out.print("\t2. Yes");
	   switch(readChoice()){
	      case 1: break;
              case 2: AddMembersToChat(esql); break;
              default : System.out.println("Unrecognized choice!"); break;
	   }
	}catch(Exception e){
		   System.err.println (e.getMessage ());
	}
   }
   
   //Delete Row in Usr Table 
   public static void DeleteAccount(String authorisedUser,Messenger esql){
       try{
	   String query = String.format("DELETE FROM USR WHERE login = '%s'", authorisedUser);
	   esql.executeUpdate(query);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }
   }
   
//---------------Chat Menu fuctions--------------------
   public static void AddMembersToChat(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end
   
   public static void DeleteMembersFromChat(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end
   
   public static void DeleteChat(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end
   
   public static void ShowMessages(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end
   
//---------------Message Menu Fuctions----------------
   public static void CreateNewMessage(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end
   
   public static void EditMessage(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end
   
   public static void DeleteMessage(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end

}//end Messenger
