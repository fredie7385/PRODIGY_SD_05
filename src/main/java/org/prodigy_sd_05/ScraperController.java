package org.prodigy_sd_05;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ScraperController implements Initializable {

    private final Map<String, String[]> bookDetailsMap = new HashMap<>();
    @FXML
    private Label labelFrom;
    @FXML
    private Label labelPrice;
    @FXML
    private Label labelRating;
    @FXML
    private ListView<String> listView;
    @FXML
    private Button scrapeBooksButton;
    @FXML
    private Button downloadCSVButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String url = "https://books.toscrape.com/";
        String csvFile = "products.csv"; // Specify the CSV file name

        try {
            // Create a new file if it doesn't exist
            File file = new File(csvFile);
            boolean isNewFile = file.createNewFile(); // This will return true if the file was created

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                // Write the header only if the file was newly created
                if (isNewFile) {
                    writer.write("Title,Price,Rating\n"); // CSV header
                }

                Document document = Jsoup.connect(url).get();
                Elements books = document.select(".product_pod");

                if (books.isEmpty()) {
                    System.out.println("No books found. Please check the CSS selectors.");
                } else {
                    for (Element bk : books) {
                        String title = bk.select("h3 > a").attr("title");
                        String price = bk.select(".price_color").text();
                        String ratingClass = bk.select("p.star-rating").attr("class");

                        // Extract rating from the class name
                        String rating = ratingClass.isEmpty() ? "No Rating" : ratingClass.split(" ")[1];

                        // Map the rating class to a readable format
                        String readableRating = mapRating(rating);

                        // Write product information to the CSV file
                        writer.write(String.format("%s,%s,%s\n", title, price, readableRating));

                        // Add titles to the ListView and store details in a map
                        listView.getItems().add(title);
                        bookDetailsMap.put(title, new String[]{price, readableRating});
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Error creating the CSV file: " + e.getMessage());
        } finally {
            System.out.println("Scraping completed.");
        }

        // Add a listener to the ListView to handle item selection
        listView.setOnMouseClicked(event -> handleListViewClick(event));
    }


    @FXML
    private void handleDownloadCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(listView.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Title,Price,Rating\n");
                for (Map.Entry<String, String[]> entry : bookDetailsMap.entrySet()) {
                    String title = entry.getKey();
                    String[] details = entry.getValue();
                    writer.write(String.format("%s,%s,%s\n", title, details[0], details[1]));
                }
                showAlert("Success", "CSV file downloaded successfully.", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "Error writing CSV file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleScrapeBooks() {
        String url = "https://books.toscrape.com/";
        bookDetailsMap.clear();

        try {
            Document document = Jsoup.connect(url).get();
            Elements books = document.select(".product_pod");

            if (books.isEmpty()) {
                showAlert("Warning", "No books found. Please check the CSS selectors.", Alert.AlertType.WARNING);
                return;
            }

            listView.getItems().clear();

            for (Element bk : books) {
                String title = bk.select("h3 > a").attr("title");
                String price = bk.select(".price_color").text();
                String ratingClass = bk.select("p.star-rating").attr("class");

                String rating = ratingClass.isEmpty() ? "No Rating" : ratingClass.split(" ")[1];

                String readableRating = mapRating(rating);


                listView.getItems().add(title);
                bookDetailsMap.put(title, new String[]{price, readableRating});
            }
            showAlert("Success", "Scraping completed. " + books.size() + " books found.", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Error during web scraping: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void handleListViewClick(MouseEvent event) {

        String selectedBook = listView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {

            String[] details = bookDetailsMap.get(selectedBook);
            if (details != null) {
                labelFrom.setText(selectedBook);
                labelPrice.setText(details[0]); // Price
                labelRating.setText(details[1]); // Rating
            }
        }
    }


    private String mapRating(String rating) {
        switch (rating) {
            case "One":
                return "1 Star";
            case "Two":
                return "2 Stars";
            case "Three":
                return "3 Stars";
            case "Four":
                return "4 Stars";
            case "Five":
                return "5 Stars";
            default:
                return "No Rating";
        }
    }
}