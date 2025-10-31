package Objetos;

import GameGFX.*;
import Objetos.Utilidad.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import mariotest.*;

/**
 * Jugador mejorado con sistema de escaleras y colisiones corregidas
 * FIX: Permite salir de escalera al tocar bloques superiores
 */
public class Player extends GameObjetos {
    private static final float WIDTH = 16;
    private static final float HEIGHT = 16;
    
    // Estados del jugador
    private Handler handler;
    private Texturas textura;
    private BufferedImage[] spriteS;
    private PlayerEstado estado;
    private PoderMartillo poderMartillo;
    private boolean tieneMartillo;
    private BufferedImage[] spriteMartillo;
    private Animacion playerCaminaMartillo;
    
    // Animaciones
    private Animacion playerCaminaS;
    private Animacion playerSubeEscalera;
    private Animacion playerBajaEscalera;
    private Animacion playerCaminaL;
    private BufferedImage[] currSprite;
    private Animacion currAnimacion;
    
    // Flags de movimiento
    private boolean salto = false;
    private boolean adelante = true;
    private boolean enEscalera = false;
    private boolean puedeMoverseEnEscalera = false;
    private boolean subiendoEscalera = false;
    private boolean bajandoEscalera = false;
    
    // Física
    private static final float VELOCIDAD_CAMINAR = 2f;
    private static final float VELOCIDAD_ESCALERA = 1f;
    private static final float FUERZA_SALTO = -7f;
    private static final float GRAVEDAD = 0.5f;
    
    // Escalera actual
    private Escalera escaleraActual = null;
    
    // Control de salida de escalera
    private int ticksEnEscalera = 0;
    private static final int TICKS_MIN_ESCALERA = 5; // Frames mínimos antes de poder salir
    
    public Player(float x, float y, int scale, Handler handler) {
        super(x, y, ObjetosID.Jugador, WIDTH, HEIGHT, scale);
        this.handler = handler;
        this.poderMartillo = new PoderMartillo(this, handler);
        this.tieneMartillo = false;
        this.textura = Mariotest.getTextura();
        spriteMartillo = textura.getMarioMartillo();
        spriteS = textura.getMarioS();
        
        // Inicializar animaciones
        playerCaminaS = new Animacion(5, spriteS[1], spriteS[2], spriteS[3]);
        
        // Animación de subir escalera (sprites 5-11)
        playerSubeEscalera = new Animacion(5, spriteS[5], spriteS[6], spriteS[7], 
                                           spriteS[8], spriteS[9], spriteS[10], spriteS[11]);
        
        // Animación de bajar escalera (misma que subir, se puede invertir o usar otros sprites)
        playerBajaEscalera = new Animacion(5, spriteS[5], spriteS[6], spriteS[7], 
                                           spriteS[8], spriteS[9], spriteS[10], spriteS[11]);
         
    playerCaminaMartillo = new Animacion(5, 
        spriteMartillo[0], 
        spriteMartillo[1], 
        spriteMartillo[2],
        spriteMartillo[3],
        spriteMartillo[4],
        spriteMartillo[5]
    );

        estado = PlayerEstado.Pequeno;
        currSprite = spriteS;
        currAnimacion = playerCaminaS;
    }

    @Override
    public void tick() {
        // Detectar si está en una escalera
        verificarEscalera();
        
        // Aplicar física según el estado
        if (enEscalera) {
            // En escalera: no hay gravedad automática
            ticksEnEscalera++;
            
            // Verificar si debe salir de la escalera al tocar un bloque arriba
            if (subiendoEscalera && ticksEnEscalera > TICKS_MIN_ESCALERA) {
                verificarSalidaSuperiorEscalera();
            }
            
            // NUEVO: Verificar si debe salir al llegar al final de la escalera bajando
            if (bajandoEscalera && ticksEnEscalera > TICKS_MIN_ESCALERA) {
                verificarSalidaInferiorEscalera();
            }
        } else {
            // Fuera de escalera: aplicar gravedad normal
            aplicarGravedad();
            ticksEnEscalera = 0;
        }
        
        if (poderMartillo != null) {
        poderMartillo.tick();
        tieneMartillo = poderMartillo.isActivo();
    }

        
        // Aplicar movimiento
        setX(getVelX() + getX());
        setY(getVely() + getY());
        
        // Colisiones
        colisiones();
        
        // Actualizar animación según el estado
        actualizarAnimacion();
    }
    
    /**
     * NUEVO: Verifica si el jugador debe salir de la escalera al llegar abajo
     */
    private void verificarSalidaInferiorEscalera() {
        if (!enEscalera || !bajandoEscalera) return;
        
        // Verificar si hay un bloque sólido justo debajo
        boolean hayBloqueDebajo = false;
        boolean hayEscaleraDebajo = false;
        
        Rectangle areaDebajo = new Rectangle(
            (int)(getX() + getWidth() / 4),
            (int)(getY() + getHeight()),
            (int)(getWidth() / 2),
            10
        );
        
        for (GameObjetos obj : handler.getGameObjs()) {
            // Verificar bloques sólidos
            if (obj.getId() == ObjetosID.Bloque || obj.getId() == ObjetosID.Pipe) {
                if (areaDebajo.intersects(obj.getBounds())) {
                    hayBloqueDebajo = true;
                    
                    // Posicionar sobre el bloque
                    if (Math.abs(getY() + getHeight() - obj.getY()) < 15) {
                        setY(obj.getY() - getHeight());
                    }
                }
            }
            
            // Verificar si hay escalera para continuar
            if (obj.getId() == ObjetosID.Escalera) {
                if (areaDebajo.intersects(obj.getBounds())) {
                    hayEscaleraDebajo = true;
                }
            }
        }
        
        // Si hay bloque pero NO hay escalera, salir
        if (hayBloqueDebajo && !hayEscaleraDebajo) {
            salirEscalera();
            setVely(0);
            salto = false;
            System.out.println("[PLAYER] Salió de escalera al llegar abajo");
        }
    }
    
    /**
     * NUEVO: Verifica si el jugador debe salir de la escalera al llegar arriba
     * Detecta bloques sólidos justo encima de la cabeza del jugador
     */
    private void verificarSalidaSuperiorEscalera() {
        if (!enEscalera || !subiendoEscalera) return;
        
        // Verificar si hay un bloque sólido cerca de la parte superior
        for (GameObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Bloque) {
                Rectangle bloqueArea = obj.getBounds();
                
                // Crear área de detección en la parte superior del jugador
                Rectangle areaDeteccion = new Rectangle(
                    (int)(getX() + getWidth() / 4),
                    (int)(getY() - 10), // 10 píxeles por encima de la cabeza
                    (int)(getWidth() / 2),
                    15 // Altura de detección
                );
                
                // Si el área de detección toca el bloque
                if (areaDeteccion.intersects(bloqueArea)) {
                    // Posicionar al jugador SOBRE el bloque
                    float nuevaY = bloqueArea.y - getHeight();
                    
                    // Solo si está cerca de la superficie del bloque
                    if (Math.abs(getY() - nuevaY) < 20) {
                        setY(nuevaY);
                        salirEscalera();
                        setVely(0);
                        salto = false;
                        
                        System.out.println("[PLAYER] Salió de escalera al tocar bloque superior");
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Actualiza y ejecuta la animación apropiada según el estado del jugador
     */
    private void actualizarAnimacion() {
        if (enEscalera) {
            // En escalera
            if (subiendoEscalera) {
                currAnimacion = playerSubeEscalera;
                currAnimacion.runAnimacion();
            } else if (bajandoEscalera) {
                currAnimacion = playerBajaEscalera;
                currAnimacion.runAnimacion();
            }
            // Si está quieto en escalera, no correr animación
        } else {
            // Fuera de escalera
            if (getVelX() != 0 && !salto) {
                // Caminando
                currAnimacion = playerCaminaS;
                currAnimacion.runAnimacion();
            }
            // Si está saltando o quieto, no correr animación de caminar
        }
    }
    
    /**
     * Verifica si el jugador está tocando una escalera
     * MEJORADO: Detecta escaleras tanto en contacto directo como debajo
     */
    private void verificarEscalera() {
        escaleraActual = null;
        puedeMoverseEnEscalera = false;
        
        // Revisar todas las escaleras
        for (GameObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Escalera || obj.getId() == ObjetosID.EscaleraRota) {
                Escalera escalera = (Escalera) obj;
                
                // ÁREA 1: Contacto directo (para subir/estar en escalera)
                Rectangle areaEscalera = escalera.getAreaInteraccion();
                Rectangle jugadorBounds = getBounds();
                
                if (areaEscalera.intersects(jugadorBounds)) {
                    if (escalera.esUsable()) {
                        escaleraActual = escalera;
                        puedeMoverseEnEscalera = true;
                        break;
                    }
                }
                
                // ÁREA 2: Escalera debajo (para bajar desde plataforma)
                Rectangle areaDebajo = new Rectangle(
                    (int)(getX() + getWidth() / 4),
                    (int)(getY() + getHeight() - 5), // Desde los pies
                    (int)(getWidth() / 2),
                    25 // Buscar hasta 25 píxeles hacia abajo
                );
                
                if (areaDebajo.intersects(escalera.getBounds()) && escalera.esUsable()) {
                    escaleraActual = escalera;
                    puedeMoverseEnEscalera = true;
                    break;
                }
            }
        }
        
        // Si no está tocando una escalera Y no está bajando activamente, salir del modo escalera
        if (!puedeMoverseEnEscalera && enEscalera && !bajandoEscalera) {
            salirEscalera();
        }
    }
    
    /**
     * Sistema de colisiones mejorado
     * CRÍTICO: Permite atravesar bloques al subir Y bajar escaleras
     */
    private void colisiones() {
        for (int i = 0; i < handler.getGameObjs().size(); i++) {
            GameObjetos temp = handler.getGameObjs().get(i);
            
            // Ignorar colisiones con TileVisual (son decorativos)
            if (temp.getId() == ObjetosID.TileVisual) {
                continue;
            }
            
            // Colisión con bloques y tuberías
            if (temp.getId() == ObjetosID.Bloque || temp.getId() == ObjetosID.Pipe) {
                
                // ============================================
                // PRIORIDAD 1: Si está bajando por escalera, NO COLISIONAR
                // ============================================
                if (enEscalera && bajandoEscalera) {
                    continue; // Ignorar TODAS las colisiones al bajar
                }
                
                // ============================================
                // PRIORIDAD 2: Si está subiendo por escalera, NO COLISIONAR
                // ============================================
                if (enEscalera && subiendoEscalera) {
                    continue; // Ignorar TODAS las colisiones al subir
                }
                
                // ============================================
                // COLISIONES NORMALES (solo si NO está en escalera)
                // ============================================
                
                // COLISIÓN INFERIOR (aterrizar en plataforma)
                if (getBounds().intersects(temp.getBounds())) {
                    setY(temp.getY() - getHeight());
                    setVely(0);
                    salto = false;
                }
                
                // COLISIÓN SUPERIOR (golpear la cabeza)
                if (getBoundsTop().intersects(temp.getBounds())) {
                    setY(temp.getY() + temp.getHeight());
                    setVely(0);
                }
                
                // COLISIÓN DERECHA
                if (getBoundsRight().intersects(temp.getBounds())) {
                    setX(temp.getX() - getWidth());
                }
                
                // COLISIÓN IZQUIERDA
                if (getBoundsLeft().intersects(temp.getBounds())) {
                    setX(temp.getX() + temp.getWidth());
                }
            }
        }
    }
    
    @Override
    public void aplicarGravedad() {
        if (!enEscalera) {
            setVely(getVely() + GRAVEDAD);
        }
    }

@Override
public void render(Graphics g) {
    // Determinar qué sprites usar según si tiene martillo
    BufferedImage[] spritesActuales = tieneMartillo ? spriteMartillo : currSprite;
    Animacion animacionCaminar = tieneMartillo ? playerCaminaMartillo : currAnimacion;
    
    // Verificar que los sprites existan
    if (spritesActuales == null || spritesActuales.length == 0) {
        // Fallback a sprites normales
        spritesActuales = currSprite;
        animacionCaminar = currAnimacion;
    }
    
    if (enEscalera) {
        // ====== RENDERIZADO EN ESCALERA ======
        if (subiendoEscalera || bajandoEscalera) {
            // En escalera siempre usa sprites normales (sin martillo)
            currAnimacion.drawAnimacion(g, (int) getX(), (int) getY(), 
                                       (int) getWidth(), (int) getHeight());
        } else {
            // Quieto en escalera
            g.drawImage(spriteS[5], (int) getX(), (int) getY(), 
                       (int) getWidth(), (int) getHeight(), null);
        }
    } else if (salto) {
        // ====== RENDERIZADO SALTANDO ======
        BufferedImage spriteJump = spritesActuales[3]; // Frame 3 = salto
        
        if (adelante) {
            g.drawImage(spriteJump, (int) getX(), (int) getY(), 
                       (int) getWidth(), (int) getHeight(), null);
        } else {
            g.drawImage(spriteJump, (int) (getX() + getWidth()), (int) getY(), 
                       (int) -getWidth(), (int) getHeight(), null);
        }
    } else if (getVelX() > 0) {
        // ====== CAMINANDO A LA DERECHA ======
        animacionCaminar.drawAnimacion(g, (int) getX(), (int) getY(), 
                                       (int) getWidth(), (int) getHeight());
        adelante = true;
    } else if (getVelX() < 0) {
        // ====== CAMINANDO A LA IZQUIERDA ======
        animacionCaminar.drawAnimacion(g, (int) (getX() + getWidth()), (int) getY(), 
                                       (int) -getWidth(), (int) getHeight());
        adelante = false;
    } else {
        // ====== REPOSO ======
        BufferedImage spriteReposo = spritesActuales[0]; // Frame 0 = reposo
        
        if (adelante) {
            g.drawImage(spriteReposo, (int) getX(), (int) getY(), 
                       (int) getWidth(), (int) getHeight(), null);
        } else {
            g.drawImage(spriteReposo, (int) (getX() + getWidth()), (int) getY(), 
                       (int) -getWidth(), (int) getHeight(), null);
        }
    }
    
    // Renderizar martillo si está activo
    if (poderMartillo != null && tieneMartillo) {
        poderMartillo.render(g);
    }
}
    public void activarMartillo() {
        if (poderMartillo != null) {
            poderMartillo.activar();
            tieneMartillo = true;
            System.out.println("[PLAYER] ¡Martillo activado!");
        }
    }

    public void activarMartillo(int duracionSegundos) {
        if (poderMartillo != null) {
            poderMartillo.activar(duracionSegundos * 60); // Convertir a ticks
            tieneMartillo = true;
        }
    }
    
    /**
     * Ejecuta un golpe con el martillo
     * Llamar cuando el jugador presione el botón de ataque
     */
    public void golpearConMartillo() {
        if (poderMartillo != null && tieneMartillo) {
            poderMartillo.golpear();
        }
    }
    
    /**
     * Verifica si el jugador tiene el martillo activo
     */
    public boolean tieneMartillo() {
        return tieneMartillo;
    }
    
    /**
     * Obtiene el poder del martillo
     */
    public PoderMartillo getPoderMartillo() {
        return poderMartillo;
    }

    @Override
    public Rectangle getBounds() {
        // Pie del jugador para colisiones con el suelo
        return new Rectangle(
            (int)(getX() + getWidth() / 4),
            (int)(getY() + getHeight() / 2),
            (int) getWidth() / 2,
            (int) getHeight() / 2
        );
    }
    
    public Rectangle getBoundsTop() {
        // Cabeza del jugador
        return new Rectangle(
            (int) (getX() + getWidth() / 4),
            (int) getY(),
            (int) getWidth() / 2,
            (int) getHeight() / 2
        );
    }
    
    public Rectangle getBoundsRight() {
        // Lado derecho
        return new Rectangle(
            (int) (getX() + getWidth() - 5),
            (int) getY() + 5,
            5,
            (int) getHeight() - 10
        );
    }
    
    public Rectangle getBoundsLeft() {
        // Lado izquierdo
        return new Rectangle(
            (int) getX(),
            (int) (getY() + 5),
            5,
            (int) (getHeight() - 10)
        );
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Inicia el salto del jugador
     */
    public void iniciarSalto() {
        if (!salto && !enEscalera) {
            setVely(FUERZA_SALTO);
            salto = true;
        }
    }
    
    /**
     * Mueve al jugador hacia arriba en una escalera
     */
    public void subirEscalera() {
        if (puedeMoverseEnEscalera && escaleraActual != null) {
            if (!enEscalera) {
                // Centrar al jugador en la escalera al entrar
                float centroEscalera = escaleraActual.getX() + escaleraActual.getWidth() / 2;
                setX(centroEscalera - getWidth() / 2);
            }
            
            enEscalera = true;
            subiendoEscalera = true;
            bajandoEscalera = false;
            setVely(-VELOCIDAD_ESCALERA);
            setVelX(0); // Detener movimiento horizontal
            salto = false; // Resetear salto
        }
    }
    
    /**
     * Mueve al jugador hacia abajo en una escalera
     * MEJORADO: FUERZA la entrada desde plataformas sólidas
     */
    public void bajarEscalera() {
        // CASO 1: Ya está en una escalera, continuar bajando
        if (enEscalera && escaleraActual != null) {
            bajandoEscalera = true;
            subiendoEscalera = false;
            setVely(VELOCIDAD_ESCALERA);
            setVelX(0);
            return;
        }
        
        // CASO 2: NO está en escalera, pero hay una cerca
        if (puedeMoverseEnEscalera && escaleraActual != null) {
            // Centrar al jugador en la escalera
            float centroEscalera = escaleraActual.getX() + escaleraActual.getWidth() / 2;
            setX(centroEscalera - getWidth() / 2);
            
            // FORZAR entrada a modo escalera
            enEscalera = true;
            bajandoEscalera = true;
            subiendoEscalera = false;
            setVely(VELOCIDAD_ESCALERA);
            setVelX(0);
            salto = false;
            
            System.out.println("[PLAYER] Forzó entrada a escalera desde plataforma");
            return;
        }
        
        // CASO 3: No hay escalera detectada, buscar manualmente debajo
        buscarYEntrarEscaleraDebajo();
    }
    
    /**
     * NUEVO: Busca activamente una escalera debajo y FUERZA la entrada
     * Este método es más agresivo que verificarEscalera()
     */
    private void buscarYEntrarEscaleraDebajo() {
        // Área de búsqueda AMPLIA: hasta 30 píxeles hacia abajo
        Rectangle areaBusqueda = new Rectangle(
            (int)(getX() + getWidth() / 4),
            (int)(getY() + getHeight() - 5),
            (int)(getWidth() / 2),
            30 // Búsqueda extendida
        );
        
        for (GameObjetos obj : handler.getGameObjs()) {
            if (obj.getId() == ObjetosID.Escalera) {
                Escalera escalera = (Escalera) obj;
                
                if (escalera.esUsable() && areaBusqueda.intersects(escalera.getBounds())) {
                    // ¡FORZAR ENTRADA!
                    escaleraActual = escalera;
                    puedeMoverseEnEscalera = true;
                    
                    // Centrar en la escalera
                    float centroEscalera = escalera.getX() + escalera.getWidth() / 2;
                    setX(centroEscalera - getWidth() / 2);
                    
                    // Activar modo escalera INMEDIATAMENTE
                    enEscalera = true;
                    bajandoEscalera = true;
                    subiendoEscalera = false;
                    setVely(VELOCIDAD_ESCALERA);
                    setVelX(0);
                    salto = false;
                    
                    System.out.println("[PLAYER] Forzó entrada a escalera (búsqueda activa)");
                    return;
                }
            }
        }
        
        System.out.println("[PLAYER] No se encontró escalera debajo (buscó hasta 30px)");
    }
    
    /**
     * Detiene el movimiento vertical en la escalera
     */
    public void detenerMovimientoVertical() {
        if (enEscalera) {
            subiendoEscalera = false;
            bajandoEscalera = false;
            setVely(0);
        }
    }
    
    /**
     * Sale de la escalera y retoma el movimiento normal
     */
    public void salirEscalera() {
        if (enEscalera) {
            enEscalera = false;
            subiendoEscalera = false;
            bajandoEscalera = false;
            setVely(0);
            ticksEnEscalera = 0;
        }
    }
    
    /**
     * Mueve al jugador a la izquierda
     */
    public void moverIzquierda() {
        if (!enEscalera) {
            setVelX(-VELOCIDAD_CAMINAR);
        } else {
            // Si está en escalera y presiona A/D, salir de la escalera
            salirEscalera();
            setVelX(-VELOCIDAD_CAMINAR);
        }
    }
    
    /**
     * Mueve al jugador a la derecha
     */
    public void moverDerecha() {
        if (!enEscalera) {
            setVelX(VELOCIDAD_CAMINAR);
        } else {
            // Si está en escalera y presiona A/D, salir de la escalera
            salirEscalera();
            setVelX(VELOCIDAD_CAMINAR);
        }
    }
    
    /**
     * Detiene el movimiento horizontal
     */
    public void detenerMovimiento() {
        if (!enEscalera) {
            setVelX(0);
        }
    }
    
    public boolean hasSalto() {
        return salto;
    }
    
    public void setSalto(boolean hasSalto) {
        this.salto = hasSalto;
    }
    
    public boolean isEnEscalera() {
        return enEscalera;
    }
    
    public boolean isPuedeMoverseEnEscalera() {
        return puedeMoverseEnEscalera;
    }
    
    public boolean isSubiendoEscalera() {
        return subiendoEscalera;
    }
    
    public boolean isBajandoEscalera() {
        return bajandoEscalera;
    }
    
    public Escalera getEscaleraActual() {
        return escaleraActual;
    }
    
    public PlayerEstado getEstado() {
        return estado;
    }
    
    public void setEstado(PlayerEstado estado) {
        this.estado = estado;
    }
}