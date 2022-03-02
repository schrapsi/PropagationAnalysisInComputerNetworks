package edu.kit.informatik;

import edu.kit.informatik.exceptions.InvalidTreeException;
import edu.kit.informatik.exceptions.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

/**
 * The Class Network models a network structure with different tree topology´s.
 * It inherits useful methods for operations on the network structure.
 *
 * @author ucfoh
 * @version 1.0
 */
public class Network {

    private static final String ILLEGAL_TREE_TOPOLOGY = "Its not allowed to add a circular Graph";
    private static final int SMALLEST_NOTATION_POSSIBLE = 17;
    private final Map<IP, TreeSet<IP>> adjacencyList = new HashMap<>();

    /**
     * This constructor creates a new network instance and adds a valid tree topology if the inputs were correct.
     *
     * @param root     root of the tre
     * @param children children of the root
     */
    public Network(final IP root, final List<IP> children) {

        TreeSet<IP> childSet = constructorInputValidation(root, children);
        adjacencyList.put(root, childSet);
        for (IP child : childSet) {
            adjacencyList.put(child, new TreeSet<>());
            adjacencyList.get(child).add(root);
        }
    }

    /**
     * The constructor creates a new Network instance and converts the bracket notation to a valid tree topology.
     *
     * @param bracketNotation Network as bracket Notation
     * @throws ParseException when the input was not correct
     */
    public Network(final String bracketNotation) throws ParseException {

        constructorInputValidation(bracketNotation);
        String[] ips = bracketNotation.split(" ");
        LinkedList<IP> rootList = new LinkedList<>();
        boolean firstElement = true;
        IP newIp;

        for (String ip : ips) {
            if (ip.contains("(")) {
                if (firstElement) {
                    newIp = new IP(ip.substring(1));
                    adjacencyList.put(newIp, new TreeSet<>());
                    firstElement = false;
                } else {
                    newIp = new IP(ip.substring(1));
                    addIP(newIp, rootList);
                }
                rootList.add(new IP(ip.substring(1)));

            } else if (ip.contains(")")) {
                int counter = 0;
                for (int i = 0; i < ip.length(); i++) {
                    if (ip.charAt(i) == ')') {
                        counter++;
                    }
                }
                newIp = new IP(ip.substring(0, ip.length() - counter));
                addIP(newIp, rootList);
                for (int i = 0; i < counter; i++) {
                    rootList.removeLast();
                }

            } else {
                newIp = new IP(ip);
                addIP(newIp, rootList);
            }
        }
    }

    /**
     * The add Method add´s the tree topology of a given network to the other network.
     * But only if the rules for a tree topology are not harmed.
     *
     * @param subnet Network input
     * @return boolean if the add was successful or not
     */
    public boolean add(final Network subnet) {

        if (!addValidation(subnet)) {
            return false;
        }

        Map<IP, TreeSet<IP>> topologySave = new HashMap<>();
        for (IP ip : this.adjacencyList.keySet()) {
            topologySave.put(ip, new TreeSet<>(this.adjacencyList.get(ip)));

        }

        for (IP ip : subnet.adjacencyList.keySet()) {
            if (!this.adjacencyList.containsKey(ip)) {

                this.adjacencyList.put(ip, new TreeSet<>());
                this.adjacencyList.get(ip).addAll(subnet.adjacencyList.get(ip));

            } else {
                this.adjacencyList.get(ip).addAll(subnet.adjacencyList.get(ip));
            }
        }

        LinkedHashSet<IP> visited = new LinkedHashSet<>();
        for (IP ip : this.adjacencyList.keySet()) {
            if (!visited.contains(ip) && checkCycle(ip, null, visited)) {
                this.adjacencyList.clear();
                this.adjacencyList.putAll(topologySave);
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a list of all nodes.
     * @return a list of all nodes existing in the tree topology
     */
    public List<IP> list() {
        List<IP> list = new ArrayList<>(this.adjacencyList.keySet());
        Collections.sort(list);
        return list;
    }

    /**
     * Creates and edge between ip1 and ip2 if there isn´t already a Path existing.
     *
     * @param ip1 ip input 1
     * @param ip2 ip input 2
     * @return boolean if the connection was successful or not
     */
    public boolean connect(final IP ip1, final IP ip2) {

        if (contains(ip1) && contains(ip2)) {

            if (traversal(ip1, new LinkedHashSet<>()).contains(ip2)) {
                return false;
            }
            this.adjacencyList.get(ip1).add(ip2);
            this.adjacencyList.get(ip2).add(ip1);
            return true;
        } else return false;

    }

    /**
     * Disconnects an edge between ip1 and ip2 if the edge exists and if it's not the last edge.
     *
     * @param ip1 input ip 1
     * @param ip2 input ip 2
     * @return boolean if the disconnect was successful or not
     */
    public boolean disconnect(final IP ip1, final IP ip2) {
        if (this.adjacencyList.size() == 2) {
            return false;
        }
        if (this.adjacencyList.containsKey(ip1)
                && this.adjacencyList.containsKey(ip2)
                && this.adjacencyList.get(ip1).contains(ip2)
                && this.adjacencyList.get(ip2).contains(ip1)) {
            this.adjacencyList.get(ip1).remove(ip2);
            if (this.adjacencyList.get(ip1).isEmpty()) {
                this.adjacencyList.remove(ip1);
            }
            this.adjacencyList.get(ip2).remove(ip1);
            if (this.adjacencyList.get(ip2).isEmpty()) {
                this.adjacencyList.remove(ip2);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns if the ip exists in the Tree.
     * @param ip ip input
     * @return if the ip input exists in the tree Topology
     */
    public boolean contains(final IP ip) {
        return this.adjacencyList.containsKey(ip);
    }

    /**
     * Returns the hight of the tree from a given root.
     * The hight is the number of level´s in the tree.
     * But starting with zero
     *
     * @param root IP
     * @return the hight of the Tree
     */
    public int getHeight(final IP root) {
        if (!this.adjacencyList.containsKey(root)) {
            return 0;
        } else {
            return getLevels(root).size() - 1;
        }

    }

    /**
     * Returns a list of the levels in the tree topology. From a given root.
     * Level 0 is a list with just the root.
     * Level 1 is a list from the Children of the root.
     *
     * @param root IP
     * @return List of Lists of IP´s
     */
    public List<List<IP>> getLevels(final IP root) {


        List<List<IP>> levels = new LinkedList<>();
        if (!this.adjacencyList.containsKey(root)) {
            return levels;
        }
        levelList(root, 0, levels, null);
        for (List<IP> level : levels) {
            Collections.sort(level);
        }
        return levels;

    }

    /**
     * Returns the route from a start IP to an end IP in the tree.
     * The route is a list of IP´s with the start IP on the left and the end IP on the right.
     *
     * @param start route start IP
     * @param end   route end IP
     * @return the Route as List
     */
    public List<IP> getRoute(final IP start, final IP end) {


        LinkedList<IP> route = new LinkedList<>();
        if (start == null || end == null) {
            return route;
        }
        if (start.equals(end) || !this.adjacencyList.containsKey(start) || !this.adjacencyList.containsKey(end)) {
            return route;
        }
        route(start, end, new LinkedHashSet<>(), route);
        return route;
    }

    /**
     * Returns the networstructure as bracket notation from the given IP as root.
     * @param root IP
     * @return Bracketnotaion as string
     */
    public String toString(IP root) {

        if (root == null || !this.adjacencyList.containsKey(root)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        bracketNotation(root, null, sb);

        return sb.toString();
    }

    /**
     * Method do travel through the Tree. (based on DFS algorithm)
     * @param root starting point
     * @param visited set of visited nodes
     * @return the List of the Visited nodes
     */
    private HashSet<IP> traversal(IP root, LinkedHashSet<IP> visited) {

        visited.add(root);

        for (IP ip : this.adjacencyList.get(root)) {
            if (!visited.contains(ip)) {
                traversal(ip, visited);
            }
        }
        return visited;
    }

    /**
     * Checks if the Tree contains a cycle. (based on DFS algorithm)
     * @param root starting point
     * @param parent parent of the node
     * @param visited Set of visited nodes
     * @return if there is a cycle or not
     */
    private boolean checkCycle(IP root, IP parent, LinkedHashSet<IP> visited) {

        visited.add(root);

        for (IP ip : adjacencyList.get(root)) {
            if (!ip.equals(parent) && visited.contains(ip)) {
                return true;
            }
            if (!visited.contains(ip) && checkCycle(ip, root, visited)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a route and saves the route in a Set. (based on DFS algorithm)
     * @param start starting point of the route
     * @param end end point of the route
     * @param visited Set of visited notes
     * @param route List of the IP´s of the route
     * @return boolean if there is a route or not
     */
    private boolean route(IP start, IP end, LinkedHashSet<IP> visited, LinkedList<IP> route) {

        visited.add(start);
        route.add(start);
        if (start.equals(end)) {
            return true;
        }

        for (IP ip : adjacencyList.get(start)) {
            if (!visited.contains(ip) && route(ip, end, visited, route)) {
                return true;
            }
        }
        route.removeLast();
        return false;
    }

    /**
     * Builds a tree into the bracketnotation. (based on DFS algorithm)
     * @param root root of the tree
     * @param parent parent of the node
     * @param result StringBuilder where the bracketnotaion is saved
     */
    private void bracketNotation(IP root, IP parent, StringBuilder result) {

        if (adjacencyList.get(root).size() == 1 && parent != null) {
            result.append(root);
        } else {
            result.append("(").append(root).append(" ");
            for (IP ip : adjacencyList.get(root)) {
                if (!ip.equals(parent)) {
                    bracketNotation(ip, root, result);
                    result.append(" ");
                }
            }
            result.replace(result.length() - 1, result.length(), "");
            result.append(")");
        }
    }

    /**
     * Puts all Levels of the Tree into a list. (based on DFS algorithm)
     * @param root starting point
     * @param level current level
     * @param list list where the different levels are saved
     * @param parent parent of the node
     */
    private void levelList(IP root, int level, List<List<IP>> list, IP parent) {

        if (list.size() == level) {
            list.add(new LinkedList<>());
        }
        list.get(level).add(root);
        for (IP ip : adjacencyList.get(root)) {
            if (!ip.equals(parent)) {
                levelList(ip, level + 1, list, root);
            }
        }
    }

    /**
     * Checks if the Input was correct.
     * @param root root ip
     * @param children List of Children IP´s
     * @return the children as TreeSet
     * @throws IllegalArgumentException if the Input was not correct
     */
    private TreeSet<IP> constructorInputValidation(IP root, List<IP> children) throws IllegalArgumentException {

        if (children == null) {
            throw new IllegalArgumentException("Children should not be Null");
        }
        if (root == null || children.isEmpty()) {
            throw new IllegalArgumentException("root is null or children empty");
        }
        for (IP ip : children) {
            if (ip == null) {
                throw new IllegalArgumentException("one child is null");
            }
        }
        if (children.contains(root)) {
            throw new InvalidTreeException(ILLEGAL_TREE_TOPOLOGY);
        }
        TreeSet<IP> childSet = new TreeSet<>(children);

        if (childSet.size() != children.size()) {
            throw new InvalidTreeException("its not allowed to add the same IP twice");
        }
        return childSet;

    }

    /**
     * Checks if the Input was correct.
     * @param bracketNotation a Tree in bracket notation
     * @throws ParseException if the input was not correct
     */
    private void constructorInputValidation(String bracketNotation) throws ParseException {

        if (bracketNotation == null) {
            throw new ParseException("Null is not allowed");
        }
        int bracketCounter = 0;
        if (bracketNotation.length() < SMALLEST_NOTATION_POSSIBLE) {
            throw new ParseException("not enough chars vor a valid input");
        }
        if (bracketNotation.charAt(0) != '(' || bracketNotation.charAt(bracketNotation.length() - 1) != ')') {
            throw new ParseException("no existing brackets");
        }
        for (char c : bracketNotation.toCharArray()) {
            if (c == '(') {
                bracketCounter++;
            } else if (c == ')') {
                bracketCounter--;
            }
        }
        if (bracketCounter != 0) {
            throw new ParseException("Number of Opened and Closed Brackets are not equal");
        }
    }

    /**
     * Adds a new IP and connects it with the last element of the root List and adds it to the adjacency list.
     * @param ip new node
     * @param rootList list of roots (root of every subtree)
     * @throws ParseException if the IP already exists
     */
    private void addIP(IP ip, LinkedList<IP> rootList) throws ParseException {

        if (adjacencyList.containsKey(ip)) {
            throw new ParseException(ILLEGAL_TREE_TOPOLOGY);
        }
        adjacencyList.put(ip, new TreeSet<>());
        connect(rootList.getLast(), ip);
    }

    /**
     * Checks if the subnet is null or a part tree
     * @param subnet Network to check
     * @return if the Validation was true
     */
    private boolean addValidation(Network subnet) {

        if (subnet == null) {
            return false;
        }
        for (IP ip: subnet.adjacencyList.keySet()) {
            if (!this.adjacencyList.containsKey(ip)) {
                return true;
            }
            if (!this.adjacencyList.get(ip).containsAll(subnet.adjacencyList.get(ip))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return Objects.equals(adjacencyList, network.adjacencyList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjacencyList);
    }
}
