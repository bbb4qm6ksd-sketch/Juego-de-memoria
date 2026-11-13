import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;


public class ControladorClasico {

    private static final int PAUSA_FALLO_MS = 900;     // tiempo que se ven las dos cartas de un fallo

    private final VistaTablero vista;
    private final ModeloClasico modelo = new ModeloClasico();
    private final ModeloTienda tienda;
    private final ModeloPremios ranking;
    private final String[] nombres = new String[ModeloClasico.JUGADORES];

    private final Timer relojTurno = new Timer(100, e -> pasarTiempo());
    private final Timer pausaFallo = new Timer(PAUSA_FALLO_MS, e -> resolverFallo());

    private Carta primeraCarta = null;      // primera carta destapada de la jugada
    private Carta segundaCarta = null;      // segunda carta (solo se usa en un fallo)
    private String nombrePrimeraCarta = null;
    private boolean bloqueado = true;       // true = no se aceptan clics
    private long ultimoNs = System.nanoTime();
    private int ultimoSegundo = -1;

    // Cursor del teclado: en qué tablero y en qué carta está
    private int cursorJugador = 0;
    private int cursorFila = 0;
    private int cursorCol = 0;

    public ControladorClasico(VistaTablero vista, String jugador1, String jugador2) {
        this(vista, jugador1, jugador2, null, null);
    }

    public ControladorClasico(VistaTablero vista, String jugador1, String jugador2, ModeloTienda tienda) {
        this(vista, jugador1, jugador2, tienda, null);
    }

    public ControladorClasico(VistaTablero vista, String jugador1, String jugador2, ModeloTienda tienda, ModeloPremios ranking) {
        this.vista = vista;
        this.tienda = tienda;
        this.ranking = ranking;
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
        vista.getBtnFacil().addActionListener(e -> iniciarPartida(2));
        vista.getBtnMedio().addActionListener(e -> iniciarPartida(4));
        vista.getBtnDificil().addActionListener(e -> iniciarPartida(6));
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> manejarTecla(e));
    }

    public void setNombreJugador2(String nombre) {
        nombres[1] = nombre;
        vista.setNombreJugador(1, nombre);
    }

    // ---------- Ciclo de la partida ----------

    public void nuevaPartida() {
        iniciarPartida(modelo.getTamanoMatriz());
    }

    private void iniciarPartida(int tamano) {
        detener();
        vista.detenerConfeti();
        modelo.nuevaPartida(tamano);
        vista.mostrarTablero(tamano);

        for (int j = 0; j < ModeloClasico.JUGADORES; j++) {
            for (int f = 0; f < modelo.getTamanoMatriz(); f++) {
                for (int c = 0; c < modelo.getTamanoMatriz(); c++) {
                    Carta carta = vista.getCarta(j, f, c);
                    carta.setNombre(modelo.getNombre(j, f, c));
                    carta.setEstado(Carta.Estado.OCULTA);
                }
            }
            actualizarMarcadores(j);
        }
        primeraCarta = null;
        segundaCarta = null;
        nombrePrimeraCarta = null;
        actualizarTurno();
        bloqueado = false;
        arrancarReloj();
        imprimirTablerosEnConsola();
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
        String nombreCarta = modelo.getNombre(jugador, fila, columna);
        ModeloClasico.Resultado resultado = modelo.voltear(jugador, fila, columna);
        if (resultado != ModeloClasico.Resultado.IGNORADA) {
            System.out.printf("[CLÁSICO] %s eligió (%d,%d): %s → %s%n",
                    nombres[jugador], fila + 1, columna + 1, nombreCarta, resultado);
        }

        switch (resultado) {
            case IGNORADA:
                return;

            case PRIMERA:
                carta.setEstado(Carta.Estado.VOLTEADA);
                primeraCarta = carta;
                nombrePrimeraCarta = nombreCarta;
                System.out.printf("[PAREJAS] Primera carta: %s%n", nombrePrimeraCarta);
                break;

            case ACIERTO:
                carta.setEstado(Carta.Estado.ENCONTRADA);
                primeraCarta.setEstado(Carta.Estado.ENCONTRADA);
                System.out.printf("[PAREJAS] ¡Pareja! %s + %s. Progreso de %s: %d/%d; intentos: %d.%n",
                        nombrePrimeraCarta, nombreCarta, nombres[jugador], modelo.getParejas(jugador),
                        modelo.getParejasMaximas(), modelo.getIntentos(jugador));
                primeraCarta = null;
                nombrePrimeraCarta = null;
                Sonidos.reproducir(Sonidos.Efecto.ACIERTO);
                actualizarMarcadores(jugador);
                if (modelo.partidaTerminada()) {
                    terminar();
                } else if (modelo.jugadorTermino(jugador)) {
                System.out.printf("[CLÁSICO] %s completó su tablero con %d intentos.%n",
                        nombres[jugador], modelo.getIntentos(jugador));
                    vista.setMensaje(nombres[jugador] + " completó su tablero.");
                    modelo.cambiarTurno();
                System.out.printf("[CLÁSICO] Turno para %s.%n", nombres[modelo.getJugadorActivo()]);
                    actualizarTurno();
                    ultimoNs = System.nanoTime();
                } else {
                    vista.setMensaje("¡Pareja! +" + (int) ModeloClasico.BONO_ACIERTO + " segundos.");
                    mostrarTiempo();
                }
                break;

            case FALLO:
                System.out.printf("[PAREJAS] No coinciden: %s + %s. Progreso de %s: %d/%d; intentos: %d.%n",
                        nombrePrimeraCarta, nombreCarta, nombres[jugador], modelo.getParejas(jugador),
                        modelo.getParejasMaximas(), modelo.getIntentos(jugador));
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
        int tamano = modelo.getTamanoMatriz();
        int nuevaFila = (cursorFila + df + tamano) % tamano;
        int nuevaColumna = (cursorCol + dc + tamano) % tamano;
        if (modelo.getNombre(cursorJugador, nuevaFila, nuevaColumna) == null) {
            nuevaFila = (nuevaFila + df + tamano) % tamano;
            nuevaColumna = (nuevaColumna + dc + tamano) % tamano;
        }
        cursorFila = nuevaFila;
        cursorCol = nuevaColumna;
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
        System.out.printf("[CLÁSICO] Turno para %s. Marcadores: %s %d/%d (%d intentos), %s %d/%d (%d intentos).%n",
                nombres[modelo.getJugadorActivo()], nombres[0], modelo.getParejas(0), modelo.getParejasMaximas(),
                modelo.getIntentos(0), nombres[1], modelo.getParejas(1), modelo.getParejasMaximas(),
                modelo.getIntentos(1));
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
            System.out.printf("[CLÁSICO] Tiempo agotado para %s. Se cede el turno.%n", nombres[modelo.getJugadorActivo()]);
            vista.setMensaje("Se acabó el tiempo. Se cede el turno.");
            modelo.cambiarTurno();
        System.out.printf("[CLÁSICO] Turno para %s.%n", nombres[modelo.getJugadorActivo()]);
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

        String felicitacion;
        if (ganador < 0) {
            felicitacion = "¡Empate! Ambos completaron sus parejas.";
            System.out.printf("[RANKING] Empate: no se registra ganador. %s=%d intentos; %s=%d intentos.%n",
                    nombres[0], modelo.getIntentos(0), nombres[1], modelo.getIntentos(1));
        } else {
            felicitacion = "¡Felicidades, " + nombres[ganador] + "! Ganaste con "
                    + modelo.getIntentos(ganador) + " intentos.";
            System.out.printf("[CLÁSICO] Ganador: %s (%d intentos).%n", nombres[ganador], modelo.getIntentos(ganador));
            if (ranking != null) {
                ranking.registrar(nombres[ganador], modelo.getIntentos(ganador));
                ranking.recargar();
                int mejorIntento = ranking.getMejorIntento(nombres[ganador]);
                if (mejorIntento > 0) {
                    System.out.printf("[RANKING] Registro verificado en premios.txt: %s, récord de %d intentos. Partida actual: %d intentos.%n",
                            nombres[ganador], mejorIntento, modelo.getIntentos(ganador));
                } else {
                    System.out.printf("[RANKING] No se encontró el registro de %s después de guardar.%n", nombres[ganador]);
                }
                System.out.println("[RANKING] Tabla actual (mejores cinco por intentos):");
                for (ModeloPremios.Fila fila : ranking.getTabla(1.0)) {
                    System.out.printf("  %d. %s — %d intentos%n", fila.posicion, fila.jugador, fila.intentos);
                }
            }
        }
        if (tienda != null) {
            int estrellas = ModeloTienda.calcularEstrellas(modelo.getIntentos(0));
            tienda.agregarEstrellas(estrellas);
            felicitacion += " +" + estrellas + " estrellas.";
        }
        vista.mostrarFelicitacion(felicitacion);
        Sonidos.reproducir(Sonidos.Efecto.VICTORIA);
        vista.lanzarConfeti();
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
        vista.setParejas(jugador, modelo.getParejas(jugador), modelo.getParejasMaximas());
    }

    private void mostrarTiempo() {
        vista.setTiempoTurno(segundosRestantes(), (int) Math.ceil(modelo.getTiempoMaximo()));
    }

    private void imprimirTablerosEnConsola() {
        System.out.printf("%n========== MODO CLÁSICO: TABLEROS ==========%nDificultad: %dx%d | Parejas necesarias: %d%n",
                modelo.getTamanoMatriz(), modelo.getTamanoMatriz(), modelo.getParejasMaximas());
        for (int jugador = 0; jugador < ModeloClasico.JUGADORES; jugador++) {
            System.out.printf("%nTablero de %s:%n", nombres[jugador]);
            for (int fila = 0; fila < modelo.getTamanoMatriz(); fila++) {
                for (int columna = 0; columna < modelo.getTamanoMatriz(); columna++) {
                    String nombre = modelo.getNombre(jugador, fila, columna);
                    System.out.printf("%-20s", nombre == null ? "—" : nombre);
                }
                System.out.println();
            }
        }
        System.out.println("============================================");
    }




}

