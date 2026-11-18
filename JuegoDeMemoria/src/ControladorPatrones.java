import javax.swing.Timer;


// Coordina la partida de patrones: conecta las acciones de la pantalla con las reglas del modelo.
public class ControladorPatrones {

    private final VistaPatrones vista;
    private final ModeloPatrones modelo = new ModeloPatrones();
    private final Runnable alSalir;
    private final ModeloTienda tienda;
    private final ModeloPerfil perfil;

    private boolean jugando = false;
    private boolean perdio = false;
    private int paso = 0;
    private int rondasCompletadas = 0;
    private int estrellasGanadasPartida = 0;
    private VistaPatrones.Casilla encendida = null;

    private final Timer relojMostrar = new Timer(500, e -> mostrarSiguienteCasilla());
    private final Timer relojJuego = new Timer(100, e -> pasarTiempo());
    private final Timer relojSiguiente = new Timer(50, e -> pasarCuentaAtras());

    // Prepara la partida y conecta la pantalla con las opciones disponibles.
    public ControladorPatrones(VistaPatrones vista) {
        this(vista, null, null);
    }

    // Prepara la partida y conecta la pantalla con las opciones disponibles.
    public ControladorPatrones(VistaPatrones vista, Runnable alSalir) {
        this(vista, alSalir, null);
    }

    // Prepara la partida y conecta la pantalla con las opciones disponibles.
    public ControladorPatrones(VistaPatrones vista, Runnable alSalir, ModeloTienda tienda) {
        this(vista, alSalir, tienda, null);
    }

    // Prepara la partida y conecta la pantalla con las opciones disponibles.
    public ControladorPatrones(VistaPatrones vista, Runnable alSalir, ModeloTienda tienda, ModeloPerfil perfil) {
        this.vista = vista;
        this.alSalir = alSalir;
        this.tienda = tienda;
        this.perfil = perfil;

        vista.setNivel(modelo.getNivel());
        vista.setRonda(modelo.getRonda());
        mostrarTiempos();
        vista.getBtnComenzar().addActionListener(e -> comenzar());
        vista.getBtnSalir().addActionListener(e -> salir());

        for (int fila = 0; fila < ModeloPatrones.FILAS; fila++) {
            for (int columna = 0; columna < ModeloPatrones.COLUMNAS; columna++) {
                final int f = fila, c = columna;
                vista.getCasilla(f, c).addActionListener(e -> pulsarCasilla(f, c));
            }
        }

    }

    // Inicia una ronda y prepara el tablero para mostrar el patron.
    private void comenzar() {
        relojSiguiente.stop();
        vista.ocultarCuentaAtras();
        vista.detenerConfeti();
        if (perdio) {
            modelo.nuevoJuego();
            perdio = false;
            rondasCompletadas = 0;
            estrellasGanadasPartida = 0;
        }

        modelo.nuevaRonda();
        vista.setNivel(modelo.getNivel());
        vista.setRonda(modelo.getRonda());
        mostrarTiempos();

        vista.limpiarCasillas();
        vista.setFase("Memoriza el patrón");
        vista.getBtnComenzar().setText("COMENZAR");
        vista.getBtnComenzar().setVisible(false);

        paso = 0;
        encendida = null;
        relojMostrar.start();
    }

    // Ilumina cada casilla del patron en orden y luego habilita la respuesta.
    private void mostrarSiguienteCasilla() {
        if (encendida != null) {
            encendida.setEstado(VistaPatrones.Casilla.Estado.REPOSO);
            encendida = null;
        } else if (paso < modelo.getTamanoPatron()) {
            encendida = vista.getCasilla(modelo.getFilaPatron(paso), modelo.getColumnaPatron(paso));
            encendida.setEstado(VistaPatrones.Casilla.Estado.ILUMINADA);
            paso++;
        } else {
            relojMostrar.stop();
            vista.limpiarCasillas();
            vista.setFase("Repite el patrón");
            jugando = true;
            relojJuego.start();
        }
    }

    // Comprueba la casilla elegida y actualiza la pantalla segun el resultado.
    private void pulsarCasilla(int fila, int columna) {
        if (!jugando) return;

        boolean acierto = modelo.pulsar(fila, columna);
        if (acierto) {
            vista.getCasilla(fila, columna).setEstado(VistaPatrones.Casilla.Estado.ACIERTO);
            if (modelo.patronCompleto()) ganarRonda();
        } else {
            vista.getCasilla(fila, columna).setEstado(VistaPatrones.Casilla.Estado.ERROR);
            terminarPorError("Fallaste. Pulsa REINTENTAR para volver a empezar");
        }
    }

    // Reduce el tiempo disponible y termina la ronda si se agota.
    private void pasarTiempo() {
        modelo.restarTiempo(0.1);
        mostrarTiempos();
        if (modelo.tiempoAgotado()) terminarPorError("Se acabó el tiempo. Pulsa REINTENTAR para volver a empezar");
    }

    // Actualiza la espera entre rondas y comienza la siguiente cuando llega a cero.
    private void pasarCuentaAtras() {
        modelo.restarCuentaAtras(0.05);
        if (modelo.cuentaAtrasTerminada()) {
            relojSiguiente.stop();
            comenzar();                    // comenzar() ya oculta la capa
        } else {
            vista.mostrarCuentaAtras(modelo.getTiempoSiguienteRonda());
        }
    }

    // Registra la ronda completada, entrega estrellas y prepara la siguiente.
    private void ganarRonda() {
        detener();
        int estrellasGanadas = 0;
        if (tienda != null) {
            estrellasGanadas = ModeloTienda.calcularEstrellasPatrones(modelo.getNivel());
            tienda.agregarEstrellas(estrellasGanadas);
        }
        rondasCompletadas++;
        estrellasGanadasPartida += estrellasGanadas;
        modelo.siguienteRonda();
        vista.setNivel(modelo.getNivel());
        vista.setRonda(modelo.getRonda());
        vista.setFase(tienda == null ? "¡Correcto!" : "¡Correcto! +" + estrellasGanadas + " estrellas");
        vista.lanzarConfeti();

        modelo.iniciarCuentaAtras();
        vista.mostrarCuentaAtras(modelo.getTiempoSiguienteRonda());
        relojSiguiente.start();
    }

    // Detiene la partida y muestra el motivo por el que termino.
    private void terminarPorError(String mensaje) {
        detener();
        if (perfil != null) perfil.registrarPatrones(rondasCompletadas, estrellasGanadasPartida);
        perdio = true;
        vista.setFase(mensaje);
        vista.getBtnComenzar().setText("REINTENTAR");
        vista.getBtnComenzar().setVisible(true);
    }

    // Pausa el juego y su temporizador.
    private void detener() {
        jugando = false;
        relojJuego.stop();
    }

    // Cierra el flujo de recuperacion y vuelve a la pantalla anterior.
    private void salir() {
        relojSiguiente.stop();
        vista.ocultarCuentaAtras();
        vista.detenerConfeti();
        relojMostrar.stop();
        relojJuego.stop();
        vista.dispose();
        if (alSalir != null) alSalir.run();
    }

    // Refresca en pantalla los tiempos que quedan para jugar.
    private void mostrarTiempos() {
        vista.setTiempoTotal(modelo.getTiempoTotal(), ModeloPatrones.TIEMPO_TOTAL);
        vista.setTiempoCasilla(modelo.getTiempoEntreCasillas(), ModeloPatrones.TIEMPO_ENTRE_CASILLAS);
    }
}
