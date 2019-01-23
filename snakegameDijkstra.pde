int scl = 20;
int width = 36;
int height = 27;
int fps = 200;

// int scl = 40;
// int width = 16;
// int height = 12;

int bgcol = color(29,30,58);
int gridcol = color(113,112,110);
int snakecol = color(0,204,102);
int foodcol = color(255,78,96);
int panelcol = 175;

boolean pressed_key = false;

Snake snake;
PVector food_pos = new PVector(floor(random(width))*scl, floor(random(height))*scl);

// void settings() {
//   size(scl*width+1, scl*height+1);
// }

void setup() {
  frameRate(fps);
  background(bgcol);
  fullScreen();
  pushMatrix();
  translate(50,6);

  grid(gridcol);
  snake = new Snake(false);
  food();

  popMatrix();
}

void draw() {
  background(bgcol);
  pushMatrix();
  translate(50,6);

  grid(gridcol);
  snake.decideMove();
  snake.update();
  snake.render();
  pressed_key = false;
  food();

  popMatrix();
}

void grid(color col) {
  for(int i = 0; i < width + 1; i++) {
    stroke(col);
    line(scl*i, 0, scl*i, height*scl); 
  }
  for(int i = 0; i < height + 1; i++) {
    stroke(col);
    line(0, scl*i, width*scl, scl*i); 
  }
}

void food() {
  if(snake.ateFood()) {
    boolean match = true;
    while(match) {
      match = false;
      food_pos.x = floor(random(width))*scl; 
      food_pos.y = floor(random(height))*scl;
      for(int i = 0; i < snake.pos.length; i++) {
        if(food_pos.x == snake.pos[i].x && food_pos.y == snake.pos[i].y) {
          match = true;
        }      
      }
    }
  }
  fill(foodcol);
  noStroke();
  rect(food_pos.x + 1, food_pos.y + 1, scl - 1, scl - 1);
}

boolean isOutsideWorld(PVector pos) {
  if(pos.x >= scl*width || pos.x < 0 || pos.y >= scl*height || pos.y < 0) {
    return true;
  }
  return false;
}

void keyPressed() {  
  if (key == CODED && !pressed_key) {
    if (keyCode == UP && snake.vel.y != scl) {
      snake.vel.x = 0;
      snake.vel.y = -scl;
    } else if (keyCode == DOWN && snake.vel.y != -scl) {
      snake.vel.x = 0;
      snake.vel.y = scl;
    } else if (keyCode == LEFT && snake.vel.x != scl) {
      snake.vel.x = -scl;
      snake.vel.y = 0;
    } else if (keyCode == RIGHT && snake.vel.x != -scl) {
      snake.vel.x = scl;
      snake.vel.y = 0;
    }
    pressed_key = true;
  }
  if (key == ' ') {
    snake.eatsFood();
  }
}