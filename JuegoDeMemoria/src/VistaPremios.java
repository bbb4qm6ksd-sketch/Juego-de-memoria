import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Set;

public class VistaPremios extends JFrame {

    public static final int PUESTOS = 5;
    private static final double[] PESOS = {0.10, 0.40, 0.18, 0.32};
    private static final int TAM_AVATAR = 40;
    private final JLabel[][] celdas = new JLabel[PUESTOS][4];
    private final JLabel lblCambio = new JLabel("Tipo de cambio: —");
    private final JLabel lblFormula = new JLabel("Premio = 1 / intentos × 100 × tipo de cambio");
    private final JButton btnVolver = new VistaTablero.BotonPill("VOLVER", true);

    public VistaPremios() {
        super("AstroMatch - Premios");
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
        eyebrow.setFont(fuentes.mono(Font.BOLD, 11));
        eyebrow.setForeground(Tema.HIELO);
        JLabel titulo = new JLabel("<html>Mejores <i style='color:#F6DCA2'>jugadores</i></html>");
        titulo.setFont(fuentes.kiwiSoda(Font.PLAIN, 50));
        titulo.setForeground(Tema.TEXTO);
        lblFormula.setFont(Tema.sans(Font.PLAIN, 13));
        lblFormula.setForeground(Tema.SECUNDARIO);
        lblCambio.setFont(Tema.sans(Font.BOLD, 12));
        lblCambio.setForeground(Tema.ESTRELLA);

        for (JComponent c : new JComponent[]{eyebrow, titulo, lblFormula, lblCambio}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        p.add(eyebrow);
        p.add(Box.createVerticalStrut(6));
        p.add(titulo);
        p.add(Box.createVerticalStrut(10));
        p.add(lblFormula);
        p.add(Box.createVerticalStrut(4));
        p.add(lblCambio);
        return p;
    }

    private JPanel crearTabla() {
        JPanel tabla = new JPanel(new BorderLayout(0, 8));
        tabla.setOpaque(false);

        JLabel[] cab = {
                celda("POS.", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.LEFT),
                celda("JUGADOR", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.LEFT),
                celda("INTENTOS", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.CENTER),
                celda("PREMIO", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO, SwingConstants.RIGHT)
        };
        JPanel filaCab = fila(cab, null);
        filaCab.setPreferredSize(new Dimension(0, 24));
        tabla.add(filaCab, BorderLayout.NORTH);

        JPanel filas = new JPanel(new GridLayout(PUESTOS, 1, 0, 10));
        filas.setOpaque(false);
        for (int i = 0; i < PUESTOS; i++) {
            boolean oro = i == 0;
            Color acento = oro ? Tema.ESTRELLA : Tema.TEXTO;
            celdas[i][0] = celda(String.valueOf(i + 1), Tema.serif(Font.PLAIN, 28), oro ? Tema.ESTRELLA : Tema.HIELO, SwingConstants.LEFT);
            celdas[i][1] = celda("-", fuentes.mono(Font.PLAIN, 18), Tema.TEXTO, SwingConstants.LEFT);
            celdas[i][1].setIconTextGap(12);
            celdas[i][2] = celda("—", Tema.sans(Font.PLAIN, 18), Tema.TEXTO, SwingConstants.CENTER);
            celdas[i][3] = celda("—", Tema.serif(Font.PLAIN, 26), acento, SwingConstants.RIGHT);
            filas.add(fila(celdas[i], oro ? Tema.ESTRELLA : Tema.LINEA));
        }
        tabla.add(filas, BorderLayout.CENTER);
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
            columnas[i].setPreferredSize(new Dimension(0, columnas[i].getPreferredSize().height)); // NUEVO
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

    public void setAvatar(int puesto, Set<String> accesorios) {
        if (puesto < 0 || puesto >= PUESTOS) return;
        BufferedImage avatar = VistaTienda.componerAvatar(accesorios);
        if (avatar == null) {
            celdas[puesto][1].setIcon(null);
            return;
        }
        int t = TAM_AVATAR;
        BufferedImage redonda = new BufferedImage(t, t, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = redonda.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setClip(new Ellipse2D.Float(0, 0, t, t));
        g2.setColor(new Color(0x0A1028));
        g2.fillRect(0, 0, t, t);
        double escala = Math.min((double) t / avatar.getWidth(), (double) t / avatar.getHeight());
        int w = (int) (avatar.getWidth() * escala);
        int h = (int) (avatar.getHeight() * escala);
        g2.drawImage(avatar, (t - w) / 2, (t - h) / 2, w, h, null);
        g2.dispose();
        celdas[puesto][1].setIcon(new ImageIcon(redonda));
    }

    public void setTipoCambio(String texto) { lblCambio.setText("Tipo de cambio: " + texto); }

    public void mostrarTop(List<ModeloPremios.Fila> filas, double cambio, boolean cambioActual) {
        setTipoCambio(String.format("₡%.2f", cambio) + (cambioActual ? " (BCCR, actual)" : " (último guardado, sin conexión)"));

        NumberFormat moneda = NumberFormat.getNumberInstance(Locale.US);
        moneda.setMaximumFractionDigits(2);
        moneda.setMinimumFractionDigits(2);

        for (int i = 0; i < PUESTOS; i++) {
            if (i < filas.size()) {
                ModeloPremios.Fila f = filas.get(i);
                celdas[i][1].setText(f.jugador);
                celdas[i][2].setText(String.valueOf(f.intentos));
                celdas[i][3].setText("₡" + moneda.format(f.premio));
            } else {
                celdas[i][1].setText("-");
                celdas[i][2].setText("—");
                celdas[i][3].setText("—");
            }
        }
    }

    public void mostrarErrorCambio(String mensaje) {
        lblCambio.setText(mensaje);
        for (int i = 0; i < PUESTOS; i++) {
            celdas[i][1].setText("-");
            celdas[i][2].setText("—");
            celdas[i][3].setText("—");
        }
    }


}
