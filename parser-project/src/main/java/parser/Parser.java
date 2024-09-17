package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Parser {
    private final Map<String, List<String>> rules = new HashMap<>();
    private final Map<String, Set<String>> firstSets = new HashMap<>();
    private final Map<String, Set<String>> followSets = new HashMap<>();
    private final Set<String> terminals = new HashSet<>();
    private final Set<String> nonTerminals = new HashSet<>();
    private final List<String> induction = new ArrayList<>();

    public Parser() {
        loadRules();
        computeFirstSets();
        computeFollowSets();
    }

    private void loadRules() {
        try (BufferedReader reader = new BufferedReader(new FileReader("./rules.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }

                String[] parts = line.split("->");
                if (parts.length == 2) {
                    String nonTerminal = parts[0].trim();
                    String[] productions = parts[1].split("\\|");

                    List<String> productionList = new ArrayList<>();
                    for (String production : productions) {
                        productionList.add(production.trim());
                    }
                    rules.put(nonTerminal, productionList);
                    nonTerminals.add(nonTerminal);

                    for (String production : productionList) {
                        for (String symbol : production.split("\\s+")) {
                            if (!isNonTerminal(symbol) && !symbol.equals("e’")) {
                                terminals.add(symbol);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void computeFirstSets() {
        for (String nonTerminal : nonTerminals) {
            firstSets.put(nonTerminal, computeFirstSet(nonTerminal));
        }
    }

    private Set<String> computeFirstSet(String symbol) {
        Set<String> firstSet = new HashSet<>();
        List<String> productions = rules.get(symbol);

        if (productions != null) {
            for (String production : productions) {
                String[] symbols = production.split("\\s+");
                if (symbols.length > 0) {
                    String firstSymbol = symbols[0];
                    if (isTerminal(firstSymbol)) {
                        firstSet.add(firstSymbol);
                    } else if (isNonTerminal(firstSymbol)) {
                        firstSet.addAll(computeFirstSet(firstSymbol));
                        if (firstSet.contains("e’")) {
                            firstSet.remove("e’");
                            if (symbols.length == 1 || symbols[1].equals("e’")) {
                                firstSet.add("e’");
                            }
                        }
                    }
                }
            }
        }

        return firstSet;
    }

    private void computeFollowSets() {
        for (String nonTerminal : nonTerminals) {
            followSets.put(nonTerminal, computeFollowSet(nonTerminal));
        }
    }

    private Set<String> computeFollowSet(String nonTerminal) {
        Set<String> followSet = new HashSet<>();
        if (nonTerminal.equals("S0")) {
            followSet.add("$");
        }

        for (Map.Entry<String, List<String>> entry : rules.entrySet()) {
            String lhs = entry.getKey();
            List<String> productions = entry.getValue();

            for (String production : productions) {
                String[] symbols = production.split("\\s+");
                for (int i = 0; i < symbols.length; i++) {
                    if (symbols[i].equals(nonTerminal)) {
                        if (i + 1 < symbols.length) {
                            String nextSymbol = symbols[i + 1];
                            if (isTerminal(nextSymbol)) {
                                followSet.add(nextSymbol);
                            } else if (isNonTerminal(nextSymbol)) {
                                followSet.addAll(computeFirstSet(nextSymbol));
                                if (firstSets.get(nextSymbol).contains("e’")) {
                                    followSet.addAll(computeFollowSet(lhs));
                                }
                            }
                        } else {
                            if (!lhs.equals(nonTerminal)) {
                                followSet.addAll(computeFollowSet(lhs));
                            }
                        }
                    }
                }
            }
        }

        return followSet;
    }

    private boolean isNonTerminal(String symbol) {
        return nonTerminals.contains(symbol);
    }

    private boolean isTerminal(String symbol) {
        return terminals.contains(symbol) || symbol.equals("e’");
    }

    public boolean parse(List<String> input) {
        Stack<String> stack = new Stack<>();
        stack.push("S0");
    
        int i = 0;
    
        while (!stack.isEmpty()) {
            String top = stack.pop();
    
            if (isTerminal(top)) {
                if (i < input.size() && top.equals(input.get(i))) {
                    induction.add(input.get(i));
                    i++;
                } else if (top.equals("e’")) {
                    // Do nothing for epsilon
                } else {
                    System.out.println("Error: Expected " + top + " but found " + (i < input.size() ? input.get(i) : "end of input"));
                    return false;
                }
            } else if (isNonTerminal(top)) {
                Set<String> firstSet = firstSets.get(top);
                if (i < input.size()) {
                    String nextSymbol = input.get(i);
                    boolean found = false;
                    for (String production : rules.get(top)) {
                        if (firstSet.contains(nextSymbol) || firstSet.contains("e’")) {
                            for (int j = production.split("\\s+").length - 1; j >= 0; j--) {
                                stack.push(production.split("\\s+")[j]);
                            }
                            induction.add(top + " (");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("Error: No matching production for " + top + " with next symbol " + nextSymbol);
                        return false;
                    }
                } else {
                    if (!firstSet.contains("e’")) {
                        System.out.println("Error: Expected epsilon production for " + top);
                        return false;
                    }
                }
            }
        }
    
        if (i != input.size()) {
            System.out.println("Stack is empty, when input is not ended: " + String.join(" ", input.subList(i, input.size())));
            return false;
        }
    
        return true;
    }    
}
