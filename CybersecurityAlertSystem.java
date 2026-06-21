import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// -------------------- BASIC CLASSES --------------------
abstract class Person {
    String userId;
    String username;
    abstract void displayInfo();
}

class User extends Person {
    String password;
    int loginAttempts;
    boolean blocked;

    User(String id, String name, String pass) {
        userId = id;
        username = name;
        password = pass;
        blocked = false;
        loginAttempts = 0;
    }

    boolean login(String pass) {
        if (blocked) return false;
        return password.equals(pass);
    }

    void displayInfo() {
        System.out.println(username);
    }
}

class Admin extends User {
    NodeAlert alertListHead;

    Admin(String id, String name, String pass) {
        super(id, name, pass);
    }
}

class Alert {
    static int count = 0;
    int alertId;
    String type;
    String user;

    Alert(String t, String u) {
        alertId = ++count;
        type = t;
        user = u;
    }

    public String toString() {
        return "ID:" + alertId + " | " + type + " | " + user;
    }
}

// -------------------- LINKED LIST FOR USERS --------------------
class NodeUser {
    User data;
    NodeUser next;

    NodeUser(User u) {
        data = u;
        next = null;
    }
}

class UserList {
    NodeUser head;

    void add(User u) {
        NodeUser n = new NodeUser(u);
        if (head == null) {
            head = n;
        } else {
            NodeUser temp = head;
            while (temp.next != null) temp = temp.next;
            temp.next = n;
        }
    }
}

// -------------------- LINKED LIST FOR ALERTS --------------------
class NodeAlert {
    Alert data;
    NodeAlert next;

    NodeAlert(Alert a) {
        data = a;
        next = null;
    }
}

// -------------------- SYSTEM MONITOR --------------------
class SystemMonitor {
    UserList users = new UserList();
    NodeAlert alertQueueFront, alertQueueRear;
    Admin admin;

    SystemMonitor() {
        admin = new Admin("0", "admin", "admin");
        users.add(admin);
    }

    void registerUser(String name, String pass) {
        try {
            if (name.isEmpty() || pass.isEmpty()) {
                throw new IllegalArgumentException("Username and Password required!");
            }

            int id = countUsers() + 1;
            User newUser = new User(String.valueOf(id), name, pass);
            users.add(newUser);
            JOptionPane.showMessageDialog(null, "User registered!");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error during registration.");
        }
    }

    boolean login(String name, String pass) {
        try {
            if (name.isEmpty() || pass.isEmpty()) {
                throw new IllegalArgumentException("Username and Password required!");
            }

            NodeUser temp = users.head;
            while (temp != null) {
                User u = temp.data;
                if (u.username.equals(name)) {
                    if (u.blocked) throw new SecurityException("User is blocked!");

                    if (u.login(pass)) {
                        u.loginAttempts = 0;
                        return true;
                    } else {
                        u.loginAttempts++;
                        if (u.loginAttempts >= 3) {
                            u.blocked = true;
                            Alert a = new Alert("Failed Login", u.username);
                            enqueueAlert(a);
                            admin.alertListHead = addAlertToList(admin.alertListHead, a);
                        }
                        throw new SecurityException("Invalid password!");
                    }
                }
                temp = temp.next;
            }
            throw new IllegalArgumentException("User not found!");
        } catch (IllegalArgumentException | SecurityException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error during login.");
        }
        return false;
    }

    boolean unblockUser(String name) {
        try {
            if (name.isEmpty()) throw new IllegalArgumentException("Enter username!");

            NodeUser temp = users.head;
            while (temp != null) {
                User u = temp.data;
                if (u.username.equals(name)) {
                    if (u.blocked) {
                        u.blocked = false;
                        u.loginAttempts = 0;
                        return true;
                    } else {
                        throw new IllegalArgumentException("User is not blocked!");
                    }
                }
                temp = temp.next;
            }
            throw new IllegalArgumentException("User not found!");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected error while unblocking.");
        }
        return false;
    }

    void enqueueAlert(Alert a) {
        NodeAlert n = new NodeAlert(a);
        if (alertQueueRear == null) {
            alertQueueFront = alertQueueRear = n;
        } else {
            alertQueueRear.next = n;
            alertQueueRear = n;
        }
    }

    NodeAlert addAlertToList(NodeAlert head, Alert a) {
        NodeAlert n = new NodeAlert(a);
        if (head == null) return n;
        NodeAlert temp = head;
        while (temp.next != null) temp = temp.next;
        temp.next = n;
        return head;
    }

    int countUsers() {
        int count = 0;
        NodeUser temp = users.head;
        while (temp != null) {
            count++;
            temp = temp.next;
        }
        return count;
    }
}

// -------------------- GUI --------------------
public class CybersecurityAlertSystem extends JFrame implements ActionListener {
    SystemMonitor monitor = new SystemMonitor();
    JTextArea alertArea;

    JButton btnLogin, btnRegister, btnViewAlerts, btnUnblock;
    JTextField txtUser, txtName;
    JPasswordField txtPass;

    public CybersecurityAlertSystem() {
        setTitle("Cybersecurity Alert System");
        setSize(400, 280);
        setMinimumSize(new Dimension(400, 280));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Login/Register", loginPanel());
        tabs.add("Admin Panel", adminPanel());
        tabs.add("Unblock User", unblockPanel());
        add(tabs);

        setVisible(true);
    }

    JPanel loginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        txtUser = new JTextField(12);
        JLabel passwordLabel = new JLabel("Password:");
        txtPass = new JPasswordField(12);

        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register");
        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(txtUser, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(txtPass, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    JPanel adminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        alertArea = new JTextArea(8, 30);
        alertArea.setEditable(false);

        btnViewAlerts = new JButton("View Alerts");
        btnViewAlerts.addActionListener(this);

        panel.add(new JScrollPane(alertArea), BorderLayout.CENTER);
        panel.add(btnViewAlerts, BorderLayout.SOUTH);
        return panel;
    }

    JPanel unblockPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel unblockLabel = new JLabel("Enter username to unblock:");
        txtName = new JTextField(12);
        btnUnblock = new JButton("Unblock User");
        btnUnblock.addActionListener(this);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(unblockLabel, gbc);
        gbc.gridy = 1;
        panel.add(txtName, gbc);
        gbc.gridy = 2;
        panel.add(btnUnblock, gbc);

        return panel;
    }

    // -------------------- Button Actions --------------------
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            // Get username and password
            String name = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            // Attempt login
            boolean ok = monitor.login(name, pass);

            // Expanded if-else
            if (ok == true) {
                JOptionPane.showMessageDialog(null, "Login Success!");
            } else {
                JOptionPane.showMessageDialog(null, "Login Failed!");
            }
        }
        else if (e.getSource() == btnRegister) {
            String name = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            monitor.registerUser(name, pass);
        }
        else if (e.getSource() == btnViewAlerts) {
            alertArea.setText("");
            NodeAlert temp = monitor.admin.alertListHead;
            while (temp != null) {
                alertArea.append(temp.data.toString() + "\n");
                temp = temp.next;
            }
        }
        else if (e.getSource() == btnUnblock) {
            String name = txtName.getText().trim();
            monitor.unblockUser(name);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CybersecurityAlertSystem();
            }
        });
    }
}