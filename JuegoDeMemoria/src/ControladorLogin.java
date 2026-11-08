import javax.swing.JOptionPane;

public class ControladorLogin {

    private final VistaLogin vista;

    public ControladorLogin(VistaLogin vista) {
        this.vista = vista;
        vista.getRootPane().setDefaultButton(vista.getBtnIngresar());
        vista.getBtnIngresar().addActionListener(e -> ingresar());
        vista.getBtnCrearCuenta().addActionListener(e -> abrirRegistro());
        vista.getBtnOlvide().addActionListener(e -> abrirRecuperacion());
    }

    private void ingresar() {
        String usuarioOCorreo = vista.getUsuario();
        String clave = vista.getContrasena();

        if (usuarioOCorreo.isEmpty() || clave.isEmpty()) {
            Sonidos.reproducir(Sonidos.Efecto.ERROR);
            vista.mostrarError("Escribe tu usuario y contraseña.");
            return;
        }
        String usuario = Cuentas.validar(usuarioOCorreo, clave);
        if (usuario == null) {
            Sonidos.reproducir(Sonidos.Efecto.ERROR);
            vista.mostrarError("Usuario o contraseña incorrectos.");
            return;
        }
        vista.limpiarError();
        abrirMenu(usuario);
    }

    private void abrirRegistro() {
        VistaRegistro registro = new VistaRegistro(vista);
        new ControladorRegistro(registro);
        registro.setVisible(true);
    }

    private void abrirRecuperacion() {
        VistaRecuperarContraseña recuperar = new VistaRecuperarContraseña(vista);
        new ControladorRecuperarContraseña(recuperar, new ModeloRecuperarContraseña());
        recuperar.setVisible(true);
    }
    private void abrirMenu(String usuario) {
        VistaPantallaPrincipal menu = new VistaPantallaPrincipal(usuario);
        VistaTablero tablero = new VistaTablero();
        VistaPremios premios = new VistaPremios();
        ModeloPremios modeloPremios = new ModeloPremios();
        ControladorPremios controladorPremios = new ControladorPremios(premios, modeloPremios);

        ControladorClasico clasico = new ControladorClasico(tablero, usuario, "Jugador 2");

        menu.getBtnClasico().addActionListener(e -> {
            Sonidos.cambiarMusica(Sonidos.Pista.JUEGO);
            clasico.nuevaPartida();
            menu.setVisible(false);
            tablero.setVisible(true);
        });

        // Modo patrones: cada vez que se pulsa JUGAR se crea una vista nueva y se le conecta su controlador
        menu.getBtnPatrones().addActionListener(e -> {
            Sonidos.cambiarMusica(Sonidos.Pista.JUEGO);
            VistaPatrones patrones = new VistaPatrones();
            patrones.setJugador(usuario);
            new ControladorPatrones(patrones, () -> {
                Sonidos.cambiarMusica(Sonidos.Pista.MENU);   // al salir, vuelve la música del menú
                menu.setVisible(true);
            });
            menu.setVisible(false);
            patrones.setVisible(true);
        });
        menu.getBtnPremios().addActionListener(e -> {
            menu.setVisible(false);
            premios.setVisible(true);
            controladorPremios.actualizar();
        });
        menu.getBtnCerrarSesion().addActionListener(e -> {
            Sonidos.cambiarMusica(Sonidos.Pista.MENU);
            clasico.detener();
            menu.dispose();
            tablero.dispose();
            premios.dispose();
            vista.limpiarCampos();
            vista.setVisible(true);
        });
        tablero.getBtnSalir().addActionListener(e -> {
            Sonidos.cambiarMusica(Sonidos.Pista.MENU);
            clasico.detener();
            tablero.setVisible(false);
            menu.setVisible(true);
        });
        premios.getBtnVolver().addActionListener(e -> {
            premios.setVisible(false);
            menu.setVisible(true);
        });
        menu.getBtnSonido().setText(Sonidos.isSilenciado() ? "SONIDO: OFF" : "SONIDO: ON");
        menu.getBtnSonido().addActionListener(e -> {
            Sonidos.setSilenciado(!Sonidos.isSilenciado());
            menu.getBtnSonido().setText(Sonidos.isSilenciado() ? "SONIDO: OFF" : "SONIDO: ON");
        });

        vista.setVisible(false);
        menu.setVisible(true);
    }
}