package Objetos;

import GameGFX.Animacion;
import Objetos.Utilidad.Handler;
import Objetos.Utilidad.ObjetosID;
import mariotest.Mariotest;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Clase DiegoKong - Jefe principal que lanza barriles
 * Similar a Donkey Kong del juego original
 * * @author LENOVO
 */
public class DiegoKong extends GameObjetos {
    
    // Dimensiones de Diego Kong (más grande que el jugador)
    private static final float WIDTH = 48;
    private static final float HEIGHT = 32;
    
    // Componentes
    private Handler handler;
    private BufferedImage[] dkSprites;
    
    // Animaciones
    private Animacion dkReposo;           // Parado sin hacer nada
    private Animacion dkAgarra;           // Agarra un barril
    private Animacion dkLanza;            // Lanza el barril
    private Animacion dkGolpeaPecho;      // Golpea su pecho (celebración)
    
    private Animacion animacionActual;
    
    // Estados
    private EstadoDK estado;
    private boolean mirandoDerecha = true;
    
    // Control de lanzamiento de barriles
    private int ticksDesdeUltimoLanzamiento = 0;
    private int ticksEntrelanzamientos = 180;  // 3 segundos base
    private boolean preparandoLanzamiento = false;
    private int ticksAnimacionLanzar = 0;
    private static final int DURACION_ANIMACION_LANZAR = 30; // 0.5 segundos
    
    // Configuración
    private static final int TICKS_MIN_LANZAMIENTO = 120;  // 2 segundos
    private static final int TICKS_MAX_LANZAMIENTO = 240;  // 4 segundos
    
    // Posición de spawn de barriles (relativa a DK)
    private float offsetBarrilX = 20f;
    private float offsetBarrilY = 10f;
    
    /**
     * Estados de Diego Kong
     */
    public enum EstadoDK {
        REPOSO,           // Parado esperando
        AGARRANDO,        // Agarrando un barril
        LANZANDO,         // Lanzando el barril
        GOLPEANDO_PECHO,  // Celebrando/provocando
        ENOJADO           // Cuando el jugador se acerca mucho
    }
    
    /**
     * Constructor de Diego Kong
     */
    public DiegoKong(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.DiegoKong, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.estado = EstadoDK.REPOSO;
        
        // Cargar sprites y animaciones
        cargarSprites();
        inicializarAnimaciones();
        
        System.out.println("[DIEGO KONG] Creado en (" + x + ", " + y + ")");
    }
    
    /**
     * Carga los sprites de Diego Kong desde Texturas
     */
    private void cargarSprites() {
        try {
            dkSprites = Mariotest.getTextura().getDiegoKongSprites();
          
            if (dkSprites != null && dkSprites.length > 0) {
                System.out.println("[DIEGO KONG] Sprites cargados: " + dkSprites.length);
            } else {
                 System.err.println("[ERROR] Los sprites de Diego Kong no se cargaron o el array está vacío.");
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] No se pudieron cargar sprites de Diego Kong: " + e.getMessage());
        }
    }
    
    /**
     * Inicializa las animaciones de Diego Kong
     */
    // FIX: Updated the animation logic to use the 8 loaded sprites correctly.
    private void inicializarAnimaciones() {
        if (dkSprites == null || dkSprites.length < 8) {
            System.err.println("[ERROR] No hay suficientes sprites para las animaciones de DK. Se necesitan 8, se encontraron: " + (dkSprites != null ? dkSprites.length : 0));
            return;
        }
        
        // Fila 1 (Índices 0-3): Reposo y Golpear Pecho
        dkReposo = new Animacion(15, dkSprites[0], dkSprites[1]);
        dkGolpeaPecho = new Animacion(8, dkSprites[2], dkSprites[3], dkSprites[2]);
        
        // Fila 2 (Índices 4-7): Agarrar y Lanzar
        dkAgarra = new Animacion(8, dkSprites[5], dkSprites[5]);
        dkLanza = new Animacion(6, dkSprites[4],dkSprites[4], dkSprites[6], dkSprites[6]);
        
        animacionActual = dkReposo;
        
        System.out.println("[DIEGO KONG] Animaciones inicializadas correctamente.");
    }

    @Override
    public void tick() {
        // Diego Kong no se mueve (es estático en su plataforma)
        // Solo actualiza su estado y lanza barriles
        
        // Actualizar animación actual
        if (animacionActual != null) {
            animacionActual.runAnimacion();
        }
        
        // Máquina de estados
        switch (estado) {
            case REPOSO:
                tickReposo();
                break;
                
            case AGARRANDO:
                tickAgarrando();
                break;
                
            case LANZANDO:
                tickLanzando();
                break;
                
            case GOLPEANDO_PECHO:
                tickGolpeandoPecho();
                break;
                
            case ENOJADO:
                tickEnojado();
                break;
        }
        
        // Verificar si el jugador está cerca (opcional: cambiar a ENOJADO)
        verificarProximidadJugador();
    }
    
    /**
     * Estado: Reposo - Esperando para lanzar siguiente barril
     */
    private void tickReposo() {
        animacionActual = dkReposo;
        ticksDesdeUltimoLanzamiento++;
        
        // Es momento de lanzar un barril?
        if (ticksDesdeUltimoLanzamiento >= ticksEntrelanzamientos) {
            iniciarLanzamiento();
        }
    }
    
    /**
     * Estado: Agarrando - Preparando el barril para lanzar
     */
    private void tickAgarrando() {
        animacionActual = dkAgarra;
        ticksAnimacionLanzar++;
        
        // Después de agarrar, pasar a lanzar
        if (ticksAnimacionLanzar >= 15) { // 0.25 segundos
            estado = EstadoDK.LANZANDO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    /**
     * Estado: Lanzando - Lanza el barril
     */
    private void tickLanzando() {
        animacionActual = dkLanza;
        ticksAnimacionLanzar++;
        
        // En el frame correcto, spawner el barril
        if (ticksAnimacionLanzar == 10) { // Medio de la animación
            //lanzarBarril();
        }
        
        // Terminar animación y volver a reposo
        if (ticksAnimacionLanzar >= DURACION_ANIMACION_LANZAR) {
            ticksAnimacionLanzar = 0;
            ticksDesdeUltimoLanzamiento = 0;
            
            // A veces golpea el pecho después de lanzar
            if (Math.random() < 0.3) { // 30% probabilidad
                estado = EstadoDK.GOLPEANDO_PECHO;
            } else {
                estado = EstadoDK.REPOSO;
            }
            
            // Randomizar próximo lanzamiento
            ticksEntrelanzamientos = TICKS_MIN_LANZAMIENTO + 
                (int)(Math.random() * (TICKS_MAX_LANZAMIENTO - TICKS_MIN_LANZAMIENTO));
        }
    }
    
    /**
     * Estado: Golpeando pecho - Celebración/provocación
     */
    private void tickGolpeandoPecho() {
        animacionActual = dkGolpeaPecho;
        ticksAnimacionLanzar++;
        
        // Después de golpear el pecho, volver a reposo
        if (ticksAnimacionLanzar >= 40) { // ~0.67 segundos
            estado = EstadoDK.REPOSO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    /**
     * Estado: Enojado - Cuando el jugador se acerca mucho
     */
    private void tickEnojado() {
        animacionActual = dkGolpeaPecho; // Usar animación de golpear pecho
        ticksAnimacionLanzar++;
        
        // Lanzar barriles más rápido cuando está enojado
        if (ticksAnimacionLanzar >= 60) {
            iniciarLanzamiento();
            ticksAnimacionLanzar = 0;
        }
        
        // Volver a estado normal si el jugador se aleja
        if (!jugadorCerca()) {
            estado = EstadoDK.REPOSO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    /**
     * Inicia la secuencia de lanzamiento de barril
     */
    private void iniciarLanzamiento() {
        estado = EstadoDK.AGARRANDO;
        ticksAnimacionLanzar = 0;
        System.out.println("[DIEGO KONG] Iniciando lanzamiento de barril...");
    }
    
    /**
     * Lanza un barril hacia el jugador
     */
    
    private void lanzarBarril() {
        // Calcular posición de spawn del barril
        float barrilX = getX() + (mirandoDerecha ? offsetBarrilX : -offsetBarrilX);
        float barrilY = getY() + offsetBarrilY;
        
        // Determinar dirección del barril (hacia donde mira DK)
        int direccion = mirandoDerecha ? 1 : -1;
        
        // Crear el barril
        Barril barril = new Barril(barrilX, barrilY, 2, handler, direccion);
        handler.addObj(barril);
        
        System.out.println("[DIEGO KONG] ¡Barril lanzado en (" + barrilX + ", " + barrilY + ") dirección: " + 
                          (direccion > 0 ? "DERECHA" : "IZQUIERDA") + "!");
    }
    
    /**
     * Verifica si el jugador está cerca de Diego Kong
     */
    private void verificarProximidadJugador() {
        Player player = handler.getPlayer();
        if (player == null) return;
        
        // Calcular distancia al jugador
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        // Si el jugador está muy cerca y en la misma altura aproximada
        if (distanciaX < 200 && distanciaY < 100 && estado == EstadoDK.REPOSO) {
            // Cambiar a estado enojado
            if (Math.random() < 0.1) { // 10% probabilidad por tick cuando está cerca
                estado = EstadoDK.ENOJADO;
                ticksAnimacionLanzar = 0;
                System.out.println("[DIEGO KONG] ¡Se ha puesto ENOJADO!");
            }
        }
        
        // Actualizar dirección para mirar al jugador
        mirandoDerecha = player.getX() > getX();
    }
    
    /**
     * Verifica si el jugador está cerca
     */
    private boolean jugadorCerca() {
        Player player = handler.getPlayer();
        if (player == null) return false;
        
        float distanciaX = Math.abs(player.getX() - getX());
        float distanciaY = Math.abs(player.getY() - getY());
        
        return distanciaX < 200 && distanciaY < 100;
    }
    
    @Override
    public void aplicarGravedad() {
        // Diego Kong no tiene gravedad (está fijo en su plataforma)
    }

    @Override
    public void render(Graphics g) {
        if (animacionActual != null) {
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
        } else {
            // Placeholder visual si las animaciones fallan
            g.setColor(new Color(89, 47, 20));
            g.fillRect((int) getX(), (int) getY(), 
                      (int) getWidth(), (int) getHeight());
            
            g.setColor(Color.YELLOW);
            g.drawString("DK", (int) getX() + 15, (int) getY() - 5);
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
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Fuerza a Diego Kong a lanzar un barril inmediatamente
     */
    public void forzarLanzamiento() {
        if (estado == EstadoDK.REPOSO) {
            iniciarLanzamiento();
        }
    }
    
    /**
     * Cambia la velocidad de lanzamiento de barriles
     */
    public void setVelocidadLanzamiento(int ticksMin, int ticksMax) {
        if (ticksMin > 0 && ticksMax > ticksMin) {
            ticksEntrelanzamientos = ticksMin + 
                (int)(Math.random() * (ticksMax - ticksMin));
        }
    }
    
    /**
     * Activa el modo enojado manualmente
     */
    public void activarModoEnojado() {
        if (estado == EstadoDK.REPOSO) {
            estado = EstadoDK.ENOJADO;
            ticksAnimacionLanzar = 0;
        }
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public EstadoDK getEstado() {
        return estado;
    }
    
    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }
    
    public void setMirandoDerecha(boolean mirandoDerecha) {
        this.mirandoDerecha = mirandoDerecha;
    }
    
    public int getTicksParaProximoLanzamiento() {
        return ticksEntrelanzamientos - ticksDesdeUltimoLanzamiento;
    }
}