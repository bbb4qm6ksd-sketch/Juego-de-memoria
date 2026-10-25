import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VistaLogin vista = new VistaLogin();
            vista.setVisible(true);
        });
    }
}