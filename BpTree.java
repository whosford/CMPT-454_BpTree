import java.util.LinkedList;
import java.util.Queue;

public class BpTree {
	//Default constructor
	public BpTree() {
		N = 4; //Set max number of keys to be 4
        root = new BLeafNode();
	}
	
	//Constructor with order specified
	public BpTree(int n) {
		N = n; //Set max number of keys to be n
        root = new BLeafNode();
	}
	
	//Copy constructor, order can be changed for B+ tree copy
	public BpTree(int n, BpTree B) {
		N = n; //Set max number of keys to be n
		root = new BLeafNode();
		
		if (B.getRoot().getKeyCount() != 0)
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
					this.setRoot(node); 
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
				this.setRoot(node); 
		}
	}
	
	public void printKeys() {
		if (getRoot().keyCount == 0)
			return;
		
		Queue<BNode> q = new LinkedList<BNode>();
		int nodesRemaining = 0;
		q.add(getRoot());
		while (!q.isEmpty()) {
			nodesRemaining = q.size();
			while (nodesRemaining > 0) {
				BNode n = (BNode) q.remove();
				if (n.getKeyCount() != 0)
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
		if (getRoot().getKeyCount() == 0)
			return;
		
		printValues(getRoot());
	}
	
	private BNode root; //Pointer to root
	private final int N; //Maximum number of keys in interior and leaf nodes
	
	private BNode getRoot() {
		return this.root;
	}
	
	private void setRoot(BNode node) {
		this.root = node;
	}
	
	private void copyTree(BNode node) {
		if (node.getNodeType() == "InnerNode")
			copyTree(((BInnerNode) node).getChild(0));
		else {
			for (int i = 0; i < ((BLeafNode) node).getKeyCount(); ++i)
				insert(((BLeafNode) node).getKey(i), ((BLeafNode) node).getValue(i));
			if (((BLeafNode) node).getNextLeafNode() != null)
				copyTree(((BLeafNode) node).getNextLeafNode());
		}
	}
	
	private BLeafNode findCorrectLeaf(int key) {
		BNode node = this.root;
		while (node.getNodeType() == "InnerNode") {
			node = ((BInnerNode) node).getChild(node.find(key));
		}
		
		return (BLeafNode) node;
	}
	
	private void printKeys(BNode node) {
		int i;
		System.out.print(" [");
		for (i = 0; i < node.getKeyCount() - 1; ++i)
			System.out.print(node.getKey(i) + ",");
		System.out.print(node.getKey(i) + "]");
	}
	
	private void printValues(BNode node) {
		if (node.getNodeType() == "InnerNode")
			printValues(((BInnerNode) node).getChild(0));
		else {
			for (int i = 0; i < ((BLeafNode) node).getKeyCount(); ++i)
				System.out.println(((BLeafNode) node).getValue(i));
			if (((BLeafNode) node).getNextLeafNode() != null)
				printValues(((BLeafNode) node).getNextLeafNode());
		}
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
			return this.getKeyCount() < ((this.keys.length + 1) / 2);
		}
		
		//This function returns true if sibling of node with too few keys can lend a key
		public boolean canGiveKey() {
			return this.getKeyCount() > ((this.keys.length + 1) / 2);
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
			
			//Check whether right sibling can give a key
			BNode rightSibling = this.getRSibling();
			if ((rightSibling != null) && (rightSibling.canGiveKey())) {
				this.getParent().processRedistribute(this, rightSibling, 0);
				return null;
			}
			
			//Check whether left sibling can give key
			BNode leftSibling = this.getLSibling();
			if ((leftSibling != null) && (leftSibling.canGiveKey())) {
				this.getParent().processRedistribute(this, leftSibling, leftSibling.getKeyCount() - 1);
				return null;
			}
			
			//Neither siblings can give a key, need to coalesce nodes
			if (rightSibling != null) {
				return this.getParent().processCoalesce(this, rightSibling);
			}
			else {
				return this.getParent().processCoalesce(leftSibling, this);
			}
		}
		
		protected abstract void processRedistribute(BNode TakerNode, BNode GiverNode, int Index);
		protected abstract BNode processCoalesce(BNode lChild, BNode rChild);
		protected abstract void coalesce(int sinkKey, BNode rightSibling);
		protected abstract int redistribute(int sinkKey, BNode sibling, int borrowIndex);
		//End deletion helper functions
	}
	
	class BLeafNode extends BNode {
		private String[] values;
		private BLeafNode nextLeafNode;
		
		protected BLeafNode() {
			this.keys = new int[N];
			this.values = new String[N];
			this.nodeType = "LeafNode";
			this.setNextLeafNode(null);
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
		
		public BLeafNode getNextLeafNode() {
			return this.nextLeafNode;
		}

		public void setNextLeafNode(BLeafNode node) {
			this.nextLeafNode = node;
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
			int middleIndex = (this.keys.length + 1) / 2; //Get mid-point in node to be split
			
			BLeafNode newNode = new BLeafNode(); //Create new node to put half of the keys from full node
			for (int i = 0; i < this.keys.length - middleIndex; ++i) {
				newNode.setKey(i, this.getKey(i + middleIndex));
				newNode.setValue(i, this.getValue(i + middleIndex));
				this.setKey(i + middleIndex, 0);
				this.setValue(i + middleIndex, null);
			}
			newNode.keyCount = this.getKeyCount() - middleIndex;
			this.keyCount = middleIndex;
			newNode.setNextLeafNode(this.getNextLeafNode());
			this.setNextLeafNode(newNode);
			
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
		 * processRedistribute function not supported for leaf node, defined for inner node 
		*/
		protected void processRedistribute(BNode takerNode, BNode giverNode, int Index) {
			throw new UnsupportedOperationException();
		}
		
		/*
		 * processCoalesce function not supported for leaf node, defined for inner node 
		*/
		protected BNode processCoalesce(BNode leftChild, BNode rightChild) {
			throw new UnsupportedOperationException();
		}
		
		/*
		 * This function coalesces two leaf nodes together when one of them has too few nodes
		*/
		protected void coalesce(int keyDown, BNode leftSibling) {
			BLeafNode siblingLeaf = (BLeafNode) leftSibling;
			BLeafNode siblingLeafLeft = (BLeafNode) siblingLeaf.getLSibling();
			
			for (int i = 0; i < siblingLeaf.getKeyCount(); ++i) {
				this.insert(siblingLeaf.getKey(i), siblingLeaf.getValue(i));
			}
			
			this.setLSibling(siblingLeafLeft);
			if (siblingLeafLeft != null) {
				siblingLeafLeft.setRSibling(this);
				siblingLeafLeft.setNextLeafNode(this);
			}	
		}
		
		/*
		 * This function takes a key from a sibling and gives it too the node that has too few keys 
		*/
		protected int redistribute(int keyDown, BNode sibling, int Index) {
			BLeafNode siblingNode = (BLeafNode) sibling;
			
			this.insert(siblingNode.getKey(Index), siblingNode.getValue(Index)); //Insert key from sibling into node that has too few keys
			siblingNode.deleteAt(Index); //Delete key from node that gave it to node that had too few keys
			
			return Index == 0 ? this.getKey(this.getKeyCount() - 1) : sibling.getKey(Index - 1);
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
		
		private void insertFront(int key, BNode child) {
			for (int i = this.getKeyCount() - 1; i >= 0; --i) {
				this.setKey(i + 1, this.getKey(i));
			}
			for (int i = this.getKeyCount(); i >= 0; --i) {
				this.setChild(i + 1, this.getChild(i));
			}
			this.setKey(0, key);
			this.setChild(0, child);
			++this.keyCount;
		}
		
		/*
		 * This function deals with when a node has been split
		 * In this case the middle key is kicked out and pushed up to the parent
		*/
		protected BNode split() {
			int middleIndex = (this.keys.length + 1) / 2;
			
			BInnerNode newNode = new BInnerNode();
			for (int i = 0; i < this.keys.length - middleIndex; ++i) {
				newNode.setKey(i, this.getKey(i + middleIndex));
				this.setKey(i + middleIndex, 0);
			}
			for (int i = 0; i <= this.keys.length - middleIndex; ++i) {
				newNode.setChild(i, this.getChild(i + middleIndex));
				newNode.getChild(i).setParent(newNode);
				this.setChild(i + middleIndex, null);
			}
			this.setKey(middleIndex, 0);
			newNode.keyCount = this.getKeyCount() - middleIndex;
			this.keyCount = middleIndex;
			
			return newNode;
		}
		
		protected BNode pushKeyUp(int key, BNode leftChild, BNode rightChild) {
			// Find position where new key should be inserted
			int index = this.find(key);

			// Check whether the node now needs to be split
			if (this.isFull()) {
				BInnerNode node = (BInnerNode) this.fixFullNode();
				if (key > this.getKey(this.getKeyCount() - 1)) {
					index = this.getRSibling().find(key);
					BInnerNode rightSibling = (BInnerNode) this.getRSibling();
					rightSibling.insertAt(index, key, leftChild, rightChild);
				}
				else {
					index = this.find(key);
					this.insertAt(index, key, leftChild, rightChild);
				}
				return node;
			}
			else {
				this.insertAt(index, key, leftChild, rightChild);
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
				this.setChild(i, this.getChild(i + 1));
			}
			this.setKey(i, 0);
			this.setChild(i, this.getChild(i + 1));
			this.setChild(i + 1, null);
			--this.keyCount;
		}
		
		
		protected void processRedistribute(BNode takerNode, BNode giverNode, int Index) {
			int takerChildIndex = 0;
			while ((takerChildIndex < this.getKeyCount() + 1) && (this.getChild(takerChildIndex) != takerNode))
				++takerChildIndex;
			
			if (Index == 0) {
				// Take key from right sibling
				int KeyUp = takerNode.redistribute(this.getKey(takerChildIndex), giverNode, Index);
				this.setKey(takerChildIndex, KeyUp);
			}
			else {
				// Take key from left sibling
				int KeyUp = takerNode.redistribute(this.getKey(takerChildIndex - 1), giverNode, Index);
				this.setKey(takerChildIndex - 1, KeyUp);
			}
		}
		
		protected BNode processCoalesce(BNode leftChild, BNode rightChild) {
			int index = 0;
			while (index < this.getKeyCount() && this.getChild(index) != leftChild)
				++index;
			int KeyDown = this.getKey(index);
			
			// Coalesce two children and the key that has to move down
			rightChild.coalesce(KeyDown, leftChild);
			
			// Remove the key that moves down, keep the right child and abandon the left
			this.deleteAt(index);
			
			// Check whether the need to take a key or coalesce nodes needs to propagate further up the tree
			if (this.isTooEmpty()) {
				if (this.getParent() == null) {
					// Current node is root, here we either remove keys or if none are left delete the entire root node
					if (this.getKeyCount() == 0) {
						rightChild.setParent(null);
						return rightChild;
					}
					else {
						return null;
					}
				}
				
				return this.fixNodeTooEmpty();
			}
			
			return null;
		}
		
		protected void coalesce(int KeyDown, BNode leftSibling) {
			BInnerNode siblingNode = (BInnerNode) leftSibling;
			
			int j = siblingNode.getKeyCount();
			for (int i = j - 1; i >= 0; --i) {
				this.insertFront(siblingNode.getKey(i), siblingNode.getChild(i));
			}
			
			this.setLSibling(siblingNode.lSibling);
			if (siblingNode.lSibling != null)
				siblingNode.lSibling.setRSibling(this);	
		}

		protected int redistribute(int DownKey, BNode sibling, int Index) {
			BInnerNode siblingNode = (BInnerNode) sibling;
			
			int KeyUp = 0;
			if (Index == 0) {
				// Take the first key from right sibling, append it to tail
				int index = this.getKeyCount();
				this.setKey(index, this.getRSibling().getKey(Index));
				this.setChild(index, siblingNode.getChild(Index));			
				++this.keyCount;
				
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