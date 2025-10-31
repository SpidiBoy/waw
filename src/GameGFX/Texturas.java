package GameGFX;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Clase Texturas actualizada con sprites de Fuego
 * @author LENOVO
 */
public class Texturas {
    private final String folder = "/Imagenes";
    
    private final int mario_L_count = 21;
    private final int mario_S_count = 14;
    private final int mario_martillo_count = 6; // 🆕 NUEVO
    private final int Tile_1_count = 28;
    private final int Tile_2_count = 33;
    private final int barril_count = 8;
    private final int diegokong_count = 8;
    private final int bloque_count = 8;
    private final int princesa_count = 8;
    private final int fuego_count = 4; 
    private final int llama_count = 4; 
    private final int martillo_count = 4; // 🆕 NUEVO
    private final int items_count = 8; //
    
    private CargadorImagenes cargar;
    
    // HOJAS DE SPRITES
    private BufferedImage player_sheet, enemy_sheet_, bloque_sheet, barril_sheet;
    private BufferedImage diegokong_sheet, princesaSheet, fuego_sheet, llama_sheet; 
    private HashMap<Integer, BufferedImage> tileSprites;

    // ARRAYS DE SPRITES
    private BufferedImage[] mario_l, mario_s, tile1, tile2, tile3, tile4, mario_martillo;
    private BufferedImage[] barril_sprites, diegokong_sprites, bloque_sprites;
    private BufferedImage items_sheet; // 🆕 NUEVO
    private BufferedImage[] princesaSprites, fuego_sprites, llama_sprites; 
    private BufferedImage[] martillo_sprites, paraguas_sprites, bolso_sprites, sombrero_sprites; // 🆕 NUEVO
    
    public Texturas(){
        mario_l = new BufferedImage[mario_L_count];
        mario_s = new BufferedImage[mario_S_count];
        mario_martillo = new BufferedImage[mario_martillo_count];
        tile1 = new BufferedImage[Tile_1_count + Tile_2_count];
        tile2 = new BufferedImage[Tile_1_count + Tile_2_count];
        tile3 = new BufferedImage[Tile_1_count + Tile_2_count];
        tile4 = new BufferedImage[Tile_1_count + Tile_2_count];  
        barril_sprites = new BufferedImage[barril_count];
        diegokong_sprites = new BufferedImage[diegokong_count];
        princesaSprites = new BufferedImage[princesa_count];
        fuego_sprites = new BufferedImage[fuego_count]; 
        llama_sprites = new BufferedImage[llama_count]; 
        
        martillo_sprites = new BufferedImage[1];
        paraguas_sprites = new BufferedImage[1];
        bolso_sprites = new BufferedImage[1];
        sombrero_sprites = new BufferedImage[1];
        tileSprites = new HashMap<>();
        cargar = new CargadorImagenes();
        
        try{
           player_sheet = cargar.loadImage(folder + "/testt.png");
           barril_sheet = cargar.loadImage(folder + "/testt.png");
           diegokong_sheet = cargar.loadImage(folder + "/testt.png");
           bloque_sheet = cargar.loadImage(folder + "/bloques3.png");
           //bloque_sheet = cargar.loadImage(folder + "/bloques2.png");
           princesaSheet = cargar.loadImage(folder + "/testt.png");
           fuego_sheet = cargar.loadImage(folder + "/testt.png"); 
           llama_sheet = cargar.loadImage(folder + "/testt.png"); 
           items_sheet = cargar.loadImage(folder + "/testt.png");
         
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        getPlayerTexturas();
        getBarrilTexturas();
        getItemsTexturas();
        getPlayerMartilloTexturas();
        getDiegoKongTexturas();
        getPrincesaTexturas();
        getFuegoTexturas();
        getLlamaTexturas(); 
        getBloquesTexturas();
    }
    
    private void getPlayerTexturas(){
        int x_off = 1;
        int y_off = 1;
        int width = 16;
        int height = 16;
        
        for (int i = 0 ; i < mario_S_count; i++){
            mario_s[i] = player_sheet.getSubimage(x_off + i*(width+2), y_off, width, height);
        }
    }
    
    private void getPlayerMartilloTexturas() {
        int x_off = 1;
        int y_off = 73;
        int width = 32;
        int height = 32;
        
        try {
            for (int i = 0; i < mario_martillo_count; i++) {
                mario_martillo[i] = player_sheet.getSubimage(
                    x_off + i * (width + 2), 
                    y_off, 
                    width, 
                    height
                );
            }
            
            System.out.println("[TEXTURAS] Sprites de Mario con martillo cargados: " + mario_martillo_count);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar sprites de Mario con martillo: " + e.getMessage());
            e.printStackTrace();
            
        
        }
    }
    
private void getItemsTexturas() {
    try {
        // =============================================
        // MARTILLO (1 frame estático)
        // =============================================
        int x_martillo = 1;
        int y_martillo = 55;
        int w_martillo = 16;
        int h_martillo = 16;
        
        martillo_sprites[0] = items_sheet.getSubimage(x_martillo, y_martillo, w_martillo, h_martillo);
        
        // =============================================
        // ITEMS DE BONIFICACIÓN (Paraguas, Bolso, Sombrero)
        // =============================================
        int x_items = 145; // Coordenada X base
        int y_items = 157; // Coordenada Y base
        int w_item = 16;
        int h_item = 16;
        int spacing = 2;   // Espacio entre sprites
        
        // PARAGUAS (posición 0)
        paraguas_sprites[0] = items_sheet.getSubimage(
            x_items, 
            y_items, 
            w_item, 
            h_item
        );
        
        // BOLSO (posición 1 - 2 sprites a la derecha)
        int x_bolso = x_items + (w_item + spacing) * 2;
        bolso_sprites[0] = items_sheet.getSubimage(
            x_bolso, 
            y_items, 
            w_item, 
            h_item
        );
        
        // SOMBRERO (posición 2 - 4 sprites a la derecha)
        int x_sombrero = x_items + (w_item + spacing) * 4;
        sombrero_sprites[0] = items_sheet.getSubimage(
            x_sombrero, 
            y_items, 
            w_item, 
            h_item
        );
        
        // =============================================
        // LOG DE CONFIRMACIÓN
        // =============================================
        System.out.println("[TEXTURAS] ✅ Sprites de items cargados correctamente:");
        System.out.println("  - Martillo:  " + (martillo_sprites[0] != null ? "OK" : "FAIL"));
        System.out.println("  - Paraguas:  " + (paraguas_sprites[0] != null ? "OK" : "FAIL"));
        System.out.println("  - Bolso:     " + (bolso_sprites[0] != null ? "OK" : "FAIL"));
        System.out.println("  - Sombrero:  " + (sombrero_sprites[0] != null ? "OK" : "FAIL"));
        
    } catch (Exception e) {
        System.err.println("[ERROR] ❌ Fallo al cargar sprites de items: " + e.getMessage());
        e.printStackTrace();
        
        // Crear placeholders si falla
        martillo_sprites[0] = crearPlaceholder(16, 16, new java.awt.Color(128, 128, 128));
        paraguas_sprites[0] = crearPlaceholder(16, 16, new java.awt.Color(255, 0, 0));
        bolso_sprites[0] = crearPlaceholder(16, 16, new java.awt.Color(255, 192, 203));
        sombrero_sprites[0] = crearPlaceholder(16, 16, java.awt.Color.BLACK);
    }
}

/**
 * Crea un sprite placeholder simple
 */
private BufferedImage crearPlaceholder(int width, int height, java.awt.Color color) {
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g = img.createGraphics();
    g.setColor(color);
    g.fillRect(0, 0, width, height);
    g.setColor(java.awt.Color.WHITE);
    g.drawRect(0, 0, width - 1, height - 1);
    g.dispose();
    return img;
}
    
    private void getBarrilTexturas(){
        int x_off = 1;
        int y_off = 229;
        int width = 16;
        int height = 16;
        
        for (int i = 0; i < barril_count; i++) {
            barril_sprites[i] = barril_sheet.getSubimage(
                x_off + i * (width + 2), 
                y_off, 
                width, 
                height
            );
        }
    }
    
    private void getDiegoKongTexturas() {
        int width = 48;
        int height = 32;
        int spritesPrimeraFila = 4;

        try {
            // Fila 1 (Y=258)
            int x_off_fila1 = 1;
            int y_off_fila1 = 258;
            for (int i = 0; i < spritesPrimeraFila && i < diegokong_count; i++) {
                diegokong_sprites[i] = diegokong_sheet.getSubimage(
                    x_off_fila1 + i * (width + 2), 
                    y_off_fila1, 
                    width, 
                    height
                );
            }
            
            // Fila 2 (Y=292)
            int x_off_fila2 = 1;
            int y_off_fila2 = 292;
            for (int i = spritesPrimeraFila; i < diegokong_count; i++) {
                int spriteIndexEnFila = i - spritesPrimeraFila;
                diegokong_sprites[i] = diegokong_sheet.getSubimage(
                    x_off_fila2 + spriteIndexEnFila * (width + 2), 
                    y_off_fila2, 
                    width, 
                    height
                );
            }
            
            System.out.println("[TEXTURAS] Sprites de Diego Kong cargados: " + diegokong_count);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar sprites de Diego Kong: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void getPrincesaTexturas(){
        int x_off = 1;
        int y_off = 141;
        int width = 16;
        int height = 32;
        
        for (int i = 0 ; i < princesa_count; i++){
            princesaSprites[i] = princesaSheet.getSubimage(
                x_off + i*(width+2), y_off, width, height
            );
        }
    }
    
    private void getFuegoTexturas() {
        int x_off = 1;
        int y_off = 193;  // Justo debajo de la princesa (141 + 32 + 2)
        int width = 16;
        int height = 16;
        
        try {
            for (int i = 0; i < fuego_count; i++) {
                fuego_sprites[i] = fuego_sheet.getSubimage(
                    x_off + i * (width + 2), 
                    y_off, 
                    width, 
                    height
                );
            }
            
            System.out.println("[TEXTURAS] Sprites de Fuego cargados: " + fuego_count);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar sprites de Fuego: " + e.getMessage());
            System.err.println("[INFO] Asegúrate de que los sprites estén en Y=175 de testt.png");
            e.printStackTrace();
            
            // Crear sprites placeholder si falla
            for (int i = 0; i < fuego_count; i++) {
                fuego_sprites[i] = crearFuegoPlaceholder(width, height, i);
            }
        }
    }
    
    /**
     * Crea un sprite placeholder para el fuego si no se puede cargar
     */
    private BufferedImage crearFuegoPlaceholder(int width, int height, int frame) {
        BufferedImage sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = sprite.createGraphics();
        
        // Colores del fuego alternando
        java.awt.Color[] colores = {
            new java.awt.Color(255, 69, 0),   // Rojo-naranja
            new java.awt.Color(255, 140, 0),  // Naranja
            new java.awt.Color(255, 165, 0),  // Naranja claro
            new java.awt.Color(255, 215, 0)   // Amarillo-naranja
        };
        
        g.setColor(colores[frame % 4]);
        
        // Forma de llama
        int[] xPoints = {width/2, width/4, 0, width/4, width/2, 3*width/4, width, 3*width/4};
        int[] yPoints = {0, height/4, height/2, 3*height/4, height, 3*height/4, height/2, height/4};
        g.fillPolygon(xPoints, yPoints, 8);
        
        // Centro brillante
        g.setColor(java.awt.Color.YELLOW);
        g.fillOval(width/4, height/3, width/2, height/2);
        
        g.dispose();
        return sprite;
    }
    
    private void getLlamaTexturas() {
        int x_off = 163;
        int y_off = 193;  // Justo debajo del fuego (175 + 16 + 2)
        int width = 16;
        int height = 16;  // Más alta para forma de llama
        
        try {
            for (int i = 0; i < llama_count; i++) {
                llama_sprites[i] = llama_sheet.getSubimage(
                    x_off + i * (width + 2), 
                    y_off, 
                    width, 
                    height
                );
            }
            
            System.out.println("[TEXTURAS] Sprites de Llama cargados: " + llama_count);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Fallo al cargar sprites de Llama: " + e.getMessage());
            System.err.println("[INFO] Asegúrate de que los sprites estén en Y=193 de testt.png");
            e.printStackTrace();
            
            // Crear sprites placeholder si falla
            for (int i = 0; i < llama_count; i++) {
                llama_sprites[i] = crearLlamaPlaceholder(width, height, i);
            }
        }
    }
    
    /**
     * Crea un sprite placeholder para la llama si no se puede cargar
     */
    private BufferedImage crearLlamaPlaceholder(int width, int height, int frame) {
        BufferedImage sprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = sprite.createGraphics();
        
        // Colores del fuego
        java.awt.Color colorBase = new java.awt.Color(255, 69, 0);
        java.awt.Color colorMedio = new java.awt.Color(255, 140, 0);
        java.awt.Color colorPunta = new java.awt.Color(255, 215, 0);
        
        int alturaVar = (frame % 2 == 0) ? 2 : -2;
        
        // Base (roja)
        g.setColor(colorBase);
        int[] xBase = {width/2, width/4, 0, width/4, width/2, 3*width/4, width, 3*width/4};
        int[] yBase = {height+alturaVar, height-2, height-4, height-6, height-8, height-6, height-4, height-2};
        g.fillPolygon(xBase, yBase, 8);
        
        // Medio (naranja)
        g.setColor(colorMedio);
        int[] xMedio = {width/2, width/3, width/6, width/3, width/2, 2*width/3, 5*width/6, 2*width/3};
        int[] yMedio = {height-8+alturaVar, height-10, height-12, height-14, height-16, height-14, height-12, height-10};
        g.fillPolygon(xMedio, yMedio, 8);
        
        // Punta (amarilla)
        g.setColor(colorPunta);
        int[] xPunta = {width/2, width/3, width/3, width/2, 2*width/3, 2*width/3};
        int[] yPunta = {0+alturaVar, 4, height/3, height/3-4, height/3, 4};
        g.fillPolygon(xPunta, yPunta, 6);
        
        g.dispose();
        return sprite;
    }
    
    private void getBloquesTexturas() {
        if (bloque_sheet == null) {
            System.err.println("[ERROR] La hoja de sprites 'bloques2.png' no se pudo cargar.");
            return;
        }
        
        final int x_off = 0; 
        final int y_off = 0;
        final int tileWidth = 8;
        final int tileHeight = 8;
        final int x_spacing = 0;
        final int y_spacing = 0;
        final int firstgid = 1;
        final int NUM_FILAS_A_CORTAR = 5;

        int currentTileID = firstgid;
        
        System.out.println("[TEXTURAS] Cargando " + NUM_FILAS_A_CORTAR + " filas de tiles del mapa...");
        
        for (int fila = 0; fila < NUM_FILAS_A_CORTAR; fila++) {
            int y = y_off + fila * (tileHeight + y_spacing);

            if (y + tileHeight > bloque_sheet.getHeight()) {
                System.err.println("[ADVERTENCIA] La fila " + (fila + 1) + " excede el alto de la hoja de sprites.");
                break;
            }

            for (int x = x_off; x + tileWidth <= bloque_sheet.getWidth(); x += tileWidth + x_spacing) {
                BufferedImage sprite = bloque_sheet.getSubimage(x, y, tileWidth, tileHeight);
                tileSprites.put(currentTileID, sprite);
                currentTileID++;
            }
        }
    }
    
    // ==================== GETTERS ====================
    
    public BufferedImage[] getMarioL(){
        return mario_l;
    }
    
    public BufferedImage[] getMarioS(){
        return mario_s;
    }
    
    public BufferedImage[] getMarioMartillo() {
        return mario_martillo;
    }
      
    public BufferedImage[] getTile1(){
        return tile1;
    }
    
    public BufferedImage[] getTile2(){
        return tile2;
    }
    
    public BufferedImage[] getTile3(){
        return tile3;
    }
    
    public BufferedImage[] getTile4(){
        return tile4;
    }
    
    public BufferedImage[] getBarrilSprites() {
        return barril_sprites;
    }
    
    public BufferedImage[] getDiegoKongSprites() {
        return diegokong_sprites;
    }
    
    public BufferedImage getSpritePorID(int tileID) {
        return tileSprites.get(tileID);
    }

    public BufferedImage[] getPrincesaSprites() {
        return princesaSprites;
    }
    
    public BufferedImage[] getFuegoSprites() {
        return fuego_sprites;
    }
    
    public BufferedImage[] getLlamaSprites() {
        return llama_sprites;
    }
    public BufferedImage[] getMartilloSprites() {
        return martillo_sprites;
    }
    
    public BufferedImage[] getParaguasSprites() {
        return paraguas_sprites;
    }
    
    public BufferedImage[] getBolsoSprites() {
        return bolso_sprites;
    }
    
    public BufferedImage[] getSombreroSprites() {
        return sombrero_sprites;
    }
}