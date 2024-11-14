import java.sql.*;
import java.util.Scanner;

// Abstract ATM class with common properties and abstract methods
abstract class ATM_abstract {
    protected static final String URL = "jdbc:mysql://localhost:3306/atm_db";
    protected static final String USER = "root"; // Replace with your MySQL username
    protected static final String PASSWORD = "14-Jun-06"; // Replace with your MySQL password
    protected static Connection connection;

    public ATM_abstract() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    protected abstract void createAccount(Scanner scanner);

    protected abstract void login(Scanner scanner);
}


class AccountManager extends ATM_abstract {

    public AccountManager() {
        super();
    }

    
    protected void createAccount(Scanner scanner) {
        try {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();
            System.out.print("Enter your PIN: ");
            String pin = scanner.nextLine();
            System.out.print("Enter account type (Checking/Savings): ");
            String accountType = scanner.nextLine();
            System.out.print("Enter your mobile number: ");
            String mobileNumber = scanner.nextLine();
            double balance = 0.0;

            String query = "INSERT INTO accounts (account_number, name, pin, account_type, mobile_number, balance, created_at) " +
                           "VALUES (?, ?, ?, ?, ?, ?, NOW())";
            PreparedStatement statement = connection.prepareStatement(query);

            String accountNumber = generateAccountNumber();

            statement.setString(1, accountNumber);
            statement.setString(2, name);
            statement.setString(3, pin);
            statement.setString(4, accountType);
            statement.setString(5, mobileNumber);
            statement.setDouble(6, balance);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account created successfully! Your account number is: " + accountNumber);
            } else {
                System.out.println("Error creating the account.");
            }

        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    
    protected void login(Scanner scanner) {
        System.out.print("Enter your account number: ");
        String accountNumber = scanner.nextLine();

        if (accountExists(accountNumber)) {
            System.out.print("Enter your PIN: ");
            String pin = scanner.nextLine();

            if (validatePin(accountNumber, pin)) {
                System.out.println("Login successful!");
                showAccountMenu(scanner, accountNumber);
            } else {
                System.out.println("Invalid PIN.");
            }
        } else {
            System.out.println("Account does not exist.");
        }
    }

    private void showAccountMenu(Scanner scanner, String accountNumber) {
        while (true) {
            System.out.println("\nAccount Menu:");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Change PIN");
            System.out.println("5. Logout");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    checkBalance(accountNumber);
                    break;
                case 2:
                    depositMoney(scanner, accountNumber);
                    break;
                case 3:
                    withdrawMoney(scanner, accountNumber);
                    break;
                case 4:
                    changePin(scanner, accountNumber);
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void checkBalance(String accountNumber) {
        try {
            String query = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, accountNumber);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                System.out.println("Your current balance is: $" + balance);
            }

        } catch (SQLException e) {
            System.out.println("Error checking balance: " + e.getMessage());
        }
    }

    private void depositMoney(Scanner scanner, String accountNumber) {
        try {
            System.out.print("Enter the amount to deposit: ");
            double depositAmount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            String query = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, depositAmount);
            statement.setString(2, accountNumber);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deposit successful.");
            }

        } catch (SQLException e) {
            System.out.println("Error depositing money: " + e.getMessage());
        }
    }

    private void withdrawMoney(Scanner scanner, String accountNumber) {
        try {
            System.out.print("Enter the amount to withdraw: ");
            double withdrawAmount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            String query = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, accountNumber);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");

                if (currentBalance >= withdrawAmount) {
                    String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setDouble(1, withdrawAmount);
                    updateStatement.setString(2, accountNumber);

                    int rowsAffected = updateStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Withdrawal successful.");
                    }
                } else {
                    System.out.println("Insufficient balance.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error withdrawing money: " + e.getMessage());
        }
    }

    private void changePin(Scanner scanner, String accountNumber) {
        try {
            System.out.print("Enter your current PIN: ");
            String currentPin = scanner.nextLine();

            if (validatePin(accountNumber, currentPin)) {
                System.out.print("Enter your new PIN: ");
                String newPin = scanner.nextLine();

                String query = "UPDATE accounts SET pin = ? WHERE account_number = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newPin);
                statement.setString(2, accountNumber);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("PIN changed successfully.");
                }
            } else {
                System.out.println("Incorrect current PIN.");
            }

        } catch (SQLException e) {
            System.out.println("Error changing PIN: " + e.getMessage());
        }
    }

    private boolean accountExists(String accountNumber) {
        try {
            String query = "SELECT * FROM accounts WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, accountNumber);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error checking if account exists: " + e.getMessage());
            return false;
        }
    }

    private boolean validatePin(String accountNumber, String pin) {
        try {
            String query = "SELECT * FROM accounts WHERE account_number = ? AND pin = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, accountNumber);
            statement.setString(2, pin);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error validating PIN: " + e.getMessage());
            return false;
        }
    }

    private String generateAccountNumber() {
        int accountNumber = (int)(Math.random() * 900000) + 1000000000;
        return String.valueOf(accountNumber);
    }
}

// MainATM class to handle the application flow and initialize the AccountManager
public class ATM {
    public static void main(String[] args) {
        AccountManager accountManager = new AccountManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nATM Menu:");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    accountManager.createAccount(scanner);
                    break;
                case 2:
                    accountManager.login(scanner);
                    break;
                case 3:
                    System.out.println("Exiting the ATM System.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
