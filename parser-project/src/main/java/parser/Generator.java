package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Generator {
    private final Map<String, List<String>> rules = new HashMap<>();
    private final Random random = new Random();

    public Generator() {
        loadRules();
    }

    private void loadRules() {
        try (BufferedReader reader = new BufferedReader(new FileReader("./rules.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Пропускаем пустые строки и строки, начинающиеся с комментариев
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }
    
                // Разделяем правило на левую и правую части
                String[] parts = line.split("->");
                if (parts.length == 2) {
                    String nonTerminal = parts[0].trim();
                    String[] productions = parts[1].split("\\|");
    
                    // Убираем пробелы и добавляем все варианты правил
                    List<String> productionList = new ArrayList<>();
                    for (String production : productions) {
                        productionList.add(production.trim());
                    }
                    rules.put(nonTerminal, productionList);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //System.out.println("Rules:");
        //for (Map.Entry<String, List<String>> entry : rules.entrySet()) {
        //    System.out.println(entry.getKey() + " -> " + String.join(" | ", entry.getValue()));
        //}
    }


    public String generate() {
        List<String> result = new ArrayList<>();
        result.add("S0");
        while (result.stream().anyMatch(this::isNonTerminal)) {
            for (int i = 0; i < result.size(); i++) {
                String element = result.get(i);
                if (isNonTerminal(element)) {
                    result.remove(i);
                    List<String> productions = rules.get(element);
                    if (productions != null) {
                        String production = productions.get(random.nextInt(productions.size()));
                        Collections.addAll(result, production.split(" "));
                    }
                    break; // Перезапуск цикла после замены
                }
            }
        }
        result.removeIf(s -> s.equals("e’"));
        return String.join(" ", result);
    }

    private boolean isNonTerminal(String symbol) {
        return rules.containsKey(symbol);
    }
}
