package ATM;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ATMProject {

    static class Transaction {
        private String type;
        private double amount;
        private double balanceAfter;
        private LocalDateTime timestamp;
        private String note;

        public Transaction(String type, double amount, double balanceAfter, LocalDateTime timestamp, String note) {
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.timestamp = timestamp;
            this.note = note;
        }

        @Override
        public String toString() {
            String time = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return String.format("%s | %s | %.2f | Balance: %.2f | %s",
                    time, type, amount, balanceAfter, note);
        }
    }

    static class Account {
        private String accountNumber;
        private String pin;
        private double balance;
        private List<Transaction> transactions = new ArrayList<>();

        public Account(String accountNumber, String pin, double initialBalance) {
            this.accountNumber = accountNumber;
            this.pin = pin;
            this.balance = initialBalance;
            transactions.add(new Transaction("Account Opened", initialBalance, initialBalance, LocalDateTime.now(), ""));
        }

        public String getAccountNumber() { return accountNumber; }
        public double getBalance() { return balance; }
        public boolean checkPin(String pin) { return this.pin.equals(pin); }

        public synchronized void deposit(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            balance += amount;
            transactions.add(new Transaction("Deposit", amount, balance, LocalDateTime.now(), ""));
        }

        public synchronized void withdraw(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            if (amount > balance) throw new IllegalArgumentException("Insufficient funds");
            balance -= amount;
            transactions.add(new Transaction("Withdrawal", amount, balance, LocalDateTime.now(), ""));
        }

        public synchronized void addTransaction(Transaction t) {
            transactions.add(t);
        }

        public List<Transaction> getMiniStatement(int maxItems) {
            int size = transactions.size();
            int from = Math.max(0, size - maxItems);
            return new ArrayList<>(transactions.subList(from, size));
        }

        public void changePin(String newPin) {
            if (newPin == null || newPin.length() < 4)
                throw new IllegalArgumentException("PIN must be at least 4 digits");
            this.pin = newPin;
        }
    }

    private Map<String, Account> accounts = new HashMap<>();
    private Scanner sc = new Scanner(System.in);

    public ATMProject() {
        accounts.put("1001", new Account("1001", "1234", 1000.0));
        accounts.put("1002", new Account("1002", "2222", 500.0));
        accounts.put("1003", new Account("1003", "3333", 2000.0));
    }

    public void start() {
        System.out.println("=== Welcome to Simple ATM ===");
        while (true) {
            System.out.println("\n1) Login  2) Exit");
            String choice = sc.nextLine().trim();
            if ("1".equals(choice)) {
                Account acc = login();
                if (acc != null) userMenu(acc);
            } else if ("2".equals(choice)) {
                System.out.println("Thank you for using Simple ATM. Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private Account login() {
        System.out.print("Account number: ");
        String accNum = sc.nextLine().trim();
        System.out.print("PIN: ");
        String pin = sc.nextLine().trim();
        Account acc = accounts.get(accNum);
        if (acc != null && acc.checkPin(pin)) {
            System.out.println("Login successful.");
            return acc;
        } else {
            System.out.println("Invalid account number or PIN.");
            return null;
        }
    }

    private void userMenu(Account acc) {
        while (true) {
            System.out.println("\n--- ATM Menu ---");
            System.out.println("1) Balance");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) Transfer");
            System.out.println("5) Mini-Statement");
            System.out.println("6) Change PIN");
            System.out.println("7) Logout");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        System.out.printf("Your Balance: %.2f%n", acc.getBalance());
                        break;
                    case "2":
                        System.out.print("Amount to deposit: ");
                        double d = Double.parseDouble(sc.nextLine());
                        acc.deposit(d);
                        System.out.println("Deposited successfully.");
                        break;
                    case "3":
                        System.out.print("Amount to withdraw: ");
                        double w = Double.parseDouble(sc.nextLine());
                        acc.withdraw(w);
                        System.out.println("Withdrawn successfully.");
                        break;
                    case "4":
                        System.out.print("Target account number: ");
                        String toAcc = sc.nextLine().trim();
                        Account dest = accounts.get(toAcc);
                        if (dest == null) {
                            System.out.println("No such account.");
                            break;
                        }
                        System.out.print("Amount to transfer: ");
                        double t = Double.parseDouble(sc.nextLine());
                        synchronized (this) {
                            acc.withdraw(t);
                            dest.deposit(t);
                            acc.addTransaction(new Transaction("Transfer Out", t, acc.getBalance(),
                                    LocalDateTime.now(), "To " + toAcc));
                            dest.addTransaction(new Transaction("Transfer In", t, dest.getBalance(),
                                    LocalDateTime.now(), "From " + acc.getAccountNumber()));
                        }
                        System.out.println("Transfer successful.");
                        break;
                    case "5":
                        List<Transaction> stm = acc.getMiniStatement(5);
                        System.out.println("Last transactions:");
                        for (Transaction tx : stm) System.out.println(tx);
                        break;
                    case "6":
                        System.out.print("New PIN: ");
                        String np = sc.nextLine().trim();
                        acc.changePin(np);
                        System.out.println("PIN changed successfully.");
                        break;
                    case "7":
                        System.out.println("Logged out.");
                        return;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new ATMProject().start();
    }
}
	