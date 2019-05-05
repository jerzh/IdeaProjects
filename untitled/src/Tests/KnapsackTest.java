package Tests;

import java.util.ArrayList;

public class KnapsackTest {
    public static void main(String[] args) {
        Item[] items = {new Item("water", 3, 10),
            new Item("book", 1, 3),
            new Item("food", 2, 9),
            new Item("jacket", 2, 5),
            new Item("camera", 1, 6)};
        int capacity = 6;

        System.out.println("Capacity: " + capacity);
        System.out.println("Items:");
        for (Item item : items) {
            System.out.printf("%10s | %10d | %10d%n", item.getName(), item.getWeight(), item.getValue());
        }
        System.out.println();

        ArrayList<ArrayList<Item>> subproblems = new ArrayList<>(capacity);

        for (int i = 0; i < capacity; i++) {
            subproblems.add(new ArrayList<>());
        }

        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            ArrayList<ArrayList<Item>> prev = subproblems;
            subproblems = new ArrayList<>(capacity);
            for (int j = 0; j < capacity; j++) {
                if (j >= item.getWeight()) {
                    ArrayList<Item> withoutItem = prev.get(j);
                    ArrayList<Item> withItem = new ArrayList<>(prev.get(j - item.getWeight()));
                    withItem.add(item);
                    subproblems.add(maxValueList(withoutItem, withItem));
                } else {
                    subproblems.add(prev.get(j));
                }
            }

            System.out.println("Iteration " + (i + 1) + ": " + item.getName());
            System.out.println(subproblems);
            System.out.println();
        }

        System.out.println("Solution:");
        System.out.println(subproblems.get(capacity - 1));
    }

    private static int findValue(ArrayList<Item> set) {
        int value = 0;
        for (Item item : set) {
            value += item.getValue();
        }
        return value;
    }

    private static ArrayList<Item> maxValueList(ArrayList<Item> list1, ArrayList<Item> list2) {
        if (findValue(list1) > findValue(list2)) {
            return list1;
        }
        return list2;
    }
}

class Item {
    private String name;
    private int weight;
    private int value;

    public Item(String name, int weight, int value) {
        this.name = name;
        this.weight = weight;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name.substring(0, 1);
    }
}
