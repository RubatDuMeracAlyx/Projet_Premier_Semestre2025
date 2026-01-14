# **!!! A FAIRE AVANT DE JOUER !!!**

Après avoir installé le projet dans IntelliJ, il faut impérativement pour jouer : 

  - Importez un **SDK** (en haut à droite quand vous avez finis d'installer)
  - **[clic droit]** sur le dossier **[src]** et ensuite **"ajouter comme source"**
  - Redéclarer fungraphics comme **library** (clic droit sur le fichier puis -> "Add as a Library") 

# **BOMBER MAN**

// Pour lancer le jeu : Placer vous dans le document **"maze_game.scala"** puis faites **Run**.

  ## 1. **Description**
  
Notre jeu est un Bomber-man.
Bomber-man ce joue en 1 contre 1 sur le même écran, ou les 2 joueurs s'affronte sur un carte dont ils peuvent choisir la taille et le zoom.
Le but ? Faire exploser son adversaire ! Pour ce faire vous devrez détruire les caisses qui vous sépare et user de faintes afin de surpasser votre adversaire.

<img width="447" height="299" alt="game-settingscreen" src="https://github.com/user-attachments/assets/7adcf7e5-5e4d-4ff6-a86a-b64fa81fd8fb" />

<img width="703" height="436" alt="game-mainscreen" src="https://github.com/user-attachments/assets/3d63c1c8-d38d-4426-8c97-d86f50a067c3" />

  ## 2. **Mode d'emplois**

### _CONTRÔLES :_

JOUEUR 1 // ROUGE (en haut à gauche) 
- Flèche haut : Aller en haut
- Flèche de droite : Aller à droite
- Flèche de gauche : Aller à gauche
- Flèche du bas : Aller en bas
- Shift : Poser une bombe (Note : utiliser le shift droit c'est mieux)

JOUEUR 2 // VERT (en bas à droite)
- 'w' : Aller en haut
- 'd' : Aller à droite
- 'a' : Aller à gauche
- 's' : Aller en bas
- 'e' : Poser une bombe

## Structure du code Bomberman

### Architecture générale

Le code est organisé en plusieurs objets et classes Scala qui gèrent différents aspects du jeu :

#### Classes utilitaires
- **`Button`** : Gère les boutons interactifs (clics, états on/off, affichage)

#### Objets d'affichage
- **`Display`** : Charge les ressources graphiques (sprites) et gère le rendu de la grille avec la méthode `blit()`
- **`setting_screen`** : Écran de configuration initial permettant de régler le zoom, la largeur et la hauteur du terrain
- **`Game_screen`** : Fenêtre de jeu principale

#### Logique du jeu
- **`Motor`** : Génère la grille de jeu (murs fixes, blocs destructibles, espaces vides)
- **`Bomb`** : Classe gérant le cycle de vie d'une bombe (placement, timer, explosion en croix, disparition)
- **`Player1`** et **`Player2`** : Gèrent la position et les actions des deux joueurs (déplacement, pose de bombe)

#### Boucle principale
- **`MainGame`** : Point d'entrée qui :
  1. Lance l'écran de configuration
  2. Initialise la partie
  3. Gère les contrôles clavier (flèches + Shift pour J1, WASD + Space pour J2)
  4. Exécute la boucle de jeu avec détection de mort et affichage du gagnant

### Système de grille

Le jeu utilise un système de grille où chaque case contient un entier représentant son type :
- **0** = vide
- **1** = mur fixe
- **2** = bloc destructible
- **4** = joueur 1
- **5** = joueur 2
- **6** = bombe
- **7** = explosion



