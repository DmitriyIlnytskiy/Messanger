module org.example.messanger {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.messanger to javafx.fxml;
    exports org.example.messanger;
}