import hevs.graphics.FunGraphics
import hevs.graphics.utils.GraphicsBitmap
import scala.util.control.Breaks._

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
  val wallFile = new GraphicsBitmap("/res/bloc.png")
  val solFile = new GraphicsBitmap("/res/sol.png")
  val persFile = new GraphicsBitmap("/res/joueur.png")
  val rocFile = new GraphicsBitmap("/res/bloc (1).png")//place holder
  val bombFile = new GraphicsBitmap("/res/bomb.png")
  val bomb32x32File = new GraphicsBitmap("/res/bomb32x32.png")
  val bomb64x64File = new GraphicsBitmap("/res/bomb64x64.png")
  val titleWhiteFile = new GraphicsBitmap("/res/titleWhite.png")
  val crate100x100File = new GraphicsBitmap("/res/crate100x100.png")
  val boomFile = new GraphicsBitmap("/res/explosion.png")
  val pers2File = new GraphicsBitmap("/res/joueur2.png")


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
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, pers2File)
        }
        if (y == 6) { // 6 in the grid is a Bomb
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, bombFile)
        }
        if (y == 7) { // 4 in the grid is an explosion
          Game_screen.gameWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, boomFile)
        }
      }
      Game_screen.gameWindow.drawString(Game_screen.WIDTH * pixel_value / 4, Game_screen.HEIGHT * pixel_value / 2,s"${MainGame.win}","",1,pixel_value,Color.WHITE,2,2)
    }
  }
}

object Motor {
  def generategame(width: Int, height: Int): Array[Array[Int]] = {
    var game: Array[Array[Int]] = Array.ofDim[Int](width, height)
    for (x <- 0 until game.length) {
      for (y <- 0 until game(x).length) {
        if ((x == 0) || (y == 0) || (x == game.length - 1) || (y == game(x).length - 1)) { //contours
          game(x)(y) = 1
        }
        if ((x % 2 == 0) && (y % 2 == 0)) { //pilliers
          game(x)(y) = 1
        }
        if (game(x)(y) == 0) {
          if (math.random < 0.75) {
            game(x)(y) = 2
          }
        }
      }
    }
    return game
  }
}

class Bomb(bombX: Int, bombY: Int) {
  val time_before_explosion = 10000
  val explosion_time = 3000
  val West = (1+math.random()*3).toInt
  val East = (1+math.random()*3).toInt
  val North = (1+math.random()*3).toInt
  val South = (1+math.random()*3).toInt
  var bomb_placed = false
  var remainig_time = time_before_explosion + explosion_time

  def place_bomb(): Boolean = {
    MainGame.game(bombX)(bombY) = 6
    MainGame.timemanager(bombX)(bombY) = remainig_time
    bomb_placed = true
    return bomb_placed
  }

  def tick(): Int = {
    if (bomb_placed == true) {
      MainGame.timemanager(bombX)(bombY) -= 1
      remainig_time = MainGame.timemanager(bombX)(bombY)
    }
    return remainig_time
  }

  def explode(): Unit = {
    // centre
    MainGame.game(bombX)(bombY) = 7
    // WEST
    breakable {
      for (i <- 1 to West) {
        val nx = bombX - i
        val ny = bombY

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 7
      }
    }
    // EAST
    breakable {
      for (i <- 1 to East) {
        val nx = bombX + i
        val ny = bombY

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 7
      }
    }
    // NORTH
    breakable {
      for (i <- 1 to North) {
        val nx = bombX
        val ny = bombY - i

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 7
      }
    }
    // SOUTH
    breakable {
      for (i <- 1 to South) {
        val nx = bombX
        val ny = bombY + i

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 7
      }
    }
  }

  def unexplode(): Unit = {
    // centre
    MainGame.game(bombX)(bombY) = 0
    // WEST
    breakable {
      for (i <- 1 to West) {
        val nx = bombX - i
        val ny = bombY

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 0
      }
    }
    // EAST
    breakable {
      for (i <- 1 to East) {
        val nx = bombX + i
        val ny = bombY

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 0
      }
    }
    // NORTH
    breakable {
      for (i <- 1 to North) {
        val nx = bombX
        val ny = bombY - i

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 0
      }
    }
    // SOUTH
    breakable {
      for (i <- 1 to South) {
        val nx = bombX
        val ny = bombY + i

        if (MainGame.game(nx)(ny) == 1) break

        MainGame.game(nx)(ny) = 0
      }
    }
  }

  def manage(): Unit = {
    if (bomb_placed == true) {
      MainGame.game(bombX)(bombY) = 6
      if (remainig_time <= explosion_time) {
        explode()
        if (remainig_time <= explosion_time / 2) {
          unexplode()
          bomb_placed = false
        }
      }
    }
  }
}

object Player1 {
  //spawn du joueur
  var x: Int = 1
  var y: Int = 1
  MainGame.game(x + 1)(y) = 0
  MainGame.game(x)(y + 1) = 0

  var currentBomb: Option[Bomb] = None

  def Nextpos(): Unit = {
    MainGame.game(x)(y) = 4
  }

  def bomb(): Unit = {
    val b = new Bomb(x, y)
    b.place_bomb()
    currentBomb = Some(b)
  }
}

object Player2 {
  //spawn du joueur
  var x: Int = Game_screen.WIDTH - 2
  var y: Int = Game_screen.HEIGHT - 2
  MainGame.game(x - 1)(y) = 0
  MainGame.game(x)(y - 1) = 0

  var currentBomb: Option[Bomb] = None

  def Nextpos(): Unit = {
    MainGame.game(x)(y) = 5
  }

  def bomb(): Unit = {
    val b = new Bomb(x, y)
    b.place_bomb()
    currentBomb = Some(b)
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
  val MIN_ZOOM: Button = new Button(120, 200, 40, 40)
  val ADD_ZOOM: Button = new Button(260, 200, 40, 40)

  val MIN_WIDTH: Button = new Button(120, 270, 40, 40)
  val ADD_WIDTH: Button = new Button(260, 270, 40, 40)

  val MIN_HIGHT: Button = new Button(120, 340, 40, 40)
  val ADD_HIGHT: Button = new Button(260, 340, 40, 40)

  val START_BUTTON: Button = new Button(450, 340, 110, 40)

  // --- VALEURS ---
  var zoom = 45 // taille des tiles en pixels
  var largeur = 15 // nb de cases en largeur
  var hauteur = 15 // nb de cases en hauteur

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
      settingWindow.clear(Color.GRAY) // efface tout
      settingWindow.drawTransformedPicture(300,40,0,1.7,Display.titleWhiteFile)
      settingWindow.drawTransformedPicture(530,300, 0,0.6,Display.bomb64x64File)
      settingWindow.drawTransformedPicture(200,175, 0,0.5,Display.crate100x100File)
      settingWindow.drawTransformedPicture(260,180, 0,0.4,Display.crate100x100File)
      settingWindow.drawTransformedPicture(240,128, 0.35,0.5,Display.crate100x100File)
      settingWindow.drawTransformedPicture(530,320, 0,0.4,Display.crate100x100File)
      settingWindow.drawTransformedPicture(495,330, 0,0.2,Display.crate100x100File)

      // --- Zoom ---
      settingWindow.drawString(50, 230, "Zoom :", Color.WHITE, 18)
      settingWindow.drawRect(120, 200, 180, 40)
      MIN_ZOOM.text = " -"
      ADD_ZOOM.text = " +"
      MIN_ZOOM.draw(settingWindow)
      ADD_ZOOM.draw(settingWindow)
      settingWindow.drawString(200, 230, zoom.toString, Color.WHITE, 18)

      // --- Width ---
      settingWindow.drawString(50, 300, "Width :", Color.WHITE, 18)
      settingWindow.drawRect(120, 270, 180, 40)
      MIN_WIDTH.text = " -"
      ADD_WIDTH.text = " +"
      MIN_WIDTH.draw(settingWindow)
      ADD_WIDTH.draw(settingWindow)
      settingWindow.drawString(200, 300, largeur.toString, Color.WHITE, 18)

      // --- Height ---
      settingWindow.drawString(50, 370, "Height :", Color.WHITE, 18)
      settingWindow.drawRect(120, 340, 180, 40)
      MIN_HIGHT.text = " -"
      ADD_HIGHT.text = " +"
      MIN_HIGHT.draw(settingWindow)
      ADD_HIGHT.draw(settingWindow)
      settingWindow.drawString(200, 370, hauteur.toString, Color.WHITE, 18)

      // --- Start ---
      START_BUTTON.draw(settingWindow)
      START_BUTTON.text = "START"

      // Stabilise le framerate (~FPS)
      settingWindow.syncGameLogic(10)
      Thread.sleep(20)
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
  gameWindow.syncGameLogic(60)
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
          if (game(Player1.x)(Player1.y - 1) != 1 && game(Player1.x)(Player1.y - 1) != 2) Player1.y -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_DOWN) {
          if (game(Player1.x)(Player1.y + 1) != 1 && game(Player1.x)(Player1.y + 1) != 2) Player1.y += 1
        }
        if (e.getKeyCode == KeyEvent.VK_LEFT) {
          if (game(Player1.x - 1)(Player1.y) != 1 && game(Player1.x - 1)(Player1.y) != 2) Player1.x -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_RIGHT) {
          if (game(Player1.x + 1)(Player1.y) != 1 && game(Player1.x + 1)(Player1.y) != 2) Player1.x += 1
        }
        if (e.getKeyCode == KeyEvent.VK_SHIFT) {
          Player1.bomb()
        }

        game(Player2.x)(Player2.y) = 0
        if (e.getKeyCode == KeyEvent.VK_W) {
          if (game(Player2.x)(Player2.y - 1) != 1 && game(Player2.x)(Player2.y - 1) != 2) Player2.y -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_S) {
          if (game(Player2.x)(Player2.y + 1) != 1 && game(Player2.x)(Player2.y + 1) != 2) Player2.y += 1
        }
        if (e.getKeyCode == KeyEvent.VK_A) {
          if (game(Player2.x - 1)(Player2.y) != 1 && game(Player2.x - 1)(Player2.y) != 2) Player2.x -= 1
        }
        if (e.getKeyCode == KeyEvent.VK_D) {
          if (game(Player2.x + 1)(Player2.y) != 1 && game(Player2.x + 1)(Player2.y) != 2) Player2.x += 1
        }
        if (e.getKeyCode == KeyEvent.VK_SPACE) {
          Player2.bomb()
        }
      }
    })
  }

  var gameOver: Boolean = false
  var game: Array[Array[Int]] = Motor.generategame(Game_screen.WIDTH, Game_screen.HEIGHT)
  var timemanager: Array[Array[Int]] = Array.ofDim(Game_screen.WIDTH, Game_screen.HEIGHT)
  var win:String=""
  while (!gameOver) {
    Thread.sleep(20)

    for (x <- 0 until timemanager.length) {
      for (y <- 0 until timemanager(x).length) {
        Player1.currentBomb.foreach { b =>
          b.tick()
          b.manage()
        }
        Player2.currentBomb.foreach { b =>
          b.tick()
          b.manage()
        }
      }
    }

    // Vérification mort joueur 1 et 2
    if(game(Player1.x)(Player1.y) == 7 && game(Player2.x)(Player2.y) == 7){
      win="Draw!"
    }
    // Vérification mort joueur 1
    else if (game(Player1.x)(Player1.y) == 7) {
      win="Player 2 win"
      gameOver = true
    }
    // Vérification mort joueur 2
    else if (game(Player2.x)(Player2.y) == 7) {
      win="Player 1 win"
      gameOver = true
    }

    Player1.Nextpos
    Player2.Nextpos

    Display.blit(game)
  }
}



