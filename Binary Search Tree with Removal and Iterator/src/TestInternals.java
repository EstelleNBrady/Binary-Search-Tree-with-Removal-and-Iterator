import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.uwm.cs351.Appointment;
import edu.uwm.cs351.NewApptBook;
import edu.uwm.cs351.Duration;
import edu.uwm.cs351.Period;
import edu.uwm.cs351.Time;


public class TestInternals extends NewApptBook.TestInvariantChecker {
	protected Time now = new Time();
	protected Appointment e1 = new Appointment(new Period(now,Duration.HOUR),"1: think");
	protected Appointment e2 = new Appointment(new Period(now,Duration.DAY),"2: current");
	protected Appointment e3 = new Appointment(new Period(now.add(Duration.HOUR),Duration.HOUR),"3: eat");
	protected Appointment e3a = new Appointment(new Period(now.add(Duration.HOUR),Duration.HOUR),"3: eat");
	protected Appointment e4 = new Appointment(new Period(now.add(Duration.HOUR.scale(2)),Duration.HOUR.scale(8)),"4: sleep");
	protected Appointment e5 = new Appointment(new Period(now.add(Duration.DAY),Duration.DAY),"5: tomorrow");

	private int reports = 0;
		
	protected void assertReporting(boolean expected, Supplier<Boolean> test) {
		reports = 0;
		Consumer<String> savedReporter = getReporter();
		try {
			setReporter((String message) -> {
				++reports;
				if (message == null || message.trim().isEmpty()) {
					assertFalse("Uninformative report is not acceptable", true);
				}
				if (expected) {
					assertFalse("Reported error incorrectly: " + message, true);
				}
			});
			assertEquals(expected, test.get().booleanValue());
			if (!expected) {
				assertEquals("Expected exactly one invariant error to be reported", 1, reports);
			}
			setReporter(null);
		} finally {
			setReporter(savedReporter);
		}

	}

	//// Tests
	// The tests fall into different groups that
	// are not ordered with respect to each other.
	// (So, for example, failing testC7 doesn't preclude 
	// one from getting credit for passing all testF tests.)

	
	/// Locked tests
	
	public void test() {
		testNextCursor(false);
	}
	
	@SuppressWarnings("unused")
	private void testNextCursor(boolean ignored) {
		// Suppose we have this tree:
		//    k
		//  /   \
		// b     t
		//  \     \
		//   f     x
		//  /
		// d
		//
		// Give the possibilities for nextCursor
		// by putting letters in order.  If it could be 
		// the "f" node, or null, write "fnull".
		String a1 = Ts(877277084); // if the cursor points at the 'b' node
		String a2 = Ts(789435991); // if the cursor points at the 'f' node
		String a3 = Ts(1354127775); // if the cursor points at the 't' node
		String a4 = Ts(1152669318); // if the cursor points at the 'x' node
		String a5 = Ts(1373693722); // if the cursor is null
	}
	
	
	
	
	/// testWx: tests of wellFormed
	
	private void assertWellFormed(boolean expected) {
		assertReporting(expected, () -> wellFormed());
	}

	public void test00() {
		setManyItems(1);
		assertWellFormed(false);
		setManyItems(0);
		assertWellFormed(true);
		setManyItems(-1);
		assertWellFormed(false);
	}
	
	public void test01() {
		Node n1 = newNode(e1,null,null);
		setRoot(n1);
		setManyItems(1);
		assertWellFormed(true);
		setManyItems(0);
		assertWellFormed(false);
		setManyItems(2);
		assertWellFormed(false);
	}
	
	public void test02() {
		Node n1 = newNode(e2, null, null);
		Node n2 = newNode(e4, n1, null);
		setRoot(n2);
		setManyItems(2);
		assertWellFormed(true);
		setManyItems(3);
		assertWellFormed(false);
		n1.setRight(newNode(e5, null, null));
		assertWellFormed(false);
	}
	
	public void test03() {
		Node n3 = newNode(e3, null, null);
		Node n3a = newNode(e3a, null, n3);
		Node n4 = newNode(e4, n3a, null);
		setRoot(n4);
		setManyItems(3);
		assertWellFormed(true);
		
		n3.setRight(n3a);
		assertWellFormed(false);
		
		setManyItems(5);
		assertWellFormed(false);
	}
	
	public void test04() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
	}
	
	private Iterator iterator;
	
	public void test10() {
		iterator = new Iterator(); // null for cursor and nextCursor
		assertReporting(true, () -> iterator.wellFormed());
		
		Node n1 = new Node(e1, null, null);
		iterator.setCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setColVersion(3);
		assertReporting(true, () -> iterator.wellFormed());
	}
	
	public void test11() {
		Node n1 = newNode(e1,null,null);
		setRoot(n1);
		setManyItems(1);
		assertWellFormed(true);
		
		iterator = new Iterator(); // null for cursor and nextCursor
		assertReporting(true, () -> iterator.wellFormed());
		
		setManyItems(0);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setColVersion(3);
		assertReporting(false, () -> iterator.wellFormed());
	}
	
	public void test12() {
		Node n2 = newNode(e2,null,null);
		setRoot(n2);
		setManyItems(1);
		assertWellFormed(true);
		
		iterator = new Iterator(); // null for cursor and nextCursor
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setNextCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setCursor(null);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setColVersion(3);
		assertReporting(true, () -> iterator.wellFormed());
	}
	
	public void test13() {
		Node n3 = newNode(e3,null,null);
		setRoot(n3);
		setManyItems(1);
		assertWellFormed(true);
		
		iterator = new Iterator(); // null for cursor and nextCursor
		assertReporting(true, () -> iterator.wellFormed());
		
		Node n3a = newNode(e3,null,null);
		
		iterator.setCursor(n3a);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n3a);
		assertReporting(false, () -> iterator.wellFormed());
	}
	
	public void test14() {
		Node n1 = newNode(e2, null, null);
		Node n2 = newNode(e4, n1, null);
		setRoot(n2);
		setManyItems(2);
		assertWellFormed(true);
		
		iterator = new Iterator(); // null for cursor and nextCursor
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setNextCursor(n1);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setNextCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(true, () -> iterator.wellFormed());
	}
	
	public void test15() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator(); // null for cursor and nextCursor
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n4);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n5);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(true, () -> iterator.wellFormed());
	}
	
	public void test16() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		iterator.setCursor(n1);
		
		iterator.setNextCursor(n1);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n4);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n5);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(false, () -> iterator.wellFormed());
	}	
	
	public void test17() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		iterator.setCursor(n2);
		
		iterator.setNextCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n4);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n5);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(false, () -> iterator.wellFormed());
	}	

	public void test18() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		iterator.setCursor(n3);
		
		iterator.setNextCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n4);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n5);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(false, () -> iterator.wellFormed());
	}	
	
	public void test19() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		iterator.setCursor(n4);
		
		iterator.setNextCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n4);
		assertReporting(true, () -> iterator.wellFormed());

		iterator.setNextCursor(n5);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(false, () -> iterator.wellFormed());
	}	
	
	public void test20() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		iterator.setCursor(n5);
		
		iterator.setNextCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n4);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n5);
		assertReporting(true, () -> iterator.wellFormed());
		
		iterator.setNextCursor(null);
		assertReporting(true, () -> iterator.wellFormed());
	}	
	
	public void test21() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		Node n2a = newNode(e1, null, null);
		iterator.setCursor(n2a);
		
		iterator.setNextCursor(null);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n2a);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n3);
		assertReporting(false, () -> iterator.wellFormed());
		
		
		iterator.setCursor(n2);
		assertReporting(true, () -> iterator.wellFormed());
	}
	
	public void test22() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		Node n2a = newNode(e1, null, null);
		iterator.setNextCursor(n2a);

		iterator.setCursor(null);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setCursor(n1);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setCursor(n2);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setCursor(n2a);
		assertReporting(false, () -> iterator.wellFormed());
		
		
		iterator.setColVersion(2);
		assertReporting(true, () -> iterator.wellFormed());
	}
	
	public void test23() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		Node n5a = newNode(e5, n4, null);
		iterator.setCursor(n5a);
		
		iterator.setNextCursor(n5);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(n5a);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setNextCursor(null);
		assertReporting(false, () -> iterator.wellFormed());
		
	}

	public void test24() {
		Node n2 = newNode(e1, null, null);
		Node n1 = newNode(e1, null, n2);
		Node n4 = newNode(e3, null, null);
		Node n5 = newNode(e5, n4, null);
		Node n3 = newNode(e3, n1, n5);
		setRoot(n3);
		setManyItems(5);
		assertWellFormed(true);
		
		iterator = new Iterator();
		Node n5a = newNode(e5, n4, null);
		iterator.setNextCursor(n5a);
		
		iterator.setCursor(null);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setCursor(n4);
		assertReporting(false, () -> iterator.wellFormed());

		iterator.setCursor(n5);
		assertReporting(false, () -> iterator.wellFormed());
		
		iterator.setCursor(n5a);
		assertReporting(false, () -> iterator.wellFormed());
	}


	public void test99() {
		new NewApptBook();
	}
}
