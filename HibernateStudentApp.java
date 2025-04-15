import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import javax.persistence.*;
import java.util.List;

// Entity Class
@Entity
@Table(name = "students")
class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private int age;

    public Student() {}
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }

    @Override
    public String toString() {
        return "Student [id=" + id + ", name=" + name + ", age=" + age + "]";
    }
}

// Main Application
public class HibernateStudentApp {

    private static SessionFactory factory;

    public static void main(String[] args) {
        // Step 1: Configure Hibernate and build SessionFactory
        try {
            Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml"); // This should be in classpath
            cfg.addAnnotatedClass(Student.class);
            factory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create SessionFactory." + ex);
            throw new ExceptionInInitializerError(ex);
        }

        HibernateStudentApp app = new HibernateStudentApp();

        // Step 2: Perform CRUD operations
        Student s1 = new Student("John Doe", 22);
        app.saveStudent(s1); // CREATE

        Student fetched = app.getStudent(s1.getId()); // READ
        System.out.println("Fetched: " + fetched);

        fetched.setAge(23);
        app.updateStudent(fetched); // UPDATE
        System.out.println("Updated: " + app.getStudent(fetched.getId()));

        System.out.println("All Students:");
        for (Student s : app.getAllStudents()) {
            System.out.println(s);
        }

        app.deleteStudent(fetched.getId()); // DELETE
        System.out.println("Deleted student with ID: " + fetched.getId());

        factory.close();
    }

    public void saveStudent(Student student) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        session.save(student);
        tx.commit();
        session.close();
    }

    public Student getStudent(int id) {
        Session session = factory.openSession();
        Student student = session.get(Student.class, id);
        session.close();
        return student;
    }

    public List<Student> getAllStudents() {
        Session session = factory.openSession();
        List<Student> students = session.createQuery("from Student", Student.class).list();
        session.close();
        return students;
    }

    public void updateStudent(Student student) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        session.update(student);
        tx.commit();
        session.close();
    }

    public void deleteStudent(int id) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        Student student = session.get(Student.class, id);
        if (student != null) {
            session.delete(student);
        }
        tx.commit();
        session.close();
    }
}
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
  "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
  "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/your_database</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">your_password</property>

        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="hibernate.show_sql">true</property>
    </session-factory>
</hibernate-configuration>
