import java.io.PrintStream;
import java.util.*;

public class MVCCTest5 {
	// This is an example test file. Try others to debug your system!!!
	private static PrintStream log = System.out;

	public static void main(String[] args) {
		// # of test to execute
		// For automatic validation, it is not possible to execute all tests at once
		// You can get the TEST# from args and execute all tests using a shell-script 
		int TEST = 12;
		try {
			switch (TEST) {
				case 1: test1(); break;
				case 2: test2(); break;
				case 3: test3(); break;
				case 4: test4(); break;
				case 5: test5(); break;
				case 6: test6(); break;
				case 7: test7(); break;
				case 8: test8(); break;
				case 9: test9(); break;
				case 10: test10(); break;
				case 11: test11(); break;
				case 12: test12(); break;
				case 13: test13(); break;
				case 14: test14(); break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void test1() {
		log.println("----------- Test 1 -----------");
		/* Example schedule:
		 T1: I(1) C
		 T2:        R(1) W(1)           R(1) W(1) C
		 T3:                  R(1) W(1)             C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(1),__C_                                        },
			/*T2:*/ {____,____,R(1),W(1),____,____,R(1),W(1),__C_     },
			/*T3:*/ {____,____,____,____,R(1),W(1),____,____,____,__C_}
		};
//		T(1):I(1,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):R(1) => 4
//		T(2):W(1,10)
//		T(3):R(1) => 10
//		T(3):W(1,14)
//		T(2):R(1) => 10
//		T(2):W(1,18)
//		T(2):ROLLBACK
//		T(3):ROLLBACK
//		    ROLLBACK T(2):W(1,18)
//		T(3):COMMIT START
//		    T(3) DOES NOT EXIST
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(3)] = STEP(1);
		expectedResults[T(3)][STEP(5)] = STEP(4);
		expectedResults[T(2)][STEP(7)] = STEP(4);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	//if you did the OPTIONAL BONUS WORK
	private static void test2(){
		log.println("----------- Test 2 -----------");
		/* Example schedule:
		 T1: I(2) C
		 T2:        R(2) W(2)           M(4) W(2) C
		 T3:                  R(2) W(2)             C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(2),__C_                                        },
			/*T2:*/ {____,____,R(2),W(2),____,____,M(4),W(2),__C_     },
			/*T3:*/ {____,____,____,____,R(2),W(2),____,____,____,__C_}
		};
//		T(1):I(2,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):R(2) => 4
//		T(2):W(2,10)
//		T(3):R(2) => 10
//		T(3):W(2,14)
//		T(2):MODQUERY(4) => []
//		T(2):W(2,18)
//		T(2):ROLLBACK
//		T(3):ROLLBACK
//		    ROLLBACK T(2):W(2,18)
//		T(3):COMMIT START
//		    T(3) DOES NOT EXIST
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(3)] = STEP(1);
		expectedResults[T(3)][STEP(5)] = STEP(4);
		expectedResults[T(2)][STEP(7)] = new int[]{STEP(4)};
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test3() {
		log.println("----------- Test 3 -----------");
		/* Example schedule:
		 T1: I(3) C
		 T2:        R(3)          R(3) C
		 T3:             W(3)               C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(3),__C_                         },
			/*T2:*/ {____,____,R(3),____,R(3),__C_     },
			/*T3:*/ {____,____,____,W(3),____,____,__C_}
		};
//		T(1):I(3,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):R(3) => 4
//		T(3):W(3,10)
//		T(2):R(3) => 4
//		T(2):COMMIT START
//		T(2):COMMIT FINISH
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(3)] = STEP(1);
		expectedResults[T(2)][STEP(5)] = STEP(1);
		executeSchedule(schedule, expectedResults, maxLen);
	}

	private static void test4() {
		log.println("----------- Test 4 -----------");
		/* Example schedule:
		 T1: I(4) C
		 T2:        R(4) W(4)           R(4) W(4) C
		 T3:                  R(4) W(4)             C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(4),__C_                                        },
			/*T2:*/ {____,____,R(4),W(4),____,____,____,R(4),W(4),__C_},
			/*T3:*/ {____,____,____,____,R(4),W(4),__C_               }
		};
//		T(1):I(4,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):R(4) => 4
//		T(2):W(4,10)
//		T(3):R(4) => 10
//		T(3):W(4,14)
//		T(3):COMMIT START
//		T(2):R(4) => 10
//		T(2):W(4,20)
//		T(2):ROLLBACK
//		T(3):COMMIT UNSUCCESSFUL
//		T(3):ROLLBACK
//		    ROLLBACK T(2):W(4,20)
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(3)] = STEP(1);
		expectedResults[T(3)][STEP(5)] = STEP(4);
		expectedResults[T(2)][STEP(8)] = STEP(4);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test5() {
		log.println("----------- Test 5 -----------");
		/* Example schedule:
		 T1: I(5) C
		 T2:        R(5)          W(5) C
		 T3:             W(5)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(5),__C_                         },
			/*T2:*/ {____,____,R(5),____,____,W(5),__C_},
			/*T3:*/ {____,____,____,W(5),__C_          }
		};
//		T(1):I(5,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):R(5) => 4
//		T(3):W(5,10)
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
//		T(2):W(5,14)
//		T(2):COMMIT START
//		T(2):COMMIT FINISH
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(3)] = STEP(1);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test6() {
		log.println("----------- Test 6 -----------");
		/* Example schedule:
		 T1: I(6) C
		 T2:        R(6)          W(6) C
		 T3:             R(6)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(6),__C_                         },
			/*T2:*/ {____,____,R(6),____,____,W(6),__C_},
			/*T3:*/ {____,____,____,R(6),__C_          }
		};
//		T(1):I(6,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):R(6) => 4
//		T(3):R(6) => 4
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
//		T(2):W(6,14)
//		T(2):ROLLBACK
//		    ROLLBACK T(2):W(6,14)
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(3)] = STEP(1);
		expectedResults[T(3)][STEP(4)] = STEP(1);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test7() {
		log.println("----------- Test 7 -----------");
		/* Example schedule:
		 T1: I(7) C
		 T2:        W(7)          W(7) C
		 T3:             R(7)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(7),__C_                         },
			/*T2:*/ {____,____,W(7),____,____,W(7),__C_},
			/*T3:*/ {____,____,____,R(7),__C_          }
		};
//		T(1):I(7,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):W(7,8)
//		T(3):R(7) => 8
//		T(3):COMMIT START
//		T(2):W(7,14)
//		T(2):ROLLBACK
//		T(3):COMMIT UNSUCCESSFUL
//		T(3):ROLLBACK
//		    ROLLBACK T(2):W(7,14)
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(3)][STEP(4)] = STEP(3);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test8() {
		log.println("----------- Test 8 -----------");
		/* Example schedule:
		 T1: I(8) C
		 T2:        W(8)          ROLLBACK
		 T3:             R(8)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(8),__C_                    },
			/*T2:*/ {____,____,W(8),____,____,_RB_},
			/*T3:*/ {____,____,____,R(8),__C_     }
		};
//		T(1):I(8,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):W(8,8)
//		T(3):R(8) => 8
//		T(3):COMMIT START
//		T(2):ROLLBACK
//		T(3):COMMIT UNSUCCESSFUL
//		T(3):ROLLBACK
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(3)][STEP(4)] = STEP(3);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test9() {
		log.println("----------- Test 9 -----------");
		/* Example schedule:
		 T1: I(9) C
		 T2:        W(9)          W(9) C
		 T3:             M(2)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(9),__C_                         },
			/*T2:*/ {____,____,W(9),____,____,W(9),__C_},
			/*T3:*/ {____,____,____,M(2),__C_          }
		};
//		T(1):I(9,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):W(9,8)
//		T(3):MODQUERY(2) => [9]
//		T(3):COMMIT START
//		T(2):W(9,14)
//		T(2):ROLLBACK
//		T(3):COMMIT UNSUCCESSFUL
//		T(3):ROLLBACK
//		    ROLLBACK T(2):W(9,14)
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(3)][STEP(4)] = new int[]{STEP(3)};
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test10() {
		log.println("----------- Test 10 -----------");
		/* Example schedule:
		 T1: I(1) C
		 T2:        W(1)          W(1) C
		 T3:             M(3)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(1),__C_                         },
			/*T2:*/ {____,____,W(1),____,____,W(1),__C_},
			/*T3:*/ {____,____,____,M(3),__C_          }
		};
//		T(1):I(1,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):W(1,8)
//		T(3):MODQUERY(3) => []
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
//		T(2):W(1,14)
//		T(2):COMMIT START
//		T(2):COMMIT FINISH
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(3)][STEP(4)] = new int[]{STEP(3)};
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test11() {
		log.println("----------- Test 11 -----------");
		/* Example schedule:
		 T1: I(2) C
		 T2:        W(2)          W(2) I(3)   C
		 T3:             M(9)   C
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(2),__C_                                   },
			/*T2:*/ {____,____,W(2),____,____,W(2),____,I(3),__C_},
			/*T3:*/ {____,____,____,M(9),__C_                    }
		};
//		T(1):I(2,4)
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(2):W(2,8)
//		T(3):MODQUERY(9) => []
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
//		T(2):W(2,14)
//		T(2):I(3,18)
//		T(2):ROLLBACK
//		    ROLLBACK T(2):I(3,18)
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(3)][STEP(4)] = new int[]{STEP(3)};
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test12() {
		log.println("----------- Test 12 -----------");
		/* Example schedule:
		 T1: I(2),     I(7),                                C 
		 T2:      I(2),     W(2),               R(2),  C 
		 T3:                     R(2),R(7),  C 
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(2),____,I(7),____,____,____,____,____,____,__C_},
			/*T2:*/ {____,I(2),____,W(2),____,____,____,R(2),__C_     },
			/*T3:*/ {____,____,____,____,R(2),R(7),__C_               }
		};
//		T(1):I(2,4)
//		T(2):I(2,6)
//		T(2):ROLLBACK
//		    KEY ALREADY EXISTS IN T(2):I(2)
//		T(1):I(7,8)
//		T(3):R(2) => 4
//		T(3):R(7) => 8
//		T(3):COMMIT START
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(3):COMMIT FINISH
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(3)][STEP(5)] = STEP(1);
		expectedResults[T(3)][STEP(6)] = STEP(3);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test13() {
		log.println("----------- Test 13 -----------");
		/* Example schedule:
		 T1: I(2),     I(7),       C 
		 T2:      W(2),     R(7),               W(2),  C 
		 T3:                          R(7),  C 
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(2),____,I(7),____,__C_                    },
			/*T2:*/ {____,W(2),____,R(7),____,____,____,W(2),__C_},
			/*T3:*/ {____,____,____,____,____,R(7),__C_          }
		};
//		T(1):I(2,4)
//		T(2):W(2,6)
//		T(1):I(7,8)
//		T(2):R(7) => 8
//		T(1):COMMIT START
//		T(1):COMMIT FINISH
//		T(3):R(7) => 8
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
//		T(2):W(2,18)
//		T(2):COMMIT START
//		T(2):COMMIT FINISH
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(4)] = STEP(3);
		expectedResults[T(3)][STEP(6)] = STEP(3);
		executeSchedule(schedule, expectedResults, maxLen);
	}
	
	private static void test14() {
		log.println("----------- Test 14 -----------");
		/* Example schedule:
		 T1: I(2),                RB 
		 T2:      R(2),  C 
		 T3:                W(2),       C 
		*/
		int[][][] schedule = new int[][][]{
			/*T1:*/ {I(2),____,____,____,_RB_     },
			/*T2:*/ {____,R(2),__C_               },
			/*T3:*/ {____,____,____,W(2),____,__C_}
		};
//		T(1):I(2,4)
//		T(2):R(2) => 4
//		T(2):COMMIT START
//		T(3):W(2,10)
//		T(1):ROLLBACK
//		T(2):COMMIT UNSUCCESSFUL
//		T(2):ROLLBACK
//		T(3):COMMIT START
//		T(3):COMMIT FINISH
		int maxLen = analyzeSchedule(schedule);
		printSchedule(schedule);
		Object[][] expectedResults = new Object[schedule.length][maxLen];
		expectedResults[T(2)][STEP(2)] = STEP(1);
		executeSchedule(schedule, expectedResults, maxLen);
	}

	/**
	 * This method is for executing a schedule.
	 * 
	 * @param schedule is a 3D array containing one transaction 
	 *                 in each row, and in each cell is one operation
	 * @param expectedResults is the array of expected result in each
	 *                 READ or MOD_QUERY operation. For:
	 *                  - READ: the cell contains the STEP# (zero-based)
	 *                          in the schedule that WRITTEN or INSERTED
	 *                          the value that should be read here.
	 *                  - MOD_QUERY: the cell contains an array of STEP
	 *                          numbers in the schedule that WRITTEN or
	 *                          INSERTED values that can be a candidate
	 *                          for the result-set.
	 * @param maxLen is the maximum length of schedule
	 */
	private static void executeSchedule(int[][][] schedule, Object[][] expectedResults, int maxLen) {
		Map<Integer, Integer> xactLabelToXact = new HashMap<Integer, Integer>();
		Set<Integer> ignoredXactLabels = new HashSet<Integer>();

		for(int step=0; step<maxLen; step++) {
			for(int i=0; i<schedule.length; i++) {
				if(step < schedule[i].length && schedule[i][step] != null) {
					int[] xactOps = schedule[i][step];
					int xactLabel = i+1;
					if(ignoredXactLabels.contains(xactLabel)) break;
					
					int xact = 0;
					try {
						if(xactLabelToXact.containsKey(xactLabel)) {
							xact = xactLabelToXact.get(xactLabel);
						} else {
							xact = MVCC.begin_transaction();
							xactLabelToXact.put(xactLabel, xact);
						}
						if(xactOps.length == 1) {
							switch(xactOps[0]) {
								case COMMIT: MVCC.commit(xact); break;
								case ROLL_BACK: MVCC.rollback(xact); break;
							}
						} else {
							switch(xactOps[0]) {
								case INSERT: MVCC.insert(xact, xactOps[1], getValue(step)); break;
								case WRITE: MVCC.write(xact, xactOps[1], getValue(step)); break;
								case READ: {
									int readValue = MVCC.read(xact, xactOps[1]);
									int expected = getValue((Integer)expectedResults[T(xactLabel)][step]);
									if(readValue != expected) {
										throw new WrongResultException(xactLabel, step, xactOps, readValue, expected);
									}
									break;
								}
								case MOD_QUERY: {
									List<Integer> readValues = MVCC.modquery(xact, xactOps[1]);
									Object expected = getValues((int[])expectedResults[T(xactLabel)][step], xactOps[1], schedule);
									if(!readValues.equals(expected)) {
										throw new WrongResultException(xactLabel, step, xactOps,readValues,expected);
									}
									break;
								}
							}
						}
					} catch (WrongResultException e) {
						throw e;
					} catch (Exception e) {
						ignoredXactLabels.add(xactLabel);
						log.println("    "+e.getMessage());
						//e.printStackTrace();
					}
					break;
				}
			}
		}
	}
	/**
	 * @param k is k in MOD_QUERY operation
	 * @param expectedResults is the current expectedResults array
	 * @param schedule is the current schedule array
	 * @return the actual expected result of a MOD_QUERY operation
	 *         in a schedule.
	 */
	private static Object getValues(int[] expectedResults, int k, int[][][] schedule) {
		List<Integer> res = new LinkedList<Integer>();
		for(int step : expectedResults) {
			if(getValue(step) % k == 0){
				for(int xactLabel = 1 ; xactLabel <= schedule.length; xactLabel++) {
					if(step >= schedule[T(xactLabel)].length) continue;
					int[] xactOps = schedule[T(xactLabel)][step];
					if(xactOps != null) {
						//it should be an Inser ot Write
						res.add(xactOps[1]);
					}
				}
			}
		}
		return res;
	}
	/**
	 * @param step is the STEP# in the schedule (zero-based)
	 * @return the expected result of a READ operation in a schedule.
	 */
	private static int getValue(int step) {
		return (step+2)*2;
	}

	private static void printSchedule(int[][][] schedule) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<schedule.length; i++) {
			sb.append("T").append(i+1).append(": ");
			for(int j=0; j<schedule[i].length; j++) {
				int[] xactOps = schedule[i][j];
				if(xactOps == null) {
					sb.append("     ");
				} else if(xactOps.length == 1) {
					switch(xactOps[0]) {
						case COMMIT: sb.append("  C "); break;
						case ROLL_BACK: sb.append(" RB "); break;
					}
				} else {
					switch(xactOps[0]) {
						case INSERT: sb.append("I"); break;
						case WRITE: sb.append("W"); break;
						case READ: sb.append("R"); break;
						case MOD_QUERY: sb.append("M"); break;
					}
					sb.append("(").append(xactOps[1]).append(")");
				}
				if(j+1<schedule[i].length && xactOps != null){
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		log.println("\n"+sb.toString());
	}

	/**
	 * Analyzes and validates the given schedule.
	 * 
	 * @return maximum number of steps in the
	 *         transactions inside the given schedule
	 */
	private static int analyzeSchedule(int[][][] schedule) {
		int maxLen = 0;
		for(int i=0; i<schedule.length; i++) {
			if(maxLen < schedule[i].length) {
				maxLen = schedule[i].length;
			}
			for(int j=0; j<schedule[i].length; j++) {
				int[] xactOps = schedule[i][j];
				if(xactOps == null) {
					// no operation
				} else if(xactOps.length == 1 && (xactOps[0] == COMMIT || xactOps[0] == ROLL_BACK)) {
					// commit or roll back
				} else if(xactOps.length == 2){
					switch(xactOps[0]) {
						case INSERT: /*insert*/ break;
						case WRITE: /*write*/; break;
						case READ: /*read*/; break;
						case MOD_QUERY: /*read*/; break;
						default: throw new RuntimeException("Unknown operation in schedule: T"+(i+1)+", Operation "+(j+1));
					}
				} else {
					throw new RuntimeException("Unknown operation in schedule: T"+(i+1)+", Operation "+(j+1));
				}
			}
		}
		return maxLen;
	}
	
	private final static int INSERT = 1, WRITE = 2, READ = 3, MOD_QUERY=4, COMMIT = 5, ROLL_BACK = 6;
	private final static int[] __C_ = {COMMIT}, _RB_ = {ROLL_BACK}, ____ = null;

	//transaction
	private static int T(int i) {
		return i-1;
	}
	//step
	private static int STEP(int i) {
		return i-1;
	}
	//insert
	public static int[] I(int key) {
		return new int[]{INSERT,key};
	}
	//write
	public static int[] W(int key) {
		return new int[]{WRITE,key};
	}
	//read
	public static int[] R(int key) {
		return new int[]{READ,key};
	}
	//mod_query
	public static int[] M(int k) {
		return new int[]{MOD_QUERY,k};
	}
}

class WrongResultException extends RuntimeException {
	private static final long serialVersionUID = -7630223385777784923L;

	public WrongResultException(int xactLabel, int step, int[] operation, Object actual, Object expected) {
		super("Wrong result in T("+xactLabel+") in step " + (step+1) + " (Actual: " + actual+", Expected: " + expected + ")");
	}
}