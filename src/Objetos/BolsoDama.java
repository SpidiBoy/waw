package Objetos;

import Objetos.Utilidad.*;
import java.awt.Color;
import java.awt.Graphics;
import mariotest.*;

/**
 * Item BolsoDama - Coleccionable de bonificación
 * 
 * ✅ FIX: Corregida carga de sprites
 * 
 * @author LENOVO
 */
public class BolsoDama extends Item {
    private static final float WIDTH = 16f;
    private static final float HEIGHT = 16f;
    private static final int VALOR_PUNTOS = 400;
    
    public BolsoDama(float x, float y, int scale, Handler handler) {
        super(x, y, WIDTH, HEIGHT, scale, handler, VALOR_PUNTOS);
        this.flotar = true;
        this.desapareceDespuesDeRecoger = true;
        cargarSprites();
    }
    
    /**
     * ✅ FIX: Carga correcta de sprites
     */
    private void cargarSprites() {
        try {
            sprites = Mariotest.getTextura().getBolsoSprites();
            
            if (sprites != null && sprites.length > 0 && sprites[0] != null) {
                System.out.println("[BOLSO] Sprite estático cargado correctamente");
            } else {
                System.err.println("[BOLSO] ERROR: Array de sprites vacío o nulo.");
                sprites = null;
            }
        } catch (Exception e) {
            System.err.println("[BOLSO] Error cargando sprites: " + e.getMessage());
            e.printStackTrace();
            sprites = null;
        }
    }
    
    @Override
    protected void aplicarEfecto(Player player) {
        // Solo da puntos
        System.out.println("[BOLSO] ¡Recolectado!");
    }
    
    @Override
    protected void renderPlaceholder(Graphics g) {
        g.setColor(new Color(255, 192, 203)); // Rosa
        g.fillRect((int)getX() + 2, (int)getY() + 4, 12, 10);
        g.setColor(Color.BLACK);
        g.drawRect((int)getX() + 2, (int)getY() + 4, 12, 10);
        // Asa
        g.drawArc((int)getX() + 4, (int)getY(), 8, 6, 0, 180);
    }
}