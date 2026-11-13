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
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false);
        p.add(etiqueta("ASTROMATCH  •  OBSERVATORIO",fuentes.mono(Font.BOLD,11),Tema.SECUNDARIO),BorderLayout.WEST);
        JPanel saldoPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0)); saldoPanel.setOpaque(false);
        saldoPanel.add(etiqueta("ESTRELLAS DISPONIBLES",fuentes.mono(Font.BOLD,12),Tema.SECUNDARIO)); saldoPanel.add(lblSaldo); saldoPanel.add(volver); p.add(saldoPanel,BorderLayout.EAST); return p;
    }

    private JPanel crearSistemaSolar() {
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel guia=etiqueta("ELIGE UN PLANETA PARA EXPLORAR SUS ARTÍCULOS",fuentes.mono(Font.PLAIN,12),Tema.SECUNDARIO); guia.setHorizontalAlignment(SwingConstants.CENTER);
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
        private final javax.swing.Timer animacion = new javax.swing.Timer(35, e -> { fase += 0.008; repaint(); });
        MapaSolar() {
            setOpaque(false); setPreferredSize(new Dimension(900, 570));
            // Un único planeta por cada sección, repartidos alrededor del Sol.
            planetas.add(new Planeta(CategoriaTienda.ACCESORIOS,"JÚPITER","ACCESORIOS",new Color(0xE58A3A),1.35,-90,128,.50));
            planetas.add(new Planeta(CategoriaTienda.TIEMPO,"SATURNO","TIEMPO EXTRA",new Color(0xD6A45E),1.35,30,116,.50));
            planetas.add(new Planeta(CategoriaTienda.PUNTOS,"MARTE","PUNTOS EXTRA",new Color(0xD84F32),1.35,150,108,.50));
            addMouseListener(new java.awt.event.MouseAdapter(){public void mouseClicked(java.awt.event.MouseEvent e){for(Planeta p:planetas)if(p.bounds.contains(e.getPoint())){layout.show(paginas,p.categoria.name());break;}}});
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter(){public void mouseMoved(java.awt.event.MouseEvent e){boolean hit=false;for(Planeta p:planetas)hit|=p.bounds.contains(e.getPoint());setCursor(Cursor.getPredefinedCursor(hit?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR));}});
            addHierarchyListener(e->{if((e.getChangeFlags()&java.awt.event.HierarchyEvent.SHOWING_CHANGED)!=0){if(isShowing())animacion.start();else animacion.stop();}});
        }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g); Graphics2D x=(Graphics2D)g.create();
            x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double cx=getWidth()/2.0,cy=getHeight()/2.0,maxR=Math.min(getWidth()*.43,getHeight()*.43);
            for(int i=1;i<=5;i++){double rx=maxR*(.30+i*.21),ry=rx*.52;x.setColor(new Color(117,150,224,45));x.setStroke(new BasicStroke(1.3f));x.draw(new Ellipse2D.Double(cx-rx,cy-ry,rx*2,ry*2));}
            for(Planeta p:planetas){double a=Math.toRadians(p.angulo+fase*p.velocidad*180),rx=maxR*p.orbita;dibujarPlaneta(x,p,(int)(cx+Math.cos(a)*rx),(int)(cy+Math.sin(a)*rx*.52));}
            dibujarSol(x,cx,cy);x.dispose();
        }
        private void dibujarSol(Graphics2D x,double cx,double cy){
            int r=58;
            x.setPaint(new RadialGradientPaint(new Point2D.Double(cx,cy),132,new float[]{0f,.55f,1f},new Color[]{new Color(255,199,58,105),new Color(255,130,24,42),new Color(255,130,24,0)}));
            x.fill(new Ellipse2D.Double(cx-132,cy-132,264,264));
            // Corona irregular con destellos finos, como plasma alrededor del disco.
            Path2D corona=new Path2D.Double();int pasos=120;
            for(int i=0;i<pasos;i++){double a=2*Math.PI*i/pasos;double pulso=5*Math.sin(a*9)+3*Math.sin(a*17)+2*Math.cos(a*23);double rr=r+12+pulso;if(i==0)corona.moveTo(cx+Math.cos(a)*rr,cy+Math.sin(a)*rr);else corona.lineTo(cx+Math.cos(a)*rr,cy+Math.sin(a)*rr);}corona.closePath();
            x.setPaint(new GradientPaint((float)cx-r,(float)cy-r,new Color(0xFFCE3B),(float)cx+r,(float)cy+r,new Color(0xF26A12)));x.fill(corona);
            Ellipse2D disco=new Ellipse2D.Double(cx-r,cy-r,r*2,r*2);
            x.setPaint(new RadialGradientPaint(new Point2D.Double(cx-r*.28,cy-r*.34),r*1.8f,new float[]{0f,.55f,1f},new Color[]{new Color(0xFFF27A),new Color(0xFFB51D),new Color(0xE95D0C)}));x.fill(disco);
            x.setClip(disco);
            x.setColor(new Color(255,239,130,95));
            for(int i=0;i<8;i++){double ox=Math.sin(i*2.2)*r*.55,oy=Math.cos(i*1.7)*r*.48,rr=4+(i%3)*2;x.fill(new Ellipse2D.Double(cx+ox-rr,cy+oy-rr,rr*2.4,rr*1.2));}
            x.setColor(new Color(255,95,18,100));x.setStroke(new BasicStroke(2.5f));
            for(int i=0;i<5;i++){double y=cy-r*.6+i*16;x.draw(new Arc2D.Double(cx-r*.9,y,r*1.8,12,15,145,Arc2D.OPEN));}
            x.setClip(null);
            x.setFont(fuentes.kiwiSoda(Font.BOLD,13));String t="TIENDA ESTELAR";FontMetrics fm=x.getFontMetrics();int tw=fm.stringWidth(t);
            x.setColor(new Color(82,37,12,225));x.fillRoundRect((int)cx-tw/2-7,(int)cy-11,tw+14,22,9,9);x.setColor(new Color(255,246,209));x.drawString(t,(int)cx-tw/2,(int)cy+5);
        }
        private void dibujarPlaneta(Graphics2D x,Planeta p,int cx,int cy){
            int r=p.diam/2;
            Ellipse2D bola=new Ellipse2D.Double(cx-r,cy-r,p.diam,p.diam);
            // Anillo posterior de Saturno.
            if(p.nombre.equals("SATURNO")){x.setPaint(new GradientPaint(cx-r-10,cy,new Color(224,192,128,150),cx+r+12,cy,new Color(139,98,61,225)));x.setStroke(new BasicStroke(7f));x.draw(new Ellipse2D.Double(cx-r-15,cy-r*.28,r*2+30,r*.8));}
            x.setPaint(new RadialGradientPaint(new Point2D.Double(cx-r*.35,cy-r*.42),p.diam*1.35f,new float[]{0f,.6f,1f},new Color[]{p.color.brighter(),p.color,new Color(Math.max(0,p.color.getRed()/3),Math.max(0,p.color.getGreen()/3),Math.max(0,p.color.getBlue()/3))}));x.fill(bola);
            x.setClip(bola);
            if(p.nombre.equals("JÚPITER")||p.nombre.equals("SATURNO")||p.nombre.equals("NEPTUNO")){
                Color banda=p.nombre.equals("JÚPITER")?new Color(117,61,35,105):new Color(245,220,166,85);
                for(int i=0;i<7;i++){double yy=cy-r+i*p.diam/7.0; x.setColor(i%2==0?banda:new Color(255,235,190,48));Path2D franja=new Path2D.Double();franja.moveTo(cx-r,yy);franja.curveTo(cx-r*.3,yy-5,cx+r*.25,yy+6,cx+r,yy-2);franja.lineTo(cx+r,yy+6);franja.curveTo(cx+r*.3,yy+12,cx-r*.2,yy+2,cx-r,yy+7);franja.closePath();x.fill(franja);}
                if(p.nombre.equals("JÚPITER")){x.setColor(new Color(160,67,40,180));x.fill(new Ellipse2D.Double(cx+r*.18,cy+r*.10,r*.45,r*.22));x.setColor(new Color(255,193,128,125));x.draw(new Ellipse2D.Double(cx+r*.18,cy+r*.10,r*.45,r*.22));}
            }
            if(p.nombre.equals("TIERRA")){
                x.setColor(new Color(71,150,80));Path2D tierra=new Path2D.Double();tierra.moveTo(cx-r*.72,cy-r*.1);tierra.curveTo(cx-r*.35,cy-r*.78,cx-r*.05,cy-r*.35,cx+r*.08,cy-r*.55);tierra.curveTo(cx+r*.5,cy-r*.48,cx+r*.25,cy-r*.12,cx+r*.66,cy+r*.02);tierra.curveTo(cx+r*.35,cy+r*.22,cx+r*.4,cy+r*.6,cx+r*.05,cy+r*.68);tierra.curveTo(cx-r*.18,cy+r*.38,cx-r*.35,cy+r*.2,cx-r*.72,cy-r*.1);tierra.closePath();x.fill(tierra);
                x.setColor(new Color(235,245,245,125));x.setStroke(new BasicStroke(2f));x.draw(new Arc2D.Double(cx-r*.65,cy-r*.45,r*1.3,r*.95,15,125,Arc2D.OPEN));
            }
            if(p.nombre.equals("MARTE")||p.nombre.equals("LUNA")){
                Random rand=new Random(p.nombre.hashCode());for(int i=0;i<8;i++){double ox=(rand.nextDouble()*1.35-.68)*r,oy=(rand.nextDouble()*1.35-.68)*r,cr=2+rand.nextDouble()*4;x.setColor(new Color(45,31,31,55));x.fill(new Ellipse2D.Double(cx+ox-cr,cy+oy-cr,cr*2,cr*1.5));x.setColor(new Color(255,208,170,45));x.draw(new Arc2D.Double(cx+ox-cr,cy+oy-cr,cr*2,cr*1.5,15,150,Arc2D.OPEN));}
            }
            // Sombreado nocturno y brillo atmosférico.
            x.setPaint(new GradientPaint(cx-r*.5f,cy-r*.8f,new Color(255,255,255,38),cx+r,cy+r,new Color(4,8,24,175)));x.fill(bola);x.setClip(null);
            x.setColor(new Color(175,218,255,210));x.setStroke(new BasicStroke(1.4f));x.draw(bola);
            // Anillo delantero de Saturno.
            if(p.nombre.equals("SATURNO")){x.setColor(new Color(229,193,132,220));x.setStroke(new BasicStroke(3f));x.draw(new Arc2D.Double(cx-r-15,cy-r*.28,r*2+30,r*.8,190,160,Arc2D.OPEN));}
            String t=p.nombre+" - "+p.seccion;x.setFont(fuentes.mono(Font.BOLD,9));FontMetrics fm=x.getFontMetrics();int tw=fm.stringWidth(t);boolean encima=cy<getHeight()/2;int yy=encima?cy-r-8:cy+r+16;int yCaja=encima?yy-fm.getAscent()-3:yy-fm.getAscent()-3;x.setColor(new Color(6,10,28,225));x.fillRoundRect(cx-tw/2-6,yCaja,tw+12,fm.getHeight()+6,9,9);x.setColor(Tema.TEXTO);x.drawString(t,cx-tw/2,yy);p.bounds.setBounds(cx-tw/2-10,Math.min(cy-r-6,yCaja-3),tw+20,(cy+r+6)-(Math.min(cy-r-6,yCaja-3))+6);
        }
    }

    private static final class Planeta {
        final CategoriaTienda categoria;final String nombre,seccion;final Color color;final double orbita,angulo,velocidad;final int diam;final Rectangle bounds=new Rectangle();
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

    private static BufferedImage componerAvatar(Set<String> ids) {
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
        final JButton comprar=Tema.botonTexto("COMPRAR");
        final JLabel disponibilidad=etiqueta("",fuentes.mono(Font.BOLD,10),Tema.SECUNDARIO);
        boolean adquirido, equipado, asequible;

        Tarjeta(CategoriaTienda categoria,String nombre,String descripcion,int precio,int valor,boolean unico,BufferedImage imagen,Runnable click){
            this.precio=precio;this.valor=valor;this.unico=unico;this.categoria=categoria;
            setOpaque(false);setBorder(new EmptyBorder(14,14,14,14));setLayout(new BorderLayout(8,8));
            if(imagen!=null){
                JLabel dibujo=new JLabel(new ImageIcon(escalar(imagen,112,126)));dibujo.setHorizontalAlignment(SwingConstants.CENTER);add(dibujo,BorderLayout.CENTER);setPreferredSize(new Dimension(145,194));
                JPanel datos=new JPanel();datos.setOpaque(false);datos.setLayout(new BoxLayout(datos,BoxLayout.Y_AXIS));
                JLabel n=etiqueta(nombre,fuentes.mono(Font.BOLD,10),Tema.TEXTO);n.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel p=etiqueta(precio+" ESTRELLAS",fuentes.mono(Font.BOLD,9),Tema.ESTRELLA);p.setAlignmentX(Component.CENTER_ALIGNMENT);
                datos.add(n);datos.add(Box.createVerticalStrut(3));datos.add(p);datos.add(disponibilidad);disponibilidad.setAlignmentX(Component.CENTER_ALIGNMENT);add(datos,BorderLayout.NORTH);
            } else {
                JPanel contenido=new JPanel();contenido.setOpaque(false);contenido.setLayout(new BoxLayout(contenido,BoxLayout.Y_AXIS));
                IconoExtra icono=new IconoExtra(categoria);icono.setAlignmentX(Component.CENTER_ALIGNMENT);contenido.add(icono);
                contenido.add(Box.createVerticalStrut(9));
                JLabel nombreLbl=etiqueta(nombre.toUpperCase(),fuentes.mono(Font.BOLD,17),Tema.TEXTO);nombreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);contenido.add(nombreLbl);
                String cantidad="+"+valor+(categoria==CategoriaTienda.TIEMPO?" SEGUNDOS":" PUNTOS");
                JLabel cantidadLbl=etiqueta(cantidad,fuentes.kiwiSoda(Font.BOLD,29),categoria==CategoriaTienda.TIEMPO?Tema.HIELO:Tema.ESTRELLA);cantidadLbl.setAlignmentX(Component.CENTER_ALIGNMENT);contenido.add(Box.createVerticalStrut(5));contenido.add(cantidadLbl);
                JLabel detalle=etiqueta("<html><div style='text-align:center;width:210px'>"+descripcion+"</div></html>",fuentes.mono(Font.PLAIN,11),Tema.SECUNDARIO);detalle.setAlignmentX(Component.CENTER_ALIGNMENT);contenido.add(Box.createVerticalStrut(6));contenido.add(detalle);
                JLabel costo=etiqueta("COSTO: "+precio+" ESTRELLAS",fuentes.mono(Font.BOLD,12),Tema.ESTRELLA);costo.setAlignmentX(Component.CENTER_ALIGNMENT);contenido.add(Box.createVerticalStrut(10));contenido.add(costo);
                disponibilidad.setAlignmentX(Component.CENTER_ALIGNMENT);contenido.add(Box.createVerticalStrut(5));contenido.add(disponibilidad);add(contenido,BorderLayout.CENTER);
                setPreferredSize(new Dimension(260,330));
            }
            comprar.setFont(fuentes.mono(Font.BOLD,11));comprar.addActionListener(e->click.run());add(comprar,BorderLayout.SOUTH);
        }
        private static BufferedImage escalar(BufferedImage fuente,int ancho,int alto){
            BufferedImage mini=new BufferedImage(ancho,alto,BufferedImage.TYPE_INT_ARGB);Graphics2D g=mini.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(fuente,0,0,ancho,alto,null);g.dispose();return mini;
        }
        void setSaldo(int saldo){asequible=saldo>=precio;actualizarBoton();if(adquirido)disponibilidad.setText(equipado?"EN USO":"COMPRADO");else if(asequible)disponibilidad.setText("SALDO SUFICIENTE");else disponibilidad.setText("TE FALTAN "+(precio-saldo)+" ESTRELLAS");disponibilidad.setForeground(asequible?Tema.HIELO:Tema.ERROR);}
        void setComprado(boolean b){adquirido=b;actualizarBoton();}
        void setEquipado(boolean b){equipado=b;actualizarBoton();}
        private void actualizarBoton(){comprar.setText(equipado?"RETIRAR":(adquirido?"EQUIPAR":"COMPRAR"));comprar.setEnabled(adquirido||asequible);}
        @Override protected void paintComponent(Graphics g){Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);x.setColor(new Color(8,13,32,195));x.fillRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);x.setColor(adquirido?Tema.ESTRELLA:Tema.LINEA);x.drawRoundRect(0,0,getWidth()-1,getHeight()-1,22,22);x.dispose();}
    }

    private static class IconoExtra extends JComponent {
        private final CategoriaTienda tipo;
        IconoExtra(CategoriaTienda tipo){this.tipo=tipo;setPreferredSize(new Dimension(74,74));setMaximumSize(new Dimension(74,74));}
        @Override protected void paintComponent(Graphics g){
            Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int d=64,cx=getWidth()/2,cy=getHeight()/2;
            Color acento=tipo==CategoriaTienda.TIEMPO?Tema.HIELO:Tema.ESTRELLA;
            x.setPaint(new RadialGradientPaint(new Point(cx,cy),d/2,new float[]{0f,1f},new Color[]{new Color(acento.getRed(),acento.getGreen(),acento.getBlue(),100),new Color(acento.getRed(),acento.getGreen(),acento.getBlue(),15)}));x.fill(new Ellipse2D.Double(cx-d/2,cy-d/2,d,d));x.setColor(acento);x.setStroke(new BasicStroke(3f));
            if(tipo==CategoriaTienda.TIEMPO){x.draw(new Ellipse2D.Double(cx-20,cy-20,40,40));x.drawLine(cx,cy-13,cx,cy);x.drawLine(cx,cy,cx+12,cy+7);x.fill(new Ellipse2D.Double(cx-3,cy-3,6,6));}
            else {Path2D estrella=new Path2D.Double();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5,r=i%2==0?23:10,px=cx+Math.cos(a)*r,py=cy+Math.sin(a)*r;if(i==0)estrella.moveTo(px,py);else estrella.lineTo(px,py);}estrella.closePath();x.fill(estrella);}
            x.dispose();
        }
    }
    private static JLabel etiqueta(String t,Font f,Color c){JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);return l;}
}


