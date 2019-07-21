import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class snakeDijkstraAI extends PApplet {

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

// no mostrar búsqueda (true) o sí mostrarla (false)
boolean notRenderSearchKey = true;
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

public void settings() {
  size(scl*horSqrs+1, scl*verSqrs+1);
}

public void setup() {
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
public void draw() {
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

    // Si la búsqueda no se está mostrando, avanzar el juego normal...
    if(!renderingMainSearch) {
      background(bgcol);
      grid(gridcol);
      snake.update();
      updateFood();
      snake.search();
      p = 0;
    // ...pero si la búsqueda sí se está mostrando...
    } else {
      if(snake.justAte) {
        // primero renderizar la búsqueda principal (la morada con camino naranja)
        snake.controller.renderMainSearch();
        // y si la serpiente está atrapada y tiene que buscar el camino más largo...
        if(snake.controller.mainSearch.size() == 0 && snake.controller.inLongestPath) {
          p++;
          stroke(longpathcol);
          strokeWeight(4);
          // dibujar toda línea  de dicho camino (esta es la línea amarilla aparece de vez en cuando)
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
public void grid(int col) {
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
public void updateFood() {
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
public void renderFood() {
  fill(foodcol);
  noStroke();
  rect(food_pos.x + 1, food_pos.y + 1, scl - 1, scl - 1);
}

public boolean isOutsideWorld(PVector pos) {
  if(pos.x >= scl*horSqrs || pos.x < 0 || pos.y >= scl*verSqrs || pos.y < 0) {
    return true;
  }
  return false;
}

// D: usar solo dijstra, R: mostrar búsqueda, K: pausar, J: desacelerar, L: acelerar
public void keyPressed() {  
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
public class Controller1 {

  boolean inLongestPath = false;
  ArrayList<PVector> longestPath = new ArrayList<PVector>();
  ArrayList<PVector> mainSearch = new ArrayList<PVector>();
  ArrayList<PVector> mainPathGeneral = new ArrayList<PVector>();

  public void control() {
    mainSearch = new ArrayList<PVector>();
    mainPathGeneral = dijkstra(snake, PApplet.parseInt(food_pos.x/scl), PApplet.parseInt(food_pos.y/scl), false);

    if(mainPathGeneral.size() > 0) {
      if(justDijkstra) {
        int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
        chooseSpeed(snake, mainPathGeneral.get(1), mainHead);
      } else {
        Snake virtualSnake = snake.copy();
        int[] currentHead = {0,0};

        for (int i = 1; i < mainPathGeneral.size(); ++i) {
          //posible error por no ser -1
          currentHead[0] = PApplet.parseInt(virtualSnake.pos[0].x/scl);
          currentHead[1] = PApplet.parseInt(virtualSnake.pos[0].y/scl);
          chooseSpeed(virtualSnake, mainPathGeneral.get(i), currentHead);
          if(i == mainPathGeneral.size() - 1) {
            virtualSnake.eatsFood();
          }
          virtualSnake.update();
        }

        //posible error por no ser -1
        ArrayList<PVector> tracebackBack = dijkstra(virtualSnake, PApplet.parseInt(virtualSnake.pos[virtualSnake.pos.length-1].x/scl), PApplet.parseInt(virtualSnake.pos[virtualSnake.pos.length-1].y/scl), false);

        int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
        if(tracebackBack.size() > 0) {
          chooseSpeed(snake, mainPathGeneral.get(1), mainHead);
          inLongestPath = false;
        } else {
          if(inLongestPath && longestPath.size() > 1) {
            chooseSpeed(snake, longestPath.get(1), mainHead);
            longestPath.remove(0);
          } else {
            longestPathHeadTail();
          }
        }
      }
    } else {
      if(justDijkstra) {
        ArrayList<PVector> haltPath = new ArrayList<PVector>();
        for (int h = snake.pos.length-1; h > 0; h--) {
          haltPath = dijkstra(snake, PApplet.parseInt(snake.pos[h].x/scl), PApplet.parseInt(snake.pos[h].y/scl), false);
          if(haltPath.size() > 0) {
            break;
          }
        }

        longestPathHeadTail();
      } else {
        int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
        if(inLongestPath && longestPath.size() > 1) {
          chooseSpeed(snake, longestPath.get(1), mainHead);
          longestPath.remove(0);
        } else {
          longestPathHeadTail();
        }
      }
    }
  }

  public void renderMainSearch() {
    if(mainSearch.size() > 0) {
      frameRate(2000);
      fill(color(255,255,0));
      noStroke();
      rect(mainSearch.get(0).x*scl + 1, mainSearch.get(0).y*scl + 1, scl - 1, scl - 1);
      if(mainSearch.get(0).x*scl == food_pos.x && mainSearch.get(0).y*scl == food_pos.y) {
        fill(color(255,165,0));
        for (PVector place : mainPathGeneral) {
          rect(place.x*scl + 1, place.y*scl + 1, scl - 1, scl - 1);
        }
      }
      mainSearch.remove(0);
    } else {
      renderingMainSearch = false;
    }
  }

  public ArrayList<PVector> dijkstra(Snake currentSnake, int desX, int desY, boolean print) {
    int[][] nodes = new int[width][height];
    int[] firstNode = {PApplet.parseInt(currentSnake.pos[0].x/scl), PApplet.parseInt(currentSnake.pos[0].y/scl)};
    ArrayList<PVector> queue = new ArrayList<PVector>();
    PVector currentNode = new PVector(currentSnake.pos[0].x, currentSnake.pos[0].y);
    boolean[][] checked = new boolean[width][height];

    //Inicializar todos los nodos con un valor infinito, excepto al inicial, que es 0
    for (int i = 0; i < width; ++i) {
      for (int ii = 0; ii < height; ++ii) {
        if(firstNode[0] != i || firstNode[1] != ii) {
          nodes[i][ii] = Integer.MAX_VALUE;
          checked[i][ii] = false;
        } else {
          nodes[i][ii] = 0;
          checked[i][ii] = true;
        }
      }
    }

    queue.add(new PVector(firstNode[0], firstNode[1]));
    boolean somethingInQueue = true;
    int i = 0;

    while(somethingInQueue) {
      i++;

      int horIndex = PApplet.parseInt(queue.get(0).x);
      int verIndex = PApplet.parseInt(queue.get(0).y);
      
      int value = Integer.MAX_VALUE;

      //Left, right, top, bottom
      value = checkSideNode(horIndex, 0, horIndex-1, verIndex, value, nodes, queue, currentSnake);
      value = checkSideNode(-horIndex, 1-width, horIndex+1, verIndex, value, nodes, queue, currentSnake);
      value = checkSideNode(verIndex, 0, horIndex, verIndex-1, value, nodes, queue, currentSnake);
      value = checkSideNode(-verIndex, 1-height, horIndex, verIndex+1, value, nodes, queue, currentSnake);

      queue.remove(0);

      if(PApplet.parseInt(horIndex) != firstNode[0] || PApplet.parseInt(verIndex) != firstNode[1]) {
        if(!renderingMainSearch) {
          mainSearch.add(new PVector(horIndex, verIndex));
        }
        nodes[horIndex][verIndex] = value;
        checked[horIndex][verIndex] = true;
      }

      if(queue.size() == 0) {
        somethingInQueue = false;
        if(print) {
          printScreen(nodes);
        }
      }
    }

    ArrayList<PVector> tracebackNodes = new ArrayList<PVector>();
    int[] tracebackNode = {desX, desY};
    tracebackNodes = new ArrayList<PVector>();
    tracebackNodes.add(new PVector(tracebackNode[0], tracebackNode[1]));
    boolean closed = false;
  
    //Aquí se devuelve desde la comida hasta el nodo de la cabeza para elegir el camino
    while(tracebackNode[0] != firstNode[0] || tracebackNode[1] != firstNode[1]) {
      PVector move = lowestNextTo(tracebackNode[0], tracebackNode[1], nodes);
      if(move.x == -1 && move.y == -1) {
        return new ArrayList<PVector>();
      }
      tracebackNodes.add(0, move);
      tracebackNode[0] = PApplet.parseInt(move.x);
      tracebackNode[1] = PApplet.parseInt(move.y);
    }

    renderingMainSearch = true;
    return tracebackNodes;
  }

  public void longestPathHeadTail() {
    // wentToTail = true;
    ArrayList<PVector> path = longestPathHeadTo(PApplet.parseInt(snake.pos[snake.pos.length-1].x/scl), PApplet.parseInt(snake.pos[snake.pos.length-1].y/scl));

    println(path.size());
    if(path.size() > 0) {
      int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
      chooseSpeed(snake, path.get(1), mainHead);
      path.remove(0);
      longestPath = path;
      inLongestPath = true;
    } else {
      //Esto por alguna razón nunca se ejecuta cosa que es extraña pero me da pereza averiguar por qué
      println("No hay contacto de cabeza a cola");
      delay(30000);
      System.exit(0);
    }
  }

  public ArrayList<PVector> longestPathHeadTo(int desX, int desY) {
    ArrayList<PVector> path = dijkstra(snake, desX, desY, false);

    //A very long print function
    // for (int o = 0; o < height; ++o) {
    //   for (int oo = 0; oo < width; ++oo) {
    //     boolean matchFound = false;
    //     int matchIndex = 0;
    //     for (int i = 0; i < path.size(); ++i) {
    //       if(path.get(i).x == oo && path.get(i).y == o) {
    //         matchFound = true;
    //         matchIndex = i;
    //       }
    //     }
    //     if(matchFound) {
    //       if(matchIndex < 10) {
    //         print(matchIndex + "   ");
    //       } else if(matchIndex < 100) {
    //         print(matchIndex + "  ");
    //       } else {
    //         print(matchIndex + " ");
    //       }
    //     } else {
    //       print("n   ");
    //     }
    //   }
    //   print('\n');
    // }

    if(path.size() > 0) {
      boolean aPairFound = true;

      while(aPairFound) {
        aPairFound = false;
        for (int i = 0; i < path.size()-1; ++i) {
          //Dos nodos en la misma columna
          if(path.get(i).x == path.get(i+1).x) {
            //Expandir hacia la izquierda
            if(areValidForLongestPath(path.get(i).x-1, path.get(i).y, path.get(i+1).x-1, path.get(i+1).y, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x-1, path.get(i).y));
              path.add(i+2, new PVector(path.get(i+2).x-1, path.get(i+2).y));
              aPairFound = true;
              break;
            //Expandir hacia la derecha
            } else if(areValidForLongestPath(path.get(i).x+1, path.get(i).y, path.get(i+1).x+1, path.get(i+1).y, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x+1, path.get(i).y));
              path.add(i+2, new PVector(path.get(i+2).x+1, path.get(i+2).y));
              aPairFound = true;
              break;
            }
          //Dos nodos en la misma fila
          } else if(path.get(i).y == path.get(i+1).y) {
            //Expandir hacia abajo
            if(areValidForLongestPath(path.get(i).x, path.get(i).y+1, path.get(i+1).x, path.get(i+1).y+1, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x, path.get(i).y+1));
              path.add(i+2, new PVector(path.get(i+2).x, path.get(i+2).y+1));
              aPairFound = true;
              break;
            //Expandir hacia arriba
            } else if(areValidForLongestPath(path.get(i).x, path.get(i).y-1, path.get(i+1).x, path.get(i+1).y-1, path, snake)) {
              path.add(i+1, new PVector(path.get(i).x, path.get(i).y-1));
              path.add(i+2, new PVector(path.get(i+2).x, path.get(i+2).y-1));
              aPairFound = true;
              break;
            }
          }
        }
      }

      int[] mainHead = {PApplet.parseInt(snake.pos[0].x/scl), PApplet.parseInt(snake.pos[0].y/scl)};
      chooseSpeed(snake, path.get(1), mainHead);
      path.remove(0);
      longestPath = path;
      inLongestPath = true;

      return path;
    }

    return new ArrayList<PVector>();
  }

  public int checkSideNode(int checked, int checkTo, int checkHor, int checkVer, int cValue, int[][] nodes, ArrayList<PVector> queue, Snake cSnake) {
    if(checked > checkTo) {
      if(nodes[checkHor][checkVer] < Integer.MAX_VALUE) {
        if(nodes[checkHor][checkVer] < cValue) {
          return nodes[checkHor][checkVer] + 1;
        }
      } else {
        if(!cSnake.isInBody(checkHor, checkVer)) {
          if(!queue.contains(new PVector(checkHor, checkVer))) {
            queue.add(new PVector(checkHor, checkVer));
          }
        }
      }
    }
    return cValue;
  }

  public boolean areValidForLongestPath(float x1, float y1, float x2, float y2, ArrayList<PVector> path, Snake cSnake) {
    // path.contains(new PVector(path.get(i+1).x, path.get(i+1).y-1))
    if (!path.contains(new PVector(x1, y1)) && 
        !path.contains(new PVector(x2, y2)) && 
        !isOutsideWorld(new PVector(x1*scl, y1*scl)) &&
        !isOutsideWorld(new PVector(x2*scl, y2*scl)) && 
        !cSnake.isInBody(PApplet.parseInt(x1), PApplet.parseInt(y1)) &&
        !cSnake.isInBody(PApplet.parseInt(x2), PApplet.parseInt(y2)) &&
        (x1 != PApplet.parseInt(food_pos.x/scl) || y1 != PApplet.parseInt(food_pos.y/scl)) &&
        (x2 != PApplet.parseInt(food_pos.x/scl) || y2 != PApplet.parseInt(food_pos.y/scl))) {
      return true;
    }
    return false;
  }

  public PVector lowestNextTo(int x, int y, int[][] nodes) {
    int lowestXInd = 0;
    int lowestYInd = 0;
    int lowestValue = Integer.MAX_VALUE;
    boolean closed = true;

    if(x > 0) {
      if(nodes[x-1][y] < lowestValue) {
        closed = false;
        lowestValue = nodes[x-1][y] + 1;
        lowestXInd = x-1;
        lowestYInd = y;
      }
    }
    if(x < width - 1) {
      if(nodes[x+1][y] < lowestValue - 1) {
        closed = false;
        lowestValue = nodes[x+1][y] + 1;
        lowestXInd = x+1;
        lowestYInd = y;
      }
    }
    if(y > 0) {
      if(nodes[x][y-1] < lowestValue - 1) {
        closed = false;
        lowestValue = nodes[x][y-1] + 1;
        lowestXInd = x;
        lowestYInd = y-1;
      }
    }
    if(y < height - 1) {
      if(nodes[x][y+1] < lowestValue - 1) {
        closed = false;
        lowestValue = nodes[x][y+1] + 1;
        lowestXInd = x;
        lowestYInd = y+1;
      }
    }
    
    if(closed) {
      return new PVector(-1, -1);
    }
    return new PVector(lowestXInd, lowestYInd);
    
  }

  public void chooseSpeed(Snake cSnake, PVector move, int[] cHead) {

    int horMove = PApplet.parseInt(move.x) - cHead[0];
    int verMove = PApplet.parseInt(move.y) - cHead[1];

    if(horMove == -1 && verMove == 0) {
      cSnake.vel.x = -scl;
      cSnake.vel.y = 0;
    } else if(horMove == 1 && verMove == 0) {
      cSnake.vel.x = scl;
      cSnake.vel.y = 0;
    } else if(horMove == 0 && verMove == -1) {
      cSnake.vel.x = 0;
      cSnake.vel.y = -scl;
    } else if(horMove == 0 && verMove == 1) {
      cSnake.vel.x = 0;
      cSnake.vel.y = scl;
    }
  }

  public void printScreen(int[][] nodes) {
    for (int o = 0; o < height; ++o) {
      for (int oo = 0; oo < width; ++oo) {
        if(nodes[oo][o] == Integer.MAX_VALUE) {
          print("i  ");
        } else {
          if(nodes[oo][o] < 10) {
            print(nodes[oo][o] + "  ");
          } else if(nodes[oo][o] < 100) {
            print(nodes[oo][o] + " ");
          }
        }
      }
      print('\n');
    }
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "snakeDijkstraAI" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
