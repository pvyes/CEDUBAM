package domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.bayesserver.Link;
import com.bayesserver.Network;
import com.bayesserver.Node;
import com.bayesserver.Variable;

/**
 * This class implements different graph functions on the given Bayes Network
 * @author Peter
 *
 */
public class GraphFunctions {
	/**
	 * Returns a list of nodes containing all the descendants and their parents. The given node is not included. 
	 * @param node
	 */
	public static Collection<Node> getDescendantsAndTheirParents(Node node) {
		Collection<Node> nodes = new HashSet<Node>();
		for (Link linkOut : node.getLinksOut()) {
			Node child = linkOut.getTo();
			nodes.add(child);
			nodes = addCollection(nodes, getParentNodes(child));
			nodes.remove(node);
			nodes = addCollection(nodes, getDescendantsAndTheirParents(child));
		}
		return nodes;		
	}
	
	/**
	 * Returns a list of nodes containing all but the descendants and their parents. The given node is not included. 
	 * @param node
	 */
	public static Collection<Node> getAllButDescendantsAndTheirParents(Node node) {
		Network network = node.getNetwork();
		Collection<Node> nodes = new HashSet<Node>(network.getNodes());
		nodes.removeAll(getDescendantsAndTheirParents(node));
		return nodes;
	}

	public static Collection<Node> getParentNodes(Node node) {
		Collection<Node> nodes = new HashSet<Node>();
		for (Link linkIn : node.getLinksIn()) {
			Node parent = linkIn.getFrom();
			nodes.add(parent);
		}
		return nodes;
	}
	
	public static Collection<Node> getParentNodes(Collection<Node> nodes) {
		Collection<Node> allnodes = new HashSet<Node>();
		for (Node node : nodes) {
			Collection<Node> parents = getParentNodes(node);
			allnodes.addAll(parents);
		}
		return allnodes;
	}
	
	public static Collection<Node> getParentNodes(Node node, Collection<Node> collection) {
		for (Link linkIn : node.getLinksIn()) {
			Node parent = linkIn.getFrom();
			collection.add(parent);
		}
		return collection;
	}
	
	public static Collection<Node> getChildNodes(Node node) {
		Collection<Node> nodes = new HashSet<Node>();
		for (Link linkOut : node.getLinksOut()) {
			Node child = linkOut.getTo();
			nodes.add(child);
		}
		return nodes;
	}
	
	public static Collection<Node> addCollection(Collection<Node> nodes, Collection<Node> newNodes) {
		Iterator<Node> it = newNodes.iterator();
		while (it.hasNext()) {
			nodes.add(it.next());
		}
		return nodes;
	}

	public static Collection<Node> getAncestors(Node node, Collection<Node> collection) {
		if (collection == null) {
			collection = new HashSet<Node>();
		}
		Collection<Node> ancestors = new HashSet<Node>();
		ancestors = getParentNodes(node);
		collection.addAll(ancestors);
		for (Node p : ancestors) {
			collection.addAll(getAncestors(p, collection));
		}
		return collection;
	}
	
	public static Collection<Node> getRootNodes(Network network) {
		Collection<Node> nodes = new HashSet<Node>();	
		for (Node node: network.getNodes()) {
			if (node.getLinksIn().size() == 0) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	public static Collection<Node> getRootAncestors(Node node) {
		Collection<Node> rootAncestors = new HashSet<Node>();
		Collection<Node> ancestors = new HashSet<Node>();
		GraphFunctions.getAncestors(node, ancestors);
		Collection<Node> rootnodes = GraphFunctions.getRootNodes(node.getNetwork());
		for (Node anc: ancestors) {
			if (rootnodes.contains(anc)) {
				rootAncestors.add(anc);
			}
		}
		return rootAncestors;
	}
	
	public static Collection<Variable> getRootAncestors(Variable var) {
		Collection<Variable> rootAncestors = new HashSet<Variable>();
		Collection<Node> ancNodes = getRootAncestors(var.getNode());
		for (Node anc: ancNodes) {
			rootAncestors.addAll(anc.getVariables());
		}
		return rootAncestors;
	}
	
	public static Collection<Variable> getRootAncestors(Collection<Variable> vars) {
		Collection<Variable> rootAncestors = new HashSet<Variable>();		
		for (Variable var: vars) {
			rootAncestors.addAll(getRootAncestors(var));
		}
		return rootAncestors;
	}
}