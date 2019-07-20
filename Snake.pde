/*
  En su mayor parte esta es una clase Snake estándar para
  este juego. La única diferencia es el objeto controller
  que es el que aplica la búsqueda
*/
class Snake {
  int length = 0;
  boolean justAte = false;
  Controller controller = new Controller();
  PVector[] pos = new PVector[1]; // arreglo de posiciones del cuerpo de la serpiente
  PVector vel = new PVector(1,0);
  PVector prev_head = new PVector(0,0);
  
  /* La snake virtual es para cuando está haciendo la búsqueda,
     que se crea una virtual que es la que hace el recorrido que comprueba
     si el camino está libre */
  Snake(boolean isVirtual) {
    vel.mult(scl);
    pos[0] = new PVector(floor(horSqrs/2)*scl,floor(verSqrs/2)*scl);
    // renderizar solo si es virtual
    if(!isVirtual) {
      this.square(pos[0].x, pos[0].y);
    }
  }
  
  /* Se ejecuta en cada fotograma, esta es toda la lógica principal del juego Snake */
  void update() {
    justAte = false;
    prev_head = pos[0].get();
    pos[0].add(vel);
    this.checkEatFood();
    this.checkBoundaries();
    this.move();
    this.checkCollBody();
  }

  // Se ejecuta en cada fotograma, esta es la búsqueda
  void render() {
    for(int i = 0; i < this.pos.length-1; i++) {
      square(this.pos[i].x, this.pos[i].y);
      /* Todo lo que sigue es para dibujar un rectangulito entre
         dos cuadrados del cuerpo, esto es para que la serpiente
         se vea continua y no como muchos cuadrados separados. 
         Se puede comentar todo el bloque para ver qué pasa. */ 
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
    square(pos[pos.length-1].x, pos[pos.length-1].y); // dibujar última parte del cuerpo
  }
  
  // Aquí se mueve el cuerpo de la serpiente
  void move() {
    PVector previous = this.prev_head.get();
    PVector previous_copy = this.prev_head.get(); 
    for(int i = 1; i < this.pos.length; i++) {
      previous = pos[i];
      pos[i] = previous_copy;
      previous_copy = previous;
    }
  }

  /* Autoexplicativo, pero esta es la función importante. Se llama en
     el draw() principal y no aquí en update(). */
  void search() {
    this.controller.control();
  }
  
  void checkEatFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) { 
      this.eatsFood();
    }
  }
  
  // Estira la serpiente después de comer
  void eatsFood() {
    if(this.pos.length == 1) {
      this.pos = (PVector[])append(this.pos, new PVector(this.prev_head.x, this.prev_head.y));
    } else {
      this.pos = (PVector[])append(this.pos, new PVector(this.pos[this.pos.length - 1].x, this.pos[this.pos.length - 1].y));
    }
  }

  /* Creo que las siguientes son autoexplicativas */
  
  boolean ateFood() {
    if(this.pos[0].x == food_pos.x && this.pos[0].y == food_pos.y) {
      justAte = true;
      return true;
    }
    return false;
  }
  
  void died() {
    this.pos = new PVector[1];
    this.pos[0] = new PVector(floor(random(horSqrs))*scl, floor(random(verSqrs))*scl);
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

  boolean isInBody(int x, int y) {
    for(int i = 1; i < this.pos.length; i++) {
      if(x*scl == this.pos[i].x && y*scl == this.pos[i].y) {
        return true;
      }
    }
    return false;
  }

  boolean isInBody(PVector position) {
    return isInBody(int(position.x/scl), int(position.y/scl));
  }
  
  void square(float x, float y) {
    noStroke();
    fill(snakecol);
    rect(x + 2, y + 2, scl - 3, scl - 3);
  }

  // Para realizar una copia virtual de la serpiente
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