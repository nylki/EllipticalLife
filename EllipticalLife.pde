import java.awt.Point;


int steps = 150;
int maxChildParentDistance = 250;
//int maxChilds = 2;
LinkedList<Kreis> allCircles;
Kreis k1, k2, kreisCreatedAfterMousePressed;
int groupsCount=0;



void setup() {
  size(1300, 800, P2D);
  smooth(8);
  colorMode(HSB, 1000, 1000, 1000, 100);
  frameRate(50);
  noStroke();
  allCircles = new LinkedList();
  k1 = new Kreis(random(width), random(height), 1, 50, null);
  k2 = new Kreis(random(width), random(height), 1, 50, null);

  allCircles.add(k1);
  allCircles.add(k2);


  k1.direction.set(k2.location);
  k1.generateChildren(4);

  k2.direction.set(k1.location);
  k2.generateChildren(4);
}

//noch bedingungen für verkleinerung eines kreises später
public class Kreis {
  int groupID;
  float speed, radius;
  color c;

  //ToDo: using PVector instead of single floats for better readability and performance (vector operations)

  PVector direction, distance, location;

  LinkedList<Kreis> childs;
  Kreis parent;
  boolean influencedByParent = false;

  void generateChildren(int amount) {
    for (int i=1;i<= amount;i++) {
      Kreis neuerKreis = new Kreis(this.location.x + random(60), this.location.y + random(60), random(1, 3), (this.radius/1.5), this);
      this.childs.add(neuerKreis); 
      allCircles.add(neuerKreis);
    }
  }

  void removeKreis(Kreis k) { 
    for (Kreis c : k.childs)
      c.parent = c; 

    k.parent.childs.remove(k); 
    allCircles.remove(k);

    //set the new parent for the childs
  }




  Kreis(float initialX, float initialY, float initialSpeed, float r, Kreis p) {
    super();
    this.location = new PVector(initialX, initialY, 0);
    this.speed = initialSpeed;

    this.direction = new PVector(random(width-50), random(height-50), 0);
    this.radius = r;
    this.parent = p;
    if (p == null)
      this.parent = this;


    //if object has no parent
    //generate new color for itself and its children
    //and set a new groupID for identification
    if (parent == this) {
      this.c = color(random(1000), random(300, 1000), random(700, 1000), 100);
      groupsCount++;
      this.groupID = groupsCount;
    }
    else {
      this.c = parent.c;
      this.groupID = parent.groupID;
    }

    childs = new LinkedList();
  }

  boolean collision(Kreis k1, Kreis k2) {
    return (PVector.dist(k1.location, k2.location) < (k1.radius + k2.radius)/2);
  }




  void update() {
    if (this.parent != this)
      this.c = parent.c;


    //if kreis is too small, remove it
    if (this.radius < 4) 
      removeKreis(this);

    //if kreis too big, let it explode into 3 colors (1 including the original
    if (this.radius > (width + height)/6) {
      Kreis newK1 = new Kreis(this.location.x + random(60), this.location.y + random(60), 1, 30, this);
      Kreis newK2 = new Kreis(this.location.x + random(60), this.location.y + random(60), 1, 30, null);
      Kreis newK3 = new Kreis(this.location.x + random(60), this.location.y + random(60), 1, 30, null);
      allCircles.add(newK1); 
      allCircles.add(newK2); 
      allCircles.add(newK3); 
      newK1.generateChildren(7); 
      newK2.generateChildren(5); 
      newK3.generateChildren(5);
      removeKreis(this);
    }

    //changing parents if radius changes
    if (this.radius > this.parent.radius) {
      //the parent of the parent
      for (int i=0; i<this.parent.childs.size(); i++) {
        Kreis child = this.parent.childs.get(i);
        child.parent = this;
      }
      this.parent.parent = this;
      this.parent = this;
    }

    stroke(this.c);
    strokeWeight(0.4);


    if (random(1900) < 2) {
      if (allCircles.size() < 200)
        generateChildren((int)random(1, 2.1));
    }

    for (int i = 0; i < allCircles.size(); i++) {
      Kreis k = allCircles.get(i);

      //ueberpruefe Kollision
      // kollidiere nur mit anderen Gruppen/Farben
      //bei entfernungen als naechstes noch kinder und eltern überpruefen und fixen, weil verbinden zerstoert werden

      if ( collision(k, this)) {
        if ( !(k == this) && !(this.groupID == k.groupID)) {

          k.direction.set(random(width), random(height), 0);
          this.direction.cross(k.direction );
          Kreis smallerK = (k.radius < this.radius) ? k : this;
          Kreis biggerK = (k.radius >= this.radius) ? k : this;
          smallerK.radius = smallerK.radius - 0.4;
          biggerK.radius = biggerK.radius + 0.3; 

          //chance to destroy the smaller one or assimilate the smaller one
          int r = (int)random(1, 1100);
          if (r < 1000 && !(smallerK.parent == smallerK && biggerK.parent == biggerK) ) {

            //add smaller circle to the bigger circles list and groupID etc
            if (r > 880 ) {
              smallerK.groupID = biggerK.groupID;
              smallerK.c = biggerK.c;
              smallerK.parent = biggerK;
              if (smallerK.parent != smallerK) {
                smallerK.parent.childs.remove(smallerK);
                biggerK.childs.add(smallerK);
              }
              //set the new parent for the childs
              /*for (int j=0; j<k.childs.size(); j++) {
               Kreis child = k.childs.get(j);
               k.parent.childs.add(child);
               child.parent = k.parent;
               }*/
            }
            //else the bigger one eats the smaller one
            else if (r > 810) {
              biggerK.radius = biggerK.radius + smallerK.radius/3;
              removeKreis(smallerK);
              //sonst erstelle neuen kreis/neue gruppe
            } 
            else if (r > 760 && allCircles.size() < 150) {
              Kreis neuerKreis = new Kreis(biggerK.location.x + random(20), biggerK.location.y + random(20), 1, (smallerK.radius + biggerK.radius)/2, null);
              allCircles.add(neuerKreis);
              removeKreis(biggerK);
              neuerKreis.generateChildren((int)random(0, 5));
            }
          }
          // continue;
        }
      }
    }
    //check if childs distance to parent is too far away
    //if so, restrict the current object
    //improvement: might be checked only for each child, for better speed/scalability
    //atm we have two loops. Also might use point objects
    this.influencedByParent = (PVector.dist(this.location, this.parent.location) > maxChildParentDistance) ? true : false;

    if ( this.influencedByParent == true) {
      //draw extra strong line to indicate distance
      stroke(this.c);
      strokeWeight(2);

      this.direction.set(this.parent.location);
      this.direction.cross(this.location);
      this.speed = 0.1;//  = this.parent + random(-20, 20);
      //this.direction.y = 500;//this.direction.y + random(-20, 20);
      //this.direction.add(random(-20, 20),random(-20, 20),0);
    } 
    else
    {
      stroke(this.c);
      strokeWeight(0.3);
    }    
    //draw line to all childs
    if (!(this == this.parent) && !(this.parent.childs.size() == 0))
      line(this.location.x, this.location.y, this.parent.location.x, this.parent.location.y);
    noStroke();

    //check wether going minus or positiv
    //get the distance to use for the position increment
    distance = PVector.sub(this.direction, this.location);

    //if circle is very close to its destination set its coordinates to its destination
    if ( PVector.dist(this.location, this.direction) <= 20.0) {
      //this.direction.set(this.location);
      this.direction.set(random(width), random(height), 0);
      this.speed = 0.3;
    } 
    distance = PVector.sub(this.direction, this.location);
    //move circle

    //this.x = (this.x + (speed*(distX/steps)) ); etc.
    PVector newLocationPart = new PVector();
    newLocationPart.set(this.distance);
    newLocationPart.div(steps);
    newLocationPart.mult(speed);
    this.location.add(newLocationPart);
  }
}

void draw() {
  //System.out.println(allCircles.size());
  background(200);
  //lights();
  //k1.x = mouseX;
  //k1.y = mouseY;



  for (int i = 0; i < allCircles.size(); i++) {
    Kreis k = allCircles.get(i);
    k.update();
    //color the object
    fill(k.c);
    //fill(this.x,this.y,this.z);

    ellipse(k.location.x, k.location.y, k.radius, k.radius);
    /*pushMatrix();
     translate(k.location.x, k.location.y, 0);
     
     sphere(k.radius); 
     popMatrix();*/
  }

  if (mousePressed == true && kreisCreatedAfterMousePressed != null) {
    kreisCreatedAfterMousePressed.radius = kreisCreatedAfterMousePressed.radius+1;
    kreisCreatedAfterMousePressed.location.set((float)mouseX, (float)mouseY, 0);
  }
}

void mousePressed() {

  kreisCreatedAfterMousePressed = new Kreis(mouseX, mouseY, 0, 5, null);
  allCircles.add(kreisCreatedAfterMousePressed);
}



void mouseReleased() {
  kreisCreatedAfterMousePressed.speed = random(1, 3);
  kreisCreatedAfterMousePressed = null;
}

