@Table(name = "PersonTable")
public class Person {
    public Person(String firstName, String secondName, char keyLetter, int age, float weight) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.keyLetter = keyLetter;
        this.age = age;
        this.weight = weight;
    }

    @Column
    private String firstName;
    @Column
    private String secondName;
    @Column
    private char keyLetter;
    @Column
    private int age;
    @Column
    private float weight;
}