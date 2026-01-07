import MainGame.game
import hevs.graphics.FunGraphics
import hevs.graphics.utils.GraphicsBitmap

import java.awt.event.{KeyAdapter, KeyEvent, MouseAdapter, MouseEvent, MouseMotionAdapter}
import java.awt.Color

class Button(val x: Int,
             val y: Int,
             val width: Int,
             val height: Int) {

  // État du bouton
  var isOn: Boolean = false

  //texte du bouton
  var text: String = ""

  // Pour éviter de déclencher 15 fois pendant qu'on garde la souris enfoncée
  private var wasPressedLastFrame: Boolean = false

  /**
   * Dessine le bouton
   */
  def draw(g: FunGraphics): Unit = {
    // Couleur différente selon l'état
    val fillColor =
      if (isOn) Color.gray
      else Color.LIGHT_GRAY

    // Fond
    g.setColor(fillColor)
    g.drawFillRect(x, y, width, height)

    // Bordure
    g.setColor(Color.BLACK)
    g.drawRect(x, y, width, height)

    g.drawString(x + text.length * 5, y + height - 10, text)
  }

  /**
   * Met à jour l'état du bouton en fonction de la souris.
   *
   * @return true si le bouton vient d'être cliqué à cette frame
   */
  def update(mouseX: Int, mouseY: Int, mousePressed: Boolean): Boolean = {
    val inside = contains(mouseX, mouseY)
    val justPressedNow = mousePressed && !wasPressedLastFrame
    val justClicked = inside && justPressedNow
    if (justClicked) {
      isOn = !isOn // on inverse l'état
    }
    wasPressedLastFrame = mousePressed
    justClicked
  }

  /**
   * Test si (mx,my) est à l'intérieur du rectangle
   */
  private def contains(mx: Int, my: Int): Boolean =
    mx >= x && mx <= x + width &&
      my >= y && my <= y + height
}

object Display {
  val pixel_value: Int = setting_screen.zoom
  val wallFile = new GraphicsBitmap("/res/wall 16x16.jpg")
  val solFile = new GraphicsBitmap("/res/sol 16x16.jpg")
  val persFile = new GraphicsBitmap("/res/perso 16x16.jpg")
  val rocFile = new GraphicsBitmap("/res/end_flag 16x16.jpg")//place holder

  def blit(grid: Array[Array[Int]]): Unit = {
    Game_screen.gameWindow.frontBuffer.synchronized {
      Game_screen.gameWindow.clear()
      for ((x, xPos) <- grid.zipWithIndex;
           (y, yPos) <- x.zipWithIndex) {
        if (y == 0) { // 0 in the grid is a pathway
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, solFile)
        }
        if (y == 1) { // 1 in the grid is a wall
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, wallFile)
        }
        if (y == 2) { // 2 in the grid is a breakable block
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, rocFile)
        }
        if (y == 4) { // 4 in the grid is the player1
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, persFile)
        }
        if (y == 5) { // 5 in the grid is the player2
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, persFile)
        }
      }
    }
  }
}

object Motor {
  def generategame(width: Int, height: Int, visualize: Boolean = false): Array[Array[Int]] = {
    var game: Array[Array[Int]] = Array.ofDim[Int](width, height)
    for(x<-0 until game.length){
      for(y<-0 until game(x).length){
        if ((x==0)||(y==0)||(x==game.length-1)||(y==game(x).length-1)){//contours
          game(x)(y)=1
        }
        if ((x%2==0)&&(y%2==0)){//pilliers
          game(x)(y)=1
        }
        if (game(x)(y)==0){
          if (math.random < 0.75){
            game(x)(y)=2
          }
        }
      }
    }
    Display.blit(game)
    return game
  }
}

object Player1 {
  //spawn du joueur
  var x: Int = 1
  var y: Int = 1
  MainGame.game(Player1.x+1)(Player1.y) = 0
  MainGame.game(Player1.x)(Player1.y+1) = 0

  def Nextpos(): Unit = {
    MainGame.game(Player1.x)(Player1.y) = 4
    Display.blit(MainGame.game)
  }
}
object Player2 {
  //spawn du joueur
  var x: Int = Game_screen.WIDTH -2
  var y: Int = Game_screen.HEIGHT -2
  MainGame.game(Player2.x-1)(Player2.y) = 0
  MainGame.game(Player2.x)(Player2.y-1) = 0

  def Nextpos(): Unit = {
    MainGame.game(Player2.x)(Player2.y) = 5
    Display.blit(MainGame.game)
  }
}
object setting_screen {
  var START: Boolean = false
  val settingWindow: FunGraphics = new FunGraphics(600, 400, "Maze Settings")

  // --- ÉTAT DE LA SOURIS ---
  var mouseX: Int = 0
  var mouseY: Int = 0

  // On met à jour mouseX / mouseY quand la souris bouge
  settingWindow.addMouseMotionListener(new MouseMotionAdapter {
    override def mouseMoved(e: MouseEvent): Unit = {
      mouseX = e.getX
      mouseY = e.getY
    }

    override def mouseDragged(e: MouseEvent): Unit = {
      mouseX = e.getX
      mouseY = e.getY
    }
  })

  var mouseDown: Boolean = false
  var click_DONE = false

  // On met à jour mousePressed quand on appuie / relâche
  settingWindow.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      mouseDown = true
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      mouseDown = false
      click_DONE = false
      super.mouseReleased(e)
    }
  })

  // --- BOUTONS ---
  val MIN_ZOOM: Button = new Button(120, 40, 40, 40)
  val ADD_ZOOM: Button = new Button(260, 40, 40, 40)

  val MIN_WIDTH: Button = new Button(120, 100, 40, 40)
  val ADD_WIDTH: Button = new Button(260, 100, 40, 40)

  val MIN_HIGHT: Button = new Button(120, 160, 40, 40)
  val ADD_HIGHT: Button = new Button(260, 160, 40, 40)

  val START_BUTTON: Button = new Button(475, 320, 115, 40)

  // --- VALEURS ---
  var zoom = 28 // taille des tiles en pixels
  var largeur = 10 // nb de cases en largeur
  var hauteur = 10 // nb de cases en hauteur

  // Méthode à appeler depuis ton main : setting_screen.run()
  def run(): Unit = {
    START = false

    while (START == false) {
      // LOGIQUE DES BOUTONS

      // Zoom
      if (MIN_ZOOM.update(mouseX, mouseY, mouseDown) && zoom > 4)
        zoom -= 1
      if (ADD_ZOOM.update(mouseX, mouseY, mouseDown))
        zoom += 1

      // Largeur
      if (MIN_WIDTH.update(mouseX, mouseY, mouseDown) && largeur > 2)
        largeur -= 1
      if (ADD_WIDTH.update(mouseX, mouseY, mouseDown))
        largeur += 1

      // Hauteur
      if (MIN_HIGHT.update(mouseX, mouseY, mouseDown) && hauteur > 2)
        hauteur -= 1
      if (ADD_HIGHT.update(mouseX, mouseY, mouseDown))
        hauteur += 1

      // Start
      if (START_BUTTON.update(mouseX, mouseY, mouseDown))
        START = true

      // DESSIN
      settingWindow.clear(Color.WHITE) // efface tout

      // --- Zoom ---
      settingWindow.drawString(50, 70, "Zoom :", Color.BLACK, 18)
      settingWindow.drawRect(120, 40, 180, 40)
      MIN_ZOOM.text = "-"
      ADD_ZOOM.text = "+"
      MIN_ZOOM.draw(settingWindow)
      ADD_ZOOM.draw(settingWindow)
      settingWindow.drawString(200, 70, zoom.toString, Color.BLACK, 18)

      // --- Width ---
      settingWindow.drawString(50, 130, "Width :", Color.BLACK, 18)
      settingWindow.drawRect(120, 100, 180, 40)
      MIN_WIDTH.text = "-"
      ADD_WIDTH.text = "+"
      MIN_WIDTH.draw(settingWindow)
      ADD_WIDTH.draw(settingWindow)
      settingWindow.drawString(200, 130, largeur.toString, Color.BLACK, 18)

      // --- Height ---
      settingWindow.drawString(50, 190, "Height :", Color.BLACK, 18)
      settingWindow.drawRect(120, 160, 180, 40)
      MIN_HIGHT.text = "-"
      ADD_HIGHT.text = "+"
      MIN_HIGHT.draw(settingWindow)
      ADD_HIGHT.draw(settingWindow)
      settingWindow.drawString(200, 190, hauteur.toString, Color.BLACK, 18)

      // --- Start ---
      START_BUTTON.draw(settingWindow)
      START_BUTTON.text = "START"

      // Stabilise le framerate (~FPS)
      settingWindow.syncGameLogic(60)
    }

    // Quand tu sors de la boucle, tu as les valeurs choisies
    println(s"Paramètres finaux : zoom=$zoom, largeur=$largeur, hauteur=$hauteur")
  }
}

object Game_screen {
  var WIDTH: Int = setting_screen.largeur
  if (WIDTH % 2 == 0) {
    WIDTH += 1
  } //on veux que la largeur soit impair
  var HEIGHT: Int = setting_screen.hauteur
  if (HEIGHT % 2 == 0) {
    HEIGHT += 1
  } //on veux que la hauteur soit impair

  val gameWindow: FunGraphics = new FunGraphics(WIDTH * Display.pixel_value, HEIGHT * Display.pixel_value, "Game 2025")
  gameWindow.syncGameLogic(8)
}

object MainGame extends App {

  val first_screen: FunGraphics = setting_screen.settingWindow
  setting_screen.run()
  if (setting_screen.START == true) {
    val second_screen: FunGraphics = Game_screen.gameWindow
    second_screen.setKeyManager(new KeyAdapter() {
      override def keyPressed(e: KeyEvent): Unit = {
        game(Player1.x)(Player1.y) = 0
        if (e.getKeyCode == KeyEvent.VK_UP) {
          if (game(Player1.x)(Player1.y - 1) == 0) Player1.y -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_DOWN) {
          if (game(Player1.x)(Player1.y + 1) == 0) Player1.y += 1
        }
        if (e.getKeyCode == KeyEvent.VK_LEFT) {
          if (game(Player1.x - 1)(Player1.y) == 0) Player1.x -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_RIGHT) {
          if (game(Player1.x + 1)(Player1.y) == 0) Player1.x += 1
        }

        game(Player2.x)(Player2.y) = 0
        if (e.getKeyCode == KeyEvent.VK_W) {
          if (game(Player2.x)(Player2.y - 1) == 0) Player2.y -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_S) {
          if (game(Player2.x)(Player2.y + 1) == 0) Player2.y += 1
        }
        if (e.getKeyCode == KeyEvent.VK_A) {
          if (game(Player2.x - 1)(Player2.y) == 0) Player2.x -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_D) {
          if (game(Player2.x + 1)(Player2.y) == 0) Player2.x += 1
        }
      }
    })
  }
  var game: Array[Array[Int]] = Motor.generategame(Game_screen.WIDTH, Game_screen.HEIGHT)
  while(true){
    Player1.Nextpos
    Player2.Nextpos
    Display.blit(game)
  }
}



