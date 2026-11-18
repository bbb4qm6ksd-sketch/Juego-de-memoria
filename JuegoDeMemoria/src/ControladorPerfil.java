import java.awt.image.BufferedImage;

// Coordina el perfil: obtiene estadísticas de los modelos, actualiza la vista
// y conecta sus botones con la navegación de la pantalla principal.
public class ControladorPerfil {

    private final VistaPantallaPrincipal menu;
    private final VistaPerfil vista = new VistaPerfil();
    private final ModeloPerfil perfil;
    private final ModeloTienda tienda;
    private final ModeloPremios ranking;
    private final String usuario;

    // Instala la vista en el menú, conecta la navegación y prepara el avatar
    // que se muestra junto al botón de perfil.
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

    // Actualiza los datos visibles y cambia la pantalla al perfil.
    private void mostrar() {
        refrescar();
        menu.mostrarPerfil();
    }

    // Vuelve a leer los modelos y entrega a la vista la cuenta y sus estadísticas.
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

    // Actualiza únicamente el avatar pequeño del menú al regresar a opciones.
    private void refrescarAvatarMenu() {
        menu.setAvatarPerfil(VistaTienda.componerAvatar(tienda.getAccesoriosEquipados()));
    }

    // Prepara el texto que indica cuántas partidas faltan para subir de rango.
    private String textoProgreso() {
        int faltan = perfil.getPartidasParaSiguienteRango();
        if (faltan < 0) return "Rango máximo alcanzado";
        return "Faltan " + faltan + (faltan == 1 ? " partida" : " partidas") + " para " + perfil.getSiguienteRango();
    }
}
