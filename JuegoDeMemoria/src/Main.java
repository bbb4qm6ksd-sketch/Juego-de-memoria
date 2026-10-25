import javax.swing.*;

import javax.swing.SwingUtilities;

/*
 * Previsualizacion de las vistas (datos de ejemplo, sin logica).
 * Argumento del programa: login (por defecto) | clasico | patrones | premios
 */

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            String vista = args.length > 0
                    ? args[0].toLowerCase()
                    : "login";

            if (vista.equals("clasico")) {

                VistaTablero v = new VistaTablero();

                v.setNombreJugador(0, "Camila");
                v.setNombreJugador(1, "Diego");
                v.setJugadorActivo(0);
                v.setTiempoTurno(7, 10);

                v.setIntentos(0, 4);
                v.setIntentos(1, 3);

                v.setParejas(0, 2, 18);
                v.setParejas(1, 1, 18);

                v.getCarta(0, 0, 0).setEstado(Carta.Estado.VOLTEADA);
                v.getCarta(0, 0, 0).setNombre("Orión");

                v.getCarta(0, 0, 1).setEstado(Carta.Estado.ENCONTRADA);
                v.getCarta(0, 0, 1).setNombre("Lira");

                v.getCarta(1, 2, 2).setEstado(Carta.Estado.ENCONTRADA);
                v.getCarta(1, 2, 2).setNombre("Leo");

                v.setMensaje("Camila: te quedan 7 segundos.");
                v.setVisible(true);

            } else if (vista.equals("patrones")) {

                VistaTablero v = new VistaTablero();

                v.setModo(VistaTablero.Modo.PATRONES);
                v.setNombreJugador(0, "Camila");
                v.setNivel(3);
                v.setRonda(1);

                v.setMensaje("Memoriza el patrón…");
                v.setTiempoTotal(8.5, 12);
                v.setTiempoCasilla(1.2, 2);

                v.getCarta(0, 0, 1).setEstado(Carta.Estado.VOLTEADA);
                v.getCarta(0, 2, 3).setEstado(Carta.Estado.VOLTEADA);
                v.getCarta(0, 4, 4).setEstado(Carta.Estado.VOLTEADA);

                v.setVisible(true);

            } else if (vista.equals("premios")) {

                VistaPremios v = new VistaPremios();

                v.setTipoCambio("₡ 510,00 por dólar (ejemplo)");

                v.setPuesto(1, "Camila", 18, "₡ 2.833");
                v.setPuesto(2, "Diego", 20, "₡ 2.550");
                v.setPuesto(3, "Andrea", 22, "₡ 2.318");
                v.setPuesto(4, "Luis", 25, "₡ 2.040");
                v.setPuesto(5, "Sofía", 28, "₡ 1.821");

                v.setVisible(true);

            } else {

                new VistaLogin().setVisible(true);

            }
        });
    }

}
