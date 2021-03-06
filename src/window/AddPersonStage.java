/**
 * Changelog:
 * 2016-02-23 : Built basic layout from top down to drop-down menus.  Need to fix ordering of labels
 * 
 * 2016-02-24 : Fixed ordering of input area first row.
 * 2016-02-24 : Added rest of components and fixed spacing/alignment
 * 2016-02-24 : Added updateAllTables() and updateTable(Shift) methods
 * 2016-02-24 : Got three-shift view working
 * 2016-02-24 : Got rank/shift/skill/workcenter choiceboxes populated
 * 
 * 2016-02-25 : Got workcenter choicebox to change what is shown in the tables
 * 2016-02-25 : Implemented 'add person' functionality, it verifies that all data has been entered (except date), and only updates the affected shift table
 * 2016-02-25 : Added Skill Level column to people tables, moved Rank column to far left
 * 
 * 2016-02-28 : Extracted logic from addButton's action handler to a new addPerson() method
 * 2016-02-28 : The addPerson() method now also adds a start date for the added person
 * 2016-02-28 : Added closeWindow() method to consult user whenever entered data will be lost
 * 2016-02-28 : Added cancelButton that calls closeWindow()
 * 
 * 2016-02-29 : Added Javadoc for: addPerson(), closeWindow(), display(), updateAllTables(), updateTables()
 * 2016-02-29 : Updated addPerson() so that the ChoiceBoxes are cleared out after a successful add
 * 2016-02-29 : Reordered method declarations so that they are in alphabetic order
 * 
 * 2016-03-01 : Added an AlertBox message to the latter part of addPerson()
 * 
 * 2016-03-02 : Created 'dateChoice' field to hold the current date selected by startDateBox
 * 2016-03-02 : Commented out the nullification of startDateBox's date after a person is added
 * 2016-03-02 : Made startDateBox default to today's date when the window is opened
 * 
 * 2016-03-24 : Grouped imports by root package
 * 2016-03-24 : Formatted to match Google Java Style
 * 
 * 2016-03-25 : Replaced debug System.out calls with Logger calls
 */

package window;

import domain.Person;
import domain.PersonDAO;
import domain.RankDAO;
import domain.ShiftDAO;
import domain.SkillDAO;
import domain.WorkcenterDAO;

import java.sql.Date;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import window.modal.AlertBox;
import window.modal.ConfirmBox;

/**
 *
 * @author alanjohnson
 */
public class AddPersonStage {
    
    // ===========================  Inputs  ==========================
    private static TableView swingsTable;
    private static TableView daysTable;
    private static TableView midsTable;
    private static ChoiceBox skillBox;
    private static TextField lastNameField;
    private static TextField firstNameField;
    private static DatePicker startDateBox;
    private static ChoiceBox rankBox;
    private static ChoiceBox shiftBox;
    private static ChoiceBox workcenterBox;
    private static LocalDate dateChoice;
    
    // ============================  Misc  ===========================
    private static Logger logger = Logger.getLogger(AddPersonStage.class.getName());
    private static Stage window;
    
    // ============================  Misc  ===========================
    private enum Shift { MIDS, DAYS, SWINGS};
    
    // ===========================  Methods  =========================
    
    /**
     * <p>Checks the inputted data for validity, then creates a new
     * <code>Person</code> instance from that data, and finally calls
     * <code>PersonDAO.addPerson()</code> to add the new person and new start
     * date.</p>
     */
  private static void addPerson() {

    // =======================  Gather  Inputs  ======================
    String firstName = firstNameField.getText();
    String lastName = lastNameField.getText();
    int workcenterID = workcenterBox.getSelectionModel().getSelectedIndex() + 1;
    int shiftID = shiftBox.getSelectionModel().getSelectedIndex() + 1;
    int rankID = rankBox.getSelectionModel().getSelectedIndex() + 1;
    int skillID = skillBox.getSelectionModel().getSelectedIndex() + 1;
    LocalDate startLocalDate = startDateBox.getValue();

    // =======================  Verify  Inputs  ======================
    if (firstName.equals("") || lastName.equals("")
        || workcenterID < 1 || shiftID < 1
        || rankID < 1 || skillID < 1
        || startLocalDate == null) {
      String title = "Invalid input";
      String message = "Please ensure all inputs are filled.";
      (new AlertBox()).display(title, message);
      return;
    }

    //  TODO:  Acquire start date data and use it as part of input requirements
    Date startDate = Date.valueOf(startLocalDate);

    PersonDAO personDao = new PersonDAO();

    //  DEBUG:
    logger.fine("\nworkcenterID = " + workcenterID);
    logger.fine("\nshiftID = "      + shiftID);
    logger.fine("\nrankID = "       + rankID);
    logger.fine("\nStart Date = "   + startDate);

    if (personDao.addPerson(firstName, lastName, rankID, workcenterID,
                            shiftID,   skillID,  startDate)) {
      firstNameField.setText("");
      lastNameField.setText("");
      shiftBox.getSelectionModel().select(null);
      rankBox.getSelectionModel().select(null);
      skillBox.getSelectionModel().select(null);
//            startDateBox.setValue(null);  //  should not null out, now that tables populate based on selected date

      //  TODO: Make this line update only the shift affected
      switch (shiftID) {
        case 1:
          updateTable(Shift.MIDS);
          break;

        case 2:
          updateTable(Shift.DAYS);
          break;

        case 3:
          updateTable(Shift.SWINGS);
          break;
      }  //  Update table for affected shift

      String title = "Success";
      String message = "Successfully added new person";
      AlertBox.display(title, message);

    }  //  Adding new person was successful

  } //  end method addPerson()

  /**
   * <p>
   * This method first checks if any data has been entered by the user, which
   * may be lost if it in fact closes.</p>
   *
   * <p>
   * If there is any unsaved data, a <code>ConfirmBox</code> instance is created
   * to verify the user's request.</p>
   */
  private static void closeWindow() {
    //  TODO:  Removed workcenter from the data check.  Verify this is desired.

    // =======================   Check Inputs  =======================
    String firstName = firstNameField.getText();
    String lastName = lastNameField.getText();
    int shiftID = shiftBox.getSelectionModel().getSelectedIndex() + 1;
    int rankID = rankBox.getSelectionModel().getSelectedIndex() + 1;
    int skillID = skillBox.getSelectionModel().getSelectedIndex() + 1;
    LocalDate startLocalDate = startDateBox.getValue();

    // =================   If data has been entered,  ================
    // ===================   verify close request  ===================
    if (!firstName.equals("") || !lastName.equals("") || shiftID >= 1 ||
        rankID >= 1           || skillID >= 1         ||
        startLocalDate != null) {

      String title = "Confirm close";
      String message = "You have entered data.\nAre you sure you want to close?";

      if (!ConfirmBox.display(title, message)) {
        return;
      }
    }

    window.close();
  }  //  end method closeWindow()

    /**
     * <p>Displays the 'Add Person' stage (window)</p>
     */
  public static void display() {
    window = new Stage();
    window.setTitle("Add Person");
    window.setOnCloseRequest(e -> {
      e.consume();
      closeWindow();
    });
    Pane rootLayout = new VBox(20);
    Scene scene = new Scene(rootLayout, 600, 750);
    window.setScene(scene);

    // ===========================  Title Label  ==========================
    Pane titlePane = new StackPane();
    rootLayout.getChildren().add(titlePane);

    // ===========================  Input Area  ===========================
    GridPane inputPane = new GridPane();
    inputPane.setAlignment(Pos.CENTER);
    inputPane.setHgap(60);
    inputPane.setVgap(10);
    inputPane.setPadding(new Insets(0, 20, 0, 20));
    rootLayout.getChildren().add(inputPane);

    //      ========================  Labels  ========================
    //           ==================  1st  Row  ==================
    Label workcenterLabel = new Label("Workcenter");
    GridPane.setConstraints(workcenterLabel, 0, 0);

    Label shiftLabel = new Label("Shift");
    GridPane.setConstraints(shiftLabel, 1, 0);

    Label rankLabel = new Label("Rank");
    GridPane.setConstraints(rankLabel, 2, 0);

    Label startDateLabel = new Label("Start Date");
    GridPane.setConstraints(startDateLabel, 3, 0);

    //           ==================  2nd  Row  ==================
    Label firstNameLabel = new Label("First Name");
    GridPane.setConstraints(firstNameLabel, 0, 2);

    Label lastNameLabel = new Label("Last Name");
    GridPane.setConstraints(lastNameLabel, 1, 2);

    Label skillLabel = new Label("Skill Level");
    GridPane.setConstraints(skillLabel, 2, 2);

    inputPane.getChildren().addAll(workcenterLabel, shiftLabel,
        rankLabel, startDateLabel,
        firstNameLabel, lastNameLabel,
        skillLabel);

    //      ========================  Inputs  ========================
    //           ==================  1st  Row  ==================
    workcenterBox = new ChoiceBox((new WorkcenterDAO()).getList());
    workcenterBox.getSelectionModel().selectedIndexProperty()
        .addListener(new ChangeListener<Number>() {
          @Override
          public void changed(ObservableValue ov, Number oldValue, Number newValue) {
            updateAllTables();
          }
        });
    GridPane.setConstraints(workcenterBox, 0, 1);

    shiftBox = new ChoiceBox((new ShiftDAO()).getList());
    GridPane.setConstraints(shiftBox, 1, 1);

    rankBox = new ChoiceBox((new RankDAO()).getList());
    GridPane.setConstraints(rankBox, 2, 1);

    startDateBox = new DatePicker(LocalDate.now());
    startDateBox.valueProperty().addListener((obsVal, oldVal, newVal) -> {
      dateChoice = newVal;
      logger.log(Level.FINE, "Date: %s\n", dateChoice);
      updateAllTables();
    });
    GridPane.setConstraints(startDateBox, 3, 1);

    //           ==================  2nd  Row  ==================
    firstNameField = new TextField();
    GridPane.setConstraints(firstNameField, 0, 3);

    lastNameField = new TextField();
    GridPane.setConstraints(lastNameField, 1, 3);

    skillBox = new ChoiceBox((new SkillDAO()).getList());
    GridPane.setConstraints(skillBox, 2, 3);

    //           ==================  3rd  Row  ==================
    //           =================  Add Button  =================
    Button addButton = new Button("Add");
    addButton.setOnAction(e -> addPerson());
    HBox buttonPane = new HBox(10);
    GridPane.setConstraints(buttonPane, 0, 4, 4, 1);

    //           ===============  Cancel  Button  ===============
    Button cancelButton = new Button("Cancel");
    cancelButton.setOnAction(e -> closeWindow());
    GridPane.setConstraints(buttonPane, 0, 4, 4, 1);

    buttonPane.getChildren().addAll(addButton, cancelButton);
    buttonPane.setAlignment(Pos.CENTER);

    //           ===============  Finish  Inputs  ===============
    inputPane.getChildren().addAll(workcenterBox,  shiftBox,
                                   rankBox,        startDateBox,
                                   firstNameField, lastNameField,
                                   skillBox,       buttonPane);

    // ===========================  Table Area  ===========================
    VBox tableBox = new VBox(10);
    tableBox.setAlignment(Pos.CENTER);
    tableBox.setPadding(new Insets(10, 30, 30, 30));
    rootLayout.getChildren().add(tableBox);

    Label midsLabel = new Label("Mids");
    midsTable = new TableView();

    Label daysLabel = new Label("Days");
    daysTable = new TableView();

    Label swingsLabel = new Label("Swings");
    swingsTable = new TableView();

    tableBox.getChildren().addAll(midsLabel,   midsTable,
                                  daysLabel,   daysTable,
                                  swingsLabel, swingsTable);

    // =============================  Finish  =============================
    updateAllTables();
    window.initModality(Modality.APPLICATION_MODAL);
    window.showAndWait();

  }
    
    
  /**
   * <p>Calls <code>updateTable()</code> on all three <code>Shift</code>
   * members.</p>
   */
  static private void updateAllTables() {
    updateTable(Shift.MIDS);
    updateTable(Shift.DAYS);
    updateTable(Shift.SWINGS);
  }
    
  /**
   * <p>Builds a table model according to the given <code>Shift</code>
   * parameter and assigns it to the appropriate table.</p>
   * 
   * @param shift     Indicates which shift's table that should be updated
   */
  static private void updateTable(Shift shift) {

    //  1. Build model/data
    //  2. Turn off auto resize (if applicable/necessary)
    //  3. Set column widths
    //  4. Create Columns
    TableColumn rankColumn = new TableColumn("Rank");
    TableColumn firstNameColumn = new TableColumn("First Name");
    TableColumn lastNameColumn = new TableColumn("Last Name");
    TableColumn skillColumn = new TableColumn("Skill Lv");

    //  Bind Person properties to table columns
    rankColumn.setCellValueFactory(
        new PropertyValueFactory<Person, String>("rank")
    );
    firstNameColumn.setCellValueFactory(
        new PropertyValueFactory<Person, String>("firstName")
    );
    lastNameColumn.setCellValueFactory(
        new PropertyValueFactory<Person, String>("lastName")
    );
    skillColumn.setCellValueFactory(
        new PropertyValueFactory<Person, String>("skill")
    );

    ObservableList<Person> data = null;
    TableView table = null;
    PersonDAO personDao = new PersonDAO();
    int workcenterID = workcenterBox.getSelectionModel().getSelectedIndex() + 1;

    //  DEBUG:
    logger.fine("\nSelected Workcenter: " + workcenterID);

    switch (shift) {
      case MIDS:
        table = midsTable;
        data = personDao.getPeopleObsListByShift(1, workcenterID, dateChoice);
        break;

      case DAYS:
        table = daysTable;
        data = personDao.getPeopleObsListByShift(2, workcenterID, dateChoice);
        break;

      case SWINGS:
        table = swingsTable;
        data = personDao.getPeopleObsListByShift(3, workcenterID, dateChoice);
        break;
    }

    table.getColumns().clear();
    table.getColumns().addAll(rankColumn, firstNameColumn, lastNameColumn, skillColumn);
    table.setItems(data);
  }

}
