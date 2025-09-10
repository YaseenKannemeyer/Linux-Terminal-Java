package mycput.ac.za.terminalapp;
/**
 *
 * @author mogamatyaseenkannemeyer
 */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import javax.swing.text.*;

public class TerminalApp {

    private JTextPane terminalOutput;
    private JTextField terminalInput;
    private FileSystemManager fsManager = new FileSystemManager();
    private CommandRegistry commandRegistry = new CommandRegistry();
    private Timer caretBlinkTimer;   // Declare the Timer here
    private boolean caretVisible = true;  // Declare the caret visibility flag

    public TerminalApp() {
        JFrame frame = new JFrame("kali@kali:~");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon backgroundImage = new ImageIcon("kali1.jpg");

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };

        terminalOutput = new JTextPane();
        terminalOutput.setEditable(false);
        terminalOutput.setOpaque(false); // Make it transparent
        terminalOutput.setForeground(Color.GREEN);
        terminalOutput.setFont(new Font("Monospaced", Font.PLAIN, 14));

        terminalOutput.setBackground(new Color(0, 0, 0, 50)); // semi-transparent black
        JScrollPane scrollPane = new JScrollPane(terminalOutput);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        terminalInput = new JTextField();
        terminalInput.setBackground(Color.BLACK);
        terminalInput.setForeground(Color.GREEN);
        terminalInput.setFont(new Font("Monospaced", Font.PLAIN, 14));

// Set a custom caret (cursor)
        terminalInput.setCaretColor(Color.GREEN);
        terminalInput.setCaretPosition(0);
        // Start a timer to blink the caret
        startBlinkingCaret();

        terminalInput.addActionListener(e -> processCommand(terminalInput.getText()));
        panel.add(terminalInput, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        printOutput("Welcome to Kali Terminal Simulator\nType 'help' to see available commands.");
        terminalInput.requestFocusInWindow();
        terminalInput.requestFocus();
    }

    private void startBlinkingCaret() {
        caretBlinkTimer = new Timer(500, e -> {
            caretVisible = !caretVisible;
            if (caretVisible) {
                terminalInput.setCaretColor(Color.GREEN); // Show caret
            } else {
                terminalInput.setCaretColor(Color.BLACK); // Hide caret
            }
        });
        caretBlinkTimer.start();
    }

    private void processCommand(String input) {
        input = input.trim();

        // Handle "clear" command
        if ("clear".equals(input)) {
            terminalOutput.setText("Type 'help' to see available commands.\n");

            // Re-display the terminal prompt after clearing
            appendStyledText("(kali", Color.CYAN);
            appendStyledText("@", Color.LIGHT_GRAY);
            appendStyledText("kali)-", Color.CYAN);
            appendStyledText("[" + fsManager.getCurrentPath() + "] ", new Color(128, 255, 128));
            appendStyledText("$ " + "" + "\n", Color.GREEN);

            terminalInput.setText("");  // Clear the input field
            terminalInput.requestFocusInWindow();
            terminalInput.requestFocus();

            // Ensure the caret is blinking again
            startBlinkingCaret();  // Restart the blinking caret timer

            return;
        }

        // Print styled prompt with input
        printOutput(input);

        // Execute command
        String result = "";
        if (input.contains(">>")) {
            // Append redirection
            String[] parts = input.split(">>", 2);
            String command = parts[0].trim();
            String file = parts[1].trim();
            String[] tokens = command.split("\\s+");
            result = commandRegistry.executeCommand(tokens, fsManager);
            fsManager.appendToFile(file, result);
            result = ""; // Don't print output to terminal
        } else if (input.contains(">")) {
            // Overwrite redirection
            String[] parts = input.split(">", 2);
            String command = parts[0].trim();
            String file = parts[1].trim();
            String[] tokens = command.split("\\s+");
            result = commandRegistry.executeCommand(tokens, fsManager);
            fsManager.writeToFile(file, result);
            result = ""; // Don't print output to terminal
        } else {
            // No redirection
            String[] tokens = input.split("\\s+");
            result = commandRegistry.executeCommand(tokens, fsManager);
        }

        // Print command output
        if (!result.isEmpty()) {
            appendStyledText(result + "\n", Color.WHITE); // Change color if needed
        }

        // Clear the input field
        terminalInput.setText("");
    }

    private void appendStyledText(String text, Color color) {
        StyledDocument doc = terminalOutput.getStyledDocument();
        Style style = terminalOutput.addStyle("Style", null);
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void printOutput(String text) {
        appendStyledText("(kali", Color.CYAN);
        appendStyledText("@", Color.LIGHT_GRAY);
        appendStyledText("kali)-", Color.CYAN);
        appendStyledText("[" + fsManager.getCurrentPath() + "] ", new Color(128, 255, 128));
        appendStyledText("$ " + text + "\n", Color.GREEN);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TerminalApp::new);
    }
}

interface Command {

    String execute(String[] args, FileSystemManager fsManager);
}

class CommandRegistry {

    private Map<String, Command> commandMap = new HashMap<>();

    public CommandRegistry() {
        register("pwd", (args, fs) -> fs.getCurrentPath());
        register("ls", (args, fs) -> fs.listContents());
        register("cd", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: cd <directory>";
            }
            return fs.changeDirectory(args[1]) ? "" : "Directory not found";
        });
        register("cat", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: cat <filename>";
            }
            return fs.readFile(args[1]);
        });
        register("help", (args, fs) -> {
            StringBuilder sb = new StringBuilder("Available commands:");
            for (String cmd : commandMap.keySet()) {
                sb.append("\n  ").append(cmd);
            }
            return sb.toString();
        });

        // Updated classes and logic for implementing extended command set in the TerminalApp
// Add to CommandRegistry constructor
        register("mkdir", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: mkdir <directory>";
            }
            return fs.makeDirectory(args[1]);
        });

        register("rmdir", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: rmdir <directory>";
            }
            return fs.removeDirectory(args[1]);
        });

        register("mv", (args, fs) -> {
            if (args.length < 3) {
                return "Usage: mv <source> <destination>";
            }
            return fs.move(args[1], args[2]);
        });

        register("cp", (args, fs) -> {
            if (args.length < 3) {
                return "Usage: cp <source> <destination>";
            }
            return fs.copy(args[1], args[2]);
        });

        register("rm", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: rm <target>";
            }
            return fs.remove(args[1]);
        });

        register("touch", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: touch <filename>";
            }
            return fs.touch(args[1]);
        });

        register("grep", (args, fs) -> {
            if (args.length < 3) {
                return "Usage: grep <pattern> <filename>";
            }
            return fs.grep(args[1], args[2]);
        });

        register("head", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: head <filename>";
            }
            return fs.head(args[1]);
        });

        register("tail", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: tail <filename>";
            }
            return fs.tail(args[1]);
        });

        register("diff", (args, fs) -> {
            if (args.length < 3) {
                return "Usage: diff <file1> <file2>";
            }
            return fs.diff(args[1], args[2]);
        });

        register("sort", (args, fs) -> {
            if (args.length < 2) {
                return "Usage: sort <filename>";
            }
            return fs.sort(args[1]);
        });

        register("chmod", (args, fs) -> {
            if (args.length < 3) {
                return "Usage: chmod <permissions> <filename>";
            }
            return fs.chmod(args[1], args[2]);
        });

        register("chown", (args, fs) -> {
            if (args.length < 3) {
                return "Usage: chown <owner> <filename>";
            }
            return fs.chown(args[1], args[2]);
        });

        register("uname", (args, fs) -> "Linux kali 5.15.0-kali3-amd64");
        register("whoami", (args, fs) -> "kali");
        register("date", (args, fs) -> java.time.LocalDateTime.now().toString());
        register("cal", (args, fs) -> fs.calendar());
        register("man", (args, fs) -> fs.manual(args.length > 1 ? args[1] : ""));
        register("echo", (args, fs) -> String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        register("clear", (args, fs) -> "\u000C"); // Form feed for clear simulation
        register("shutdown", (args, fs) -> "System shutting down...");
        register("reboot", (args, fs) -> "System rebooting...");

    }

    public void register(String name, Command command) {
        commandMap.put(name, command);
    }

    public String executeCommand(String[] input, FileSystemManager fsManager) {
        if (input.length == 0 || input[0].isEmpty()) {
            return "";
        }
        Command cmd = commandMap.get(input[0]);
        return (cmd != null) ? cmd.execute(input, fsManager) : "Command not found";
    }
}

class VirtualFile {

    private String name, content, permissions;

    public VirtualFile(String name, String content, String permissions) {
        this.name = name;
        this.content = content;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getPermissions() {
        return permissions;
    }
}

class VirtualDirectory {

    private String name;
    private VirtualDirectory parent;
    private List<VirtualDirectory> subDirs = new ArrayList<>();
    private List<VirtualFile> files = new ArrayList<>();

    public VirtualDirectory(String name, VirtualDirectory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public VirtualDirectory getParent() {
        return parent;
    }

    public List<VirtualDirectory> getSubDirectories() {
        return subDirs;
    }

    public List<VirtualFile> getFiles() {
        return files;
    }

    public void addDirectory(VirtualDirectory dir) {
        subDirs.add(dir);
    }

    public void addFile(VirtualFile file) {
        files.add(file);
    }

    public VirtualDirectory getSubDirectoryByName(String name) {
        for (VirtualDirectory dir : subDirs) {
            if (dir.getName().equals(name)) {
                return dir;
            }
        }
        return null;
    }

    public VirtualFile getFileByName(String name) {
        for (VirtualFile file : files) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null;
    }
}

class FileSystemManager {

    private VirtualDirectory root, current;

    public FileSystemManager() {
        root = new VirtualDirectory("", null);
        current = root;

        VirtualDirectory home = new VirtualDirectory("home", root);
        VirtualDirectory kali = new VirtualDirectory("kali", home);
        VirtualFile flag = new VirtualFile("flag.txt", "CTF{well_done_you_found_me}", "rw-");

        root.addDirectory(home);
        home.addDirectory(kali);
        kali.addFile(flag);
    }

    public String getCurrentPath() {
        List<String> path = new ArrayList<>();
        VirtualDirectory dir = current;
        while (dir != null && dir.getParent() != null) {
            path.add(dir.getName());
            dir = dir.getParent();
        }
        Collections.reverse(path);
        return "/" + String.join("/", path);
    }

    public String listContents() {
        StringBuilder sb = new StringBuilder();
        for (VirtualDirectory dir : current.getSubDirectories()) {
            sb.append("[DIR] ").append(dir.getName()).append("\n");
        }
        for (VirtualFile file : current.getFiles()) {
            sb.append("[FILE] ").append(file.getName()).append(" ").append(file.getPermissions()).append("\n");
        }
        return sb.toString();
    }

    public boolean changeDirectory(String path) {
        if (path.equals("/")) {
            current = root;
            return true;
        }

        String[] parts = path.split("/");
        VirtualDirectory temp = path.startsWith("/") ? root : current;
        for (String part : parts) {
            if (part.equals("") || part.equals(".")) {
                continue;
            }
            if (part.equals("..")) {
                if (temp.getParent() != null) {
                    temp = temp.getParent();
                }
            } else {
                VirtualDirectory next = temp.getSubDirectoryByName(part);
                if (next == null) {
                    return false;
                }
                temp = next;
            }
        }
        current = temp;
        return true;
    }

    public String readFile(String filename) {
        VirtualFile file = current.getFileByName(filename);
        return (file != null) ? file.getContent() : "File not found";
    }

    public String makeDirectory(String name) {
        if (current.getSubDirectoryByName(name) != null) {
            return "Directory already exists";
        }
        VirtualDirectory newDir = new VirtualDirectory(name, current);
        current.addDirectory(newDir);
        return "";
    }

    public String removeDirectory(String name) {
        VirtualDirectory dir = current.getSubDirectoryByName(name);
        if (dir == null) {
            return "Directory not found";
        }
        current.getSubDirectories().remove(dir);
        return "";
    }

    public String move(String source, String destination) {
        VirtualFile file = current.getFileByName(source);
        if (file != null) {
            current.getFiles().remove(file);
            file = new VirtualFile(destination, file.getContent(), file.getPermissions());
            current.addFile(file);
            return "";
        }
        VirtualDirectory dir = current.getSubDirectoryByName(source);
        if (dir != null) {
            current.getSubDirectories().remove(dir);
            dir = new VirtualDirectory(destination, current);
            current.addDirectory(dir);
            return "";
        }
        return "Source not found";
    }

    public String copy(String source, String destination) {
        VirtualFile file = current.getFileByName(source);
        if (file == null) {
            return "Source file not found";
        }
        VirtualFile copied = new VirtualFile(destination, file.getContent(), file.getPermissions());
        current.addFile(copied);
        return "";
    }

    public String remove(String name) {
        VirtualFile file = current.getFileByName(name);
        if (file != null) {
            current.getFiles().remove(file);
            return "";
        }
        VirtualDirectory dir = current.getSubDirectoryByName(name);
        if (dir != null) {
            current.getSubDirectories().remove(dir);
            return "";
        }
        return "File or directory not found";
    }

    public String touch(String name) {
        if (current.getFileByName(name) != null) {
            return "File already exists";
        }
        current.addFile(new VirtualFile(name, "", "rw-"));
        return "";
    }

    public String grep(String pattern, String filename) {
        VirtualFile file = current.getFileByName(filename);
        if (file == null) {
            return "File not found";
        }
        StringBuilder sb = new StringBuilder();
        for (String line : file.getContent().split("\n")) {
            if (line.contains(pattern)) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().isEmpty() ? "No match found" : sb.toString();
    }

    public String head(String filename) {
        VirtualFile file = current.getFileByName(filename);
        if (file == null) {
            return "File not found";
        }
        String[] lines = file.getContent().split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(10, lines.length); i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    public String tail(String filename) {
        VirtualFile file = current.getFileByName(filename);
        if (file == null) {
            return "File not found";
        }
        String[] lines = file.getContent().split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = Math.max(0, lines.length - 10); i < lines.length; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    public String diff(String file1Name, String file2Name) {
        VirtualFile file1 = current.getFileByName(file1Name);
        VirtualFile file2 = current.getFileByName(file2Name);

        if (file1 == null || file2 == null) {
            return "One or both files not found";
        }

        String[] lines1 = file1.getContent().split("\n");
        String[] lines2 = file2.getContent().split("\n");

        StringBuilder sb = new StringBuilder();
        int maxLines = Math.max(lines1.length, lines2.length);

        for (int i = 0; i < maxLines; i++) {
            String l1 = i < lines1.length ? lines1[i] : "";
            String l2 = i < lines2.length ? lines2[i] : "";

            if (!l1.equals(l2)) {
                sb.append("- ").append(l1).append("\n+ ").append(l2).append("\n");
            }
        }

        return sb.toString().isEmpty() ? "Files are identical" : sb.toString();
    }

    public String sort(String filename) {
        VirtualFile file = current.getFileByName(filename);
        if (file == null) {
            return "File not found";
        }
        List<String> lines = new ArrayList<>(List.of(file.getContent().split("\n")));
        Collections.sort(lines);
        return String.join("\n", lines);
    }

    public String chmod(String permissions, String filename) {
        VirtualFile file = current.getFileByName(filename);
        if (file == null) {
            return "File not found";
        }
        file = new VirtualFile(file.getName(), file.getContent(), permissions);
        current.getFiles().removeIf(f -> f.getName().equals(filename));
        current.addFile(file);
        return "";
    }

    public String chown(String owner, String filename) {
        // Placeholder if ownership logic is needed later
        VirtualFile file = current.getFileByName(filename);
        return (file != null) ? "Owner changed (simulated)" : "File not found";
    }

    public String calendar() {
        return java.time.YearMonth.now().atDay(1).getDayOfWeek().toString(); // Simplified
    }

    public String manual(String command) {
        return switch (command) {
            case "ls" ->
                "List directory contents";
            case "cd" ->
                "Change directory";
            case "cat" ->
                "Display file contents";
            case "grep" ->
                "Search for pattern in file";
            default ->
                "No manual entry for " + command;
        };
    }

    public String writeToFile(String filename, String content) {
        VirtualFile file = current.getFileByName(filename);
        if (file != null) {
            current.getFiles().remove(file);
        }
        current.addFile(new VirtualFile(filename, content, "rw-"));
        return "";
    }

    public String appendToFile(String filename, String content) {
        VirtualFile file = current.getFileByName(filename);
        if (file == null) {
            current.addFile(new VirtualFile(filename, content, "rw-"));
        } else {
            String updatedContent = file.getContent() + content;
            current.getFiles().remove(file);
            current.addFile(new VirtualFile(filename, updatedContent, file.getPermissions()));
        }
        return "";
    }

}
