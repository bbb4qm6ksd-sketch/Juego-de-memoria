import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Carta del tablero. Solo se encarga de DIBUJARSE segun su estado visual.
 * Que estado tiene en cada momento lo decide el controlador.
 */
public class Carta extends JButton {

    public enum Estado { OCULTA, VOLTEADA, ENCONTRADA }

    private Estado estado = Estado.OCULTA;
    private String nombre = "";
    private Icon imagen;

    public Carta() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setEstado(Estado estado) { this.estado = estado; repaint(); }
    public Estado getEstado()            { return estado; }
    public void setNombre(String nombre) { this.nombre = nombre; repaint(); }
    public void setImagen(Icon imagen)   { this.imagen = imagen; repaint(); }

    private static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean oculta = estado == Estado.OCULTA;

        RoundRectangle2D forma = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 18, 18);
        g2.setPaint(new GradientPaint(0, 0, oculta ? new Color(0x111A33) : new Color(0x141D3A),
                w, h, oculta ? new Color(0x0A0F20) : new Color(0x0C1226)));
        g2.fill(forma);

        if (estado == Estado.ENCONTRADA) {            // resplandor dorado
            for (int i = 3; i >= 1; i--) {
                g2.setColor(alfa(Tema.ESTRELLA, 28 / i));
                g2.setStroke(new BasicStroke(1 + i * 2.5f));
                g2.draw(forma);
            }
        }
        Color borde = estado == Estado.ENCONTRADA ? Tema.ESTRELLA
                : (oculta && getModel().isRollover() ? Tema.HIELO : Tema.LINEA);
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(forma);

        if (oculta) dibujarEstrella(g2, w / 2f, h / 2f);
        else        dibujarContenido(g2, w, h);
        g2.dispose();
    }

    private void dibujarEstrella(Graphics2D g2, float cx, float cy) {
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), 14f,
                new float[]{0f, 1f}, new Color[]{alfa(Tema.HIELO, 110), alfa(Tema.HIELO, 0)}));
        g2.fillOval((int) (cx - 14), (int) (cy - 14), 28, 28);
        g2.setColor(Tema.HIELO);
        g2.fillOval((int) (cx - 2.5f), (int) (cy - 2.5f), 5, 5);
    }

    private void dibujarContenido(Graphics2D g2, int w, int h) {
        g2.setFont(Tema.serif(Font.ITALIC, Math.max(12, h / 7)));
        FontMetrics fm = g2.getFontMetrics();
        int altoTexto = fm.getHeight() + 10;

        if (imagen != null) {
            int x = (w - imagen.getIconWidth()) / 2;
            int y = (h - altoTexto - imagen.getIconHeight()) / 2;
            imagen.paintIcon(this, g2, x, y);
        }
        g2.setColor(estado == Estado.ENCONTRADA ? Tema.ESTRELLA : Tema.TEXTO);
        g2.drawString(nombre, (w - fm.stringWidth(nombre)) / 2, h - 14);
    }
}