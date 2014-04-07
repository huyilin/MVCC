import java.util.List;
import java.util.ArrayList;

// IMPORTANT -- THIS IS INDIVIDUAL WORK. ABSOLUTELY NO COLLABORATION!!!
// implement a (main-memory) data store with MVCC.
// objects are <int, int> key-value pairs.
// if an operation is to be refused by the MVCC protocol,
// undo its xact (what work does this take?) and throw an exception.
// garbage collection of versions is optional.
// Throw exceptions when necessary, such as when we try to execute an operation in
// a transaction that is not running; when we insert an object with an existing
// key; when we try to read or write a nonexisting key, etc.
// You may but do not need to create different exceptions for operations that
// are refused and for operations that are refused and cause the Xact to be
// aborted. Keep it simple!
// Keep the interface, we want to test automatically!

public class MVCC {
  /* TODO -- your versioned key-value store data structure */

  private static int max_xact = 0;

  // returns transaction id == logical start timestamp
  public static int begin_transaction() { return ++max_xact; }

  // create and initialize new object in transaction xact
  public static void insert(int xact, int key, int value) throws Exception
  {
    /* TODO */
  }

  // return value of object key in transaction xact
  public static int read(int xact, int key) throws Exception
  {
    /* TODO */
    return 0; // FIX THIS
  }

  // write value of existing object identified by key in transaction xact
  public static void write(int xact, int key, int value) throws Exception
  {
    /* TODO */
  }

  // Implementing queries is OPTIONAL for bonus points!
  // return the list of keys of objects whose values mod k are zero.
  // this is our only kind of query / bulk read. Your implementation must still
  // guarantee serializability. How do you deal with inserts? By maintaining
  // a history of querys with suitable metadata (xact?). Do you need a form of
  // locking?
  public static List<Integer> modquery(int xact, int k) throws Exception
  {
    List<Integer> l = new ArrayList<Integer>();
    /* TODO */
    return l;
  }

  public static void commit(int xact)   throws Exception { /* TODO */ }
  public static void rollback(int xact) throws Exception { /* TODO */ }
}

