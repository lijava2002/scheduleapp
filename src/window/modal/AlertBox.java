//  AlertBox.java

/**
 * Changelog:
 * 2016-02-25 : Created file
 * 
 * 2016-02-29 : Removed unused imports
 * 2016-02-29 : Added Javadoc for display()
 */

package window.modal;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author alanjohnson
 */
public class AlertBox {
    
    /**
     * <p>Displays a window to let the user know some piece of information,
     * without asking for a hard confirmation.</p>
     * 
     * @param title       title of the pop-up window
     * @param message     message to be relayed to the user
     */
    public static void display (String title, String message) {
        Stage window = new Stage();
        window.setTitle(title);
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10, 5, 10, 5));
        Scene scene = new Scene(layout, 200, 100);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        layout.getChildren().add(messageLabel);
        window.setScene(scene);
        
        // ==========================  Buttons  ==========================
        Button confirmButton = new Button("OK");
        confirmButton.setOnAction(e -> {
            window.close();
                });
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(confirmButton);
        layout.getChildren().add(buttonBox);
        
        // ==========================  Sizing  ===========================
        double height = window.getHeight();
        //  Get width based on the Golden Ratio:
        double width = height * 1.618;
        
        window.setMinHeight(height);
        window.setMinWidth(width);
        window.setResizable(false);
        
        //  The following makes messageLabel grow to its preferred size, and
        //  then increases the window's height by the amount messageLabel itself
        //  increased:
        messageLabel.setMinHeight(Control.USE_PREF_SIZE);
        
        // =========================  Finalize  ==========================
        window.initModality(Modality.APPLICATION_MODAL);
        window.showAndWait();
    }
    
}
