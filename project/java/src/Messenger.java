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
               case 2: authorisedUser = LogIn(esql);if(authorisedUser== null){System.out.println("Invalid username or password.");} break;
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
                   case 3: NewMessage(authorisedUser,esql); break;
                   case 4: ListBlocked(authorisedUser, esql); break;
                   case 5: BrowseChats(authorisedUser, esql); break;
                   case 6: NewChat(false,authorisedUser, esql); break;
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
   public static void AddToContact(String authorisedUser, Messenger esql){
	   try{
		 System.out.print("\tEnter login of user to add: ");
		 String userToAdd = in.readLine();
		 //TODO: validate that userToAdd is a user!
		 //TODO: make sure user isn't already in user's contact list
		 String query1 = String.format("SELECT contact_list FROM Usr WHERE login = '%s'", authorisedUser);
		 List<List<String>> contact_list = esql.executeQueryAndReturnResult(query1); 
		 int i = Integer.parseInt(contact_list.get(0).get(0));
		 String query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", i, userToAdd);
		 esql.executeUpdate(query);
	   }catch(Exception e){
		   System.err.println (e.getMessage ());
	   }
   }
   public static void AddToBlocked(String authorisedUser, Messenger esql){
	   try{
		 System.out.print("\tEnter login of user to block: ");
		 String userToAdd = in.readLine();
		 //TODO: validate that userToAdd is a user!
		 //TODO: make sure user isn't already in user's contact list
		 String query1 = String.format("SELECT block_list FROM Usr WHERE login = '%s'", authorisedUser);
		 List<List<String>> contact_list = esql.executeQueryAndReturnResult(query1); 
		 int i = Integer.parseInt(contact_list.get(0).get(0));
		 String query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", i, userToAdd);
		 esql.executeUpdate(query);
	   }catch(Exception e){
		   System.err.println (e.getMessage ());
	   }
   }


	//access the user's contacts and list them by name
	//DONE
   public static void ListContacts(String authorisedUser, Messenger esql){
	   try{
		String query = String.format("SELECT C.list_member, U.status FROM Usr U, USER_LIST_CONTAINS C WHERE U.login = '%s' AND U.contact_list = C.list_id ",authorisedUser);
		System.out.println("\nContacts: ");
		int success = esql.executeQueryAndPrintResult (query);
		boolean usermenu=true;
		while(usermenu){
		System.out.println("");
		System.out.println("1. Add User to Contact List");
                System.out.println("2. Remove User from Contact List");
                System.out.println("3. Exit to Main Menu");
                switch (readChoice()){
                   case 1: AddToContact(authorisedUser, esql); break;
                   case 2: DeleteContact(authorisedUser,esql); break;
                   case 3: usermenu=false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
		}
	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   public static void DeleteContact(String authorisedUser,Messenger esql){
       try{
	   String query1 = String.format("SELECT contact_list FROM Usr WHERE login = '%s'",authorisedUser);
	   List<List<String>> list_id = esql.executeQueryAndReturnResult(query1);
	   System.out.print("\tEnter user to remove from contacts: ");	//get user to add to chat
	   String user_to_del = in.readLine();
	   String query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id='%s' AND list_member='%s'",Integer.parseInt(list_id.get(0).get(0)), user_to_del);
	   esql.executeUpdate(query);
       }catch(Exception e){
	   System.err.println(e.getMessage());
       }
   }

  public static void DeleteBlocked(String authorisedUser,Messenger esql){
       try{
	   String query1 = String.format("SELECT block_list FROM Usr WHERE login = '%s'",authorisedUser);
	   List<List<String>> list_id = esql.executeQueryAndReturnResult(query1);
	   System.out.print("\tEnter user to unblock: ");	//get user to add to chat
	   String user_to_del = in.readLine();
	   String query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id='%s' AND list_member='%s'",Integer.parseInt(list_id.get(0).get(0)), user_to_del);
	   esql.executeUpdate(query);
       }catch(Exception e){
	   System.err.println(e.getMessage());
       }
   }


   public static void NewMessage(String authorisedUser, Messenger esql){
       try{
	   //create a new chat 
	   int new_chat = NewChat(true, authorisedUser,esql);
	   CreateNewMessage(new_chat, authorisedUser, esql);

	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }

   } 
   
   public static void ListBlocked(String authorisedUser, Messenger esql){
      try{
		String query = String.format("SELECT C.list_member FROM Usr U, USER_LIST_CONTAINS C WHERE U.login = '%s' AND U.block_list = C.list_id ",authorisedUser);
		System.out.println("\nBlocked Users:");
		int success = esql.executeQueryAndPrintResult (query);
		System.out.println("");
		boolean usermenu=true;
		while(usermenu){
		System.out.println("");
		System.out.println("1. Add User to Block List");
                System.out.println("2. Remove User from Block List");
                System.out.println("3. Exit to Main Menu");
                switch (readChoice()){
                   case 1: AddToBlocked(authorisedUser, esql); break;
                   case 2: DeleteBlocked(authorisedUser,esql); break;
                   case 3: usermenu=false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
		}
	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   
   //show a user their chats
   //We might want to have this method ask the user what chat they want to view
   //and then call view chat from here
   //for this to work the rest of the chat methods need to be implemented
   public static void BrowseChats(String authorisedUser, Messenger esql){
        try{
	    	//print the list of Chat_ids 
		String query = String.format("SELECT C.chat_id FROM CHAT_LIST C WHERE C.member = '%s'",authorisedUser);
		int success = esql.executeQueryAndPrintResult (query);
		//ask which chat they want to enter
		//TODO: allow them to remove themselves from chats here????
		System.out.print("\tEnter chat_id of chat to view: ");
		String chat_to_view = in.readLine();
		//find out if they are the init sender of the chat
		//TODO: MAKE SURE THE USER IS IN THIS CHAT!!!!!!
		String query1 = String.format("SELECT init_sender FROM CHAT WHERE chat_id = '%s'", chat_to_view);
		List<List<String>> init_sender = esql.executeQueryAndReturnResult(query1);
		String init = init_sender.get(0).get(0);
		if(authorisedUser == init){
		    boolean usermenu = true;
		    while(usermenu){
			System.out.print("\tEnter Choice: ");
			System.out.print("\t1. Add Members ");
			System.out.print("\t2. Delete Members");
			System.out.print("\t3. Delete Chat ");
			System.out.print("\t4. Continue to Messages ");
			switch(readChoice()){
			    case 1: AddMembersToChat(Integer.parseInt(chat_to_view), esql); break;
			    case 2: DeleteMembersFromChat(Integer.parseInt(chat_to_view),esql); break;
			    case 3: DeleteChat(Integer.parseInt(chat_to_view), esql); usermenu=false; break;
			    case 4: usermenu=false; break;
			    default : System.out.println("Unrecognized choice!"); break;
			}
		    }
		}
		    //find all message and order by timestamp
		    List<List<String>> message_list = ShowMessages(Integer.parseInt(chat_to_view), esql);
		    //each List<String>> contains the sender, timestamp, and text
		    int index = message_list.size()-11;
		    PrintMessagesout(index, message_list);
		    boolean usermenu2 = true;
		    while(usermenu2){
			System.out.println("Enter Choice: ");
			System.out.println("\t1. Load Earlier Messages ");
			System.out.println("\t2. Delete Message");
			System.out.println("\t3. Write New Message ");
			System.out.println("\t4. Edit a Message ");
			System.out.println("\t5. Exit to Main Menu ");
			switch(readChoice()){
			    case 1: 
			    	index=index-10;
				PrintMessagesout(index,message_list);
				break;
			    case 2:{ 
				DeleteMessage(Integer.parseInt(chat_to_view),esql);
				message_list=ShowMessages(Integer.parseInt(chat_to_view),esql);
				index=message_list.size()-11;
				PrintMessagesout(index,message_list); 
				break;
			    }case 3:{ 
				CreateNewMessage(Integer.parseInt(chat_to_view),authorisedUser, esql);
				message_list=ShowMessages(Integer.parseInt(chat_to_view),esql);
				index=message_list.size()-11;
				PrintMessagesout(index,message_list); 
				break;
			    }case 4:{ 
				EditMessage(Integer.parseInt(chat_to_view), esql);
				message_list=ShowMessages(Integer.parseInt(chat_to_view),esql);
				index=message_list.size()-11;
				PrintMessagesout(index,message_list); 
				break;
			    }case 5:usermenu2=false; break; //back to main menu
			    default : System.out.println("Unrecognized choice!"); break;
			}
	       }
	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }

   public static void PrintMessagesout(int index, List<List<String>> message_list){
       System.out.println("");
       if(index<0){index=0;}
       for (int i = index; i< message_list.size() ; ++i){
	    System.out.println("Sender: " + message_list.get(i).get(0));
	    System.out.println("Message ID:" + message_list.get(i).get(1));
	    System.out.println(message_list.get(i).get(2));
	    System.out.println(message_list.get(i).get(3));
	}
   }


   
   //let a user make a NewChat
   //generate new chat_id from sequence
   //ask if it's private or public 
   //set the init sender to authorisedUser
   //DONE
   public static int NewChat(boolean isMsg, String authorisedUser,Messenger esql){
       try{
	   //create a new chat row in CHAT
	   //starts as private
	   String query = String.format("INSERT INTO CHAT(chat_type, init_sender) VALUES ( 'private', '%s')", authorisedUser);
	   esql.executeUpdate(query);
	   //get chat_id of previously created chat
	   int new_chat_id = esql.getCurrSeqVal("chat_chat_id_seq"); //can't get value from seq
	   //add init sender to chat in chat_list
	   String query2 = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES ('%s', '%s')", new_chat_id, authorisedUser);
	   esql.executeUpdate(query2);
	   //ask for other user to add to the chat
	   AddMembersToChat(new_chat_id, esql);
	   //ask if they want to add more users
	   if(!isMsg){
	   boolean usermenu = true;
	   boolean group = false;
	   while(usermenu){
	       System.out.print("Would you like to add more users?\n");
	       System.out.print("\t1. Yes\n");
	       System.out.print("\t2. No\n");
	       switch(readChoice()){
		  case 1: if(!group){SwitchToGroup(new_chat_id, esql);group=true;}AddMembersToChat(new_chat_id, esql); break;
		  case 2: usermenu=false; break;
		  default: System.out.println("Unrecognized choice!"); break;
	       }
	   }
	   //each chat needs at least one message
	   System.out.println("Please write a message to the chat.");
	   CreateNewMessage(new_chat_id, authorisedUser,esql);
	   }
	   return new_chat_id;
	}catch(Exception e){
		   System.err.println (e.getMessage ());
		   return -1;
	}
   }
   
   //delete Rows in CHAT_LIST with that user login
   //if theyre the init_sender in CHAT --> call DeleteChat
   //iterate through Message table deleting messages they've sent sender=current user -->deleteMess
   //delete usr_list_contains --> delete own contact list and blocked list
   //delete usr_list search for list ID
   //Delete Row in Usr Table (might have to do this before user_list_contains 
   //DONE
   public static void DeleteAccount(String authorisedUser,Messenger esql){
       try{
	   //delete messages where they are the sender
	   String query3 = String.format("DELETE FROM MESSAGE WHERE sender_login='%s'", authorisedUser);
	   esql.executeUpdate(query3);

	   //delete Rows in chat_list with that user login
	   String query1 = String.format("DELETE FROM CHAT_LIST WHERE member='%s'", authorisedUser);
	   esql.executeUpdate(query1);
	   //delete chats that user is init sender of
	   String query2 = String.format("SELECT chat_id FROM CHAT WHERE init_sender='%s'", authorisedUser);
	   List<List<String>> init_sender_chats = esql.executeQueryAndReturnResult(query2);
	   //iterate through init_sender_chats and call DeleteChat()
	   for( int i = 0; i < init_sender_chats.get(0).size(); ++i){
	       DeleteChat(Integer.parseInt(init_sender_chats.get(0).get(i)), esql);
	   }

	   //delete row in USR table
	   String query9 = String.format("DELETE FROM USR WHERE login='%s'", authorisedUser);
	   esql.executeUpdate(query9);

	   //delete user's contact list and block list
	   String query4 = String.format("SELECT contact_list, block_list FROM USR WHERE login = '%s'", authorisedUser);
	   List<List<String>> user_lists = esql.executeQueryAndReturnResult(query4);
	   String query5 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id='%s'",user_lists.get(0).get(0));
	   esql.executeUpdate(query5);
	   String query6 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id='%s'",user_lists.get(0).get(1));
	   esql.executeUpdate(query6);

	   //delete row in usr_list 
	   String query7 = String.format("DELETE FROM USER_LIST WHERE list_id='%s'",user_lists.get(0).get(1));
	   esql.executeUpdate(query7);
	   String query8 = String.format("DELETE FROM USER_LIST WHERE list_id='%s'",user_lists.get(0).get(1));
	   esql.executeUpdate(query8);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }
   }
   
//---------------Chat Menu fuctions--------------------
	//switches a chat from private to group
	//DONE
   public static void SwitchToGroup(int chat_id, Messenger esql){
       try{
	   String query = String.format("UPDATE CHAT SET chat_type = 'group' WHERE chat_id = '%s'", chat_id);
	   esql.executeUpdate(query);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }
   }


//DONE.. ask for a member to add to chat and adds them to corresponding chat of chat_id
   public static void AddMembersToChat(int chat_id, Messenger esql){
       try{
	   System.out.print("\tEnter user to add to the chat: ");	//get user to add to chat
	   String user_to_add = in.readLine();
	   String query3 = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES ('%s', '%s')", chat_id, user_to_add);
	   esql.executeUpdate(query3);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }
   }
   
   //DONE.. ask for a member to delete from the chat
   public static void DeleteMembersFromChat(int chat_id, Messenger esql){
      try{
	   System.out.print("\tEnter user to remove to the chat: ");	//get user to add to chat
	   String user_to_del = in.readLine();
	   String query3 = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s' AND member='%s'",chat_id, user_to_del);
	   esql.executeUpdate(query3);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }

   }
   
   //delete row from CHAT
   //delete rows from CHAT_LIST
   //delete all messages = chat_id
   //DONE
   public static void DeleteChat(int chat_id, Messenger esql){
       try{
	   //delete from CHAT table
	   	   //delete from CHAT_LIST table
	   String query4 = String.format("DELETE FROM CHAT_LIST WHERE chat_id='%s'",chat_id );
	   esql.executeUpdate(query4);
	   //delete from MESSAGE
	   String query5 = String.format("DELETE FROM MESSAGE WHERE chat_id='%s'",chat_id );
	   esql.executeUpdate(query5);
	   String query3 = String.format("DELETE FROM CHAT WHERE chat_id='%s'",chat_id);
	   esql.executeUpdate(query3);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }

   }
   
   //takes the chat_id, and returns a list of all messages in the chat indexed by timestamp
   //DONE
   public static List<List<String>> ShowMessages(int chat_id, Messenger esql){
       try{
	 String query = String.format("SELECT sender_login,msg_id, msg_timestamp, msg_text FROM MESSAGE WHERE chat_id = '%s' ORDER BY msg_timestamp",chat_id );
	 List<List<String>> message_list = esql.executeQueryAndReturnResult(query);
	 return message_list;
       }catch(Exception e){
	   System.err.println (e.getMessage ());
	 return null;
       }
   }
   
//---------------Message Menu Fuctions----------------
//parameter for chat_id
//ask user for text
// timestamp = (SELECT LOCAL TIMESTAMP(2))
//DONE ! 
   public static void CreateNewMessage(int chat_id, String authorisedUser, Messenger esql){
       try{
	   System.out.print("\nPlease enter message body of new message: ");
	   String text = in.readLine();
	   String query3 = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES ('%s', (SELECT LOCALTIMESTAMP(2)), '%s', '%s')", text, authorisedUser, chat_id);
	   esql.executeUpdate(query3);
	   System.out.println("\nMessage Created!");
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }
   }
   
   //UPDATE ask for mesg id from user
   //DONE
   public static void EditMessage(int chat_id, Messenger esql){
       try{
	   System.out.print("\tEnter message id of message to edit: ");
	   String msg_to_edit = in.readLine();
	   System.out.print("\tEnter new message body: ");
	   String new_text = in.readLine();
	   //TODO: make sure the user is the creator of the message and that the message exists
	   String query3 = String.format("UPDATE MESSAGE SET msg_text='%s' WHERE chat_id='%s' AND msg_id='%s'", new_text, chat_id, msg_to_edit);
	   esql.executeUpdate(query3);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }
   }
   
   //DELETE asking for message id from user
   //delete message from MESSAGE table
   //DONE
   public static void DeleteMessage(int chat_id, Messenger esql){
      try{
	   System.out.print("\tEnter message id of message to remove: ");
	   String msg_to_del = in.readLine();
	   //TODO: make sure the user is the creator of the message
	   String query3 = String.format("DELETE FROM MESSAGE WHERE chat_id='%s' AND msg_id='%s'",chat_id, msg_to_del);
	   esql.executeUpdate(query3);
       }catch(Exception e){
	   System.err.println (e.getMessage ());
       }

   }

}//end Messenger
