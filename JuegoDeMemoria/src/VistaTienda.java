import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/** Pantalla de tienda integrada en la ventana principal. */
public class VistaTienda extends JPanel {
    public enum CategoriaTienda {
        ACCESORIOS("ACCESORIOS", "JÚPITER"), TIEMPO("TIEMPO EXTRA", "SATURNO"), PUNTOS("PUNTOS EXTRA", "MARTE");
        final String titulo, planeta;
        CategoriaTienda(String titulo, String planeta) { this.titulo=titulo; this.planeta=planeta; }
    }
    private final JLabel lblSaldo = etiqueta("0", fuentes.kiwiSoda(Font.BOLD, 26), Tema.ESTRELLA);
    private final JLabel lblMensaje = etiqueta(" ", fuentes.mono(Font.BOLD, 12), Tema.HIELO);
    private final JButton volver = Tema.botonTexto("← MENÚ PRINCIPAL");
    private final VistaPantallaPrincipal principal;
    private final Map<CategoriaTienda,JPanel> listas = new EnumMap<>(CategoriaTienda.class);
    private final Map<String,Tarjeta> articulos = new LinkedHashMap<>();
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
        saldoPanel.add(etiqueta("SALDO",fuentes.mono(Font.BOLD,12),Tema.SECUNDARIO)); saldoPanel.add(lblSaldo); saldoPanel.add(volver); p.add(saldoPanel,BorderLayout.EAST); return p;
    }

    private JPanel crearSistemaSolar() {
        JPanel p=new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel guia=etiqueta("ELIGE UN PLANETA PARA EXPLORAR SUS ARTÍCULOS",fuentes.mono(Font.PLAIN,12),Tema.SECUNDARIO); guia.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(guia,BorderLayout.NORTH); p.add(new MapaSolar(),BorderLayout.CENTER); return p;
    }

    private JPanel crearCatalogo(CategoriaTienda cat) {
        JPanel p=new JPanel(new BorderLayout(0,16)); p.setOpaque(false);
        JLabel titulo=etiqueta(cat.titulo,fuentes.kiwiSoda(Font.PLAIN,38),Tema.TEXTO);
        JLabel subt=etiqueta("Artículos disponibles en "+cat.planeta+"  •  compra con tus estrellas",fuentes.mono(Font.PLAIN,12),Tema.SECUNDARIO);
        JPanel top=new JPanel(new BorderLayout(0,8));top.setOpaque(false);JPanel labels=new JPanel();labels.setOpaque(false);labels.setLayout(new BoxLayout(labels,BoxLayout.Y_AXIS));titulo.setAlignmentX(Component.LEFT_ALIGNMENT);subt.setAlignmentX(Component.LEFT_ALIGNMENT);labels.add(titulo);labels.add(Box.createVerticalStrut(5));labels.add(subt);JButton atras=Tema.botonTexto("← SISTEMA SOLAR");atras.addActionListener(e->layout.show(paginas,"solar"));top.add(labels,BorderLayout.CENTER);top.add(atras,BorderLayout.EAST);
        JPanel lista=new JPanel();lista.setOpaque(false);lista.setLayout(new BoxLayout(lista,BoxLayout.Y_AXIS));JScrollPane scroll=new JScrollPane(lista);scroll.setOpaque(false);scroll.getViewport().setOpaque(false);scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(18);
        listas.put(cat,lista);if(cat==CategoriaTienda.ACCESORIOS){lista.add(avatarPreview);lista.add(Box.createVerticalStrut(18));}p.add(top,BorderLayout.NORTH);p.add(scroll,BorderLayout.CENTER);return p;
    }

    public void setAlComprar(Consumer<String> accion){alComprar=accion;}
    public void mostrarTienda(){layout.show(paginas,"solar");}
    public void setSaldo(int nuevo){saldo=nuevo;lblSaldo.setText(String.valueOf(nuevo));for(Tarjeta t:articulos.values())t.setAsequible(nuevo>=t.precio);}
    public void agregarArticulo(String id,CategoriaTienda cat,String nombre,String descripcion,int precio,boolean unico){Tarjeta t=new Tarjeta(cat,nombre,descripcion,precio,unico,()->{if(alComprar!=null)alComprar.accept(id);});articulos.put(id,t);listas.get(cat).add(t);listas.get(cat).add(Box.createVerticalStrut(12));t.setAsequible(saldo>=precio);}
    public void marcarComprado(String id){Tarjeta t=articulos.get(id);if(t!=null)t.setComprado(true);}
    public void setAccesoriosEquipados(Set<String> ids){avatarPreview.setAccesorios(ids);}
    public void limpiarTienda(){
        articulos.clear();
        for(Map.Entry<CategoriaTienda,JPanel> e:listas.entrySet()){
            JPanel p=e.getValue();p.removeAll();
            if(e.getKey()==CategoriaTienda.ACCESORIOS){p.add(avatarPreview);p.add(Box.createVerticalStrut(18));}
            p.revalidate();p.repaint();
        }
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
    private static final class AvatarPreview extends JPanel {
        private Set<String> accesorios = Collections.emptySet();
        AvatarPreview(){setOpaque(false);setPreferredSize(new Dimension(700,220));setMaximumSize(new Dimension(Integer.MAX_VALUE,220));setAlignmentX(Component.LEFT_ALIGNMENT);}
        void setAccesorios(Set<String> ids){accesorios=new LinkedHashSet<>(ids);repaint();}
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int cx=getWidth()/2,cy=getHeight()/2+8;x.setColor(new Color(4,12,34,155));x.fillRoundRect(4,4,getWidth()-8,getHeight()-8,24,24);x.setColor(Tema.LINEA);x.drawRoundRect(4,4,getWidth()-9,getHeight()-9,24,24);
            x.setFont(fuentes.mono(Font.BOLD,12));x.setColor(Tema.HIELO);x.drawString("TU AVATAR - ACCESORIOS EQUIPADOS",24,30);
            // Mochila y capa quedan detrás del astronauta.
            if(accesorios.contains("accesorio_capa")){x.setColor(new Color(105,91,201));Path2D capa=new Path2D.Double();capa.moveTo(cx-28,cy+12);capa.curveTo(cx-74,cy+32,cx-62,cy+77,cx-50,cy+87);capa.lineTo(cx+48,cy+87);capa.curveTo(cx+67,cy+48,cx+57,cy+28,cx+28,cy+12);capa.closePath();x.fill(capa);}
            if(accesorios.contains("accesorio_mochila")){x.setColor(new Color(205,89,45));x.fillRoundRect(cx+28,cy+15,34,58,13,13);x.setColor(new Color(255,190,75));x.fillRoundRect(cx+35,cy+59,20,18,8,8);}
            // Traje, casco y cara del avatar base.
            x.setPaint(new GradientPaint(cx-38,cy+10,new Color(0xF4F7FF),cx+38,cy+95,new Color(0xA9BCFF)));x.fillRoundRect(cx-42,cy+10,84,88,38,38);x.setColor(new Color(0xD4E5FF));x.fillRoundRect(cx-29,cy+23,58,40,22,22);
            x.setPaint(new GradientPaint(cx-38,cy-52,new Color(0xFFF0D1),cx+38,cy+25,new Color(0xDCA77D)));x.fill(new Ellipse2D.Double(cx-39,cy-61,78,78));
            if(accesorios.contains("accesorio_casco")){x.setColor(new Color(178,220,255,105));x.fill(new Arc2D.Double(cx-48,cy-70,96,90,0,180,Arc2D.PIE));x.setColor(new Color(212,236,255,210));x.setStroke(new BasicStroke(3));x.draw(new Arc2D.Double(cx-48,cy-70,96,90,0,180,Arc2D.OPEN));}
            if(accesorios.contains("accesorio_visor")){x.setColor(new Color(36,96,139,220));x.fillRoundRect(cx-38,cy-24,76,24,13,13);x.setColor(new Color(153,232,255,220));x.setStroke(new BasicStroke(2));x.drawLine(cx-28,cy-18,cx-4,cy-18);}
            if(accesorios.contains("accesorio_antenas")){x.setColor(new Color(196,214,245));x.setStroke(new BasicStroke(3));x.drawLine(cx-18,cy-56,cx-28,cy-81);x.drawLine(cx+18,cy-56,cx+28,cy-81);x.setColor(new Color(255,218,112));x.fill(new Ellipse2D.Double(cx-33,cy-87,11,11));x.fill(new Ellipse2D.Double(cx+22,cy-87,11,11));}
            if(accesorios.contains("accesorio_halo")){x.setColor(new Color(255,218,112,220));x.setStroke(new BasicStroke(4));x.draw(new Ellipse2D.Double(cx-43,cy-88,86,19));}
            x.setColor(Tema.SECUNDARIO);x.setFont(fuentes.mono(Font.PLAIN,11));x.drawString(accesorios.isEmpty()?"Compra accesorios para personalizarlo":"Accesorios aplicados automáticamente",24,getHeight()-20);
            x.dispose();
        }
    }

    private static class Tarjeta extends JPanel {
        final int precio;final boolean unico;final JButton comprar=Tema.botonTexto("COMPRAR");boolean adquirido;
        Tarjeta(CategoriaTienda c,String nombre,String desc,int precio,boolean unico,Runnable click){this.precio=precio;this.unico=unico;setOpaque(false);setBorder(new EmptyBorder(15,18,15,18));setLayout(new BorderLayout(14,10));JLabel n=etiqueta(nombre,fuentes.mono(Font.BOLD,16),Tema.TEXTO),d=etiqueta("<html><body style='width:450px'>"+desc+"</body></html>",fuentes.mono(Font.PLAIN,12),Tema.SECUNDARIO);JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));info.add(n);info.add(Box.createVerticalStrut(6));info.add(d);JPanel pie=new JPanel(new BorderLayout());pie.setOpaque(false);pie.add(etiqueta(precio+" ESTRELLAS",fuentes.mono(Font.BOLD,12),Tema.ESTRELLA),BorderLayout.WEST);pie.add(comprar,BorderLayout.EAST);comprar.addActionListener(e->click.run());add(info,BorderLayout.CENTER);add(pie,BorderLayout.SOUTH);}
        void setAsequible(boolean b){if(!adquirido)comprar.setEnabled(b);}void setComprado(boolean b){if(unico){adquirido=b;comprar.setText(b?"COMPRADO":"COMPRAR");comprar.setEnabled(!b);}}
        protected void paintComponent(Graphics g){Graphics2D x=(Graphics2D)g.create();x.setColor(new Color(8,13,32,190));x.fillRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);x.setColor(Tema.LINEA);x.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);x.dispose();}
    }
    private static JLabel etiqueta(String t,Font f,Color c){JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);return l;}
}


