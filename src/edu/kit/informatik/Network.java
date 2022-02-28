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
    private final Map<IP, TreeSet<IP>> treeTopology = new HashMap<>();

    /**
     * This constructor creates a new network instance and adds a valid tree topology if the inputs were correct.
     *
     * @param root     root of the tre
     * @param children children of the root
     */
    public Network(final IP root, final List<IP> children) {

        TreeSet<IP> childSet = constructorInputValidation(root, children);
        treeTopology.put(root, childSet);
        for (IP child : childSet) {
            treeTopology.put(child, new TreeSet<>());
            treeTopology.get(child).add(root);
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
                    treeTopology.put(newIp, new TreeSet<>());
                    firstElement = false;
                } else {
                    newIp = new IP(ip.substring(1));
                    convertIPToAdjList(newIp, rootList);
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
                convertIPToAdjList(newIp, rootList);
                for (int i = 0; i < counter; i++) {
                    rootList.removeLast();
                }

            } else {
                newIp = new IP(ip);
                convertIPToAdjList(newIp, rootList);
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
        for (IP ip : this.treeTopology.keySet()) {
            topologySave.put(ip, new TreeSet<>(this.treeTopology.get(ip)));

        }

        for (IP ip : subnet.treeTopology.keySet()) {
            if (!this.treeTopology.containsKey(ip)) {

                this.treeTopology.put(ip, new TreeSet<>());
                this.treeTopology.get(ip).addAll(subnet.treeTopology.get(ip));

            } else {
                this.treeTopology.get(ip).addAll(subnet.treeTopology.get(ip));
            }
        }

        LinkedHashSet<IP> visited = new LinkedHashSet<>();
        for (IP ip : this.treeTopology.keySet()) {
            if (!visited.contains(ip) && checkCycle(ip, null, visited)) {
                this.treeTopology.clear();
                this.treeTopology.putAll(topologySave);
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
        List<IP> list = new ArrayList<>();
        list.addAll(this.treeTopology.keySet());
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

            if (traversal(ip1, new LinkedHashSet<>(), this).contains(ip2)) {
                return false;
            }
            this.treeTopology.get(ip1).add(ip2);
            this.treeTopology.get(ip2).add(ip1);
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
        if (this.treeTopology.size() == 2) {
            return false;
        }
        if (this.treeTopology.containsKey(ip1)
                && this.treeTopology.containsKey(ip2)
                && this.treeTopology.get(ip1).contains(ip2)
                && this.treeTopology.get(ip2).contains(ip1)) {
            this.treeTopology.get(ip1).remove(ip2);
            if (this.treeTopology.get(ip1).isEmpty()) {
                this.treeTopology.remove(ip1);
            }
            this.treeTopology.get(ip2).remove(ip1);
            if (this.treeTopology.get(ip2).isEmpty()) {
                this.treeTopology.remove(ip2);
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
        return this.treeTopology.containsKey(ip);
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
        if (!this.treeTopology.containsKey(root)) {
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
        if (!this.treeTopology.containsKey(root)) {
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
        if (start.equals(end) || !this.treeTopology.containsKey(start) || !this.treeTopology.containsKey(end)) {
            return route;
        }
        route(start, end, new LinkedHashSet<>(), route);
        return route;
    }

    /**
     * Returns the networstructure as bracketnotaion from the given IP as root.
     *
     * @param root IP
     * @return Bracketnotaion as string
     */
    public String toString(IP root) {

        if (root == null || !this.treeTopology.containsKey(root)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        bracketNotation(root, null, sb);

        return sb.toString();
    }

    private HashSet<IP> traversal(IP root, LinkedHashSet<IP> visited, Network subnet) {

        visited.add(root);

        for (IP ip : subnet.treeTopology.get(root)) {
            if (!visited.contains(ip)) {
                traversal(ip, visited, subnet);
            }
        }
        return visited;
    }

    private boolean checkCycle(IP root, IP parent, LinkedHashSet<IP> visited) {

        visited.add(root);

        for (IP ip : treeTopology.get(root)) {
            if (!ip.equals(parent) && visited.contains(ip)) {
                return true;
            }
            if (!visited.contains(ip) && checkCycle(ip, root, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean route(IP start, IP end, LinkedHashSet<IP> visited, LinkedList<IP> route) {

        visited.add(start);
        route.add(start);
        if (start.equals(end)) {
            return true;
        }

        for (IP ip : treeTopology.get(start)) {
            if (!visited.contains(ip) && route(ip, end, visited, route)) {
                return true;
            }
        }
        route.removeLast();
        return false;
    }

    private void bracketNotation(IP root, IP parent, StringBuilder result) {

        if (treeTopology.get(root).size() == 1 && parent != null) {
            result.append(root);
        } else {
            result.append("(").append(root).append(" ");
            for (IP ip : treeTopology.get(root)) {
                if (!ip.equals(parent)) {
                    bracketNotation(ip, root, result);
                    result.append(" ");
                }
            }
            result.replace(result.length() - 1, result.length(), "");
            result.append(")");
        }
    }

    private void levelList(IP root, int level, List<List<IP>> list, IP parent) {

        if (list.size() == level) {
            list.add(new LinkedList<>());
        }
        list.get(level).add(root);
        for (IP ip : treeTopology.get(root)) {
            if (!ip.equals(parent)) {
                levelList(ip, level + 1, list, root);
            }
        }
    }

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

    private void convertIPToAdjList(IP newIp, LinkedList<IP> rootList) throws ParseException {

        if (treeTopology.containsKey(newIp)) {
            throw new ParseException(ILLEGAL_TREE_TOPOLOGY);
        }
        treeTopology.put(newIp, new TreeSet<>());
        connect(rootList.getLast(), newIp);
    }

    private boolean addValidation(Network subnet) {

        if (subnet == null) {
            return false;
        }
        for (IP ip: subnet.treeTopology.keySet()) {
            if (!this.treeTopology.containsKey(ip)) {
                return true;
            }
            if (!this.treeTopology.get(ip).containsAll(subnet.treeTopology.get(ip))) {
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
        return Objects.equals(treeTopology, network.treeTopology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(treeTopology);
    }
}
