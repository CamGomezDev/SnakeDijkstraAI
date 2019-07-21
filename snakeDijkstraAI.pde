int fps = 300;

// Tablero 36x27
int horSqrs = 36;
int verSqrs = 27;
// Tablero 12x12
// int horSqrs = 12;
// int verSqrs = 12;

// Modo ventana para tablero 36x27
int scl = 22;
// Pantalla completa, tablero 36x27
// int scl = 28;
// Pantalla completa, tablero 12x12
// int scl = 63;

// Colores
int bgcol = color(44, 47, 124);
int gridcol = color(114, 119, 255);
int snakecol = color(0, 249, 124);
int foodcol = color(255, 48, 69);
int searchcol = color(152, 69, 209);
int shortpathcol = color(242, 149, 29);
int longpathcol = color(255, 250, 0);

boolean notRenderSearchKey = true; // no mostrar búsqueda (true) o sí mostrarla (false)
boolean renderingMainSearch = false;
boolean gamePaused = false;
/* Nótese que hay dos modos de búsqueda: el más simple, que solo
   hace búsqueda por Dijkstra y no completa el juego, y uno más complejo
   que además de Dijsktra comprueba si la serpiente se encierra. Con esta
   variable se elige cuál usar, y nótese que en keyPressed() hay una tecla
   para cambiarla dentro del juego */
boolean justDijkstra = false;

Snake snake;
PVector food_pos = new PVector(floor(random(horSqrs))*scl, floor(random(verSqrs))*scl);

void settings() {
  size(scl*horSqrs+1, scl*verSqrs+1);
}

void setup() {
  // Para pantalla completa
  // background(bgcol);
  // fullScreen();
  // pushMatrix();
  // translate(170,6);

  grid(gridcol);
  snake = new Snake(false);
  updateFood();
  renderFood();

  //popMatrix(); // Para pantalla completa
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
    // Para pantalla completa
    // pushMatrix();
    // translate(170,6);

    if(!renderingMainSearch) { // Si la búsqueda no se está renderizando, avanzar el juego normal...
      background(bgcol);
      grid(gridcol);
      snake.update();
      updateFood();
      snake.search();
      p = 0;
    } else { // ...pero si la búsqueda sí se está renderizando...
      if(snake.justAte) {
        snake.controller.renderMainSearch(); // primero renderizar la búsqueda principal (la morada con camino naranja)
        /* y si ya acabó de renderizar la  búsqueda y la serpiente está atrapada
           y tiene que buscar el camino más largo... */
        if(snake.controller.mainSearch.size() == 0 && snake.controller.inLongestPath) {
          p++;
          stroke(longpathcol);
          strokeWeight(4);
          // ...dibujar toda línea  de dicho camino (esta es la línea amarilla aparece de vez en cuando)
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
    // popMatrix(); //Para pantalla completa
    if(snake.controller.mainSearch.size() == 0 && snake.controller.inLongestPath && p==2) {
      delay(3000);
    }
  }
}

// Esto es para dibujar la cuadrícula
void grid(color col) {
  for(int i = 0; i < horSqrs + 1; i++) {
    stroke(col);
    line(scl*i, 0, scl*i, verSqrs*scl); 
  }
  for(int i = 0; i < verSqrs + 1; i++) {
    stroke(col);
    line(0, scl*i, horSqrs*scl, scl*i); 
  }
}

/*
  Podría haber creado una clase para la comida pero preferí dejarlo todo
  en estas dos funciones
*/
void updateFood() {
  if(snake.ateFood()) {
    boolean match = true;
    while(match) {
      match = false;
      food_pos.x = floor(random(horSqrs))*scl; 
      food_pos.y = floor(random(verSqrs))*scl;
      // Esto es para asegurarse de que la comida no salga en un lugar donde hay cuerpo de la serpiente
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
  if(pos.x >= scl*horSqrs || pos.x < 0 || pos.y >= scl*verSqrs || pos.y < 0) {
    return true;
  }
  return false;
}

// D: usar solo dijkstra, R: mostrar búsqueda, K: pausar, J: desacelerar, L: acelerar
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