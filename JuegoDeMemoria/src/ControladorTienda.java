
// Conecta la tienda con el catálogo y actualiza la interfaz cuando se compra o equipa algo.
public class ControladorTienda {
    private final VistaTienda vista;
    private final ModeloTienda modelo;
    private final VistaPantallaPrincipal principal;

    // Conecta el catalogo con el saldo, las compras y los accesorios.
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

    // Carga en pantalla los articulos definidos por el modelo.
    private void cargarCatalogo() {
        vista.limpiarTienda();
        for (Articulo a : modelo.getCatalogo()) {
            vista.agregarArticulo(a.getId(), categoria(a.getTipo()), a.getNombre(), a.getDescripcion(), a.getPrecio(), a.isUnico(), a.getValor());
            if (a.isUnico() && modelo.yaComprado(a.getId())) vista.marcarComprado(a.getId());
        }
    }
    // Actualiza el saldo que aparece en la tienda.
    private void refrescar() { vista.setSaldo(modelo.getSaldo()); }
    // Actualiza los datos y abre la tienda.
    private void refrescarYMostrar() { refrescar(); vista.mostrarTienda(); principal.mostrarTienda(); }

    // Procesa la compra o el equipamiento y muestra el resultado.
    private void alComprar(String id) {
        Articulo a = modelo.buscar(id);
        if (a == null) { vista.mostrarMensajeTienda("Ese artículo ya no existe", true); return; }
        if (a.getTipo() == Articulo.Tipo.ACCESORIO) {
            if (!modelo.yaComprado(id)) {
                ModeloTienda.Resultado resultado = modelo.comprar(id);
                if (resultado == ModeloTienda.Resultado.SALDO_INSUFICIENTE) {
                    vista.mostrarMensajeTienda("Saldo insuficiente: te faltan " + (a.getPrecio()-modelo.getSaldo()) + " estrellas", true);
                    return;
                }
            } else if (modelo.getAccesoriosEquipados().contains(id)) {
                modelo.quitarAccesorio(id);
            } else {
                modelo.equiparAccesorio(id);
            }
            refrescar();
            vista.marcarComprado(id);
            vista.setAccesoriosEquipados(modelo.getAccesoriosEquipados());
            vista.mostrarMensajeTienda(modelo.getAccesoriosEquipados().contains(id)
                    ? "¡" + a.getNombre() + " equipado en tu astronauta!"
                    : "Accesorio retirado del astronauta", false);
            return;
        }
        ModeloTienda.Resultado r = modelo.comprar(id);
        switch (r) {
            case EXITO:
                refrescar();
                if (a.getTipo() == Articulo.Tipo.TIEMPO) vista.mostrarMensajeTienda("¡Compra exitosa! Tiempo extra acumulado: " + modelo.getSegundosExtra() + " s", false);
                else vista.mostrarMensajeTienda("¡Compra exitosa! Puntos extra acumulados: " + modelo.getPuntosExtra(), false);
                break;
            case SALDO_INSUFICIENTE: vista.mostrarMensajeTienda("Saldo insuficiente: te faltan " + (a.getPrecio()-modelo.getSaldo()) + " estrellas", true); break;
            case YA_COMPRADO: vista.mostrarMensajeTienda("Ya tienes ese artículo", true); break;
            default: vista.mostrarMensajeTienda("Ese artículo ya no existe", true);
        }
    }
    // Convierte la categoria del modelo en la categoria de la interfaz.
    private static VistaTienda.CategoriaTienda categoria(Articulo.Tipo tipo) {
        switch (tipo) {
            case ACCESORIO: return VistaTienda.CategoriaTienda.ACCESORIOS;
            case TIEMPO: return VistaTienda.CategoriaTienda.TIEMPO;
            default: return VistaTienda.CategoriaTienda.PUNTOS;
        }
    }
    // Devuelve el modelo de tienda asociado a este controlador.
    public ModeloTienda getModelo() { return modelo; }
}

