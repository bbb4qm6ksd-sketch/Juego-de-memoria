
import java.util.List;
import javax.swing.SwingWorker;

public class ControladorPremios {

    // Referencias a la interfaz y al modelo que proporciona los datos.
    private final VistaPremios vista;
    private final ModeloPremios modelo;


    // Recibe y guarda los componentes que este controlador coordina.
    public ControladorPremios(VistaPremios vista, ModeloPremios modelo) {
        this.vista = vista;
        this.modelo = modelo;
    }

    // Actualiza la tabla cuando se abre: recarga los datos y solicita el tipo de cambio.
    public void actualizar() {

        // Carga los datos recientes y muestra que la consulta del tipo de cambio está en curso.
        modelo.recargar();
        vista.setTipoCambio("consultando...");
        // La consulta de red se ejecuta en segundo plano para mantener activa la interfaz.
        new SwingWorker<double[], Void>() {          // {valor, esActual (1 o 0)}
            @Override

            // Obtiene el tipo de cambio y lo guarda; si falla, usa el último valor disponible.
            protected double[] doInBackground() {
                try {
                    double valor = TipoCambio.obtener();
                    modelo.guardarCambio(valor);
                    // El segundo elemento indica que se obtuvo un valor actualizado.
                    return new double[]{valor, 1};
                } catch (Exception e) {
                    System.err.println("Tipo de cambio: " + e.getMessage());
                    // El indicador 0 señala que se recurrió al valor guardado anteriormente.
                    return new double[]{modelo.ultimoCambio(), 0};
                }
            }

            @Override
            protected void done() {
                try {
                    double[] r = get();
                    // Sin un tipo de cambio válido, muestra un error y no calcula la tabla.
                    if (r[0] <= 0) {
                        vista.mostrarErrorCambio("No se pudo obtener el tipo de cambio.");
                        return;
                    }
                    // Calcula las filas de premios con el tipo de cambio obtenido o guardado.
                    List<ModeloPremios.Fila> filas = modelo.getTabla(r[0]);
                    vista.mostrarTop(filas, r[0], r[1] == 1);
                    // Asigna a cada fila el avatar equipado por el jugador correspondiente.
                    for (int i = 0; i < filas.size(); i++) {
                        vista.setAvatar(i, ModeloTienda.equipadosDe(filas.get(i).jugador));
                    }
                } catch (Exception e) {
                    // Informa a la vista si hubo un problema al recuperar o mostrar los resultados.
                    vista.mostrarErrorCambio("Error al calcular los premios.");
                }
            }
        }.execute();
    }
}
