/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoxpress.GUI;

import autoxpress.entities.Livraison;
import autoxpress.utils.MyConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author user
 */
public class LivraisonConducteurController implements Initializable {

    @FXML
    private TableView<Livraison> table_livraison;
    @FXML
    private TableColumn<Livraison, Integer> colid_livraison;
    @FXML
    private TableColumn<Livraison, String> coladdresse_liv;
    @FXML
    private TableColumn<Livraison, String> coldestinataire;
    @FXML
    private TableColumn<Livraison, String> colcontenu;
    @FXML
    private TableColumn<Livraison, Float> colprix_liv;
    @FXML
    private Button btnAffLiv;
    
                public ObservableList<Livraison> data = FXCollections.observableArrayList();
    @FXML
    private TableColumn<Livraison, Float> colpoids;


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void afficherliv(ActionEvent event) {
        
         try{
            String requete = "SELECT * FROM livraison";
        Statement st = MyConnection.getInstance().getCnx()
                .createStatement();
        ResultSet rs = st.executeQuery(requete);
        while(rs.next()) {
            data.add(new Livraison(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getFloat(4), rs.getString(5), rs.getFloat(6)));
            
        }
        }catch(Exception e){
            e.printStackTrace();
        }
        colid_livraison.setCellValueFactory(new PropertyValueFactory<Livraison, Integer>("id_livraison"));
        coladdresse_liv.setCellValueFactory(new PropertyValueFactory<Livraison, String>("addresse_liv"));
        coldestinataire.setCellValueFactory(new PropertyValueFactory<Livraison, String>("destinataire"));
        colpoids.setCellValueFactory(new PropertyValueFactory<Livraison, Float>("poids"));
        colcontenu.setCellValueFactory(new PropertyValueFactory<Livraison, String>("contenu"));
        colprix_liv.setCellValueFactory(new PropertyValueFactory<Livraison, Float>("prix_liv"));



    table_livraison.setItems(data);
    }
    
}
