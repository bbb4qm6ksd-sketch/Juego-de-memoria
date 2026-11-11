import javax.swing.Timer;


public class ControladorPatrones {

    private final VistaPatrones vista;
    private final ModeloPatrones modelo = new ModeloPatrones();
    private final Runnable alSalir;

    private boolean jugando = false;
    private boolean perdio = false;
    private int paso = 0;
    private VistaPatrones.Casilla encendida = null;

    private final Timer relojMostrar = new Timer(500, e -> mostrarSiguienteCasilla());
    private final Timer relojJuego = new Timer(100, e -> pasarTiempo());
    private final Timer relojSiguiente = new Timer(50, e -> pasarCuentaAtras());

    public ControladorPatrones(VistaPatrones vista) {
        this(vista, null);
    }

    public ControladorPatrones(VistaPatrones vista, Runnable alSalir) {
        this.vista = vista;
        this.alSalir = alSalir;

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

    private void comenzar() {
        relojSiguiente.stop();
        vista.ocultarCuentaAtras();
        vista.detenerConfeti();
        if (perdio) {
            modelo.nuevoJuego();
            perdio = false;
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

    private void pasarTiempo() {
        modelo.restarTiempo(0.1);
        mostrarTiempos();
        if (modelo.tiempoAgotado()) terminarPorError("Se acabó el tiempo. Pulsa REINTENTAR para volver a empezar");
    }

    private void pasarCuentaAtras() {
        modelo.restarCuentaAtras(0.05);
        if (modelo.cuentaAtrasTerminada()) {
            relojSiguiente.stop();
            comenzar();                    // comenzar() ya oculta la capa
        } else {
            vista.mostrarCuentaAtras(modelo.getTiempoSiguienteRonda());
        }
    }

    private void ganarRonda() {
        detener();
        modelo.siguienteRonda();
        vista.setNivel(modelo.getNivel());
        vista.setRonda(modelo.getRonda());
        vista.setFase("¡Correcto!");
        vista.lanzarConfeti();

        modelo.iniciarCuentaAtras();
        vista.mostrarCuentaAtras(modelo.getTiempoSiguienteRonda());
        relojSiguiente.start();
    }

    private void terminarPorError(String mensaje) {
        detener();
        perdio = true;
        vista.setFase(mensaje);
        vista.getBtnComenzar().setText("REINTENTAR");
        vista.getBtnComenzar().setVisible(true);
    }

    private void detener() {
        jugando = false;
        relojJuego.stop();
    }

    private void salir() {
        relojSiguiente.stop();
        vista.ocultarCuentaAtras();
        vista.detenerConfeti();
        relojMostrar.stop();
        relojJuego.stop();
        vista.dispose();
        if (alSalir != null) alSalir.run();
    }

    private void mostrarTiempos() {
        vista.setTiempoTotal(modelo.getTiempoTotal(), ModeloPatrones.TIEMPO_TOTAL);
        vista.setTiempoCasilla(modelo.getTiempoEntreCasillas(), ModeloPatrones.TIEMPO_ENTRE_CASILLAS);
    }
}