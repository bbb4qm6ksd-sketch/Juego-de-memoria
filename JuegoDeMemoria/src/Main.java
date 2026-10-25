import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("tablero")) {
                VistaTablero v = new VistaTablero();
                v.setJugador("Camila");
                v.getCarta(0, 0).setEstado(Carta.Estado.VOLTEADA);
                v.getCarta(0, 0).setNombre("Orión");
                v.getCarta(0, 1).setEstado(Carta.Estado.ENCONTRADA);
                v.getCarta(0, 1).setNombre("Lira");
                v.setMensaje("Encuentra las 18 parejas.");
                v.setVisible(true);
            } else {
                new VistaLogin().setVisible(true);
            }
        });
    }
}