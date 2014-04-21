import java.util.*;

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
	
// Data Structures used
  private static class Version{
	  int rts;
	  int value;
	  boolean writer_commit=false;
	  boolean inserted=false;
  }
  
  private static class Transaction{
	  HashMap<Integer,ArrayList<Integer>> readed=new HashMap<Integer,ArrayList<Integer>>();
	  HashMap<Integer,Integer> writed=new HashMap<Integer,Integer>();
	  ArrayList<Integer> inserted=new ArrayList<Integer>();
  }
 
  private static int max_xact = 0;
  private static HashMap<Integer,Transaction> xact_map=new HashMap<Integer,Transaction>();
  private static ArrayList<Integer> block_xacts=new ArrayList<Integer>();
  private static ArrayList<Integer> abort_xacts=new ArrayList<Integer>();
  private static ArrayList<Integer> committed_xacts=new ArrayList<Integer>();
  private static HashMap<Integer, HashMap<Integer,Version>> version_pool
	=new HashMap<Integer,HashMap<Integer,Version>>(); 
  private static HashMap<Integer,Integer> bulk_map =new HashMap<Integer,Integer>(); 
  // returns transaction id == logical start timestamp
  public static int begin_transaction() { 
	  xact_map.put(max_xact+1,new Transaction());
	  return ++max_xact;
  }

  // create and initialize new object in transaction xact
  public static void insert(int xact, int key, int value) throws Exception{
    /* TODO */
	System.out.printf("T(%d):I(%d,%d)\n",xact,key,value);
	if(abort_xacts.contains(xact)){
		  String ex_info="T("+Integer.toString(xact)+"):ALREADY ABORTED";
		  throw new Exception(ex_info);
	}
	if(version_pool.containsKey(key)){
		rollback(xact);
		String ex_info="KEY ALREADY EXISTS IN T("+Integer.toString(xact)+"):I("+Integer.toString(key)+")";
		//System.out.println(ex_info);
		throw new Exception(ex_info);
	}
	Version version=new Version();
	version.value=value;
	version.rts=0;
	version.inserted=true;
	version_pool.put(key,new HashMap<Integer,Version>());
	version_pool.get(key).put(0,version);
	xact_map.get(xact).inserted.add(key);
	if (bulk_mod_conflict(xact,value)){
		rollback(xact);
		String ex_info="T("+Integer.toString(xact)+"):ROLLBACK COMPLETE";
		throw new Exception(ex_info);
	}
  }

  // return value of object key in transaction xact
  public static int read(int xact, int key) throws Exception{
    /* TODO */
	  int read_ver=xact;
	  if(abort_xacts.contains(xact)){
		  String ex_info="T("+Integer.toString(xact)+"):ALREADY ABORTED";
		  throw new Exception(ex_info);
	  }
	  if (!version_pool.containsKey(key)||!version_pool.get(key).get(0).inserted){	//for use of the mod, to warn the afterwards insertion of this readed object
		  rollback(xact);
		  String ex_info="OBJECT NOT EXISTS IN T("+Integer.toString(xact)+"):R("+Integer.toString(key)+")";
		  throw new Exception(ex_info);
	  }
	  for(;read_ver>=0;read_ver--){
		  if (version_pool.get(key).containsKey(read_ver)){
			  version_pool.get(key).get(read_ver).rts=Math.max(xact,version_pool.get(key).get(read_ver).rts);
			  break;
		  }
	  }
	  int return_value=version_pool.get(key).get(read_ver).value;
	  if(!xact_map.get(xact).readed.containsKey(key)){
		ArrayList<Integer> readed_list=new ArrayList<Integer>();
		readed_list.add(read_ver);
		xact_map.get(xact).readed.put(key,readed_list);
	  }
	  else xact_map.get(xact).readed.get(key).add(read_ver);
	  System.out.printf("T(%d):R(%d) => %d\n",xact,key,return_value);
	  return return_value;
  }
  
  public static boolean bulk_mod_conflict(int wts , int value) throws Exception{
	  for(Map.Entry<Integer,Integer> entry: bulk_map.entrySet()){
		  if(entry.getKey()>wts&&value%entry.getValue()==0){
			  return true;
		  }
	  }
	  return false;
  }
   
  // write value of existing object identified by key in transaction xact
  public static void write(int xact, int key, int value) throws Exception{
	  int write_ver=xact;
	  if(abort_xacts.contains(xact)){
		  String ex_info="T("+Integer.toString(xact)+"):ALREADY ABORTED";
		  throw new Exception(ex_info);
	  }
	  if (!version_pool.containsKey(key)||!version_pool.get(key).get(0).inserted){
		  rollback(xact);
		  String ex_info="OBJECT NOT EXISTS IN T("+Integer.toString(xact)+"):W("+Integer.toString(key)+")";
		  throw new Exception(ex_info);
	  }
	  for(;write_ver>=0;write_ver--){
		  if (version_pool.get(key).containsKey(write_ver)){
			  Version version=new Version();
			  version.value=value;
			  version.rts=version_pool.get(key).get(write_ver).rts;
			  version_pool.get(key).put(xact,version);
			  xact_map.get(xact).writed.put(key,xact);
			  System.out.printf("T(%d):W(%d,%d)\n",xact,key,value);
			  if(version_pool.get(key).get(write_ver).rts > xact||bulk_mod_conflict(xact,value)){
				  rollback(xact);
				  String ex_info="T("+Integer.toString(xact)+"):ROLLBACK COMPLETE";
				  throw new Exception(ex_info);
			  }
			  break;
		  }
		  }
	 }

  // Implementing queries is OPTIONAL for bonus points!
  // return the list of keys of objects whose values mod k are zero.
  // this is our only kind of query / bulk read. Your implementation must still
  // guarantee serializability. How do you deal with inserts? By maintaining
  // a history of querys with suitable metadata (xact?). Do you need aform of
  // locking?
  public static List<Integer> modquery(int xact, int k) throws Exception
  {
	if(abort_xacts.contains(xact)){
		String ex_info="T("+Integer.toString(xact)+"):ALREADY ABORTED";
		throw new Exception(ex_info);
	}
	List<Integer> l = new ArrayList<Integer>();
    int read_ver=xact;
    int condition=k;
    bulk_map.put(xact, condition);
    for(int key: version_pool.keySet()){
    	for(;read_ver>=0;read_ver--){
    		if (version_pool.get(key).containsKey(read_ver)){
  			  break;
    		}
  	  	}
    	if(version_pool.get(key).get(read_ver).value%k==0){
        	version_pool.get(key).get(read_ver).rts=Math.max(xact,version_pool.get(key).get(read_ver).rts);
        	if(!xact_map.get(xact).readed.containsKey(key)){
        		ArrayList<Integer> readed_list=new ArrayList<Integer>();
        		readed_list.add(read_ver);
        		xact_map.get(xact).readed.put(key,readed_list);
        	  }
        	else xact_map.get(xact).readed.get(key).add(read_ver);
      	  	l.add(key);
    	}
    }
    System.out.printf("T(%d):MODQUERY(%d) => ",xact,k);
	System.out.println(l);
    return l;
  }
  
  public static String read_check(int xact) throws Exception{ //check if all of the readed_version committed or not
	  HashMap<Integer,ArrayList<Integer>> map=xact_map.get(xact).readed;
	  boolean block_tag=false;
	  for(Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet()){
		  int object_id=entry.getKey();
		  ArrayList<Integer> object_versions=entry.getValue();
		  for(int object_version: object_versions){
			  if(version_pool.get(object_id).get(object_version)==null) return "abort";
			  if(!version_pool.get(object_id).get(object_version).writer_commit&&object_version!=xact) block_tag=true;  
		  }
		}
	  if (block_tag) return "block";
	  return "commit";
  }
  
  public static void writed_check(int xact) throws Exception{	//tag the all writed versions with committed 
	  HashMap<Integer,Integer> map=xact_map.get(xact).writed;
	  for(Map.Entry<Integer, Integer> entry : map.entrySet()){
		  int object_id=entry.getKey();
		  int object_version=entry.getValue();
		  version_pool.get(object_id).get(object_version).writer_commit=true;
		}
	  for(int insert_object:xact_map.get(xact).inserted){
		  version_pool.get(insert_object).get(0).writer_commit=true;
	  }
  }
  
  public static void update_block() throws Exception{
	  Iterator<Integer> iter =block_xacts.iterator();
	  int xact;
	  while(iter.hasNext()){
		  xact=iter.next().intValue();
		  if(read_check(xact)=="commit"){
				committed_xacts.add(xact);
				writed_check(xact);
				System.out.printf("T(%d):COMMIT FINISH\n",xact);
				iter.remove();
				update_block();
			}
	  }
  }
  
  public static void commit(int xact)  throws Exception {	
	  String result=read_check(xact);
	  if(committed_xacts.contains(xact)){
		  String ex_info="T("+Integer.toString(xact)+"):ALREADY COMMITTED";
		  throw new Exception(ex_info);
	  }
	  if(!committed_xacts.contains(xact)&&!abort_xacts.contains(xact)){
		  System.out.printf("T(%d):COMMIT START\n",xact);
		  if (result=="commit"){
			  committed_xacts.add(xact);
			  writed_check(xact);
			  System.out.printf("T(%d):COMMIT FINISH\n",xact);
			  update_block();
		  }
		  else if (result=="block"){
			  block_xacts.add(xact);
		  }
		  else if (result=="abort"){
			  rollback(xact);
			  System.out.printf("T(%d):COMMIT UNSUCCESSFUL\n",xact);
			  String ex_info="T("+Integer.toString(xact)+"):ROLLBACK COMPLETE";
			  throw new Exception(ex_info);
		  }
	  }
  }

  public static void rollback(int xact) throws Exception {
	  HashMap<Integer,Integer> writed_ver=xact_map.get(xact).writed;
	  int value;
	  System.out.printf("T(%d):ROLLBACK\n",xact);
	  for(Map.Entry<Integer, Integer> entry : writed_ver.entrySet()){
		  int key=entry.getKey();
		  value=version_pool.get(key).get(writed_ver.get(key)).value;
		  System.out.printf("ROLLBACK T(%d):W(%d,%d)\n",xact,key,value);
		  version_pool.get(key).remove(writed_ver.get(key));
	  }
	  for(int insert_object:xact_map.get(xact).inserted){
		  value=version_pool.get(insert_object).get(0).value;
		  version_pool.get(insert_object).remove(0);
		  System.out.printf("ROLLBACK T(%d):I(%d,%d)\n",xact,insert_object,value);
	  }
	  abort_xacts.add(xact);
	  update_block();
	  for(int key: xact_map.keySet()){
		  if(read_check(key)=="abort"&&!abort_xacts.contains(key)){
			  if(block_xacts.contains(key)){
				  System.out.printf("T(%d):COMMIT UNSUCCESSFUL\n",key);
				  block_xacts.remove(block_xacts.indexOf(key));
			  }
			  else if(committed_xacts.contains(key)){
				  committed_xacts.remove(committed_xacts.indexOf(key));
			  }
			  rollback(key);
		  }
	  }
  }
}