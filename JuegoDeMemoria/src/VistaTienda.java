import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;


public class VistaTienda extends JPanel {
    public enum CategoriaTienda {
        ACCESORIOS("ACCESORIOS", "JÚPITER"), TIEMPO("TIEMPO EXTRA", "SATURNO"), PUNTOS("PUNTOS EXTRA", "MARTE");
        final String titulo, planeta;
        CategoriaTienda(String titulo, String planeta) { this.titulo=titulo; this.planeta=planeta; }
    }
    private enum TipoAccesorio {
        ANTENAS("antenas", "ANTENAS"), CASCO("casco", "CASCO"), CAPA("capa", "CAPA"),
        HALO("halo", "HALO"), MOCHILA("mochila", "MOCHILA"), VISOR("visor", "VISOR");
        final String id, titulo;
        TipoAccesorio(String id, String titulo) { this.id=id; this.titulo=titulo; }
    }
    private final JLabel lblSaldo = etiqueta("0", fuentes.kiwiSoda(Font.BOLD, 26), Tema.ESTRELLA);
    private final JLabel lblMensaje = etiqueta(" ", fuentes.mono(Font.BOLD, 12), Tema.HIELO);
    private final JButton volver = Tema.botonTexto("← MENÚ PRINCIPAL");
    private final VistaPantallaPrincipal principal;
    private final Map<CategoriaTienda,JPanel> listas = new EnumMap<>(CategoriaTienda.class);
    private final Map<TipoAccesorio,JPanel> grillasAccesorios = new EnumMap<>(TipoAccesorio.class);
    private final Map<String,Tarjeta> articulos = new LinkedHashMap<>();
    private static final Map<String,BufferedImage> CACHE_IMAGENES = new HashMap<>();
    private final AvatarPreview avatarPreview = new AvatarPreview();
    private final CardLayout layout = new CardLayout();
    private final JPanel paginas = new JPanel(layout);
    private Consumer<String> alComprar;
    private int saldo;
    private javax.swing.Timer temporizador;

    public VistaTienda(VistaPantallaPrincipal principal) {
        this.principal = principal;
        setOpaque(false);
        setLayout(new BorderLayout());
        Tema fondo = new Tema(170, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true); fondo.setViaje(true); fondo.setLayout(new BorderLayout(24,18));
        fondo.setBorder(new EmptyBorder(22,34,24,34));
        paginas.setOpaque(false);
        paginas.add(crearSistemaSolar(), "solar");
        for (CategoriaTienda c : CategoriaTienda.values()) paginas.add(crearCatalogo(c), c.name());
        fondo.add(crearCabecera(), BorderLayout.NORTH);
        fondo.add(paginas, BorderLayout.CENTER);
        fondo.add(lblMensaje, BorderLayout.SOUTH);
        add(fondo, BorderLayout.CENTER);
        volver.addActionListener(e -> principal.mostrarOpciones());
    }

    private JPanel crearCabecera() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        p.add(
                etiqueta("ASTROMATCH  •  OBSERVATORIO",
                        fuentes.mono(Font.BOLD, 11),
                        Tema.SECUNDARIO),
                BorderLayout.WEST
        );

        JPanel saldoPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 0)
        );
        saldoPanel.setOpaque(false);

        saldoPanel.add(
                etiqueta("ESTRELLAS DISPONIBLES",
                        fuentes.mono(Font.BOLD, 12),
                        Tema.SECUNDARIO)
        );

        BufferedImage imagenEstrella =
                prepararImagen("/iconos/estrella.png", 40);

        JPanel saldoConMoneda = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 3, 0)
        );
        saldoConMoneda.setOpaque(false);

        if (imagenEstrella != null) {
            MonedaAnimada iconoEstrella = new MonedaAnimada(imagenEstrella);
            iconoEstrella.setToolTipText("Estrellas disponibles");
            saldoConMoneda.add(iconoEstrella);
        }

        saldoConMoneda.add(lblSaldo);
        saldoPanel.add(saldoConMoneda);
        saldoPanel.add(volver);

        p.add(saldoPanel, BorderLayout.EAST);
        return p;
    }

    private JPanel crearSistemaSolar() {
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel guia=etiqueta("ELIGE UN PLANETA PARA EXPLORAR SUS ARTÍCULOS",fuentes.kiwiSoda(Font.PLAIN,18),Tema.TEXTO); guia.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(guia,BorderLayout.NORTH); p.add(new MapaSolar(),BorderLayout.CENTER); return p;
    }

    private JPanel crearCatalogo(CategoriaTienda cat) {
        JPanel pagina = new JPanel(new BorderLayout(0, 16)); pagina.setOpaque(false);
        JLabel titulo = etiqueta(cat.titulo, fuentes.kiwiSoda(Font.PLAIN, 38), Tema.TEXTO);
        JLabel subtitulo = etiqueta("Artículos de " + cat.planeta + " - compra con tus estrellas", fuentes.mono(Font.PLAIN, 12), Tema.SECUNDARIO);
        JPanel etiquetas = new JPanel(); etiquetas.setOpaque(false); etiquetas.setLayout(new BoxLayout(etiquetas, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT); subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        etiquetas.add(titulo); etiquetas.add(Box.createVerticalStrut(5)); etiquetas.add(subtitulo);
        JButton atras = Tema.botonTexto("← SISTEMA SOLAR"); atras.addActionListener(e -> layout.show(paginas, "solar"));
        JPanel cabecera = new JPanel(new BorderLayout()); cabecera.setOpaque(false);
        cabecera.add(etiquetas, BorderLayout.CENTER); cabecera.add(atras, BorderLayout.EAST);
        pagina.add(cabecera, BorderLayout.NORTH);

        if (cat == CategoriaTienda.ACCESORIOS) {
            pagina.add(crearEditorAccesorios(), BorderLayout.CENTER);
        } else {
            JPanel lista = new JPanel(new GridLayout(1, 3, 18, 0)); lista.setOpaque(false);
            JScrollPane scroll = crearScroll(lista);
            listas.put(cat, lista); pagina.add(scroll, BorderLayout.CENTER);
        }
        return pagina;
    }

    private JPanel crearEditorAccesorios() {
        JPanel editor = new JPanel(new GridLayout(1, 2, 22, 0)); editor.setOpaque(false);
        editor.add(avatarPreview);
        JPanel selector = new JPanel(new BorderLayout(0, 12)); selector.setOpaque(false);
        JPanel botones = new JPanel(new GridLayout(1, TipoAccesorio.values().length, 5, 0)); botones.setOpaque(false);
        CardLayout paginasAccesorios = new CardLayout();
        JPanel opciones = new JPanel(paginasAccesorios); opciones.setOpaque(false);
        for (TipoAccesorio tipo : TipoAccesorio.values()) {
            JButton boton = Tema.botonTexto(tipo.titulo);
            boton.setFont(fuentes.mono(Font.BOLD, 11));
            boton.addActionListener(e -> paginasAccesorios.show(opciones, tipo.name()));
            botones.add(boton);
            JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8)); grid.setOpaque(false);
            grillasAccesorios.put(tipo, grid);
            opciones.add(crearScroll(grid), tipo.name());
        }
        selector.add(botones, BorderLayout.NORTH); selector.add(opciones, BorderLayout.CENTER);
        editor.add(selector); return editor;
    }

    private JScrollPane crearScroll(JPanel contenido) {
        JScrollPane scroll = new JScrollPane(contenido, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public void setAlComprar(Consumer<String> accion){alComprar=accion;}
    public void mostrarTienda(){layout.show(paginas,"solar");}
    public void setSaldo(int nuevo){saldo=nuevo;lblSaldo.setText(String.valueOf(nuevo));for(Tarjeta t:articulos.values())t.setSaldo(nuevo);}
    public void agregarArticulo(String id,CategoriaTienda cat,String nombre,String descripcion,int precio,boolean unico,int valor){
        BufferedImage compuesto = cat == CategoriaTienda.ACCESORIOS ? componerAvatar(Collections.singleton(id)) : null;
        Tarjeta t = new Tarjeta(cat, nombre, descripcion, precio, valor, unico, compuesto, () -> { if (alComprar != null) alComprar.accept(id); });
        articulos.put(id, t);
        if (cat == CategoriaTienda.ACCESORIOS) {
            TipoAccesorio tipo = tipoAccesorio(id);
            if (tipo != null) grillasAccesorios.get(tipo).add(t);
        } else {
            listas.get(cat).add(t);
        }
        t.setSaldo(saldo);
    }
    public void marcarComprado(String id){Tarjeta t=articulos.get(id);if(t!=null)t.setComprado(true);}
    public void setAccesoriosEquipados(Set<String> ids){
        avatarPreview.setAccesorios(ids);
        for(Map.Entry<String,Tarjeta> e:articulos.entrySet()) e.getValue().setEquipado(ids.contains(e.getKey()));
    }
    public void limpiarTienda(){
        articulos.clear();
        for (JPanel lista : listas.values()) { lista.removeAll(); lista.revalidate(); lista.repaint(); }
        for (JPanel grid : grillasAccesorios.values()) { grid.removeAll(); grid.revalidate(); grid.repaint(); }
    }
    public void mostrarMensajeTienda(String texto,boolean error){lblMensaje.setForeground(error?Tema.ERROR:Tema.ESTRELLA);lblMensaje.setText(texto);if(temporizador!=null)temporizador.stop();temporizador=new javax.swing.Timer(3500,e->lblMensaje.setText(" "));temporizador.setRepeats(false);temporizador.start();}

    private final class MapaSolar extends JPanel {
        private final java.util.List<Planeta> planetas = new ArrayList<>();
        private double fase = 0;
        private final javax.swing.Timer animacion = new javax.swing.Timer(35, e -> {
            fase += 0.002;
            repaint();
        });

        private final BufferedImage imagenSol = prepararImagen("/planetas/sol.png", 190);
        private Planeta planetaHover;

        MapaSolar() {
            setOpaque(false);
            setPreferredSize(new Dimension(900, 570));
            // Un único planeta por cada sección, repartidos alrededor del Sol.
            planetas.add(new Planeta(CategoriaTienda.ACCESORIOS, "JÚPITER", "ACCESORIOS", new Color(0xE58A3A), 1.65, -90, 128, .50));
            planetas.add(new Planeta(CategoriaTienda.TIEMPO, "SATURNO", "TIEMPO EXTRA", new Color(0xD6A45E), 1.65, 30, 116, .50));
            planetas.add(new Planeta(CategoriaTienda.PUNTOS, "MARTE", "PUNTOS EXTRA", new Color(0xD84F32), 1.65, 150, 108, .50));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (Planeta p : planetas)
                        if (p.bounds.contains(e.getPoint())) {
                            layout.show(paginas, p.categoria.name());
                            break;
                        }
                }
            });

            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    Planeta sobre = null;

                    for (Planeta p : planetas) {
                        if (p.bounds.contains(e.getPoint())) {
                            sobre = p;
                            break;
                        }
                    }

                    if (sobre != planetaHover) {
                        planetaHover = sobre;
                        repaint();
                    }

                    setCursor(Cursor.getPredefinedCursor(
                            sobre != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR
                    ));
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    planetaHover = null;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }

            });



            addHierarchyListener(e -> {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) animacion.start();
                    else animacion.stop();
                }
            });


        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D x = (Graphics2D) g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double cx = getWidth() / 2.0, cy = getHeight() / 2.0,
                    maxR = Math.min(getWidth() * .34, getHeight() * .34);
            for (int i = 1; i <= 5; i++) {
                double rx = maxR * (.30 + i * .27), ry = rx * .60;
                x.setColor(new Color(117, 150, 224, 45));
                x.setStroke(new BasicStroke(1.3f));
                x.draw(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));
            }
            dibujarResplandorSol(x, cx, cy);

            for (Planeta p : planetas) {
                double a = Math.toRadians(p.angulo + fase * p.velocidad * 180), rx = maxR * p.orbita;
                dibujarPlaneta(
                        x,
                        p,
                        (int) (cx + Math.cos(a) * rx),
                        (int) (cy + Math.sin(a) * rx * .60),
                        p == planetaHover
                );
            }
            dibujarSol(x, cx, cy);
            x.dispose();
        }
        private void dibujarResplandorSol(Graphics2D x, double cx, double cy) {
            float radio = 165f;

            x.setPaint(new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    radio,
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{
                            new Color(255, 190, 55, 105),
                            new Color(255, 125, 25, 45),
                            new Color(255, 100, 15, 0)
                    }
            ));

            x.fill(new Ellipse2D.Double(
                    cx - radio, cy - radio,
                    radio * 2, radio * 2
            ));
        }

        private void dibujarSol(Graphics2D x, double cx, double cy) {
            if (imagenSol != null) {
                int xSol = (int) cx - imagenSol.getWidth() / 2;
                int ySol = (int) cy - imagenSol.getHeight() / 2;
                x.drawImage(imagenSol, xSol, ySol, null);
            }

            int centroX = (int) cx;
            int lineaBase = (int) cy + (imagenSol != null
                    ? imagenSol.getHeight() / 2 + 32
                    : 75);

            dibujarEtiqueta(
                    x,
                    "TIENDA ESTELAR",
                    centroX,
                    lineaBase,
                    18,
                    true
            );
        }

        private Rectangle dibujarEtiqueta(
                Graphics2D x, String texto, int centroX, int lineaBase,
                int tamanoFuente, boolean principal) {

            Font fuente = fuentes.kiwiSoda(Font.BOLD, tamanoFuente);
            x.setFont(fuente);
            FontMetrics fm = x.getFontMetrics();

            int ancho = fm.stringWidth(texto);
            int alto = fm.getHeight();
            int margenX = principal ? 18 : 12;
            int margenY = principal ? 10 : 7;
            int cajaX = centroX - ancho / 2 - margenX;
            int cajaY = lineaBase - fm.getAscent() - margenY;
            int cajaAncho = ancho + margenX * 2;
            int cajaAlto = alto + margenY * 2;

            // Sombra
            x.setColor(new Color(0, 0, 0, 150));
            x.fillRoundRect(cajaX + 3, cajaY + 4, cajaAncho, cajaAlto, 18, 18);

            // Placa con degradado
            Color inicio = principal
                    ? new Color(255, 205, 72)
                    : new Color(54, 91, 150);
            Color fin = principal
                    ? new Color(190, 91, 22)
                    : new Color(22, 37, 75);

            x.setPaint(new GradientPaint(cajaX, cajaY, inicio, cajaX, cajaY + cajaAlto, fin));
            x.fillRoundRect(cajaX, cajaY, cajaAncho, cajaAlto, 18, 18);

            // Borde
            x.setColor(principal ? new Color(255, 239, 166) : new Color(153, 211, 255));
            x.setStroke(new BasicStroke(principal ? 2.5f : 1.5f));
            x.drawRoundRect(cajaX, cajaY, cajaAncho, cajaAlto, 18, 18);

            // Texto
            x.setColor(Color.WHITE);
            x.drawString(texto, centroX - ancho / 2, lineaBase);

            return new Rectangle(cajaX, cajaY, cajaAncho, cajaAlto);
        }

        private void dibujarPlaneta(
                Graphics2D x, Planeta p, int cx, int cy, boolean resaltado) {
            int r = p.diam / 2;
            if (!p.spriteIntentado) {
                p.sprite = prepararSprite(p);
                p.spriteIntentado = true;
            }
            if (p.sprite != null) {
                Color c = p.color;
                float escala = resaltado ? 1.12f : 1f;
                float radioHalo = r * (resaltado ? 2.0f : 1.6f);
                int alpha = resaltado ? 145 : 90;

                x.setPaint(new RadialGradientPaint(
                        new Point2D.Double(cx, cy),
                        radioHalo,
                        new float[]{0f, .6f, 1f},
                        new Color[]{
                                new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha),
                                new Color(c.getRed(), c.getGreen(), c.getBlue(), 60),
                                new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)
                        }
                ));

                x.fill(new Ellipse2D.Double(
                        cx - radioHalo, cy - radioHalo,
                        radioHalo * 2, radioHalo * 2
                ));

                int ancho = (int) (p.sprite.getWidth() * escala);
                int alto = (int) (p.sprite.getHeight() * escala);

                x.drawImage(
                        p.sprite,
                        cx - ancho / 2,
                        cy - alto / 2,
                        ancho,
                        alto,
                        null
                );
            }

// La etiqueta y el área clicable se calculan aunque falte la imagen.
            String texto = p.nombre + " - " + p.seccion;
            boolean encima = cy < getHeight() / 2;
            int lineaBase = encima ? cy - r - 12 : cy + r + 24;

            Rectangle etiqueta = dibujarEtiqueta(
                    x,
                    texto,
                    cx,
                    lineaBase,
                    11,
                    false
            );

            Rectangle areaPlaneta = new Rectangle(
                    cx - r - 10,
                    cy - r - 10,
                    p.diam + 20,
                    p.diam + 20
            );

            p.bounds.setBounds(areaPlaneta.union(etiqueta));
        }
    }
    private static final class Planeta {
        final CategoriaTienda categoria;final String nombre,seccion;final Color color;final double orbita,angulo,velocidad;final int diam;final Rectangle bounds=new Rectangle();
        BufferedImage sprite; boolean spriteIntentado;
        Planeta(CategoriaTienda c,String n,String s,Color color,double o,double a,int d,double v){categoria=c;nombre=n;seccion=s;this.color=color;orbita=o;angulo=a;diam=d;velocidad=v;}
    }
    private static TipoAccesorio tipoAccesorio(String id) {
        for (TipoAccesorio tipo : TipoAccesorio.values()) if (id.startsWith("accesorio_" + tipo.id + "_")) return tipo;
        return null;
    }

    private static BufferedImage cargarImagen(String recurso) {
        synchronized (CACHE_IMAGENES) {
            BufferedImage cached = CACHE_IMAGENES.get(recurso);
            if (cached != null) return cached;
            try (java.io.InputStream in = VistaTienda.class.getResourceAsStream(recurso)) {
                BufferedImage imagen = in == null ? null : javax.imageio.ImageIO.read(in);
                if (imagen != null) CACHE_IMAGENES.put(recurso, imagen);
                return imagen;
            } catch (java.io.IOException e) { return null; }
        }
    }

    private static final double FACTOR_SATURNO = 1.93;   // la imagen incluye los anillos

    /** Carga la textura del planeta y la deja ya escalada al tamaño con que se dibuja. */
    private static BufferedImage prepararSprite(Planeta p) {
        String nombre = java.text.Normalizer.normalize(p.nombre, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();               // "JÚPITER" -> "jupiter"
        BufferedImage origen = cargarImagen("/planetas/" + nombre + ".png");
        if (origen == null) return null;

        int ancho = (int) Math.round(p.diam * (p.nombre.equals("SATURNO") ? FACTOR_SATURNO : 1.0));
        int alto = (int) Math.round(ancho * (double) origen.getHeight() / origen.getWidth());

        BufferedImage actual = origen;
        while (actual.getWidth() / 2 > ancho) {                         // reducción por mitades
            BufferedImage mitad = new BufferedImage(actual.getWidth() / 2, actual.getHeight() / 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = mitad.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(actual, 0, 0, mitad.getWidth(), mitad.getHeight(), null);
            g.dispose();
            actual = mitad;
        }
        BufferedImage fin = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fin.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(actual, 0, 0, ancho, alto, null);
        g.dispose();
        return fin;
    }

    private static BufferedImage prepararImagen(String recurso, int ancho) {
        BufferedImage origen = cargarImagen(recurso);
        if (origen == null) return null;

        int alto = (int) Math.round(
                ancho * (double) origen.getHeight() / origen.getWidth()
        );

        BufferedImage escalada =
                new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = escalada.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        g.drawImage(origen, 0, 0, ancho, alto, null);
        g.dispose();

        return escalada;
    }

    static BufferedImage componerAvatar(Set<String> ids) {
        BufferedImage base = cargarImagen("/avatar_astronauta/base/astronauta_base.png");
        if (base == null) return null;
        BufferedImage resultado = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resultado.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        dibujarCapas(g, ids, true);
        g.drawImage(base, 0, 0, null);
        dibujarCapas(g, ids, false);
        g.dispose();
        return resultado;
    }

    private static void dibujarCapas(Graphics2D g, Set<String> ids, boolean atras) {
        String[] orden = atras ? new String[]{"capa", "mochila"} : new String[]{"antenas", "casco", "visor", "halo"};
        for (String ranura : orden) for (String id : ids) {
            TipoAccesorio tipo = tipoAccesorio(id);
            if (tipo == null || !tipo.id.equals(ranura)) continue;
            String ruta = "/avatar_astronauta/" + tipo.id + "/accesorio_" + tipo.id + "_" + id.substring(id.lastIndexOf('_') + 1) + ".png";
            BufferedImage capa = cargarImagen(ruta);
            if (capa != null) g.drawImage(capa, 0, 0, null);
        }
    }

    private static final class AvatarPreview extends JPanel {
        private Set<String> accesorios = Collections.emptySet();
        private BufferedImage imagenActual;
        AvatarPreview(){setOpaque(false);setPreferredSize(new Dimension(430,500));setMinimumSize(new Dimension(280,360));imagenActual=componerAvatar(accesorios);}
        void setAccesorios(Set<String> ids){accesorios=new LinkedHashSet<>(ids);imagenActual=componerAvatar(accesorios);repaint();}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            x.setColor(new Color(4,12,34,165));x.fillRoundRect(4,4,getWidth()-8,getHeight()-8,24,24);x.setColor(Tema.LINEA);x.drawRoundRect(4,4,getWidth()-9,getHeight()-9,24,24);
            x.setFont(fuentes.mono(Font.BOLD,12));x.setColor(Tema.HIELO);x.drawString("TU ASTRONAUTA",20,26);
            BufferedImage imagen=imagenActual;
            if(imagen!=null){double escala=Math.min((getWidth()-34.0)/imagen.getWidth(),(getHeight()-62.0)/imagen.getHeight());int w=(int)(imagen.getWidth()*escala),h=(int)(imagen.getHeight()*escala);x.drawImage(imagen,(getWidth()-w)/2,(getHeight()-h)/2+8,w,h,null);}
            else {x.setColor(Tema.SECUNDARIO);x.drawString("No se encontraron los recursos del avatar",20,52);}
            x.setColor(Tema.SECUNDARIO);x.setFont(fuentes.mono(Font.PLAIN,10));x.drawString(accesorios.isEmpty()?"Elige diseños para personalizarlo":"Los accesorios comprados se equipan aquí",20,getHeight()-17);x.dispose();
        }
    }

    private static class Tarjeta extends JPanel {
        final int precio; final int valor; final boolean unico; final CategoriaTienda categoria;
        final VistaTablero.BotonPill comprar =
                new VistaTablero.BotonPill("COMPRAR", true, true);
        final JLabel disponibilidad=etiqueta("",fuentes.mono(Font.BOLD,10),Tema.SECUNDARIO);

        boolean adquirido, equipado, asequible;

        Tarjeta(CategoriaTienda categoria,String nombre,String descripcion,int precio,int valor,boolean unico,BufferedImage imagen,Runnable click) {
            this.precio = precio;
            this.valor = valor;
            this.unico = unico;
            this.categoria = categoria;

            disponibilidad.setHorizontalAlignment(SwingConstants.CENTER);

            setOpaque(false);
            setBorder(new EmptyBorder(14, 14, 14, 14));
            setLayout(new BorderLayout(8, 8));
            if (imagen != null) {
                JLabel dibujo = new JLabel(new ImageIcon(escalar(imagen, 112, 126)));
                dibujo.setHorizontalAlignment(SwingConstants.CENTER);
                add(dibujo, BorderLayout.CENTER);
                setPreferredSize(new Dimension(145, 194));
                JPanel datos = new JPanel();
                datos.setOpaque(false);
                datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
                JLabel n = etiqueta(nombre, fuentes.mono(Font.BOLD, 10), Tema.TEXTO);
                n.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel p = etiqueta(precio + " ESTRELLAS", fuentes.mono(Font.BOLD, 9), Tema.ESTRELLA);
                p.setAlignmentX(Component.CENTER_ALIGNMENT);
                datos.add(n);
                datos.add(Box.createVerticalStrut(3));
                datos.add(p);
                datos.add(disponibilidad);
                disponibilidad.setAlignmentX(Component.CENTER_ALIGNMENT);
                add(datos, BorderLayout.NORTH);
            } else {
                JPanel contenido = new JPanel();
                contenido.setOpaque(false);
                contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
                IconoExtra icono = new IconoExtra(categoria);
                icono.setAlignmentX(Component.CENTER_ALIGNMENT);
                contenido.add(icono);
                contenido.add(Box.createVerticalStrut(9));
                String cantidad = "+" + valor + (categoria == CategoriaTienda.TIEMPO ? " SEGUNDOS" : " PUNTOS");
                JLabel cantidadLbl = etiqueta(cantidad, fuentes.kiwiSoda(Font.BOLD, 29), categoria == CategoriaTienda.TIEMPO ? Tema.HIELO : Tema.ESTRELLA);
                cantidadLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                contenido.add(Box.createVerticalStrut(5));
                contenido.add(cantidadLbl);
                JLabel detalle = etiqueta(
                        "<html><div style='text-align:center;width:210px'>"
                                + descripcion
                                + "</div></html>",
                        fuentes.mono(Font.PLAIN, 11),
                        Tema.SECUNDARIO
                );
                detalle.setAlignmentX(Component.CENTER_ALIGNMENT);

                Dimension altoDetalle = new Dimension(230, 38);
                detalle.setPreferredSize(altoDetalle);
                detalle.setMinimumSize(altoDetalle);
                detalle.setMaximumSize(altoDetalle);

                contenido.add(Box.createVerticalStrut(6));
                contenido.add(detalle);
                JLabel costo = etiqueta("COSTO: " + precio + " ESTRELLAS", fuentes.mono(Font.BOLD, 12), Tema.ESTRELLA);
                costo.setAlignmentX(Component.CENTER_ALIGNMENT);
                contenido.add(Box.createVerticalStrut(10));
                contenido.add(costo);
                costo.setHorizontalAlignment(SwingConstants.CENTER);
                disponibilidad.setAlignmentX(Component.CENTER_ALIGNMENT);
                contenido.add(Box.createVerticalStrut(5));
                contenido.add(disponibilidad);
                add(contenido, BorderLayout.CENTER);
                setPreferredSize(new Dimension(260, 330));
            }

            comprar.addActionListener(e -> click.run());
            add(comprar, BorderLayout.SOUTH);
        }

        private static BufferedImage escalar(BufferedImage fuente,int ancho,int alto){
            BufferedImage mini=new BufferedImage(ancho,alto,BufferedImage.TYPE_INT_ARGB);Graphics2D g=mini.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(fuente,0,0,ancho,alto,null);g.dispose();return mini;
        }

        void setSaldo(int saldo){asequible=saldo>=precio;actualizarBoton();if(adquirido)disponibilidad.setText(equipado?"EN USO":"COMPRADO");else if(asequible)disponibilidad.setText("SALDO SUFICIENTE");else disponibilidad.setText("TE FALTAN "+(precio-saldo)+" ESTRELLAS");disponibilidad.setForeground(asequible?Tema.HIELO:Tema.ERROR);}
        void setComprado(boolean b){adquirido=b;actualizarBoton();}
        void setEquipado(boolean b){equipado=b;actualizarBoton();}

        private void actualizarBoton() {
            comprar.setText(equipado ? "RETIRAR" : (adquirido ? "EQUIPAR" : "COMPRAR"));

            boolean habilitado = adquirido || asequible;
            comprar.setEnabled(habilitado);
            comprar.setForeground(Color.BLACK);
        }
                @Override protected void paintComponent(Graphics g){Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);x.setColor(new Color(8,13,32,195));x.fillRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);x.setColor(adquirido?Tema.ESTRELLA:Tema.LINEA);x.drawRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);x.dispose();}
    }

    private static final class MonedaAnimada extends JComponent {
        private final BufferedImage imagen;
        private double angulo;
        private final javax.swing.Timer giro;

        MonedaAnimada(BufferedImage imagen) {
            this.imagen = imagen;
            setPreferredSize(new Dimension(44, 36));

            giro = new javax.swing.Timer(40, e -> {
                angulo += 0.05;
                repaint();
            });

            addHierarchyListener(e -> {
                if ((e.getChangeFlags()
                        & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        giro.start();
                    } else {
                        giro.stop();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D x = (Graphics2D) g.create();
            x.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            x.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            double giroHorizontal = Math.cos(angulo);
            int centroX = getWidth() / 2;
            int centroY = getHeight() / 2;

            if (Math.abs(giroHorizontal) < 0.08) {
                // Vista de canto cuando la moneda está de lado.
                x.setColor(new Color(0xF5B900));
                x.fillRoundRect(centroX - 2, centroY - 13, 4, 26, 4, 4);
                x.setColor(Color.BLACK);
                x.drawRoundRect(centroX - 2, centroY - 13, 4, 26, 4, 4);
            } else {
                // Se estrecha y se refleja al girar sobre su eje.
                x.translate(centroX, centroY);
                x.scale(giroHorizontal, 1.0);
                x.drawImage(
                        imagen,
                        -imagen.getWidth() / 2,
                        -imagen.getHeight() / 2,
                        null
                );
            }

            x.dispose();
        }
    }

    private static class IconoExtra extends JComponent {
        private final CategoriaTienda tipo;
        IconoExtra(CategoriaTienda tipo){this.tipo=tipo;setPreferredSize(new Dimension(74,74));setMaximumSize(new Dimension(74,74));}
        @Override protected void paintComponent(Graphics g){
            Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int d=64,cx=getWidth()/2,cy=getHeight()/2;
            Color acento=tipo==CategoriaTienda.TIEMPO?Tema.HIELO:Tema.ESTRELLA;
            x.setPaint(new RadialGradientPaint(new Point(cx,cy),d/2,new float[]{0f,1f},new Color[]{new Color(acento.getRed(),acento.getGreen(),acento.getBlue(),100),new Color(acento.getRed(),acento.getGreen(),acento.getBlue(),15)}));x.fill(new Ellipse2D.Double(cx-d/2,cy-d/2,d,d));x.setColor(acento);x.setStroke(new BasicStroke(3f));
            if (tipo == CategoriaTienda.TIEMPO) {
                int radio = 21;

                // Borde doble del reloj
                x.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                x.drawOval(cx - radio, cy - radio, radio * 2, radio * 2);

                x.setStroke(new BasicStroke(1.4f));
                x.drawOval(cx - radio + 5, cy - radio + 5, (radio - 5) * 2, (radio - 5) * 2);

                // Marcas de las horas
                for (int i = 0; i < 12; i++) {
                    double angulo = Math.toRadians(i * 30 - 90);
                    int largo = (i % 3 == 0) ? 5 : 3;
                    int x1 = cx + (int) (Math.cos(angulo) * (radio - 5));
                    int y1 = cy + (int) (Math.sin(angulo) * (radio - 5));
                    int x2 = cx + (int) (Math.cos(angulo) * (radio - 5 - largo));
                    int y2 = cy + (int) (Math.sin(angulo) * (radio - 5 - largo));
                    x.drawLine(x1, y1, x2, y2);
                }

                // Manecillas y centro
                x.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                x.drawLine(cx, cy, cx, cy - 13);
                x.drawLine(cx, cy, cx + 11, cy + 7);
                x.fillOval(cx - 3, cy - 3, 6, 6);
            }
            else {Path2D estrella=new Path2D.Double();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5,r=i%2==0?23:10,px=cx+Math.cos(a)*r,py=cy+Math.sin(a)*r;if(i==0)estrella.moveTo(px,py);else estrella.lineTo(px,py);}estrella.closePath();x.fill(estrella);}
            x.dispose();
        }
    }
    private static JLabel etiqueta(String t,Font f,Color c){JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);return l;}
}


