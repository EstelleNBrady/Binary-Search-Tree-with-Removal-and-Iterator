// This is an assignment for students to complete after reading Chapter 3 of
// "Data Structures and Other Objects Using Java" by Michael Main.

package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import java.util.NoSuchElementException;

import java.util.function.Consumer;
import edu.uwm.cs.junit.LockedTestCase;


//Estelle Brady
//CS 351 - 401
//Collaborated with Miguel Garcia, Julian Moreno, Christian Ortega and tutoring with Matt

/******************************************************************************
 * This class is a homework assignment;
 * A NewApptBook ("book" for short) is a collection of Appointment objects in sorted order.
 ******************************************************************************/

public class NewApptBook extends AbstractCollection<Appointment> implements Cloneable {
	// TODO: Declare the data structure
	private int manyItems;
	private Node root;
	private int version;

	private static class Node{
		Appointment data;
		Node left;
		Node right;
		
		Node(Appointment data){
			this.data = data;
			left = null;
			right = null;
		}
	}

	private static Consumer<String> reporter = (s) -> { System.err.println("Invariant error: " + s); };
	
	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}

	// TODO: Helper methods.  You are free to copy from Homework #8
	// (your code or the solution)
	
	private boolean wellFormed() {
		// Check the invariant.
		// Invariant: (simpler than in Homework #8
		// 1. The tree must have height bounded by the number of items
		if(checkHeight(root, manyItems) == false)
			return report("The tree must have height bounded by the number of items");
		// 2. The number of nodes must match manyItems
		if(manyItems != countNodes(root))
			return report("The number of nodes must match manyItems");
		// 3. Every node's data must not be null and be in range.
		if(allInRange(root, null, null) == false)
			return false;
	
		// If no problems found, then return true:
		return true;
	}

	// This is only for testing the invariant.  Do not change!
	private NewApptBook(boolean testInvariant) { }

	/**
	 * Initialize an empty book. 
	 **/   
	public NewApptBook( )
	{
		root = null;
		manyItems = 0;
		assert wellFormed() : "invariant failed at end of constructor";
	}
	

	/**
	 * Add a new element to this book, in order.  If an equal appointment is already
	 * in the book, it is inserted after the last of these. 
	 * The current element (if any) is not affected.
	 * @param element
	 *   the new element that is being added, must not be null
	 * @param Node r 
	 * 	the node that we are using
	 * @postcondition
	 *   A new copy of the element has been added to this book. The current
	 *   element (whether or not is exists) is not changed.l
	 *  @return r
	 **/
	
	//taken from notes in class
	private Node insertHelper(Node r, Appointment Helement) {
		if(r == null) {
			return new Node(Helement);
		}
		if(Helement.compareTo(r.data) < 0) {
			r.left = insertHelper(r.left, Helement);
		}else {
			r.right = insertHelper(r.right, Helement);
		}
		return r;
	}
	
	@Override // implementation
	public boolean add(Appointment element)
	{
		assert wellFormed() : "invariant failed at start of insert";
		
		if(element == null)	throw new NullPointerException("element cannot be null in insert");
		
		root = insertHelper(root, element);
		//increment manyItems
		++manyItems;
		++version;
		
		assert wellFormed() : "invariant failed at end of insert";
		return false;
	}
	
	@Override//required
	public int size() {
		return manyItems;
	}

	// other methods of the main class.
	// You should not need to suppress *any* warnings in any code
	// you write this week.
	
	@Override // required
	public Iterator<Appointment> iterator() {
		return new MyIterator();
	}
	
	
	/** sets the iterator after this appointment
	 * @throws NullPointerException
	 * @returns iterator at specific appointment
	 */
	public Iterator<Appointment> iterator(Appointment appt) {
		assert wellFormed() : "invariant failed at start of remove";

		//can't use if the appointment is null
		if(appt == null)
			throw new NullPointerException("n");
		return new MyIterator(appt);
		
	}
	
//HELPER METHODS
	
	/**
	 * Return the number of nodes in a subtree that has no cycles.
	 * @param r root of the subtree to count nodes in, may be null
	 * @return number of nodes in subtree
	 */
	//referenced this video
	//https://www.youtube.com/watch?v=1YB9DzLfj_0&list=PLt4nG7RVVk1hXIW5w54uKEN25MqHNS7_A&index=5
	private int countNodes(Node r) {
		if(r == null)
			manyItems = 0;
		else
			manyItems = countNodes(r.left)+countNodes(r.right)+1;
		return manyItems;
	}
	
	/**
	 * Return whether all the nodes in the subtree are in the given range,
	 * and also in their respective subranges.
	 * @param r root of subtree to check, may be null
	 * @param lo inclusive lower bound, may be null (no lower bound)
	 * @param hi exclusive upper bound, may be null (no upper bound)
	 * @return
	 */
	//helped by TA Anjali
	private boolean allInRange(Node r, Appointment lo, Appointment hi) {
		if(r == null)	return true;
		
		//if the data is null, throw a report
		if(r.data == null)
			return report("data cannot be null");
		
		//if the high is not infinite
		//and the data is equal to or greater than the high
		if(hi!=null)
			if(r.data.compareTo(hi) >= 0)
				return report("low cannot come before high");
		
		//if the low is not infinite
		//and the data is less than the low
		if(lo!=null)
			if(r.data.compareTo(lo) <0)
				return report("high cannot come before low");
		
		//if both statements are true then return 
		return allInRange(r.left, lo, r.data) && allInRange(r.right, r.data, hi);
		}
	
	/**
	 * Return true if the given subtree has height no more than a given bound.
	 * In particular if the "tree" has a cycle, then false will be returned
	 * since it has unbounded height.
	 * @param r root of subtree to check, may be null
	 * @param max maximum permitted height (null has height 0)
	 * @return whether the subtree has at most this height
	 */
	//tutor Matt helped me with this method and Professor Boyland
	private boolean checkHeight(Node r, int max) {
		//r can be null as long as the max is 0 or greater
		if(r == null && max >= 0)	return true;
		//max cannot be equal to 0 if r is not null
		if(max < 0) return false;
		
		
		return checkHeight(r.left, max-1) && checkHeight(r.right, max-1);
	}
	
	//used this video:
	//https://www.youtube.com/watch?v=8K7EO7s_iFE
	private Node removeHelper(Node r, Node reElement) {
		
	//version changes	
	++version;
	int compareNum = r.data.compareTo(reElement.data);

	//if is is on the left side of the tree
	if(compareNum > 0) r.left = removeHelper(r.left, reElement);

	//if it is on the right side
	else if(compareNum < 0) r.right = removeHelper(r.right, reElement);
					
	//if it is equal
	else {
		//if it only has a right child
		if(r.left == null) r = r.right;
		//if it only has a left subtree
		else if(r.right == null) r = r.left;
		
		//if it has both children, we use the left subtree
		//we want the left one to always take over
	
        else{
        	//if we have 2 child nodes
            //change the data
            r.data = firstInTree(r.right).data;
            //right subtree
            r.right = removeHelper(r.right, r);
        	}
    	}
	return r;
	}	


	//mostly from hw 5 solution
	@Override //efficiency
	public void clear() {
		assert wellFormed() : "invariant failed at start of insert";
		if(manyItems == 0) return;
		++version;
		manyItems = 0;
		root = null;
		assert wellFormed() : "invariant failed at start of insert";	
	}
	
	/**
	 * Generate a copy of this book.
	 * @return
	 *   The return value is a copy of this book. Subsequent changes to the
	 *   copy will not affect the original, nor vice versa.
	 **/ 

	//used this link to help me with clone/clone helper
	//https://interview.hackingnote.com/en/problems/clone-binary-tree
	//got help from Matt
	private Node cloneHelper(Node r, NewApptBook answer) {
	    if (r == null)
	        return null;
	      Node newNode = new Node(r.data); //must create a new node
	      //traverse to the left and right
	      newNode.left = cloneHelper(r.left, answer);
	      newNode.right = cloneHelper(r.right, answer);
	
	      return newNode;
	}
	
	@Override // extends implementation
	public NewApptBook clone( ) { 
		assert wellFormed() : "invariant failed at start of clone";
		NewApptBook answer;
	
		try
		{
			answer = (NewApptBook) super.clone( );
		}
		catch (CloneNotSupportedException e)
		{  // This exception should not occur. But if it does, it would probably
			// indicate a programming error that made super.clone unavailable.
			// The most common error would be forgetting the "Implements Cloneable"
			// clause at the start of this class.
			throw new RuntimeException
			("This class does not implement Cloneable");
		}
		//set manyItems
		answer.manyItems = manyItems;
		//use the helper
		answer.root= cloneHelper(this.root, answer);
	
		assert wellFormed() : "invariant failed at end of clone";
		assert answer.wellFormed() : "invariant on answer failed at end of clone";
		return answer;
	}
	
	@Override // efficiency 
	public boolean remove(Object o) {
		assert wellFormed() : "invariant failed at start of remove";
		
		//if it is not an instance, then we call super.rempoce
		
		//since we are removing an object, we have to make sure
		//we actually have it in our tree
		if(!contains(o))	return false;
	
		Node r = nextInTree(root, (Appointment)o, true, null);
		root = removeHelper(root, r);
		
		//decrement manyItems and increment version
		++version;
		--manyItems;
		
		assert wellFormed() : "invariant failed at end of remove";
		return true;
	}
	/**
	 * Place all the appointments of another book (which may be the
	 * same book as this!) into this book in order as in {@link #insert}.
	 * The elements should added one by one.
	 * @param Node r
	 *   a book whose contents will be placed into this book
	 * @precondition
	 *   The parameter, addend, is not null. 
	 * @postcondition
	 *   The elements from addend have been placed into
	 *   this book. The current el;ement (if any) is
	 *   unchanged.
	 **/
	//helped by tutor Matt
	private void addAllHelper(Node r) {
		if(r == null) 
			return;
		add(r.data);
		addAllHelper(r.left);
		addAllHelper(r.right);
		}
	

	//help by tutor Matt
	@Override // efficiency
	public boolean addAll(Collection<? extends Appointment> addend) {
		// TODO Auto-generated method stub
		if(addend == null)
			throw new NullPointerException("nope");
		if(!(addend instanceof NewApptBook))
			return super.addAll(addend);
		if(addend == this)	addend =((NewApptBook)addend).clone();
		
		addAllHelper(((NewApptBook)addend).root);
		
		assert wellFormed() : "invariant failed at end of insertAll";
		assert ((NewApptBook) addend).wellFormed() : "invariant of addend broken in insertAll";
		return true;
		}
	/**
	 * Find the node that has the appt (if acceptEquivalent) or the first thing
	 * after it.  Return that node.  Return the alternate if everything in the subtree
	 * comes before the given appt.
	 * @param r subtree to look into, may be null
	 * @param appt appointment to look for, must not be null
	 * @param acceptEquivalent whether we accept something equivalent.  Otherwise, only
	 * appointments after the appt are accepted.
	 * @param alt what to return if no node in subtree is acceptable.
	 * @return node that has the first element equal (if acceptEquivalent) or after
	 * the appt.
	 */
	private Node nextInTree(Node r, Appointment appt, boolean acceptEquivalent, Node alt) {
		// TODO: recursion not required, but is simpler
        
        if(r != null) {
            // if appt is equal to r
            if(appt.compareTo(r.data) == 0) {
                if(acceptEquivalent) return r;
                else {
                    // find the successor
                    // if r has a right child then get the left most of it
                    // consider about duplicate elements
                    return nextInTree(r.right, appt, acceptEquivalent, alt);
                }
            } 
            // if appt comes before r
            else if(appt.compareTo(r.data) < 0) {
                // whenever we go to the left we know that the root is our next element
                // unless there is a right child
                // go to the left node
                return nextInTree(r.left, appt, acceptEquivalent, r);
            } 
            // if appt comes after r
            else {
                // go to the right node
                return nextInTree(r.right, appt, acceptEquivalent, alt);
            } 
        }
        return alt;
	}
	
	//hw 3 solution
	@Override //efficiency
	public boolean contains(Object o) {
		assert wellFormed() : "invariant failed at end of insertAll";

		if(!(o instanceof Appointment))	return false;
		Appointment a = (Appointment)o;
		Iterator<Appointment> it = iterator(a);
		if(it.hasNext()) {
			Appointment b = it.next();
			if(a.equals(b)) return true;
		}
		assert wellFormed() : "invariant failed at end of insertAll";

		return false;
	}

	/**
	 * Return the first node in a non-empty subtree.
	 * It doesn't examine the data in teh nodes; 
	 * it just uses the structure.
	 * @param r subtree, must not be null
	 * @return first node in the subtree
	 */
	private Node firstInTree(Node r) {
		if(r!= null) {
			while(r.left!=null) {
				r = r.left;
			}
		//null means we are at the end of the loop
		if(r.left == null)
			return r;
		}
		return null; // TODO: non-recursive is fine
	}

	private class MyIterator implements Iterator<Appointment> {
		// TODO data structure and wellFormed
		// NB: don't declare as public or private
		
		Node cursor;
		Node nextCursor;
		int colVersion;
		
		MyIterator(boolean ignored) {} // do not change this
		
		MyIterator() {
			cursor = firstInTree(root);
			nextCursor = cursor;
			colVersion = version;
			
			assert wellFormed() : "invariant failed in iterator constructor";
		}
			
		
		public MyIterator(Appointment appt) {
	
			
			cursor = nextInTree(root, appt, true, null);
			nextCursor = cursor;
			version=colVersion;
			
			assert wellFormed() : "invariant failed at start of specifying";
			}
		
		private boolean wellFormed() {
			
			if(!NewApptBook.this.wellFormed()) return false;
			if(colVersion != version) return true; // not my fault if invariant broken
			if(foundCursor(root) == false)
				return report("cannot find cursor");
			if(cursor == null && nextCursor != null)
				return report("next cursor must");
			
			if(cursor!=null && cursor != nextCursor) {
				if(cursor.right != null) {
					if(nextCursor!= firstInTree(cursor.right))	return report("nextCursor is wrong");
					}else {
						Node b = nextInTree(root, cursor.data, false, null);
						if(nextCursor != b)
							return report("nope");
					}
			}
			return true;
		}


		// TODO: Finish the iterator class

		@Override //required
		public boolean hasNext() {
			checkVersion();
			
			return nextCursor != null;
			
		}

		@Override//required
		public Appointment next() {
			assert wellFormed() : "invariant failed at start of next";
			// TODO Auto-generated method stub
			
			checkVersion();
			if(!hasNext()) throw new NoSuchElementException("there is no next element");

			cursor = nextCursor;
			
			if(cursor.right !=null) {
				nextCursor=	firstInTree(cursor.right);
			}

			//otherwise, we go next in tree
			else {
				nextCursor = nextInTree(root, cursor.data, false, null);
			}

			
			return cursor.data;
		}
		
	
		@Override // implementation
		public void remove() {
			assert wellFormed() : "invariant failed at start of remove";
			checkVersion();
			
			if(cursor == nextCursor)	throw new IllegalStateException("cursor cannot be equal to nextCursor to remove");
			
			root = removeHelper(root, cursor);			

			manyItems--;
			colVersion=version;
			
			cursor = nextCursor;
		
			assert wellFormed() : "invariant failed at end of remove";
		}

		/**
		 * Return whether the cursor was found in the tree.
		 * If the cursor is null, it should always be found since 
		 * a binary search tree has many null pointers in it.
		 * This method doesn't examine any of the data elements;
		 * it works fine even on trees that are badly structured, as long as
		 * they are height-bounded.
		 * @param r subtree to check, may be null, but must have bounded height
		 * @return true if the cursor was found in the subtree
		 */
		private boolean foundCursor(Node r) {
			//means cursor is found
			if(r == cursor)
				return true;
			
			if(r==null) 
				return false;
			
			//if the cursor is found on either side
			return foundCursor(r.right)||foundCursor(r.left);	
		}
		
		
		private void checkVersion() {
			if (colVersion != version) throw new ConcurrentModificationException("stale iterator");
		}


	}
	
	// don't change this nested class:
	public static class TestInvariantChecker extends LockedTestCase {
		protected NewApptBook self;
		protected NewApptBook.MyIterator iterator;

		protected Consumer<String> getReporter() {
			return reporter;
		}
		
		protected void setReporter(Consumer<String> c) {
			reporter = c;
		}
		
		private static Appointment a = new Appointment(new Period(new Time(), Duration.HOUR), "default");
		
		protected class Node extends NewApptBook.Node {
			public Node(Appointment d, Node n1, Node n2) {
				super(a);
				data = d;
				left = n1;
				right = n2;
			}
			public void setLeft(Node l) {
				left = l;
			}
			public void setRight(Node r) {
				right = r;
			}
		}
		
		protected class Iterator extends MyIterator {
			public Iterator(Node n1, Node n2, int v) {
				self.super(false);
				cursor = n1;
				nextCursor = n2;
				colVersion = v;
			}
			
			public Iterator() {
				this(null,null,self.version);
			}
			
			public boolean wellFormed() {
				return super.wellFormed();
			}
			
			public void setColVersion(int cv) {
				colVersion = cv;
			}
			
			public void setCursor(Node c) {
				cursor = c;
			}
			
			public void setNextCursor(Node c) {
				nextCursor = c;
			}
		}
		
		protected Node newNode(Appointment a, Node l, Node r) {
			return new Node(a, l, r);
		}
		
		protected void setRoot(Node n) {
			self.root = n;
		}
		
		protected void setManyItems(int mi) {
			self.manyItems = mi;
		}
		
		protected void setUp() {
			self = new NewApptBook(false);
			self.root = null;
			self.manyItems = 0;
		}

		protected boolean wellFormed() {
			return self.wellFormed();
		}
		
		/// Prevent this test suite from running by itself
		
		public void test() {
			assertFalse("Don't attempt to run this test", true);
		}
	}
}

