<<<<<<< HEAD
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
=======
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    IO.println(String.format("Hello and welcome!"));

    for (int i = 1; i <= 5; i++) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        IO.println("Marilyyyyyyyyyyyyyyyyn = " + i);
>>>>>>> origin/main
    }
}