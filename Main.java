import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

import java.util.Set;
import java.util.HashSet;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;



public class Main {
    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(1);
        LS.showLoadingScreen(latch);
        
        try {
            latch.await(); // Wait until the latch counts down to 0
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Connection[] conn = {null}; // Declare as final array
        try {
            String url = "jdbc:oracle:thin:@localhost:1521:xe";
            String username = "SOURAV";
            String pass = "sourav";
            conn[0] = DriverManager.getConnection(url, username, pass); // Assign to array
            System.out.println("Success");

            SwingUtilities.invokeLater(() -> {
                mainGUI gui = new mainGUI(conn[0]); // Use array element
                gui.setVisible(true);
            });
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage());
        }
    }
}

class LS {
    public static void showLoadingScreen(CountDownLatch latch) {
        JFrame loadingFrame = new JFrame("Loading...");
        loadingFrame.setUndecorated(true);
        loadingFrame.setSize(300, 200);
        loadingFrame.setLocationRelativeTo(null);

        ImageIcon imageIcon = new ImageIcon("branding-image.jpg");
        JLabel imageLabel = new JLabel(imageIcon);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(imageLabel, BorderLayout.CENTER);

        // Create the loading bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setOrientation(JProgressBar.HORIZONTAL);
        progressBar.setStringPainted(true);
        progressBar.setString("");

        // Set the foreground color of the loading bar to green
        progressBar.setForeground(Color.GREEN);

        // Set the color of the text inside the loading bar to black
        UIManager.put("ProgressBar.selectionForeground", Color.BLACK);
        progressBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionForeground() {
                return Color.BLACK;
            }
        });

        panel.add(progressBar, BorderLayout.SOUTH);

        // Add the panel to the frame
        loadingFrame.add(panel);

        loadingFrame.setVisible(true);

        // Timer to update the progress
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int progress = 0;

            public void run() {
                progress += 20; // Increment the progress
                if (progress <= 100) {
                    progressBar.setValue(progress); // Set the progress value
                    progressBar.setString(progress + "%"); // Update progress string
                } else {
                    loadingFrame.dispose(); // Dispose of the loading frame after completion
                    latch.countDown(); // Release the latch after the loading frame is disposed
                    cancel(); // Cancel the timer
                }
            }
        }, 0, 400); // Update progress every 500 milliseconds
    }
}







class mainGUI extends JFrame {
    
    Connection conn;
    mainGUI(Connection conn) {
super("Class Management System");
        this.conn = conn;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Class Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridBagLayout());

        // Left panel for text input and choose existing button
        JPanel leftPanel = new JPanel(new GridLayout(5, 1, 10, 10)); // Changed to GridLayout
        JTextField marksheetNameField = new JTextField();
        marksheetNameField.setPreferredSize(new Dimension(200, 30));
        leftPanel.add(marksheetNameField);

        JButton chooseExistingButton = new JButton("Choose an Mark Sheet");
        chooseExistingButton.setPreferredSize(new Dimension(200, 30)); // Set same size as other buttons
        chooseExistingButton.setBackground(Color.BLUE);
        chooseExistingButton.setForeground(Color.WHITE);
        leftPanel.add(chooseExistingButton);

        // Add empty panels to match the number of buttons on the right side
        leftPanel.add(new JPanel());
        leftPanel.add(new JPanel());
        leftPanel.add(new JPanel());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(80, 0, 0, 20); // Space between left and right panels
        centerPanel.add(leftPanel, gbc);

        // Right panel for the remaining buttons
        JPanel rightPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton showTableButton = new JButton("Show Mark Sheets");
        JButton createNewButton = new JButton("Create New Mark Sheet");
        JButton deleteTableButton = new JButton("Delete Mark Sheet");
        JButton quitButton = new JButton("Quit Program");

        showTableButton.setPreferredSize(new Dimension(200, 30));
        createNewButton.setPreferredSize(new Dimension(200, 30));
        deleteTableButton.setPreferredSize(new Dimension(200, 30));
        quitButton.setPreferredSize(new Dimension(200, 30));
        rightPanel.add(showTableButton);
        rightPanel.add(createNewButton);
        rightPanel.add(deleteTableButton);
        rightPanel.add(quitButton);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx =0.5;
        gbc.weighty = 0.5;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(rightPanel, gbc);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Action listeners for buttons
        showTableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showClasses(conn);
            }
        });


        chooseExistingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String marksheetName = marksheetNameField.getText().trim().toUpperCase();
                if (marksheetName.isEmpty()) {
                    JOptionPane.showMessageDialog(mainGUI.this, "Please enter a mark sheet name.");
                } else {
                    if (isTableExists(conn, marksheetName + "_Mark")) {
                        intermediatePanel(conn, marksheetName);
                    } else {
                        JOptionPane.showMessageDialog(mainGUI.this,
                                "Mark sheet '" + marksheetName + "' does not exist.");
                    }
                }
            }
        });

        createNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String marksheetName = marksheetNameField.getText().trim();
                if (marksheetName.isEmpty()) {
                    JOptionPane.showMessageDialog(mainGUI.this, "Please enter a mark sheet name.");
                } else if (!marksheetName.toLowerCase().startsWith("cse") &&
                        !marksheetName.toLowerCase().startsWith("it") &&
                        !marksheetName.toLowerCase().startsWith("csse") &&
                        !marksheetName.toLowerCase().startsWith("csce")) {
                    JOptionPane.showMessageDialog(mainGUI.this, "Mark sheet name must start with 'CSE, IT, CSSE, or CSCE'.");
                } else {
                    newClass(conn, marksheetName);
                }
            }
        });

        deleteTableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteClass(conn);
            }
        });

        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainGUI.this,
                            "Error occurred while closing connection: " + ex.getMessage());
                }
                dispose(); // Close the JFrame
            }
        });
    }

    private boolean isTableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName.toUpperCase(), null);
            return tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void intermediatePanel(Connection conn, String marksheet_name) {
        JFrame frame1 = new JFrame("Choose"); // Create a Jframe for the dashboard
        frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame1.setSize(400, 250);
        frame1.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout()); // Use BorderLayout for main panel

        JPanel titlePanel = new JPanel(new BorderLayout()); // Panel for title
        JLabel titleLabel = new JLabel("SECTION: " + marksheet_name);

        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setPreferredSize(new Dimension(20, 30));

        panel.add(titlePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        buttonPanel.setBackground(Color.blue);

        String[] menuOptions = { "Attendance", "Marksheet", "Show Students", "Edit Student ", "Add Student ",
                "Delete Student ", "Quit program" };

        for (int i = 0; i < menuOptions.length; i++) {

            JButton button = new JButton(menuOptions[i]);

            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String buttonText = ((JButton) e.getSource()).getText();
                    switch (buttonText) {
                    case "Attendance":
                        if (hasStudents(conn, marksheet_name)) {
                            attendanceDashbord(conn, marksheet_name);
                        } else {
                            JOptionPane.showMessageDialog(frame1, "No students were inserted.");
                        }
                        break;
                    case "Marksheet":
                        if (hasStudents(conn, marksheet_name)) {
                            dashboard(conn, marksheet_name);
                        } else {
                            JOptionPane.showMessageDialog(frame1, "No students were inserted.");
                        }
                        break;
                    case "Show Students":
                        showStudents(conn, marksheet_name + "_Mark");
                        break;
                    case "Edit Student ":
                        editStudent(conn, marksheet_name);
                        break;
                    case "Add Student ":
                        addStudent(conn, marksheet_name);
                        break;
                    case "Delete Student ":
                        deleteStudent(conn, marksheet_name);
                        break;
                    case "Quit program":

                        frame1.dispose(); // Close the Jframe1
                        break;
                    }
                }
            });
            buttonPanel.add(button); // Add button to the button panel
        }
        panel.add(buttonPanel, BorderLayout.CENTER); // Add button panel to the main panel
        frame1.add(panel);
        frame1.setVisible(true);
    }

    //////////////////////////////
    private boolean hasStudents(Connection conn, String marksheet_name) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + marksheet_name + "_Mark");
            rs.next();
            int count = rs.getInt(1);
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    ////////////////////////

    // Attendance DashBord
    public void attendanceDashbord(Connection conn, String class_name) {
        String marksheet_name = class_name + "_Attendence";
        JFrame frame = new JFrame("Dashboard"); // Create a JFrame for the dashboard
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout()); // Use BorderLayout for main panel

        JPanel titlePanel = new JPanel(new BorderLayout()); // Panel for title
        // titlePanel.setBackground(Color.blue);
        JLabel titleLabel = new JLabel("ATTENDANCE Dashboard of: " + class_name);

        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setPreferredSize(new Dimension(40, 40));

        panel.add(titlePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        buttonPanel.setBackground(Color.blue);

        String[] menuOptions = { "Show attendance of all Student", "Show attendace of a student",
                "Add/Update Attendance", "Quit program" };
        for (int i = 0; i < menuOptions.length; i++) {

            JButton button = new JButton(menuOptions[i]);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String buttonText = ((JButton) e.getSource()).getText();
                    switch (buttonText) {
                    case "Show attendance of all Student":
                        ShowAttendanceOfAll(conn, marksheet_name);
                        break;
                    case "Show attendace of a student":
                        ShowAttendance(conn, marksheet_name);
                        break;
                    case "Add/Update Attendance":
                        AddstudentAttendance(conn, marksheet_name);
                        break;
                    case "Quit program":
                        frame.dispose(); // Close the JFrame
                        break;
                    }
                }
            });
            buttonPanel.add(button); // Add button to the button panel
        }
        panel.add(buttonPanel, BorderLayout.CENTER); // Add button panel to the main panel

        frame.add(panel);
        frame.setVisible(true);

    }

    public void ShowAttendanceOfAll(Connection conn, String marksheet_name) {

        try {
            String query = "SELECT rollno, name, Java_per as JAVA , Dbms_per as DBMS, Coa_per as COA, DM_per as DM FROM "
                    + marksheet_name;
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet set = pstmt.executeQuery();

            // Create a JTable to display the result set
            JTable table = new JTable(buildTableModel(set));

            // Show the table in a scroll pane
            JScrollPane scrollPane = new JScrollPane(table);
            JOptionPane.showMessageDialog(null, scrollPane, "Mark Sheet (Percentage): " + marksheet_name,
                    JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Error occurred in displayTable(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);

        }

    }

    public void ShowAttendance(Connection conn, String className) {
        try {
            int rollNumber = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter the Roll number:"));

            if (rollNumber != 0) {
                String query = "SELECT * FROM " + className + " WHERE Rollno = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, rollNumber); // Set rollNumber as parameter
                ResultSet set = pstmt.executeQuery();

                if (set.next()) { // Check if result set has data
                    // Create a message to display the result set
                    String message = "Roll Number: " + set.getInt("Rollno") + "\n" + "Name: " + set.getString("Name")+ "\n" 
                            
                            + "Java:     Present    Absent    Percentage" + "\n" + 
                            "                  " + set.getInt("Java_P") + 
                            "               " + set.getInt("Java_A") + 
                            "               " + set.getDouble("Java_Per") + "\n" 
                            
                            + "DBMS:   Present    Absent    Percentage" + "\n" +
                            "                   " + set.getInt("Dbms_P") + 
                            "              " + set.getInt("Dbms_A") +
                            "                " + set.getDouble("Dbms_Per") + "\n"
                            
                            + "COA:     Present    Absent    Percentage" + "\n" + 
                            "                  "+ set.getInt("Coa_P") + 
                            "               " + set.getInt("Coa_A") + 
                            "               " + set.getDouble("Coa_Per") + "\n" 
                            
                            + "DM:       Present    Absent    Percentage" + "\n"
                            + "                  " + set.getInt("Dm_P") + 
                            "               " + set.getInt("Dm_A")
                            + "               " + set.getDouble("Dm_Per") + "\n";
                    
                    JOptionPane.showMessageDialog(null, message, "Attendance Details for Roll Number " + rollNumber,
                            JOptionPane.PLAIN_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "No data found for roll number: " + rollNumber, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid integer for Roll Number.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Exception occurred: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void AddstudentAttendance(Connection conn, String marksheetName) {
        try {
            JTextField rollNumberField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(new JLabel("Enter the roll no of student to fetch: "));
            panel.add(rollNumberField);
            String rollNumberInput = JOptionPane.showInputDialog(null, "Enter Roll Number:");
            if (rollNumberInput != null && !rollNumberInput.isEmpty()) {
                int result = Integer.parseInt(rollNumberInput);
                try {
                    double details[][] = new double[4][3];
                    String[] subjects = { "Java", "Coa", "Dm", "Dbms" };
                    for (int i = 0; i < 4; i++) {
                        JTextField presentField = new JTextField();
                        JTextField absentField = new JTextField();
                        Object[] fields = { "Enter Number of Days Present name:", presentField,
                                "Enter number of Days Absent", absentField, };
                        int option = JOptionPane.showConfirmDialog(null, fields, subjects[i],
                                JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            double present = Double.parseDouble(presentField.getText());
                            double absent = Double.parseDouble(absentField.getText());
                            double totalDays = present + absent;
                            double percentage = (present / totalDays) * 100;
                            details[i][0] = present;
                            details[i][1] = absent;
                            details[i][2] = percentage;
                            JOptionPane.showMessageDialog(null, "Attendance Entered.");
                        } else {
                            JOptionPane.showMessageDialog(null, "Attendance not Entered", "Info",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    String query = "UPDATE " + marksheetName + " SET Java_P=" + details[0][0] + ", Java_A="
                            + details[0][1] + ", Java_Per=" + details[0][2] + ", Dbms_P=" + details[1][0] + ", Dbms_A="
                            + details[1][1] + ", Dbms_Per=" + details[1][2] + ", Coa_P=" + details[2][0] + ", Coa_A="
                            + details[2][1] + ", Coa_Per=" + details[2][2] + ", Dm_P=" + details[3][0] + ", Dm_A="
                            + details[3][1] + ", Dm_Per=" + details[3][2] + " WHERE Rollno=" + result;
                    System.out.println("Generated Query: " + query); // Print the generated query
                    Statement pstmt = conn.createStatement();
                    int rowsAffected = pstmt.executeUpdate(query);
                    System.out.println("Rows Affected: " + rowsAffected);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Please enter valid numeric values for roll number and marks.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dashboard
    public void dashboard(Connection conn, String class_name) {
        String marksheet_name = class_name + "_Mark";
        JFrame frame = new JFrame("Dashboard"); // Create a JFrame for the dashboard
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout()); // Use BorderLayout for main panel

        JPanel titlePanel = new JPanel(new BorderLayout()); // Panel for title
        // titlePanel.setBackground(Color.blue);

        JLabel titleLabel = new JLabel("MARKSHEET Dashboard of: " + class_name);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.setPreferredSize(new Dimension(40, 30));

        panel.add(titlePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));
        buttonPanel.setBackground(Color.blue);

        String[] menuOptions = { "Display Marksheet", "Add/Edit Student Mark", "Show Student details from roll",
                "Quit program" };

        for (int i = 0; i < menuOptions.length; i++) {
            JButton button = new JButton(menuOptions[i]);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String buttonText = ((JButton) e.getSource()).getText();
                    switch (buttonText) {
                    case "Display Marksheet":
                        displayTable(conn, marksheet_name);
                        break;
                    case "Add/Edit Student Mark":
                        addStudentMark(conn, marksheet_name);
                        break;
                    case "Show Student details from roll":
                        showStudent_mark(conn, marksheet_name);
                        break;
                    case "Quit program":
                        frame.dispose(); // Close the JFrame
                        break;
                    }
                }
            });
            buttonPanel.add(button); // Add button to the button panel
        }
        panel.add(buttonPanel, BorderLayout.CENTER); // Add button panel to the main panel

        frame.add(panel);
        frame.setVisible(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void displayTable(Connection conn, String marksheet_name) {
        try {
            String query = "SELECT * FROM " + marksheet_name;
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet set = pstmt.executeQuery();

            // Create a JTable to display the result set
            JTable table = new JTable(buildTableModel(set));

            // Show the table in a scroll pane
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Mark Sheet: " + marksheet_name, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error occurred in displayTable(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Helper method to convert ResultSet to DefaultTableModel
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Get column names
        int columnCount = metaData.getColumnCount();
        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Get row data
        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                row.add(rs.getObject(columnIndex));
            }
            data.add(row);
        }
        return new DefaultTableModel(data, columnNames);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void addStudent(Connection conn, String marksheetName) {
        try {
            int noOfStudents = Integer
                    .parseInt(JOptionPane.showInputDialog(null, "Enter the number of students to add:"));
            if (noOfStudents <= 0) {
                JOptionPane.showMessageDialog(null, "Number of students must be greater than zero.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // PreparedStatement to prevent SQL Injection
            String query = "INSERT INTO " + marksheetName + "_Attendence (Rollno, Name) VALUES (?, ?)";
            String query2 = "INSERT INTO " + marksheetName + "_Mark (Rollno, Name) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            PreparedStatement pstmt2 = conn.prepareStatement(query2);
            for (int i = 0; i < noOfStudents; i++) {
                JTextField nameField = new JTextField();
                JTextField rollnoField = new JTextField();

                Object[] fields = { "Enter student name:", nameField, "Enter student roll number:", rollnoField, };

                int option = JOptionPane.showConfirmDialog(null, fields, "Add Student Details",
                        JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    int rollno = Integer.parseInt(rollnoField.getText());
                    String name = nameField.getText();

                    pstmt.setInt(1, rollno);
                    pstmt.setString(2, name);
                    pstmt.executeUpdate();
                    pstmt2.setInt(1, rollno);
                    pstmt2.setString(2, name);
                    pstmt2.executeUpdate();
                } else {
                    JOptionPane.showMessageDialog(null, "Student details not added.", "Info",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }
            JOptionPane.showMessageDialog(null, "Student details added successfully.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid numeric values for roll number and marks.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in addStudentDetails(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void addStudentMark(Connection conn, String marksheetName) {
        try {
            int noOfStudents = Integer
                    .parseInt(JOptionPane.showInputDialog(null, "Enter the number of students to add:"));
            if (noOfStudents <= 0) {
                JOptionPane.showMessageDialog(null, "Number of students must be greater than zero.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "UPDATE " + marksheetName + " SET Java=?, Dbms=?, Coa=?, Dm=?,AVG=? WHERE Rollno=?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                for (int i = 0; i < noOfStudents; i++) {
                    JTextField rollNumberField = new JTextField();
                    JPanel panel = new JPanel(new GridLayout(2, 1));
                    panel.add(new JLabel("Enter the roll no of student to fetch: "));
                    panel.add(rollNumberField);

                    String rollNumberInput = JOptionPane.showInputDialog(null, "Enter Roll Number:");
                    if (rollNumberInput != null && !rollNumberInput.isEmpty()) {
                        int result = Integer.parseInt(rollNumberInput);

                        JTextField JavaField = new JTextField();
                        JTextField DbmsField = new JTextField();
                        JTextField CoaField = new JTextField();
                        JTextField DmField = new JTextField();

                        Object[] fields = { "Enter marks of Java:", JavaField, "Enter marks of Dbms:", DbmsField,
                                "Enter marks of Coa:", CoaField, "Enter marks of Dm:", DmField };

                        int option = JOptionPane.showConfirmDialog(null, fields, "Add Student Details",
                                JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            int Java = Integer.parseInt(JavaField.getText());
                            int Dbms = Integer.parseInt(DbmsField.getText());
                            int Coa = Integer.parseInt(CoaField.getText());
                            int Dm = Integer.parseInt(DmField.getText());
                            double AVG = (Java + Dbms + Coa + Dm) / 4;

                            pstmt.setInt(1, Java);
                            pstmt.setInt(2, Dbms);
                            pstmt.setInt(3, Coa);
                            pstmt.setInt(4, Dm);
                            pstmt.setDouble(5, AVG);
                            pstmt.setInt(6, result); // Set Rollno
                            pstmt.executeUpdate();
                            JOptionPane.showMessageDialog(null, "Student details added successfully.");
                        } else {
                            JOptionPane.showMessageDialog(null, "Student details not added.", "Info",
                                    JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid Roll Number.", "Error", JOptionPane.ERROR_MESSAGE);
                        return; // Exit method if Roll Number is invalid
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid numeric values for roll number and marks.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in addStudentDetails(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void showStudent_mark(Connection conn, String marksheet_name) {
        try {
            JTextField rollNumberField = new JTextField();
            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(new JLabel("Enter the roll no of student to fetch: "));
            panel.add(rollNumberField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Enter Roll Number", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                int roll = Integer.parseInt(rollNumberField.getText());
                String query = "SELECT Name,Java, Dbms, Coa, Dm,AVG FROM " + marksheet_name + " WHERE Rollno = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, roll);
                ResultSet set = pstmt.executeQuery();

                if (set.next()) {
                    String name = set.getString(1);
                    int Java = set.getInt(2);
                    int Dbms = set.getInt(3);
                    int Coa = set.getInt(4);
                    int Dm = set.getInt(5);
                    int AVG = set.getInt(5);

                    StringBuilder message = new StringBuilder();
                    message.append("Marks for Roll No ").append(roll).append("\n");
                    message.append("Name: ").append(name).append("\n");
                    message.append("Java: ").append(Java).append("\n");
                    message.append("Dbms: ").append(Dbms).append("\n");
                    message.append("Coa: ").append(Coa).append("\n");
                    message.append("Dm: ").append(Dm).append("\n");
                    message.append("Avg: ").append(AVG).append("\n");

                    JOptionPane.showMessageDialog(null, message.toString(), "Student Details",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Student with roll number " + roll + " not found.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error occurred: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /////////////////////////////////////
    public void showClasses(Connection conn) {
    try {
        String query = "SELECT * FROM TAB WHERE TNAME LIKE 'CSE%' OR TNAME LIKE 'IT%' OR TNAME LIKE 'CSSE%' OR TNAME LIKE 'CSCE%'";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet set = pstmt.executeQuery();

        Set<String> classNames = new HashSet<>();
        while (set.next()) {
            String tableName = set.getString("TNAME");
            String[] parts = tableName.split("_"); // Splitting the table name by underscore
            if (parts.length >= 2) {
                classNames.add(parts[0]); // Extracting the class identifier and adding to the set
            }
        }

        StringBuilder message = new StringBuilder();
        message.append("Here are the list of Marksheets in our school:\n");
        for (String className : classNames) {
            message.append(className).append("\n");
        }

        JOptionPane.showMessageDialog(null, message.toString(), "List of Marksheets",
                JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error in showClasses(): " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void showStudents(Connection conn, String marksheet_name) {
        try {
            String query = "SELECT Rollno,name FROM " + marksheet_name;
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet set = pstmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("Here are the list of students:\n");
            while (set.next()) {
                String tableName = set.getString(1);
                String tableName2 = set.getString(2);

                message.append(tableName).append(" , ");
                message.append(tableName2).append("\n");
            }

            JTextArea textArea = new JTextArea(message.toString());
            textArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setPreferredSize(new Dimension(300, 200)); // Set preferred size here

            JOptionPane.showMessageDialog(null, scrollPane, "List of Marksheets", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error in showClasses(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    ////////////////////////////////////////////////////////////
    public void newClass(Connection conn, String marksheet_name) {
        try {
            String className = marksheet_name;
            if (!className.isEmpty()) {
                // Create the table in the database
                String query = "CREATE TABLE " + className
                        + "_Mark (Rollno NUMBER(8) Primary key, Name VARCHAR2(45), Java NUMBER(5,2), Dbms NUMBER(5,2), Coa NUMBER(5,2), Dm NUMBER(5,2), AVG NUMBER(5,2))";
                String query2 = "CREATE TABLE " + className
                        + "_Attendence (Rollno NUMBER(8) Primary key, Name VARCHAR2(45), Java_P NUMBER(3),Java_A NUMBER(3),Java_Per NUMBER(5,2), Dbms_P NUMBER(3),Dbms_A NUMBER(3),Dbms_Per NUMBER(5,2), Coa_P NUMBER(3),Coa_A NUMBER(3),Coa_Per NUMBER(5,2), Dm_P NUMBER(3),Dm_A NUMBER(3),Dm_Per NUMBER(5,2))";
                PreparedStatement pstmt = conn.prepareStatement(query);
                PreparedStatement pstmt2 = conn.prepareStatement(query2);
                pstmt.executeUpdate();
                pstmt2.executeUpdate();
                JOptionPane.showMessageDialog(null, "Class " + className + " added successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a class name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in createNewClass(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    public static void editStudent(Connection conn, String marksheetName) {
        try {
            String query = "UPDATE " + marksheetName + " SET Name=? WHERE Rollno=?";
            PreparedStatement pstmt = conn.prepareStatement(query);

            JTextField rollnoField = new JTextField();
            JTextField nameField = new JTextField();

            Object[] fields = { "Enter the Rollno whose details has to be changed:", rollnoField, "Enter new name:",
                    nameField,

            };

            int option = JOptionPane.showConfirmDialog(null, fields, "Edit MarkSheet", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int rollno = Integer.parseInt(rollnoField.getText());
                String name = nameField.getText();

                pstmt.setString(1, name);
                pstmt.setInt(6, rollno);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Student details updated.");
            } else {
                JOptionPane.showMessageDialog(null, "Student details not updated.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid numeric values for roll number and marks.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in editStudent(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void deleteStudent(Connection conn, String marksheetName) {
        try {
            JTextField rollnoField = new JTextField();
            Object[] fields = { "Enter the roll no of student to delete:", rollnoField };
            int option = JOptionPane.showConfirmDialog(null, fields, "Delete Student", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                int rollno = Integer.parseInt(rollnoField.getText());

                String query = "DELETE FROM " + marksheetName + "_Attendence" + " WHERE Rollno=?";
                String query2 = "DELETE FROM " + marksheetName + "_Mark" + " WHERE Rollno=?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                PreparedStatement pstmt2 = conn.prepareStatement(query2);
                pstmt.setInt(1, rollno);
                pstmt.executeUpdate();
                pstmt2.setInt(1, rollno);
                pstmt2.executeUpdate();

                JOptionPane.showMessageDialog(null, "Student details deleted successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Student details not deleted.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid integer for roll number.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in deleteStudent(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    public static void deleteClass(Connection conn) {
        try {
            // Create a JTextField for marksheet name input
            JTextField marksheetNameField = new JTextField();
            Object[] message = { "Enter the name of marksheet to delete:", marksheetNameField };
            int option = JOptionPane.showConfirmDialog(null, message, "Delete Marksheet", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String marksheetName = marksheetNameField.getText();
                if (!marksheetName.isEmpty()) {
                    // Delete the table from the database
                    String query = "DROP TABLE " + marksheetName + "_Mark";
                    String query2 = "DROP TABLE " + marksheetName + "_Attendence";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    PreparedStatement pstmt2 = conn.prepareStatement(query2);
                    pstmt.executeUpdate();
                    pstmt2.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Marksheet " + marksheetName + " deleted successfully");
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a marksheet name.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error in deleteClass(): " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
