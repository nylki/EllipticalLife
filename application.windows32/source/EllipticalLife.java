import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class EllipticalLife extends PApplet {

int steps = 300;
int maxCircles = 300;

//the distance to which children can move autonom.
//if more far away than this var, parent drags children
int maxChildParentDistance = 250;

LinkedList<Kreis> allCircles;
Kreis k1, k2, kreisCreatedAfterMousePressed;
int groupsCount=0;



public void setup() {
  size(1300, 800, P2D);
  smooth(8);
  colorMode(HSB, 1000, 1000, 1000, 100);
  frameRate(50);
  noStroke();
  allCircles = new LinkedList();
  float randomHue = random(0,1000);
  k1 = new Kreis(random(width), random(height), 1, 50, null);
  k2 = new Kreis(random(width), random(height), 1, 50, null);
  k1.c = color(randomHue, random(500,1000), random(600,1000));
  k1.c = color(1000 - randomHue, random(400,1000), random(600,1000)); //making sure, counterpart colors
  allCircles.add(k1);
  allCircles.add(k2);


  k1.direction.set(k2.location);
  k1.generateChildren(4);

  k2.direction.set(k1.location);
  k2.generateChildren(4);
}

//noch bedingungen f\u00fcr verkleinerung eines kreises sp\u00e4ter
public class Kreis {
  int groupID;
  float speed, durchmesser;
  int c;

  //ToDo: using PVector instead of single floats for better readability and performance (vector operations)

  PVector direction, distance, location;

  LinkedList<Kreis> childs;
  Kreis parent;
  boolean influencedByParent = false;

  public void generateChildren(int amount) {
    for (int i=1;i<= amount;i++) {
      Kreis neuerKreis = new Kreis(this.location.x + random(60), this.location.y + random(60), random(1, 4), (this.durchmesser/1.5f), this);
      this.childs.add(neuerKreis); 
      allCircles.add(neuerKreis);
    }
  }

  public void removeKreis(Kreis k) { 
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
    this.durchmesser = r;
    this.parent = p;
    if (p == null)
      this.parent = this;


    //if object has no parent
    //generate new color for itself and its children
    //and set a new groupID for identification
    if (parent == this) {
      this.c = color(random(1000), random(500, 1000), random(600, 1000), 100);
      groupsCount++;
      this.groupID = groupsCount;
    }
    else {
      this.c = parent.c;
      this.groupID = parent.groupID;
    }

    childs = new LinkedList();
  }

  public boolean collision(Kreis k1, Kreis k2) {
    return (PVector.dist(k1.location, k2.location) < (k1.durchmesser + k2.durchmesser)/2);
  }




  public void update() {
    if (this.parent != this)
      this.c = parent.c;


    //if kreis is too small, remove it
    if (this.durchmesser < 4) 
      removeKreis(this);

    //if kreis too big, let it explode into 3 colors (1 including the original
    if (this.durchmesser > (width + height)/7) {
      Kreis newK1 = new Kreis(this.location.x + random(60), this.location.y + random(60), 1, 30, this);
      Kreis newK2 = new Kreis(this.location.x + random(60), this.location.y + random(60), 1, 30, null);
      Kreis newK3 = new Kreis(this.location.x + random(60), this.location.y + random(60), 1, 30, null);
      allCircles.add(newK1); 
      allCircles.add(newK2); 
      allCircles.add(newK3); 
      newK1.generateChildren(2); 
      newK2.generateChildren(4); 
      newK3.generateChildren(5);
      removeKreis(this);
    }

    //changing parents if durchmesser changes
    if (this.durchmesser > this.parent.durchmesser) {
      //the parent of the parent
      for (int i=0; i<this.parent.childs.size(); i++) {
        Kreis child = this.parent.childs.get(i);
        child.parent = this;
      }
      this.parent.parent = this;
      this.parent = this;
    }

    stroke(this.c);
    strokeWeight(0.4f);


    if (random(1700) < 2) {
      if (allCircles.size() < maxCircles)
        generateChildren((int)random(1, 2.1f));
    }

    for (int i = 0; i < allCircles.size(); i++) {
      Kreis k = allCircles.get(i);

      //ueberpruefe Kollision
      // kollidiere nur mit anderen Gruppen/Farben
      //bei entfernungen als naechstes noch kinder und eltern \u00fcberpruefen und fixen, weil verbinden zerstoert werden

      if ( collision(k, this)) {
        if ( !(k == this) && !(this.groupID == k.groupID)) {

          k.direction.set(random(width), random(height), 0);
          this.direction.cross(k.direction );
          Kreis smallerK = (k.durchmesser < this.durchmesser) ? k : this;
          Kreis biggerK = (k.durchmesser >= this.durchmesser) ? k : this;
          smallerK.durchmesser = smallerK.durchmesser - smallerK.durchmesser/60;
          biggerK.durchmesser = biggerK.durchmesser + smallerK.durchmesser/60; 

          //chance to destroy the smaller one or assimilate the smaller one
          int r = (int)random(1, 1100);
          if (r < 1000 && !(smallerK.parent == smallerK && biggerK.parent == biggerK) ) {

            //add smaller circle to the bigger circles list and groupID etc
            if (r > 860 ) {
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
            else if (r > 790) {
              biggerK.durchmesser = biggerK.durchmesser + smallerK.durchmesser/2.5f;
              removeKreis(smallerK);
              //sonst erstelle neuen kreis/neue gruppe
            } 
            else if (r > 760 && allCircles.size() < 150 && abs(k.durchmesser - this.durchmesser) < 30) {
              Kreis neuerKreis = new Kreis(biggerK.location.x + random(20), biggerK.location.y + random(20), 1, (smallerK.durchmesser + biggerK.durchmesser)/2, null);
              Kreis neuerKreis2 = new Kreis(biggerK.location.x + random(20), biggerK.location.y + random(20), 1, (smallerK.durchmesser + biggerK.durchmesser)/3, null);
              allCircles.add(neuerKreis);
              removeKreis(biggerK);
              neuerKreis.generateChildren((int)random(1, 3));
              neuerKreis2.generateChildren((int)random(1, 3));
            }
            else if(r > 750){
              removeKreis(k);
            }
          }
           //continue;
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
      this.speed = 0.2f;//  = this.parent + random(-20, 20);
      //this.direction.y = 500;//this.direction.y + random(-20, 20);
      //this.direction.add(random(-20, 20),random(-20, 20),0);
    } 
    else
    {
      stroke(this.c);
      strokeWeight(0.3f);
    }    
    //draw line to all childs
    if (!(this == this.parent) && !(this.parent.childs.size() == 0))
      line(this.location.x, this.location.y, this.parent.location.x, this.parent.location.y);
    noStroke();

    //check wether going minus or positiv
    //get the distance to use for the position increment
    distance = PVector.sub(this.direction, this.location);

    //if circle is very close to its destination set its coordinates to its destination
    if ( PVector.dist(this.location, this.direction) <= 20.0f) {
      //this.direction.set(this.location);
      this.direction.set(random(width), random(height), 0);
      this.speed = 0.3f;
    } 
    distance = PVector.sub(this.direction, this.location);
    //move circle

    //this.x = (this.x + (speed*(distX/steps)) ); etc.
    PVector newLocationPart = new PVector();
    newLocationPart.set(this.distance);
    newLocationPart.div(steps);
    newLocationPart.mult(speed);
    this.location.add(newLocationPart);
    //this.location.x += random(-0.7, 0.7) * noise( this.location.x,  this.location.y);
    //this.location.y += random(-0.7, 0.7) * noise( this.location.x,  this.location.y);
    
  }
}

public void draw() {
  //System.out.println(allCircles.size());
  background(250);
  //lights();

  for (int i = 0; i < allCircles.size(); i++) {
    Kreis k = allCircles.get(i);
    k.update();
    //color the object
    fill(k.c);
    //fill(this.x,this.y,this.z);

    ellipse(k.location.x, k.location.y, k.durchmesser, k.durchmesser);
    /*pushMatrix();
     translate(k.location.x, k.location.y, 0);
     
     sphere(k.durchmesser); 
     popMatrix();*/
  }

  if (mousePressed == true && kreisCreatedAfterMousePressed != null) {
    kreisCreatedAfterMousePressed.durchmesser = kreisCreatedAfterMousePressed.durchmesser+1;
    kreisCreatedAfterMousePressed.location.set((float)mouseX, (float)mouseY, 0);
  }
}

public void mousePressed() {

  kreisCreatedAfterMousePressed = new Kreis(mouseX, mouseY, 0, 5, null);
  allCircles.add(kreisCreatedAfterMousePressed);
}



public void mouseReleased() {
  kreisCreatedAfterMousePressed.speed = random(1, 3);
  kreisCreatedAfterMousePressed = null;
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "EllipticalLife" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
