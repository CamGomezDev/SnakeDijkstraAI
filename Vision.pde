public class Controller {

  boolean inLongestPath = false;
  ArrayList<PVector> longestPath = new ArrayList<PVector>();

  void control() {

    ArrayList<PVector> mainPath = dijkstra(snake, int(food_pos.x/scl), int(food_pos.y/scl), false);

    if(mainPath.size() > 0) {
      Snake virtualSnake = snake.copy();
      int[] currentHead = {0,0};

      for (int i = 1; i < mainPath.size(); ++i) {
        //posible error por no ser -1
        currentHead[0] = int(virtualSnake.pos[0].x/scl);
        currentHead[1] = int(virtualSnake.pos[0].y/scl);
        chooseSpeed(virtualSnake, mainPath.get(i), currentHead);
        if(i == mainPath.size() - 1) {
          virtualSnake.eatsFood();
        }
        virtualSnake.update();
      }

      //posible error por no ser -1
      ArrayList<PVector> tracebackBack = dijkstra(virtualSnake, int(virtualSnake.pos[virtualSnake.pos.length-1].x/scl), int(virtualSnake.pos[virtualSnake.pos.length-1].y/scl), false);

      int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};
      if(tracebackBack.size() > 0) {
        chooseSpeed(snake, mainPath.get(1), mainHead);
        inLongestPath = false;
      } else {
        if(inLongestPath && longestPath.size() > 1) {
          chooseSpeed(snake, longestPath.get(1), mainHead);
          longestPath.remove(0);
        } else {
          longestPathHeadTail();
        }
      }
    } else {
      int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};
      if(inLongestPath && longestPath.size() > 1) {
        chooseSpeed(snake, longestPath.get(1), mainHead);
        longestPath.remove(0);
      } else {
        longestPathHeadTail();
      }
    }

    renderingSearch = true;
  }

  ArrayList<PVector> dijkstra(Snake currentSnake, int desX, int desY, boolean print) {
    int[][] nodes = new int[width][height];
    int[] firstNode = {int(currentSnake.pos[0].x/scl), int(currentSnake.pos[0].y/scl)};
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

      int horIndex = int(queue.get(0).x);
      int verIndex = int(queue.get(0).y);
      
      int value = Integer.MAX_VALUE;

      //Left, right, top, bottom
      value = checkSideNode(horIndex, 0, horIndex-1, verIndex, value, nodes, queue, currentSnake);
      value = checkSideNode(-horIndex, 1-width, horIndex+1, verIndex, value, nodes, queue, currentSnake);
      value = checkSideNode(verIndex, 0, horIndex, verIndex-1, value, nodes, queue, currentSnake);
      value = checkSideNode(-verIndex, 1-height, horIndex, verIndex+1, value, nodes, queue, currentSnake);

      queue.remove(0);

      if(int(horIndex) != firstNode[0] || int(verIndex) != firstNode[1]) {
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
      tracebackNode[0] = int(move.x);
      tracebackNode[1] = int(move.y);
    }

    return tracebackNodes;
  }

  void longestPathHeadTail() {
    ArrayList<PVector> path = dijkstra(snake, int(snake.pos[snake.pos.length-1].x/scl), int(snake.pos[snake.pos.length-1].y/scl), false);

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

      int[] mainHead = {int(snake.pos[0].x/scl), int(snake.pos[0].y/scl)};
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

  int checkSideNode(int checked, int checkTo, int checkHor, int checkVer, int cValue, int[][] nodes, ArrayList<PVector> queue, Snake cSnake) {
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

  boolean areValidForLongestPath(float x1, float y1, float x2, float y2, ArrayList<PVector> path, Snake cSnake) {
    // path.contains(new PVector(path.get(i+1).x, path.get(i+1).y-1))
    if (!path.contains(new PVector(x1, y1)) && 
        !path.contains(new PVector(x2, y2)) && 
        !isOutsideWorld(new PVector(x1*scl, y1*scl)) &&
        !isOutsideWorld(new PVector(x2*scl, y2*scl)) && 
        !cSnake.isInBody(int(x1), int(y1)) &&
        !cSnake.isInBody(int(x2), int(y2)) &&
        (x1 != int(food_pos.x/scl) || y1 != int(food_pos.y/scl)) &&
        (x2 != int(food_pos.x/scl) || y2 != int(food_pos.y/scl))) {
      return true;
    }
    return false;
  }

  PVector lowestNextTo(int x, int y, int[][] nodes) {
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

  void chooseSpeed(Snake cSnake, PVector move, int[] cHead) {

    int horMove = int(move.x) - cHead[0];
    int verMove = int(move.y) - cHead[1];

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

  void printScreen(int[][] nodes) {
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
