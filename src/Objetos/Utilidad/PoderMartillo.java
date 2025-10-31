package Objetos.Utilidad;

import Objetos.*;
import GameGFX.Animacion;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import mariotest.Mariotest;

/**
 * Sistema de poder del martillo
 * Patrón Strategy para el comportamiento del power-up
 * 
 * @author LENOVO
 */
public class PoderMartillo {
    
    private Player player;
    private Handler handler;
    
    // Estado del poder
    private boolean activo;
    private int ticksRestantes;
    private int ticksMaximos;
    private static final int DURACION_DEFAULT = 600; // 10 segundos
    
    // Animación del martillo
    private Animacion animacionMartillo;
    private BufferedImage[] spritesMartillo;
    private int frameActual;
    
    // Área de ataque
    private Rectangle areaAtaque;
    private static final int RANGO_ATAQUE = 20;
    
    // Control de golpes
    private boolean golpeando;
    private int ticksGolpe;
    private static final int DURACION_GOLPE = 15; // 0.25 segundos
    private int cooldownGolpe;
    private static final int COOLDOWN_DEFAULT = 15; // 0.33 segundos entre golpes
    
    // Efectos visuales
    private List<EfectoGolpe> efectosGolpe;
    
    // Estadísticas
    private int enemigosDestruidos;
    
    /**
     * Constructor
     */
    public PoderMartillo(Player player, Handler handler) {
        this.player = player;
        this.handler = handler;
        this.activo = false;
        this.ticksRestantes = 0;
        this.ticksMaximos = DURACION_DEFAULT;
        this.golpeando = false;
        this.ticksGolpe = 0;
        this.cooldownGolpe = 0;
        this.enemigosDestruidos = 0;
        this.efectosGolpe = new ArrayList<>();
        
        inicializarAnimacion();
    }
    
    /**
     * Inicializa la animación del martillo
     */
private void inicializarAnimacion() {
    try {
        spritesMartillo = Mariotest.getTextura().getMarioMartillo();

        if (spritesMartillo != null && spritesMartillo.length >= 4) {
            animacionMartillo = new Animacion(3,
                spritesMartillo[0],
                spritesMartillo[1],
                spritesMartillo[2],
                spritesMartillo[3]
            );
            System.out.println("[PODER MARTILLO] ✅ Animación real cargada.");
        } else {
            System.err.println("[PODER MARTILLO] ❌ No se encontraron sprites de martillo, usando placeholder.");
            spritesMartillo = crearSpritesPlaceholder();
            animacionMartillo = new Animacion(3, spritesMartillo);
        }
    } catch (Exception e) {
        System.err.println("[PODER MARTILLO] Error al inicializar animación: " + e.getMessage());
        spritesMartillo = crearSpritesPlaceholder();
        animacionMartillo = new Animacion(3, spritesMartillo);
    }
}
    
    /**
     * Crea sprites placeholder del martillo
     */
    private BufferedImage[] crearSpritesPlaceholder() {
        BufferedImage[] sprites = new BufferedImage[4];
        
        for (int i = 0; i < 4; i++) {
            sprites[i] = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = sprites[i].createGraphics();
            
            // Ángulo de rotación según frame
            int angulo = i * 30; // 0°, 30°, 60°, 90°
            
            // Mango
            g.setColor(new Color(139, 69, 19));
            g.fillRect(10, 8, 4, 12);
            
            // Cabeza del martillo
            g.setColor(new Color(128, 128, 128));
            
            // Rotar según frame
            int offsetX = (int)(Math.cos(Math.toRadians(angulo)) * 8);
            int offsetY = (int)(Math.sin(Math.toRadians(angulo)) * 8);
            
            g.fillRect(8 + offsetX, 4 + offsetY, 8, 6);
            
            // Borde
            g.setColor(Color.BLACK);
            g.drawRect(8 + offsetX, 4 + offsetY, 8, 6);
            
            g.dispose();
        }
        
        return sprites;
    }
    
    /**
     * Activa el poder del martillo
     */
    public void activar() {
        activar(DURACION_DEFAULT);
    }
    
    /**
     * Activa el poder con duración personalizada
     */
    public void activar(int duracionTicks) {
        this.activo = true;
        this.ticksRestantes = duracionTicks;
        this.ticksMaximos = duracionTicks;
        this.enemigosDestruidos = 0;
        
        System.out.println("[MARTILLO] ¡Poder activado! Duración: " + 
                          (duracionTicks / 60) + " segundos");
    }
    
    /**
     * Desactiva el poder
     */
    public void desactivar() {
        if (activo) {
            activo = false;
            golpeando = false;
            
            System.out.println("[MARTILLO] Poder desactivado. Enemigos destruidos: " + 
                              enemigosDestruidos);
            
            // Bonus por enemigos destruidos
            if (enemigosDestruidos > 0) {
                int bonus = enemigosDestruidos * 100;
                EstadoJuego.getInstance().sumarPuntos(bonus);
            }
        }
    }
    
    /**
     * Actualiza el poder cada tick
     */
public void tick() {
    if (!activo) return;
    
    // Decrementar tiempo restante
    ticksRestantes--;
    if (ticksRestantes <= 0) {
        desactivar();
        return;
    }
    
    // Actualizar cooldown
    if (cooldownGolpe > 0) {
        cooldownGolpe--;
    }
    
    // 🆕 GOLPEO AUTOMÁTICO - Golpea cada vez que termina el cooldown
    if (cooldownGolpe == 0 && !golpeando) {
        golpearAutomatico();
    }
    
    // Actualizar animación de golpe
    if (golpeando) {
        ticksGolpe++;
        
        if (ticksGolpe >= DURACION_GOLPE) {
            golpeando = false;
            ticksGolpe = 0;
        }
    }
    
    // Actualizar animación del martillo
    if (animacionMartillo != null) {
        animacionMartillo.runAnimacion();
    }
    
    // Actualizar efectos visuales
    for (int i = efectosGolpe.size() - 1; i >= 0; i--) {
        EfectoGolpe efecto = efectosGolpe.get(i);
        efecto.tick();
        
        if (efecto.terminado()) {
            efectosGolpe.remove(i);
        }
    }
    
    // Advertencia cuando queda poco tiempo
    if (ticksRestantes == 180) { // 3 segundos
        System.out.println("[MARTILLO] ¡Advertencia! Quedan 3 segundos");
    }
}

private void golpearAutomatico() {
    golpeando = true;
    ticksGolpe = 0;
    cooldownGolpe = COOLDOWN_DEFAULT;
    
    // Calcular área de ataque según dirección del jugador
    actualizarAreaAtaque();
    
    // Detectar enemigos en el área de ataque
    detectarEnemigos();
    
    // Crear efecto visual
    crearEfectoGolpe();
    
    // Solo mostrar mensaje si destruyó algo
    // System.out.println("[MARTILLO] Golpe automático");
}
    
    /**
     * Ejecuta un golpe con el martillo
     */
    public void golpear() {
        if (!activo || cooldownGolpe > 0) {
            return;
        }
      golpearAutomatico();
    }
    
    /**
     * Actualiza el área de ataque según posición y dirección del jugador
     */
    private void actualizarAreaAtaque() {
        boolean mirandoDerecha = player.getVelX() >= 0;
        
        int x = (int)player.getX();
        int y = (int)player.getY();
        int w = (int)player.getWidth();
        int h = (int)player.getHeight();
        
        if (mirandoDerecha) {
            // Área de ataque a la derecha
            areaAtaque = new Rectangle(
                x + w - 5, 
                y, 
                RANGO_ATAQUE, 
                h
            );
        } else {
            // Área de ataque a la izquierda
            areaAtaque = new Rectangle(
                x - RANGO_ATAQUE + 5, 
                y, 
                RANGO_ATAQUE, 
                h
            );
        }
    }
    
    /**
     * Detecta y destruye enemigos en el área de ataque
     */
    private void detectarEnemigos() {
        List<GameObjetos> objetosAEliminar = new ArrayList<>();
        
        for (GameObjetos obj : handler.getGameObjs()) {
            ObjetosID id = obj.getId();
            
            // Verificar si es un enemigo destructible
            if (id == ObjetosID.Barril || 
                id == ObjetosID.Fuego || 
                id == ObjetosID.DiegoKong) {
                
                if (areaAtaque.intersects(obj.getBounds())) {
                    objetosAEliminar.add(obj);
                }
            }
        }
        
        // Destruir enemigos detectados
        for (GameObjetos enemigo : objetosAEliminar) {
            destruirEnemigo(enemigo);
        }
    }
    
    /**
     * Destruye un enemigo y otorga puntos
     */
    private void destruirEnemigo(GameObjetos enemigo) {
        // Puntos según tipo de enemigo
        int puntos = 0;
        
        switch (enemigo.getId()) {
            case Barril:
                puntos = 500;
                break;
            case Fuego:
                puntos = 800;
                break;
            case DiegoKong:
                puntos = 5000; // ¡Bonus enorme por golpear a DK!
                break;
        }
        
        // Registrar en estado del juego
        EstadoJuego estado = EstadoJuego.getInstance();
        estado.enemigoEliminado(puntos);
        
        // Incrementar contador
        enemigosDestruidos++;
        
        // Crear explosión
        crearExplosion(enemigo.getX(), enemigo.getY());
        
        // Eliminar enemigo
        handler.removeObj(enemigo);
        
        System.out.println("[MARTILLO] ¡Enemigo destruido! (+" + puntos + " pts)");
    }
    
    /**
     * Crea efecto visual de golpe
     */
    private void crearEfectoGolpe() {
        int x = (int)(areaAtaque.x + areaAtaque.width / 2);
        int y = (int)(areaAtaque.y + areaAtaque.height / 2);
        
        EfectoGolpe efecto = new EfectoGolpe(x, y);
        efectosGolpe.add(efecto);
    }
    
    /**
     * Crea explosión cuando se destruye un enemigo
     */
    private void crearExplosion(float x, float y) {
        Explosion explosion = new Explosion(x, y, 2, handler);
        handler.addObj(explosion);
    }
    
    /**
     * Renderiza el martillo y efectos
     */
    public void render(Graphics g) {
        if (!activo) return;
        
        // Renderizar martillo sobre el jugador
        renderMartillo(g);
        
        // Renderizar efectos de golpe
        for (EfectoGolpe efecto : efectosGolpe) {
            efecto.render(g);
        }
        
        // Renderizar barra de tiempo restante
        renderBarraTiempo(g);
        
        // Debug: mostrar área de ataque
        if (areaAtaque != null && golpeando) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(
                areaAtaque.x, 
                areaAtaque.y, 
                areaAtaque.width, 
                areaAtaque.height
            );
        }
    }
    
    /**
     * Renderiza el martillo sobre el jugador
     */
    private void renderMartillo(Graphics g) {
        if (animacionMartillo != null && spritesMartillo != null) {
            int x = (int)(player.getX() + player.getWidth() / 2 - 12);
            int y = (int)(player.getY() - 10);
            
            // Ajustar posición si está golpeando
            if (golpeando) {
                boolean mirandoDerecha = player.getVelX() >= 0;
                x += mirandoDerecha ? 8 : -8;
                y += 5;
            }
            
            animacionMartillo.drawAnimacion(g, x, y, 24, 24);
        } else {
            // Placeholder
            g.setColor(Color.GRAY);
            g.fillRect(
                (int)(player.getX() + player.getWidth() / 2 - 6),
                (int)(player.getY() - 10),
                12, 12
            );
        }
    }
    
    /**
     * Renderiza barra de tiempo restante
     */
    private void renderBarraTiempo(Graphics g) {
        int barWidth = 100;
        int barHeight = 8;
        int x = (int)(player.getX() - 25);
        int y = (int)(player.getY() - 25);
        
        // Fondo
        g.setColor(Color.BLACK);
        g.fillRect(x, y, barWidth, barHeight);
        
        // Barra de progreso
        float progreso = (float)ticksRestantes / ticksMaximos;
        int fillWidth = (int)(barWidth * progreso);
        
        // Color según tiempo restante
        Color colorBarra;
        if (progreso > 0.5f) {
            colorBarra = Color.GREEN;
        } else if (progreso > 0.25f) {
            colorBarra = Color.YELLOW;
        } else {
            colorBarra = Color.RED;
        }
        
        g.setColor(colorBarra);
        g.fillRect(x, y, fillWidth, barHeight);
        
        // Borde
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
    }
    
    // ==================== CLASE INTERNA: EFECTO GOLPE ====================
    
    /**
     * Efecto visual del golpe del martillo
     */
    private class EfectoGolpe {
        private int x, y;
        private int ticks;
        private int duracion = 20;
        private int radio;
        
        public EfectoGolpe(int x, int y) {
            this.x = x;
            this.y = y;
            this.ticks = 0;
            this.radio = 5;
        }
        
        public void tick() {
            ticks++;
            radio += 2;
        }
        
        public boolean terminado() {
            return ticks >= duracion;
        }
        
        public void render(Graphics g) {
            float alpha = 1.0f - ((float)ticks / duracion);
            int alphaInt = (int)(alpha * 255);
            
            // Círculo de impacto
            g.setColor(new Color(255, 255, 0, alphaInt));
            g.fillOval(x - radio, y - radio, radio * 2, radio * 2);
            
            // Borde
            g.setColor(new Color(255, 165, 0, alphaInt));
            g.drawOval(x - radio, y - radio, radio * 2, radio * 2);
            
            // Líneas de impacto
            for (int i = 0; i < 8; i++) {
                double angulo = Math.toRadians(i * 45);
                int x1 = x + (int)(Math.cos(angulo) * radio);
                int y1 = y + (int)(Math.sin(angulo) * radio);
                int x2 = x + (int)(Math.cos(angulo) * (radio + 10));
                int y2 = y + (int)(Math.sin(angulo) * (radio + 10));
                
                g.setColor(new Color(255, 255, 255, alphaInt / 2));
                g.drawLine(x1, y1, x2, y2);
            }
        }
    }
    
    // ==================== GETTERS ====================
    
    public boolean isActivo() {
        return activo;
    }
    
    public int getTicksRestantes() {
        return ticksRestantes;
    }
    
    public int getTiempoRestanteSegundos() {
        return ticksRestantes / 60;
    }
    
    public boolean isGolpeando() {
        return golpeando;
    }
    
    public int getEnemigosDestruidos() {
        return enemigosDestruidos;
    }
    
    public float getProgreso() {
        return (float)ticksRestantes / ticksMaximos;
    }
}