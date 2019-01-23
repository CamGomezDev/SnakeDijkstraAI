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
  
  void update() {
    prev_head = pos[0].get();
    pos[0].add(vel);
    this.checkEatFood();
    this.checkBoundaries();
    this.move();
    this.checkCollBody();
  }

  void render() {
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
  
  void move() {
    PVector previous = this.prev_head.get();
    PVector previous_copy = this.prev_head.get(); 
    for(int i = 1; i < this.pos.length; i++) {
      previous = pos[i];
      pos[i] = previous_copy;
      previous_copy = previous;
    }
  }

  void decideMove() {
    this.controller.control();
  }
  
  void checkEatFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) { 
      this.eatsFood();
    }
  }
  
  void eatsFood() {
    if(this.pos.length == 1) {
      this.pos = (PVector[])append(this.pos, new PVector(this.prev_head.x, this.prev_head.y));
    } else {
      this.pos = (PVector[])append(this.pos, new PVector(this.pos[this.pos.length - 1].x, this.pos[this.pos.length - 1].y));
    }
  }
  
  boolean ateFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) {
      return true;
    }
    return false;
  }
  
  void died() {
    this.pos = new PVector[1];
    this.pos[0] = new PVector(floor(random(width))*scl, floor(random(height))*scl);
  }
  
  void checkBoundaries() {
    if(isOutsideWorld(pos[0])) {
      this.died();
    }
  }
  
  void checkCollBody() {
    if(isInBody(this.pos[0])) {
      this.died();
    }
  }

  boolean isInBody(PVector position) {
    for(int i = 1; i < this.pos.length; i++) {
      if(position.x == this.pos[i].x && position.y == this.pos[i].y) {
        return true;
      }
    }
    return false;
  }

  boolean isInBody(int x, int y) {
    for(int i = 1; i < this.pos.length; i++) {
      if(x*scl == this.pos[i].x && y*scl == this.pos[i].y) {
        return true;
      }
    }
    return false;
  }
  
  void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 2, y + 2, scl - 3, scl - 3);
  }

  Snake copy() {
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