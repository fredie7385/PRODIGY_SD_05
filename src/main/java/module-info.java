module org.prodigy_sd_05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;


    opens org.prodigy_sd_05 to javafx.fxml;
    exports org.prodigy_sd_05;
}