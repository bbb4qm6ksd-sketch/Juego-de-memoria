
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;



public final class DialogoAstro {

    // Evita crear instancias: todos los métodos de esta clase son estáticos.
    private DialogoAstro() { }

    // Muestra una pregunta y devuelve true si el usuario acepta.
    public static boolean confirmar(Window dueno, String titulo, String mensaje,
                                    String aceptar, String cancelar) {
        return Boolean.TRUE.equals(mostrar(dueno, titulo, mensaje, null, aceptar, cancelar, false));
    }

    // Solicita texto; devuelve lo ingresado o null si se cancela o falla el diálogo.
    public static String pedirTexto(Window dueno, String titulo, String mensaje,
                                    String inicial, String aceptar, String cancelar) {
        Object resultado = mostrar(dueno, titulo, mensaje, inicial == null ? "" : inicial,
                aceptar, cancelar, true);
        return resultado instanceof String ? (String) resultado : null;
    }

    // Muestra un mensaje informativo que el usuario puede cerrar.
    public static void informar(Window dueno, String titulo, String mensaje) {
        mostrar(dueno, titulo, mensaje, null, "ENTENDIDO", null, false);
    }

    // Construye y presenta el diálogo; pedirTexto determina si incluye un campo de entrada.
    private static Object mostrar(Window dueno, String titulo, String mensaje, String inicial,
                                  String aceptar, String cancelar, boolean pedirTexto) {
        // Swing debe modificarse en su hilo de eventos; si estamos en otro hilo, se delega allí.
        if (!SwingUtilities.isEventDispatchThread()) {
            final Object[] resultado = new Object[1];
            try {
                SwingUtilities.invokeAndWait(() -> resultado[0] = mostrar(
                        dueno, titulo, mensaje, inicial, aceptar, cancelar, pedirTexto));
            } catch (Exception e) {
                return null;
            }
            return resultado[0];
        }

        // Encuentra la raíz de la ventana, donde se instalará la capa del diálogo.
        JRootPane raiz = dueno instanceof RootPaneContainer
                ? ((RootPaneContainer) dueno).getRootPane() : SwingUtilities.getRootPane(dueno);
        if (raiz == null) return null;

        // Conserva el contenido previo de la ventana para restaurarlo al cerrar el diálogo.
        SecondaryLoop bucle = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
        Object[] resultado = {null};
        Component capaAnterior = raiz.getGlassPane();
        boolean capaAnteriorVisible = capaAnterior.isVisible();
        JButton botonAnterior = raiz.getDefaultButton();

        // Prepara el panel principal con colores, borde y distribución vertical.
        JPanel contenido = new JPanel();
        contenido.setOpaque(true);
        contenido.setBackground(new Color(0x111A38));
        contenido.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(169, 188, 255, 180), 1, true),
                new EmptyBorder(26, 32, 24, 32)));
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));

        // Encabezado decorativo, título y mensaje central del diálogo.
        JLabel kicker = new JLabel("ASTROMATCH  ✦  CENTRO DE CONTROL");
        kicker.setFont(fuentes.mono(Font.BOLD, 10));
        kicker.setForeground(Tema.HIELO);
        JLabel encabezado = new JLabel(titulo, SwingConstants.CENTER);
        encabezado.setFont(fuentes.kiwiSoda(Font.PLAIN, 30));
        encabezado.setForeground(Tema.TEXTO);
        encabezado.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel cuerpo = new JLabel("<html><div style='width:430px;text-align:center'>"
                + escapar(mensaje) + "</div></html>");
        cuerpo.setFont(Tema.sans(Font.PLAIN, 15));
        cuerpo.setForeground(Tema.SECUNDARIO);
        cuerpo.setAlignmentX(Component.CENTER_ALIGNMENT);

        contenido.add(kicker);
        contenido.add(Box.createVerticalStrut(12));
        contenido.add(encabezado);
        contenido.add(Box.createVerticalStrut(14));
        contenido.add(cuerpo);

        // El campo de texto solo se agrega en los diálogos que solicitan una respuesta.
        JTextField campo = null;
        if (pedirTexto) {
            campo = new JTextField(inicial, 24);
            campo.setFont(Tema.sans(Font.PLAIN, 17));
            campo.setForeground(Tema.TEXTO);
            campo.setCaretColor(Tema.ESTRELLA);
            campo.setBackground(new Color(0x0A1028));
            campo.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Tema.LINEA, 1, true),
                    new EmptyBorder(11, 14, 11, 14)));
            campo.setMaximumSize(new Dimension(470, 48));
            contenido.add(Box.createVerticalStrut(20));
            contenido.add(campo);
        }

        // Crea los botones y los organiza en la zona de acciones.
        JButton btnAceptar = new VistaTablero.BotonPill(aceptar, true);
        JButton btnCancelar = cancelar == null ? null : Tema.botonTexto(cancelar);
        btnAceptar.setPreferredSize(new Dimension(210, 48));
        btnAceptar.setMaximumSize(new Dimension(240, 48));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        acciones.setOpaque(false);
        if (btnCancelar != null) acciones.add(btnCancelar);
        acciones.add(btnAceptar);
        contenido.add(Box.createVerticalStrut(20));
        contenido.add(acciones);

        // Glass pane semitransparente que oscurece la ventana detrás del contenido.
        JPanel capa = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(5, 9, 24, 190));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        capa.setOpaque(false);
        capa.add(contenido);

        final JTextField campoFinal = campo;
        // Cierra el diálogo, restaura la capa y el botón predeterminado de la ventana.
        Runnable cerrar = () -> {
            capa.setVisible(false);
            raiz.setGlassPane(capaAnterior);
            capaAnterior.setVisible(capaAnteriorVisible);
            raiz.setDefaultButton(botonAnterior);
            raiz.revalidate();
            raiz.repaint();
            bucle.exit();
        };
        // Al aceptar, valida el texto si corresponde y guarda el resultado.
        btnAceptar.addActionListener(e -> {
            if (pedirTexto) {
                String valor = campoFinal.getText().trim();
                if (valor.isEmpty()) {
                    campoFinal.requestFocusInWindow();
                    return;
                }
                resultado[0] = valor;
            } else {
                resultado[0] = Boolean.TRUE;
            }
            cerrar.run();
        });
        if (btnCancelar != null) btnCancelar.addActionListener(e -> cerrar.run());
        if (campoFinal != null) campoFinal.addActionListener(e -> btnAceptar.doClick());

        // Muestra la capa, enfoca el control inicial y espera hasta que el diálogo se cierre.
        raiz.setGlassPane(capa);
        capa.setVisible(true);
        raiz.setDefaultButton(btnAceptar);
        SwingUtilities.invokeLater(() -> {
            if (campoFinal != null) campoFinal.requestFocusInWindow();
            else btnAceptar.requestFocusInWindow();
        });
        bucle.enter();
        return resultado[0];
    }

    // Escapa caracteres especiales para que el mensaje se interprete como texto dentro del HTML.
    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
