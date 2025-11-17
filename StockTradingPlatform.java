import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * TASK 2: Stock Trading Platform
 * A comprehensive trading platform with market data display, buy/sell
 * operations,
 * portfolio tracking, and data persistence.
 */
public class StockTradingPlatform extends JFrame {
    private TradingEngine tradingEngine;
    private User currentUser;
    private JTabbedPane tabbedPane;

    // Market Panel Components
    private JTable marketTable;
    private DefaultTableModel marketTableModel;
    private JLabel portfolioValueLabel;
    private JLabel cashBalanceLabel;
    private JLabel totalPLLabel;

    // Portfolio Panel Components
    private JTable portfolioTable;
    private DefaultTableModel portfolioTableModel;

    // Transaction History Components
    private JTable transactionTable;
    private DefaultTableModel transactionTableModel;

    // Colors
    private static final Color PRIMARY_COLOR = new Color(26, 35, 126);
    private static final Color SUCCESS_COLOR = new Color(27, 94, 32);
    private static final Color DANGER_COLOR = new Color(198, 40, 40);
    private static final Color ACCENT_COLOR = new Color(13, 71, 161);
    private static final Color PROFIT_COLOR = new Color(46, 125, 50);
    private static final Color LOSS_COLOR = new Color(211, 47, 47);
    private static final Color LIGHT_BG = new Color(250, 250, 250);

    private DecimalFormat priceFormat = new DecimalFormat("$#,##0.00");
    private DecimalFormat percentFormat = new DecimalFormat("+0.00%;-0.00%");

    public StockTradingPlatform() {
        tradingEngine = new TradingEngine();
        currentUser = new User("Trader001", "John Doe", 100000.00); // Starting with $100,000
        tradingEngine.setCurrentUser(currentUser);

        initializeGUI();
        startMarketUpdates();
        refreshAllData();
    }

    private void initializeGUI() {
        setTitle("📈 Stock Trading Platform");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(LIGHT_BG);

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("📊 Market", createMarketPanel());
        tabbedPane.addTab("💼 Portfolio", createPortfolioPanel());
        tabbedPane.addTab("📜 Transactions", createTransactionPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title
        JLabel titleLabel = new JLabel("Stock Trading Platform");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // User info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        infoPanel.setOpaque(false);

        cashBalanceLabel = new JLabel();
        cashBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cashBalanceLabel.setForeground(Color.WHITE);

        portfolioValueLabel = new JLabel();
        portfolioValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        portfolioValueLabel.setForeground(Color.WHITE);

        totalPLLabel = new JLabel();
        totalPLLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        infoPanel.add(new JLabel("💰 Cash: ") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        });
        infoPanel.add(cashBalanceLabel);
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(new JLabel("📊 Portfolio: ") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        });
        infoPanel.add(portfolioValueLabel);
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(new JLabel("📈 P/L: ") {
            {
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        });
        infoPanel.add(totalPLLabel);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMarketPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(LIGHT_BG);

        // Market table
        String[] columns = { "Symbol", "Company", "Price", "Change", "Change %", "Volume", "Market Cap" };
        marketTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        marketTable = new JTable(marketTableModel);
        marketTable.setRowHeight(35);
        marketTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        marketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        marketTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        marketTable.getTableHeader().setBackground(PRIMARY_COLOR);
        marketTable.getTableHeader().setForeground(Color.WHITE);
        marketTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Custom renderer for colored change columns
        marketTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 3 || column == 4) { // Change columns
                    String val = value.toString();
                    if (val.startsWith("+")) {
                        c.setForeground(PROFIT_COLOR);
                    } else if (val.startsWith("-")) {
                        c.setForeground(LOSS_COLOR);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(marketTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(LIGHT_BG);

        JButton buyButton = createStyledButton("🛒 Buy Stock", SUCCESS_COLOR);
        buyButton.addActionListener(e -> showBuyDialog());

        JButton sellButton = createStyledButton("💵 Sell Stock", DANGER_COLOR);
        sellButton.addActionListener(e -> showSellDialog());

        JButton refreshButton = createStyledButton("🔄 Refresh", ACCENT_COLOR);
        refreshButton.addActionListener(e -> {
            tradingEngine.updateMarketPrices();
            refreshAllData();
        });

        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createPortfolioPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(LIGHT_BG);

        // Title
        JLabel titleLabel = new JLabel("My Portfolio Holdings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Portfolio table
        String[] columns = { "Symbol", "Company", "Shares", "Avg Cost", "Current Price", "Market Value", "Gain/Loss",
                "Gain/Loss %" };
        portfolioTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        portfolioTable = new JTable(portfolioTableModel);
        portfolioTable.setRowHeight(35);
        portfolioTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        portfolioTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        portfolioTable.getTableHeader().setBackground(PRIMARY_COLOR);
        portfolioTable.getTableHeader().setForeground(Color.WHITE);
        portfolioTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Custom renderer
        portfolioTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 6 || column == 7) { // Gain/Loss columns
                    String val = value.toString();
                    if (val.startsWith("+") || (val.startsWith("$") && !val.contains("-"))) {
                        c.setForeground(PROFIT_COLOR);
                    } else if (val.startsWith("-") || val.contains("-")) {
                        c.setForeground(LOSS_COLOR);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createTransactionPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(LIGHT_BG);

        // Title
        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Transaction table
        String[] columns = { "Date/Time", "Type", "Symbol", "Shares", "Price", "Total Amount", "Status" };
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(transactionTableModel);
        transactionTable.setRowHeight(35);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionTable.getTableHeader().setBackground(PRIMARY_COLOR);
        transactionTable.getTableHeader().setForeground(Color.WHITE);
        transactionTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Custom renderer
        transactionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 1) { // Type column
                    if ("BUY".equals(value)) {
                        c.setForeground(SUCCESS_COLOR);
                    } else if ("SELL".equals(value)) {
                        c.setForeground(DANGER_COLOR);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void showBuyDialog() {
        int selectedRow = marketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock to buy.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String symbol = marketTable.getValueAt(selectedRow, 0).toString();
        Stock stock = tradingEngine.getStock(symbol);

        JDialog dialog = new JDialog(this, "Buy Stock - " + symbol, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(symbol + " - " + stock.getCompanyName()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Current Price:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(priceFormat.format(stock.getCurrentPrice())), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Shares:"), gbc);
        gbc.gridx = 1;
        JSpinner sharesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        panel.add(sharesSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Total Cost:"), gbc);
        gbc.gridx = 1;
        JLabel totalLabel = new JLabel(priceFormat.format(stock.getCurrentPrice()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(totalLabel, gbc);

        // Update total when shares change
        sharesSpinner.addChangeListener(e -> {
            int shares = (int) sharesSpinner.getValue();
            double total = shares * stock.getCurrentPrice();
            totalLabel.setText(priceFormat.format(total));
        });

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton confirmButton = createStyledButton("Confirm Buy", SUCCESS_COLOR);
        confirmButton.addActionListener(e -> {
            int shares = (int) sharesSpinner.getValue();
            if (tradingEngine.buyStock(symbol, shares)) {
                JOptionPane.showMessageDialog(dialog,
                        "Successfully purchased " + shares + " shares of " + symbol,
                        "Purchase Successful", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Insufficient funds to complete this purchase.",
                        "Purchase Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = createStyledButton("Cancel", DANGER_COLOR);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showSellDialog() {
        int selectedRow = portfolioTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock from your portfolio to sell.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String symbol = portfolioTable.getValueAt(selectedRow, 0).toString();
        int availableShares = Integer.parseInt(portfolioTable.getValueAt(selectedRow, 2).toString());
        Stock stock = tradingEngine.getStock(symbol);

        JDialog dialog = new JDialog(this, "Sell Stock - " + symbol, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(symbol + " - " + stock.getCompanyName()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Current Price:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(priceFormat.format(stock.getCurrentPrice())), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Available Shares:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(availableShares)), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Shares to Sell:"), gbc);
        gbc.gridx = 1;
        JSpinner sharesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, availableShares, 1));
        panel.add(sharesSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Total Value:"), gbc);
        gbc.gridx = 1;
        JLabel totalLabel = new JLabel(priceFormat.format(stock.getCurrentPrice()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(totalLabel, gbc);

        sharesSpinner.addChangeListener(e -> {
            int shares = (int) sharesSpinner.getValue();
            double total = shares * stock.getCurrentPrice();
            totalLabel.setText(priceFormat.format(total));
        });

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton confirmButton = createStyledButton("Confirm Sell", SUCCESS_COLOR);
        confirmButton.addActionListener(e -> {
            int shares = (int) sharesSpinner.getValue();
            if (tradingEngine.sellStock(symbol, shares)) {
                JOptionPane.showMessageDialog(dialog,
                        "Successfully sold " + shares + " shares of " + symbol,
                        "Sale Successful", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to sell stocks. Please try again.",
                        "Sale Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = createStyledButton("Cancel", DANGER_COLOR);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void refreshAllData() {
        // Update market table
        marketTableModel.setRowCount(0);
        for (Stock stock : tradingEngine.getAllStocks()) {
            marketTableModel.addRow(new Object[] {
                    stock.getSymbol(),
                    stock.getCompanyName(),
                    priceFormat.format(stock.getCurrentPrice()),
                    priceFormat.format(stock.getPriceChange()),
                    percentFormat.format(stock.getChangePercent()),
                    formatVolume(stock.getVolume()),
                    formatMarketCap(stock.getMarketCap())
            });
        }

        // Update portfolio table
        portfolioTableModel.setRowCount(0);
        for (PortfolioHolding holding : currentUser.getPortfolio().getHoldings()) {
            Stock stock = tradingEngine.getStock(holding.getSymbol());
            double marketValue = holding.getShares() * stock.getCurrentPrice();
            double gainLoss = marketValue - (holding.getShares() * holding.getAverageCost());
            double gainLossPercent = ((stock.getCurrentPrice() - holding.getAverageCost()) / holding.getAverageCost());

            portfolioTableModel.addRow(new Object[] {
                    stock.getSymbol(),
                    stock.getCompanyName(),
                    holding.getShares(),
                    priceFormat.format(holding.getAverageCost()),
                    priceFormat.format(stock.getCurrentPrice()),
                    priceFormat.format(marketValue),
                    priceFormat.format(gainLoss),
                    percentFormat.format(gainLossPercent)
            });
        }

        // Update transaction table
        transactionTableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        for (Transaction tx : currentUser.getTransactionHistory()) {
            transactionTableModel.addRow(new Object[] {
                    sdf.format(tx.getTimestamp()),
                    tx.getType(),
                    tx.getSymbol(),
                    tx.getShares(),
                    priceFormat.format(tx.getPrice()),
                    priceFormat.format(tx.getTotalAmount()),
                    "Completed"
            });
        }

        // Update header info
        cashBalanceLabel.setText(priceFormat.format(currentUser.getCashBalance()));
        double portfolioValue = currentUser.getPortfolio().getTotalValue(tradingEngine);
        portfolioValueLabel.setText(priceFormat.format(portfolioValue));

        double totalPL = currentUser.getPortfolio().getTotalGainLoss(tradingEngine);
        totalPLLabel.setText(priceFormat.format(totalPL));
        totalPLLabel.setForeground(totalPL >= 0 ? PROFIT_COLOR : LOSS_COLOR);
    }

    private void startMarketUpdates() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tradingEngine.updateMarketPrices();
                SwingUtilities.invokeLater(() -> refreshAllData());
            }
        }, 5000, 5000); // Update every 5 seconds
    }

    private String formatVolume(long volume) {
        if (volume >= 1000000) {
            return String.format("%.2fM", volume / 1000000.0);
        } else if (volume >= 1000) {
            return String.format("%.2fK", volume / 1000.0);
        }
        return String.valueOf(volume);
    }

    private String formatMarketCap(double marketCap) {
        if (marketCap >= 1000000000) {
            return String.format("$%.2fB", marketCap / 1000000000.0);
        } else if (marketCap >= 1000000) {
            return String.format("$%.2fM", marketCap / 1000000.0);
        }
        return priceFormat.format(marketCap);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            StockTradingPlatform platform = new StockTradingPlatform();
            platform.setVisible(true);
        });
    }
}

/**
 * Stock Class - Represents a stock with market data
 */
class Stock implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String companyName;
    private double currentPrice;
    private double openPrice;
    private double previousClose;
    private long volume;
    private double marketCap;
    private Random random;

    public Stock(String symbol, String companyName, double initialPrice, double marketCap) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = initialPrice;
        this.openPrice = initialPrice;
        this.previousClose = initialPrice;
        this.marketCap = marketCap;
        this.volume = (long) (Math.random() * 10000000) + 1000000;
        this.random = new Random();
    }

    public void updatePrice() {
        // Simulate price movement (±2%)
        double change = (random.nextDouble() - 0.5) * 0.04 * currentPrice;
        currentPrice += change;
        currentPrice = Math.max(1.0, currentPrice); // Minimum price $1
        volume += (long) (random.nextInt(100000));
    }

    public double getPriceChange() {
        return currentPrice - previousClose;
    }

    public double getChangePercent() {
        return (currentPrice - previousClose) / previousClose;
    }

    // Getters
    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getPreviousClose() {
        return previousClose;
    }

    public long getVolume() {
        return volume;
    }

    public double getMarketCap() {
        return marketCap;
    }
}

/**
 * Transaction Class - Represents a buy/sell transaction
 */
class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String symbol;
    private String type; // BUY or SELL
    private int shares;
    private double price;
    private double totalAmount;
    private Date timestamp;

    public Transaction(String symbol, String type, int shares, double price) {
        this.transactionId = "TX" + System.currentTimeMillis();
        this.symbol = symbol;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.totalAmount = shares * price;
        this.timestamp = new Date();
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getType() {
        return type;
    }

    public int getShares() {
        return shares;
    }

    public double getPrice() {
        return price;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

/**
 * PortfolioHolding - Represents a stock holding in portfolio
 */
class PortfolioHolding implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private int shares;
    private double averageCost;

    public PortfolioHolding(String symbol, int shares, double cost) {
        this.symbol = symbol;
        this.shares = shares;
        this.averageCost = cost;
    }

    public void addShares(int newShares, double price) {
        double totalCost = (shares * averageCost) + (newShares * price);
        shares += newShares;
        averageCost = totalCost / shares;
    }

    public void removeShares(int sharesToSell) {
        shares -= sharesToSell;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public int getShares() {
        return shares;
    }

    public double getAverageCost() {
        return averageCost;
    }
}

/**
 * Portfolio Class - Manages user's stock holdings
 */
class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, PortfolioHolding> holdings;

    public Portfolio() {
        holdings = new HashMap<>();
    }

    public void addHolding(String symbol, int shares, double price) {
        if (holdings.containsKey(symbol)) {
            holdings.get(symbol).addShares(shares, price);
        } else {
            holdings.put(symbol, new PortfolioHolding(symbol, shares, price));
        }
    }

    public boolean removeHolding(String symbol, int shares) {
        if (!holdings.containsKey(symbol))
            return false;

        PortfolioHolding holding = holdings.get(symbol);
        if (holding.getShares() < shares)
            return false;

        holding.removeShares(shares);
        if (holding.getShares() == 0) {
            holdings.remove(symbol);
        }
        return true;
    }

    public int getShares(String symbol) {
        return holdings.containsKey(symbol) ? holdings.get(symbol).getShares() : 0;
    }

    public double getTotalValue(TradingEngine engine) {
        double total = 0;
        for (PortfolioHolding holding : holdings.values()) {
            Stock stock = engine.getStock(holding.getSymbol());
            total += holding.getShares() * stock.getCurrentPrice();
        }
        return total;
    }

    public double getTotalGainLoss(TradingEngine engine) {
        double totalGainLoss = 0;
        for (PortfolioHolding holding : holdings.values()) {
            Stock stock = engine.getStock(holding.getSymbol());
            double marketValue = holding.getShares() * stock.getCurrentPrice();
            double costBasis = holding.getShares() * holding.getAverageCost();
            totalGainLoss += (marketValue - costBasis);
        }
        return totalGainLoss;
    }

    public List<PortfolioHolding> getHoldings() {
        return new ArrayList<>(holdings.values());
    }
}

/**
 * User Class - Represents a trading platform user
 */
class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String name;
    private double cashBalance;
    private Portfolio portfolio;
    private List<Transaction> transactionHistory;

    public User(String userId, String name, double initialBalance) {
        this.userId = userId;
        this.name = name;
        this.cashBalance = initialBalance;
        this.portfolio = new Portfolio();
        this.transactionHistory = new ArrayList<>();
    }

    public boolean buyStock(String symbol, int shares, double price) {
        double totalCost = shares * price;
        if (cashBalance < totalCost)
            return false;

        cashBalance -= totalCost;
        portfolio.addHolding(symbol, shares, price);
        transactionHistory.add(new Transaction(symbol, "BUY", shares, price));
        return true;
    }

    public boolean sellStock(String symbol, int shares, double price) {
        if (portfolio.getShares(symbol) < shares)
            return false;

        double totalValue = shares * price;
        cashBalance += totalValue;
        portfolio.removeHolding(symbol, shares);
        transactionHistory.add(new Transaction(symbol, "SELL", shares, price));
        return true;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }
}

/**
 * TradingEngine - Main engine managing stocks and transactions
 */
class TradingEngine {
    private Map<String, Stock> stocks;
    private User currentUser;
    private static final String DATA_FILE = "trading_data.dat";

    public TradingEngine() {
        stocks = new HashMap<>();
        initializeStocks();
        loadData();
    }

    private void initializeStocks() {
        // Major tech stocks
        stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 178.50, 2800000000000.0));
        stocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 140.25, 1750000000000.0));
        stocks.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 380.75, 2850000000000.0));
        stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 145.80, 1500000000000.0));
        stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 242.50, 770000000000.0));
        stocks.put("META", new Stock("META", "Meta Platforms", 325.60, 850000000000.0));
        stocks.put("NVDA", new Stock("NVDA", "NVIDIA Corp.", 485.20, 1200000000000.0));
        stocks.put("NFLX", new Stock("NFLX", "Netflix Inc.", 440.90, 195000000000.0));

        // Other popular stocks
        stocks.put("DIS", new Stock("DIS", "Walt Disney Co.", 95.40, 175000000000.0));
        stocks.put("BA", new Stock("BA", "Boeing Co.", 210.30, 130000000000.0));
        stocks.put("INTC", new Stock("INTC", "Intel Corp.", 45.20, 185000000000.0));
        stocks.put("AMD", new Stock("AMD", "AMD Inc.", 120.75, 195000000000.0));
    }

    public void updateMarketPrices() {
        for (Stock stock : stocks.values()) {
            stock.updatePrice();
        }
        saveData();
    }

    public boolean buyStock(String symbol, int shares) {
        Stock stock = stocks.get(symbol);
        if (stock == null || currentUser == null)
            return false;
        return currentUser.buyStock(symbol, shares, stock.getCurrentPrice());
    }

    public boolean sellStock(String symbol, int shares) {
        Stock stock = stocks.get(symbol);
        if (stock == null || currentUser == null)
            return false;
        return currentUser.sellStock(symbol, shares, stock.getCurrentPrice());
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    public List<Stock> getAllStocks() {
        return new ArrayList<>(stocks.values());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    // Data persistence
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(stocks);
            oos.writeObject(currentUser);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            stocks = (Map<String, Stock>) ois.readObject();
            currentUser = (User) ois.readObject();
        } catch (FileNotFoundException e) {
            // File doesn't exist yet
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}
