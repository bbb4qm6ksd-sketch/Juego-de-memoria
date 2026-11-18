import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Pantalla de perfil instalada dentro de la ventana principal.
// Solo dibuja los datos que recibe de ControladorPerfil.
public class VistaPerfil extends JPanel {

    // Número máximo de partidas recientes que se dibujan en pantalla.
    public static final int FILAS_HISTORIAL = 7;
    // Proporción del ancho asignada a cada columna de una fila del historial.
    private static final double[] PESOS = {0.15, 0.17, 0.15, 0.37, 0.16};
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM  HH:mm");

    private final JButton btnVolver = Tema.botonTexto("← MENÚ PRINCIPAL");

    // tarjeta del jugador (izquierda)
    private final Retrato retrato = new Retrato();
    private final JLabel lblUsuario = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblRango = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblProgreso = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblCorreo = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblFaceId = new JLabel(" ", SwingConstants.CENTER);

    // mosaicos de estadisticas (derecha, arriba)
    private final Mosaico mEstrellas = new Mosaico("ESTRELLAS", Tema.ESTRELLA);
    private final Mosaico mPartidas = new Mosaico("PARTIDAS", Tema.TEXTO);
    private final Mosaico mVictorias = new Mosaico("VICTORIAS", Tema.TEXTO);
    private final Mosaico mRecord = new Mosaico("MEJOR RÉCORD", Tema.HIELO);
    private final Mosaico mRonda = new Mosaico("MEJOR RONDA", Tema.HIELO);
    private final Mosaico mAccesorios = new Mosaico("ACCESORIOS", Tema.TEXTO);

    // historial (derecha, abajo)
    private final JPanel listaHistorial = new JPanel(new GridLayout(FILAS_HISTORIAL, 1, 0, 6));

    public VistaPerfil() {
        setOpaque(false);
        setLayout(new BorderLayout());

        Tema fondo = new Tema(170, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true);
        fondo.setViaje(true);
        fondo.setLayout(new BorderLayout(24, 18));
        fondo.setBorder(new EmptyBorder(22, 34, 24, 34));
        fondo.add(crearCabecera(), BorderLayout.NORTH);
        fondo.add(crearContenido(), BorderLayout.CENTER);
        add(fondo, BorderLayout.CENTER);
    }

    // ---------- Construccion ----------

    // Construye el título y el botón que permite regresar al menú de opciones.
    private JPanel crearCabecera() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel eyebrow = etiqueta("ASTROMATCH  *  OBSERVATORIO", fuentes.mono(Font.BOLD, 11), Tema.HIELO);
        JLabel titulo = etiqueta("<html>Tu <i style='color:#F6DCA2'>perfil</i></html>",
                fuentes.kiwiSoda(Font.PLAIN, 44), Tema.TEXTO);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(eyebrow);
        textos.add(Box.createVerticalStrut(4));
        textos.add(titulo);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(btnVolver);

        p.add(textos, BorderLayout.WEST);
        p.add(derecha, BorderLayout.EAST);
        return p;
    }

    // Distribuye la tarjeta del jugador y el área de estadísticas e historial.
    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout(24, 0));
        contenido.setOpaque(false);
        contenido.add(crearTarjetaJugador(), BorderLayout.WEST);
        contenido.add(crearDerecha(), BorderLayout.CENTER);
        return contenido;
    }

    // Presenta el avatar, nombre, rango, progreso, correo y estado de Face ID.
    private JPanel crearTarjetaJugador() {
        PanelRedondo tarjeta = new PanelRedondo(24);
        tarjeta.setLayout(new BorderLayout(0, 12));
        tarjeta.setBorder(new EmptyBorder(18, 22, 20, 22));
        tarjeta.setPreferredSize(new Dimension(330, 100));

        JLabel cab = etiqueta("TU ASTRONAUTA", fuentes.mono(Font.BOLD, 12), Tema.HIELO);

        lblUsuario.setFont(fuentes.kiwiSoda(Font.PLAIN, 30));
        lblUsuario.setForeground(Tema.TEXTO);
        lblRango.setFont(fuentes.mono(Font.BOLD, 12));
        lblRango.setForeground(Tema.ESTRELLA);
        lblProgreso.setFont(fuentes.mono(Font.PLAIN, 11));
        lblProgreso.setForeground(Tema.SECUNDARIO);
        lblCorreo.setFont(fuentes.mono(Font.PLAIN, 12));
        lblCorreo.setForeground(Tema.SECUNDARIO);
        lblFaceId.setFont(fuentes.mono(Font.BOLD, 10));

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        for (JLabel l : new JLabel[]{lblUsuario, lblRango, lblProgreso, lblCorreo, lblFaceId}) {
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        datos.add(lblUsuario);
        datos.add(Box.createVerticalStrut(4));
        datos.add(lblRango);
        datos.add(Box.createVerticalStrut(3));
        datos.add(lblProgreso);
        datos.add(Box.createVerticalStrut(14));
        datos.add(lblCorreo);
        datos.add(Box.createVerticalStrut(5));
        datos.add(lblFaceId);

        tarjeta.add(cab, BorderLayout.NORTH);
        tarjeta.add(retrato, BorderLayout.CENTER);
        tarjeta.add(datos, BorderLayout.SOUTH);
        return tarjeta;
    }

    // Agrupa las estadísticas resumidas y las filas de partidas recientes.
    private JPanel crearDerecha() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel mosaicos = new JPanel(new GridLayout(2, 3, 14, 14));
        mosaicos.setOpaque(false);
        mosaicos.add(mEstrellas);
        mosaicos.add(mPartidas);
        mosaicos.add(mVictorias);
        mosaicos.add(mRecord);
        mosaicos.add(mRonda);
        mosaicos.add(mAccesorios);

        PanelRedondo historial = new PanelRedondo(24);
        historial.setLayout(new BorderLayout(0, 12));
        historial.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel titulo = etiqueta("HISTORIAL DE PARTIDAS", fuentes.mono(Font.BOLD, 12), Tema.HIELO);
        listaHistorial.setOpaque(false);
        historial.add(titulo, BorderLayout.NORTH);
        historial.add(listaHistorial, BorderLayout.CENTER);

        p.add(mosaicos, BorderLayout.NORTH);
        p.add(historial, BorderLayout.CENTER);
        return p;
    }

    // ---------- API para el controlador ----------

    // Botón que el controlador conecta con la navegación de regreso al menú.
    public JButton getBtnVolver() { return btnVolver; }

    // Actualiza el retrato y usa la inicial del usuario si no hay imagen.
    public void setAvatar(BufferedImage imagen, String usuario) {
        retrato.setImagen(imagen, usuario);
    }

    // Actualiza los datos de cuenta que aparecen bajo el avatar.
    public void setCuenta(String usuario, String correo, boolean faceId) {
        // el nombre se achica si es muy largo para que quepa en la tarjeta
        int tam = 30;
        Font f = fuentes.kiwiSoda(Font.PLAIN, tam);
        while (tam > 14 && getFontMetrics(f).stringWidth(usuario) > 280) {
            tam -= 2;
            f = fuentes.kiwiSoda(Font.PLAIN, tam);
        }
        lblUsuario.setFont(f);
        lblUsuario.setText(usuario);
        lblCorreo.setText(correo == null || correo.isEmpty() ? " " : correo);
        lblFaceId.setText(faceId ? "FACE ID: ACTIVADO" : "FACE ID: NO ACTIVADO");
        lblFaceId.setForeground(faceId ? Tema.HIELO : Tema.SECUNDARIO);
    }

    // Actualiza el rango actual y el texto de avance hacia el siguiente.
    public void setRango(String rango, String progreso) {
        lblRango.setText("RANGO  *  " + rango);
        lblProgreso.setText(progreso);
    }

    // Muestra el saldo actual de la tienda y las estrellas ganadas jugando.
    public void setEstrellas(int saldo, int ganadas) {
        mEstrellas.setValores(String.valueOf(saldo), ganadas + " ganadas jugando");
    }

    // Muestra el total de partidas y su división por modo.
    public void setPartidas(int total, int clasico, int patrones) {
        mPartidas.setValores(String.valueOf(total), clasico + " clásico  *  " + patrones + " patrones");
    }

    // Muestra victorias clásicas y su porcentaje sobre las partidas clásicas.
    public void setVictorias(int victorias, int porcentaje, int jugadasClasico) {
        mVictorias.setValores(String.valueOf(victorias),
                jugadasClasico == 0 ? "modo clásico" : porcentaje + "% de " + jugadasClasico + " partidas");
    }

    // intentos <= 0 significa que todavía no tiene récord.
    // Muestra el mejor resultado del ranking clásico o un texto inicial.
    public void setRecord(int intentos) {
        mRecord.setValores(intentos > 0 ? String.valueOf(intentos) : "-",
                intentos > 0 ? "intentos en la clasificación" : "gana una partida clásica");
    }

    // Muestra la mayor cantidad de rondas completadas en modo patrones.
    public void setMejorRonda(int ronda) {
        mRonda.setValores(ronda > 0 ? String.valueOf(ronda) : "-",
                ronda > 0 ? "rondas en modo patrones" : "juega el modo patrones");
    }

    // Muestra cuántos accesorios posee el usuario respecto al catálogo disponible.
    public void setAccesorios(int comprados, int total) {
        mAccesorios.setValores(comprados + "/" + total, "comprados en la tienda");
    }

    // Sustituye las filas visibles por las partidas recientes recibidas del modelo.
    public void setHistorial(List<ModeloPerfil.Entrada> entradas) {
        listaHistorial.removeAll();
        if (entradas.isEmpty()) {
            JLabel vacio = etiqueta("Aún no hay partidas. Juega para llenar tu historial.",
                    fuentes.mono(Font.ITALIC, 14), Tema.SECUNDARIO);
            vacio.setHorizontalAlignment(SwingConstants.LEFT);
            listaHistorial.add(vacio);
        }
        for (int i = 0; i < FILAS_HISTORIAL; i++) {
            if (i < entradas.size()) listaHistorial.add(crearFila(entradas.get(i)));
            else if (!entradas.isEmpty() || i > 0) listaHistorial.add(filaVacia());
        }
        listaHistorial.revalidate();
        listaHistorial.repaint();
    }

    // ---------- Filas del historial ----------

    // Convierte una entrada del historial en una fila con fecha, resultado y estrellas.
    private JPanel crearFila(ModeloPerfil.Entrada e) {
        String fecha = FORMATO_FECHA.format(Instant.ofEpochMilli(e.momento).atZone(ZoneId.systemDefault()));
        String modo, resultado, detalle;
        Color colorResultado;

        if (e.modo == ModeloPerfil.Modo.CLASICO) {
            modo = "CLÁSICO " + e.tamano + "x" + e.tamano;
            resultado = e.victoria ? "VICTORIA" : "DERROTA";
            colorResultado = e.victoria ? Tema.ESTRELLA : Tema.ERROR;
            detalle = e.valor + " intentos" + (e.detalle.isEmpty() ? "" : "  *  vs " + e.detalle);
        } else {
            modo = "PATRONES";
            resultado = e.valor + (e.valor == 1 ? " RONDA" : " RONDAS");
            colorResultado = Tema.HIELO;
            detalle = e.valor == 0 ? "no completó ninguna ronda"
                    : "llegó a " + (ModeloPatrones.NIVEL_INICIAL + e.valor - 1) + " casillas";
        }

        JLabel lblEstrellas = etiqueta(e.estrellas > 0 ? "+" + e.estrellas : "-",
                fuentes.mono(Font.BOLD, 13), e.estrellas > 0 ? Tema.ESTRELLA : Tema.SECUNDARIO);
        lblEstrellas.setHorizontalAlignment(SwingConstants.RIGHT);
        if (e.estrellas > 0) {
            lblEstrellas.setIcon(new IconoEstrella(13, Tema.ESTRELLA));
            lblEstrellas.setHorizontalTextPosition(SwingConstants.LEFT);
            lblEstrellas.setIconTextGap(6);
        }

        JLabel[] columnas = {
                etiqueta(fecha, fuentes.mono(Font.PLAIN, 12), Tema.SECUNDARIO),
                etiqueta(modo, fuentes.mono(Font.BOLD, 11), Tema.HIELO),
                etiqueta(resultado, fuentes.mono(Font.BOLD, 13), colorResultado),
                etiqueta(detalle, fuentes.mono(Font.PLAIN, 12), Tema.TEXTO),
                lblEstrellas
        };

        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        fila.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.LINEA, 1, true), new EmptyBorder(0, 14, 0, 14)));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        for (int i = 0; i < columnas.length; i++) {
            columnas[i].setPreferredSize(new Dimension(0, columnas[i].getPreferredSize().height));
            c.gridx = i;
            c.weightx = PESOS[i];
            fila.add(columnas[i], c);
        }
        return fila;
    }

    private static JPanel filaVacia() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    // ---------- Componentes propios ----------

    // Panel reutilizable con fondo oscuro, borde y esquinas redondeadas.
    private static class PanelRedondo extends JPanel {
        private final int radio;

        PanelRedondo(int radio) {
            this.radio = radio;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, radio, radio);
            g2.setColor(new Color(4, 12, 34, 175));
            g2.fill(forma);
            g2.setColor(Tema.LINEA);
            g2.draw(forma);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Tarjeta de estadística con título, valor principal y descripción breve.
    private static class Mosaico extends PanelRedondo {
        private final JLabel lblValor = new JLabel("-");
        private final JLabel lblDetalle = new JLabel(" ");

        Mosaico(String titulo, Color colorValor) {
            super(18);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(12, 18, 10, 18));

            JLabel lblTitulo = etiqueta(titulo, fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO);
            lblValor.setFont(fuentes.kiwiSoda(Font.PLAIN, 38));
            lblValor.setForeground(colorValor);
            lblDetalle.setFont(fuentes.mono(Font.PLAIN, 11));
            lblDetalle.setForeground(Tema.SECUNDARIO);
            lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblDetalle.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(lblTitulo);
            add(Box.createVerticalGlue());
            add(lblValor);
            add(Box.createVerticalGlue());
            add(lblDetalle);
        }

        void setValores(String valor, String detalle) {
            lblValor.setText(valor);
            lblDetalle.setText(detalle);
        }
    }

    // Dibuja el avatar centrado con un halo; si falta la imagen, dibuja la inicial.
    private static class Retrato extends JComponent {
        private BufferedImage imagen;
        private String inicial = "?";

        Retrato() { setOpaque(false); }

        void setImagen(BufferedImage imagen, String usuario) {
            this.imagen = imagen;
            this.inicial = usuario == null || usuario.isEmpty() ? "?" : usuario.substring(0, 1).toUpperCase();
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            if (w <= 20 || h <= 20) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            float cx = w / 2f, cy = h / 2f, r = Math.min(w, h) * 0.62f;
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r, new float[]{0f, 1f},
                    new Color[]{new Color(169, 188, 255, 75), new Color(169, 188, 255, 0)}));
            g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));

            if (imagen != null) {
                double escala = Math.min((w - 8.0) / imagen.getWidth(), (h - 8.0) / imagen.getHeight());
                int iw = (int) (imagen.getWidth() * escala), ih = (int) (imagen.getHeight() * escala);
                g2.drawImage(imagen, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
            } else {
                int d = Math.min(w, h) - 30;
                int x = (w - d) / 2, y = (h - d) / 2;
                g2.setColor(new Color(0x0A1028));
                g2.fillOval(x, y, d, d);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(Tema.HIELO);
                g2.drawOval(x, y, d, d);
                g2.setFont(fuentes.kiwiSoda(Font.PLAIN, d * 0.5f));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Tema.ESTRELLA);
                g2.drawString(inicial, (w - fm.stringWidth(inicial)) / 2f,
                        y + (d + fm.getAscent() - fm.getDescent()) / 2f);
            }
            g2.dispose();
        }
    }

    // Ícono de estrella dibujado con Java2D para no depender de una fuente especial.
    private static class IconoEstrella implements Icon {
        private final int tam;
        private final Color color;

        IconoEstrella(int tam, Color color) {
            this.tam = tam;
            this.color = color;
        }

        @Override public int getIconWidth()  { return tam; }
        @Override public int getIconHeight() { return tam; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double cx = x + tam / 2.0, cy = y + tam / 2.0, re = tam / 2.0, ri = re * 0.45;
            Path2D.Double p = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double ang = -Math.PI / 2 + i * Math.PI / 5;
                double r = (i % 2 == 0) ? re : ri;
                double px = cx + r * Math.cos(ang), py = cy + r * Math.sin(ang);
                if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
            }
            p.closePath();
            g2.setColor(color);
            g2.fill(p);
            g2.dispose();
        }
    }
}
