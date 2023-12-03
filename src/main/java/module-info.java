module com.main.networksprojectq2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.main.networksprojectq2 to javafx.fxml;
    exports com.main.networksprojectq2;
}