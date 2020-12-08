import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCLibrary {

    static Connection conn;
    static final Logger logger = LoggerFactory.getLogger(JDBCLibrary.class);
    static Scanner reader = new Scanner(System.in);
    static String pattern = "\\p{javaWhitespace}*,\\p{javaWhitespace}*";
    static String skipPattern = "\\p{javaWhitespace}*";

    public static void main(String[] args) {
        String userID = System.getenv().get("USERNAME");
        userID = userID == null ? "jdbc" : userID;
        String userPassword = null;
        for (int i = 0; i < args.length; i++) {
            if ("-u".equals(args[i])) {
                if (i != args.length - 1) {
                    userID = args[i + 1];
                }
            }
            if ("-p".equals(args[i])) {
                Console console = System.console();
                if (console == null) {
                    logger.warn("Unable to get console handle, try running outside an IDE");
                    System.out.println("We're forced to use no mask on your password, sorry.");
                    System.out.print("Please input your password: ");
                    userPassword = reader.next();
                } else {
                    System.out.print("Please input your password: ");
                    userPassword = new String(console.readPassword());
                }
            }
        }
        start(userID, userPassword);
    }

    /**
     * Connect to mysql with userID and userPassword
     *
     * @param userID       the user's id as String
     * @param userPassword the user's userPassword as String
     */
    static void start(String userID, String userPassword) {
        // Might be SQLException, so we try
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Need this for connection with MySQL
            String connectString = "jdbc:mysql://localhost:3306/JDBCLibrary?characterEncoding=utf8&useSSL=true"
                    + "&serverTimezone=" + ZoneId.systemDefault();
//            String connectString = "jdbc:mysql://localhost:3306/JDBCLibrary?characterEncoding=utf8&useSSL=true";
            conn = DriverManager.getConnection(connectString, userID, userPassword);
            System.out.println("Welcome to the JDBCLibrary.");
            System.out.println("Choose your operation please.");
            int choice;
            while (true) {
                try {
                    choice = getChoice("""
                            1. Query books
                            2. Borrow books
                            3. Return books
                            4. Add book to stock
                            5. Add books to stock
                            6. Add books from file
                            7. Manage reader's proof
                            8. Print create tables
                            0. Exit the system""");
                } catch (InputMismatchException e) {
                    continue;
                }
                switch (choice) {
                    case 0 -> {
                        conn.close();
                        return;
                    }
                    case 1 -> checkBook();
                    case 2 -> borrowBook();
                    case 3 -> returnBook();
                    case 4 -> addBook();
                    case 5 -> addBooks();
                    case 7 -> manageProof();
                    case 8 -> showTable();
                    case 6 -> addBooksFromFile();
                    default -> logger.warn("Wrong service code: {}", choice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A thin wrapper around executePrepQuery. Everything would be converted to a
     * string to be returned. The query result will be printed to the console
     *
     * @param query the query to be executed
     * @return the result array of the query
     */
    static ArrayList<String> executePrepQuery(String query) {
        try {
            PreparedStatement pStmt = conn.prepareStatement(query);
            return executePrepQuery(pStmt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Wrapper around the executePrepQuery function
     *
     * @param query  the query to be executed (already fixed)
     * @param update whether we're trying to perform update
     * @return the result array list
     */
    static ArrayList<String> executePrepQuery(String query, boolean update) {
        try {
            PreparedStatement pStmt = conn.prepareStatement(query);
            return executePrepQuery(pStmt, update);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    static ArrayList<String> executePrepQuery(PreparedStatement pStmt) {
        return executePrepQuery(pStmt, false);
    }

    /**
     * Print the result set provided in an elegant way Useful when the result set is
     * already generated
     *
     * @param rSet the result set to be printed
     * @throws SQLException throws SQLException if error occurs when extracting
     *                      information from the rSet
     */
    static void printResultSet(ResultSet rSet) throws SQLException {
        final int widthMore = 2; // The width of a column - the width of the widest element
        int colWSum = 0; // Sum of all column widths
        String colV; // The value to be dealt with
        ResultSetMetaData rsmd = rSet.getMetaData();
        int colN = rsmd.getColumnCount();
        ArrayList<Integer> colW = new ArrayList<>(colN + 1); // The first element is always zero

        // Initialization
        for (int i = 0; i <= colN; i++) {
            colW.add(0);
        }

        // Reset result set cursor position
        rSet.beforeFirst();
        // Compute the maximum width from the elements
        while (rSet.next()) {
            for (int i = 1; i <= colN; i++) {
                colV = rSet.getString(i);
                if (colV.length() > colW.get(i))
                    colW.set(i, colV.length());
            }
        }

        // Compute the final width (with correction and comparison to column name)
        for (int i = 1; i <= colN; i++) {
            colV = rsmd.getColumnName(i);
            if (colV.length() > colW.get(i))
                colW.set(i, colV.length());
            colW.set(i, colW.get(i) + widthMore);
        }

        // Compute sum of all width
        for (Integer width : colW) {
            colWSum += width;
        }

        printLine('-', colWSum);

        // Print column names
        for (int i = 1; i <= colN; i++) {
            System.out.printf("%" + colW.get(i) + "s", rsmd.getColumnName(i));
        }
        System.out.println();

        printLine('-', colWSum);

        // Reset result set cursor position
        rSet.beforeFirst();

        // Print fixed width column value
        while (rSet.next()) {
            for (int i = 1; i <= colN; i++) {
                colV = rSet.getString(i);
                System.out.printf("%" + colW.get(i) + "s", colV);
            }
            System.out.println();
        }

        printLine('-', colWSum);
    }

    /**
     * Print formatted query result to the console (width adapted). For example:
     * varchar(2000) and max width 20 -> column width equals max width + correction
     * (default 2 chars). Everything would be converted to a string to be returned.
     * The query result will be printed to the console.
     *
     * @param pStmt the prepared statement to be executed. Must already been
     *              prepared.
     * @return the result array of the query
     */
    static ArrayList<String> executePrepQuery(PreparedStatement pStmt, boolean update) {
        ArrayList<String> results = new ArrayList<>(); // For storing the results
        ResultSet rSet; // A resultSet
        try {
            if (update) {
                int affectedN = 0;
                try {
                    affectedN = pStmt.executeUpdate();
                } catch (SQLException e) {
                    rSet = pStmt.executeQuery();
                    if (rSet.next()) {
                        results.add("1");
                    }
                }
                results.add(Integer.toString(affectedN));
                return results;
            }
            rSet = pStmt.executeQuery();
            int colN = rSet.getMetaData().getColumnCount();
            while (rSet.next()) {
                for (int i = 0; i < colN; i++) {
                    results.add(rSet.getString(i + 1));
                }
            }
            printResultSet(rSet);
        } catch (SQLException e) {
            logger.warn("Unable to perform the following query {}", pStmt);
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Print a line of ch, total number is count
     *
     * @param ch    the character to be printed as line constructor
     * @param count the number of character to print on screen
     */
    static void printLine(char ch, int count) {
        for (int i = 0; i < count; i++) {
            System.out.print(ch);
        }
        System.out.println();
    }

    /**
     * Prompted choice function
     * Prompted when mismatched throws IO exception if
     * mismatched, however the exception is handled
     *
     * @param prompt the prompt that contains explanation of the choices
     * @return the choice we need and output prompt
     * @throws InputMismatchException used as a kind of return value
     */
    static int getChoice(String prompt) throws InputMismatchException {
        String str;
        int choice;
        System.out.println(prompt);
        System.out.print("Please input the service code: ");
        try {
            choice = reader.nextInt();
        } catch (InputMismatchException e) {
            str = reader.nextLine();
            logger.warn("Unable to interpret \"{}\" as a service code", str);
            throw new InputMismatchException(); // The caller use the exception to adjust execution procedure
        }
        return choice;
    }

    /**
     * Check the books executing queries
     */
    static void checkBook() {
        int choice;// Stores the user's selection

        while (true) { // Event loop
            try { // Get a choice, if mismatch continue
                choice = getChoice("""
                        1. Query all books
                        2. Query by book name
                        3. Query by book number
                        4. Query by category
                        5. Query by press
                        6. Query by author
                        7. Query by year range
                        8. Query by price range
                        0. Return (q. Return when querying)""");
            } catch (InputMismatchException e) {
                continue;
            }
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> {
                    String query = "SELECT * FROM book";
                    if ("0".equals(executePrepQuery(query, true).get(0))) { // Returns an array whose first element is
                        // zero if nothing is found
                        System.out.println("Cannot find any book");
                    } else {
                        executePrepQuery(query); // Print the whole book table
                    }

                }
                // The prompt and query and error message is passed as parameters
                case 2 -> checkBookByString("Please input the name of the book you want to query: ",
                        "select * from book where title=?", "Cannot find any book named: \"%s\"");
                case 3 -> checkBookByString("Please input the book number (length: 10) of the book you want to query: ",
                        "select * from book where bno=?", "Cannot find any book of book number: \"%s\"");
                case 4 -> checkBookByString("Please input the category of the book you want to query: ",
                        "select * from book where category=?", "Cannot find any book of category: \"%s\"");
                case 5 -> checkBookByString("Please input the press of the book you want to query: ",
                        "select * from book where press=?", "Cannot find any book published by press: \"%s\"");
                case 6 -> checkBookByString("Please input the author of the book you want to query: ",
                        "select * from book where author=?", "Cannot find any book written by author: \"%s\"");
                case 7 -> checkBookByRange("Please input the range of the year you want to query (separated by space): ",
                        "select * from book where year between ? and ?", "Cannot find any book of year range: %d to %d",
                        false);
                case 8 -> checkBookByRange("Please input the range of the price of the book you want to query (separated by space): ",
                        "select * from book where price between ? and ?",
                        "Cannot find any book of price range: %f to %f", true);
                default -> logger.warn("Wrong service code: {}", choice);
            }
        }
    }

    /**
     * Check the books by a range and a specified query + prompt
     *
     * @param promptIn  prompt of this range selection
     * @param sqlQuery  the query that contains two ?
     * @param promptErr what to say when error occur, should have corresponding type
     *                  of range
     * @param isDouble  whether the range in represented in integer or double
     *                  precision float
     */
    static void checkBookByRange(String promptIn, String sqlQuery, String promptErr, boolean isDouble) {
        System.out.print(promptIn);
        reader.skip(skipPattern); // Skip unwanted pattern (newline or space)
        String str;
        Object left; // Might be int or double
        Object right; // Might be int or double
        while (true) {
            try {
                if (isDouble) { // Get input we need
                    left = reader.nextDouble();
                    right = reader.nextDouble();
                } else {
                    left = reader.nextInt();
                    right = reader.nextInt();
                }
                break;
            } catch (InputMismatchException e) { // Mismatch, continue loop, let user try again
                str = reader.nextLine();
                if ("q".equals(str)) // If we've got "q", then quit this check
                    return;
                logger.warn("Unable to interpret \"{}\" as an integer range", str);
                System.out.print("Wrong format as int. Try again: ");
            } catch (Exception e) {
                e.printStackTrace(); // Unable to handle
            }
        }
        try {
            // Execute query
            PreparedStatement pStmt = conn.prepareStatement(sqlQuery);
            pStmt.setObject(1, left);
            pStmt.setObject(2, right);
            logger.debug("The range read in is: {} to {}", left, right);
            ResultSet rSet = pStmt.executeQuery(); // execute the query to check whether the required book exists

            // Nothing exists
            if (!rSet.next()) {
                System.out.printf(promptErr + "\n", left, right);
                return;
            }
            printResultSet(rSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check the books by a string value.
     *
     * @param promptIn  prompt of this range selection
     * @param sqlQuery  the query that contains two ?
     * @param promptErr what to say when error occur, should have corresponding type
     *                  of string
     */
    static void checkBookByString(String promptIn, String sqlQuery, String promptErr) {
        System.out.print(promptIn);
        reader.skip(skipPattern); // Skip unwanted newline or space (we've test that calling on empty string would
        // result in input being eaten)
        String str = reader.nextLine(); // All string until next line will be processed
        if ("q".equals(str)) // Quit on entering "q"
            return;
        try {
            // Execute query
            PreparedStatement pStmt = conn.prepareStatement(sqlQuery);
            pStmt.setString(1, str);
            logger.debug("The value of the read string is: \"{}\"", str);
            ResultSet rSet = pStmt.executeQuery(); // check whether the required query exists

            // Nothing exists
            if (!rSet.next()) {
                System.out.printf(promptErr + "\n", str);
                return;
            }
            printResultSet(rSet);
        } catch (Exception e) {
            e.printStackTrace(); // Unable to handle (print useful information)
        }
    }

    /**
     * Print create table information of the tables Used for debugging
     */
    static void showTable() {
        String query = "show tables";
        ArrayList<String> tables = executePrepQuery(query);

        // Print the table needed
        for (String table : tables) {
            query = "show create table " + table;
            executePrepQuery(query);
        }
    }

    /**
     * Prints the nearest return date of a book
     * If cannot locate the book, do nothing
     *
     * @param book_number bno of the book whose nearest return date is to be returned
     */
    static void printNearestReturnDate(String book_number) {
        try {
            PreparedStatement pStmt = conn.prepareStatement("select return_date from borrow where bno=?");
            pStmt.setString(1, book_number);
            try {
                ResultSet rSet = pStmt.executeQuery();
                LocalDate return_date;
                LocalDate nearest = null;
                LocalDate now = LocalDate.now(ZoneId.systemDefault());
                // If query is unable to be executed, both rSet.next() will return false
                if (rSet.next()) {
                    nearest = rSet.getDate(1).toLocalDate();
                }
                while (rSet.next()) {
                    return_date = rSet.getDate(1).toLocalDate();
                    assert nearest != null;
                    if (return_date.isBefore(nearest)) {
                        nearest = return_date;
                    }
                }
                // Do nothing is book is unable to be located
                if (nearest != null) {
                    System.out.println("The nearest return date is: " + nearest);
                    System.out.println("Which is " + now.until(nearest, ChronoUnit.DAYS) + " days from now");
                }
            } catch (SQLException e) {
                // Unable to query (error encountered when doing query)
                logger.warn("Unable to perform query {}", pStmt);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            // Unable to prepare
            System.out.println("Unable to prepare the statement");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Borrow a book
     * The prompt is quite self explaining
     */
    static void borrowBook() {
        PreparedStatement pStmt = null; // this variable is needed when logging exception information, defined out of
        // try
        try {
            System.out.print("Please input your reader's proof number: ");
            String card_number = reader.next();
            if ("q".equals(card_number)) {
                return;
            }
            // Check proof existence, return if not
            pStmt = conn.prepareStatement("select * from card where cno=?");
            pStmt.setString(1, card_number);
            ResultSet rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("Unable to find the proof specified by card number: %s\n", card_number);
                return;
            }
            printResultSet(rSet);

            // Check borrow record
            pStmt = conn.prepareStatement("select * from borrow where cno=?");
            pStmt.setString(1, card_number);
            rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("You haven't borrowed any book as : %s\n", card_number);
            } else {
                printResultSet(rSet);
            }

            // Get the book number needed
            System.out.print("Please input the book number of your desired book: ");
            String book_number = reader.next();
            if ("q".equals(book_number)) {
                return;
            }

            // Check borrow record, return if already borrowed
            pStmt = conn.prepareStatement("select * from borrow where cno=? and bno=?");
            pStmt.setString(1, card_number);
            pStmt.setString(2, book_number);
            rSet = pStmt.executeQuery();
            if (rSet.next()) {
                System.out.printf("You've already borrowed book: %s as: %s\n", card_number, book_number);
                return;
            }

            // Check book information, return if out of stock
            pStmt = conn.prepareStatement("select * from book where bno=?");
            pStmt.setString(1, book_number);
            rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("Unable to find the book specified by book number: %s\n", book_number);
                return;
            } else if (rSet.getInt("stock") <= 0) {
                System.out.println("The book is out of stock, please return later");
                printNearestReturnDate(book_number);
                return;
            }
            printResultSet(rSet);

            // make sure...
            while (true) {
                System.out.print("Are you sure you want to borrow this book (Y/N)? ");
                String str = reader.next();
                switch (str.charAt(0)) {
                    case 'N':
                    case 'n':
                    case 'Y':
                    case 'y':
                        break;
                    default:
                        continue;
                }
                break;
            }

            // Change borrow duration if needed
            int duration = 14;// in days
            // reader.skip(skipPattern);
            while (true) {
                System.out
                        .print("Note that the default return date is 2 weeks later, do you want to change it (Y/N)? ");
                String str = reader.next();
                switch (str.charAt(0)) {
                    case 'n':
                    case 'N':
                        break;
                    case 'Y':
                    case 'y':
                        System.out.print("Please input your desired borrow duration in day: ");
                        try {
                            duration = reader.nextInt();
                            if (duration <= 0) {
                                System.out.println("Cannot accept negative value, try again");
                                continue;
                            }
                            System.out.printf("You've changed your borrowing duration to: %d day(s)\n", duration);
                        } catch (InputMismatchException e) {
                            str = reader.nextLine();
                            logger.warn("Unable to parse int from: {}", str);
                            System.out.println("Illegal input, try again");
                            continue;
                        }
                        break;
                    default:
                        continue;
                }
                break;
            }

            // Compute borrow date
            LocalDate borrow_date = LocalDate.now(ZoneId.systemDefault());
            LocalDate return_date = borrow_date.plusDays(duration);
            logger.info("Default timezone is {}", ZoneId.systemDefault());
            logger.info("Time in default time zone is {}", borrow_date);

            // execute transaction query
            conn.setAutoCommit(false);
            pStmt = conn.prepareStatement("update book set stock=stock-1 where bno=?");
            pStmt.setString(1, book_number);
            pStmt.executeUpdate();
            pStmt = conn.prepareStatement("insert into borrow values (?,?,?,?)");
            pStmt.setString(1, card_number);
            pStmt.setString(2, book_number);
            pStmt.setDate(3, Date.valueOf(borrow_date));
            pStmt.setDate(4, Date.valueOf(return_date));
            pStmt.executeUpdate();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("The books is borrowed");

            // Print borrow information
            pStmt = conn.prepareStatement("select * from borrow where cno=? and bno=?");
            pStmt.setString(1, card_number);
            pStmt.setString(2, book_number);
            executePrepQuery(pStmt);

            // Print updated book information
            pStmt = conn.prepareStatement("select * from book where bno=?");
            pStmt.setString(1, book_number);
            executePrepQuery(pStmt);
        } catch (Exception e) {
            logger.warn("Unable to perform the query: {}", pStmt);
            e.printStackTrace();
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback(); // If no error, do nothing, if exception, rollback
                }
                conn.setAutoCommit(true);
            } catch (Exception ein) {
                ein.printStackTrace();
            }
        }
    }

    /**
     * Return a book, prompted.
     */
    static void returnBook() {
        PreparedStatement pStmt = null; // Needed in catch statement, so defined outside
        try {
            // Get reader's proof
            System.out.print("Please input your reader's proof number: ");
            String card_number = reader.next();
            if ("q".equals(card_number)) {
                return;
            }

            // Check proof
            pStmt = conn.prepareStatement("select * from card where cno=?");
            pStmt.setString(1, card_number);
            ResultSet rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("Unable to find the proof specified by card number: %s\n", card_number);
                return;
            }
            printResultSet(rSet);

            // Check borrow information
            pStmt = conn.prepareStatement("select * from borrow where cno=?");
            pStmt.setString(1, card_number);
            rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("You haven't borrowed any book as : %s\n", card_number);
                return;
            } else {
                printResultSet(rSet);
            }

            // Get book number
            System.out.print("Please input the book number of the book to be returned: ");
            String book_number = reader.next();
            if ("q".equals(book_number)) {
                return;
            }

            // Check borrow information again
            pStmt = conn.prepareStatement("select * from borrow where cno=? and bno=?");
            pStmt.setString(1, card_number);
            pStmt.setString(2, book_number);
            rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("You haven't borrowed book: %s as: %s\n", card_number, book_number);
                return;
            }

            // Check book information
            pStmt = conn.prepareStatement("select * from book where bno=?");
            pStmt.setString(1, book_number);
            rSet = pStmt.executeQuery();
            if (!rSet.next()) {
                System.out.printf("Unable to find the book specified by book number: %s\n", book_number);
                return;
            } else if (rSet.getInt("stock") <= 0) {
                System.out.println("The book is out of stock, do return it if possible");
            }
            printResultSet(rSet);

            // Make sure ...
            while (true) {
                System.out.print("Are you sure you want to return this book (Y/N)? ");
                String str = reader.next();
                switch (str.charAt(0)) {
                    case 'N':
                    case 'n':
                        return;
                    case 'Y':
                    case 'y':
                        break;
                    default:
                        continue;
                }
                break;
            }

            // Execute transaction
            conn.setAutoCommit(false);
            pStmt = conn.prepareStatement("update book set stock=stock+1 where bno=?");
            pStmt.setString(1, book_number);
            pStmt.executeUpdate();
            pStmt = conn.prepareStatement("delete from borrow where cno=? and bno=?");
            pStmt.setString(1, card_number);
            pStmt.setString(2, book_number);
            pStmt.executeUpdate();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("The books is returned");

            // Print book information again
            pStmt = conn.prepareStatement("select * from book where bno=?");
            pStmt.setString(1, book_number);
            executePrepQuery(pStmt);
        } catch (Exception e) {
            logger.warn("Unable to perform the query: {}", pStmt);
            e.printStackTrace();
            try {
                if (!conn.getAutoCommit()) {
                    conn.rollback(); // If no error, do nothing, if exception, rollback
                }
                conn.setAutoCommit(true);
            } catch (Exception ein) {
                ein.printStackTrace();
            }
        }
    }

    /**
     * Accept one book in the database
     * Update with confirmation if primary key is duplicated
     */
    static void addBook() {
        System.out.println("""
                Please input the
                1. book number 2. category 3. title 4. press 5. year 6. author 7. price 8. total 9. stock
                of the book (existing books will be updated (by book number))
                (input q to quit) (separate by comma): """);
        // 0000000013, English, English Total, Ali, 1995, Bab, 100.2, 10, 10
        while (true) {
            try {
                reader.skip(skipPattern); // Get rid of the CRLF
                if (reader.hasNext("q")) { // q infers quit current add book action
                    reader.nextLine();
                    return;
                }

                // Read information needed for update
                reader.useDelimiter(pattern);
                String book_number = reader.next();
                String category = reader.next();
                String title = reader.next();
                String press = reader.next();
                int year = reader.nextInt();
                String author = reader.next();
                double price = reader.nextDouble();
                int total = reader.nextInt();
                reader.reset();
                reader.skip(pattern); // Get rid of the next pattern
                int stock = reader.nextInt(); // The last int is a little bit tricky

                // Trying to find books
                System.out.println("Executing select command to find book by book number");
                PreparedStatement pStmtSelect = conn.prepareStatement("select * from book where bno=?");
                pStmtSelect.setString(1, book_number);
                // Here we only process InputMismatch in outer try
                try {
                    // Execute query silently and print nothing
                    ResultSet rSet = pStmtSelect.executeQuery();
                    if (rSet.next()) { // Has elements
                        System.out.println("Duplication found");
                        printResultSet(rSet); // Execute query and print result
                        while (true) {
                            System.out.print("Are you sure you want to update this book's information(Y/N)? ");
                            String str = reader.next();
                            switch (str.charAt(0)) {
                                case 'N':
                                case 'n':
                                    return;
                                case 'Y':
                                case 'y':
                                    break;
                                default:
                                    continue;
                            }
                            break;
                        }
                        System.out.println("Trying to perform update");
                    } else {
                        System.out.println("No duplication found, trying to perform insertion");
                    }
                } catch (SQLException e) {
                    logger.warn("Unable to perform the selection by book number: {}", book_number);
                    e.printStackTrace();
                    return;
                }

                // Prepare update statement
                PreparedStatement pStmt = conn.prepareStatement("replace into book values (?,?,?,?,?,?,?,?,?)");
                pStmt.setString(1, book_number);
                pStmt.setString(2, category);
                pStmt.setString(3, title);
                pStmt.setString(4, press);
                pStmt.setInt(5, year);
                pStmt.setString(6, author);
                pStmt.setDouble(7, price);
                pStmt.setInt(8, total);
                pStmt.setInt(9, stock);

                // Execute update statement
                try {
                    int result = pStmt.executeUpdate();
                    System.out.printf("OK, %d row(s) affected\n", result);
                } catch (Exception e) {
                    logger.warn("Unable to perform the update or insertion, check your input");
                    e.printStackTrace();
                    break;
                }
                executePrepQuery(pStmtSelect);
                break;
            } catch (InputMismatchException e) {
                // If input cannot match, try again
                String str = reader.nextLine();
                logger.warn("Cannot interpret the input value {}, try again", str);
                System.out.println("Unable to interpret the input, try again");
                reader.reset();
            } catch (Exception e) {
                e.printStackTrace();
                reader.reset();
                return;
            }
        }
    }

    /**
     * Add books in batch
     * Utilize the executeBatch API of JDBC
     * Can be used with system input or file input
     * Handles duplication according user's confirmation
     */
    static void addBooks() {
        ArrayList<Map<String, String>> maps = new ArrayList<>();
        PreparedStatement pStmt; // needed for error log
        PreparedStatement pStmtSelect; // needed for error log

        System.out.println("""
                Please input the
                1. book number 2. category 3. title 4. press 5. year 6. author 7. price 8. total 9. stock
                of the book (existing books will be updated (by book number))
                (input b to break, q to quit) (separate by comma): """);
        while (true) {
            Map<String, String> map = new HashMap<>(); // new map on every loop
            try {
                reader.skip(skipPattern);
                if (!reader.hasNext()) { // For file (EOF returns false)
                    break;
                }
                if (reader.hasNext("b")) { // b infers break
                    reader.nextLine();
                    break;
                }
                if (reader.hasNext("q")) { // q infers quit (quit current add books operation)
                    reader.nextLine();
                    return;
                }

                // Getting the input
                reader.useDelimiter(pattern); // pattern: \\p{javaWhitespace}*,\\p{javaWhitespace}*
                map.put("book_number", reader.next());
                map.put("category", reader.next());
                map.put("title", reader.next());
                map.put("press", reader.next());
                map.put("year", Integer.toString(reader.nextInt()));
                map.put("author", reader.next());
                map.put("price", Double.toString(reader.nextDouble()));
                map.put("total", Integer.toString(reader.nextInt()));
                reader.reset(); // the delimiter of the last input is a little tricky
                reader.skip(pattern); // Get rid of the next pattern
                map.put("stock", Integer.toString(reader.nextInt()));

                // Update the outer map array
                maps.add(map);
            } catch (InputMismatchException e) {
                String str = reader.nextLine();
                logger.warn("Cannot interpret input {}", str);
                reader.reset();
            } catch (Exception e) {
                e.printStackTrace();
                reader.reset(); // Do a reset of input stream however the execution (clear delimiter)
                return;
            }
        }

        if (!maps.isEmpty()) {
            // Confirmation of entry number
            System.out.printf("We've received %d entry(s) to update or insert\n", maps.size());
            try {
                // Select book information
                StringBuilder sql = new StringBuilder("select * from book where ");
                sql.append("bno=? or ".repeat(maps.size()));
                sql = new StringBuilder(sql.substring(0, sql.length() - 4));
                pStmtSelect = conn.prepareStatement(sql.toString());
                for (int i = 0; i < maps.size(); i++) {
                    pStmtSelect.setString(i + 1, maps.get(i).get("book_number"));
                }
                ResultSet rSet = pStmtSelect.executeQuery();
                if (rSet.next()) {
                    System.out.println("Duplication found");
                    printResultSet(rSet);

                    // Backup the scanner in case we are reading from file
                    // If in file the current scanner should have been exhausted
                    while (true) {
                        System.out.print("Are you sure you want to update this(these) book(s)' information(Y/N)? ");
                        String str = (new Scanner(System.in)).next(); // Don't know why eclipse warns me about not
                        // closing the scanner here. Isn't system.in to be
                        // used later, why bother closing it?
                        switch (str.charAt(0)) {
                            case 'N':
                            case 'n':
                                return;
                            case 'Y':
                            case 'y':
                                break;
                            default:
                                continue;
                        }
                        break;
                    }
                    System.out.println("Trying to perform update");
                } else {
                    System.out.println("No duplication found, trying to perform update");
                }
                pStmt = conn.prepareStatement("replace into book values (?,?,?,?,?,?,?,?,?)");
                for (Map<String, String> map : maps) {
                    pStmt.setString(1, map.get("book_number"));
                    pStmt.setString(2, map.get("category"));
                    pStmt.setString(3, map.get("title"));
                    pStmt.setString(4, map.get("press"));
                    pStmt.setInt(5, Integer.parseInt(map.get("year")));
                    pStmt.setString(6, map.get("author"));
                    pStmt.setDouble(7, Double.parseDouble(map.get("price")));
                    pStmt.setInt(8, Integer.parseInt(map.get("total")));
                    pStmt.setInt(9, Integer.parseInt(map.get("stock")));
                    pStmt.addBatch();
                }
                try {
                    // ResultSetMetaData rsmd = pStmt.getMetaData();
                    int[] ns = pStmt.executeBatch();
                    int sum = 0;
                    for (int n : ns) {
                        sum += n;
                    }
                    System.out.println("OK, " + sum + " row(s) affected."); // batch中每个SQL执行的结果数量
                    // Select book information
                    executePrepQuery(pStmtSelect);
                } catch (SQLException e) {
                    logger.warn("Unable to perform batch execution: {}", pStmt);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manage reader's proof
     */
    static void manageProof() {
        int choice;
        while (true) {
            try {
                choice = getChoice("""
                        1. Delete reader's proof
                        2. Add reader's proof
                        3. Modify reader's proof
                        0. Return (q. return when altering)""");
            } catch (InputMismatchException e) {
                continue;
            }
            switch (choice) {
                case 0 -> {
                    return;
                }
                case 1 -> deleteProof();
                case 2 -> addProof();
                case 3 -> modifyProof();
                default -> logger.warn("Wrong service code: {}", choice);
            }
        }
    }

    /**
     * Delete unwanted user's proof
     * Will guide through the user step by step
     */
    static void deleteProof() {
        System.out.print("Please input the card number you want to delete: ");
        reader.skip(skipPattern); // Get rid of unwanted chars
        String card_number = reader.nextLine();
        if ("q".equals(card_number)) {
            return;
        }

        // Execute the modification
        System.out.println("Executing selection from table card");
        try {
            // Check existence
            PreparedStatement pStmtSelect = conn.prepareStatement("select * from card where cno=?");
            pStmtSelect.setString(1, card_number);
            PreparedStatement pStmt = null;
            try {
                ResultSet rSet = pStmtSelect.executeQuery();
                if (!rSet.next()) {
                    System.out.printf("Unable to find the proof specified by card number: %s\n", card_number);
                    return;
                }

                // Print information
                printResultSet(rSet);
                while (true) { // Loop till a valid input is get
                    System.out.print("Is this the card you want to delete (Y/N)? ");
                    String str = reader.next();
                    switch (str.charAt(0)) {
                        case 'N':
                        case 'n':
                            return;
                        case 'Y':
                        case 'y':
                            break;
                        default:
                            continue;
                    }
                    break;
                }
            } catch (SQLException e) {
                logger.warn("Unable to perform the query: {}", pStmtSelect);
                e.printStackTrace();
            }
            try {
                // Execute update
                System.out.println("Trying to perform update");
                pStmt = conn.prepareStatement("delete from card where cno=?");
                pStmt.setString(1, card_number);
                int result = pStmt.executeUpdate();
                System.out.printf("OK, %d row(s) affected\n", result);
            } catch (SQLException e) {
                logger.warn("Unable to perform the query: {}", pStmt);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset();
        }
    }

    /**
     * Similar as delete proof
     * Check the existence of the proof by card number first
     * Then get other information
     * Ask for confirmation when found duplication
     */
    static void addProof() {
        System.out.print("Please input the card number you want to add: ");
        reader.skip(skipPattern);
        String card_number = reader.nextLine();
        if ("q".equals(card_number)) {
            return;
        }
        System.out.println("Executing selection from table card");
        try {
            PreparedStatement pStmtSelect = conn.prepareStatement("select * from card where cno=?");
            PreparedStatement pStmt = null;
            pStmtSelect.setString(1, card_number);
            try {
                ResultSet rSet = pStmtSelect.executeQuery();
                if (rSet.next()) {
                    System.out.println("Found duplication"); // if duplicated, ask for confirmation
                    printResultSet(rSet);
                    while (true) {
                        System.out.print("Do you want to modify this card's information (Y/N)? ");
                        String str = reader.next();
                        switch (str.charAt(0)) {
                            case 'N':
                            case 'n':
                                return;
                            case 'Y':
                            case 'y':
                                break;
                            default:
                                continue;
                        }
                        break;
                    }
                }
            } catch (SQLException e) {
                logger.warn("Unable to perform the query: {}", pStmtSelect);
                e.printStackTrace();
            }
            try {
                // Here we use the preview feature of Java 14
                // in text block we can also use \040: octal escape char as space
                System.out.println("""
                        Please input the
                        1. owner name 2. department 3. type ("S" or "T")
                        of the new card (separate by comma): """);
                pStmt = conn.prepareStatement("replace into card values (?,?,?,?)");
                reader.skip(skipPattern); // Get rid of the CRLF
                if (reader.hasNext("q")) {
                    reader.nextLine();
                    return;
                }

                // Change delimiter
                reader.useDelimiter(pattern);
                String name = reader.next();
                String department = reader.next();
                reader.reset();
                reader.skip(pattern); // Get rid of the next pattern
                String type = reader.next();

                // Perform query
                pStmt.setString(1, card_number);
                pStmt.setString(2, name);
                pStmt.setString(3, department);
                pStmt.setString(4, type);
                int result = pStmt.executeUpdate();
                System.out.printf("OK, %d row(s) affected\n", result);
                executePrepQuery(pStmtSelect);
            } catch (SQLException e) {
                logger.warn("Unable to perform the query: {}", pStmt);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset(); // reset the reader's delimiter upon error
        }
    }

    /**
     * Like the previous
     */
    static void modifyProof() {
        System.out.print("Please input the card number you want to modify: ");
        reader.skip(skipPattern);
        String card_number = reader.nextLine();
        if ("q".equals(card_number)) {
            return;
        }
        System.out.println("Executing selection from table card");
        try {
            PreparedStatement pStmtSelect = conn.prepareStatement("select * from card where cno=?");
            pStmtSelect.setString(1, card_number);
            PreparedStatement pStmt = null;
            try {
                ResultSet rSet = pStmtSelect.executeQuery();
                if (!rSet.next()) {
                    System.out.printf("Unable to find the proof specified by card number: %s\n", card_number);
                    return;
                }
                printResultSet(rSet);
                while (true) {
                    System.out.print("Is this the card you want to modify (Y/N)? ");
                    String str = reader.next();
                    switch (str.charAt(0)) {
                        case 'N':
                        case 'n':
                            return;
                        case 'Y':
                        case 'y':
                            break;
                        default:
                            continue;
                    }
                    break;
                }
            } catch (SQLException e) {
                logger.warn("Unable to perform the query: {}", pStmtSelect);
                e.printStackTrace();
            }
            try {
                System.out.println("""
                        Please input the
                        1. owner name 2. department 3. type ("S" or "T")
                        of the new card (separate by comma): """);
                pStmt = conn.prepareStatement("replace into card values (?,?,?,?)");
                reader.skip(skipPattern); // Get rid of the CRLF
                if (reader.hasNext("q")) {
                    reader.nextLine();
                    return;
                }
                reader.useDelimiter(pattern);
                String name = reader.next();
                String department = reader.next();
                reader.reset();
                reader.skip(pattern); // Get rid of the next pattern
                String type = reader.next();
                pStmt.setString(1, card_number);
                pStmt.setString(2, name);
                pStmt.setString(3, department);
                pStmt.setString(4, type);
                int result = pStmt.executeUpdate();
                System.out.printf("OK, %d row(s) affected\n", result);
                executePrepQuery(pStmtSelect);
            } catch (SQLException e) {
                logger.warn("Unable to perform the query: {}", pStmt);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.reset(); // reset reader's delimiter
        }
    }

    /**
     * Calls addBooks() after changing the reader (Scanner) to a file stream
     */
    static void addBooksFromFile() {
        Scanner backup = reader;
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.print("Please input the file that contains the book information: ");
        while (true) {
            String filePath = reader.next();
            if ("q".equals(filePath)) {
                return;
            }
            try {
                reader = new Scanner(new File(filePath)); // Try initializing the file with given path
                break;
            } catch (FileNotFoundException e) {
                logger.warn("File not found for file path: \"{}\"", filePath);
                System.out.print("Cannot find the file specified, try again: ");
            }
        }

        addBooks();
        reader.close();
        reader = backup;
    }
}