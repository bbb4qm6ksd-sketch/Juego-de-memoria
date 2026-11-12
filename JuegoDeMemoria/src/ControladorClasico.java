import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;


public class ControladorClasico {

    private static final int PAUSA_FALLO_MS = 900;     // tiempo que se ven las dos cartas de un fallo

    private final VistaTablero vista;
    private final ModeloClasico modelo = new ModeloClasico();
    private final String[] nombres = new String[ModeloClasico.JUGADORES];

    private final Timer relojTurno = new Timer(100, e -> pasarTiempo());
    private final Timer pausaFallo = new Timer(PAUSA_FALLO_MS, e -> resolverFallo());

    private Carta primeraCarta = null;      // primera carta destapada de la jugada
    private Carta segundaCarta = null;      // segunda carta (solo se usa en un fallo)
    private boolean bloqueado = true;       // true = no se aceptan clics
    private long ultimoNs = System.nanoTime();
    private int ultimoSegundo = -1;

    // Cursor del teclado: en qué tablero y en qué carta está
    private int cursorJugador = 0;
    private int cursorFila = 0;
    private int cursorCol = 0;

    public ControladorClasico(VistaTablero vista, String jugador1, String jugador2) {
        this.vista = vista;
        nombres[0] = jugador1;
        nombres[1] = jugador2;
        pausaFallo.setRepeats(false);

        vista.setNombreJugador(0, jugador1);
        vista.setNombreJugador(1, jugador2);

        for (int j = 0; j < ModeloClasico.JUGADORES; j++) {
            for (int f = 0; f < ModeloClasico.FILAS; f++) {
                for (int c = 0; c < ModeloClasico.COLUMNAS; c++) {
                    final int jug = j, fila = f, col = c;
                    vista.getCarta(j, f, c).addActionListener(e -> pulsarCarta(jug, fila, col));
                }
            }
        }
        vista.getBtnAccion().addActionListener(e -> nuevaPartida());
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> manejarTecla(e));
    }

    // ---------- Ciclo de la partida ----------

    public void nuevaPartida() {
        detener();
        vista.detenerConfeti();
        modelo.nuevaPartida();

        for (int j = 0; j < ModeloClasico.JUGADORES; j++) {
            for (int f = 0; f < ModeloClasico.FILAS; f++) {
                for (int c = 0; c < ModeloClasico.COLUMNAS; c++) {
                    Carta carta = vista.getCarta(j, f, c);
                    carta.setNombre(modelo.getNombre(j, f, c));
                    carta.setEstado(Carta.Estado.OCULTA);
                }
            }
            actualizarMarcadores(j);
        }
        primeraCarta = null;
        segundaCarta = null;
        actualizarTurno();
        bloqueado = false;
        arrancarReloj();


    }

    // Para relojes y bloquea el tablero (al salir al menu, cerrar sesion o reiniciar).
    public void detener() {
        relojTurno.stop();
        pausaFallo.stop();
        bloqueado = true;
        marcarCursor(false);
    }

    private void arrancarReloj() {
        ultimoNs = System.nanoTime();
        ultimoSegundo = -1;
        relojTurno.start();
    }

    // ---------- Clic en una carta ----------

    private void pulsarCarta(int jugador, int fila, int columna) {
        if (bloqueado) return;
        Carta carta = vista.getCarta(jugador, fila, columna);

        switch (modelo.voltear(jugador, fila, columna)) {
            case IGNORADA:
                return;

            case PRIMERA:
                carta.setEstado(Carta.Estado.VOLTEADA);
                primeraCarta = carta;
                break;

            case ACIERTO:
                carta.setEstado(Carta.Estado.ENCONTRADA);
                primeraCarta.setEstado(Carta.Estado.ENCONTRADA);
                primeraCarta = null;
                Sonidos.reproducir(Sonidos.Efecto.ACIERTO);
                actualizarMarcadores(jugador);
                if (modelo.partidaTerminada()) {
                    terminar();
                } else if (modelo.jugadorTermino(jugador)) {
                    vista.setMensaje(nombres[jugador] + " completó su tablero.");
                    modelo.cambiarTurno();
                    actualizarTurno();
                    ultimoNs = System.nanoTime();
                } else {
                    vista.setMensaje("¡Pareja! +" + (int) ModeloClasico.BONO_ACIERTO + " segundos.");
                    mostrarTiempo();
                }
                break;

            case FALLO:
                carta.setEstado(Carta.Estado.VOLTEADA);
                segundaCarta = carta;
                bloqueado = true;
                relojTurno.stop();
                Sonidos.reproducir(Sonidos.Efecto.FALLO);
                actualizarMarcadores(jugador);
                vista.setMensaje("No coinciden. Se cede el turno.");
                pausaFallo.restart();
                break;
        }
    }

    // ---------- Teclado ----------

    // Devuelve true si la tecla se usó (así nadie más la procesa)
    private boolean manejarTecla(KeyEvent e) {
        if (e.getID() != KeyEvent.KEY_PRESSED || bloqueado) return false;

        int tecla = e.getKeyCode();

        // Espacio = confirmar: es lo mismo que hacer clic en la carta del cursor
        if (tecla == KeyEvent.VK_SPACE) {
            pulsarCarta(cursorJugador, cursorFila, cursorCol);
            return true;
        }

        // WASD o flechas = mover el cursor
        int df = 0, dc = 0;
        if (tecla == KeyEvent.VK_UP    || tecla == KeyEvent.VK_W) df = -1;
        else if (tecla == KeyEvent.VK_DOWN  || tecla == KeyEvent.VK_S) df = 1;
        else if (tecla == KeyEvent.VK_LEFT  || tecla == KeyEvent.VK_A) dc = -1;
        else if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) dc = 1;
        else return false;   // otra tecla: no hacemos nada

        marcarCursor(false);   // quita el resaltado de la carta actual
        cursorFila = (cursorFila + df + ModeloClasico.FILAS) % ModeloClasico.FILAS;
        cursorCol  = (cursorCol  + dc + ModeloClasico.COLUMNAS) % ModeloClasico.COLUMNAS;
        marcarCursor(true);    // resalta la nueva
        return true;
    }

    // Enciende o apaga el resaltado de la carta donde está el cursor
    private void marcarCursor(boolean activo) {
        vista.getCarta(cursorJugador, cursorFila, cursorCol).setResaltada(activo);
    }

    // Pasados unos instantes de un fallo: se tapan las cartas y juega el otro.
    private void resolverFallo() {
        if (primeraCarta != null) primeraCarta.setEstado(Carta.Estado.OCULTA);
        if (segundaCarta != null) segundaCarta.setEstado(Carta.Estado.OCULTA);
        primeraCarta = null;
        segundaCarta = null;
        modelo.cambiarTurno();
        actualizarTurno();
        bloqueado = false;
        arrancarReloj();
    }

    // ---------- Reloj del turno ----------

    private void pasarTiempo() {
        long ahora = System.nanoTime();
        modelo.restarTiempo((ahora - ultimoNs) / 1e9);     // tiempo real transcurrido
        ultimoNs = ahora;
        mostrarTiempo();

        if (modelo.tiempoAgotado()) {
            relojTurno.stop();
            if (primeraCarta != null) {                    // quedo una carta a medias: se tapa
                primeraCarta.setEstado(Carta.Estado.OCULTA);
                primeraCarta = null;
            }
            Sonidos.reproducir(Sonidos.Efecto.FALLO);
            vista.setMensaje("Se acabó el tiempo. Se cede el turno.");
            modelo.cambiarTurno();
            actualizarTurno();
            arrancarReloj();
            return;
        }

        int seg = segundosRestantes();                     // tic en los ultimos 3 segundos
        if (seg != ultimoSegundo) {
            ultimoSegundo = seg;
            if (seg >= 1 && seg <= 3) Sonidos.reproducir(Sonidos.Efecto.TICK);
        }
    }

    private int segundosRestantes() {
        return (int) Math.ceil(modelo.getTiempo() - 1e-6);
    }

    // ---------- Fin de la partida ----------

    private void terminar() {
        detener();
        int ganador = modelo.getGanador();

        String titulo;
        String mensaje;
        if (ganador < 0) {
            titulo = "!Empate!";
            mensaje = " Ambos usaron " + modelo.getIntentos(0) + " intentos.";
        } else {
            titulo = "!FELICIDADES!";
            mensaje = "¡Gana " + nombres[ganador] + " con " + modelo.getIntentos(ganador)
                    + " intentos!  (" + nombres[1 - ganador] + ": " + modelo.getIntentos(1 - ganador) + ")";
        }
        vista.setMensaje(titulo);
        Sonidos.reproducir(Sonidos.Efecto.VICTORIA);
        vista.lanzarConfeti();
        vista.mostrarMensajeFinal(titulo, mensaje);
    }

    // ---------- Refrescar la vista ----------

    private void actualizarTurno() {
        int activo = modelo.getJugadorActivo();
        vista.setJugadorActivo(activo);
        vista.setMensaje("Turno de " + nombres[activo] + ".");
        mostrarTiempo();

        // el cursor pasa al tablero del jugador que tiene el turno
        marcarCursor(false);
        cursorJugador = activo;
        cursorFila = 0;
        cursorCol = 0;
        marcarCursor(true);
    }

    private void actualizarMarcadores(int jugador) {
        vista.setIntentos(jugador, modelo.getIntentos(jugador));
        vista.setParejas(jugador, modelo.getParejas(jugador), ModeloClasico.PAREJAS);
    }

    private void mostrarTiempo() {
        vista.setTiempoTurno(segundosRestantes(), (int) Math.ceil(modelo.getTiempoMaximo()));
    }




}