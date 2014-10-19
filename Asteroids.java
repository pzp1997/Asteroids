import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Asteroids extends PApplet {

/*
Asteroids.pde: The classic game "Asteroids" written in Processing
 Author: Palmer Paul
 Email: pzpaul2002@yahoo.com
 Twitter: @pzp1997
 Version: 1.2.1 (10/19/14 09:45)
 Copyright: (c) 2014, Palmer Paul
 */



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
final float asterMinSpeed = 1.5f;
final float asterMaxSpeed = 2.0f;
final int maxProjs = 10;

public void setup() {
  size(displayWidth, displayHeight, P2D);
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

public void draw() {
  if (paused) {
    if (keyPressed) {
      if (key == ' ') {
        paused = false;
      }
    }
  } else {
    if (asteroids.size() == 0) {
      genAsters(PApplet.parseInt(random(4, 6)));
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

public void keyPressed() {
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

public void keyReleased() {
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

public void mousePressed() {
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

public void startGame() {
  ship = new Ship();
  asteroids = new ArrayList<Asteroid>();
  projectiles = new ArrayList<Proj>();
  lives = 2;
  score = 0;
  bonus = 0;
}

public void displayMessage(String msg, String subtext) {
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

public void genAsters(int numAsters) {
  for (int i = 0; i < numAsters; i++) {
    Asteroid a = new Asteroid(random(width), random(height), random(asterMinSpeed, asterMaxSpeed), random(360), 0);
    while (a.hasCollided (ship.x, ship.y, ship.sz/2 + 20)) {
      a = new Asteroid(random(width), random(height), random(asterMinSpeed, asterMaxSpeed), random(360), 0);
    }
    asteroids.add(a);
  }
}

public void scoreboard() {
  fill(255);
  textSize(48);
  text(score, 10, 45);
  shape(shipShape, 25, 80, 30, 30);
  textSize(30);
  text("x", 45, 88);
  text(lives, 68, 90);
}

class Asteroid extends Obj {
  final int points;
  final int lvl;

  Asteroid(float x, float y, float spd, float dir, int lvl_) {
    super(x, y, spd, dir, asterSzLvls[lvl_]);
    points = asterPntLvls[lvl_];
    lvl = lvl_;
  }

  public void display() {
    asterShape.setStrokeWeight(15 + 10*lvl);
    super.display(asterShape);
  }

  public void destroy() {
    score += points;
    bonus += points;
    if (lvl < 2) {
      breakApart();
    }
    asteroids.remove(this);


    if (sound != 0) {
      sounds[1 + 3*(sound-1)].trigger();
    }
  }

  public boolean hasCollided(float ix, float iy, float r) {
    return
      (ix + r > x-sz/2
      ||  ix - r > x-sz/2)
      && (ix + r < x+sz/2
        ||  ix - r < x+sz/2)

        && (iy + r > y-sz/2
          || iy - r > y-sz/2)
          && (iy + r < y+sz/2
            || iy - r < y+sz/2);
  }

  public void breakApart() {
    asteroids.add(new Asteroid(x, y, random(asterMinSpeed, asterMaxSpeed)*(lvl+1), random(360), lvl+1));
    asteroids.add(new Asteroid(x, y, random(asterMinSpeed, asterMaxSpeed)*(lvl+1), random(360), lvl+1));
  }
}

class Button {
  float x;
  float y;
  int sz;

  Button(float x_, float y_, int sz_) {
    x = x_;
    y = y_;
    sz = sz_;
  }

  public boolean overButton() {
    return mouseX >= x-sz/2 && mouseX <= x+sz/2 && 
      mouseY >= y-sz/2 && mouseY <= y+sz/2;
  }

  public void update() {
    stroke(255);
    strokeWeight(2);
    if (overButton()) {
      fill(100);
    } else {
      noFill();
    }
    rect(x, y, sz, sz);
  }
}

class Obj {
  float x;
  float y;
  float spd;
  float dir;
  final int sz;
  
  Obj(float x_, float y_, float spd_, float dir_, int sz_) {
    x = x_;
    y = y_;
    spd = spd_;
    dir = radians(dir_);
    sz = sz_;
  }

  public void update() {
    x += cos(dir) * spd;
    y -= sin(dir) * spd;
    wrap();
  }

  public void display(PShape shape) {
    pushMatrix();
    translate(x, y);
    rotate(-dir + HALF_PI);
    shape(shape, 0, 0, sz, sz);
    popMatrix();
  }

  public void wrap() {
    if (x < -sz/2) {
      x = width + sz/2;
    }
    if (x > width + sz/2) {
      x = -sz/2;
    }
    if (y < -sz/2) {
      y = height + sz/2;
    }
    if (y > height + sz/2) {
      y = -sz/2;
    }
  }
}

class Proj {  
  float x;
  float y;
  float dx;
  float dy;
  final int sz;
  final float spd;

  Proj(float x_, float y_, float dir) {
    sz = 5;
    spd = 15;
    x = x_;
    y = y_;
    dx = cos(dir) * spd;
    dy = sin(dir) * spd;
  }
  
  public void update() {
    x += dx; 
    y -= dy;
  }

  public void display() {
    fill(255);
    noStroke();
    ellipse(x, y, sz, sz);
  }

  public boolean outOfBounds() {
    return x < 0 || x > width || y < 0 || y > height;
  }
}
class Ship extends Obj {
  int timeDecel;
  int timeImmune;
  
  boolean left;
  boolean up;
  boolean right;
  boolean down;
  boolean space;
  boolean immune;

  final float maxSpeed = 10;
  final float accel = 0.1f;
  final float decel = 0.2f;
  final float handling = radians(3);

  Ship() {
    super(width/2, height/2, 0.0f, 90.0f, 56);
  }

  public void update() {
    if (up) {
      if (spd < maxSpeed) {
        spd += accel;
      } else {
        spd += 1/(2*frameRate);
      }
    }
    if (left) {
      dir += handling;
    }
    if (right) {
      dir -= handling;
    }
    if (down) {
      hyperspace();
    }
    if (space) {
      shoot();
    }
    
    if (millis() - timeDecel >= 100) {
      spd -= decel;
      if (spd < 0) {
        spd = 0.0f;
      }
      timeDecel = millis();
    }
    
    if (immune) {
      if (millis() - timeImmune >= 3000) {
        immune = false;
      }
    } 
    
    super.update();
  }

  public void shoot() {
    if (projectiles.size() < maxProjs) {
      projectiles.add(new Proj(x + cos(dir)*sz/2, y - sin(dir)*sz/2, dir));
      space = false;

      if (sound != 0) {
        sounds[3*(sound-1)].trigger();
      }
    }
  }

  public void display() {
    super.display(shipShape);
  }

  public void hyperspace() {
    x = random(width);
    y = random(height);
    down = false;
  }

  public void destroy() {
    x = width/2;
    y = height/2;
    spd = 0.0f;
    dir = HALF_PI;
    lives -= 1;
    immune = true;
    timeImmune = millis();

    if (sound != 0) {
      sounds[2 + 3*(sound-1)].trigger();
    }

    if (lives == -1) {
      displayMessage("GAME OVER", "Score: " + score);
      startGame();
    }
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#000000", "--hide-stop", "Asteroids" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
