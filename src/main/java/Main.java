import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {
    private static HashMap<Class, String> classToString = new HashMap<Class, String>();

    static {
        classToString.put(int.class, "INTEGER");
        classToString.put(long.class, "BIGINT");
        classToString.put(double.class, "REAL");
        classToString.put(float.class, "REAL");
        classToString.put(String.class, "TEXT");
        classToString.put(char.class, "CHARACTER");
    }

    private static String classToStringFunction(Class classField) {
        if (classToString.containsKey(classField)) {
            return classToString.get(classField);
        }
        return "TEXT";
    }

    private static String fieldToStringValue(Field classField, Object obj) throws IllegalAccessException {
        if (classField.getType() == String.class) {
            return "\'" + classField.get(obj) + "\'";
        }
        if (classField.getType() == char.class) {
            return "\'" + classField.get(obj) + "\'";
        }
        return classField.get(obj).toString();
    }

    private static void classToDB(Object obj, Connection connection) {
        Class objClass = obj.getClass();
        if (!objClass.isAnnotationPresent(Table.class)) {
            return;
        }

        Table annotation = (Table) objClass.getAnnotation(Table.class);
        System.out.println(annotation.name());
        String tableName = annotation.name();
        try {
            Statement stmt = connection.createStatement();
            String request = "DROP TABLE IF EXISTS " + tableName + "; CREATE TABLE " + tableName + " (";

            for (Field field : objClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    System.out.println(field.getName());
                    request += field.getName() + " " + classToStringFunction(field.getType()) + ",";
                }
            }
            if (request.charAt(request.length() - 1) == ',') {
                request = request.substring(0, request.length() - 1);
            }
            request += ");";

            stmt.executeUpdate(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addClassToDB(Object obj, Connection connection) {
        Class objClass = obj.getClass();
        if (!objClass.isAnnotationPresent(Table.class)) {
            return;
        }

        Table annotation = (Table) objClass.getAnnotation(Table.class);
        String tableName = annotation.name();
        List<Field> fields = new ArrayList<>();
        List<Object> fieldsValues = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            String request = "INSERT INTO " + tableName + " (";

            for (Field field : objClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    fields.add(field);
                }
            }
            for (Field field : fields) {
                request += field.getName() + ",";
            }
            if (request.charAt(request.length() - 1) == ',') {
                request = request.substring(0, request.length() - 1);
            }
            request += ") VALUES (";
            for (Field field : fields) {
                field.setAccessible(true);
                request += fieldToStringValue(field, obj) + ",";
            }
            if (request.charAt(request.length() - 1) == ',') {
                request = request.substring(0, request.length() - 1);
            }
            request += ");";

            System.out.println(request);

            stmt.executeUpdate(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //<database_host>:<port>/<database_name>
        Class.forName("org.postgresql.Driver");

        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/VTB_TEST_9",
                "postgres", "12345678");

        Person person = new Person("Кот", "Кот", "С".charAt(0), 4, 5.6f);
        Person personK = new Person("Илья", "Крутько", "K".charAt(0), 22, 155.6f);

        classToDB(person, connection);
        addClassToDB(person, connection);
        addClassToDB(personK, connection);

        connection.close();
    }
}
