/**
 * Artículo que se puede comprar en la tienda de AstroMatch.
 * Es una clase inmutable: una vez creado no cambia.
 */
public class Articulo {

    /** ACCESORIO = cosmético único; TIEMPO y PUNTOS = consumibles que se pueden comprar varias veces. */
    public enum Tipo { ACCESORIO, TIEMPO, PUNTOS }

    private final String id;
    private final Tipo tipo;
    private final String nombre;
    private final String descripcion;
    private final int precio;
    private final int valor;   // TIEMPO: segundos extra | PUNTOS: puntos extra | ACCESORIO: no se usa (0)

    public Articulo(String id, Tipo tipo, String nombre, String descripcion, int precio, int valor) {
        if (precio < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.valor = valor;
    }

    public String getId()          { return id; }
    public Tipo getTipo()          { return tipo; }
    public String getNombre()      { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPrecio()         { return precio; }
    public int getValor()          { return valor; }

    /** Los accesorios se compran una sola vez; los consumibles, todas las veces que se quiera. */
    public boolean isUnico()       { return tipo == Tipo.ACCESORIO; }

    @Override public String toString() {
        return nombre + " (" + tipo + ", " + precio + " estrellas)";
    }
}