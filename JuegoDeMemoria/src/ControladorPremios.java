import java.util.List;
import javax.swing.SwingWorker;

public class ControladorPremios {

    private final VistaPremios vista;
    private final ModeloPremios modelo;


    public ControladorPremios(VistaPremios vista, ModeloPremios modelo) {
        this.vista = vista;
        this.modelo = modelo;
    }

    //se actualiza cada vez que se abre la tabla
    public void actualizar() {
        modelo.recargar();
        vista.setTipoCambio("consultando...");
        new SwingWorker<double[], Void>() {          // {valor, esActual (1 o 0)}
            @Override
            protected double[] doInBackground() {
                try {
                    double valor = TipoCambio.obtener();
                    modelo.guardarCambio(valor);
                    return new double[]{valor, 1};
                } catch (Exception e) {
                    System.err.println("Tipo de cambio: " + e.getMessage());
                    return new double[]{modelo.ultimoCambio(), 0};
                }
            }

            @Override
            protected void done() {
                try {
                    double[] r = get();
                    if (r[0] <= 0) {
                        vista.mostrarErrorCambio("No se pudo obtener el tipo de cambio.");
                        return;
                    }
                    List<ModeloPremios.Fila> filas = modelo.getTabla(r[0]);
                    vista.mostrarTop(filas, r[0], r[1] == 1);
                } catch (Exception e) {
                    vista.mostrarErrorCambio("Error al calcular los premios.");
                }
            }
        }.execute();
    }
}