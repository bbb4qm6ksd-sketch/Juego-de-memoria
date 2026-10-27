import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Sonidos.iniciar();
            VistaLogin login = new VistaLogin();
            new ControladorLogin(login);
            login.setVisible(true);
        });
    }
}