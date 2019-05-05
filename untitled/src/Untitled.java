public class Untitled {
    private static class Person {
        public String isHappy() {
            return "probably";
        }
    }

    private static class Student extends Person {
        @Override
        public String isHappy() {
            return "probably not";
        }
    }

    public static void main(String[] args) {
        Student s = new Student();
        System.out.println(s.isHappy());
    }
}
