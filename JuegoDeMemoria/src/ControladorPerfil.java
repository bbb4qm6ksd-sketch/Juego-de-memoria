import java.awt.image.BufferedImage;

/**
 * Conecta la pantalla de perfil con los datos del jugador.
 * Se instala solo en la pantalla principal: crea el panel, lo agrega y escucha el boton PERFIL.
 */
public class ControladorPerfil {

    private final VistaPantallaPrincipal menu;
    private final VistaPerfil vista = new VistaPerfil();
    private final ModeloPerfil perfil;
    private final ModeloTienda tienda;
    private final ModeloPremios ranking;
    private final String usuario;

    public ControladorPerfil(VistaPantallaPrincipal menu, ModeloPerfil perfil,
                             ModeloTienda tienda, ModeloPremios ranking) {
        this.menu = menu;
        this.perfil = perfil;
        this.tienda = tienda;
        this.ranking = ranking;
        this.usuario = perfil.getUsuario();

        menu.instalarPerfil(vista);
        menu.getBtnPerfil().addActionListener(e -> mostrar());
        vista.getBtnVolver().addActionListener(e -> menu.mostrarOpciones());

        // el circulo del menu se actualiza cada vez que se vuelve a las opciones (por si cambio su astronauta en la tienda)
        menu.setAlMostrarOpciones(this::refrescarAvatarMenu);
        refrescarAvatarMenu();
    }

    private void mostrar() {
        refrescar();
        menu.mostrarPerfil();
    }

    /** Vuelve a leer todos los datos y los pinta en la vista. */
    public void refrescar() {
        ranking.recargar();

        BufferedImage avatar = VistaTienda.componerAvatar(tienda.getAccesoriosEquipados());
        vista.setAvatar(avatar, usuario);
        menu.setAvatarPerfil(avatar);

        vista.setCuenta(usuario, Cuentas.correoDe(usuario), FaceID.tieneFaceID(usuario));
        vista.setRango(perfil.getRango(), textoProgreso());

        int totalAccesorios = 0;
        for (Articulo a : tienda.getCatalogo()) {
            if (a.getTipo() == Articulo.Tipo.ACCESORIO) totalAccesorios++;
        }

        vista.setEstrellas(tienda.getSaldo(), perfil.getEstrellasGanadas());
        vista.setPartidas(perfil.getPartidasTotales(), perfil.getPartidasClasico(), perfil.getPartidasPatrones());
        vista.setVictorias(perfil.getVictoriasClasico(), perfil.getPorcentajeVictorias(), perfil.getPartidasClasico());
        vista.setRecord(ranking.getMejorIntento(usuario));
        vista.setMejorRonda(perfil.getMejorRonda());
        vista.setAccesorios(tienda.getAccesoriosComprados().size(), totalAccesorios);
        vista.setHistorial(perfil.getHistorial(VistaPerfil.FILAS_HISTORIAL));
    }

    /** Solo el circulo del menu (es mas liviano que refrescar todo el perfil). */
    private void refrescarAvatarMenu() {
        menu.setAvatarPerfil(VistaTienda.componerAvatar(tienda.getAccesoriosEquipados()));
    }

    private String textoProgreso() {
        int faltan = perfil.getPartidasParaSiguienteRango();
        if (faltan < 0) return "Rango máximo alcanzado";
        return "Faltan " + faltan + (faltan == 1 ? " partida" : " partidas") + " para " + perfil.getSiguienteRango();
    }
}