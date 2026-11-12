/** Controla la tienda y conecta su vista independiente con el modelo. */
public class ControladorTienda {
    private final VistaTienda vista;
    private final ModeloTienda modelo;
    private final VistaPantallaPrincipal principal;

    public ControladorTienda(VistaPantallaPrincipal principal, ModeloTienda modelo) {
        this.principal = principal;
        this.vista = new VistaTienda(principal);
        principal.instalarTienda(vista);
        this.modelo = modelo;
        cargarCatalogo();
        vista.setAlComprar(this::alComprar);
        vista.setAccesoriosEquipados(modelo.getAccesoriosEquipados());
        principal.getBtnTienda().addActionListener(e -> refrescarYMostrar());
        refrescar();
    }

    private void cargarCatalogo() {
        vista.limpiarTienda();
        for (Articulo a : modelo.getCatalogo()) {
            vista.agregarArticulo(a.getId(), categoria(a.getTipo()), a.getNombre(), a.getDescripcion(), a.getPrecio(), a.isUnico());
            if (a.isUnico() && modelo.yaComprado(a.getId())) vista.marcarComprado(a.getId());
        }
    }
    private void refrescar() { vista.setSaldo(modelo.getSaldo()); }
    private void refrescarYMostrar() { refrescar(); vista.mostrarTienda(); principal.mostrarTienda(); }

    private void alComprar(String id) {
        Articulo a = modelo.buscar(id);
        ModeloTienda.Resultado r = modelo.comprar(id);
        switch (r) {
            case EXITO:
                refrescar();
                if (a.isUnico()) { vista.marcarComprado(id); vista.setAccesoriosEquipados(modelo.getAccesoriosEquipados()); vista.mostrarMensajeTienda("¡" + a.getNombre() + " se agregó y equipó en tu avatar!", false); }
                else if (a.getTipo() == Articulo.Tipo.TIEMPO) vista.mostrarMensajeTienda("¡Compra exitosa! Tiempo extra acumulado: " + modelo.getSegundosExtra() + " s", false);
                else vista.mostrarMensajeTienda("¡Compra exitosa! Puntos extra acumulados: " + modelo.getPuntosExtra(), false);
                break;
            case SALDO_INSUFICIENTE: vista.mostrarMensajeTienda("Saldo insuficiente: te faltan " + (a.getPrecio()-modelo.getSaldo()) + " estrellas", true); break;
            case YA_COMPRADO: vista.mostrarMensajeTienda("Ya tienes ese avatar", true); break;
            default: vista.mostrarMensajeTienda("Ese artículo ya no existe", true);
        }
    }
    private static VistaTienda.CategoriaTienda categoria(Articulo.Tipo tipo) {
        switch (tipo) {
            case ACCESORIO: return VistaTienda.CategoriaTienda.ACCESORIOS;
            case TIEMPO: return VistaTienda.CategoriaTienda.TIEMPO;
            default: return VistaTienda.CategoriaTienda.PUNTOS;
        }
    }
    public ModeloTienda getModelo() { return modelo; }
}
