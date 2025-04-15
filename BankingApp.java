import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.springframework.*;

import javax.persistence.*;
import java.util.*;
@Entity
@Table(name = "accounts")
class Account {
    @Id
    private int id;
    private String owner;
    private double balance;
    public Account() {}
    public Account(int id, String owner, double balance) {
        this.id = id;
        this.owner = owner;
        this.balance = balance;
    }
    public int getId() { return id; }
    public String getOwner() { return owner; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    @Override
    public String toString() {
        return "Account{id=" + id + ", owner='" + owner + "', balance=" + balance + '}';
    }
}
@Entity
@Table(name = "transactions")
class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int fromAccount;
    private int toAccount;
    private double amount;
    private Date timestamp;
    public BankTransaction() {}
    public BankTransaction(int from, int to, double amount) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.timestamp = new Date();
    }
    @Override
    public String toString() {
        return "Transaction from " + fromAccount + " to " + toAccount + " of $" + amount;
    }
}
@Configuration
@EnableTransactionManagement
@ComponentScan
class AppConfig {
    @Bean
    public SessionFactory sessionFactory() {
        Configuration config = new Configuration();
        config.configure("hibernate.cfg.xml");
        config.addAnnotatedClass(Account.class);
        config.addAnnotatedClass(BankTransaction.class);
        return config.buildSessionFactory();
    }
    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sf) {
        return new HibernateTransactionManager(sf);
    }
    @Bean
    public BankService bankService() {
        return new BankService();
    }
}
class BankService {
    @org.springframework.beans.factory.annotation.Autowired
    private SessionFactory sessionFactory;
    @Transactional
    public void transferMoney(int fromId, int toId, double amount) {
        Session session = sessionFactory.getCurrentSession();
        Account from = session.get(Account.class, fromId);
        Account to = session.get(Account.class, toId);
        if (from == null || to == null) throw new RuntimeException("Account not found");
        if (from.getBalance() < amount) throw new RuntimeException("Insufficient balance");
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);
        session.update(from);
        session.update(to);
        session.save(new BankTransaction(fromId, toId, amount));
        System.out.println("Transfer successful: $" + amount + " from " + from.getOwner() + " to " + to.getOwner());
    }
    public void showAllAccounts() {
        Session session = sessionFactory.openSession();
        List<Account> accounts = session.createQuery("from Account", Account.class).list();
        accounts.forEach(System.out::println);
        session.close();
    }
}
public class BankingApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        BankService bankService = context.getBean(BankService.class);
        preloadAccounts(context);
        System.out.println("Initial Account State:");
        bankService.showAllAccounts();
        try {
            bankService.transferMoney(1, 2, 100.0);
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
        try {
            bankService.transferMoney(1, 2, 10000.0);
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
        System.out.println("Final Account State:");
        bankService.showAllAccounts();
        context.close();
    }
    private static void preloadAccounts(AnnotationConfigApplicationContext context) {
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        if (session.get(Account.class, 1) == null) {
            session.save(new Account(1, "Alice", 500.0));
            session.save(new Account(2, "Bob", 300.0));
        }
        tx.commit();
        session.close();
    }
}
