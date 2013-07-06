/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.corpus;

import java.util.Arrays;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProjectivityChecker {
	
	private static final int INITIAL_EDGE_BUFFER_SIZE = 3;
	
	private static final int LTR = 0;
	private static final int RTL = 1;

	/* 
	 * "Tree" representation:
	 * 
	 * Every entry is a list of child indices with the first value 
	 * being the number of current children
	 */
	int[][] edges;
	
	// Copy of supplied heads array
	int[] heads;
	
	/*
	 *  Marks the left and right index of an edge as encountered 
	 *  during the top-down traversal.
	 *  [0] = left_bound
	 *  [1] = right_bound
	 *  [2] = direction indicator: 0 = ltr, 1 =rtl
	 */
	int[][] hbounds;
	
	// Stores current index within child indices list of each node
	int[] tracking;
	
	int root;
	int size;
	
	int bufferSize = -1;
	
	boolean[] result;
	
	boolean projective;
	
	protected int rootValue = -1;

	public ProjectivityChecker() {
		// no-op
	}

	public ProjectivityChecker(int bufferSize) {
		edges = new int[bufferSize][];
		heads = new int[bufferSize];
		hbounds = new int[bufferSize][];
		result = new boolean[bufferSize];
		tracking = new int[bufferSize];
		
		this.bufferSize = bufferSize;
	}
	
	public void init(int[] head_array) {
		
		size = head_array.length;
		
		// Ensures sufficient start-up capacity instead
		// of slowboating to usable size
		int len = Math.max(size, 50);

		if(bufferSize==-1 || bufferSize<len) {
			bufferSize = len*2;
			
			heads = new int[bufferSize];
			result = new boolean [bufferSize];
			tracking = new int[bufferSize];
			
			edges = new int[bufferSize][];
			hbounds = new int[bufferSize][];			
		}
		
		// Reset root
		root = -1;
		
		// Clear important buffers
		for (int i = 0; i < size; i++) {			
			heads[i] = head_array[i];
			tracking[i] = -1;
			
			int[] list = edges[i];
			if(list!=null) {
				list[0] = 0;
			}
		}
		
		// Build tree
		for (int i = 0; i < size; i++) {
			int head = heads[i];
			if(head == rootValue) {
				if(root!=-1)
					throw new IllegalArgumentException("Duplicate root at index "+i+" after first root was "+root); //$NON-NLS-1$ //$NON-NLS-2$
				root = i;
			} else if (head<0 || head>=size) {
				throw new IllegalArgumentException("Head value at index "+i+" out of bounds: "+head); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				//System.out.printf("saving head %d for node %d\n", head, i);
				int[] list = edges[head];
				if (list == null) {
					list = new int[INITIAL_EDGE_BUFFER_SIZE];
					edges[head] = list;
				} else if (list[0] >= list.length - 1) {
					edges[head] = Arrays.copyOf(list, list.length+list.length);
				}

				list[0]++;
				list[list[0]] = i;
			}
		}
		
		/*
		 * Projectivity constraints:
		 * (w_i,w_j) is projective when w_i is ancestor of
		 * all nodes in the span w_i+1 to w_j
		 * 
		 * Algorithm:
		 * 
		 *	1.	Traverse tree top-down following edges pre-order
		 * 	2. 	Mark start and end index of edge in bounds array
		 * 		plus direction of edge (ltr vs. rtl)
		 *		
		 * 
		 */
		
		// Populate bounds array
		int node = root;
		int depth = 0;		
		while(true) {	
			int[] list = edges[node];
			int target = ++tracking[node];
			
			if(list==null || target>=list[0]) {
				node = heads[node];
			} else {
				target = list[target+1];
				int[] bound = hbounds[depth];
				if(bound==null) {
					hbounds[depth] = bound = new int[3];
				}
				bound[0] = node<target ? node : target;
				bound[1] = node>target ? node : target;
				bound[2] = node<target ? LTR : RTL;
				
				node = target;
				depth++;
				
				if(depth>=size-1)
					break;
			}
		}
		
		 
		// Now check projectivity
		
		dumpBounds();
	}
	
	private void dumpBounds() {
		char[] buffer = new char[size];
		
		for(int i=0; i<size-1; i++) {
			int[] bound = hbounds[i];
			
			for(int k=0; k<size; k++) {
				if(k<bound[0] || k>bound[1]) {
					buffer[k] = ' ';
				} else if(k==bound[0]) {
					buffer[k] = bound[2]==RTL ? '<' : '|';
				} else if(k==bound[1]) {
					buffer[k] = bound[2]==LTR ? '>' : '|';
				} else {
					buffer[k] = '-';
				}
			}
			
			System.out.println(buffer);
		}
	}
	
	public static void main(String[] args) {
		ProjectivityChecker pc = new ProjectivityChecker();
		
		int[] head_array = new int[]{-1, 4, 1, 6, 0, 2, 5};
		
		pc.init(head_array);
	}
}
