import java.util.LinkedList;
import java.util.Queue;

public class BpTree {
	//Default constructor
	public BpTree() {
		N = 4;
        root = new BLeafNode();
	}
	
	//Constructor with order specified
	public BpTree(int n) {
		N = n;
        root = new BLeafNode();
	}
	
	//Copy constructor, order can be changed for B+ tree copy
	public BpTree(int n, BpTree B) {
		N = n;
		root = new BLeafNode();
			
		copyTree(B.getRoot());
	}
	

	//Insert a new key and its associated value into tree
	public void insert(int key, String value) {
		if (this.find(key) != null) 
			System.out.println("Error: Key (" + key + ") already in tree, not inserted.");
		else {
			BLeafNode leaf = this.findCorrectLeaf(key);
			
			if (leaf.isFull()) {
				BNode node = leaf.fixFullNode();
				if (node != null)
					this.root = node; 
			}
			
			leaf = this.findCorrectLeaf(key);
			leaf.insert(key, value);	
		}
	}
	
	//Search for a key and return its value or null if key is not in tree
	public String find(int key) {
		BLeafNode leaf = this.findCorrectLeaf(key);
		
		int index = leaf.find(key);
		return (index == -1) ? null : leaf.getValue(index);
	}
	

	//Delete a key and its associated value from the tree
	public void remove(int key) {
		BLeafNode leaf = this.findCorrectLeaf(key);
		
		if (leaf.removeKey(key) && leaf.isTooEmpty()) {
			BNode node = leaf.fixNodeTooEmpty();
			if (node != null)
				this.root = node; 
		}
	}
	
	public void printKeys() {
		if (root.keyCount == 0)
			return;
		
		Queue<BNode> q = new LinkedList<BNode>();
		int nodesRemaining = 0;
		q.add(root);
		while (!q.isEmpty()) {
			nodesRemaining = q.size();
			while (nodesRemaining > 0) {
				BNode n = (BNode) q.remove();
				printKeys(n);
				if (n.getNodeType() == "InnerNode") {
					for (int i = 0; i <= ((BInnerNode) n).getKeyCount(); ++i) {
						if (((BInnerNode) n).getChild(i) != null)
							q.add(((BInnerNode) n).getChild(i));
						else
							break;
					}
				}
				--nodesRemaining;
			}
			System.out.println();
		}
	}
	
	public void printValues() {
		if (root.getKeyCount() == 0)
			return;
		
		Queue<BNode> q = new LinkedList<BNode>();
		int nodesRemaining = 0;
		q.add(root);
		while (!q.isEmpty()) {
			nodesRemaining = q.size();
			while (nodesRemaining > 0) {
				BNode n = (BNode) q.remove();
				if (n.getNodeType() == "LeafNode")
					printValues((BLeafNode) n);
				else {
					for (int i = 0; i <= ((BInnerNode) n).getKeyCount(); ++i) {
						if (((BInnerNode) n).getChild(i) != null)
							q.add(((BInnerNode) n).getChild(i));
						else
							break;
					}
				}
				--nodesRemaining;
			}
		}
		System.out.println();
	}
	
	private BNode root; //Pointer to root
	private final int N; //Maximum number of keys in interior and leaf nodes
	
	private BLeafNode findCorrectLeaf(int key) {
		BNode node = this.root;
		while (node.getNodeType() == "InnerNode") {
			node = ((BInnerNode) node).getChild(node.find(key));
		}
		
		return (BLeafNode) node;
	}
	
	private BNode getRoot() {
		return this.root;
	}
	
	private void copyTree(BNode node) {
		if (node.getKeyCount() == 0)
			return;
		
		Queue<BNode> q = new LinkedList<BNode>();
		int nodesRemaining = 0;
		q.add(node);
		while (!q.isEmpty()) {
			nodesRemaining = q.size();
			while (nodesRemaining > 0) {
				BNode n = (BNode) q.remove();
				if (n.getNodeType() == "LeafNode") {
					for (int i = 0; i < ((BLeafNode) n).getKeyCount(); ++i)
						insert(((BLeafNode) n).getKey(i), ((BLeafNode) n).getValue(i));
				} else {
					for (int i = 0; i <= ((BInnerNode) n).getKeyCount(); ++i) {
						if (((BInnerNode) n).getChild(i) != null)
							q.add(((BInnerNode) n).getChild(i));
						else
							break;
					}
				}
				--nodesRemaining;
			}
		}
	}
	
	private void printKeys(BNode node) {
		int i;
		System.out.print(" [");
		for (i = 0; i < node.getKeyCount() - 1; ++i)
			System.out.print(node.getKey(i) + ",");
		System.out.print(node.getKey(i) + "]");
	}
	
	private void printValues(BNode node) {
		int i;
		System.out.print(" [");
		for (i = 0; i < ((BLeafNode) node).getKeyCount() - 1; i++)
			System.out.print(((BLeafNode) node).getValue(i) + ",");
		System.out.print(((BLeafNode) node).getValue(i) + "]");
	}
	
	abstract class BNode {
		protected int[] keys;
		protected int keyCount;
		protected BNode pNode;
		protected BNode lSibling;
		protected BNode rSibling;
		protected String nodeType;
		
		protected BNode() {
			this.keyCount = 0;
			this.pNode = null;
			this.lSibling = null;
			this.rSibling = null;
			this.nodeType = "Default";
		}
		
		public int getKeyCount() {
			return this.keyCount;
		}
		
		public int getKey(int index) {
			return this.keys[index];
		}
		
		public void setKey(int index, int key) {
			this.keys[index] = key;
		}
		
		public BNode getParent() {
			return this.pNode;
		}
		
		public void setParent(BNode parent) {
			this.pNode = parent;
		}
		
		public abstract String getNodeType();
		
		//Search for a key and return it's value if found, and -1 if not found
		public abstract int find(int key);
		
		// The following are helper functions for the insertion operation
		
		//Function returns true if node is full, false otherwise
		public boolean isFull() {
			return this.getKeyCount() == this.keys.length;
		}
		
		// Function to deal with case when node is full
		public BNode fixFullNode() {
			int middleIndex = (this.keys.length - 1) / 2; //Middle index of full node
			int KeyUp = this.getKey(middleIndex); //Key to get pushed up tree
			
			BNode newNode = this.split(); //Split the full node
			
			if (this.getParent() == null) {
				this.setParent(new BInnerNode());
			}
			newNode.setParent(this.getParent());
			
			// The following functions maintain the sibling links for the split node
			newNode.setLSibling(this);
			newNode.setRSibling(this.rSibling);
			if (this.getRSibling() != null)
				this.getRSibling().setLSibling(newNode);
			this.setRSibling(newNode);
			
			//Push up the middle key from the split node to its parent
			return this.getParent().pushKeyUp(KeyUp, this, newNode);
		}
		
		protected abstract BNode split();
		protected abstract BNode pushKeyUp(int key, BNode leftNode, BNode rightNode);
		//End insertion helper functions
		
		//The following are helper functions for the remove operation
		
		//This function returns true if node has too few keys and false otherwise
		public boolean isTooEmpty() {
			return this.getKeyCount() < (this.keys.length / 2);
		}
		
		//This function returns true if sibling of node with too few keys can lend a key
		public boolean canGiveKey() {
			return this.getKeyCount() > (this.keys.length / 2);
		}
		
		/*
		 * Checks if left sibling of node is not null, checks whether both nodes have same parent
		 * If both conditions are met this function returns the left sibling
		*/
		public BNode getLSibling() {
			if ((this.lSibling != null) && (this.lSibling.getParent() == this.getParent()))
				return this.lSibling;
			return null;
		}

		//Sets left sibling of node
		public void setLSibling(BNode sibling) {
			this.lSibling = sibling;
		}

		/*
		 * Checks if right sibling of node is not null, checks whether both nodes have same parent
		 * If both conditions are met this function returns the right sibling
		*/
		public BNode getRSibling() {
			if ((this.rSibling != null) && (this.rSibling.getParent() == this.getParent()))
				return this.rSibling;
			return null;
		}

		//Sets right sibling
		public void setRSibling(BNode silbling) {
			this.rSibling = silbling;
		}
		
		//Function fixes the node that is has too few keys
		public BNode fixNodeTooEmpty() {
			if (this.getParent() == null)
				return null;
			
			//Check whether left sibling can give key
			BNode leftSibling = this.getLSibling();
			if ((leftSibling != null) && (leftSibling.canGiveKey())) {
				this.getParent().processGiveKey(this, leftSibling, leftSibling.getKeyCount() - 1);
				return null;
			}
			
			//Check whether right sibling can give a key
			BNode rightSibling = this.getRSibling();
			if ((rightSibling != null) && (rightSibling.canGiveKey())) {
				this.getParent().processGiveKey(this, rightSibling, 0);
				return null;
			}
			
			//Neither siblings can give a key, need to merge nodes
			if (leftSibling != null) {
				return this.getParent().processMerge(leftSibling, this);
			}
			else {
				return this.getParent().processMerge(this, rightSibling);
			}
		}
		
		protected abstract void processGiveKey(BNode TakerNode, BNode GiverNode, int Index);
		protected abstract BNode processMerge(BNode lChild, BNode rChild);
		protected abstract void Merge(int sinkKey, BNode rightSibling);
		protected abstract int giveKey(int sinkKey, BNode sibling, int borrowIndex);
		//End deletion helper functions
	}
	
	class BLeafNode extends BNode {
		private String[] values;
		
		protected BLeafNode() {
			this.keys = new int[N];
			this.values = new String[N];
			this.nodeType = "LeafNode";
		}
		
		public String getValue(int index) {
			return this.values[index];
		}
		
		public void setValue(int index, String value) {
			this.values[index] = value;
		}
		
		public String getNodeType() {
			return this.nodeType;
		}
		
		/*
		 * Function returns index of key if found in leaf node
		 * Returns -1 if not found in leaf node
		*/
		public int find(int key) {
			for (int i = 0; i < this.getKeyCount(); ++i) {
				 if (this.getKey(i) == key) {
					 return i;
				 }
			}
			
			return -1;
		}
		
		//The following functions are for the insertion operation for a leaf node
		/*
		 * This function will only insert a key in a leaf node if it does not already exist in the tree 
		*/
		public void insert(int key, String value) {
			int index = 0;
			while ((index < this.getKeyCount()) && (this.getKey(index) < key))
				++index;
			this.insertAt(index, key, value);
		}
		
		//Insert key at "index" position
		private void insertAt(int index, int key, String value) {
			//Create space for new key
			for (int i = this.getKeyCount() - 1; i >= index; --i) {
				this.setKey(i + 1, this.getKey(i));
				this.setValue(i + 1, this.getValue(i));
			}
			
			//Insert new key and value
			this.setKey(index, key);
			this.setValue(index, value);
			++this.keyCount;
		}
		
		//Split operation for leaf node
		protected BNode split() {
			int middleIndex = (this.getKeyCount() + 1) / 2; //Get mid-point in node to be split
			
			BLeafNode newNode = new BLeafNode(); //Create new node to put half of the keys from full node
			for (int i = 0; i < this.getKeyCount() - middleIndex; ++i) {
				newNode.setKey(i, this.getKey(i + middleIndex));
				newNode.setValue(i, this.getValue(i + middleIndex));
				this.setKey(i + middleIndex, 0);
				this.setValue(i + middleIndex, null);
			}
			newNode.keyCount = this.getKeyCount() - middleIndex;
			this.keyCount = middleIndex;
			
			return newNode;
		}
		
		//pushKeyUp is not supported for leaf node, it is defined for an inner node
		protected BNode pushKeyUp(int key, BNode leftNode, BNode rightNode) {
			throw new UnsupportedOperationException();
		}
		//End insertion functions
		
		//The following functions are for the deletion operation for a leaf node
		/*
		 * If key is not in tree than this function returns false, true otherwise 
		*/
		public boolean removeKey(int key) {
			int index = this.find(key);
			if (index == -1)
				return false;
			
			this.deleteAt(index);
			return true;
		}
		
		/*
		 * Delete key at "index" position 
		*/
		private void deleteAt(int index) {
			int i = index;
			for (i = index; i < this.getKeyCount() - 1; ++i) {
				this.setKey(i, this.getKey(i + 1));
				this.setValue(i, this.getValue(i + 1));
			}
			this.setKey(i, 0);
			this.setValue(i, null);
			--this.keyCount;
		}
		
		/*
		 * processGiveKey function not supported for leaf node, defined for inner node 
		*/
		protected void processGiveKey(BNode takerNode, BNode giverNode, int Index) {
			throw new UnsupportedOperationException();
		}
		
		/*
		 * processMerge function not supported for leaf node, defined for inner node 
		*/
		protected BNode processMerge(BNode leftChild, BNode rightChild) {
			throw new UnsupportedOperationException();
		}
		
		/*
		 * This function merges two leaf nodes together when one of them has too few nodes
		 */
		protected void Merge(int keyDown, BNode rightSibling) {
			BLeafNode siblingLeaf = (BLeafNode) rightSibling;
			
			int j = this.getKeyCount();
			for (int i = 0; i < siblingLeaf.getKeyCount(); ++i) {
				this.setKey(j + i, siblingLeaf.getKey(i));
				this.setValue(j + i, siblingLeaf.getValue(i));
			}
			this.keyCount += siblingLeaf.getKeyCount();
			
			this.setRSibling(siblingLeaf.rSibling);
			if (siblingLeaf.rSibling != null)
				siblingLeaf.rSibling.setLSibling(this);
		}
		
		/*
		 * This function takes a key from a sibling and gives it too the node that has too few keys 
		*/
		protected int giveKey(int keyDown, BNode sibling, int Index) {
			BLeafNode siblingNode = (BLeafNode) sibling;
			
			this.insert(siblingNode.getKey(Index), siblingNode.getValue(Index)); //Insert key from sibling into node that has too few keys
			siblingNode.deleteAt(Index); //Delete key from not that gave it to node that had too few keys
			
			return Index == 0 ? sibling.getKey(0) : this.getKey(0);
		}
		//End deletion operations
	}
	
	class BInnerNode extends BNode {
		protected BNode[] children; 
		
		protected BInnerNode() {
			this.keys = new int[N];
			this.children = new BNode[N + 1];
			this.nodeType = "InnerNode";
		}
		
		public BNode getChild(int index) {
			return this.children[index];
		}
		
		public void setChild(int index, BNode child) {
			this.children[index] = child;
			if (child != null)
				child.setParent(this);
		}
		
		public String getNodeType() {
			return this.nodeType;
		}
		
		//This function determines the node that you need to go to next to find correct leaf node
		public int find(int key) {
			int index;
			for (index = 0; index < this.getKeyCount(); ++index) {
				if (key <= this.getKey(index))
					return index;
			}
			
			return index;
		}
		
		//The following functions are for the insertion operation for an inner node
		
		//This function inserts a new key at "index" position in an inner node
		private void insertAt(int index, int key, BNode leftChild, BNode rightChild) {
			// Create space for new key
			for (int i = this.getKeyCount() + 1; i > index; --i) {
				this.setChild(i, this.getChild(i - 1));
			}
			for (int i = this.getKeyCount(); i > index; --i) {
				this.setKey(i, this.getKey(i - 1));
			}
			
			// Insert the new key
			this.setKey(index, key);
			this.setChild(index, leftChild);
			this.setChild(index + 1, rightChild);
			++this.keyCount;
		}
		
		/*
		 * This function deals with when a node has been split
		 * In this case the middle key is kicked out and pushed up to the parent
		*/
		protected BNode split() {
			int middleIndex = this.getKeyCount() / 2;
			
			BInnerNode newNode = new BInnerNode();
			for (int i = middleIndex + 1; i < this.getKeyCount(); ++i) {
				newNode.setKey(i - middleIndex - 1, this.getKey(i));
				this.setKey(i, 0);
			}
			for (int i = middleIndex + 1; i <= this.getKeyCount(); ++i) {
				newNode.setChild(i - middleIndex - 1, this.getChild(i));
				newNode.getChild(i - middleIndex - 1).setParent(newNode);
				this.setChild(i, null);
			}
			this.setKey(middleIndex, 0);
			newNode.keyCount = this.getKeyCount() - middleIndex - 1;
			this.keyCount = middleIndex;
			
			return newNode;
		}
		
		protected BNode pushKeyUp(int key, BNode leftChild, BNode rightNode) {
			// Find position where new key should be inserted
			int index = this.find(key);
			
			// Insert the new key at position "index"
			this.insertAt(index, key, leftChild, rightNode);

			// Check whether the node now needs to be split
			if (this.isFull()) {
				return this.fixFullNode();
			}
			else {
				return this.getParent() == null ? this : null;
			}
		}
		//End insertion operations
		
		//The following functions are for the deletion operation for an inner node
		
		/*
		 * Delete key at position "index" 
		*/
		private void deleteAt(int index) {
			int i = 0;
			for (i = index; i < this.getKeyCount() - 1; ++i) {
				this.setKey(i, this.getKey(i + 1));
				this.setChild(i + 1, this.getChild(i + 2));
			}
			this.setKey(i, 0);
			this.setChild(i + 1, null);
			--this.keyCount;
		}
		
		
		protected void processGiveKey(BNode takerNode, BNode giverNode, int Index) {
			int takerChildIndex = 0;
			while ((takerChildIndex < this.getKeyCount() + 1) && (this.getChild(takerChildIndex) != takerNode))
				++takerChildIndex;
			
			if (Index == 0) {
				// Take key from right sibling
				int KeyUp = takerNode.giveKey(this.getKey(takerChildIndex), giverNode, Index);
				this.setKey(takerChildIndex, KeyUp);
			}
			else {
				// borrow a key from left sibling
				int KeyUp = takerNode.giveKey(this.getKey(takerChildIndex - 1), giverNode, Index);
				this.setKey(takerChildIndex - 1, KeyUp);
			}
		}
		
		protected BNode processMerge(BNode leftChild, BNode rightChild) {
			int index = 0;
			while (index < this.getKeyCount() && this.getChild(index) != leftChild)
				++index;
			int KeyDown = this.getKey(index);
			
			// Merge two children and the key that has to move down
			leftChild.Merge(KeyDown, rightChild);
			
			// Remove the key that moves down, keep the left child and abandon the right child
			this.deleteAt(index);
			
			// Check whether the need to take a key or merge nodes needs to propagate further up the tree
			if (this.isTooEmpty()) {
				if (this.getParent() == null) {
					// Current node is root, here we either remove keys or if none are left delete the entire root node
					if (this.getKeyCount() == 0) {
						leftChild.setParent(null);
						return leftChild;
					}
					else {
						return null;
					}
				}
				
				return this.fixNodeTooEmpty();
			}
			
			return null;
		}
		
		protected void Merge(int KeyDown, BNode rightSibling) {
			BInnerNode rightSiblingNode = (BInnerNode) rightSibling;
			
			int j = this.getKeyCount();
			this.setKey(j++, KeyDown);
			
			for (int i = 0; i < rightSiblingNode.getKeyCount(); ++i) {
				this.setKey(j + i, rightSiblingNode.getKey(i));
			}
			for (int i = 0; i < rightSiblingNode.getKeyCount() + 1; ++i) {
				this.setChild(j + i, rightSiblingNode.getChild(i));
			}
			this.keyCount += 1 + rightSiblingNode.getKeyCount();
			
			this.setRSibling(rightSiblingNode.rSibling);
			if (rightSiblingNode.rSibling != null)
				rightSiblingNode.rSibling.setLSibling(this);
		}

		protected int giveKey(int DownKey, BNode sibling, int Index) {
			BInnerNode siblingNode = (BInnerNode) sibling;
			
			int KeyUp = 0;
			if (Index == 0) {
				// Take the first key from right sibling, append it to tail
				int index = this.getKeyCount();
				this.setKey(index, DownKey);
				this.setChild(index + 1, siblingNode.getChild(Index));			
				this.keyCount += 1;
				
				KeyUp = siblingNode.getKey(0);
				siblingNode.deleteAt(Index);
			}
			else {
				// Take the last key from left sibling, insert it to head
				this.insertAt(0, DownKey, siblingNode.getChild(Index + 1), this.getChild(0));
				KeyUp = siblingNode.getKey(Index);
				siblingNode.deleteAt(Index);
			}
			
			return KeyUp;
		}
		//End deletion operations	
	}
}