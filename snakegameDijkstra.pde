int fps = 300;

// Modo ventana, tablero 36x27
// int scl = 22;

// Pantalla completa, tablero 36x27
int scl = 28;
int width = 36;
int height = 27;

// Pantalla completa, tablero 12x12
// int scl = 63;
// int width = 12;
// int height = 12;

int bgcol = color(44, 47, 124);
int gridcol = color(114, 119, 255);
int snakecol = color(0, 249, 124);
int foodcol = color(255, 48, 69);
int searchcol = color(152, 69, 209);
int shortpathcol = color(242, 149, 29);
int longpathcol = color(255, 250, 0);

boolean notRenderSearchKey = true;
boolean renderingMainSearch = false;
boolean gamePaused = false;
boolean nextFrame = false;
boolean justDijkstra = false;

Snake snake;
PVector food_pos = new PVector(floor(random(width))*scl, floor(random(height))*scl);

// void settings() {
//   size(scl*width+1, scl*height+1);
// }

void setup() {
  background(bgcol);
  fullScreen();
  pushMatrix();
  translate(170,6);

  grid(gridcol);
  snake = new Snake(false);
  updateFood();
  renderFood();

  popMatrix();
}

int p = 0;
void draw() {
  if(!gamePaused) {
    if(notRenderSearchKey) {
      renderingMainSearch = false;
    }
    if(!renderingMainSearch) {
      frameRate(fps);
    }
    pushMatrix();
    translate(170,6);
    if(!renderingMainSearch) {
      background(bgcol);
      grid(gridcol);
      snake.update();
      updateFood();
      snake.search();
      p = 0;
    } else {
      if(snake.justAte) {
        snake.controller.renderMainSearch();
        if(snake.controller.mainSearch.size() == 0 && snake.controller.inLongestPath) {
          p++;
          stroke(longpathcol);
          strokeWeight(4);
          line(snake.pos[0].x + scl/2, snake.pos[0].y + scl/2, snake.controller.longestPath.get(0).x*scl + scl/2, snake.controller.longestPath.get(0).y*scl + scl/2);
          for(int i = 0; i < snake.controller.longestPath.size() - 1; i++) {
            line(snake.controller.longestPath.get(i).x*scl + scl/2, snake.controller.longestPath.get(i).y*scl + scl/2, snake.controller.longestPath.get(i+1).x*scl + scl/2, snake.controller.longestPath.get(i+1).y*scl + scl/2);
          }
          strokeWeight(1);
        }
      } else {
        renderingMainSearch = false;
      }
    }
    snake.render();
    renderFood();
    popMatrix();
    if(snake.controller.mainSearch.size() == 0 && snake.controller.inLongestPath && p==2) {
      delay(3000);
    }
  }
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

void updateFood() {
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
}
void renderFood() {
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
  if (key == 'd') {
    justDijkstra = !justDijkstra;
  }
  if (key == 'r') {
    notRenderSearchKey = !notRenderSearchKey;
  }
  if (key == 'k') {
    gamePaused = !gamePaused;
  }
  if(key == 'l') {
    switch (fps) {
      case 5 :
        fps = 15;
      case 15 :
        fps = 30;
      break;
      case 30 :
        fps = 100;
      break;
      case 100 :
        fps = 200;
      break;
      case 200 :
        fps = 300;
      break;	
      default :
      break;	
    }
  }
  if(key == 'j') {
    switch (fps) {
      case 300 :
        fps = 200;
      break;
      case 200 :
        fps = 100;
      break;	
      case 100 :
        fps = 30;
      case 30 :
        fps = 15;
      case 15 :
        fps = 5;
      break;
      default :
      break;	
    }
  }
}