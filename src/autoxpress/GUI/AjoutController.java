/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoxpress.GUI;

import autoxpress.entities.Reclamation;
import autoxpress.services.ReclamationCRUD;
import autoxpress.utils.MyConnection;
import autoxpress.services.PDFGenerator;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author rania
 */
public class AjoutController implements Initializable {

    @FXML
    private DatePicker tfdate;
    @FXML
    private ChoiceBox<String> tftype;
    @FXML
    private TextArea tfdescription;

    private ReclamationCRUD rcrd = new ReclamationCRUD();
    @FXML
    private TableView<Reclamation> tableReclamations;
    @FXML
    private TableColumn<?, ?> colId;
    @FXML
    private TableColumn<?, ?> colType;
    @FXML
    private TableColumn<?, ?> colDate;
    @FXML
    private TableColumn<?, ?> colDescription;
    public ObservableList<Reclamation> data = FXCollections.observableArrayList();
    @FXML
    private Button btnsupprimer;
    @FXML
    private Button btnenvoyer;
    @FXML
    private Button btnmodifier;
    @FXML
    private Button btnreclamation;
    @FXML
    private Button pdf;
    @FXML
    private Button btnAvis;
    @FXML
    private TextField emailField;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        tftype.getItems().addAll("Service", "Technique", "Autre");
        tftype.getSelectionModel().selectFirst();
        // Initialiser la date par d??faut du DatePicker ?? aujourd'hui
        tfdate.setValue(LocalDate.now());
        show();
    }
    MyConnection cnx = null;
    Statement st = null;
    ReclamationCRUD rcd = new ReclamationCRUD();

    @FXML
    private void ajouterreclamation(ActionEvent event) {

        // V??rifier que tous les champs sont remplis
        if (tftype.getValue() == null || tfdate.getValue() == null || tfdescription.getText().isEmpty()) {
            // Afficher un message d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Champs vides");
            alert.setHeaderText(null);
            alert.setContentText("Aucune de ces informations ne doit ??tre vide. Veuillez remplir tous les champs.");
            alert.showAndWait();
            return;

        }

        // V??rifier que la longueur de la description est sup??rieure ?? 5 caract??res
        if (tfdescription.getText().length() < 6) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("La description doit contenir au moins 6 caract??res");
            alert.showAndWait();
            return;
        }
        // V??rifier que la date est comprise entre il y a deux jours et aujourd'hui
        LocalDate selectedDate = tfdate.getValue();
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);
        if (selectedDate.isBefore(twoDaysAgo) || selectedDate.isAfter(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("La date doit ??tre comprise entre il y a deux jours et aujourd'hui");
            alert.showAndWait();
            return;
        }
        // R??cup??rer les valeurs des champs
        String type = tftype.getValue();
        String date_r = tfdate.getValue().toString();
        String description = tfdescription.getText();
        rcrd.ajouterReclamation(new Reclamation(type, date_r, description));
        // Rafra??chir la liste de donn??es
        data.clear();
        show();
        // Rafra??chir la vue de la table
        tableReclamations.refresh();
        // Effacer les champs de saisie
        /*tftype.setValue(null);
    tfdate.setValue(null);
    tfdescription.setText("");*/
    // Appelle la fonction sendMail avec le champ de texte en param??tre
    sendMail(emailField);
    }

    public void show() {
        try {
            String requete = "SELECT * FROM reclamation";
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Reclamation r = new Reclamation(rs.getInt("id"), rs.getString("type"), rs.getString("date_r"), rs.getString("description"));
                data.add(r);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableReclamations.setItems(data);
    }

    @FXML
    private void supprimerreclamation(ActionEvent event) {
        // V??rifier si une r??clamation est s??lectionn??e
        if (tableReclamations.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Veuillez s??lectionner une r??clamation ?? supprimer");
            alert.showAndWait();
            return;
        }

        // Afficher une bo??te de dialogue de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Voulez-vous vraiment supprimer cette r??clamation ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // R??cup??rer l'ID de la r??clamation s??lectionn??e dans la vue de la table
            int id = tableReclamations.getSelectionModel().getSelectedItem().getId();

            // Supprimer la r??clamation de la base de donn??es
            rcrd.supprimerReclamation(id);
// Rafra??chir la liste de donn??es
            data.clear();
            show();
            // Rafra??chir la vue de la table
            tableReclamations.refresh();
        }
    }

    /**
     * Modifie les informations de la r??clamation s??lectionn??e dans la table
     * view. R??cup??re les nouvelles valeurs des champs type, date et
     * description, puis ex??cute une requ??te de mise ?? jour dans la base de
     * donn??es. Affiche un message de confirmation si la mise ?? jour a r??ussi.
     * Rafra??chit la table view pour afficher les nouvelles donn??es.
     */
    @FXML
    private void afficherReclamationSelectionnee() {
        Reclamation reclamation = tableReclamations.getSelectionModel().getSelectedItem();
        if (reclamation != null) {
            tftype.setValue(reclamation.getType());
            tfdate.setValue(LocalDate.parse(reclamation.getDate()));
            tfdescription.setText(reclamation.getDescription());
        } else {
            tftype.setValue(null);
            tfdate.setValue(null);
            tfdescription.setText("");
        }

    }

    @FXML
    private void modifierreclamation(ActionEvent event) {
        Reclamation reclamation = tableReclamations.getSelectionModel().getSelectedItem();
        if (reclamation == null) {
            // Aucune r??clamation s??lectionn??e, afficher un message d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de modification");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez s??lectionner une r??clamation ?? modifier.");
            alert.showAndWait();
        } else {
            // V??rifier que tous les champs sont remplis
            if (tftype.getValue() == null || tfdate.getValue() == null || tfdescription.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de modification");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez remplir tous les champs avant de modifier une r??clamation.");
                alert.showAndWait();
                return;
            }

            // V??rifier que la longueur de la description est sup??rieure ?? 5 caract??res
            if (tfdescription.getText().length() < 6) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("La description doit contenir au moins 6 caract??res");
                alert.showAndWait();
                return;
            }

            // R??cup??rer les nouvelles valeurs
            String type = tftype.getValue();
            String date = tfdate.getValue().toString();
            String description = tfdescription.getText();

            // Mettre ?? jour la r??clamation dans la base de donn??es
            rcrd.modifierReclamation(new Reclamation(reclamation.getId(), type, date, description));

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Modification r??ussie");
            alert.setHeaderText(null);
            alert.setContentText("La r??clamation a ??t?? modifi??e avec succ??s.");
            alert.showAndWait();

            // Rafra??chir la table view pour afficher les nouvelles donn??es
            data.clear();
            show();
        }
    }

    @FXML
    private void gererReclamations(ActionEvent event) {
        try {
            Parent reclamationParent = FXMLLoader.load(getClass().getResource("AjoutRec.fxml"));
            Scene reclamationScene = new Scene(reclamationParent);
            Stage window = (Stage) (((Button) event.getSource()).getScene().getWindow());
            window.setScene(reclamationScene);
            window.show();
        } catch (IOException e) {
        }
    }

    @FXML
    private void gererAvis(ActionEvent event) {
        try {
            Parent avisParent = FXMLLoader.load(getClass().getResource("AjoutAvis.fxml"));
            Scene avisScene = new Scene(avisParent);
            Stage window = (Stage) (((Button) event.getSource()).getScene().getWindow());
            window.setScene(avisScene);
            window.show();
        } catch (IOException e) {
        }
    }

    @FXML
    void generatePDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        // D??finir l'extension de fichier par d??faut
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Afficher la bo??te de dialogue pour enregistrer le fichier
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            PDFGenerator pdfGenerator = new PDFGenerator();
            pdfGenerator.generatePDF(data, file.getAbsolutePath());
        }
    }

    public void afficherStatistiques(ActionEvent event) {
        try {
            String requete = "SELECT type, COUNT(*) as nb_reclamations "
                    + "FROM reclamation "
                    + "GROUP BY type";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            int totalReclamations = 0;
            while (rs.next()) {
                String type = rs.getString("type");
                int nbReclamations = rs.getInt("nb_reclamations");
                totalReclamations += nbReclamations;
                pieChartData.add(new PieChart.Data(type + " (" + nbReclamations + ")", nbReclamations));
            }

            // Calcul des pourcentages
            for (PieChart.Data data : pieChartData) {
                double pourcentage = (data.getPieValue() / totalReclamations) * 100;
                String label = data.getName() + " - " + String.format("%.2f", pourcentage) + "%";
                data.setName(label);
            }

            PieChart chart = new PieChart(pieChartData);
            chart.setTitle("Statistiques de r??clamations par type");

            Stage stage = new Stage();
            Scene scene = new Scene(new Group(chart), 600, 400);
            stage.setScene(scene);
            stage.show();

            rs.close();
            pst.close();
        } catch (SQLException ex) {
        }
    }

    private void trierDate(MouseEvent event) {
        TableColumn<Reclamation, Date> dateColumn = (TableColumn<Reclamation, Date>) event.getSource();
        tableReclamations.getSortOrder().clear(); // Supprimer tout tri pr??c??dent
        data.sort((r1, r2) -> r1.getDate().compareTo(r2.getDate()));
    }

    
    
    
    
public void sendMail(TextField emailField) {

    // R??cup??re l'adresse e-mail saisie dans le champ de texte
    String recipient = emailField.getText();

    // V??rifie que l'adresse e-mail est valide
    if (!isValidEmailAddress(recipient)) {
        // Affiche une notification d'erreur
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("L'adresse e-mail saisie n'est pas valide.");
        alert.showAndWait();
        return;
    }
// R??cup??re le type de r??clamation s??lectionn?? dans la ChoiceBox
String typedereclamation = tftype.getValue().toString();
String datereclamation = tfdate.getValue().toString();
    // Param??tres de configuration pour le serveur SMTP de Gmail
    String host = "smtp.gmail.com";
    int port = 587;
    String username = "autoxpress2023@gmail.com";
    String password = "wxrdwzwwjezmcavk";

    // Configuration de la session avec les propri??t??s n??cessaires
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", port);

    // Cr??ation de la session avec authentification
    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    });

    try {
        // Cr??ation du message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject("Confirmation de r??ception de votre r??clamation");
        message.setText("Cher client,\n"
                + "Nous avons bien re??u votre r??clamation de type " +typedereclamation+ " et de la date " +datereclamation+
                "\nElle est prise en consid??ration et nous vous remercions pour votre retour. Nous nous engageons ?? vous r??pondre dans les meilleurs d??lais.\nN'h??sitez pas ?? nous contacter si vous avez besoin d'aide.\nCordialement,\nL'??quipe du service client.");            
        // Envoi du message
        Transport.send(message);

        // Affiche une notification de succ??s
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succ??s");
        alert.setHeaderText(null);
        alert.setContentText("Votre r??clamation est envoy??e avec succ??s !");
        alert.showAndWait();

    } catch (MessagingException e) {
        // Affiche une notification d'erreur
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("Une erreur s'est produite lors de l'envoi du message : " + e.getMessage());
        alert.showAndWait();
    }
}

private boolean isValidEmailAddress(String email) {
    // V??rifie que l'adresse e-mail est valide en utilisant une expression r??guli??re
    String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    return email.matches(regex);
}
}
