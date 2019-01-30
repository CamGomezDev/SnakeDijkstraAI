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

public class snakegameDijkstra extends PApplet {

int fps = 300;
// int scl = 22;

int scl = 28;
int width = 36;
int height = 27;

//
// int scl = 63;
// int width = 12;
// int height = 12;

// int bgcol = color(29,30,58);
// int gridcol = color(113,112,110);
// int snakecol = color(0,204,102);
// int foodcol = color(255,78,96);
// int searchcol = color(0, 133, 140);
// int shortpathcol = color(255, 165, 0);
// int longpathcol = color(255, 255, 0);

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

public void setup() {
  background(bgcol);
  
  pushMatrix();
  translate(170,6);

  grid(gridcol);
  snake = new Snake(false);
  updateFood();
  renderFood();

  popMatrix();
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

public void grid(int col) {
  for(int i = 0; i < width + 1; i++) {
    stroke(col);
    line(scl*i, 0, scl*i, height*scl); 
  }
  for(int i = 0; i < height + 1; i++) {
    stroke(col);
    line(0, scl*i, width*scl, scl*i); 
  }
}

public void updateFood() {
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
public void renderFood() {
  fill(foodcol);
  noStroke();
  rect(food_pos.x + 1, food_pos.y + 1, scl - 1, scl - 1);
}

public boolean isOutsideWorld(PVector pos) {
  if(pos.x >= scl*width || pos.x < 0 || pos.y >= scl*height || pos.y < 0) {
    return true;
  }
  return false;
}

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
public class Controller {

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
          currentHead[0] = PApplet.parseInt(virtualSnake.pos[0].x/scl);
          currentHead[1] = PApplet.parseInt(virtualSnake.pos[0].y/scl);
          chooseSpeed(virtualSnake, mainPathGeneral.get(i), currentHead);
          if(i == mainPathGeneral.size() - 1) {
            virtualSnake.eatsFood();
          }
          virtualSnake.update();
        }

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
        if(snake.isInBody(PVector.add(snake.pos[0], snake.vel)) || isOutsideWorld(PVector.add(snake.pos[0], snake.vel))) {
          PVector rotateRight = snake.vel.copy().rotate(HALF_PI);
          rotateRight.x = PApplet.parseInt(rotateRight.x);
          rotateRight.y = PApplet.parseInt(rotateRight.y);
          PVector rotateLeft = snake.vel.copy().rotate(-HALF_PI);
          rotateLeft.x = PApplet.parseInt(rotateLeft.x);
          rotateLeft.y = PApplet.parseInt(rotateLeft.y);
          if(!snake.isInBody(new PVector(snake.pos[0].x + rotateRight.x, snake.pos[0].y + rotateRight.y)) && !isOutsideWorld(new PVector(snake.pos[0].x + rotateRight.x, snake.pos[0].y + rotateRight.y))) {
            println("Rotando a la derecha");
            snake.vel = rotateRight;
          } else if(!snake.isInBody(new PVector(snake.pos[0].x + rotateLeft.x, snake.pos[0].y + rotateLeft.y)) && !isOutsideWorld(new PVector(snake.pos[0].x + rotateLeft.x, snake.pos[0].y + rotateLeft.y))) {
            println("Rotando a la izquierda");
            snake.vel = rotateLeft;
          }
        }
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
      fill(searchcol);
      noStroke();
      rect(mainSearch.get(0).x*scl + 1, mainSearch.get(0).y*scl + 1, scl - 1, scl - 1);
      if(mainSearch.get(0).x*scl == food_pos.x && mainSearch.get(0).y*scl == food_pos.y) {
        fill(shortpathcol);
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
    ArrayList<PVector> path = dijkstra(snake, PApplet.parseInt(snake.pos[snake.pos.length-1].x/scl), PApplet.parseInt(snake.pos[snake.pos.length-1].y/scl), false);

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
    } else {
      //Esto por alguna razón nunca se ejecuta cosa que es extraña pero me da pereza averiguar por qué
      println("No hay contacto de cabeza a cola");
      delay(30000);
      System.exit(0);
    }
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
class Snake {
  int length = 0;
  boolean justAte = false;
  Controller controller = new Controller();
  PVector[] pos = new PVector[1];
  PVector vel = new PVector(1,0);
  PVector prev_head = new PVector(0,0);
  
  Snake(boolean isVirtual) {
    vel.mult(scl);
    pos[0] = new PVector(floor(width/2)*scl,floor(height/2)*scl);
    if(!isVirtual) {
      this.square(pos[0].x, pos[0].y);
    }
  }
  
  public void update() {
    justAte = false;
    prev_head = pos[0].get();
    pos[0].add(vel);
    this.checkEatFood();
    this.checkBoundaries();
    this.move();
    this.checkCollBody();
  }

  public void render() {
    for(int i = 0; i < this.pos.length-1; i++) {
      square(this.pos[i].x, this.pos[i].y);
      // Misma columna
      if(pos[i].x == pos[i+1].x) {
        // Arreglo aumenta hacia abajo
        if(pos[i].y < pos[i+1].y) {
          rect(pos[i].x + 2, pos[i].y + scl - 1, scl - 3, 3);
        }
        // Arreglo aumenta hacia arriba
        if(pos[i].y > pos[i+1].y) {
          rect(pos[i].x + 2, pos[i+1].y + scl - 1, scl - 3, 3);
        }
      }
      // Misma fila
      if(pos[i].y == pos[i+1].y) {
        // Arreglo aumenta hacia la derecha
        if(pos[i].x < pos[i+1].x) {
          rect(pos[i].x + scl - 1, pos[i].y + 2, 3, scl - 3);
        }
        // Arreglo aumenta hacia la izquiera
        if(pos[i].x > pos[i+1].x) {
          rect(pos[i+1].x + scl - 1, pos[i].y + 2, 3, scl - 3);
        }
      }
    }
    square(pos[pos.length-1].x, pos[pos.length-1].y);
  }
  
  public void move() {
    PVector previous = this.prev_head.get();
    PVector previous_copy = this.prev_head.get(); 
    for(int i = 1; i < this.pos.length; i++) {
      previous = pos[i];
      pos[i] = previous_copy;
      previous_copy = previous;
    }
  }

  public void search() {
    this.controller.control();
  }
  
  public void checkEatFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) { 
      this.eatsFood();
    }
  }
  
  public void eatsFood() {
    if(this.pos.length == 1) {
      this.pos = (PVector[])append(this.pos, new PVector(this.prev_head.x, this.prev_head.y));
    } else {
      this.pos = (PVector[])append(this.pos, new PVector(this.pos[this.pos.length - 1].x, this.pos[this.pos.length - 1].y));
    }
  }
  
  public boolean ateFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) {
      justAte = true;
      return true;
    }
    return false;
  }
  
  public void died() {
    this.pos = new PVector[1];
    this.pos[0] = new PVector(floor(random(width))*scl, floor(random(height))*scl);
  }
  
  public void checkBoundaries() {
    if(isOutsideWorld(pos[0])) {
      this.died();
    }
  }
  
  public void checkCollBody() {
    if(isInBody(this.pos[0])) {
      this.died();
    }
  }

  public boolean isInBody(PVector position) {
    for(int i = 1; i < this.pos.length; i++) {
      if(position.x == this.pos[i].x && position.y == this.pos[i].y) {
        return true;
      }
    }
    return false;
  }

  public boolean isInBody(int x, int y) {
    for(int i = 1; i < this.pos.length; i++) {
      if(x*scl == this.pos[i].x && y*scl == this.pos[i].y) {
        return true;
      }
    }
    return false;
  }
  
  public void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 2, y + 2, scl - 3, scl - 3);
  }

  public Snake copy() {
    Snake copy = new Snake(true);
    copy.pos[0] = pos[0].copy();
    for (int i = 1; i < pos.length; ++i) {
      copy.pos = (PVector[])append(copy.pos, pos[i].copy());
    }
    copy.vel = vel.copy();
    copy.prev_head = prev_head.copy();

    return copy;
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "snakegameDijkstra" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
