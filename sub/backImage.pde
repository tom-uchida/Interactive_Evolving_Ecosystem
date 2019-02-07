PImage backImage(){
  PImage img = createImage(width, height, RGB);

  for ( int y = 0; y < height; y++ ) {
    for ( int x = 0; x < width; x++ ) {
      int loc = y*width+x;

      float rad = sqrt( sq(x-0.5*width) + sq(y-0.5*height) );
      float ang = abs( atan2(y-0.5*height, x-0.5*width) );

      float g = 15*noise(ang, 0.01*rad) + 0.07*rad + 0;
      float b = 20*noise(ang, 0.03*rad) + 0.07*rad + 20;
      float rand = random(10+0.04*rad);

      img.pixels[loc] = color(rand,rand+g,rand+b);
    }
  }
  
  return img;
}