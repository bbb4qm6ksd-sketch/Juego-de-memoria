import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;


public class VistaPremios extends JFrame {

    public static final int PUESTOS = 5;
    private static final double[] PESOS = {0.12, 0.44, 0.18, 0.26};
    private final JLabel[][] celdas = new JLabel[PUESTOS][4];
    private final JLabel lblCambio = new JLabel("Tipo de cambio: —");
    private final JButton btnVolver = new VistaTablero.BotonPill("VOLVER", true);

    public VistaPremios() {
        super("Constela - Premios");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Tema fondo = new Tema(110, new Color(0x141F47), Tema.FONDO);
        fondo.setLayout(new BorderLayout(0, 28));
        fondo.setBorder(new EmptyBorder(34, 48, 30, 48));
        setContentPane(fondo);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        setSize(840, 660);
        setMinimumSize(new Dimension(700, 600));
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("CLASIFICACIÓN");
        eyebrow.setFont(Tema.sans(Font.BOLD, 11));
        eyebrow.setForeground(Tema.HIELO);
        JLabel titulo = new JLabel("<html>Mejores <i style='color:#F6DCA2'>jugadores</i></html>");
        titulo.setFont(Tema.serif(Font.PLAIN, 46));
        titulo.setForeground(Tema.TEXTO);
        JLabel formula = new JLabel("Premio = 1 / intentos × 100 × tipo de cambio");
        formula.setFont(Tema.sans(Font.PLAIN, 13));
        formula.setForeground(Tema.SECUNDARIO);
        lblCambio.setFont(Tema.sans(Font.BOLD, 12));
        lblCambio.setForeground(Tema.ESTRELLA);

        for (JComponent c : new JComponent[]{eyebrow, titulo, formula, lblCambio}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        p.add(eyebrow);
        p.add(Box.createVerticalStrut(6));
        p.add(titulo);
        p.add(Box.createVerticalStrut(10));
        p.add(formula);
        p.add(Box.createVerticalStrut(4));
        p.add(lblCambio);
        return p;
    }

    private JPanel crearTabla() {
        JPanel tabla = new JPanel(new GridLayout(PUESTOS + 1, 1, 0, 10));
        tabla.setOpaque(false);

        JLabel[] cab = {
                celda("POS.", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.LEFT),
                celda("JUGADOR", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.LEFT),
                celda("INTENTOS", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.CENTER),
                celda("PREMIO", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.RIGHT)
        };
        tabla.add(fila(cab, null));

        for (int i = 0; i < PUESTOS; i++) {
            boolean oro = i == 0;
            Color acento = oro ? Tema.ESTRELLA : Tema.TEXTO;
            celdas[i][0] = celda(String.valueOf(i + 1), Tema.serif(Font.PLAIN, 28), oro ? Tema.ESTRELLA : Tema.HIELO, SwingConstants.LEFT);
            celdas[i][1] = celda("—", Tema.sans(Font.PLAIN, 18), Tema.TEXTO, SwingConstants.LEFT);
            celdas[i][2] = celda("—", Tema.sans(Font.PLAIN, 18), Tema.TEXTO, SwingConstants.CENTER);
            celdas[i][3] = celda("—", Tema.serif(Font.PLAIN, 26), acento, SwingConstants.RIGHT);
            tabla.add(fila(celdas[i], oro ? Tema.ESTRELLA : Tema.LINEA));
        }
        return tabla;
    }

    private static JLabel celda(String texto, Font fuente, Color color, int alineacion) {
        JLabel l = new JLabel(texto, alineacion);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    private static JPanel fila(JLabel[] columnas, Color borde) {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        if (borde != null) {
            fila.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(borde, 1, true), new EmptyBorder(0, 22, 0, 22)));
        } else {
            fila.setBorder(new EmptyBorder(0, 23, 0, 23));
        }
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        for (int i = 0; i < columnas.length; i++) {
            c.gridx = i;
            c.weightx = PESOS[i];
            fila.add(columnas[i], c);
        }
        return fila;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        btnVolver.setPreferredSize(new Dimension(170, 42));
        pie.add(btnVolver, BorderLayout.EAST);
        return pie;
    }


    public JButton getBtnVolver() { return btnVolver; }

    public void setTipoCambio(String texto) { lblCambio.setText("Tipo de cambio: " + texto); }

    public void setPuesto(int posicion, String jugador, int intentos, String premio) {
        int i = posicion - 1;
        celdas[i][1].setText(jugador);
        celdas[i][2].setText(String.valueOf(intentos));
        celdas[i][3].setText(premio);
    }

    public void limpiarPuestos() {
        for (int i = 0; i < PUESTOS; i++) {
            for (int c = 1; c < 4; c++) celdas[i][c].setText("—");
        }
    }
}