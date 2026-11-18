//Articulos que se pueden comprar en la tienda
// Reúne los datos de un artículo para que la tienda pueda mostrarlo y procesar su compra.
public class Articulo {

    public enum Tipo { ACCESORIO, TIEMPO, PUNTOS } //categorías

    private final String id;
    private final Tipo tipo;
    private final String nombre;
    private final String descripcion;
    private final int precio;
    private final int valor;   //magnitud del efecto (segundos o puntos; 0 para accesorios)

    // Prepara el articulo con los datos que la tienda necesita para presentarlo.
    public Articulo(String id, Tipo tipo, String nombre, String descripcion, int precio, int valor) {
        // Validación: un artículo no puede tener un precio negativo
        if (precio < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.valor = valor;
    }

    // Devuelve el identificador que permite reconocer este articulo en el catalogo.
    public String getId()          { return id; }
    // Indica a que categoria pertenece el articulo.
    public Tipo getTipo()          { return tipo; }
    // Devuelve el nombre que vera la persona en la tienda.
    public String getNombre()      { return nombre; }
    // Devuelve el detalle que explica que ofrece el articulo.
    public String getDescripcion() { return descripcion; }
    // Indica cuantas estrellas cuesta el articulo.
    public int getPrecio()         { return precio; }
    // Devuelve el efecto o la cantidad que aporta el articulo.
    public int getValor()          { return valor; }


    // Indica si el articulo es un accesorio que se compra una sola vez.
    public boolean isUnico()       { return tipo == Tipo.ACCESORIO; }

    //cadena con el nombre, tipo y precio del artículo
    @Override public String toString() {
        return nombre + " (" + tipo + ", " + precio + " estrellas)";
    }
}