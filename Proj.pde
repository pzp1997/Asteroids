class Proj {  
  float x;
  float y;
  float dx;
  float dy;
  final int sz = 5;
  final float spd = 15;

  Proj(float x_, float y_, float dir) {
    x = x_;
    y = y_;
    dx = cos(dir) * spd;
    dy = sin(dir) * spd;
  }
  
  void update() {
    x += dx; 
    y -= dy;
  }

  void display() {
    fill(255);
    noStroke();
    ellipse(x, y, sz, sz);
  }

  boolean outOfBounds() {
    return x < 0 || x > width || y < 0 || y > height;
  }
}
