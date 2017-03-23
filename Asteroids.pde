/*
Asteroids.pde: The classic game "Asteroids" written in Processing
 Author: Palmer Paul
 Email: pzpaul2002@yahoo.com
 Twitter: @pzp1997
 Version: 1.3.0 (03/23/17 01:10)
 Copyright: (c) 2014-2017, Palmer Paul
 */

import ddf.minim.*;

PShape shipShape;
PShape asterShape;
PImage[] imgs = new PImage[4];
AudioSample[] sounds = new AudioSample[6];
ArrayList<Asteroid> asteroids;
ArrayList<Proj> projectiles;
int[] asterPntLvls = new int[3];
int[] asterSzLvls = new int[3];
int lives;
int score;
int bonus;
int sound;
boolean paused;
Ship ship;
Button pauseBtn;
Button soundBtn;
Minim minim;

final int pointBonus = 5000;
final float asterMinSpeed = 1.5;
final float asterMaxSpeed = 2.0;
final int maxProjs = 10;

void setup() {
  fullScreen(P2D);
  frameRate(60);
  rectMode(CENTER);
  imageMode(CENTER);
  shapeMode(CENTER);
  minim = new Minim(this);

  shipShape = loadShape("Ship.svg");
  shipShape.setStrokeWeight(30);
  shipShape.setStroke(color(255));
  asterShape = loadShape("Asteroid.svg");
  asterShape.setStroke(color(255));

  imgs[0] = loadImage("Mute.png");
  imgs[1] = loadImage("Sound.png");
  imgs[2] = loadImage("Daniel.png");
  imgs[3] = loadImage("Pause.png");

  sounds[0] = minim.loadSample("laser.mp3");
  sounds[1] = minim.loadSample("pop.mp3");
  sounds[2] = minim.loadSample("explosion.mp3");
  sounds[3] = minim.loadSample("laserDaniel.mp3");
  sounds[4] = minim.loadSample("popDaniel.mp3");
  sounds[5] = minim.loadSample("explosionDaniel.mp3");

  asterPntLvls[0] = 20;
  asterPntLvls[1] = 50;
  asterPntLvls[2] = 100;
  asterSzLvls[0] = 100;
  asterSzLvls[1] = 50;
  asterSzLvls[2] = 20;

  pauseBtn = new Button(width-30, 30, 40);
  soundBtn = new Button(width-80, 30, 40);

  displayMessage("ASTEROIDS", "Press SPACE to play!");
  text("by Palmer Paul", width-300, height-30);
  startGame();
}

void draw() {
  if (paused) {
    if (keyPressed) {
      if (key == ' ') {
        paused = false;
      }
    }
  } else {
    if (asteroids.size() == 0) {
      genAsters(int(random(4, 6)));
    } 

    if (bonus >= pointBonus) {
      lives += 1;
      bonus -= pointBonus;
    }

    background(0);

    scoreboard();
    pauseBtn.update();
    soundBtn.update();
    image(imgs[3], width-30, 30, 25, 25);
    image(imgs[sound], width-80, 30, 30, 30);

    ship.update();
    ship.display();

    for (int a = asteroids.size () - 1; a >= 0; a--) {
      Asteroid asteroid = asteroids.get(a);
      asteroid.update();
      if (!ship.immune) {
        if (asteroid.hasCollided(ship.x, ship.y, ship.sz/2)) {
          asteroid.destroy();
          ship.destroy();
          break;
        }
      }
      asteroid.display();
    }

    for (int p = projectiles.size () - 1; p >= 0; p--) {
      Proj projectile = projectiles.get(p);
      projectile.update();
      if (projectile.outOfBounds()) {
        projectiles.remove(p);
      } else {
        for (int a = asteroids.size () - 1; a >= 0; a--) {
          Asteroid asteroid = asteroids.get(a);
          if (asteroid.hasCollided(projectile.x, projectile.y, projectile.sz)) {
            asteroid.destroy();
            projectiles.remove(p);
            break;
          }
        }
        projectile.display();
      }
    }
  }
}

void keyPressed() {
  if (key == CODED) {
    switch(keyCode) {
    case UP:
      ship.up = true;
      break;
    case LEFT:
      ship.left = true;
      break;
    case RIGHT:
      ship.right = true;
      break;
    case DOWN:
      ship.down = true;
      break;
    }
  } else if (key == ' ') {
    ship.space = true;
  }
}

void keyReleased() {
  if (key == CODED) {
    switch(keyCode) {
    case UP:
      ship.up = false;
      break;
    case LEFT:
      ship.left = false;
      break;
    case RIGHT:
      ship.right = false;
      break;
    }
  }
}

void mousePressed() {
  if (!paused) {
    if (pauseBtn.overButton()) {
      displayMessage("PAUSED", "Press SPACE to resume");
    }
    if (soundBtn.overButton()) {
      sound++;
      sound %= imgs.length-1;
    }
  }
}

void startGame() {
  ship = new Ship();
  asteroids = new ArrayList<Asteroid>();
  projectiles = new ArrayList<Proj>();
  lives = 2;
  score = 0;
  bonus = 0;
}

void displayMessage(String msg, String subtext) {
  paused = true;
  background(0);
  fill(255);
  textAlign(CENTER);
  textSize(72);
  text(msg, width/2, height/2 - 25);
  textSize(40);
  text(subtext, width/2, height/2 + 25);
  textAlign(LEFT);
}

void genAsters(int numAsters) {
  for (int i = 0; i < numAsters; i++) {
    Asteroid a = new Asteroid(random(width), random(height), random(asterMinSpeed, asterMaxSpeed), random(360), 0);
    while (a.hasCollided (ship.x, ship.y, ship.sz/2 + 20)) {
      a = new Asteroid(random(width), random(height), random(asterMinSpeed, asterMaxSpeed), random(360), 0);
    }
    asteroids.add(a);
  }
}

void scoreboard() {
  fill(255);
  textSize(48);
  text(score, 10, 45);
  shape(shipShape, 25, 80, 30, 30);
  textSize(30);
  text("x", 45, 88);
  text(lives, 68, 90);
}