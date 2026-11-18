import java.awt.Container;

public class ControladorLogin {

    // Conecta las acciones de la pantalla de login con el resto del juego.

    private final VistaLogin vista;
    private final Container contenidoLogin;
    private final String tituloLogin;

    public ControladorLogin(VistaLogin vista) {
        // Guarda la vista para poder cambiar su contenido y responder a los botones.
        this.vista = vista;
        this.contenidoLogin = vista.getContentPane();
        this.tituloLogin = vista.getTitle();
        vista.getRootPane().setDefaultButton(vista.getBtnIngresar());
        vista.getBtnIngresar().addActionListener(e -> ingresar());
        vista.getBtnCrearCuenta().addActionListener(e -> abrirRegistro());
        vista.getBtnOlvide().addActionListener(e -> abrirRecuperacion());
        FaceID.instalarEnLogin(vista, this::abrirMenu);   // botón "ENTRAR CON FACE ID"
    }

    // Revisa los datos ingresados y, si son correctos, abre el menú principal.
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
        FaceID.ofrecerActivar(vista, usuario);   // si aún no tiene Face ID, ofrece activarlo
        abrirMenu(usuario);
    }

    // Muestra el formulario para crear una cuenta.
    private void abrirRegistro() {
        VistaRegistro registro = new VistaRegistro(vista);
        vista.setContentPane(registro.getContentPane());
        vista.setTitle("AstroMatch - Crear cuenta");
        new ControladorRegistro(registro, vista, () -> restaurarLogin(registro));
        actualizarVentana();
    }

    // Abre la pantalla para recuperar la contraseña.
    private void abrirRecuperacion() {
        VistaRecuperarContraseña recuperar = new VistaRecuperarContraseña();
        vista.setContentPane(recuperar);
        vista.setTitle("AstroMatch - Restablecer contraseña");
        new ControladorRecuperarContraseña(recuperar, new ModeloRecuperarContraseña(),
                this::restaurarLogin);
        actualizarVentana();
    }

    // Vuelve a mostrar la pantalla de login después de cerrar otra pantalla.
    private void restaurarLogin() {
        vista.setContentPane(contenidoLogin);
        vista.setTitle(tituloLogin);
        actualizarVentana();
    }

    private void restaurarLogin(java.awt.Window vistaSecundaria) {
        vistaSecundaria.dispose();
        restaurarLogin();
    }

    // Refresca la ventana para que se vean los cambios.
    private void actualizarVentana() {
        vista.revalidate();
        vista.repaint();
        vista.setVisible(true);
    }
    // Prepara las pantallas y controles del usuario que inició sesión.
    private void abrirMenu(String usuario) {
        VistaPantallaPrincipal menu = new VistaPantallaPrincipal(usuario);
        Container contenidoMenu = menu.getContentPane();
        String tituloMenu = menu.getTitle();
        VistaTablero tablero = new VistaTablero();
        VistaPremios premios = new VistaPremios();
        ModeloPremios modeloPremios = new ModeloPremios();
        ControladorPremios controladorPremios = new ControladorPremios(premios, modeloPremios);

        ModeloTienda modeloTienda = new ModeloTienda(usuario);
        new ControladorTienda(menu, modeloTienda);
        ModeloPerfil modeloPerfil = new ModeloPerfil(usuario);
        new ControladorPerfil(menu, modeloPerfil, modeloTienda, modeloPremios);

        ControladorClasico clasico = new ControladorClasico(tablero, usuario, "Jugador 2",
                modeloTienda, modeloPremios, modeloPerfil);

        menu.getBtnClasico().addActionListener(e -> {
            Sonidos.cambiarMusica(Sonidos.Pista.JUEGO);
            menu.getRootPane().getGlassPane().setVisible(false);
            menu.setContentPane(tablero.getContentPane());
            menu.setTitle("AstroMatch - Modo clásico");
            menu.revalidate();
            menu.repaint();
            menu.setVisible(true);
            String jugador2 = tablero.solicitarNombreJugador2("Jugador 2");
            if (jugador2 == null || jugador2.trim().isEmpty()) {
                menu.setContentPane(contenidoMenu);
                menu.setTitle(tituloMenu);
                menu.getRootPane().getGlassPane().setVisible(true);
                menu.revalidate();
                menu.repaint();
                Sonidos.cambiarMusica(Sonidos.Pista.MENU);
                return;
            }
            clasico.setNombreJugador2(jugador2);
            tablero.mostrarSelectorNiveles();
        });

        // Cada modo ocupa el mismo marco principal; solo se cambia el contenido.
        menu.getBtnPatrones().addActionListener(e -> {
            Sonidos.cambiarMusica(Sonidos.Pista.JUEGO);
            VistaPatrones patrones = new VistaPatrones();
            new ControladorPatrones(patrones, () -> {
                Sonidos.cambiarMusica(Sonidos.Pista.MENU);   // al salir, vuelve la música del menú
                menu.setContentPane(contenidoMenu);
                menu.setTitle(tituloMenu);
                menu.getRootPane().getGlassPane().setVisible(true);
                menu.revalidate();
                menu.repaint();
            }, modeloTienda, modeloPerfil);
            menu.getRootPane().getGlassPane().setVisible(false);
            menu.setContentPane(patrones.getContentPane());
            menu.setTitle("AstroMatch - Modo patrones");
            menu.revalidate();
            menu.repaint();
        });
        menu.getBtnPremios().addActionListener(e -> {
            menu.getRootPane().getGlassPane().setVisible(false);
            menu.setContentPane(premios.getContentPane());
            menu.setTitle("AstroMatch - Premios");
            menu.revalidate();
            menu.repaint();
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
            menu.setContentPane(contenidoMenu);
            menu.setTitle(tituloMenu);
            menu.getRootPane().getGlassPane().setVisible(true);
            menu.revalidate();
            menu.repaint();
            menu.setVisible(true);
        });
        premios.getBtnVolver().addActionListener(e -> {
            menu.setContentPane(contenidoMenu);
            menu.setTitle(tituloMenu);
            menu.getRootPane().getGlassPane().setVisible(true);
            menu.revalidate();
            menu.repaint();
        });
        menu.getBtnSonido().setText(Sonidos.isSilenciado() ? "SONIDO: OFF" : "SONIDO: ON");
        menu.getBtnSonido().addActionListener(e -> {
            Sonidos.setSilenciado(!Sonidos.isSilenciado());
            menu.getBtnSonido().setText(Sonidos.isSilenciado() ? "SONIDO: OFF" : "SONIDO: ON");
        });

        vista.setVisible(false);
        menu.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        menu.setVisible(true);
    }
}


