import javax.imageio.ImageIO;
import javax.print.DocFlavor.URL;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SavApp extends JFrame {
    private JTable table;
    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JTextField numTickField;
    private JTextField dateField;
    private JComboBox<String> etatTicketComboBox;
    private JComboBox<String> declarationComboBox;
    private JComboBox<Integer> idUserComboBox;
    private JComboBox<Integer> idProduitsComboBox;
    private JComboBox<String> nomUserComboBox;
    private JComboBox<String> prenomUserComboBox;
    private JComboBox<String> nomProduitsComboBox;
    private JComboBox<Boolean> requeteTraiteeComboBox;
    private JTextField searchField;
    private JButton searchButton;

    public SavApp() {
    	
        setTitle("Gestion des SAV");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
       
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Barre de recherche:");
        searchField = new JTextField(15);
        searchButton = new JButton("Rechercher");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        String[] columns = {"ID_User", "ID_Produits", "Num_ticket", "État_tic", "Déclaration", "Date", "Nom_User", "Prenom_User", "Nom_Produit", "RequeteTraitee"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Ajouter");
        deleteButton = new JButton("Supprimer");
        updateButton = new JButton("Modifier");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel inputPanel = new JPanel(new GridLayout(10, 2));
        mainPanel.add(inputPanel, BorderLayout.WEST);

        idUserComboBox = new JComboBox<>();
        idProduitsComboBox = new JComboBox<>();
        nomUserComboBox = new JComboBox<>();
        prenomUserComboBox = new JComboBox<>();
        nomProduitsComboBox = new JComboBox<>();
        requeteTraiteeComboBox = new JComboBox<>(new Boolean[]{true, false});
        numTickField = new JTextField();
        dateField = new JTextField();
        etatTicketComboBox = new JComboBox<>(new String[]{"Nouveau", "En cours", "Fermer"});
        declarationComboBox = new JComboBox<>(new String[]{"Retour", "Maintenance", "Réparation"});
        inputPanel.add(new JLabel("ID_User:"));
        inputPanel.add(idUserComboBox);
        inputPanel.add(new JLabel("ID_Produits:"));
        inputPanel.add(idProduitsComboBox);
        inputPanel.add(new JLabel("Nom_User:"));
        inputPanel.add(nomUserComboBox);
        inputPanel.add(new JLabel("Prenom_User:"));
        inputPanel.add(prenomUserComboBox);
        inputPanel.add(new JLabel("Nom_Produit:"));
        inputPanel.add(nomProduitsComboBox);
        inputPanel.add(new JLabel("Numéro de ticket:"));
        inputPanel.add(numTickField);
        inputPanel.add(new JLabel("Date:"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("État du ticket:"));
        inputPanel.add(etatTicketComboBox);
        inputPanel.add(new JLabel("Déclaration:"));
        inputPanel.add(declarationComboBox);
        inputPanel.add(new JLabel("Requête traitée:"));
        inputPanel.add(requeteTraiteeComboBox);

        Dimension fieldSize = new Dimension(150, 25);
        numTickField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);

        loadDataFromDatabase(model);
        loadIdUserData(idUserComboBox);
        loadProduitsData(idProduitsComboBox);
        loadNomUserData(nomUserComboBox);
        loadPrenomUserData(prenomUserComboBox);
        loadNomProduitsData(nomProduitsComboBox);
        

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Vérifier si tous les champs obligatoires sont remplis
                if (idUserComboBox.getSelectedItem() == null || idProduitsComboBox.getSelectedItem() == null ||
                        numTickField.getText().isEmpty() || dateField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(SavApp.this, "Veuillez remplir tous les champs obligatoires.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return; // Arrêter l'exécution de la méthode si des champs obligatoires ne sont pas remplis
                }

                // Vérifier si le format de la date est correct
                String dateFormat = "yyyy-MM-dd"; // Format de date attendu
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                sdf.setLenient(false); // Désactiver la tolérance pour les dates invalides
                try {
                    sdf.parse(dateField.getText()); // Essayer de parser la date
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(SavApp.this, "Format de date invalide. Veuillez saisir une date au format YYYY-MM-DD.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return; // Arrêter l'exécution de la méthode si la date est invalide
                }

                // Si toutes les validations passent, ajouter le nouvel enregistrement
                try {
                    int selectedUserId = (int) idUserComboBox.getSelectedItem();
                    int selectedProduitsId = (int) idProduitsComboBox.getSelectedItem();
                    String selectedNomUser = (String) nomUserComboBox.getSelectedItem();
                    String selectedPrenomUser = (String) prenomUserComboBox.getSelectedItem();
                    String selectedNomProduits = (String) nomProduitsComboBox.getSelectedItem();
                    Boolean selectedRequeteTraitee = (Boolean) requeteTraiteeComboBox.getSelectedItem();
                    String numTickFieldText = numTickField.getText();
                    String dateFieldText = dateField.getText();
                    String etatTicketFieldText = (String) etatTicketComboBox.getSelectedItem();
                    String declarationFieldText = (String) declarationComboBox.getSelectedItem();

                    // Code pour se connecter à la base de données et ajouter l'enregistrement
                    String url = "jdbc:mysql://localhost:8889/iPomme";
                    String username = "new";
                    String password = "new";
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection conn = DriverManager.getConnection(url, username, password);
                    String sql = "INSERT INTO SAV_App (ID_User, ID_Produits, Num_ticket, Date, Etat_tic, Declaration, Nom_User, Prenom_User, Nom_Produit, RequeteTraitee) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, selectedUserId);
                    stmt.setInt(2, selectedProduitsId);
                    stmt.setString(3, numTickFieldText);
                    stmt.setString(4, dateFieldText);
                    stmt.setString(5, etatTicketFieldText);
                    stmt.setString(6, declarationFieldText);
                    stmt.setString(7, selectedNomUser);
                    stmt.setString(8, selectedPrenomUser);
                    stmt.setString(9, selectedNomProduits);
                    stmt.setBoolean(10, selectedRequeteTraitee);
                    stmt.executeUpdate();
                    conn.close();

                    // Ajouter la nouvelle ligne à la table
                    Object[] newRow = {selectedUserId, selectedProduitsId, numTickFieldText, etatTicketFieldText, declarationFieldText, dateFieldText, selectedNomUser, selectedPrenomUser, selectedNomProduits, selectedRequeteTraitee};
                    model.addRow(newRow);

                    JOptionPane.showMessageDialog(SavApp.this, "Enregistrement ajouté avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SavApp.this, "Erreur lors de l'ajout de l'enregistrement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int idUserToDelete = (int) table.getValueAt(selectedRow, 0);
                    int idProduitsToDelete = (int) table.getValueAt(selectedRow, 1); // Ajoutez cette ligne pour obtenir l'ID des produits

                    try {
                        String url = "jdbc:mysql://localhost:8889/iPomme";
                        String username = "new";
                        String password = "new";
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        Connection conn = DriverManager.getConnection(url, username, password);
                        String sql = "DELETE FROM SAV_App WHERE ID_User = ? AND ID_Produits = ?"; // Ajoutez ID_Produits à la clause WHERE
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, idUserToDelete);
                        stmt.setInt(2, idProduitsToDelete); // Paramètre supplémentaire pour ID_Produits
                        stmt.executeUpdate();
                        conn.close();

                        model.removeRow(selectedRow);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            }
        });


        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String url = "jdbc:mysql://localhost:8889/iPomme";
                    String username = "new";
                    String password = "new";
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection conn = DriverManager.getConnection(url, username, password);
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    int rowCount = model.getRowCount();
                    for (int i = 0; i < rowCount; i++) {
                        String numTicket = String.valueOf(model.getValueAt(i, 2)); // Numéro de ticket
                        String date = (String) model.getValueAt(i, 5); // Date
                        String etatTicket = (String) model.getValueAt(i, 3); // Etat_tic
                        String declaration = (String) model.getValueAt(i, 4); // Declaration

                        // Requête pour mettre à jour les champs Date, Etat_tic et Declaration pour un Num_ticket donné
                        String updateQuery = "UPDATE SAV_App SET Date = ?, Etat_tic = ?, Declaration = ? WHERE Num_ticket = ?";
                        PreparedStatement st = conn.prepareStatement(updateQuery);
                        st.setString(1, date);
                        st.setString(2, etatTicket);
                        st.setString(3, declaration);
                        st.setString(4, numTicket);
                        st.executeUpdate();
                    }
                    conn.close();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        });


        // Appliquer le rendu personnalisé à la colonne "RequeteTraitee"
        table.getColumnModel().getColumn(9).setCellRenderer(new RequeteTraiteeCellRenderer());

        // Ajout de l'écouteur pour le bouton de recherche
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText(); // Retirez toLowerCase() ici

                boolean found = false;

                // Vérifier si le champ spécifié existe dans une des colonnes
                for (int i = 0; i < table.getColumnCount(); i++) {
                    for (int j = 0; j < table.getRowCount(); j++) {
                        Object value = table.getValueAt(j, i);
                        // Utilisez equalsIgnoreCase() pour ignorer la casse lors de la comparaison
                        if (value != null && value.toString().toLowerCase().contains(searchText.toLowerCase())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }

                // Si le champ spécifié existe, filtrer les résultats
                if (found) {
                    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
                    table.setRowSorter(sorter);
                    if (searchText.length() == 0) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter(searchText, 2, 6, 7)); // Filter by ticket number, nom_user, and prenom_user
                    }
                } else {
                    // Si le champ spécifié n'existe pas, ne rien filtrer
                    JOptionPane.showMessageDialog(SavApp.this, "Aucune correspondance trouvée pour \"" + searchText + "\"", "Aucune correspondance", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });


        JButton resetButton = new JButton("Réinitialiser"); // Bouton de réinitialisation
        searchPanel.add(resetButton); // Ajout du bouton de réinitialisation

        // Ajout de l'écouteur pour le bouton de réinitialisation
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Réinitialisation du filtre
                TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) table.getRowSorter();
                sorter.setRowFilter(null);
                // Effacer le texte de la barre de recherche
                searchField.setText("");
            }
        });

        setVisible(true);
    }

    private void loadDataFromDatabase(DefaultTableModel model) {
        try {
            String url = "jdbc:mysql://localhost:8889/iPomme";
            String username = "new";
            String password = "new";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM SAV_App");
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("ID_User"),
                        rs.getInt("ID_Produits"),
                        rs.getInt("Num_ticket"), // Utilisation de getInt pour Num_ticket
                        rs.getString("Etat_tic"),
                        rs.getString("Declaration"),
                        rs.getString("Date"),
                        rs.getString("Nom_User"),
                        rs.getString("Prenom_User"),
                        rs.getString("Nom_Produit"),
                        rs.getBoolean("RequeteTraitee")
                };
                model.addRow(row);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
  




    private void loadIdUserData(JComboBox<Integer> comboBox) {
        try {
            String url = "jdbc:mysql://localhost:8889/iPomme";
            String username = "new";
            String password = "new";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID_User FROM User");
            while (rs.next()) {
                int idUser = rs.getInt("ID_User");
                comboBox.addItem(idUser);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void loadProduitsData(JComboBox<Integer> comboBox) {
        try {
            String url = "jdbc:mysql://localhost:8889/iPomme";
            String username = "new";
            String password = "new";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID_Produits FROM PRODUITS");
            while (rs.next()) {
                int idProduits = rs.getInt("ID_Produits");
                comboBox.addItem(idProduits);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void loadNomUserData(JComboBox<String> comboBox) {
        try {
            String url = "jdbc:mysql://localhost:8889/iPomme";
            String username = "new";
            String password = "new";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT Nom FROM User");
            while (rs.next()) {
                String nomUser = rs.getString("Nom");
                comboBox.addItem(nomUser);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void loadPrenomUserData(JComboBox<String> comboBox) {
        try {
            String url = "jdbc:mysql://localhost:8889/iPomme";
            String username = "new";
            String password = "new";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT Prenom FROM User");
            while (rs.next()) {
                String prenomUser = rs.getString("Prenom");
                comboBox.addItem(prenomUser);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void loadNomProduitsData(JComboBox<String> comboBox) {
        try {
            String url = "jdbc:mysql://localhost:8889/iPomme";
            String username = "new";
            String password = "new";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT Nom FROM Produits");
            while (rs.next()) {
                String nomProduits = rs.getString("Nom");
                comboBox.addItem(nomProduits);
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SavApp();
            }
        });
    }

    // Renderer pour la colonne RequeteTraitee
    class RequeteTraiteeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Vérifier si la valeur est true
            if ((Boolean) value) {
                cellComponent.setBackground(Color.GREEN);
            } else {
                cellComponent.setBackground(Color.RED);
            }
            return cellComponent;
        }
    }
}
