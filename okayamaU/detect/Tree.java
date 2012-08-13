package detect;

import java.util.ArrayList;

public class Tree {

	final public String node;
	private Tree[] childNodes;
	private boolean error;

	private Tree parent;

	public boolean isLeaf() {
		return childNodes == null;
	}

	public boolean hasLeaf() {
		if (isLeaf()) {
			return false;
		}

		for (Tree t : childNodes) {
			if (t.isLeaf()) {
				return true;
			}
		}

		return false;
	}

	public Tree[] getChildNodes() {
		return childNodes;
	}

	public int getNumOfChildNodes() {
		if (childNodes == null) {
			return 0;
		} else {
			return childNodes.length;
		}
	}

	public Tree[] getLeaves () {
		return getLeaves(this);
	}

	public Tree[] getLeaves (Tree t) {
		if (t.isLeaf()) {
			return new Tree[]{t};
		}

		return (Tree[]) getLeaves(t, new ArrayList<Tree>()).toArray(new Tree[0]);
	}

	private ArrayList<Tree> getLeaves (Tree t, ArrayList<Tree> l) {
		if (t.isLeaf()) {
			l.add(t);
			return l;
		}

		for (Tree c: t.getChildNodes()) {
			l = getLeaves(c, l);
		}

		return l;
	}

	public String getLeavesString() {
		return getLeavesString(this);
	}

	public String getLeavesString(Tree root) {
		if (root.isLeaf()) {
			return root.node;
		}

		if (root.childNodes.length == 1) {
			return getLeavesString(root.childNodes[0]);
		}

		StringBuffer phrase = new StringBuffer();
		for (Tree t: root.childNodes) {
			String substring = getLeavesString(t);
			int apos = substring.indexOf('\'');
			if (apos == -1) {
				phrase.append(' ');
			} else {
				int space = substring.indexOf(' ');
				if (space > 0 && space < apos) {
					phrase.append(' ');
				}
			}
			phrase.append(substring);
		}

		return phrase.toString().trim();
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public Tree getLeftmostLeaf () {
		return getLeftmostLeaf(this);
	}

	public Tree getLeftmostLeaf (Tree t) {
		if (t.isLeaf()) {
			return t;
		} else {
			return getLeftmostLeaf(t.getChildNodes()[0]);
		}
	}

	private void printIter(Tree root) {

		if (root.isLeaf()) {
			System.out.print(root.node);
			return;
		}

		if (root.hasLeaf()) {
			printIter(root.childNodes[0]);
			System.out.print("/");
			System.out.print(root.node);
			System.out.print(" ");
			return;
		}

		System.out.print("[");
		System.out.print(root.node);
		System.out.print(" ");
		for (Tree t : root.childNodes) {
			printIter(t);
		}
		System.out.print("] ");

	}

	public void print() {
		// printIter(this, 0, true);
		for (Tree t : this.childNodes) {
			printIter(t);
		}
		System.out.print("\n");
	}

	public void setChildNodes(Tree[] newChildNodes) {
		childNodes = newChildNodes;
	}

	public void setChildNode(Tree newChildNode) {
		childNodes = new Tree[] { newChildNode };
	}

	public void addChildNode(Tree newChildNode) {
		if (childNodes == null) {
			setChildNode(newChildNode);
		} else {
			Tree[] tmp = new Tree[childNodes.length + 1];
			System.arraycopy(childNodes, 0, tmp, 0, childNodes.length);
			tmp[childNodes.length] = newChildNode;
			childNodes = tmp;
		}
	}

	public void setParentNode(Tree parent) {
		this.parent = parent;
	}

	public Tree getParent() {
		return parent;
	}

	public Tree(String node) {
		this.node = node;
		childNodes = null;
	}

	public Tree (String node, Tree parent) {
		this.node = node;
		this.parent = parent;
		childNodes = null;
	}

	public Tree (Tree t) {
		this.node = t.node;
		this.parent = t.parent;
		childNodes = t.childNodes;
	}
}
