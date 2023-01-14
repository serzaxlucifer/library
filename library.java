// Our goal is to produce 3 sets of programs and to compare their efficiency!
/*
 * 1st MODEL: Uses .bin files to store data. We believe, this will provide the quickest program (in case data is not very huge)!
 * 2ND MODEL: Uses mySQL databases to sync all data on program boot-up in RAM. Slower, and requires a lot of setup on client side.
 * 3RD MODEL: Totally based on DBMS (mySQL). All execution will involve the database. Updates will be directly made there. Can handle very large data with more efficiency and much more optimally. (no such need is required for now. So we will not work on this model. Besides, this model IS JUST an extension of Model-2, can be worked later on!)
 */

 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.*;
 import java.util.Date;
 import java.io.*;
 import java.text.*;
 import java.time.*;
import java.time.temporal.ChronoUnit;
import java.sql.*;
 
 // For fast access, Member IDs have been directly hashed to an array. No searching is required.
 // WRITTEN BY MUKUL MALIK (21UCS133)
 // auto queue and one more thing -- stack
 
 // REMOVE CASE SENSITIVITY IN SEARCH FUNCTIONS. ADD JDBC - Java DataBase Connectivity

 /* 
 class DeletedRecords {
     book[] bookRecs;
     members[] memberRecs;
     int bc = 0;
     int mc = 0;
 
     DeletedRecords() {
         bookRecs = new book[5];
         memberRecs = new members[5];
     }
 
     void addBRec(book o) {
 
     }
 
     void addMRec(members o) {
         if (mc == memberRecs.length) {
             resizeArray();
         }
         memberRecs[mc] = o;
         mc++;
     }
 
     void flushRecs() {
         for (int i = 0; i < mc; i++) {
             memberRecs[i] = null;
         }
         for (int i = 0; i < bc; i++) {
             bookRecs[i] = null;
         }
         mc = 0;
         bc = 0;
     }
 
     void printDelRecords() {
         System.out.printf(
                 "Displaying Archived -- TERMINATED Member Records: \n\nUSER ID\t\tDATE OF JOINING\t\tUSER NAME\t\tPHONE NUMBER\n");
         for (int i = 0; i < mc; i++) {
             System.out.printf("%d\t\t%d - %d - %d \t\t%s\t\t%d\n", memberRecs[i].id, memberRecs[i].joinDate.getDate(),
                     (memberRecs[i].joinDate.getMonth()), memberRecs[i].joinDate.getYear(), memberRecs[i].name,
                     memberRecs[i].phoneNumber);
             System.out.println();
             System.out.println("                    ISSUE HISTORY");
             System.out.println("-------------------------------------------------------");
             System.out.printf("BOOK ID \t ISSUE DATE \t RETURN DATE \n");
             for (int j = 0; j < memberRecs[i].rec_count; j++) {
                 if (j == memberRecs[i].rec_count - 1) {
                     if (memberRecs[i].record[j].ret == null) {
                         System.out.printf("%d \t\t %d-%d-%d \t NOT YET RETURNED.\n", memberRecs[i].record[j].book_id,
                                 memberRecs[i].record[j].issue.getDate(), (memberRecs[i].record[j].issue.getMonth()),
                                 memberRecs[i].record[j].issue.getYear());
                     } else {
                         System.out.printf("%d \t\t %d-%d-%d \t %d-%d-%d \n", memberRecs[i].record[j].book_id,
                                 memberRecs[i].record[j].issue.getDate(), (memberRecs[i].record[j].issue.getMonth()),
                                 memberRecs[i].record[j].issue.getYear(), memberRecs[i].record[j].ret.getDate(),
                                 (memberRecs[i].record[j].ret.getMonth()), memberRecs[i].record[j].ret.getYear());
                     }
                 } else {
                     System.out.printf("%d \t\t %d-%d-%d \t %d-%d-%d \n", memberRecs[i].record[j].book_id,
                             memberRecs[i].record[j].issue.getDate(), (memberRecs[i].record[j].issue.getMonth()),
                             memberRecs[i].record[j].issue.getYear(), memberRecs[i].record[j].ret.getDate(),
                             (memberRecs[i].record[j].ret.getMonth()), memberRecs[i].record[j].ret.getYear());
                 }
             }
             System.out.println();
             System.out.println("-------------------------------------------------------");
         }
         System.out.println();
     }
 
     void resizeArray() {
         members[] newarr = new members[2 * memberRecs.length];
         for (int i = 0; i < mc; i++) {
             newarr[i] = memberRecs[i];
         }
         memberRecs = newarr;
     }
 }
 */
 
 class issue_record {
     int book_id; // book_id in case this array is used by user class to store issue history and
                  // used as user_id when used by book class to store issue history of a book.
     LocalDate issue;
     LocalDate ret;
     int status; // 0 if it is not returned, 1 if it was returned.
 
     issue_record(int b, LocalDate d) {
         status = 0;
         book_id = b;
         issue = d;
         ret = null;
     }
 
     issue_record(int b, java.sql.Date d, java.sql.Date ret, int stat) // for synchronization with DB
     {
         status = stat;
         book_id = b;
 
         issue = d.toLocalDate();
 
         if (ret == null) {
 
             this.ret = null;
         } else {
             this.ret = ret.toLocalDate();
         }
 
     }
 
     void close_rec(LocalDate d) {
         ret = d;
         status = 1;
     }
 
 }
 
 class book {
 
     int book_id;
     String name;
     String author;
     int quantity;
     int issued_copies = 0;
 
     Queue<Integer> idQueue = new LinkedList<Integer>(); // Member IDs. But then if it is static -- it is shared over
                                                         // all. I want queue of a particular book.
 
     static int no_books = 0;
 
     public issue_record[] record = null;
     int rec_count = 0;
 
     book() {
         record = new issue_record[5];
     }
 
     book(int book_id, String name, String author, int quantity, int issued_copies, int rec_count, int q1, int q2,
             int q3, int q4, int q5) {
         record = new issue_record[5];
         this.book_id = book_id;
         this.name = name;
         this.author = author;
         this.quantity = quantity;
         this.issued_copies = issued_copies;
         // this.rec_count = rec_count;
         if (q1 != 0)
             idQueue.add(q1);
         if (q2 != 0)
             idQueue.add(q2);
         if (q3 != 0)
             idQueue.add(q3);
         if (q4 != 0)
             idQueue.add(q4);
         if (q5 != 0)
             idQueue.add(q5);
 
     }
 
     void addNewBook(int id, BufferedReader reader)
             throws IOException {
         book_id = id;
 
         System.out.print("Please Enter the QUANITITY of the book you want to add:   ");
         quantity = Integer.parseInt(reader.readLine());
         if (quantity <= 0) {
             System.out.println(
                     "QUANTITY CAN NOT BE LESS THAN 1. Setting it to default i.e. 1. You may delete this book from the command panel.");
             quantity = 1;
         }
 
         System.out.print("Please Enter the NAME of the book you want to add:   ");
         name = reader.readLine();
 
         System.out.print("Please Enter the AUTHOR'S NAME of the book you want to add:   ");
         author = reader.readLine();
 
         System.out.println();
 
         issued_copies = 0;
         no_books++;
     }
 
     void searchName(String bname) {
 
         for (int i = 0; i < name.length() - bname.length() + 1; i++) {
             for (int j = 0; j < bname.length(); j++) {
                 if (name.charAt(i + j) != bname.charAt(j)) {
                     break;
                 }
                 if (j == bname.length() - 1) {
                     System.out.printf("\n%d\t\t\t%s\t\t%s\t\t%d\t\t\t%d\t\n", book_id, name, author, quantity,
                             issued_copies);
                     if (issued_copies != 0) {
                         System.out.printf("Issued currently to:\t");
                         for (int k = 0; k < rec_count; k++) {
                             if (record[k].status == 0) {
                                 System.out.printf("%d    ", record[k].book_id);
                             }
                         }
                     }
                     return;
                 }
             }
         }
     }
 
     void searchAuthor(String aname) {
         for (int i = 0; i < author.length() - aname.length() + 1; i++) {
             for (int j = 0; j < aname.length(); j++) {
                 if (author.charAt(i + j) != aname.charAt(j)) {
                     break;
                 }
                 if (j == aname.length() - 1) {
                     System.out.printf("\n%d\t\t\t%s\t\t%s\t\t%d\t\t\t%d\t\n", book_id, name, author, quantity,
                             issued_copies);
                     if (issued_copies != 0) {
                         System.out.printf("Issued currently to:\t");
                         for (int k = 0; k < rec_count; k++) {
                             if (record[k].status == 0) {
                                 System.out.printf("%d    ", record[k].book_id);
                             }
                         }
                     }
                     return;
                 }
             }
         }
     }
 
     void updateRecs(int u_id) {
         for (int i = 0; i < rec_count; i++) {
             if (record[i].book_id == u_id) {                    // UID is member ID Delete all fields of this ID for this book!
                 
                 for (int j = i; j < rec_count - 1; j++) {
                     record[j] = record[j + 1];
                 }
                 record[rec_count - 1] = null;
                 rec_count--;
             }
         }
     }
 
     void issue(int user_id, LocalDate d) {
         issued_copies++;
         issue_record add_rec = new issue_record(user_id, d);
         if (rec_count == record.length) {
             record = resizeArray(record);
         }
         record[rec_count] = add_rec;
         rec_count++;
     }
 
     void ret(LocalDate d, int id) {
         issued_copies--;
         System.out.println("THIS WAS CALLED!");
         for (int i = 0; i < rec_count; i++) {
             if (record[i].book_id == id && record[i].status == 0) {
                System.out.println("closing your book record!");
                 record[i].close_rec(d);
                 break;
             }
             if (i == rec_count - 1) {
                 System.out.println("This book was never issued by this User ID.");
             }
         }
     }
 
     void modify(BufferedReader reader, Statement statement)
             throws IOException {
 
         System.out.println("Welcome to Book Modification Panel:    ");
         System.out.println("CURRENT DETAILS:    ");
         System.out.println();
         System.out.println("1) NAME:    " + name);
         System.out.println("2) BOOK ID:    " + book_id);
         System.out.println("3) AUTHOR NAME:" + author);
         System.out.println();
         System.out.print("WHAT FIELD DO YOU WISH TO MODIFY? [1, 2 or 3] :    ");
         switch (Integer.parseInt(reader.readLine())) {
             case 1:
                 System.out.print("Enter Updated Name");
                 this.name = reader.readLine();
                 try {
                                         
                     System.out.println(
                         // update book set name = ? WHERE book_id = ?
                         "COMMAND: update book set name = \"" + this.name + "\" WHERE book_id = " + this.book_id);
                         
                         int res = statement.executeUpdate(
                             "update book set name = \"" + this.name + "\" WHERE book_id = " + this.book_id);
                             
                 } 
                 catch (Exception ebisu) 
                 {
                             System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                             ebisu.printStackTrace();
                             
                             System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                 }
                 break;
             case 2:
                 System.out.println(
                         "Book IDs are not modifiable at this point of time. We expect this feature to launch in later time.");
                 break;
             case 3:
                 System.out.print("Enter Updated Author Name:    ");
                 author = reader.readLine();
                 try {
                                         
                     System.out.println(
                         // update book set name = ? WHERE book_id = ?
                         "COMMAND: update book set author = \"" + this.author + "\" WHERE book_id = " + this.book_id);
                         
                         int res = statement.executeUpdate(
                             "update book set author = \"" + this.author + "\" WHERE book_id = " + this.book_id);
                             
                 } 
                 catch (Exception ebisu) 
                 {
                             System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                             ebisu.printStackTrace();
                             
                             System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                 }
                 break;
             default:
                 System.out.println("Invalid field! No changes were made.");
                 break;
         }
     }
 
     void addStock(BufferedReader reader, Statement statement)
             throws IOException {
         System.out.println("CURRENT QUANTITY OF BOOK: " + quantity);
         System.out.print("Please Enter the QUANITITY of the book to add or subtract:   ");
         quantity += Integer.parseInt(reader.readLine());
         if (quantity <= 0) {
             System.out.println(
                     "Upon updating the stock, the stock quantity became less than or equal to 0 which is not acceptable. So, we've set it to default value i.e. 1.");
             quantity = 1;
         }
         try {
                                         
             System.out.println(
                 // update book set name = ? WHERE book_id = ?
                 "COMMAND: update book set quantity = " + quantity + " WHERE book_id = " + book_id);
                 
                 int res = statement.executeUpdate(
                     "update book set quantity = " + quantity + " WHERE book_id = " + book_id);
         } 
         catch (Exception e1) 
         {
                     System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                     e1.printStackTrace();
                     
                     System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
         }
         System.out.println("UPDATED QUANTITY OF BOOK: " + quantity);
     }
 
     void printHistory() {
         System.out.println();
         System.out.println("                    ISSUE HISTORY");
         System.out.println("-------------------------------------------------------");
         System.out.printf("USER ID \t ISSUE DATE \t RETURN DATE \n");
         for (int i = 0; i < rec_count; i++) {
             if (record[i].ret == null) {
                 System.out.printf("%d \t\t %s \t NOT YET RETURNED.\n", record[i].book_id,
                         record[i].issue.toString());
             } else {
                 System.out.printf("%d \t\t %s \t %s \n", record[i].book_id, record[i].issue.toString(),
                         record[i].ret.toString());
             }
         }
         System.out.println();
         System.out.println("-------------------------------------------------------");
     }
 
     void retDate() {
         System.out.println();
         System.out.println("          Expected Return Dates for this book");
         System.out.println("-------------------------------------------------------");
         System.out.printf("USER ID \t ISSUE DATE \t EXPECTED RETURN DATE \n");
         for (int i = 0; i < rec_count; i++) {
             if (record[i].ret == null) {
                 LocalDate date1 = record[i].issue;
                 LocalDate returnvalue = date1.plusDays(7);
 
                 System.out.printf("%d \t\t %s \t %s.\n", record[i].book_id, date1.toString(), returnvalue.toString());
             }
         }
         System.out.println();
         System.out.println("-------------------------------------------------------");
     }
 
     issue_record[] resizeArray(issue_record[] arr) {
         int s = arr.length;
         issue_record[] newarr = new issue_record[2 * s];
         for (int i = 0; i < s; i++) {
             newarr[i] = arr[i];
         }
         return newarr;
 
     }
 
 }
 
 class members {
 
     int id;
     long phoneNumber;
     String name;
     boolean issue = false;
     LocalDate joinDate;
     boolean membership; // 0 for suspension and 1 for active membership
     boolean status; // 0 for terminated account and 1 for active
 
     LocalDate[] unsubDates; // The date following an unsubscribe date will be its resubscribing counterpart.
     int unsubCount = 0;
 
     static int numberMembers = 0;
 
     issue_record[] record;
     int rec_count = 0;
 
     static Stack<Integer> idStack = new Stack<>(); // Available IDs.
 
     static boolean getIDStatus() {
         return idStack.empty();
     }
 
     members() {
 
     }
 
     members(int id, long phone, String name, int issue, int membership, int status, java.sql.Date joinDate, int unsubc,
             int rec_count) {
         this.id = id;
         phoneNumber = phone;
         this.name = name;
         if (issue == 1) {
             this.issue = true;
         } else {
             this.issue = false;
         }
         if (membership == 1) {
             this.membership = true;
         } else {
             this.membership = false;
         }
         if (status == 1) {
             this.status = true;
         } else {
             this.status = false;
         }
 
         this.joinDate = joinDate.toLocalDate();
 
         unsubCount = unsubc;
         this.rec_count = 0;
         // record created!
         record = new issue_record[5];
         unsubDates = new LocalDate[5];
     }
 
     int addMemberStack(BufferedReader reader)
             throws IOException {
         // assume that if this is called then stack is not empty.
         id = idStack.pop();
         System.out.print("Enter Name of the User:    ");
         name = reader.readLine();
         System.out.print("Enter phone number:    ");
         phoneNumber = Long.parseLong(reader.readLine());
 
         joinDate = LocalDate.now();
 
         rec_count = 0;
         System.out.printf("Member registered successfully! \n\nMEMBER ID: %d %n%n", id);
         record = new issue_record[5];
         membership = true;
         status = true;
 
         return (id - 1);
     }
 
     void addMember(BufferedReader reader)
             throws IOException {
         id = ++numberMembers;
 
         System.out.print("Enter Name of the User:    ");
         name = reader.readLine();
         System.out.print("Enter phone number:    ");
         phoneNumber = Long.parseLong(reader.readLine());
 
         joinDate = LocalDate.now();
 
         rec_count = 0;
         System.out.printf("Member registered successfully! \n\nMEMBER ID: %d %n%n", id);
         record = new issue_record[5];
         membership = true;
         status = true;
     }
 
     // returns whether a user was on an active membership during a specific month or
     // not.
     boolean getMembershipStatus(int month, int year) {
         LocalDate prev;
         LocalDate next;
         LocalDate cur = LocalDate.of(year, month, 1);
 
         for (int i = 0; i < unsubCount; i++) {
             prev = unsubDates[i];
             if (unsubCount - 1 == i) {
                 if ((unsubDates[i].getMonthValue() == month && unsubDates[i].getYear() == year)
                         || (cur.isAfter(prev))) {
                     return false;
                 }
                 return true;
             }
             next = unsubDates[i + 1];
             if ((unsubDates[i].getMonthValue() == month && unsubDates[i].getYear() == year)
                     || (cur.isAfter(prev) && cur.isBefore(next))) {
                 return false;
             }
 
             i++;
         }
         return true;
     }
 
     void unsubscribe(BufferedReader reader, Statement statement)
             throws IOException {
 
         if (unsubCount % 2 != 0) {
             System.out.println("You're currently already unsubscribed!");
             return;
         }
 
         System.out.print("For what month are you un-subscribing? :    ");
         int m = Integer.parseInt(reader.readLine());
         System.out.print("For what year are you un-subscribing? :    ");
         int y = Integer.parseInt(reader.readLine());
         LocalDate to_add = LocalDate.of(y, m, 1);
 
         if (getMembershipStatus(m, y) == false) {
             System.out.println("You were already unsubscribed!");
         }
 
         membership = false;
 
         if (unsubCount == 0) {
             unsubDates = new LocalDate[5];
         }
         if (unsubCount == unsubDates.length) {
             resizeUnsubArray();
         }
         unsubDates[unsubCount] = to_add;
         unsubCount++;
         try {
                                         
             System.out.println(
                 // update book set name = ? WHERE book_id = ?
                 "COMMAND: insert into unsub_dates(MemID, start, end) values(" + this.id + ", '" + to_add + "', NULL)");
                 
                 int res = statement.executeUpdate(
                     "insert into unsub_dates(MemID, start, end) values(" + this.id + ", '" + to_add + "', NULL)");
 
             System.out.println(
                         // update book set name = ? WHERE book_id = ?
                 "COMMAND: update member set unsubCount = " + unsubCount + " WHERE id = " + this.id);
                         
                 res = statement.executeUpdate(
                     "update member set unsubCount = " + unsubCount + " WHERE id = " + this.id);

                     System.out.println("COMMAND EXECUTED! res= " + res);

                     System.out.println(
                        // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                        "COMMAND: update member set membership = 0 WHERE member_id = " + this.id);
                        
            res = statement.executeUpdate(
                            "update member set membership = 0 WHERE id = " + this.id);
                     
         } 
         catch (Exception ebisu) 
         {
                     System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                     ebisu.printStackTrace();
                     
                     System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
         }
         System.out.println(
                 "Unsubcribed for specified month successfully. You won't be charged till you re-subscribe. Issuing will also be blocked till that time. Returns will still work.");
     }
 
     void resizeUnsubArray() {
         LocalDate[] newdarr = new LocalDate[2 * unsubDates.length];
         for (int i = 0; i < unsubCount; i++) {
             newdarr[i] = unsubDates[i];
         }
         unsubDates = newdarr;
     }
 
     void resubscribe(BufferedReader reader, Statement statement)
             throws IOException {
         if (unsubCount % 2 == 0) {
             System.out.println("You're currently already on an active subscrition!");
             return;
         }
 
         System.out.print("For what month are you re-subscribing? :    ");
         int m = Integer.parseInt(reader.readLine());
         System.out.print("For what year are you re-subscribing? :    ");
         int y = Integer.parseInt(reader.readLine());
         LocalDate to_add = LocalDate.of(y, m, 1);
         membership = true;
 
         if (unsubCount == unsubDates.length) {
             resizeUnsubArray();
         }
 
         unsubDates[unsubCount] = to_add;
         unsubCount++;
         try {
                                         
             System.out.println(
                 // update book set name = ? WHERE book_id = ?
                 "COMMAND: update unsub_dates set end = '" + to_add + "' WHERE MemID = " + this.id + " AND end IS NULL");
                 
                 int res = statement.executeUpdate(
                     "update unsub_dates set end = '" + to_add + "' WHERE MemID = " + this.id + " AND end IS NULL");
 
             System.out.println(
                         // update book set name = ? WHERE book_id = ?
                 "COMMAND: update member set unsubCount = " + unsubCount + " WHERE id = " + this.id);
                         
                 res = statement.executeUpdate(
                     "update member set unsubCount = " + unsubCount + " WHERE id = " + this.id);

                     System.out.println("COMMAND EXECUTED! res= " + res);

                     System.out.println(
                        // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                        "COMMAND: update member set membership = 1 WHERE member_id = " + this.id);
                        
            res = statement.executeUpdate(
                            "update member set membership = 1 WHERE id = " + this.id);
                     
         } 
         catch (Exception ebisu) 
         {
                     System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                     ebisu.printStackTrace();
                     
                     System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
         }
 
         System.out.println("You've been re-subscribed.");
     }
 
     void terminateAccount() {
         idStack.push(id);
         status = false;
     }
 
     boolean getValidityStatus() {
         return status;
     }
 
     void modify(BufferedReader reader, Statement statement)
             throws IOException {
 
         System.out.println("Welcome to Modification Panel:    ");
         System.out.println("CURRENT DETAILS:    ");
         System.out.println();
         System.out.println("1) NAME:    " + name);
         System.out.println("2) MEMBER ID:    " + id);
         System.out.println("3) PHONE NUMBER:    " + phoneNumber);
         System.out.println();
         System.out.print("WHAT FIELD DO YOU WISH TO MODIFY? [1 or 3] :    ");
         switch (Integer.parseInt(reader.readLine())) {
             case 1:
                 System.out.print("Enter New Name:    ");
                 name = reader.readLine();
                 try {
                                         
                     System.out.println(
                         // update book set name = ? WHERE book_id = ?
                         "COMMAND: update member set name = \"" + this.name + "\" WHERE id = " + this.id);
                         
                         int res = statement.executeUpdate(
                             "update member set name = \"" + this.name + "\" WHERE id = " + this.id);
                             
                 } 
                 catch (Exception ebisu) 
                 {
                             System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                             ebisu.printStackTrace();
                             
                             System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                 }
                 break;
             case 3:
                 System.out.print("Enter New Phone Number:    ");
                 phoneNumber = Long.parseLong(reader.readLine());
                 try {
                                         
                     System.out.println(
                         // update book set name = ? WHERE book_id = ?
                         "COMMAND: update member set phoneNumber = " + this.phoneNumber + " WHERE id = " + this.id);
                         
                         int res = statement.executeUpdate(
                             "update member set phoneNumber = " + this.phoneNumber + " WHERE id = " + this.id);
                             
                 } 
                 catch (Exception ebisu) 
                 {
                             System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                             ebisu.printStackTrace();
                             
                             System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                 }
                 break;
             default:
                 System.out.println("Invalid field! No changes were made.");
                 break;
         }
     }
 
     LocalDate issueBook(int id, Statement statement, ResultSet resultset) {
         issue = true;
         // date parse
         LocalDate d;
 
         d = LocalDate.now();
 
         issue_record add_rec = new issue_record(id, d);
         if (rec_count == record.length) {
             record = resizeArray(record);
         }
         record[rec_count] = add_rec;
         rec_count++;
         
         try {
                                         
             System.out.println(
                 // insert into issue_history(member_id, book_id, issue_date, ret_date, status) values()
                 "COMMAND: insert into issue_history(member_id, book_id, issue_date, ret_date, status) values(" + this.id + ", " + id + ", '" + d + "', NULL, 0)");
                 
                 int res = statement.executeUpdate(
                     "insert into issue_history(member_id, book_id, issue_date, ret_date, status) values(" + this.id + ", " + id + ", '" + d + "', NULL, 0)");
 
             System.out.println(
                         // update book set name = ? WHERE book_id = ?
                 "COMMAND: update member set rec_count = " + this.rec_count + " WHERE id = " + this.id);
                         
                 res = statement.executeUpdate(
                     "update member set rec_count = " + this.rec_count + " WHERE id = " + this.id);

                     System.out.println("COMMAND EXECUTED! res= " + res);

                     System.out.println(
                        // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                        "COMMAND: update book set issued_copies = issued_copies + 1 WHERE book_id = " + id);
                        
            res = statement.executeUpdate(
                            "update book set issued_copies = issued_copies + 1 WHERE book_id = " + id);

                            System.out.println(
                                // update book set name = ? WHERE book_id = ?
                        "COMMAND: update member set issue = 1 WHERE id = " + this.id);
                                
                        res = statement.executeUpdate(
                            "update member set issue = 1 WHERE id = " + this.id);
                     
                 } catch (Exception ebisu) {
                     System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                     ebisu.printStackTrace();
                     
                     System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                 }
 
         return d;
 
     }
 
     LocalDate retBook(Statement statement) {
         System.out.println("Book with ID: " + record[rec_count - 1].book_id + " successfully returned!");
         LocalDate d;
 
         d = LocalDate.now();
         record[rec_count - 1].close_rec(d);
         try {
                                         
             System.out.println(
                 // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                 "COMMAND: update issue_history set ret_date = '" + d + "' WHERE status = 0 AND member_id = " + this.id);
                 
                 int res = statement.executeUpdate(
                     "update issue_history set ret_date = '" + d + "' WHERE status = 0 AND member_id = " + this.id);
 
            System.out.println("COMMAND EXECUTED! res= " + res);
             System.out.println(
                 // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                 "COMMAND: update issue_history set status = 1 WHERE status = 0 AND member_id = " + this.id);
                 
                res = statement.executeUpdate(
                     "update issue_history set status = 1 WHERE status = 0 AND member_id = " + this.id);

            System.out.println("COMMAND EXECUTED! res= " + res);


                     System.out.println(
                        // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                        "COMMAND: update member set issue = 0 WHERE id = " + this.id);
                        
            res = statement.executeUpdate(
                            "update member set issue = 0 WHERE id = " + this.id);

                            System.out.println("COMMAND EXECUTED! res= " + res);

                            System.out.println(
                               // update issue_history set status = 1 AND ret_date = WHERE status = 0 AND member_id = AND book_id = 
                               "COMMAND: update book set issued_copies = issued_copies - 1 WHERE book_id = " + record[rec_count - 1].book_id);
                               
                   res = statement.executeUpdate(
                                   "update book set issued_copies = issued_copies - 1 WHERE book_id = " + record[rec_count - 1].book_id);
                     
         } 
         catch (Exception ebisu) 
         {
                     System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                     ebisu.printStackTrace();
                     
                     System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
         }
         issue = false;
 
         return d;
 
     }
 
     issue_record[] resizeArray(issue_record[] arr) {
         int s = arr.length;
         issue_record[] newarr = new issue_record[2 * s];
         for (int i = 0; i < s; i++) {
             newarr[i] = arr[i];
         }
         return newarr;
 
     }
 
     void printHistory() {
         System.out.println();
         System.out.println("                    ISSUE HISTORY");
         System.out.println("-------------------------------------------------------");
         System.out.printf("BOOK ID \t ISSUE DATE \t RETURN DATE \n");
         for (int i = 0; i < rec_count; i++) {
             if (i == rec_count - 1) {
                 if (record[i].ret == null) {
                     System.out.printf("%d \t\t %s \t NOT YET RETURNED.\n", record[i].book_id,
                             record[i].issue.toString());
                 } else {
                     System.out.printf("%d \t\t %s \t %s \n", record[i].book_id, record[i].issue.toString(),
                             record[i].ret.toString());
                 }
             } else {
                 System.out.printf("%d \t\t %s \t %s \n", record[i].book_id, record[i].issue.toString(),
                         record[i].ret.toString());
             }
         }
         System.out.println();
         System.out.println("-------------------------------------------------------");
     }
 
     // Prints bills
     void print_bill(BufferedReader reader)
             throws IOException {
         int month;
         int year;
 
         System.out.print("Please enter the year number for which you want the bill: ");
         year = Integer.parseInt(reader.readLine());
         System.out.print("Please enter the month number for which you want the bill: ");
         month = Integer.parseInt(reader.readLine());
 
         if (year < joinDate.getYear() || (year == joinDate.getYear() && joinDate.getMonthValue() > month)) {
             System.out.println("The user was not active during that month.");
             return;
         }
 
         int totalBill = 0;
         int fine = 0;
 
         LocalDate d = LocalDate.of(year, month, 1);
 
         System.out.println();
         System.out.println("+-----------------------------------------------------------------------+");
         System.out.println("|                                 BILL                                  |");
         System.out.println("+-----------------------------------------------------------------------+");
         System.out.printf("| MONTH: %d              USER ID: %d               YEAR: %d\t        |\n", month, id, year);
         System.out.printf("| USER NAME: %s        PHONE NUMBER: %d        \t\t|\n", name, phoneNumber);
         System.out.println("|                                                                       |");
         System.out.println("|                                                                       |");
         System.out.println("| BILL BREAKDOWN:                                                       |");
         System.out.println("+-----------------------------------------------------------------------+");
         if (getMembershipStatus(month, year)) {
             System.out.printf("| MEMBERSHIP CHARGES:     100                                           |\n");
             totalBill += 100;
         } else {
             System.out.printf("| MEMBERSHIP CHARGES:     000                                           |\n");
         }
         System.out.println("|                                                                       |");
         System.out.printf("| BOOK ID \t ASSOCIATED CHARGES \t ISSUE DATE \t RETURN DATE    |\n");
         // System.out.println("+----------------------------------------------------------------------+");
 
         for (int i = 0; i < rec_count; i++) {
 
             if ((record[i].issue.getMonthValue()) == month && record[i].issue.getYear() == year) {
                 if (record[i].status == 0) {
                     System.out.printf("| %d \t\t ISSUED THIS MONTH BUT NOT YET RETURNED.                |\n",
                             record[i].book_id);
                 } else if (record[i].status == 1 && (record[i].ret.getMonthValue()) == month
                         && record[i].ret.getYear() == year) {
                     int days = record[i].ret.getDayOfMonth() - record[i].issue.getDayOfMonth();
                     if (days <= 7) {
                         fine = 0;
                     } else {
                         days -= 7;
                         fine = 10 * days;
                         totalBill += fine;
                     }
                     System.out.printf("| %d\t\t %d \t\t\t %s \t %s\t|\n", record[i].book_id, fine,
                             record[i].issue.toString(), record[i].ret.toString());
                 }
             }
 
             else if (record[i].status == 1 && (record[i].ret.getMonthValue()) == month
                     && record[i].ret.getYear() == year) {
                 int days = 0;
                 LocalDate date1 = record[i].issue;
                 LocalDate date2 = record[i].ret;
                 days = (int) (ChronoUnit.DAYS.between(date1, date2));
                 // System.out.println("DAYS: " + days);
 
                 if (days <= 7) {
                     fine = 0;
                 } else if (days <= 30) {
                     fine = (days - 7) * 10;
                 } else if (days <= 60) {
                     fine = ((days - 30) * 50) + 230;
                 } else {
                     fine = 680 + (((days - 60) / 30) * 50) + ((days - 60) * 50);
                 }
                 totalBill += fine;
                 System.out.printf("| %d\t\t %d \t\t\t %s \t %s\t|\n", record[i].book_id, fine,
                         record[i].issue.toString(), record[i].ret.toString());
 
             }
 
             fine = 0;
         }
 
         System.out.println("+-----------------------------------------------------------------------+");
         System.out.printf("| BILL TOTAL: %d\t                                                |\n", totalBill);
         System.out.println("+-----------------------------------------------------------------------+");
 
     }
 
 }
 
 class driver {
     static int mainmenu(BufferedReader reader)
             throws IOException {
         int choice = 0;
 
         System.out.printf("%n%n   WELCOME TO MAIN-MENU:");
         System.out.printf("%n1. Insert a Book.");
         System.out.printf("%n2. Register a new user.");
         System.out.printf("%n3. Issue a book.");
         System.out.printf("%n4. Display Issue History of a User.");
         System.out.printf("%n5. Check issue history of a book.");
         System.out.printf("%n6. Print bill for a user.");
         System.out.printf("%n7. Return a book.");
         System.out.printf("%n8. Display available books and available users.");
         System.out.printf("%n9. Exit the program [You will lose data].");
         System.out.printf("%n10. Modify a book.");
         System.out.printf("%n11. Modify a user.");
         System.out.printf("%n12. Terminate an account.");
         System.out.printf("%n13. Unsubscribe an account.");
         System.out.printf("%n14. Resubscribe an account.");
         System.out.printf("%n15. Print all archived records and their issue histories.");
         System.out.printf("%n16. Flush all records from the archive.");
         System.out.printf("%n17. Search for a book using a keyword.");
         System.out.printf("%n18. Update quantity of a book.");
         System.out.printf("%n19. Notice on Program Updates [COMING SOON].");
         System.out.printf("%n%n    ENTER COMMAND NUMBER:    ");
         try {
             choice = Integer.parseInt(reader.readLine());
         } catch (Exception e) {
             System.out.println(e.getMessage());
             System.out.println("INVALID INPUT FORMAT. Numberic values between 1-19 are allowed.");
             choice = 100;
         }
         System.out.printf("%n%n");
         return choice;
     }
 }
 
 public class library {
 
     static members[] resizeMemberArray(members[] m) {
         members[] nm = new members[2 * m.length];
         System.out.printf("%n%nResizing member array to %d size.%n%n", 2 * m.length);
         for (int i = 0; i < m.length; i++) {
             nm[i] = m[i];
         }
         return nm;
     }
 
     static book[] resizeBookArray(book[] b) {
         book[] nb = new book[2 * b.length];
         System.out.printf("%n%nResizing book array to %d size.%n%n", 2 * b.length);
         for (int i = 0; i < b.length; i++) {
             nb[i] = b[i];
         }
         b = nb;
         return nb;
     }
 
     public static void main(String[] args)
             throws IOException, SQLException, ClassNotFoundException {
         int member_id, book_id = 0;
         LocalDate d = null;
         // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
 
         members[] memData = new members[5];
         book[] bookData = new book[5];
         // DeletedRecords archive = new DeletedRecords();
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
         System.out.printf("\nSynchronizing with Database...\n");
 
         // -------------------------------------------------------------------------
         // START SYNCHRONIZING BOOK DATA IN ENTIRETY!
 
         int id;
         Connection connection = null;
         book ref = null;
 
         Class.forName("com.mysql.cj.jdbc.Driver");
 
         connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lib", "root", "WorkH@rdPl@yH@rd098");
         //Connection connection_deletedRecs = DriverManager.getConnection("jdbc:mysql://localhost:3306/lib", "root", "WorkH@rdPl@yH@rd098");
         Statement statement = connection.createStatement();
         Statement statement2 = connection.createStatement();
         ResultSet resultset = null;
         
         try {
             resultset = statement.executeQuery("select * from book");
             ResultSet history;
             while (resultset.next()) {
 
                 if (book.no_books == bookData.length) {
                     bookData = resizeBookArray(bookData);
                 }
                 bookData[book.no_books] = new book(resultset.getInt(1), resultset.getString(2), resultset.getString(3),
                         resultset.getInt(4), resultset.getInt(5), resultset.getInt(6), resultset.getInt(7),
                         resultset.getInt(8), resultset.getInt(9), resultset.getInt(10), resultset.getInt(11));
                 book.no_books++;
 
                 // now sync its history into RAM
 
                 id = bookData[book.no_books - 1].book_id;
                 ref = bookData[book.no_books - 1];
 
                 try {
 
                     history = statement2.executeQuery("select * from issue_history where book_id = " + id);
                     
                     while (history.next()) {
 
                         if (ref.rec_count == (ref.record.length)) {
                             
                             ref.record = (bookData[book.no_books - 1].resizeArray(ref.record));
                         }
                         
                         ref.record[(bookData[book.no_books - 1].rec_count)] = new issue_record(history.getInt(1),
                                 (history.getDate(3)), (history.getDate(4)), history.getInt(5));
                         
                         ref.rec_count++;
                         
                         //history.close();
                     }
                 } catch (Exception ep) {
                     System.out.println("No record issues found for the book title.");
                     System.out.println(ep);
                     ep.printStackTrace();
                 }
 
             }
             //resultset.close();
 
         } catch (Exception Ty) {
             Ty.printStackTrace();
             System.out.println(
                     "We ran into issues while synchronizing with the database! To avoid data corruption, this program must be closed! Please contact the developer immediately for assistance.");
             System.exit(0);
         }
 
         // --------------------------------------------------------------
         // BOOK DATA SYNCED SUCCESSFULLY!
 
         System.out.printf("\n\nData Import Completed!");
         System.out.printf("\nValue of No of Books is: %d\n\n", book.no_books);
 
         // --------------------------------------------------------------
         // MEMBER DATA START SYNCHRONIZING!
 
         System.out.println("Initiating Member Data Synchronization.");
         // We have statement <-> resultset and statement2 <-> history at our disposal
         // first of all.
         // Sync the ID Static Stack first!
 
         try {
             resultset = statement.executeQuery("select * from idstack");
             while (resultset.next()) {
                 members.idStack.push(resultset.getInt(1));
             }
         } catch (Exception yu) {
             System.out.println(yu);
         }
         System.out.println("ID Stack successfully synced!");
 
         try {
 
             members ref_mem;
             resultset = statement.executeQuery("select * from member");
             ResultSet history = null;
             ResultSet history2;
             Statement statement3 = connection.createStatement();
 
             while (resultset.next()) {
 
                 if (members.numberMembers == memData.length) {
                     memData = resizeMemberArray(memData);
                 }
                 memData[members.numberMembers] = new members(resultset.getInt(1), resultset.getLong(2),
                         resultset.getString(3),
                         resultset.getInt(4), resultset.getInt(5), resultset.getInt(6),
                         resultset.getDate(7),
                         resultset.getInt(8), resultset.getInt(9));
                 members.numberMembers++;
 
                 // now sync its history into RAM
 
                 ref_mem = memData[members.numberMembers - 1];
                 id = ref_mem.id;
 
                 try {
 
                     history = statement2.executeQuery("select * from issue_history where member_id = " + id);
 
                     while (history.next()) {
 
                         if (ref_mem.rec_count == (ref_mem.record.length)) {
                             ref_mem.record = (memData[id - 1].resizeArray(ref_mem.record));
                         }
                         
 
                         ref_mem.record[(memData[members.numberMembers - 1].rec_count)] = new issue_record(
                                 history.getInt(2),
                                 history.getDate(3), history.getDate(4), history.getInt(5));
 
                         ref_mem.rec_count++;
                         //history.close();
                     }
                 } catch (Exception epj) {
                     System.out.println("No record issues found for the book title or there were troubles.");
                 }
 
                 LocalDate end = null;
                 //Instant instant = null;
                 //ZonedDateTime zdt = null;
 
                 int reff = 0;
                 try {
 
                     history2 = statement3.executeQuery("select * from unsub_dates where MemID = " + id);
 
                     while (history2.next()) {
 
                         while (ref_mem.unsubCount >= (ref_mem.unsubDates.length)) {
                             ref_mem.resizeUnsubArray();
                         }
                         ref_mem.unsubDates[reff] = history2.getDate(2).toLocalDate();
 
                         reff++;
                         if (history2.getDate(3) != null) {
                             end = history2.getDate(3).toLocalDate();
                         }
 
                         if (end == null) {
                             // do nothing
                         } else {
                             ref_mem.unsubDates[reff] = end;
                             reff++;
                         }
 
                         //history2.close();
                     }
 
                 } catch (Exception epy) {
                     System.out.println("Troubles in loading subscription details.");
                 }
                 // now update the member's subscription dates! i.e. the unsubDates. unsubCount
                 // is already synced.
 
             }
             //resultset.close();
 
         } catch (Exception Ty) {
             Ty.printStackTrace();
             System.out.println(
                     "We ran into issues while synchronizing with the database! To avoid data corruption, this program must be closed! Please contact the developer immediately for assistance.");
             System.exit(0);
         }
 
         System.out.println("Member data succesfully imported!");
 
         // --------------------------------------------------------------
         // MEMBER DATA SYNCED SUCCESSFULLY!
         // Sync Work is complete!
 
         int iss, mem, stat;
         int xFactor = 1;		                // value must be in this set {1, 2, 3, 4, 5}
 
         while (true) {
 
             switch (driver.mainmenu(reader)) {
                 case 1:
                     boolean to_break = false;
                     if (book.no_books == bookData.length) {
                         bookData = resizeBookArray(bookData);
                     }
                     System.out.print("Please Enter the ID of the book you want to add:   ");
                     book_id = Integer.parseInt(reader.readLine());
                     for (int i = 0; i < book.no_books; i++) {
                         if (bookData[i].book_id == book_id) {
                             System.out.println("This Book ID already exists!");
                             to_break = true;
                             break;
                         }
                     }
                     if (to_break) {
                         break;
                     }
                     System.out.println();
                     bookData[book.no_books] = new book();
                     bookData[book.no_books].addNewBook(book_id, reader);
                     try {
 
                         System.out.println(
                                 "COMMAND: insert into book(book_id, name, author, quantity, issued_copies, rec_count, queue_1, queue_2, queue_3, queue_4, queue_5) values("
                                         + bookData[book.no_books - 1].book_id + ", \"" + bookData[book.no_books - 1].name
                                         + "\", \"" + bookData[book.no_books - 1].author + "\", "
                                         + bookData[book.no_books - 1].quantity + ", "
                                         + bookData[book.no_books - 1].rec_count + ", " + 0 + ", " + 0 + ", " + 0 + ", "
                                         + 0 + ", " + 0 + ", " + 0 + ")");
 
                         id = statement.executeUpdate(
                                 "insert into book(book_id, name, author, quantity, issued_copies, rec_count, queue_1, queue_2, queue_3, queue_4, queue_5) values("
                                         + bookData[book.no_books - 1].book_id + ", \"" + bookData[book.no_books - 1].name
                                         + "\", \"" + bookData[book.no_books - 1].author + "\", "
                                         + bookData[book.no_books - 1].quantity + ", "
                                         + bookData[book.no_books - 1].rec_count + ", " + 0 + ", " + 0 + ", " + 0 + ", "
                                         + 0 + ", " + 0 + ", " + 0 + ")");
 
                     } catch (Exception e) {
                         System.out.printf("\nFailed to export to the database. Reverting any changes made!");
                         book.no_books--;
                     }
                     
                     System.out.println();
                     System.out.println();
                     break;
 
                 case 2:
                     if (members.numberMembers == memData.length) {
                         memData = resizeMemberArray(memData);
                     }
                     if (!members.idStack.isEmpty()) {
                         memData[members.numberMembers] = new members();
                         int i_d = memData[members.numberMembers].addMemberStack(reader);
                         memData[i_d] = memData[members.numberMembers];
                         memData[members.numberMembers] = null;
                         if (memData[i_d].issue) {
                             iss = 1;
                         } else {
                             iss = 0;
                         }
                         if (memData[i_d].membership) {
                             mem = 1;
                         } else {
                             mem = 0;
                         }
                         if (memData[i_d].status) {
                             stat = 1;
                         } else {
                             stat = 0;
                         }
                         try {
 
                             System.out.println(
                                     "COMMAND: delete from member where id = " + (i_d + 1)
                                             + " AND insert into member(id, phoneNumber, name, issue, membership, status, joinDate, unsubCount, rec_count) values("
                                             + memData[i_d].id + ", '" + memData[i_d].phoneNumber
                                             + "', \"" + memData[i_d].name + "\", "
                                             + iss + ", " + mem + ", " + stat + ", '" + memData[i_d].joinDate + "', "
                                             + memData[i_d].unsubCount + ", "
                                             + memData[i_d].rec_count + ")");
                             id = statement.executeUpdate(
                                     "delete from member where id = " + (i_d + 1));
                             
                             id = statement.executeUpdate(
                                     "delete from idstack where id = " + (i_d + 1));
 
                             id = statement.executeUpdate(
                                     "insert into member(id, phoneNumber, name, issue, membership, status, joinDate, unsubCount, rec_count) values("
                                             + memData[i_d].id + ", '" + memData[i_d].phoneNumber
                                             + "', \"" + memData[i_d].name + "\", "
                                             + iss + ", " + mem + ", " + stat + ", '" + memData[i_d].joinDate + "', "
                                             + memData[i_d].unsubCount + ", "
                                             + memData[i_d].rec_count + ")");
 
                         } catch (Exception e) {
                             System.out.printf("\nFailed to export to the database. Reverting any changes made!");
                             members.numberMembers--;
                         }
                         
                         break;
                     }
                     memData[members.numberMembers] = new members();
                     memData[members.numberMembers].addMember(reader);
                     if (memData[members.numberMembers - 1].issue) {
                         iss = 1;
                     } else {
                         iss = 0;
                     }
                     if (memData[members.numberMembers - 1].membership) {
                         mem = 1;
                     } else {
                         mem = 0;
                     }
                     if (memData[members.numberMembers - 1].status) {
                         stat = 1;
                     } else {
                         stat = 0;
                     }
                     try {
 
                         System.out.println(
                                 "COMMAND: insert into member(id, phoneNumber, name, issue, membership, status, joinDate, unsubCount, rec_count) values("
                                         + memData[members.numberMembers - 1].id + ", '"
                                         + memData[members.numberMembers - 1].phoneNumber
                                         + "', \"" + memData[members.numberMembers - 1].name + "\", "
                                         + iss + ", " + mem + ", " + stat + ", '"
                                         + memData[members.numberMembers - 1].joinDate + "', "
                                         + memData[members.numberMembers - 1].unsubCount + ", "
                                         + memData[members.numberMembers - 1].rec_count + ")");
 
                         id = statement.executeUpdate(
                                 "insert into member(id, phoneNumber, name, issue, membership, status, joinDate, unsubCount, rec_count) values("
                                         + memData[members.numberMembers - 1].id + ", '"
                                         + memData[members.numberMembers - 1].phoneNumber
                                         + "', \"" + memData[members.numberMembers - 1].name + "\", "
                                         + iss + ", " + mem + ", " + stat + ", '"
                                         + memData[members.numberMembers - 1].joinDate + "', "
                                         + memData[members.numberMembers - 1].unsubCount + ", "
                                         + memData[members.numberMembers - 1].rec_count + ")");
 
                     } catch (Exception e) {
                         System.out.printf("\nFailed to export to the database. Reverting any changes made!");
                         members.numberMembers--;
                     }
                     System.out.println();
                     System.out.println();
                     
                     break;
                 // all synced up till now! but remember that statement and sql connection are
                 // limited to main() till now.
 
                 case 3:
                     System.out.print("Please enter the ID of the member who wishes to issue a book:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     if (!memData[member_id].membership) {
                         System.out.println(
                                 "This user is not on an active membership so can not issue books right now. Resubscribe first!");
                         break;
                     }
 
                     if (memData[member_id].issue) {
                         System.out.println(
                                 "This member has not returned his previously issued book yet. Currently issued BOOK ID:    "
                                         + memData[member_id].record[((memData[member_id].rec_count) - 1)].book_id);
                         break;
                     }
 
                     System.out.print("Please enter the ID of the book you wish to issue:    ");
                     book_id = (Integer.parseInt(reader.readLine()));
                     for (int i = 0; i < book.no_books; i++) {
                         if (book_id == bookData[i].book_id) {
                             // check if it is in stock first. ADD WHEN IS THE BOOK COMING!
                             if (bookData[i].issued_copies == bookData[i].quantity) {
                                 System.out.println("All copies of this book are issued at this moment!");
                                 bookData[i].retDate();
                                 if (bookData[i].idQueue.size() == 5) {
                                     System.out.println(
                                             "Queue for this book (= 5) is also full so you could not be placed in waiting list.");
                                     break;
                                 }
                                 System.out.printf(
                                         "Would you like to queue yourself in the waiting list for this book?\nNO. OF PEOPLE IN QUEUE: %d\nWhen it returns, it'll be automatically be issued to you whenever you're first in queue. [y/n]:    ",
                                         bookData[i].idQueue.size());
                                 String todo = reader.readLine();
 
                                 if (todo.charAt(0) == 'y') 
                                 {
                                     resultset = statement.executeQuery("select queue_1, queue_2, queue_3, queue_4, queue_5 from book where book_id = " + book_id);
                                     resultset.next();
                                     for(int iy = 1; iy <= 5; iy++)
                                     {
                                         if(resultset.getInt(iy) == 0)     // empty slot is identified in DB.
                                         {
                                             xFactor = iy;
                                             break;
                                         }
                                     }
                                     try {
                                         
                                         System.out.println(
                                             "COMMAND: update book set queue_" + xFactor + " = " + (member_id+1) + " where book_id = " + book_id);
                                             
                                             id = statement.executeUpdate(
                                                 "update book set queue_" + xFactor + " = " + (member_id+1) + " where book_id = " + book_id);
                                                 
                                             } catch (Exception ebisu) {
                                                 System.out.printf("\nFailed to export to the database. Reverting any changes made!");
                                                 ebisu.printStackTrace();
                                                 
                                                 System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST REVERTED x001.");
                                                 break;
                                             }
                                             bookData[i].idQueue.add(member_id + 1);
                                             System.out.println("Successfully placed in Waiting List. Waiting Number: " + bookData[i].idQueue.size());
                                 }
                                 
                                 break;
                             } else {
                                 // Initiate issue request.
                                 d = memData[member_id].issueBook(book_id, statement, resultset);
                                 bookData[i].issue(memData[member_id].id, d);
                                 
                                 System.out.println("Issued Successfully!");
                                 break;
                             }
 
                         } else if (i == book.no_books - 1) {
                             System.out.println("THIS BOOK DOES NOT EXIST! Issue request failed.");
                             break;
                         }
                     }
                     System.out.println();
                     System.out.println();
                     break;
 
                     // db done
 
                 case 4:
                     System.out.print("Please enter the ID of the member whose issue history you wish to see:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     memData[member_id].printHistory();
                     System.out.println();
                     System.out.println();
                     break;
 
                 case 5:
                     System.out.print("Please enter the ID of the book whose issue history you wish to see:    ");
                     book_id = (Integer.parseInt(reader.readLine()));
                     for (int i = 0; i < book.no_books; i++) {
                         if (bookData[i].book_id == book_id) {
                             bookData[i].printHistory();
                             break;
                         }
                         if (i == book.no_books - 1) {
                             System.out.println("INVALID BOOK ID.");
                         }
                     }
 
                     System.out.println();
                     break;
 
                 case 6:
                     System.out.print("Please enter the ID of the member whose bill you want to generate:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     memData[member_id].print_bill(reader);
                     System.out.println();
                     System.out.println();
                     break;
 
                 case 7:
                     // Returning a book. DB updates needed
                     System.out.print("Please enter the ID of the member who wishes to return a book:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     if (!memData[member_id].issue) {
                         System.out.println("This member has returned all of this books already. No backlog exists.");
                         break;
                     }
 
                     System.out.print("Please enter the ID of the book you being returned to the catalog:    ");
                     book_id = (Integer.parseInt(reader.readLine()));
 
                     if (memData[member_id].record[(memData[member_id].rec_count) - 1].book_id == book_id) {
                         d = memData[member_id].retBook(statement);
                         
                         for (int i = 0; i < book.no_books; i++) {
                             if (book_id == bookData[i].book_id) {
                                 bookData[i].ret(d, memData[member_id].id);
                                 System.out.println("book record updated!");
                                 System.out.printf("Checking Queue\n");
                                 // Check Queue! -- do sql here
                                 String todo = "nooo"; // pooled string
                                 while (todo.charAt(0) != 'y' && !bookData[i].idQueue.isEmpty()) {
                                     int i_d = bookData[i].idQueue.peek(); // problematic
                                     System.out.printf(
                                             "%d Member-ID is currently on queue to issue this book. Would you like to issue it? [y/n]:   ",
                                             bookData[i].idQueue.remove());
                                             try {
                                         
                                                System.out.println(
                                                    // update book set queue_1 = queue_2 where book_id = ?
                                                    "COMMAND: update book set queue_1 = queue_2 where book_id = " + book_id);
                                                    
                                                    id = statement.executeUpdate(
                                                        "update book set queue_1 = queue_2 where book_id = " + book_id);
                                                    id = statement.executeUpdate(
                                                        "update book set queue_2 = queue_3 where book_id = " + book_id);
                                                    id = statement.executeUpdate(
                                                        "update book set queue_3 = queue_4 where book_id = " + book_id);
                                                    id = statement.executeUpdate(
                                                        "update book set queue_4 = queue_5 where book_id = " + book_id);
                                                    id = statement.executeUpdate(
                                                        "update book set queue_5 = 0 where book_id = " + book_id);
                                                        
                                            } 
                                            catch (Exception e2) 
                                            {
                                                        System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                                                        e2.printStackTrace();
                                                        
                                                        System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                                            }
                                     todo = reader.readLine();
 
                                     if (todo.charAt(0) == 'y') {
                                         if (memData[i_d - 1].issue) {
                                             System.out.println("THIS USER IS ALREADY ON AN ISSUE!");
                                             continue;
                                         }
                                         // SQL Update is required!
                                         d = memData[i_d - 1].issueBook(book_id, statement, resultset);
                                         bookData[i].issue(i_d, d);
                                     }
                                     // reader = new BufferedReader(new InputStreamReader(System.in));
                                 }
                                 break;
                             }
                         }
 
                     } else {
                         System.out.println("This book was NOT issued by the member.");
                     }
 
                     System.out.println();
                     System.out.println();
                     
                     break;
 
                     // db done
 
                 case 8:
                     System.out.printf(
                             "Displaying Registered Users: \n\nUSER ID\t\tUSER NAME\t\tPHONE NUMBER\t\tDATE OF JOINING\t\tISSUE STATUS\n");
                     for (int i = 0; i < members.numberMembers; i++) {
                         if (memData[i].status) {
                             if (memData[i].issue) {
                                 System.out.printf("%d\t\t%s\t\t%d\t\t%s \t\tON-ISSUE \n", memData[i].id,
                                         memData[i].name, memData[i].phoneNumber, memData[i].joinDate);
                             } else {
                                 System.out.printf("%d\t\t%s\t\t%d\t\t%s \t\tNO-ISSUE \n", memData[i].id,
                                         memData[i].name, memData[i].phoneNumber, memData[i].joinDate);
                             }
                         }
                     }
                     System.out.println();
                     System.out.println(
                             "Displaying Registered Books: \n\nBOOK ID \tBOOK NAME\t\t\tBOOK AUTHOR\t\t QUANTITY \t NO. OF AVAILABLE BOOKS");
                     for (int i = 0; i < book.no_books; i++) {
                         System.out.printf("%d \t\t%s\t\t\t%s\t\t %d \t\t %d \n", bookData[i].book_id, bookData[i].name,
                                 bookData[i].author, bookData[i].quantity,
                                 (bookData[i].quantity - bookData[i].issued_copies));
                     }
                     System.out.println();
                     System.out.println();
                     break;
 
                 case 9:
                     System.out.println("Exiting the program...");
                     connection.close();
                     System.exit(0);
 
                 case 10:
                     System.out.print("Please enter the ID of the book whose details you wish to alter:    ");
                     book_id = (Integer.parseInt(reader.readLine()));
                     for (int i = 0; i < book.no_books; i++) {
                         if (book_id == bookData[i].book_id) {
                             bookData[i].modify(reader, statement);
                             break;
 
                         } else if (i == book.no_books - 1) {
                             System.out.println("THIS BOOK DOES NOT EXIST! Request failed.");
                             break;
                         }
                     }
                     
                     break;
 
                 case 11:
                     System.out.print("Please enter the ID of the member whose info you want to alter:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     memData[member_id].modify(reader, statement);
                     
                     break;
 
                 // SQL done
 
                 case 12:
                     System.out.print("Please enter the ID of the member whose account you want to terminate:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (memData[member_id].issue) {
                         System.out.println("Return your issued book first!");
                         break;
                     }
                     memData[member_id].terminateAccount();
                     try {
                                         
                         System.out.println(
                             // update book set name = ? WHERE book_id = ?
                             "COMMAND: update member set status = 0 WHERE id = " + (member_id+1));
                             
                             int res = statement.executeUpdate(
                                 "update member set status = 0 WHERE id = " + (member_id));
 
                         System.out.println(
                                     // update book set name = ? WHERE book_id = ?
                             "COMMAND: delete from issue_history where member_id = " + (member_id+1));
                                     
                         id = statement.executeUpdate(
                                 "delete from issue_history where member_id = " + (member_id+1));
                         
                         System.out.println(
                                 // update book set name = ? WHERE book_id = ?
                         "COMMAND: insert into idstack(id) values(" + (member_id+1) + ")");
                                 
                     id = statement.executeUpdate(
                             "insert into idstack(id) values(" + (member_id+1) + ")");
                                 
                     } 
                     catch (Exception e1) 
                     {
                                 System.out.printf("\nFailed to export to the database. Reverting any changes made! \nFailed to revert. PLEASE manually update the database with above command.\n");
                                 e1.printStackTrace();
                                 
                                 System.out.println("We were unable to queue you due to an update anomaly in the database. REQUEST NOT REVERTED x001.");
                     }
                     for (int i = 0; i < book.no_books; i++) {
                         bookData[i].updateRecs(member_id + 1);
                     }
                     
                     System.out.println("Successfully terminated!");
                     // archive.addMRec(memData[member_id]);
                     System.out.println("Successfully pushed to Archive and updated Book Issue History Logs!");
                     // memData[member_id] = null;
                     break;
 
                 case 13:
                     System.out.print("Please enter the ID of the member which you want to unsubscribe:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
 
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     memData[member_id].unsubscribe(reader, statement);
                     
                     break;
 
                 case 14:
                     System.out.print("Please enter the ID of the member which you want to resubscribe:    ");
                     member_id = (Integer.parseInt(reader.readLine())) - 1;
                     if (member_id >= members.numberMembers) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
                     if (!memData[member_id].status) {
                         System.out.println("This Member ID does not exist.");
                         break;
                     }
 
                     memData[member_id].resubscribe(reader, statement);
                     
                     break;
 
 
                     // DB Updates done!
                 case 15:
                     // archive.printDelRecords();
                     break;
 
                 case 16:
                     // archive.flushRecs();
                     break;
 
                 case 17:
                     System.out.print(
                             "Please enter '1' if you would like to search for a book using name and '2' if using author name (This feature is CASE sensitive):    ");
                     member_id = (Integer.parseInt(reader.readLine()));
                     String nm = "";
                     if (member_id == 1) {
                         System.out.print("Please enter the search keyword [BOOK NAME]:    ");
                         nm = reader.readLine();
                         System.out.print("\nSEARCH RESULTS: \n\n");
                         System.out.print("BOOK ID\t\t\tBOOK NAME\t\tAUTHOR NAME\t\tQUANTITY\t\tNO. OF ISSUED COPIES");
                         for (int i = 0; i < book.no_books; i++) {
                             bookData[i].searchName(nm);
                         }
                     } else if (member_id == 2) {
                         System.out.print("Please enter the search keyword [AUTHOR NAME]:    ");
                         nm = reader.readLine();
                         System.out.print("\nSEARCH RESULTS: \n\n");
                         System.out.print("BOOK ID\t\t\tBOOK NAME\t\tAUTHOR NAME\t\tQUANTITY\t\tNO. OF ISSUED COPIES");
                         for (int i = 0; i < book.no_books; i++) {
                             bookData[i].searchAuthor(nm);
                         }
                     } else {
                         System.out.println("Invalid Input.");
                     }
                     break;
 
                 case 18:
                     System.out.print("Please enter the ID of the book whose stock you wish to see:  ");
                     book_id = (Integer.parseInt(reader.readLine()));
                     for (int i = 0; i < book.no_books; i++) {
                         if (bookData[i].book_id == book_id) {
                             bookData[i].addStock(reader, statement);
                             break;
                         }
                         if (i == book.no_books - 1) {
                             System.out.println("INVALID BOOK ID.");
                         }
                     }
                     
 
                     System.out.println();
                     break;
 
                 case 19:
                     System.out.printf(
                             "[PROGRAM DEVELOPMENT UPDATE NOTICE]%n%nBy: The Library Developer (Serzax Lucifer)%n%nHey guys!%nWe're excited to announce that we're actively developing this program further and plan to introduce so many things soon. We'll be releasing a massive update called 'Library 2.0' scheduled for release in January 2023 with exact release data to be confirmed. Stay tuned for more on the release ~%n%nThe Library 2.0 program's development update is planned to included(*)%n%n1.) JDBC-ODBC Driver Support: mySQL Integration -- Your data will not be lost when you exit the program. [Networking Support to be introduced in mid-2023].%n2.) Library 2.0 will be available in both formats -- Terminal View and a New GUI-based application that can be run from its .exe file with aesthetic designs and interfaces!%n3.) The processing of dates, input of dates will be automatic and will not need to be entered manually. We've revamped from scratch how date storage and processing works in Lib2.0 to allow automatic date and time capturing and much faster date operations' performance.%n4.) Library 2.0 will be properly exception handled. It will not CRASH during runtime unless something fatal happens internally. External exceptions will be taken care of properly. %n5.) Archived Records will become restorable.%n6.) In late 2023, Email Integration, Advanced APIs are planned to be included.%n%n(*)Subject to change without notice.%n%nThank you for your support all along! We hope you're excited about this news.");
                     System.out.println();
                     break;
 
                 default:
                     System.out.printf("%n%nPlease Enter A Valid Command Number [1-19].%n%n");
                     break;
             }
         }
 
     }
 
 }
 