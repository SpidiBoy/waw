package Objetos;

import GameGFX.Animacion;
import Objetos.Utilidad.Handler;
import Objetos.Utilidad.ObjetosID;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import mariotest.Mariotest;

/**
 * Clase Princesa - NPC que debe ser rescatado
 * Similar a Pauline del Donkey Kong original
 * 
 * @author LENOVO
 */
public class Princesa extends GameObjetos {
    
    // Dimensiones de la princesa
    private static final float WIDTH = 16;
    private static final float HEIGHT = 32;
    
    // Componentes
    private Handler handler;
    private BufferedImage[] princesaSprites;
    
    // Animaciones
    private Animacion princesaEspera;      // Esperando ser rescatada
    private Animacion princesaPideAyuda;   // Pidiendo ayuda (brazos arriba)
    private Animacion animacionActual;
    
    // Estados
    private EstadoPrincesa estado;
    private boolean mirandoDerecha = true;
    
    // Control de animación
    private int ticksDesdeUltimaAnimacion = 0;
    private int ticksEntreAnimaciones = 160;  // 2 segundos
    private static final int DURACION_PEDIR_AYUDA = 60;  // 1 segundo
    
    // Flag de rescate
    private boolean rescatada = false;
    
    /**
     * Estados de la Princesa
     */
    public enum EstadoPrincesa {
        ESPERANDO,        // Esperando ser rescatada
        PIDIENDO_AYUDA,   // Levantando brazos pidiendo ayuda
        RESCATADA,        // Ya fue rescatada
        EN_PELIGRO        // Diego Kong está cerca
    }
    
    /**
     * Constructor de la Princesa
     */
    public Princesa(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.Princesa, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.estado = EstadoPrincesa.ESPERANDO;
        
        // Cargar sprites y animaciones
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[PRINCESA] Creada en (" + x + ", " + y + ")");
    }
    
    /**
     * Carga los sprites de la princesa
     */
    private void cargarSprites() {
        try {
            // Aquí puedes cargar sprites reales desde Texturas
            princesaSprites = Mariotest.getTextura().getPrincesaSprites();
            
            System.out.println("[PRINCESA] Sprites cargados: " + princesaSprites.length);
            
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudieron cargar sprites de Princesa: " + e.getMessage());
            princesaSprites = crearSpritesPlaceholder();
        }
    }
    
    /**
     * Crea sprites placeholder para la princesa
     */
    private BufferedImage[] crearSpritesPlaceholder() {
        BufferedImage[] sprites = new BufferedImage[4];
        
        for (int i = 0; i < 4; i++) {
            sprites[i] = new BufferedImage(16, 24, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = sprites[i].createGraphics();
            
            // Vestido rosa
            g.setColor(new Color(255, 192, 203));
            g.fillRect(2, 8, 12, 14);
            
            // Cabeza (piel)
            g.setColor(new Color(255, 220, 177));
            g.fillOval(4, 2, 8, 8);
            
            // Cabello rubio
            g.setColor(new Color(255, 215, 0));
            g.fillRect(3, 1, 10, 4);
            
            // Corona
            g.setColor(new Color(255, 215, 0));
            g.fillRect(5, 0, 6, 2);
            
            // Brazos (diferente posición según frame)
            g.setColor(new Color(255, 220, 177));
            if (i >= 2) {
                // Brazos arriba (pidiendo ayuda)
                g.fillRect(1, 6, 3, 4);
                g.fillRect(12, 6, 3, 4);
            } else {
                // Brazos abajo
                g.fillRect(1, 10, 3, 4);
                g.fillRect(12, 10, 3, 4);
            }
            
            g.dispose();
        }
        
        return sprites;
    }
    
    /**
     * Inicializa las animaciones de la princesa
     */
    private void inicializarAnimaciones() {
        if (princesaSprites == null || princesaSprites.length < 4) {
            System.err.println("[ERROR] No hay suficientes sprites para las animaciones de Princesa");
            return;
        }
        
        // Animación de espera (primeros 2 frames)
        princesaEspera = new Animacion(15, princesaSprites[0], princesaSprites[1]);
        
        // Animación de pedir ayuda (últimos 2 frames con brazos arriba)
        princesaPideAyuda = new Animacion(8, princesaSprites[2], princesaSprites[3]);
        
        animacionActual = princesaEspera;
        
        System.out.println("[PRINCESA] Animaciones inicializadas correctamente.");
    }

    @Override
    public void tick() {
        // La princesa no se mueve (es estática)
        // Solo actualiza su estado y animaciones
        
        // Actualizar animación actual
        if (animacionActual != null) {
            animacionActual.runAnimacion();
        }
        
        // Máquina de estados
        switch (estado) {
            case ESPERANDO:
                tickEsperando();
                break;
                
            case PIDIENDO_AYUDA:
                tickPidiendoAyuda();
                break;
                
            case RESCATADA:
                tickRescatada();
                break;
                
            case EN_PELIGRO:
                tickEnPeligro();
                break;
        }
        
        // Verificar si el jugador está cerca
        verificarProximidadJugador();
        
        // Verificar si Diego Kong está cerca
        verificarProximidadDiegoKong();
    }
    
    /**
     * Estado: Esperando - Animación normal de espera
     */
    private void tickEsperando() {
        animacionActual = princesaEspera;
        ticksDesdeUltimaAnimacion++;
        
        // Ocasionalmente pedir ayuda
        if (ticksDesdeUltimaAnimacion >= ticksEntreAnimaciones) {
            estado = EstadoPrincesa.PIDIENDO_AYUDA;
            ticksDesdeUltimaAnimacion = 0;
        }
    }
    
    /**
     * Estado: Pidiendo ayuda - Levanta los brazos
     */
    private void tickPidiendoAyuda() {
        animacionActual = princesaPideAyuda;
        ticksDesdeUltimaAnimacion++;
        
        // Después de pedir ayuda, volver a esperar
        if (ticksDesdeUltimaAnimacion >= DURACION_PEDIR_AYUDA) {
            estado = EstadoPrincesa.ESPERANDO;
            ticksDesdeUltimaAnimacion = 0;
            
            // Randomizar próxima vez que pide ayuda
            ticksEntreAnimaciones = 60 + (int)(Math.random() * 180); // 1-4 segundos
        }
    }
    
    /**
     * Estado: Rescatada - El jugador llegó hasta ella
     */
    private void tickRescatada() {
        animacionActual = princesaPideAyuda; // Brazos arriba en celebración
        // Aquí podrías agregar efectos especiales, sonidos, etc.
    }
    
    /**
     * Estado: En peligro - Diego Kong está cerca
     */
    private void tickEnPeligro() {
        animacionActual = princesaPideAyuda; // Pide ayuda constantemente
        ticksDesdeUltimaAnimacion++;
        
        // Si DK se aleja, volver a esperar
        if (ticksDesdeUltimaAnimacion >= 120 && !diegoKongCerca()) {
            estado = EstadoPrincesa.ESPERANDO;
            ticksDesdeUltimaAnimacion = 0;
        }
    }
    
    /**
     * Verifica si el jugador está cerca de la princesa
     */
    private void verificarProximidadJugador() {
        if (rescatada) return;
        
        Player player = handler.getPlayer();
        if (player == null) return;
        
        // Calcular distancia al jugador
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        // Si el jugador está muy cerca, la rescata
        if (distanciaX < 30 && distanciaY < 30) {
            rescatar();
        }
        
        // Si está cerca, mirar hacia el jugador
        if (distanciaX < 100) {
            mirandoDerecha = player.getX() > getX();
        }
    }
    
    /**
     * Verifica si Diego Kong está cerca
     */
    private void verificarProximidadDiegoKong() {
        if (rescatada) return;
        
        boolean dkCerca = false;
        
        for (GameObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.DiegoKong) {
                float distanciaX = Math.abs(obj.getX() - getX());
                float distanciaY = Math.abs(obj.getY() - getY());
                
                if (distanciaX < 150 && distanciaY < 150) {
                    dkCerca = true;
                    break;
                }
            }
        }
        
        // Cambiar estado según proximidad de DK
        if (dkCerca && estado == EstadoPrincesa.ESPERANDO) {
            estado = EstadoPrincesa.EN_PELIGRO;
            ticksDesdeUltimaAnimacion = 0;
        }
    }
    
    /**
     * Verifica si Diego Kong está cerca
     */
    private boolean diegoKongCerca() {
        for (GameObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.DiegoKong) {
                float distanciaX = Math.abs(obj.getX() - getX());
                float distanciaY = Math.abs(obj.getY() - getY());
                
                if (distanciaX < 150 && distanciaY < 150) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Rescata a la princesa
     */
    public void rescatar() {
        if (!rescatada) {
            rescatada = true;
            estado = EstadoPrincesa.RESCATADA;
            System.out.println("[PRINCESA] ¡Ha sido rescatada!");
            
            // Aquí podrías:
            // - Reproducir sonido de victoria
            // - Mostrar mensaje de nivel completado
            // - Sumar puntos bonus
            // - Iniciar secuencia de final de nivel
        }
    }
    
    @Override
    public void aplicarGravedad() {
        // La princesa no tiene gravedad (está fija en su plataforma)
    }

    @Override
    public void render(Graphics g) {
        if (animacionActual != null && princesaSprites != null) {
            // Renderizar con dirección
            if (mirandoDerecha) {
                animacionActual.drawAnimacion(g, 
                    (int) getX(), (int) getY(), 
                    (int) getWidth(), (int) getHeight()
                );
            } else {
                // Voltear horizontalmente
                animacionActual.drawAnimacion(g, 
                    (int) (getX() + getWidth()), (int) getY(), 
                    (int) -getWidth(), (int) getHeight()
                );
            }
            
            // Dibujar texto "HELP!" cuando pide ayuda
            if (estado == EstadoPrincesa.PIDIENDO_AYUDA || estado == EstadoPrincesa.EN_PELIGRO) {
                g.setColor(Color.WHITE);
                g.drawString("AYUDA!", (int) getX() + 45, (int) getY() - -20);
            }
            
        } else {
            // Placeholder visual si las animaciones fallan
            // Vestido rosa
            g.setColor(new Color(255, 192, 203));
            g.fillRect((int) getX() + 2, (int) getY() + 8, 
                      (int) getWidth() - 4, (int) getHeight() - 10);
            
            // Cabeza
            g.setColor(new Color(255, 220, 177));
            g.fillOval((int) getX() + 4, (int) getY() + 2, 
                      (int) getWidth() - 8, 8);
            
            // Corona
            g.setColor(Color.YELLOW);
            g.fillRect((int) getX() + 5, (int) getY(), 6, 2);
            
            // Texto "P" de princesa
            g.setColor(Color.WHITE);
            g.drawString("P", (int) getX() + 6, (int) getY() + 7);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            (int) getX(),
            (int) getY(),
            (int) getWidth(),
            (int) getHeight()
        );
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public EstadoPrincesa getEstado() {
        return estado;
    }
    
    public boolean isRescatada() {
        return rescatada;
    }
    
    public void setRescatada(boolean rescatada) {
        this.rescatada = rescatada;
        if (rescatada) {
            estado = EstadoPrincesa.RESCATADA;
        }
    }
    
    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }
    
    public void setMirandoDerecha(boolean mirandoDerecha) {
        this.mirandoDerecha = mirandoDerecha;
    }
}