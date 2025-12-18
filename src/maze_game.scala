import End.{endx, endy}
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
  val endFile = new GraphicsBitmap("/res/end_flag 16x16.jpg")
  val persFile = new GraphicsBitmap("/res/perso 16x16.jpg")

  def blit(grid: Array[Array[Int]]): Unit = {
    maze_screen.mazeWindow.frontBuffer.synchronized {
      maze_screen.mazeWindow.clear()
      for ((x, xPos) <- grid.zipWithIndex;
           (y, yPos) <- x.zipWithIndex) {
        if (y == 0) { // 0 in the grid is a pathway
          maze_screen.mazeWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, solFile)
        }
        if (y == 1) { // 1 in the grid is a wall
          maze_screen.mazeWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, wallFile)
        }
        if (y == 4) { // 4 in the grid is the player
          maze_screen.mazeWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, persFile)
        }
        if (y == 5) { // 5 in the grid is the end
          maze_screen.mazeWindow.drawTransformedPicture(xPos * pixel_value + pixel_value / 2, yPos * pixel_value + pixel_value / 2, 0, pixel_value / 16, endFile)
        }
      }
      maze_screen.mazeWindow.drawString(maze_screen.WIDTH * pixel_value / 4, maze_screen.HEIGHT * pixel_value / 2,s"${game.win}","",1,pixel_value,Color.RED,2,2)

      maze_screen.mazeWindow.drawString(pixel_value / 8,pixel_value * 2/3,s"Nombre de pas : ${game.nbr_pas}","",1,pixel_value / 2,Color.WHITE)

    }
  }
}

object Maze {
  def randomInValid(array: Array[Int]): Int = {
    var numOfValid: Int = 0
    for (i <- array) {
      if (i == 1) numOfValid += 1
    }
    val ran: Int = (math.random() * numOfValid).toInt + 1
    var validcount: Int = 0
    for (i <- array.indices) {
      if (array(i) == 1) validcount += 1
      if (validcount == ran) return i
    }
    15
  }

  def generateMaze(width: Int, height: Int, visualize: Boolean = false): Array[Array[Int]] = {
    val maze: Array[Array[Int]] = Array.ofDim[Int](width, height)

    for ((y, yPos) <- maze.zipWithIndex;
         (_, xPos) <- y.zipWithIndex) {
      if ((yPos % 2) == 0 || (xPos % 2) == 0) {
        maze(yPos)(xPos) = 1
      }
    }

    val cells: Array[Array[Int]] = Array.ofDim[Int](width / 2, height / 2)


    /** *
     * 0 -> unvisited
     * 1 -> wall
     * 2 -> visited
     * 3 -> visited and backtracked
     */
    var posX: Int = 0
    var posY: Int = 0
    cells(0)(0) = 2
    var visitedAll: Boolean = false
    do {
      val validArray: Array[Int] = new Array[Int](4) // Array that stores which values are valid
      //left-down-right-up
      if (posX - 1 < 0) {
        validArray(0) = 0
      } else if (cells(posY)(posX - 1) == 2 || cells(posY)(posX - 1) == 3) {
        validArray(0) = 0
      } else {
        validArray(0) = 1
      }

      if (posY - 1 < 0) {
        validArray(1) = 0
      } else if (cells(posY - 1)(posX) == 2 || cells(posY - 1)(posX) == 3) {
        validArray(1) = 0
      } else {
        validArray(1) = 1
      }

      if (posX + 1 >= cells(0).length) {
        validArray(2) = 0
      } else if (cells(posY)(posX + 1) == 2 || cells(posY)(posX + 1) == 3) {
        validArray(2) = 0
      } else {
        validArray(2) = 1
      }

      if (posY + 1 >= cells.length) {
        validArray(3) = 0
      } else if (cells(posY + 1)(posX) == 2 || cells(posY + 1)(posX) == 3) {
        validArray(3) = 0
      } else {
        validArray(3) = 1
      }

      val direction: Int = randomInValid(validArray) // 0: left 1: down 2: right 3: up
      direction match {
        case 0 => maze(posY * 2 + 1)(posX * 2 - 1 + 1) = 0; posX -= 1
        case 1 => maze(posY * 2 - 1 + 1)(posX * 2 + 1) = 0; posY -= 1
        case 2 => maze(posY * 2 + 1)(posX * 2 + 1 + 1) = 0; posX += 1
        case 3 => maze(posY * 2 + 1 + 1)(posX * 2 + 1) = 0; posY += 1
        case _ =>
      }

      cells(posY)(posX) = 2

      visitedAll = true
      for (x <- cells; y <- x) {
        if (y != 2) visitedAll = false
      }

      var foundNewStart: Boolean = false
      if (direction == 15) {
        while (!foundNewStart) {
          posY = (math.random() * cells.length).toInt
          posX = (math.random() * cells(0).length).toInt
          if (cells(posY)(posX) == 2) {
            foundNewStart = true
          }
        }
      }
      if (visualize && !foundNewStart) {
        Display.blit(maze)
      }
    }
    while (!visitedAll)


    maze
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
  var zoom = 32 // taille des tiles en pixels
  var largeur = 8 // nb de cases en largeur
  var hauteur = 8 // nb de cases en hauteur

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

object maze_screen {
  var WIDTH: Int = setting_screen.largeur
  if (WIDTH % 2 == 0) {
    WIDTH += 1
  } //on veux que la largeur soit impair
  var HEIGHT: Int = setting_screen.hauteur
  if (HEIGHT % 2 == 0) {
    HEIGHT += 1
  } //on veux que la hauteur soit impair
  var visualize: Boolean = true

  val mazeWindow: FunGraphics = new FunGraphics(WIDTH * Display.pixel_value, HEIGHT * Display.pixel_value, "Maze Game")
  mazeWindow.syncGameLogic(8)
}

object End {
  var game_over:Boolean = false
  var endx: Int = maze_screen.WIDTH - 2
  var endy: Int = maze_screen.HEIGHT - 2

  game.maze(endx)(endy) = 5 //end of the maze

  while (game.maze(endx)(endy) == 5) {
    Player.Nextpos
    Display.blit(game.maze)
  }
  if (game.maze(endx)(endy) != 5){
    game_over = true
  }
}

object Player {
  //spawn du joueur
  var x: Int = 1
  var y: Int = 1

  def Nextpos(): Unit = {
    game.maze(Player.x)(Player.y) = 4
  }
}

object game extends App {

  var nbr_pas = 0
  var win:String =""
  val first_screen: FunGraphics = setting_screen.settingWindow
  setting_screen.run()
  if (setting_screen.START == true) {
    val second_screen: FunGraphics = maze_screen.mazeWindow
    second_screen.setKeyManager(new KeyAdapter() {
      override def keyPressed(e: KeyEvent): Unit = {
        maze(Player.x)(Player.y) = 0
        if (e.getKeyCode == KeyEvent.VK_UP) {
          if (maze(Player.x)(Player.y - 1) != 1){
            Player.y -= 1
            nbr_pas +=1
          }
        }
        if (e.getKeyCode == KeyEvent.VK_DOWN) {
          if (maze(Player.x)(Player.y + 1) != 1) {
            Player.y += 1
            nbr_pas +=1
          }
        }
        if (e.getKeyCode == KeyEvent.VK_LEFT) {
          if (maze(Player.x - 1)(Player.y) != 1) {
            Player.x -= 1
            nbr_pas +=1
          }
        }
        if (e.getKeyCode == KeyEvent.VK_RIGHT) {
          if (maze(Player.x + 1)(Player.y) != 1) {
            Player.x += 1
            nbr_pas +=1
          }
        }
      }
    })
  }
  var maze: Array[Array[Int]] = Maze.generateMaze(maze_screen.WIDTH, maze_screen.HEIGHT, maze_screen.visualize)
  End
  if (End.game_over == true){
    win="YOU WIN !"
    Display.blit(maze)
  }
}



