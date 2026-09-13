import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {
    private final JTextField display = new JTextField("0");
    private final JLabel expressionLabel = new JLabel(" ", SwingConstants.RIGHT);
    private final DecimalFormat numberFormat = new DecimalFormat("0.###############",
            DecimalFormatSymbols.getInstance(Locale.US));
    private final List<String> history = new ArrayList<>();

    private double storedValue;
    private double memory;
    private String pendingOperation;
    private boolean newNumber = true;
    private boolean hasMemory;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowWindow());
    }

    private void createAndShowWindow() {
        JFrame window = new JFrame("Calculadora");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(390, 650);
        window.setMinimumSize(window.getSize());
        window.setLocationRelativeTo(null);

        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setFont(new Font("SansSerif", Font.PLAIN, 32));
        display.setEditable(false);
        display.setFocusable(false);
        expressionLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));

        JPanel screen = new JPanel(new BorderLayout());
        screen.add(expressionLabel, BorderLayout.NORTH);
        screen.add(display, BorderLayout.CENTER);
        screen.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        JPanel buttons = new JPanel(new BorderLayout(3, 3));
        addButtons(buttons);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

        JPanel topActions = new JPanel(new BorderLayout());
        JButton equalsButton = createButton("=");
        equalsButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        equalsButton.setPreferredSize(new java.awt.Dimension(0, 24));
        equalsButton.setBackground(new java.awt.Color(210, 225, 245));
        topActions.add(equalsButton, BorderLayout.CENTER);

        JPanel historyPanel = new JPanel(new GridLayout(1, 2, 3, 3));
        JButton historyButton = createButton("Hist\u00f3rico");
        historyButton.addActionListener(event -> showHistory());
        JButton clearHistoryButton = createButton("Apagar hist\u00f3rico");
        clearHistoryButton.addActionListener(event -> history.clear());
        historyPanel.add(historyButton);
        historyPanel.add(clearHistoryButton);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2), "Hist\u00f3rico de contas"));

        JPanel calculatorArea = new JPanel(new BorderLayout(3, 3));
        calculatorArea.add(topActions, BorderLayout.NORTH);
        calculatorArea.add(buttons, BorderLayout.CENTER);
        calculatorArea.add(historyPanel, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(3, 3));
        content.add(screen, BorderLayout.NORTH);
        content.add(calculatorArea, BorderLayout.CENTER);
        content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        window.setContentPane(content);
        addKeyboardSupport(window);
        window.setVisible(true);
    }

    private void addButtons(JPanel panel) {
        JPanel topPanel = createButtonGrid(new String[]{
            "MC", "MR", "M+", "M-",
            "MS", "CE", "C", "\u232b",
            "1/x", "x\u00b2", "\u221a", "\u00f7"
        }, 3);

        JPanel numericPanel = createButtonGrid(new String[]{
            "7", "8", "9", "\u00d7",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "+/-", "0", ",", "00"
        }, 4);
        numericPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JPanel scientificPanel = createButtonGrid(new String[]{
            "%", "^", "sin", "cos",
            "tan", "log", "ln", "n!",
            "x\u00b3", "\u221b", "abs", "\u03c0",
            "e", "10\u02e3", "mod", "rnd"
        }, 4);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(numericPanel, BorderLayout.CENTER);
        panel.add(scientificPanel, BorderLayout.SOUTH);
    }

    private JPanel createButtonGrid(String[] labels, int rows) {
        JPanel panel = new JPanel(new GridLayout(rows, 4, 3, 3));
        for (String label : labels) {
            panel.add(createButton(label));
        }
        return panel;
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setEnabled(!label.isEmpty());
        if (!label.isEmpty() && !label.equals("=")) {
            button.addActionListener(event -> handleButton(label));
        } else if (label.equals("=")) {
            button.addActionListener(event -> calculateResult());
        }
        return button;
    }

    private void handleButton(String label) {
        switch (label) {
            case "MC" -> {
                memory = 0;
                hasMemory = false;
            }
            case "MR" -> {
                if (hasMemory) {
                    setDisplay(memory);
                    newNumber = true;
                }
            }
            case "M+" -> {
                memory += currentValue();
                hasMemory = true;
            }
            case "M-" -> {
                memory -= currentValue();
                hasMemory = true;
            }
            case "MS" -> {
                memory = currentValue();
                hasMemory = true;
            }
            case "CE" -> {
                display.setText("0");
                newNumber = true;
            }
            case "C" -> clear();
            case "\u232b" -> backspace();
            case "1/x" -> unaryOperation("1/x");
            case "x\u00b2" -> unaryOperation("x\u00b2");
            case "\u221a" -> unaryOperation("\u221a");
            case "sin", "cos", "tan", "log", "ln", "n!", "x\u00b3", "\u221b", "abs", "10\u02e3"
                    -> unaryOperation(label);
            case "\u03c0" -> {
                setDisplay(Math.PI);
                newNumber = true;
            }
            case "e" -> {
                setDisplay(Math.E);
                newNumber = true;
            }
            case "mod" -> chooseOperation("mod");
            case "rnd" -> {
                setDisplay(Math.round(currentValue()));
                newNumber = true;
            }
            case "00" -> addDoubleZero();
            case "+/-" -> toggleSign();
            case "," -> addDecimalSeparator();
            case "+", "-", "\u00d7", "\u00f7", "^", "%" -> chooseOperation(label);
            case "=" -> calculateResult();
            default -> addDigit(label);
        }
    }

    private void addDigit(String digit) {
        if (newNumber || display.getText().equals("0")) {
            display.setText(digit);
            newNumber = false;
        } else {
            display.setText(display.getText() + digit);
        }
    }

    private void addDoubleZero() {
        if (newNumber) {
            display.setText("0");
            newNumber = false;
        } else if (!display.getText().equals("0")) {
            display.setText(display.getText() + "00");
        }
    }

    private void addDecimalSeparator() {
        if (newNumber) {
            display.setText("0,");
            newNumber = false;
        } else if (!display.getText().contains(",")) {
            display.setText(display.getText() + ",");
        }
    }

    private void chooseOperation(String operation) {
        if (pendingOperation != null && !newNumber) {
            calculateResult();
        }
        storedValue = currentValue();
        pendingOperation = operation;
        expressionLabel.setText(format(storedValue) + " " + operation);
        newNumber = true;
    }

    private void calculateResult() {
        if (pendingOperation == null) {
            return;
        }

        double secondValue = currentValue();
        double result;
        try {
            result = applyOperation(storedValue, secondValue, pendingOperation);
        } catch (ArithmeticException exception) {
            showError(exception.getMessage());
            clear();
            return;
        }

        setDisplay(result);
        String calculation = format(storedValue) + " " + pendingOperation + " " + format(secondValue)
                + " = " + format(result);
        expressionLabel.setText(calculation);
        history.add(0, calculation);
        storedValue = result;
        pendingOperation = null;
        newNumber = true;
    }

    private double applyOperation(double first, double second, String operation) {
        return switch (operation) {
            case "+" -> first + second;
            case "-" -> first - second;
            case "\u00d7" -> first * second;
            case "\u00f7" -> {
                if (second == 0) {
                    throw new ArithmeticException("N\u00e3o \u00e9 poss\u00edvel dividir por zero.");
                }
                yield first / second;
            }
            case "^" -> Math.pow(first, second);
            case "%" -> first * second / 100;
            case "mod" -> {
                if (second == 0) {
                    throw new ArithmeticException("N\u00e3o \u00e9 poss\u00edvel calcular o resto com zero.");
                }
                yield first % second;
            }
            default -> throw new IllegalArgumentException("Opera\u00e7\u00e3o inv\u00e1lida.");
        };
    }

    private void unaryOperation(String operation) {
        double value = currentValue();
        double result;
        switch (operation) {
            case "1/x" -> {
                if (value == 0) {
                    showError("N\u00e3o \u00e9 poss\u00edvel dividir por zero.");
                    return;
                }
                result = 1 / value;
            }
            case "x\u00b2" -> result = value * value;
            case "\u221a" -> {
                if (value < 0) {
                    showError("N\u00e3o \u00e9 poss\u00edvel calcular a raiz de um n\u00famero negativo.");
                    return;
                }
                result = Math.sqrt(value);
            }
            case "sin" -> result = Math.sin(Math.toRadians(value));
            case "cos" -> result = Math.cos(Math.toRadians(value));
            case "tan" -> result = Math.tan(Math.toRadians(value));
            case "log" -> {
                if (value <= 0) {
                    showError("O logaritmo exige um n\u00famero maior que zero.");
                    return;
                }
                result = Math.log10(value);
            }
            case "ln" -> {
                if (value <= 0) {
                    showError("O logaritmo exige um n\u00famero maior que zero.");
                    return;
                }
                result = Math.log(value);
            }
            case "n!" -> {
                if (value < 0 || value != Math.floor(value) || value > 170) {
                    showError("O fatorial exige um inteiro entre 0 e 170.");
                    return;
                }
                result = factorial((int) value);
            }
            case "x\u00b3" -> result = value * value * value;
            case "\u221b" -> result = Math.cbrt(value);
            case "abs" -> result = Math.abs(value);
            case "10\u02e3" -> result = Math.pow(10, value);
            default -> throw new IllegalArgumentException("Opera\u00e7\u00e3o inv\u00e1lida.");
        }
        setDisplay(result);
        history.add(0, operation + "(" + format(value) + ") = " + format(result));
        newNumber = true;
    }

    private void showHistory() {
        if (history.isEmpty()) {
            showError("Ainda n\u00e3o h\u00e1 contas armazenadas.");
            return;
        }

        JTextArea historyText = new JTextArea(String.join("\n", history));
        historyText.setEditable(false);
        historyText.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyText);
        scrollPane.setPreferredSize(new java.awt.Dimension(420, 300));
        javax.swing.JOptionPane.showMessageDialog(null, scrollPane, "Hist\u00f3rico",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private double factorial(int value) {
        double result = 1;
        for (int i = 2; i <= value; i++) {
            result *= i;
        }
        return result;
    }

    private void toggleSign() {
        if (!display.getText().equals("0")) {
            display.setText(display.getText().startsWith("-")
                    ? display.getText().substring(1)
                    : "-" + display.getText());
        }
    }

    private void backspace() {
        if (!newNumber && display.getText().length() > 1) {
            display.setText(display.getText().substring(0, display.getText().length() - 1));
        } else {
            display.setText("0");
            newNumber = true;
        }
    }

    private void clear() {
        display.setText("0");
        expressionLabel.setText(" ");
        storedValue = 0;
        pendingOperation = null;
        newNumber = true;
    }

    private double currentValue() {
        return Double.parseDouble(display.getText().replace(',', '.'));
    }

    private void setDisplay(double value) {
        display.setText(format(value).replace('.', ','));
    }

    private String format(double value) {
        return numberFormat.format(value);
    }

    private void addKeyboardSupport(JFrame window) {
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                char key = event.getKeyChar();
                if (Character.isDigit(key)) {
                    addDigit(String.valueOf(key));
                } else if (key == '.' || key == ',') {
                    addDecimalSeparator();
                } else if ("+-*/%^".indexOf(key) >= 0) {
                    handleButton(key == '*' ? "\u00d7" : key == '/' ? "\u00f7" : String.valueOf(key));
                } else if (key == '=') {
                    calculateResult();
                }
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    backspace();
                } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    clear();
                }
            }
        });
    }

    private void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Erro",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
}
